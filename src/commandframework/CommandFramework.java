package commandframework;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;

import commandframework.utils.Utils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CommandFramework implements CommandExecutor, TabCompleter {

	/**
	 * Main instance of framework.
	 */
	
	private final Plugin plugin;
	/**
	 * Map of registered commands by framework.
	 */
	
	private final Map<Command, Map.Entry<Method, Object>> commands = new HashMap<>();
	/**
	 * Map of registered subcommands by framework.
	 */
	
	private final Map<Command, Map.Entry<Method, Object>> subCommands = new TreeMap<>(Comparator.comparing(Command::name).reversed());
	/**
	 * Map of registered sub command tab completions by framework.
	 */
	
	private final Map<Completer, Map.Entry<Method, Object>> commandCompletions = new HashMap<>();
	/**
	 * Map of registered tab completions by framework.
	 */
	
	private final Map<Completer, Map.Entry<Method, Object>> subCommandCompletions = new TreeMap<>(Comparator.comparing(Completer::name).reversed());
	/**
	 * Map of registered command cooldowns by framework.
	 */
	
	private final Map<CommandSender, Map<Command, Long>> cooldowns = new HashMap<>();
	/**
	 * Function to apply if there is no matched commands related framework.
	 *
	 * <pre>
	 *     // To disable sending usage to command sender return true
	 *     CommandFramework#setMatchFunction(arguments -> true);
	 * </pre>
	 */
	
	private Function<CommandArguments, Boolean> matchFunction = (arguments) -> false;
	/**
	 * Default command map of Bukkit.
	 */
	
	protected CommandMap commandMap;

	// Error Message Handler
	public static String ONLY_BY_PLAYERS         = ChatColor.RED + "This command is only executable by players!";
	public static String ONLY_BY_CONSOLE         = ChatColor.RED + "This command is only executable by console!";
	public static String NO_PERMISSION           = ChatColor.RED + "You don't have enough permission to execute this command!";
	public static String MUST_HAVE_OP            = ChatColor.RED + "You must have OP to execute this command!";
	public static String SHORT_OR_LONG_ARG_SIZE  = ChatColor.RED + "Required argument length is less or greater than needed!";
	public static String WAIT_BEFORE_USING_AGAIN = ChatColor.RED + "You have to wait %ds before using this command again!";

	public CommandFramework( Plugin plugin) {
		this.plugin = plugin;

		if (plugin.getServer().getPluginManager() instanceof SimplePluginManager) {
			final SimplePluginManager manager = (SimplePluginManager) plugin.getServer().getPluginManager();

			try {
				final Field field = SimplePluginManager.class.getDeclaredField("commandMap");
				field.setAccessible(true);

				commandMap = (CommandMap) field.get(manager);
			} catch (IllegalArgumentException | SecurityException | IllegalAccessException | NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Function to apply if there is no matched commands related framework.
	 *
	 * @param matchFunction to be applied if there is no matched commands
	 */
	public void setMatchFunction( Function<CommandArguments, Boolean> matchFunction) {
		this.matchFunction = matchFunction;
	}

	/**
	 * Register command methods in object class.
	 *
	 * @param instance object class
	 */
	public void registerCommands( Object instance) {
		for (final Method method : instance.getClass().getMethods()) {
			final Command command = method.getAnnotation(Command.class);

			if (command != null) {
				if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0] != CommandArguments.class) {
					continue;
				}

				registerCommand(command, method, instance);
			} else if (method.getAnnotation(Completer.class) != null) {
				if (!List.class.isAssignableFrom(method.getReturnType())) {
					plugin.getLogger().log(Level.WARNING, "Skipped registration of {0} because it is not returning java.util.List type.", method.getName());
					continue;
				}

				final Completer completer = method.getAnnotation(Completer.class);

				if (completer.name().contains(".")) {
					subCommandCompletions.put(completer, Utils.mapEntry(method, instance));
				} else {
					commandCompletions.put(completer, Utils.mapEntry(method, instance));
				}
			}
		}

		subCommands.forEach((key, value) -> {
			final String splitName = key.name().split("\\.")[0];
			boolean shouldThrowException = false;

			// This is an unsupported behaviour at least for now.
			// All sub-commands must have their own main command to be registered.
			if (commands.keySet().stream().noneMatch(cmd -> cmd.name().equals(splitName))) {
				shouldThrowException = true;

				unregisterCommand(key.name());
			}

			if (shouldThrowException)
				throw new IllegalStateException(String.format("You can not create sub-commands without a main command! (%s)", key.name()));
		});
	}

	/**
	 * Register the command with given parameters.
	 *
	 * @param command  of the main object
	 * @param method   that command will run
	 * @param instance of the method above
	 */
	private void registerCommand(Command command, Method method, Object instance) {
		final String cmdName = command.name();

		if (cmdName.contains(".")) {
			subCommands.put(command, Utils.mapEntry(method, instance));
		} else {
			commands.put(command, Utils.mapEntry(method, instance));

			try {
				final Constructor<PluginCommand> constructor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
				constructor.setAccessible(true);

				final PluginCommand pluginCommand = constructor.newInstance(cmdName, plugin);
				pluginCommand.setTabCompleter(this);
				pluginCommand.setExecutor(this);
				pluginCommand.setUsage(command.usage());
				pluginCommand.setPermission(!command.permission().isEmpty() ? null : command.permission());
				pluginCommand.setDescription(command.desc());
				pluginCommand.setAliases(Stream.of(command.aliases()).map(String::toLowerCase).collect(Collectors.toList()));

				commandMap.register(cmdName, pluginCommand);
			} catch (Exception exception) {
				exception.printStackTrace();
			}
		}
	}

	/**
	 * Unregisters command and tab completer if there is with the given name.
	 *
	 * @param commandName name of the command that's going to be removed
	 */
	public void unregisterCommand( String commandName) {
		if (commandName.contains(".")) commandName = commandName.split("\\.")[0];

		final Map.Entry<Command, Map.Entry<Method, Object>> entry = this.getAssociatedCommand(commandName, new String[0]);

		if (entry == null) {
			plugin.getLogger().log(Level.WARNING, "Command removal is failed because there is no command named ''{0}''!", commandName);
			return;
		}

		final Command command = entry.getKey();
		final String name = command.name();
		final PluginCommand pluginCommand = plugin.getServer().getPluginCommand(name);

		Optional.ofNullable(pluginCommand).ifPresent(cmd -> {
			try {
				cmd.unregister(commandMap);

				Field field = commandMap.getClass().getDeclaredField("knownCommands");
				field.setAccessible(true);

				@SuppressWarnings("unchecked")
				Map<String, org.bukkit.command.Command> knownCommands = (Map<String, org.bukkit.command.Command>) field.get(commandMap);
				knownCommands.remove(name);
			} catch (Exception exception) {
				plugin.getLogger().log(Level.WARNING, "Something went wrong while trying to unregister command(name: {0}) from server!", name);
			}

			this.commands.remove(command);
			new HashMap<>(this.subCommands).keySet().stream().filter(subCmd -> subCmd.name().startsWith(name)).forEach(this.subCommands::remove); // Copy elements to new map to avoid modification exception
		});
	}

	/**
	 * Unregisters commands and tab completers within the given instance class.
	 *
	 * @param instance the object using to get target class to unregister commands.
	 */
	public void unregisterCommands( Object instance) {
		for (final Method method : instance.getClass().getMethods()) {
			final Command command = method.getAnnotation(Command.class);

			if (command != null) {
				if (method.getParameterTypes().length > 0 && method.getParameterTypes()[0] != CommandArguments.class) {
					continue;
				}

				this.unregisterCommand(command.name());
			}
		}
	}

	/**
	 * Unregisters all of registered commands and tab completers created using that instance.
	 */
	public void unregisterCommands() {
		// Copy elements to new map to avoid modification exception
		new HashMap<>(commands).keySet().stream().map(Command::name).forEach(this::unregisterCommand);
	}

	
	private Map.Entry<Command, Map.Entry<Method, Object>> getAssociatedCommand( String commandName,  String[] possibleArgs) {
		Command command = null;

		// Search for the sub commands first
		for (Command cmd : subCommands.keySet()) {
			final String name = cmd.name(), cmdName = commandName + (possibleArgs.length == 0 ? "" : "." + String.join(".", Arrays.copyOfRange(possibleArgs, 0, name.split("\\.").length - 1)));

			if (name.equalsIgnoreCase(cmdName) || Stream.of(cmd.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
				command = cmd;
				break;
			}
		}

		// If we found the sub command then return it, otherwise search the commands map
		if (command != null) {
			return Utils.mapEntry(command, subCommands.get(command));
		}

		// If our command is not a sub command then search for a main command
		for (Command cmd : commands.keySet()) {
			final String name = cmd.name();

			if (name.equalsIgnoreCase(commandName) || Stream.of(cmd.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
				command = cmd;
				break;
			}
		}

		// If we found the command return it, otherwise return null
		if (command != null) {
			// Quick fix to accept any match consumer if defined
			if (command.min() >= possibleArgs.length || command.allowInfiniteArgs()) {
				return Utils.mapEntry(command, commands.get(command));
			}
		}

		// Return null if the given command is not registered by Command Framework
		return null;
	}

	private boolean hasCooldown(final CommandSender sender, final Command command) {
		if (command.cooldown() < 1) return false;

		final Map<Command, Long> cooldownMap = cooldowns.get(sender);

		if (cooldownMap == null) {
			cooldowns.put(sender, Utils.mapOf(command, System.currentTimeMillis()));
			return false;
		} else if (!cooldownMap.containsKey(command)) {
			cooldownMap.put(command, System.currentTimeMillis());

			cooldowns.replace(sender, cooldownMap);
			return false;
		}

		final int remainingTime = (int) ((System.currentTimeMillis() - cooldownMap.get(command)) / 1000) % 60;

		if (remainingTime <= command.cooldown()) {
			sender.sendMessage(String.format(WAIT_BEFORE_USING_AGAIN, command.cooldown() - remainingTime));
			return true;
		} else {
			cooldownMap.put(command, System.currentTimeMillis());

			cooldowns.replace(sender, cooldownMap);
			return false;
		}
	}

	@Override
	public boolean onCommand( CommandSender sender,  org.bukkit.command.Command cmd,  String label, String[] args) {
		final Map.Entry<Command, Map.Entry<Method, Object>> entry = this.getAssociatedCommand(cmd.getName(), args);

		if (entry == null) {
			if (matchFunction != null) return matchFunction.apply(new CommandArguments(sender, cmd, label, args));

			return true;
		}

		final Command command = entry.getKey();
		final String permission = command.permission();

		if (command.onlyOp() && !sender.isOp()) {
			sender.sendMessage(MUST_HAVE_OP);
			return true;
		}

		if ((!permission.isEmpty() && !sender.hasPermission(permission))) {
			sender.sendMessage(NO_PERMISSION);
			return true;
		}

		if (command.senderType() == Command.SenderType.PLAYER && !(sender instanceof Player)) {
			sender.sendMessage(ONLY_BY_PLAYERS);
			return true;
		}

		if (command.senderType() == Command.SenderType.CONSOLE && sender instanceof Player) {
			sender.sendMessage(ONLY_BY_CONSOLE);
			return true;
		}

		if (this.hasCooldown(sender, command)) return true;

		final String[] splitted = command.name().split("\\."), newArgs = Arrays.copyOfRange(args, splitted.length - 1, args.length);

		if (args.length >= command.min() + splitted.length - 1 && newArgs.length <= (command.max() == -1 ? newArgs.length + 1 : command.max())) {
			final Runnable invocation = () -> {
				try {
					entry.getValue().getKey().invoke(entry.getValue().getValue(), new CommandArguments(sender, cmd, label, newArgs));
				} catch (IllegalAccessException | InvocationTargetException e) {
					e.printStackTrace();
				}
            };

			if (command.async()) {
				plugin.getServer().getScheduler().runTaskAsynchronously(plugin, invocation);
			} else {
				invocation.run();
			}
        } else {
			sender.sendMessage(SHORT_OR_LONG_ARG_SIZE);
        }

        return true;
    }

	
	private Map.Entry<Completer, Map.Entry<Method, Object>> getAssociatedCompleter( String commandName,  String[] possibleArgs) {
		Completer completer = null;

		for (Completer comp : subCommandCompletions.keySet()) {
			final String name = comp.name(), cmdName = commandName + (possibleArgs.length == 0 ? "" : "." + String.join(".", Arrays.copyOfRange(possibleArgs, 0, name.split("\\.").length - 1)));

			if (name.equalsIgnoreCase(cmdName) || Stream.of(comp.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
				completer = comp;
				break;
			}
		}

		if (completer != null) {
			return Utils.mapEntry(completer, subCommandCompletions.get(completer));
		}

		for (Completer comp : commandCompletions.keySet()) {
			final String name = comp.name();

			if (name.equalsIgnoreCase(commandName) || Stream.of(comp.aliases()).anyMatch(commandName::equalsIgnoreCase)) {
				completer = comp;
				break;
			}
		}

		if (completer != null) {
			return Utils.mapEntry(completer, commandCompletions.get(completer));
		}

		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> onTabComplete( CommandSender sender,  org.bukkit.command.Command cmd,  String label, String[] args) {
		final Map.Entry<Completer, Map.Entry<Method, Object>> entry = this.getAssociatedCompleter(cmd.getName(), args);

		if (entry == null) return null;

		final String permission = entry.getKey().permission();

		if (!permission.isEmpty() && !sender.hasPermission(permission)) return null;

		try {
			final Object instance = entry.getValue().getKey().invoke(entry.getValue().getValue(), new CommandArguments(sender, cmd, label, args));

			return (List<String>) instance;
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Get a copy of registered sub-commands.
	 *
	 * @return list of sub-commands.
	 */
	
	public List<Command> getSubCommands() {
		return new ArrayList<>(this.subCommands.keySet());
	}

	/**
	 * Get a copy of registered commands and sub-commands.
	 *
	 * @return list of commands and sub-commands.
	 */
	
	public List<Command> getCommands() {
		List<Command> commands = new ArrayList<>(this.commands.keySet());
		commands.addAll(this.subCommands.keySet());

		return commands;
	}
}