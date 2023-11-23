package game.sign;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

import game.Main;
import game.arena.Arena;
import game.arena.ArenaState;
import game.user.User;
import game.utility.ConfigUtils;
import game.utility.LocationSerializer;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class SignManager implements Listener {
	
	private final Set<ArenaSign> arenaSigns;

	private final List<String> signLines;

	private final Map<ArenaState, String> gameStateToString;

	public SignManager(Main plugin) {
		this.arenaSigns = new HashSet<>();
		this.signLines = ConfigUtils.getConfig(plugin, "config").getStringList("signs.lines");
		this.gameStateToString = new EnumMap<>(ArenaState.class);
		
		for (ArenaState state : ArenaState.values()) {
			this.gameStateToString.put(state,("signs.game-states." + state.name.toLowerCase(Locale.ENGLISH)));
			loadSigns();
		}
	}

	@EventHandler
	public void onSignChange(SignChangeEvent event) {
		User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
		if (!user.hasPermission("mm.admin.sign.create") || !event.getLine(0).equalsIgnoreCase("[top]")) {
			return;
			
		}
			
		String line = event.getLine(1);
		
		if (line.isEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}
		
		Arena arena = Main.getInstance().getArenaRegistry().getArena(line);
		if (arena == null) {
			user.sendMessage("admin-commands.no-arena-found-with-that-name");
			return;
		}
		
		Block block = event.getBlock();
		this.arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));
		for (int i = 0; i < this.signLines.size(); i++)
			event.setLine(i, formatSign(this.signLines.get(i), arena));
		user.sendMessage("&aArena sign has been created successfully!");
		FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) Main.getInstance(), "arena");
		String path = "instance.%s.signs".formatted(new Object[] { arena });
		List<String> locs = config.getStringList(path);
		locs.add(LocationSerializer.toString(event.getBlock().getLocation()));
		config.set(path, locs);
		ConfigUtils.saveConfig((JavaPlugin) Main.getInstance(), config, "arena");
	}

	@EventHandler
	public void onSignDestroy(BlockBreakEvent event) {
		Block block = event.getBlock();
		ArenaSign arenaSign = getArenaSignByBlock(block);
		if (arenaSign == null) {
			return;
			
		}
			
		User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
		if (!user.hasPermission("mm.admin.sign.break")) {
			event.setCancelled(true);
			user.sendMessage("&cYou don't have enough permission to break this sign!");
			return;
		}
		
		this.arenaSigns.remove(arenaSign);
		String location = LocationSerializer.toString(block.getLocation());
		String path = "instance.%s.signs".formatted(new Object[] { arenaSign.arena() });
		FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) Main.getInstance(), "arena");
		List<String> signs = config.getStringList(path);
		for (String loc : signs) {
			if (loc.equals(location)) {
				signs.remove(location);
				config.set(path, signs);
				ConfigUtils.saveConfig((JavaPlugin) Main.getInstance(), config, "arena");
				user.sendMessage("&aSign removed successfully!");
				return;
			}
		}
		user.sendMessage("&cCouldn't remove arena sign! Please do manually!");
	}

	@EventHandler
	public void onJoinAttempt(PlayerInteractEvent event) {
		ArenaSign arenaSign = getArenaSignByBlock(event.getClickedBlock());
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK && arenaSign != null) {
			Arena arena = arenaSign.arena();
			if (arena == null)
				return;
			User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
			if (user.isInArena()) {
				user.sendMessage("messages.arena.already-playing");
				return;
			}
			Main.getInstance().getArenaManager().joinAttempt(user, arena);
		}
	}

	public void loadSigns() {
		this.arenaSigns.clear();
		FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) Main.getInstance(), "arena");
		for (String path : config.getConfigurationSection("instance").getKeys(false)) {
			for (String location : config.getStringList("instance." + path + ".signs")) {
				Location loc = LocationSerializer.fromString(location);
				BlockState blockState = loc.getBlock().getState();
				if (blockState instanceof Sign) {
					Sign sign = (Sign) blockState;
					this.arenaSigns.add(new ArenaSign(sign, Main.getInstance().getArenaRegistry().getArena(path)));
					continue;
				}
				Main.getInstance().getLogger().log(Level.WARNING, "Block at location {0} for arena {1} is not a sign!",
						new Object[] { loc, path });
			}
		}
		updateSigns();
	}

	public void updateSign(Arena arena) {
		this.arenaSigns.stream().filter(arenaSign -> arenaSign.arena().equals(arena)).forEach(this::updateSign);
	}

	private void updateSign(ArenaSign arenaSign) {
		Sign sign = arenaSign.sign();
		for (int i = 0; i < this.signLines.size(); i++)
			sign.setLine(i, formatSign(this.signLines.get(i), arenaSign.arena()));
		sign.update();
	}

	public void updateSigns() {
		for (ArenaSign arenaSign : this.arenaSigns) {
			Sign sign = arenaSign.sign();
			for (int i = 0; i < this.signLines.size(); i++)
				sign.setLine(i, formatSign(this.signLines.get(i), arenaSign.arena()));
			sign.update();
		}
	}

	public boolean isGameSign(Block block) {
		return this.arenaSigns.stream().anyMatch(sign -> sign.sign().getLocation().equals(block.getLocation()));
	}

	public void addArenaSign(Block block, Arena arena) {
		this.arenaSigns.add(new ArenaSign((Sign) block.getState(), arena));
	}

	private String formatSign(String msg, Arena arena) {
		String formatted = msg;
		int size = arena.getPlayers().size(), max = arena.getMaximumPlayers();
		formatted = formatted.replace("%map_name%", arena.getMapName());
		formatted = formatted.replace("%players%", Integer.toString(size));
		formatted = formatted.replace("%max_players%", Integer.toString(max));
		if (size >= max) {
			formatted = formatted.replace("%state%", "FULL!");
		} else {
			formatted = formatted.replace("%state%", this.gameStateToString.get(arena.getArenaState()));
		}
		return "";
	}

	private ArenaSign getArenaSignByBlock(Block block) {
		return (block == null || !(block.getState() instanceof Sign)) ? null
				: this.arenaSigns.stream().filter(sign -> sign.sign().getLocation().equals(block.getLocation()))
						.findFirst().orElse(null);
	}
}
