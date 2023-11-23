package commandframework;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;

import commandframework.utils.Utils;

public class CommandArguments {

	private final CommandSender commandSender;
	private final Command command;
	private final String label, arguments[];

	public CommandArguments(CommandSender commandSender, Command command, String label, String... arguments) {
		this.commandSender = commandSender;
		this.command = command;
		this.label = label;
		this.arguments = arguments;
	}

	/**
	 * Do not try to cast objects except subclasses of {@link CommandSender}
	 * otherwise {@link ClassCastException} will occur. Also casting for {@link Player}
	 * or {@link org.bukkit.command.ConsoleCommandSender} isn't needed.
	 *
	 * @return sender of command as Player or CommandSender
	 */
	@SuppressWarnings("unchecked")
	public <T extends CommandSender> T getSender() {
		return (T) commandSender;
	}

	/**
	 * @return base command.
	 */
	public Command getCommand() {
		return command;
	}

	/**
	 * @return label of the command.
	 */
	
	public String getLabel() {
		return label;
	}

	/**
	 * @return arguments of the command.
	 */
	
	public String[] getArguments() {
		return arguments;
	}

	// SOME GETTER METHODS FOR COMMON PRIMITIVE TYPES //

	/**
	 * @param i index
	 * @return indexed element or null if index out of bounds
	 */
	
	public String getArgument(int i) {
		return arguments.length > i && i >= 0 ? arguments[i] : null;
	}

	/**
	 * @param i index
	 * @return Integer if indexed element is primitive type of int
	 * or 0 if element is null.
	 */
	
	public Integer getArgumentAsInt(int i) {
		return Utils.getInt(this.getArgument(i));
	}

	/**
	 * @param i index
	 * @return Double if indexed element is primitive type of double
	 * or 0 if element is null.
	 */
	
	public Double getArgumentAsDouble(int i) {
		return Utils.getDouble(this.getArgument(i));
	}

	/**
	 * @param i index
	 * @return Float if indexed element is primitive type of float
	 * or 0 if element is null.
	 */
	
	public Float getArgumentAsFloat(int i) {
		return Utils.getFloat(this.getArgument(i));
	}

	/**
	 * @param i index
	 * @return Long if indexed element is primitive type of long
	 * or 0 if element is null.
	 */
	
	public Long getArgumentAsLong(int i) {
		return Utils.getLong(this.getArgument(i));
	}

	/**
	 * @param i index
	 * @return Boolean if indexed element is primitive type of boolean
	 * or 0 if element is null.
	 */
	
	public Boolean getArgumentAsBoolean(int i) {
		final String arg = this.getArgument(i);
		return arg != null && arg.equalsIgnoreCase("true");
	}

	// ---------------------------------------------- //

	/**
	 * @return true if command arguments are empty otherwise false
	 */
	public boolean isArgumentsEmpty() {
		return arguments.length == 0;
	}

	/**
	 * Sends message to sender without receiving command
	 * sender.
	 *
	 * @param message to send
	 */
	public void sendMessage(String message) {
		if (message == null) return;
		commandSender.sendMessage(message);
	}

	/**
	 * Checks if command sender is console.
	 *
	 * @return true if sender is console otherwise false
	 */
	public boolean isSenderConsole() {
		return !isSenderPlayer();
	}

	/**
	 * Checks if command sender is player.
	 *
	 * @return true if sender is player otherwise false
	 */
	public boolean isSenderPlayer() {
		return commandSender instanceof Player;
	}

	/**
	 * Checks if command sender has specified permission.
	 *
	 * @param permission to check
	 * @return true if sender has permission otherwise false
	 */
	public boolean hasPermission(String permission) {
		return commandSender.hasPermission(permission);
	}

	/**
	 * @return length of the arguments
	 */
	public int getArgumentsLength() {
		return arguments.length;
	}
}