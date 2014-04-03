/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package nz.co.lolnet.james137137.ScavDuels;

import java.io.File;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author James
 */
public class Config {

    public static boolean HealPlayerOnForceStop;
    private static File myPluginFolder;
    private static long MaxWaitTimeToAccept;
    private static int CountDownTime;
    private static boolean KillLeaver;
    public static long maxTimeForWinerToLoot;
    public static long maxMatchTime;

    public static File getArenaFileLocationPath() {
        return myPluginFolder;
    }

    public static long getMaxMatchTime() {
        return maxMatchTime;
    }
    

    static long getMaxWaitTimeToAccept() {
        return MaxWaitTimeToAccept;
    }

    public static int getCountDownTime() {
        return CountDownTime;
    }

    public static boolean KillLeaver() {
        return KillLeaver;
    }
    public Config(ScavDuels plugin) {
        plugin.saveDefaultConfig();
        FileConfiguration config = plugin.getConfig();
        myPluginFolder = plugin.getDataFolder();
        
        
        
        HealPlayerOnForceStop = config.getBoolean("HealPlayerOnForceStop");
        MaxWaitTimeToAccept = config.getLong("MaxWaitTimeToAccept")*1000;
        CountDownTime = config.getInt("CountDownTime");
        KillLeaver = config.getBoolean("KillLeaver");
        maxTimeForWinerToLoot = config.getLong("maxTimeForWinerToLoot");
        maxMatchTime = config.getLong("maxMatchTime");
    }
    
}
