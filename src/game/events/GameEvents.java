package game.events;

import game.Main;
import game.arena.Arena;
import game.arena.ArenaState;
import game.user.User;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.meta.FireworkMeta;

public class GameEvents implements Listener {

    @EventHandler
    public void onFoodLevelChangeEvent(FoodLevelChangeEvent event) {
        HumanEntity humanEntity = event.getEntity();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            User user = Main.getInstance().getUserManager().getUser(player);
            if (user.isInArena()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onDropItemEvent(PlayerDropItemEvent event) {
        User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
        if (user.isInArena()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        User user = Main.getInstance().getUserManager().getUser(event.getPlayer());

        if (user.getArena() != null) {
            Arena arena = user.getArena();
            Location playerLocation = event.getTo();

            if (playerLocation != null) {
                Block playerBlock = playerLocation.getBlock();

                if (playerBlock.getType() == Material.HEAVY_WEIGHTED_PRESSURE_PLATE) {
                    // Check if the pressure plate is on top of a gold block
                    Location belowPressurePlate = playerBlock.getLocation().subtract(0, 1, 0);
                    Block belowBlock = belowPressurePlate.getBlock();

                    if (belowBlock.getType() == Material.GOLD_BLOCK) {
                        if (!arena.getCompletedPlayers().contains(user) && !arena.isSpectator(user)) {
                            arena.broadcastMessage(event.getPlayer().getDisplayName() + ChatColor.AQUA + " has successfully entered the capitol!");
                            arena.addCompletedPlayer(user);

                            Firework firework = event.getPlayer().getWorld().spawn(event.getPlayer().getLocation(), Firework.class);
                            FireworkMeta fireworkMeta = firework.getFireworkMeta();
                            fireworkMeta.addEffect(FireworkEffect.builder()
                                    .withColor(Color.GREEN)
                                    .with(FireworkEffect.Type.BALL)
                                    .build());
                            fireworkMeta.setPower(1);
                            firework.setFireworkMeta(fireworkMeta);

                            for (User users : arena.getAllPlayers()) {
                                users.getPlayer().playSound(user.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1F, 1F);
                            }
                        }
                    }
                }
            } else {
                event.getPlayer().sendMessage("Player location is null.");
            }
        }
    }

    @EventHandler
    public void onItemMove(InventoryClickEvent e) {
        HumanEntity humanEntity = e.getWhoClicked();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;

            if (Main.getInstance().getUserManager().getUser(player).isInArena() &&
                    (e.getView().getType() == InventoryType.CRAFTING || e.getView().getType() == InventoryType.PLAYER)) {
                e.setResult(Event.Result.DENY);
            }
        }
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
        
        if (user.isInArena()) {
            event.setCancelled(true);
            return;
        }

        if (user.isSpectator()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onItemSwap(PlayerSwapHandItemsEvent event) {
        User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
        if (user.isInArena()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
        if (user.isInArena()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
    	HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            User user = Main.getInstance().getUserManager().getUser(player);
            if (user.isInArena()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
    	HumanEntity humanEntity = event.getPlayer();
        if (humanEntity instanceof Player) {
            Player player = (Player) humanEntity;
            User user = Main.getInstance().getUserManager().getUser(player);
            if (user.isInArena()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onLobbyDamage(EntityDamageEvent event) {
        if (event.getEntity().getType() != EntityType.PLAYER) {
            return;
        }

        Player player = (Player) event.getEntity();
        User user = Main.getInstance().getUserManager().getUser(player);
        Arena arena = user.getArena();

        if (arena.isInArena(user)) {
            event.setCancelled(true);
        }

        if (arena == null || arena.getArenaState() == ArenaState.IN_GAME) {
            event.setCancelled(true);
        }
        event.setCancelled(true);
        player.setFireTicks(0);
    }
}