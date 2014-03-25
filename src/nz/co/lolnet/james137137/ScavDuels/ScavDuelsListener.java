/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nz.co.lolnet.james137137.ScavDuels;

import nz.co.lolnet.james137137.ScavDuels.Arena.Arena;
import nz.co.lolnet.james137137.ScavDuels.Arena.ScavDuelsPlayer;
import static nz.co.lolnet.james137137.ScavDuels.ScavDuels.ArenaList;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 *
 * @author James
 */
class ScavDuelsListener implements Listener {

    ScavDuels plugin;

    public ScavDuelsListener(ScavDuels aThis) {
        plugin = aThis;
    }
    
    @EventHandler
    protected void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        
        if (event.isCancelled())
        {
            return;
        }
        Player player = event.getPlayer();
        ScavDuelsPlayer dPlayer2 = ScavDuels.scavDuelsPlayer.get(player.getName());
        if (dPlayer2 != null && dPlayer2.isDueling) {
            String[] split = event.getMessage().split(" ");
            if (!split[0].equalsIgnoreCase("/duel") && !event.getPlayer().hasPermission("ScavDuels.duel.bypassCommands"))
            {
                event.getPlayer().sendMessage("You in a duel. Type /duel Leave to leave");
                event.setCancelled(true);
                return;
            }
        }
        
        
        
    }

    @EventHandler
    protected void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ScavDuels.scavDuelsPlayer.put(player.getName(), new ScavDuelsPlayer(player));
    }

    @EventHandler
    protected void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ScavDuelsPlayer dPlayer2 = ScavDuels.scavDuelsPlayer.get(player.getName());
        if (dPlayer2 != null && dPlayer2.isDueling) {
            Arena arena = null;
            for (Arena tempArena : ArenaList) {
                if (tempArena.name.equalsIgnoreCase(dPlayer2.ArenaName)) {
                    arena = tempArena;
                    break;
                }
            }
            if (arena == null || !arena.isRunning) {
                dPlayer2.isDueling = false;
            }
            else{
                arena.StopArenaOnLeave(dPlayer2);
            }
            
            ScavDuels.scavDuelsPlayer.remove(player.getName());
            
        }
    }

    @EventHandler
    protected void onPlayerDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            Player player = (Player) entity;
            ScavDuelsPlayer dPlayer2 = ScavDuels.scavDuelsPlayer.get(player.getName());
            if (dPlayer2 != null && dPlayer2.isDueling) {
                Arena arena = null;
                for (Arena tempArena : ArenaList) {
                    if (tempArena.name.equalsIgnoreCase(dPlayer2.ArenaName)) {
                        arena = tempArena;
                        break;
                    }
                }
                if (arena == null || !arena.isRunning) {
                    dPlayer2.isDueling = false;
                    return;
                }

                arena.StopArenaOnDeath(dPlayer2);
            }
        }

    }

}
