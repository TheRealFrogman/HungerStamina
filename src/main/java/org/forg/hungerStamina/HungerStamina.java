package org.forg.hungerStamina;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;

public class HungerStamina extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    private static final int HUNGER_DEPLETION_RATE_TICKS = 10;
    private static final int DEPLETION_STEP = 2;
    private static final HashMap<Player, BukkitRunnable> Hunger_Depletion_Tasks = new HashMap<>();

    private static final int COOLDOWN_UNTIL_RESTORATION_TICKS = 100;
    private static final int HUNGER_RESTORATION_RATE_TICKS = 3;
    private static final int RESTORE_STEP = 1;
    private static final HashMap<Player, BukkitRunnable> Hunger_Restore_Tasks = new HashMap<>();

    @EventHandler
    void onPlayerSprintToggle(PlayerToggleSprintEvent e) {
        Player p = e.getPlayer();
        if(p.getGameMode().name().equalsIgnoreCase("creative")) return;
        if(p.isSprinting()) {
            if (!Hunger_Restore_Tasks.containsKey(p)) {

                Hunger_Restore_Tasks.put(p, new BukkitRunnable() {
                    public void run() { AlterHunger(p, RESTORE_STEP); }
                });
                Hunger_Restore_Tasks.get(p).runTaskTimer(this, COOLDOWN_UNTIL_RESTORATION_TICKS, HUNGER_RESTORATION_RATE_TICKS);
            }
            if (Hunger_Depletion_Tasks.containsKey(p)) {
                Hunger_Depletion_Tasks.get(p).cancel();
                Hunger_Depletion_Tasks.remove(p);
            }
        } else {
            if (Hunger_Restore_Tasks.containsKey(p)) {
                Hunger_Restore_Tasks.get(p).cancel();
                Hunger_Restore_Tasks.remove(p);
            }
            if (!Hunger_Depletion_Tasks.containsKey(p)) {
                Hunger_Depletion_Tasks.put(p,new BukkitRunnable() {
                    public void run() { AlterHunger(p, -DEPLETION_STEP);}
                });
                Hunger_Depletion_Tasks.get(p).runTaskTimer(this,0, HUNGER_DEPLETION_RATE_TICKS);
            }
        }
    }

    private void AlterHunger(Player target, int amount){
        final int MAX_FOOD_LEVEL = 20;

        int newFoodLevel = target.getFoodLevel() + amount;
        newFoodLevel = Math.max(0, Math.min(newFoodLevel, MAX_FOOD_LEVEL));
        target.setFoodLevel(newFoodLevel);
    }

    @EventHandler
    void onPlayerQuit(PlayerQuitEvent e) {
        Hunger_Restore_Tasks.remove(e.getPlayer());
        Hunger_Depletion_Tasks.remove(e.getPlayer());
    }
}
