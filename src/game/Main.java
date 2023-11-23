package game;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import commandframework.CommandFramework;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import game.arena.Arena;
import game.arena.ArenaManager;
import game.arena.ArenaRegistry;
import game.commands.AbstractCommand;
import game.events.GameEvents;
import game.events.JoinQuitEvents;
import game.sign.SignManager;
import game.song.NBSDecoder;
import game.song.Song;
import game.user.User;
import game.user.UserManager;

public class Main extends JavaPlugin {
	
	private static Main instance;
	private UpdateTask update;
	private List<Song> songs = new LinkedList<>();
	private UserManager userManager;
	private CommandFramework commandFramework;
	private ArenaRegistry arenaRegistry;
	private ArenaManager arenaManager;
	private SignManager signManager;

	public void onEnable() {
	    instance = this;
		initializeClasses();
		setupSongs();
		this.update = new UpdateTask(this);
		
	}

	public void onDisable() {
		this.update = null;
		this.update.cancel();

		for (Arena arena : this.arenaRegistry.getArenas()) {
			arena.getGameBar().removeAll();
			for (User user : arena.getPlayers()) {
				Player player = user.getPlayer();
				arena.teleportToEndLocation(user);
				player.setFlySpeed(0.1F);
				player.setWalkSpeed(0.2F);
				player.getInventory().clear();
				player.getInventory().setArmorContents(null);
				player.getActivePotionEffects().forEach(effect -> player.removePotionEffect(effect.getType()));
			}
			arena.cleanUpArena();
		}
	}
	
	private void registerListeners() {
        Listener[] listeners = {
                new GameEvents(),
                new SignManager(this),
                new JoinQuitEvents()
        };

        PluginManager pluginManager = getServer().getPluginManager();
        for (Listener listener : listeners) {
            pluginManager.registerEvents(listener, this);
        }
    }

	private void initializeClasses() {
		registerListeners();
		setupConfigurationFiles();
		this.userManager = new UserManager(this);
		this.commandFramework = new CommandFramework((Plugin) this);
		this.arenaRegistry = new ArenaRegistry(this);
		this.arenaManager = new ArenaManager(this);
		this.signManager = new SignManager(this);		
		AbstractCommand.registerCommands(this);
		User.cooldownHandlerTask();	
	}

	private void setupConfigurationFiles() {
		saveDefaultConfig();
		Stream.<String>of(
				new String[] { "arena", "stats", "mysql", "rewards", "bungee"})
				.filter(fileName -> !(new File(getDataFolder(), fileName + ".yml")).exists())
				.forEach(fileName -> saveResource(fileName + ".yml", false));
	}
	
	public void setupSongs() {
		this.getServer().getLogger().info("Loading all NBS song files!");
	    File folder = new File(getDataFolder().getPath() + "/songs/");
	    if (!folder.exists()) {
	      saveResource("songs/0.nbs", true);
	      saveResource("songs/1.nbs", true);
	      saveResource("songs/2.nbs", true);
	      saveResource("songs/3.nbs", true);
	      saveResource("songs/4.nbs", true);
	      saveResource("songs/5.nbs", true);
	    } 
	    for (File file : (new File(getDataFolder().getPath() + "/songs/")).listFiles()) {
	      if (file.getName().endsWith(".nbs"))
	        this.songs.add(NBSDecoder.parse(file)); 
	    } 
	}

	public UserManager getUserManager() {
		return this.userManager;
	}
	
	public CommandFramework getCommandFramework() {
		return this.commandFramework;
	}

	public ArenaRegistry getArenaRegistry() {
		return this.arenaRegistry;
	}
	
	public ArenaManager getArenaManager() {
		return this.arenaManager;
	}

	public SignManager getSignManager() {
		return this.signManager;
	}
	
	public List<Song> getSongs() {
	    return this.songs;
	}
	
	public UpdateTask getUpdateTask() {
	    return this.update;
	}

	public static Main getInstance() {
		return instance;
	}
}
