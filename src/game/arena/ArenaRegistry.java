package game.arena;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import game.Main;
import game.user.User;
import game.utility.ConfigUtils;
import game.utility.LocationSerializer;

public class ArenaRegistry {
	
	private final Main plugin;
	
	private final Set<Arena> arenas;

	private int bungeeArena = -1;

	public ArenaRegistry(Main plugin) {
		this.plugin = plugin;
		this.arenas = new HashSet<>();
		registerArenas();
	}

	public void registerArena(Arena arena) {
		this.arenas.add(arena);
	}

	public void unregisterArena(Arena arena) {
		this.arenas.remove(arena);
	}

	
	public Set<Arena> getArenas() {
		return Set.copyOf(this.arenas);
	}

	
	public Arena getArena(String id) {
		if (id == null)
			return null;
		return this.arenas.stream().filter(arena -> arena.getId().equals(id)).findFirst().orElse(null);
	}

	public Arena getArena(User user) {
		if (user == null)
			return null;
		return this.arenas.stream().filter(arena -> arena.isInArena(user)).findFirst().orElse(null);
	}

	public boolean isArena(String arenaId) {
		return (arenaId != null && getArena(arenaId) != null);
	}

	public boolean isInArena(User user) {
		return (getArena(user) != null);
	}

	private void registerArenas() {
		this.arenas.clear();
		FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) this.plugin, "arena");
		ConfigurationSection section = config.getConfigurationSection("instance");
		if (section == null) {
			this.plugin.getLogger()
					.warning("Couldn't find 'instance' section in arena.yml, delete the file to regenerate it!");
			return;
		}
		for (String id : section.getKeys(false)) {
			if (id.equals("default"))
				continue;
			String path = "instance.%s.".formatted(new Object[] { id });
			Arena arena = new Arena(id);
			registerArena(arena);
			arena.setReady(config.getBoolean(path + "ready"));
			arena.setMapName(config.getString(path + "mapName"));
			arena.setMinimumPlayers(config.getInt(path + "minimumPlayers"));
			arena.setMaximumPlayers(config.getInt(path + "maximumPlayers"));
			arena.setGameplayTime(config.getInt(path + "gameplayTime"));
			arena.setLobbyLocation(LocationSerializer.fromString(config.getString(path + "lobbyLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.setParkourEndLocation(LocationSerializer.fromString(config.getString(path + "parkourEndLocation")));
			arena.setPlayerSpawnPoints((List<Location>) config.getStringList(path + "playerSpawnPoints").stream().map(LocationSerializer::fromString).collect(Collectors.toList()));
			
			if (!arena.isReady()) {
				this.plugin.getLogger().log(Level.WARNING, "Setup of arena ''{0}'' is not finished yet!", id);
				return;
			}
			arena.start();
		}
	}

	public void shuffleBungeeArena() {
		this.bungeeArena = ThreadLocalRandom.current().nextInt(this.arenas.size());
	}

	public Arena getBungeeArena() {
		return List.<Arena>copyOf(this.arenas)
				.get((this.bungeeArena == -1)
						? (this.bungeeArena = ThreadLocalRandom.current().nextInt(this.arenas.size()))
						: this.bungeeArena);
	}
}
