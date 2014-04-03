/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.ScavDuels.Arena;

import nz.co.lolnet.james137137.ScavDuels.Config;
import nz.co.lolnet.james137137.ScavDuels.ScavDuels;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author James
 */
public class ScavDuelsPlayer {

    private final Player player;
    public boolean isDueling;
    boolean canMove;
    boolean canRunCommands;
    Location lastLocation;
    public String inviteName;
    public String invitedName;
    public long inviteTime;
    public long invitedTime;
    public String ArenaName;
    public boolean canLeaveAfterWinning = false;
    public boolean MatchHasStarted;
    long currentMatchID;

    public ScavDuelsPlayer(Player player) {
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    void EndDuelOnStop() {
        this.isDueling = false;
        this.TeleportBack();
        getPlayer().sendMessage(ChatColor.GOLD + "The duel has been stoped by the server or an Admin");
        if (Config.HealPlayerOnForceStop) {
            getPlayer().setHealth(getPlayer().getMaxHealth());
        }
    }

    public void EndDuelOnLeave(ScavDuelsPlayer leaver) {
        this.isDueling = false;
        if (Config.KillLeaver() && leaver.getPlayer().getHealth() > 0.0)
        {
            leaver.getPlayer().setHealth(0.0);
        }
        if (!this.player.equals(leaver.player))
        {
            player.sendMessage("You have " + Config.maxTimeForWinerToLoot + " Secounds to collect the loot. Type /duel leave when finished");
            canLeaveAfterWinning = true;
            Bukkit.getServer().getScheduler().runTaskLater(ScavDuels.getMyPlugin(), new Runnable() {

            @Override
            public void run() {
                TeleportBack();
            }
        }, Config.maxTimeForWinerToLoot * 20);
        }
        
        
    }

    public void TeleportBack() {
        // added teleport 2 times to prevent back.
        this.getPlayer().teleport(lastLocation);
        this.getPlayer().teleport(lastLocation);
        canLeaveAfterWinning = false;
    }

    public void sendMessage(String message) {
        getPlayer().sendMessage(message);
    }

    void teleport(Location spawnLocation) {
        getPlayer().teleport(spawnLocation);
    }

    public void EndDuelOnDeath(ScavDuelsPlayer Losser) {
        this.isDueling = false;
        if (!this.player.equals(Losser.player))
        {
            player.sendMessage("You have " + Config.maxTimeForWinerToLoot + " Secounds to collect the loot. Type /duel leave when finished");
            canLeaveAfterWinning = true;
            Bukkit.getServer().getScheduler().runTaskLater(ScavDuels.getMyPlugin(), new Runnable() {

            @Override
            public void run() {
                TeleportBack();
            }
        }, Config.maxTimeForWinerToLoot * 20);
        }
    }

    void endMatchOnMaxTimeReached() {
        this.isDueling = false;
        player.sendMessage("Arena has ended due to that maximum time the duel can be has reached.");
        TeleportBack();
        player.setHealth(player.getMaxHealth());
        
    }

    

}
