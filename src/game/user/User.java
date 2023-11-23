package game.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Scoreboard;

import game.Main;
import game.arena.Arena;
import game.utility.AttributeUtils;
import game.utility.Titles;

public class User {
	
	private static final Main plugin = (Main) JavaPlugin.getPlugin(Main.class);
	private static long cooldownCounter = 0L;
	private final UUID uuid;
	private final Map<String, Double> cooldowns;
	private Player player;
	private boolean spectator;

	public User(UUID uuid) {
		this.uuid = uuid;
		this.cooldowns = new HashMap<>();
		setPlayer();
	}

	public void setPlayer() {
		this.player = plugin.getServer().getPlayer(this.uuid);
	}

	public void closeOpenedInventory() {
		getPlayer().closeInventory();
	}

	public boolean isInArena() {
		return plugin.getArenaRegistry().isInArena(this);
	}

	public Arena getArena() {
		return plugin.getArenaRegistry().getArena(this);
	}

	public boolean isSpectator() {
		return this.spectator;
	}

	public void setSpectator(boolean spectator) {
		this.spectator = spectator;
	}
	
	public void sendMessage(String message) {
        getPlayer().sendMessage(message);
    }

	public Location getLocation() {
		return getPlayer().getLocation();
	}

	public Player getPlayer() {
		if (this.player == null) {
			this.player = plugin.getServer().getPlayer(this.uuid);
			return this.player;
		}
		return this.player;
	}

	public String getName() {
		return getPlayer().getName();
	}

	public UUID getUniqueId() {
		return this.uuid;
	}

	public boolean hasPermission(String permission) {
		Player player = getPlayer();
		return (player.isOp() || player.hasPermission(permission));
	}

	public void setCooldown(String s, double seconds) {
		this.cooldowns.put(s, Double.valueOf(seconds + cooldownCounter));
	}

	public double getCooldown(String s) {
		Double cooldown = this.cooldowns.get(s);
		return (cooldown == null || cooldown.doubleValue() <= cooldownCounter) ? 0.0D
				: (cooldown.doubleValue() - cooldownCounter);
	}

	public void removePotionEffectsExcept(PotionEffectType... effectTypes) {
		Set<PotionEffectType> setOfEffects = Set.of(effectTypes);
		for (PotionEffect activePotion : getPlayer().getActivePotionEffects()) {
			if (setOfEffects.contains(activePotion.getType()))
				continue;
			this.player.removePotionEffect(activePotion.getType());
		}
	}
	
	public void clearPlayerScoreboard(Player player) {
	    Scoreboard emptyScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
	    player.setScoreboard(emptyScoreboard);
	}

	public void sendForcedRawTitle(String title, String subtitle) {
		Titles.sendTitle(getPlayer(), 5, 60, 5, title, subtitle);
	}

	public void sendActionBar(String message) {
		getPlayer().spigot().sendMessage(ChatMessageType.ACTION_BAR, (BaseComponent) new TextComponent(message));
		
	}

	public void playDeathEffect() {
		this.player.setAllowFlight(true);
		this.player.setFlying(true);
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 2147483647, 1, false, false, false));
		this.player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 80, 1, false, false, false));
		this.player.setVelocity(this.player.getLocation().getDirection().setY(-1).multiply(-1.15D));
	}

	public void heal() {
		AttributeUtils.healPlayer(getPlayer());
	}

	public boolean equals(Object obj) {
		User other;
		if (obj instanceof User) {
			other = (User) obj;
		} else {
			return false;
		}
		return other.getUniqueId().equals(this.uuid);
	}

	public String toString() {
		return "name=%s, uuid=%s".formatted(new Object[] { this.player.getName(), this.uuid });
	}

	public static void cooldownHandlerTask() {
		plugin.getServer().getScheduler().runTaskTimerAsynchronously((Plugin) plugin, () -> cooldownCounter++, 20L,
				20L);
	}

	public Scoreboard getScoreboard() {
	    return getPlayer().getScoreboard();
	}
}