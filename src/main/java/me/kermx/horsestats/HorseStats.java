package me.kermx.horsestats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class HorseStats extends JavaPlugin implements Listener {
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private int cooldownConfig;
    private String statsItem;

    @Override
    public void onEnable() {
        // Plugin startup logic
        saveDefaultConfig();
        reloadConfig();
        cooldownConfig = getConfig().getInt("HorseStats.cooldown");
        statsItem = getConfig().getString("HorseStats.statsItem");
        Objects.requireNonNull(getCommand("horsestats")).setExecutor(new ReloadCommand(this));
        getServer().getPluginManager().registerEvents(this,this);

        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + ">>" + ChatColor.GREEN + " HorseStats enabled successfully");

    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (cooldowns.containsKey(player.getUniqueId())){
            int COOLDOWN_SECONDS = cooldownConfig;
            long timeLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + COOLDOWN_SECONDS) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0){
                player.sendMessage(ChatColor.RED + "You must wait " + timeLeft + " seconds before checking horse stats again!");
                event.setCancelled(true);
                return;
            }
        }

        if (player.isSneaking() || player.getInventory().getItemInMainHand().getType() != Material.valueOf(statsItem)) {
            return;
        }

        if (entity instanceof Horse && !player.isSneaking() && player.getInventory().getItemInMainHand().getType() == Material.valueOf(statsItem)) {
            Horse horse = (Horse) entity;
            double horseCurrentHealth = horse.getHealth();
            double horseMaxHealth = Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
            double horseSpeed = Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getValue() * 42.157787584D;
            double jumpStat = horse.getJumpStrength();
            double maxJumpHeight = (-0.1817584952 * jumpStat * jumpStat * jumpStat) + (3.689713992 * jumpStat * jumpStat) + (2.128599134 * jumpStat) + -0.343930367;
            double roundedHorseSpeed = Math.round(horseSpeed * 1000) / 1000.0;
            double roundedMaxJumpHeight = Math.round(maxJumpHeight * 1000) / 1000.0;
            double roundedHorseMaxHealth = Math.round(horseMaxHealth * 10) / 10.0;
            double roundedHorseCurrentHealth = Math.round(horseCurrentHealth * 10) / 10.0;
            String color = horse.getColor().name().toLowerCase().replace("_", " ");
            String style = horse.getStyle().name().toLowerCase().replace("_", " ");

            String horseOwner;
            if (horse.isTamed()){
                horseOwner = horse.getOwner().getName();
            } else {
                horseOwner = "not tamed";
            }

            String horseName;
            if (horse.getCustomName() != null){
                horseName = horse.getCustomName();
            } else {
                horseName = "no name";
            }

            String horseAge;
            if (horse.getAge() >= 0){
                horseAge = "adult";
            }else {
                horseAge = Math.abs(horse.getAge() / 20) + " secs";
            }

            String breedableTimer;
            if (horse.getAge() > 0){
                breedableTimer = "breed again in " + horse.getAge() / 20 + " secs";
            } else if (horse.getAge() == 0) {
                breedableTimer = "can breed";
            } else {
                breedableTimer = "too young";
            }

            String horseInfo = ChatColor.DARK_AQUA + "----------Horse Stats----------" +
                    ChatColor.DARK_GREEN + "\nName: " + ChatColor.GREEN + horseName + ChatColor.DARK_GREEN + " Owner: " + ChatColor.GREEN + horseOwner +
                    ChatColor.DARK_GREEN + "\nColor: " + ChatColor.GREEN + color + ChatColor.DARK_GREEN + " Pattern: " + ChatColor.GREEN + style +
                    ChatColor.DARK_GREEN + "\nHealth: " + ChatColor.GREEN + roundedHorseCurrentHealth +"/"+roundedHorseMaxHealth +
                    ChatColor.DARK_GREEN + "\nSpeed: " + ChatColor.GREEN + roundedHorseSpeed +
                    ChatColor.DARK_GREEN + "\nMax Jump Height: " + ChatColor.GREEN + roundedMaxJumpHeight +
                    ChatColor.DARK_GREEN + "\nBreedable: " + ChatColor.GREEN + breedableTimer +
                    ChatColor.DARK_GREEN + "\nTime Until Adult: " + ChatColor.GREEN + horseAge +
                    ChatColor.DARK_AQUA + "\n-------------------------------";

            player.sendMessage(horseInfo);
            event.setCancelled(true);
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public void loadConfig(){
        reloadConfig();
        cooldownConfig = getConfig().getInt("HorseStats.cooldown");
        statsItem = getConfig().getString("HorseStats.statsItem");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + ">>" + ChatColor.RED + " HorseStats disabled");
    }
}
