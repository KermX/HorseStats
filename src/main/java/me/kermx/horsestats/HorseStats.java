package me.kermx.horsestats;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public final class HorseStats extends JavaPlugin implements Listener {
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private int cooldownConfig;
    private String statsItem;

    //messages config
    private String messagesTopMessage;
    private String messagesName;
    private String messagesOwner;
    private String messagesColor;
    private String messagesPattern;
    private String messagesHealth;
    private String messagesSpeed;
    private String messagesMaxJumpHeight;
    private String messagesBreedable;
    private String messagesTimeUntilAdult;
    private String messagesBottomMessage;
    private String messagesNoOwner;
    private String messagesNoName;
    private String messagesAdult;
    private String messagesBreedableAgainIn;
    private String messagesSeconds;
    private String messagesCanBreed;
    private String messagesTooYoungToBreed;
    private String messagesNotApplicable;

    private String messagesCooldownPart1;
    private String messagesCooldownPart2;

    @Override
    public void onEnable() {
        // load config
        saveDefaultConfig();
        reloadConfig();
        cooldownConfig = getConfig().getInt("HorseStats.cooldown");
        statsItem = getConfig().getString("HorseStats.statsItem");
        messagesTopMessage = getConfig().getString("Messages.TopMessage");
        messagesName = getConfig().getString("Messages.Name");
        messagesOwner = getConfig().getString("Messages.Owner");
        messagesColor = getConfig().getString("Messages.Color");
        messagesPattern = getConfig().getString("Messages.Pattern");
        messagesHealth = getConfig().getString("Messages.Health");
        messagesSpeed = getConfig().getString("Messages.Speed");
        messagesMaxJumpHeight = getConfig().getString("Messages.MaxJumpHeight");
        messagesBreedable = getConfig().getString("Messages.Breedable");
        messagesTimeUntilAdult = getConfig().getString("Messages.TimeUntilAdult");
        messagesBottomMessage = getConfig().getString("Messages.BottomMessage");
        messagesNoOwner = getConfig().getString("Messages.NoOwner");
        messagesNoName = getConfig().getString("Messages.NoName");
        messagesAdult = getConfig().getString("Messages.Adult");
        messagesBreedableAgainIn = getConfig().getString("Messages.BreedableAgainIn");
        messagesSeconds = getConfig().getString("Messages.Seconds");
        messagesCanBreed = getConfig().getString("Messages.CanBreed");
        messagesTooYoungToBreed = getConfig().getString("Messages.TooYoungToBreed");
        messagesNotApplicable = getConfig().getString("Messages.NotApplicable");
        messagesCooldownPart1 = getConfig().getString("Messages.CooldownPart1");
        messagesCooldownPart2 = getConfig().getString("Messages.CooldownPart2");
        // load command
        Objects.requireNonNull(getCommand("horsestats")).setExecutor(new ReloadCommand(this));
        // load event
        getServer().getPluginManager().registerEvents(this,this);

        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + ">>" + ChatColor.GREEN + " HorseStats enabled successfully");

    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if (event.getHand() != EquipmentSlot.HAND){
            return;
        }

        if (cooldowns.containsKey(player.getUniqueId())){
            int COOLDOWN_SECONDS = cooldownConfig;
            long timeLeft = ((cooldowns.get(player.getUniqueId()) / 1000) + COOLDOWN_SECONDS) - (System.currentTimeMillis() / 1000);
            if (timeLeft > 0){
                player.sendMessage(ChatColor.RED + messagesCooldownPart1 + timeLeft + messagesCooldownPart2);
                event.setCancelled(true);
                return;
            }
        }

        if (player.isSneaking() || player.getInventory().getItemInMainHand().getType() != Material.valueOf(statsItem)) {
            return;
        }

        if (event.getRightClicked() instanceof AbstractHorse) {
            AbstractHorse horse = (AbstractHorse) event.getRightClicked();
            double horseCurrentHealth = horse.getHealth();
            double horseMaxHealth = Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MAX_HEALTH)).getValue();
            double horseSpeed = Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getValue() * 42.157787584D;
            double jumpStat = horse.getJumpStrength();
            double maxJumpHeight = (-0.1817584952 * jumpStat * jumpStat * jumpStat) + (3.689713992 * jumpStat * jumpStat) + (2.128599134 * jumpStat) + -0.343930367;
            double roundedHorseSpeed = Math.round(horseSpeed * 1000) / 1000.0;
            double roundedMaxJumpHeight = Math.round(maxJumpHeight * 1000) / 1000.0;
            double roundedHorseMaxHealth = Math.round(horseMaxHealth * 10) / 10.0;
            double roundedHorseCurrentHealth = Math.round(horseCurrentHealth * 10) / 10.0;
            String style = messagesNotApplicable;
            String color = messagesNotApplicable;
            if (entity instanceof Horse) {
                Horse realhorse = (Horse) entity;
                color = realhorse.getColor().name().toLowerCase().replace("_", " ");
                style = realhorse.getStyle().name().toLowerCase().replace("_", " ");
            }

            String horseOwner;
            if (horse.isTamed()) {
                horseOwner = horse.getOwner().getName();
            } else {
                horseOwner = messagesNoOwner;
            }

            String horseName;
            if (horse.getCustomName() != null) {
                horseName = horse.getCustomName();
            } else {
                horseName = messagesNoName;
            }

            String horseAge;
            if (horse.getAge() >= 0) {
                horseAge = messagesAdult;
            } else {
                horseAge = Math.abs(horse.getAge() / 20) + messagesSeconds;
            }

            String breedableTimer;
            if (horse instanceof ZombieHorse || horse instanceof SkeletonHorse || horse instanceof Mule){
                breedableTimer = messagesNotApplicable;
            } else if (horse.getAge() > 0) {
                breedableTimer = messagesBreedableAgainIn + horse.getAge() / 20 + messagesSeconds;
            } else if (horse.getAge() == 0) {
                breedableTimer = messagesCanBreed;
            } else {
                breedableTimer = messagesTooYoungToBreed;
            }

            String horseInfo = ChatColor.DARK_AQUA + messagesTopMessage +
                    ChatColor.DARK_GREEN + "\n" + messagesName + ChatColor.GREEN + horseName + ChatColor.DARK_GREEN + messagesOwner + ChatColor.GREEN + horseOwner +
                    ChatColor.DARK_GREEN + "\n" + messagesColor + ChatColor.GREEN + color + ChatColor.DARK_GREEN + messagesPattern + ChatColor.GREEN + style +
                    ChatColor.DARK_GREEN + "\n" + messagesHealth + ChatColor.GREEN + roundedHorseCurrentHealth + "/" + roundedHorseMaxHealth +
                    ChatColor.DARK_GREEN + "\n" + messagesSpeed + ChatColor.GREEN + roundedHorseSpeed +
                    ChatColor.DARK_GREEN + "\n" + messagesMaxJumpHeight + ChatColor.GREEN + roundedMaxJumpHeight +
                    ChatColor.DARK_GREEN + "\n" + messagesBreedable + ChatColor.GREEN + breedableTimer +
                    ChatColor.DARK_GREEN + "\n" + messagesTimeUntilAdult + ChatColor.GREEN + horseAge +
                    ChatColor.DARK_AQUA + "\n" + messagesBottomMessage;

            player.sendMessage(horseInfo);
            event.setCancelled(true);
            cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    public void loadConfig(){
        reloadConfig();
        cooldownConfig = getConfig().getInt("HorseStats.cooldown");
        statsItem = getConfig().getString("HorseStats.statsItem");
        messagesTopMessage = getConfig().getString("Messages.TopMessage");
        messagesName = getConfig().getString("Messages.Name");
        messagesOwner = getConfig().getString("Messages.Owner");
        messagesColor = getConfig().getString("Messages.Color");
        messagesPattern = getConfig().getString("Messages.Pattern");
        messagesHealth = getConfig().getString("Messages.Health");
        messagesSpeed = getConfig().getString("Messages.Speed");
        messagesMaxJumpHeight = getConfig().getString("Messages.MaxJumpHeight");
        messagesBreedable = getConfig().getString("Messages.Breedable");
        messagesTimeUntilAdult = getConfig().getString("Messages.TimeUntilAdult");
        messagesBottomMessage = getConfig().getString("Messages.BottomMessage");
        messagesNoOwner = getConfig().getString("Messages.NoOwner");
        messagesNoName = getConfig().getString("Messages.NoName");
        messagesAdult = getConfig().getString("Messages.Adult");
        messagesBreedableAgainIn = getConfig().getString("Messages.BreedableAgainIn");
        messagesSeconds = getConfig().getString("Messages.Seconds");
        messagesCanBreed = getConfig().getString("Messages.CanBreed");
        messagesTooYoungToBreed = getConfig().getString("Messages.TooYoungToBreed");
        messagesNotApplicable = getConfig().getString("Messages.NotApplicable");
        messagesCooldownPart1 = getConfig().getString("Messages.CooldownPart1");
        messagesCooldownPart2 = getConfig().getString("Messages.CooldownPart2");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD + ">>" + ChatColor.RED + " HorseStats disabled");
    }
}
