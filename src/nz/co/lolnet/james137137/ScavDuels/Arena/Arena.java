/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.ScavDuels.Arena;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import nz.co.lolnet.james137137.ScavDuels.Config;
import nz.co.lolnet.james137137.ScavDuels.ScavDuels;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author James
 */
public class Arena {

    ScavDuelsPlayer player1, player2;
    public String name;
    Location spawnLocation1, spawnLocation2;
    public boolean isRunning;

    public static List<Arena> getAllArenas() {
        List<Arena> ArenaList = new ArrayList<>();
        List<String> list = getList();
        if (list != null) {

            for (String string : list) {
                System.out.println(string);
                ArenaList.add(ScavDuels.LoadFromDisk(string));
            }
        }

        return ArenaList;
    }

    public static List<String> getList() {
        File arenaFileLocationPath = Config.getArenaFileLocationPath();
        File arenadir = new File(arenaFileLocationPath, "arenas");
        if (!arenadir.exists()) {
            arenadir.mkdir();
            return null;
        }
        File[] listFiles = arenadir.listFiles();
        System.out.println(arenaFileLocationPath);
        System.out.println(arenadir.getAbsolutePath());
        List<String> output = new ArrayList();
        for (File file : listFiles) {
            output.add(file.getName());
        }

        return output;
    }

    public Arena(String name, Location spawnLocation1, Location spawnLocation2, boolean isRunning) {
        this.name = name.replaceAll(".yml", "");
        this.spawnLocation1 = spawnLocation1;
        this.spawnLocation2 = spawnLocation2;
        this.isRunning = isRunning;
        System.out.println("ArenaName" + this.name);
    }

    public Arena() {
    }

    public void StopArena() {
        this.isRunning = false;
        if (this.isRunning) {
            player1.EndDuelOnStop();
            player2.EndDuelOnStop();
        }

    }

    public void StopArenaOnLeave(ScavDuelsPlayer leaver) {
        this.isRunning = false;
        player1.EndDuelOnLeave(leaver);
        player2.EndDuelOnLeave(leaver);
    }

    public ScavDuelsPlayer getPlayer1() {
        return player1;
    }

    public ScavDuelsPlayer getPlayer2() {
        return player2;
    }

    public Location getSpawnLocation1() {
        return spawnLocation1;
    }

    public Location getSpawnLocation2() {
        return spawnLocation2;
    }

    public boolean setSpawnLocation(Player player, int i) {
        if (i == 1) {
            spawnLocation1 = player.getLocation();
            return true;
        } else if (i == 2) {
            spawnLocation2 = player.getLocation();
            return true;
        }
        return false;
    }

    public boolean SaveToDisk() {
        if (spawnLocation1 == null || spawnLocation2 == null) {
            return false;
        }
        File arenaFileLocationPath = Config.getArenaFileLocationPath();
        File arenadir = new File(arenaFileLocationPath, "arenas");
        if (!arenadir.exists()) {
            arenadir.mkdir();
        }
        File ArenaFile = new File(arenadir, name + ".yml");
        try {
            ArenaFile.createNewFile();
            PrintWriter out = new PrintWriter(ArenaFile);
            out.append("world: " + spawnLocation1.getWorld().getName());
            out.append("\n");
            out.append("x1: " + spawnLocation1.getX());
            out.append("\n");
            out.append("y1: " + spawnLocation1.getY());
            out.append("\n");
            out.append("z1: " + spawnLocation1.getZ());
            out.append("\n");
            out.append("yaw1: " + spawnLocation1.getYaw());
            out.append("\n");
            out.append("pitch1: " + spawnLocation1.getPitch());
            out.append("\n");
            out.append("x2: " + spawnLocation2.getX());
            out.append("\n");
            out.append("y2: " + spawnLocation2.getY());
            out.append("\n");
            out.append("z2: " + spawnLocation2.getZ());
            out.append("\n");
            out.append("yaw2: " + spawnLocation2.getYaw());
            out.append("\n");
            out.append("pitch2: " + spawnLocation2.getPitch());
            out.append("\n");
            out.close();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    public void setuptDuel(ScavDuelsPlayer dPlayer1, ScavDuelsPlayer dPlayer2) {
        this.isRunning = true;
        dPlayer1.sendMessage("Starting Game");
        dPlayer2.sendMessage("Starting Game");
        this.player1 = dPlayer1;
        this.player2 = dPlayer2;
        player1.lastLocation = player1.getPlayer().getLocation();
        player1.teleport(this.getSpawnLocation1());
        player2.lastLocation = player2.getPlayer().getLocation();
        player2.teleport(this.getSpawnLocation2());
        StartMatch();

    }

    private void StartMatch() {
        player1.canMove = false;
        player2.canMove = false;
        new Arena.ThreadCountDown(player1, player2);
    }

    private static class ThreadCountDown implements Runnable {

        ScavDuelsPlayer player1;
        ScavDuelsPlayer player2;
        int countDownTime;

        private ThreadCountDown(ScavDuelsPlayer player1, ScavDuelsPlayer player2) {
            this.player1 = player1;
            this.player2 = player2;
            this.countDownTime = Config.getCountDownTime();
            start();
        }

        private void start() {
            Thread t = new Thread(this);
            t.start();
        }

        @Override
        public void run() {
            player1.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, countDownTime*20, 100));
            player2.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.SLOW, countDownTime*20, 100));
            sendMessage("Duel Starts in " + countDownTime + " Secounds.");
            for (int i = countDownTime; i > 0; i--) {
                if (player1.isDueling && player2.isDueling) {
                    sendMessage("" + i);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Arena.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    return;
                }

            }
            player1.getPlayer().removePotionEffect(PotionEffectType.SLOW);
            player2.getPlayer().removePotionEffect(PotionEffectType.SLOW);
            sendMessage("FIGHT!");
        }

        private void sendMessage(String message) {
            player1.sendMessage(message);
            player2.sendMessage(message);
        }
    }

}
