package game.commands;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import commandframework.Command;
import commandframework.CommandArguments;
import commandframework.Completer;
import game.Main;
import game.arena.Arena;
import game.arena.ArenaOption;
import game.arena.ArenaState;
import game.user.User;
import game.utility.ConfigUtils;
import game.utility.LocationSerializer;
import game.utility.MiscUtils;
import net.md_5.bungee.api.ChatColor;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

public class AdminCommands extends AbstractCommand {
		
	public AdminCommands(Main plugin) {
		super(plugin);
	}

	@Command(name = "top", usage = "/top help", desc = "Main command for the Game Setup")
	public void mmCommand(CommandArguments arguments) {
		if (arguments.isArgumentsEmpty()) {
			arguments.sendMessage("&3This server is running &bTales of Panem " + this.plugin.getDescription().getVersion() + " &3by &bOndre.");
			if (arguments.hasPermission("mm.admin"))
				arguments.sendMessage(("&3Commands: &b/" + arguments.getLabel() + " help"));
		}
	}

	@Command(name = "top.create", permission = "top.admin.create", desc = "Create an arena with default configuration", usage = "/top create <arena name>", senderType = Command.SenderType.PLAYER)
	public void createArena(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (this.plugin.getArenaRegistry().isInArena(user)) {
			user.sendMessage(ChatColor.RED + "You cannot do that command whilist in game!");
			return;
		}
		
		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}
		
		String arenaId = arguments.getArgument(0);
		if (this.plugin.getArenaRegistry().isArena(arenaId)) {
			user.sendMessage("admin-commands.there-is-already-an-arena");
			return;
		}
		
