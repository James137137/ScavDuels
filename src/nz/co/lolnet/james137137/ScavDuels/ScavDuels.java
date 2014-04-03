/*
 * This program/Plugin was created by James Anderson
 */
package nz.co.lolnet.james137137.ScavDuels;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import nz.co.lolnet.james137137.ScavDuels.Arena.Arena;
import nz.co.lolnet.james137137.ScavDuels.Arena.ScavDuelsPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author James
 */
public class ScavDuels extends JavaPlugin {

    static List<Arena> ArenaList;
    public static HashMap<String, ScavDuelsPlayer> scavDuelsPlayer;
    private static ScavDuels plugin;
    public static long nextMatchID;

    public static Plugin getMyPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        nextMatchID = 1;
        plugin = this;
        Config config = new Config(this);
        scavDuelsPlayer = new HashMap<>();
        ArenaList = Arena.getAllArenas();
        getServer().getPluginManager().registerEvents(new ScavDuelsListener(this), this);

    }

    @Override
    public void onDisable() {
        for (Arena arena : ArenaList) {
            arena.StopArena();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String commandName = command.getName();
        if (commandName.equalsIgnoreCase("Duel")) {
            if (sender.hasPermission("ScavDuels.command.Duel")) {

                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("help")) {
                        sender.sendMessage(ChatColor.GOLD + "Commands");
                        sender.sendMessage("/duel status or /deul list - Displays all Arenas and if they are avilable.");
                        sender.sendMessage("/duel playerName ArenaName - send an invite to duel a player");
                        sender.sendMessage("/duel Accept PlayerName - Accept an Invite to duel a player");
                        sender.sendMessage("/deul leave - Leave an arena during or after a duel");
                        sender.sendMessage("(note: that while dueling (other player is alive) leaving will be counted as a loss, and possibly death)");
                    } else if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("list")) {
                        if (ArenaList.isEmpty()) {
                            sender.sendMessage("There are no Arenas made yet");
                        } else {
                            for (Arena arena : ArenaList) {
                                if (arena.isRunning) {
                                    sender.sendMessage(ChatColor.GOLD + arena.name + ": " + arena.getPlayer1().getPlayer().getName() + " VS " + arena.getPlayer2().getPlayer().getName());
                                } else {
                                    sender.sendMessage(ChatColor.GOLD + arena.name + ":" + ChatColor.GREEN + " Available");
                                }
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("accept")) {
                        if (sender instanceof Player) {
                            Player player2 = (Player) sender;
                            ScavDuelsPlayer dPlayer2 = scavDuelsPlayer.get(player2.getName());
                            if (dPlayer2 == null) {

                                dPlayer2 = new ScavDuelsPlayer(player2);
                                sender.sendMessage("No one has sent you an invite. try /duel PlayerName");
                                scavDuelsPlayer.put(player2.getName(), dPlayer2);
                                return true;
                            }
                            if (dPlayer2.isDueling) {
                                sender.sendMessage("you are already in a duel. Type /duel Leave to leave");
                                return true;
                            }
                            String player1Name = dPlayer2.inviteName;
                            if (player1Name == null) {
                                sender.sendMessage("No one has sent you an invite. try /duel PlayerName");
                                return true;
                            }
                            if (player2.getVehicle() != null || player2.isSleeping()) {
                                sender.sendMessage("You can't accept a duel while riding or sleeping");
                                return true;
                            }
                            Player player1 = this.getServer().getPlayer(player1Name);
                            if (player1.getVehicle() != null || player1.isSleeping()) {
                                sender.sendMessage("Other player is riding or sleeping. Please try again later");
                                player1.sendMessage("It seems you are riding or in a bed. So you can't fight " + player2.getName() + " yet.");
                                return true;
                            }
                            if (player1 == null || !player1.isOnline()) {
                                sender.sendMessage("Player is offline");
                                return true;
                            } else {
                                if (dPlayer2.inviteTime + Config.getMaxWaitTimeToAccept() >= System.currentTimeMillis()) {
                                    ScavDuelsPlayer dPlayer1 = scavDuelsPlayer.get(player1Name);
                                    if (dPlayer1.isDueling) {
                                        sender.sendMessage("It seems that player is already fighting");
                                        return true;
                                    }
                                    if (dPlayer1.invitedName.equalsIgnoreCase(player1Name)) {
                                        Arena arena = null;
                                        for (Arena tempArena : ArenaList) {
                                            if (tempArena.name.equalsIgnoreCase(dPlayer1.ArenaName)) {
                                                arena = tempArena;
                                                break;
                                            }
                                        }
                                        if (arena == null) {
                                            sender.sendMessage("It seems that the Arena is now missing. : " + dPlayer1.ArenaName);
                                        } else {
                                            if (arena.isRunning) {
                                                sender.sendMessage("players are already in the arena, please untill it is finished");
                                                return true;
                                            }
                                            dPlayer1.isDueling = true;
                                            dPlayer2.isDueling = true;
                                            arena.setuptDuel(dPlayer1, dPlayer2);

                                        }
                                    }
                                } else {
                                    sender.sendMessage("Invite time to accept has passed");
                                }

                            }
                        } else {
                            sender.sendMessage(ChatColor.RED + "you are not a player");
                        }
                    } else if (args[0].equalsIgnoreCase("leave")) {
                        if (sender instanceof Player) {
                            Player player = (Player) sender;
                            ScavDuelsPlayer dPlayer2 = scavDuelsPlayer.get(player.getName());
                            if (dPlayer2 == null) {

                                dPlayer2 = new ScavDuelsPlayer(player);
                                sender.sendMessage("No are not in a duel");
                                scavDuelsPlayer.put(player.getName(), dPlayer2);
                                return true;
                            }
                            if (dPlayer2.canLeaveAfterWinning) {
                                dPlayer2.TeleportBack();
                                dPlayer2.canLeaveAfterWinning = false;
                                for (Arena tempArena : ArenaList) {
                                    System.out.println(dPlayer2.ArenaName);
                                    if (tempArena.name.equalsIgnoreCase(dPlayer2.ArenaName)) {
                                        tempArena.player1 = null;
                                        tempArena.player2 = null;
                                        break;
                                    }
                                }
                                return true;
                            }
                            if (!dPlayer2.isDueling) {
                                sender.sendMessage("No are not in a duel");
                                return true;
                            }
                            Arena arena = null;
                            for (Arena tempArena : ArenaList) {
                                System.out.println(dPlayer2.ArenaName);
                                if (tempArena.name.equalsIgnoreCase(dPlayer2.ArenaName)) {
                                    arena = tempArena;
                                    break;
                                }
                            }
                            if (arena == null) {
                                sender.sendMessage("Arena == null. this isn't good... did an Admin remove the arena?");
                                return true;
                            }
                            if (!arena.isRunning) {
                                sender.sendMessage("Arena is not running a duel");
                                return true;
                            }
                            arena.StopArenaOnLeave(dPlayer2);

                        } else {
                            sender.sendMessage(ChatColor.RED + "you are not a player");
                        }
                    } else {
                        String playerName = args[0];
                        Player player2 = this.getServer().getPlayer(playerName);
                        if (player2 != null && player2.isOnline()) {
                            if (sender instanceof Player) {
                                if (sender.getName().equals(playerName)) {
                                    sender.sendMessage(ChatColor.RED + "You can't vs yourself...");
                                    return true;
                                }
                                if (args.length < 2) {
                                    sender.sendMessage("Arena name missing. please go /duel player arenaName");
                                    return true;
                                }
                                Player player1 = (Player) sender;
                                String arenaName = args[1];

                                Arena arena = null;
                                for (Arena tempArena : ArenaList) {
                                    if (tempArena.name.equalsIgnoreCase(arenaName)) {
                                        arena = tempArena;
                                        break;
                                    }
                                }
                                if (arena == null) {
                                    sender.sendMessage("It seems that the Arena is now missing. (check the spelling)");
                                    return true;
                                } else {
                                    if (arena.isRunning) {
                                        sender.sendMessage("players are already in the arena, please until it is finished");
                                        return true;
                                    }
                                }

                                ScavDuelsPlayer dPlayer2 = scavDuelsPlayer.get(player2.getName());
                                if (dPlayer2 == null) {

                                    dPlayer2 = new ScavDuelsPlayer(player2);
                                    scavDuelsPlayer.put(player2.getName(), dPlayer2);
                                }
                                if (dPlayer2.isDueling) {
                                    sender.sendMessage("you are already in a duel. Type /duel Leave to leave");
                                    return true;
                                }
                                dPlayer2.inviteName = player1.getName();
                                dPlayer2.inviteTime = System.currentTimeMillis();
                                ScavDuelsPlayer dPlayer1 = scavDuelsPlayer.get(player1.getName());
                                if (dPlayer1 == null) {

                                    dPlayer1 = new ScavDuelsPlayer(player1);
                                    scavDuelsPlayer.put(player1.getName(), dPlayer1);
                                }
                                if (dPlayer1.isDueling) {
                                    sender.sendMessage("That player is already in a duel.");
                                    return true;
                                }
                                dPlayer1.ArenaName = args[1];
                                System.out.println(dPlayer1.ArenaName);
                                dPlayer1.invitedName = player1.getName();
                                dPlayer1.invitedTime = System.currentTimeMillis();
                                dPlayer1.sendMessage("request sent.");
                                dPlayer2.sendMessage("You have been invited to duel " + dPlayer1.getPlayer().getName() + " at Arena: " + dPlayer1.ArenaName + ". Go /duel accept " + dPlayer1.getPlayer().getName());

                            }
                        } else {
                            sender.sendMessage("Can't find that player.");
                        }
                    }
                } else {
                    sender.sendMessage("Args missing. type /duel help");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run that command.");
                return true;
            }

        } else if (commandName.equalsIgnoreCase("DuelAdmin")) {

            if (sender.hasPermission("ScavDuels.command.DuelAdmin")) {
                if (args.length >= 1) {
                    if (args[0].equalsIgnoreCase("create")) {
                        if (args.length < 2) {
                            sender.sendMessage("Arena Name missing");
                            return true;
                        }
                        String arenaName = args[1];
                        Arena arena = null;
                        for (Arena tempArena : ArenaList) {
                            if (tempArena.name.equalsIgnoreCase(arenaName)) {
                                arena = tempArena;
                                break;
                            }
                        }
                        if (arena == null) {

                            arena = new Arena();
                            arena.name = arenaName;
                            sender.sendMessage("Arena created.");
                            sender.sendMessage("Debug: saving to disk: " + arena.SaveToDisk());
                            ArenaList.add(arena);
                        } else {
                            sender.sendMessage("Arena Already exist.");
                        }

                    } else if (args[0].equalsIgnoreCase("setpos")) {
                        if (args.length < 3) {
                            sender.sendMessage("args Missing");
                        }
                        String arenaName = args[1];
                        Arena arena = null;
                        for (Arena tempArena : ArenaList) {
                            if (tempArena.name.equalsIgnoreCase(arenaName)) {
                                arena = tempArena;
                                break;
                            }
                        }
                        if (arena == null) {
                            sender.sendMessage("Arena not created yet");
                            return true;
                        } else {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;

                                arena.setSpawnLocation(player, Integer.parseInt(args[2]));

                                sender.sendMessage("Spawn set.");
                                sender.sendMessage("Debug: saving to disk: " + arena.SaveToDisk());
                            }
                        }
                    } else if (args[0].equalsIgnoreCase("stopall")) {
                        for (Arena arena : ArenaList) {
                            arena.StopArena();
                        }
                    } else if (args[0].equalsIgnoreCase("teleport")) {
                        if (args.length < 3) {
                            sender.sendMessage("Please use /DuelAdmin teleport ArenaName 1or2");
                            return true;
                        }

                        String arenaName = args[1];
                        Arena arena = null;
                        for (Arena tempArena : ArenaList) {
                            if (tempArena.name.equalsIgnoreCase(arenaName)) {
                                arena = tempArena;
                                break;
                            }
                        }
                        if (arena == null) {
                            sender.sendMessage("Arena not created yet");
                            return true;
                        } else {
                            if (sender instanceof Player) {
                                Player player = (Player) sender;
                                int playerNumber = Integer.parseInt(args[2]);
                                arena.teleport(player, playerNumber);
                            }
                        }
                        return true;
                    }
                } else {
                    sender.sendMessage("args missing.");
                    sender.sendMessage("commands are:");
                    sender.sendMessage("/DuelAdmin create ArenaName");
                    sender.sendMessage("/DuelAdmin setpos ArenaName 1/2");
                    sender.sendMessage("/DuelAdmin StopAll");
                    sender.sendMessage("/DuelAdmin teleport ArenaName 1/2");

                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have permission to run that command.");
                return true;
            }

        }

        return false;
    }

    public static Arena LoadFromDisk(String name) {
        File arenaFileLocationPath = Config.getArenaFileLocationPath();
        File arenadir = new File(arenaFileLocationPath, "arenas");
        if (!arenadir.exists()) {
            arenadir.mkdir();
        }
        File ArenaFile = new File(arenadir, name);
        if (!ArenaFile.exists()) {
            return null;
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader(ArenaFile));
            String arenaName;
            String world;
            double x1, y1, z1;
            float yaw1, pitch1;
            double x2, y2, z2;
            float yaw2, pitch2;
            world = in.readLine().substring(7);
            String tempxyz;;
            tempxyz = in.readLine().substring(4);
            x1 = Double.parseDouble(tempxyz);
            tempxyz = in.readLine().substring(4);
            y1 = Double.parseDouble(tempxyz);
            tempxyz = in.readLine().substring(4);
            z1 = Double.parseDouble(tempxyz);
            tempxyz = in.readLine().substring(6);
            yaw1 = Float.parseFloat(tempxyz);
            tempxyz = in.readLine().substring(8);
            pitch1 = Float.parseFloat(tempxyz);
            tempxyz = in.readLine().substring(4);
            x2 = Double.parseDouble(tempxyz);
            tempxyz = in.readLine().substring(4);
            y2 = Double.parseDouble(tempxyz);
            tempxyz = in.readLine().substring(4);
            z2 = Double.parseDouble(tempxyz);
            tempxyz = in.readLine().substring(6);
            yaw2 = Float.parseFloat(tempxyz);
            tempxyz = in.readLine().substring(8);
            pitch2 = Float.parseFloat(tempxyz);
            in.close();

            return new Arena(name, new Location(Bukkit.getWorld(world), x1, y1, z1, yaw1, pitch1),
                    new Location(Bukkit.getWorld(world), x2, y2, z2, yaw2, pitch2), false);
        } catch (FileNotFoundException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }

}
