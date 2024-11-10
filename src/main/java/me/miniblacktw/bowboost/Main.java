package me.miniblacktw.bowboost;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private final Map<String, Location> playerStartingIslands = new HashMap<>();
    private final Map<UUID, Long> titleCooldowns = new HashMap<>();
    private int playerCount = 0;

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("[MINI] Loaded BowBoostPractice");
    }

    @Override
    public void onDisable() {
        getLogger().info("[MINI] Unloaded BowBoostPractice");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (!playerStartingIslands.containsKey(player.getName())) {
            int islandNumber = (playerCount % 20) + 1;
            Location islandLocation = new Location(Bukkit.getWorld("world2"), 30 * (islandNumber - 1) + 0.5, 67, 0.5);
            playerStartingIslands.put(player.getName(), islandLocation);
            playerCount++;
        }

        player.teleport(playerStartingIslands.get(player.getName()));

        ItemStack bow = new ItemStack(Material.BOW, 1);
        ItemMeta bowMeta = bow.getItemMeta();
        if (bowMeta != null) {
            bowMeta.addEnchant(Enchantment.KNOCKBACK, 1, true);
            bowMeta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
            bowMeta.spigot().setUnbreakable(true);
            bow.setItemMeta(bowMeta);
        }
        player.getInventory().setItem(0, bow);

        ItemStack arrow = new ItemStack(Material.ARROW, 1);
        player.getInventory().setItem(1, arrow);

        ItemStack redDye = new ItemStack(Material.INK_SACK, 1, (short) 1);
        ItemMeta dyeMeta = redDye.getItemMeta();
        if (dyeMeta != null) {
            dyeMeta.setDisplayName("§cRETURN TO LOBBY §7(Right-Click to use)");
            redDye.setItemMeta(dyeMeta);
        }
        player.getInventory().setItem(8, redDye);

        Bukkit.getScheduler().runTaskTimer(this, () -> {
            if (player.isOnline()) {
                player.setHealth(player.getMaxHealth());
            }
        }, 0L, 20L);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        playerStartingIslands.remove(player.getName());
        titleCooldowns.remove(player.getUniqueId());

        if (playerCount > 0) {
            playerCount--;
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location currentLocation = player.getLocation();
        UUID playerId = player.getUniqueId();

        if (currentLocation.getY() < 55) {
            Location startingLocation = playerStartingIslands.get(player.getName());
            if (startingLocation != null) {
                player.teleport(startingLocation);
            }
        }

        if (currentLocation.getZ() > 20) {
            if (titleCooldowns.containsKey(playerId) && (System.currentTimeMillis() - titleCooldowns.get(playerId)) < 500) {
                return;
            }

            player.sendTitle(ChatColor.GREEN + "Good Job!", ChatColor.GRAY + "You got to the other end!");
            titleCooldowns.put(playerId, System.currentTimeMillis());

            Bukkit.getScheduler().runTaskLater(this, () -> {
                Location startingLocation = playerStartingIslands.get(player.getName());
                if (startingLocation != null) {
                    player.teleport(startingLocation);
                }
            }, 10L);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (player.getItemInHand().getType() == Material.INK_SACK &&
                player.getItemInHand().getItemMeta() != null &&
                "§cRETURN TO LOBBY §7(Right-Click to use)".equals(player.getItemInHand().getItemMeta().getDisplayName())) {
            player.performCommand("lobby");
        }
    }
}