		String path = "instance.%s.".formatted(new Object[] { arenaId });
		Arena arena = new Arena(arenaId);
		FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) this.plugin, "arena");
		this.plugin.getArenaRegistry().registerArena(arena);
		config.set(path + "mapName", arenaId);
		config.set(path + "minimumPlayers", Integer.valueOf(4));
		config.set(path + "maximumPlayers", Integer.valueOf(26));
		config.set(path + "gameplayTime", Integer.valueOf(ArenaOption.GAMEPLAY_TIME.getIntegerValue()));
		config.set(path + "ready", Boolean.valueOf(false));
		config.set(path + "lobbyLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "endLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "parkourEndLocation", LocationSerializer.SERIALIZED_LOCATION);
		config.set(path + "playerSpawnPoints", new ArrayList());
		config.set(path + "signs", new ArrayList());
		ConfigUtils.saveConfig((JavaPlugin) this.plugin, config, "arena");
		Player player = user.getPlayer();
		user.sendMessage("");
		MiscUtils.sendCenteredMessage(player, ChatColor.GRAY + "You have created the arena: " + ChatColor.GREEN + arenaId);
		user.sendMessage("");
	}
	
	@Command(name = "top.setarenalobby", permission = "admin.create", desc = "Create an arena with default configuration", usage = "/setarenalobby", senderType = Command.SenderType.PLAYER)
	public void setArenaLobby(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (this.plugin.getArenaRegistry().isInArena(user)) {
			user.sendMessage(ChatColor.RED + "You cannot do that in-game!");
			return;
		}
		
		if (arguments.isArgumentsEmpty()) {
			user.sendMessage(ChatColor.RED + "Please provide an arena name!");
			return;
		}
		
		String arenaId = arguments.getArgument(0);
		
		if (this.plugin.getArenaRegistry().isArena(arenaId)) {
			Arena arena = new Arena(arenaId);
			FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) this.plugin, "arena");
			String path = "instance.%s.".formatted(new Object[] { arenaId });
			Location location = user.getLocation();
			config.set(path + "lobbyLocation", LocationSerializer.toString(location));			
			ConfigUtils.saveConfig((JavaPlugin) this.plugin, config, "arena");
			arena.setLobbyLocation(location);
			user.getPlayer().sendMessage(ChatColor.GRAY + "You have added the lobby location for: " + ChatColor.AQUA + "" + arenaId);
			return;
		}
	}
	
	@Command(name = "top.setendlocation", permission = "admin.create", desc = "Create an arena with default configuration", usage = "/setarenalobby", senderType = Command.SenderType.PLAYER)
	public void setArenaEndLocation(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (this.plugin.getArenaRegistry().isInArena(user)) {
			user.sendMessage(ChatColor.RED + "You cannot do that in-game!");
			return;
		}
		
		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}
		
		String arenaId = arguments.getArgument(0);
		
		if (this.plugin.getArenaRegistry().isArena(arenaId)) {
			Arena arena = new Arena(arenaId);
			FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) this.plugin, "arena");
			String path = "instance.%s.".formatted(new Object[] { arenaId });
			Location location = user.getLocation();
			config.set(path + "endLocation", LocationSerializer.toString(location));			
			ConfigUtils.saveConfig((JavaPlugin) this.plugin, config, "arena");
			arena.setEndLocation(location);
			user.getPlayer().sendMessage(ChatColor.GRAY + "You have added the end location for: " + ChatColor.AQUA + "" + arenaId);
			return;
		}
	}
	
	@Command(name = "top.setparkourendlocation", permission = "admin.create", desc = "Create an arena with default configuration", usage = "/setarenalobby", senderType = Command.SenderType.PLAYER)
	public void setParkourEndLocation(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (this.plugin.getArenaRegistry().isInArena(user)) {
			user.sendMessage(ChatColor.RED + "You cannot do that in-game!");
			return;
		}
		
		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}
		
		String arenaId = arguments.getArgument(0);
		
		if (this.plugin.getArenaRegistry().isArena(arenaId)) {
			Arena arena = new Arena(arenaId);
			FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) this.plugin, "arena");
			String path = "instance.%s.".formatted(new Object[] { arenaId });
			Location location = user.getLocation();
			config.set(path + "parkourEndLocation", LocationSerializer.toString(location));			
			ConfigUtils.saveConfig((JavaPlugin) this.plugin, config, "arena");
			arena.setParkourEndLocation(location);
			user.getPlayer().sendMessage(ChatColor.GRAY + "You have added the parkour end location for: " + ChatColor.AQUA + "" + arenaId);
			return;
		}
	}
	
	@Command(name = "top.toggleready", permission = "admin.create", desc = "Create an arena with default configuration", usage = "/setarenalobby", senderType = Command.SenderType.PLAYER)
	public void setReady(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (this.plugin.getArenaRegistry().isInArena(user)) {
			user.sendMessage(ChatColor.RED + "You cannot do that in-game!");
			return;
		}
		
		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}
		
		String arenaId = arguments.getArgument(0);
		
		if (this.plugin.getArenaRegistry().isArena(arenaId)) {
	        Arena arena = new Arena(arenaId);
	        FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) this.plugin, "arena");
	        String path = "instance.%s.".formatted(new Object[] { arenaId });
	        
	        boolean isReady = !config.getBoolean(path + "ready");
	        config.set(path + "ready", isReady);
	        ConfigUtils.saveConfig((JavaPlugin) this.plugin, config, "arena");
	        arena.setReady(isReady);
			arena.setArenaState(ArenaState.WAITING_FOR_PLAYERS);
			arena.setMapName(config.getString(path + "mapName"));
			arena.setMinimumPlayers(config.getInt(path + "minimumPlayers"));
			arena.setMaximumPlayers(config.getInt(path + "maximumPlayers"));
			arena.setGameplayTime(config.getInt(path + "gameplayTime"));
			arena.setLobbyLocation(LocationSerializer.fromString(config.getString(path + "lobbyLocation")));
			arena.setEndLocation(LocationSerializer.fromString(config.getString(path + "endLocation")));
			arena.setParkourEndLocation(LocationSerializer.fromString(config.getString(path + "parkourEndLocation")));
			arena.setPlayerSpawnPoints((List) config.getStringList(path + "playerSpawnPoints").stream().map(LocationSerializer::fromString).collect(Collectors.toList()));
	        ConfigUtils.saveConfig((JavaPlugin) this.plugin, config, "arena");
			arena.start();
	        
	        String status = isReady ? ChatColor.GREEN + "READY" : ChatColor.RED + "NOT READY";
	        user.getPlayer().sendMessage(ChatColor.GRAY + "The arena status for " + ChatColor.AQUA + arenaId + ChatColor.GRAY + 
	        		" was toggled! " + "(" + status + ChatColor.GRAY + ")");
	        return;
	    }
	}

	@Command(name = "top.delete", permission = "top.admin.delete", desc = "Delete specified arena and its data", usage = "/top delete <arena name>", senderType = Command.SenderType.PLAYER)
	public void mmDeleteCommand(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (this.plugin.getArenaRegistry().isInArena(user)) {
			user.sendMessage(ChatColor.RED + "You cannot do that in-game!");
			return;
		}
		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}
		String arenaId = arguments.getArgument(0);
		if (!this.plugin.getArenaRegistry().isArena(arenaId)) {
			user.sendMessage("admin-commands.no-arena-found-with-that-name");
			return;
		}
		Arena arena = this.plugin.getArenaRegistry().getArena(arenaId);
		arena.stop();
		FileConfiguration config = ConfigUtils.getConfig((JavaPlugin) this.plugin, "arena");
		config.set("instance." + arenaId, null);
		ConfigUtils.saveConfig((JavaPlugin) this.plugin, config, "arena");
		this.plugin.getArenaRegistry().unregisterArena(arena);
	}

	@Command(name = "top.list", permission = "top.admin.list", desc = "Get a list of registered arenas and their status", usage = "/top list", senderType = Command.SenderType.PLAYER)
	public void mmListCommand(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		Set<Arena> arenas = this.plugin.getArenaRegistry().getArenas();
		if (arenas.isEmpty()) {
			user.sendMessage("admin-commands.list-command.no-arenas-created");
			return;
		}
		String list = arenas.stream().map(Arena::getId).collect(Collectors.joining(", "));
		arguments.sendMessage(("admin-commands.list-command.format").replace("%list%", list));
	}

	@Command(name = "top.forcestart", permission = "top.admin.forcestart", desc = "Forces arena to start without waiting time", usage = "/top forcestart", senderType = Command.SenderType.PLAYER)
	public void mmForceStartCommand(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (!user.isInArena()) {
			user.sendMessage("admin-commands.must-be-in-arena");
			return;
		}
		Arena arena = user.getArena();
		if (arena.getPlayers().size() < 2) {
			arena.broadcastMessage("messages.arena.waiting-for-players");
			return;
		}
		if (arena.isForceStart()) {
			user.sendMessage("messages.in-game.already-force-start");
			return;
		}
		if (arena.isArenaState(new ArenaState[] { ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING })) {
			arena.setArenaState(ArenaState.STARTING);
			arena.setForceStart(true);
			arena.setTimer(0);
			arena.getPlayers().forEach(u -> u.sendMessage("messages.in-game.force-start"));
		}
	}

	@Command(name = "top.stop", permission = "top.admin.stop", desc = "Stop the arena that you're in", usage = "/top stop", senderType = Command.SenderType.PLAYER)
	public void mmStopCommand(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		Arena arena = user.getArena();
		if (arena == null) {
			user.sendMessage("admin-commands.must-be-in-arena");
			return;
		}
		if (arena.getArenaState() != ArenaState.ENDING)
			this.plugin.getArenaManager().stopGame(false, arena);
	}
	
	@Completer(name = "top")
	public List<String> onTabComplete(CommandArguments arguments) {
		List<String> completions = new ArrayList<>(),
				commands = (List<String>) this.plugin.getCommandFramework().getCommands().stream()
						.map(cmd -> cmd.name().replace(arguments.getLabel() + ".", "")).collect(Collectors.toList());
		String args[] = arguments.getArguments(), arg = args[0];
		commands.remove("mm");
		if (args.length == 1)
			StringUtil.copyPartialMatches(arg,
					(arguments.hasPermission("mm.admin") || arguments.getSender().isOp()) ? commands
							: List.<String>of("top", "stats", "join", "leave", "randomjoin"),
					completions);
		if (args.length == 2) {
			if (List.<String>of("create", "list", "randomjoin", "leave").contains(arg))
				return null;
			if (arg.equalsIgnoreCase("top"))
				return List.of("wins", "loses", "kills", "deaths", "highest_score", "games_played");
			if (arg.equalsIgnoreCase("stats"))
				return (List<String>) this.plugin.getServer().getOnlinePlayers().stream().map(Player::getName)
						.collect(Collectors.toList());
			List<String> arenas = (List<String>) this.plugin.getArenaRegistry().getArenas().stream().map(Arena::getId)
					.collect(Collectors.toList());
			StringUtil.copyPartialMatches(args[1], arenas, completions);
			arenas.sort((Comparator<? super String>) null);
			return arenas;
		}
		completions.sort((Comparator<? super String>) null);
		return completions;
	}
}
