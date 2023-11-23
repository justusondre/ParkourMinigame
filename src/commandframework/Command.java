package commandframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Command {

	/**
	 * The name of the command. If command would be a sub command then
	 * sub command's name must be separated by dot. For example like the
	 * {@code "command.subcommand"}
	 *
	 * @return name of the command or subcommand
	 */
	String name();

	/**
	 * The permission that sender has to have to execute command.
	 *
	 * @return name of the permission
	 */
	String permission() default "";

	/**
	 * An alternative name list of command. Check {@link #name()}
	 * to understand how command names work.
	 *
	 * @return aliases list of the command
	 */
	String[] aliases() default {};

	/**
	 * The description of the command that will be showed when sender executes
	 * Bukkit's help command.
	 *
	 * @return description of the command
	 */
	String desc() default "";

	/**
	 * The usage of the command that will be showed when sender executes
	 * command without required or missing arguments.
	 *
	 * @return usage of the command.
	 */
	String usage() default "";

	/**
	 * Minimum value of arguments.
	 *
	 * @return minimum value of arguments.
	 */
	int min() default 0;

	/**
	 * Maximum value of arguments. -1 for infinite.
	 *
	 * @return maximum value of arguments.
	 */
	int max() default -1;

	/**
	 * The time between using command again. Use a negative
	 * integer for no cooldown.
	 *
	 * @return value of time to use command again.
	 */
	int cooldown() default -1;

	/**
	 * Allows command to take infinite arguments.
	 *
	 * @return allow infinite arguments.
	 */
	boolean allowInfiniteArgs() default false;

	/**
	 * Only op players can execute this command.
	 * <p>
	 * Permissions will be ignored if this option
	 * is enabled.
	 *
	 * @return allow only op players.
	 */
	boolean onlyOp() default false;

	/**
	 * This option makes command to execute in a separate thread
	 * but involves HIGH RISKS because the Bukkit API, except the
	 * scheduler package, is not thread safe nor guaranteed to be
	 * thread safe.
	 *
	 * @return asynchronous execution of command.
	 */
	boolean async() default false;

	/**
	 * Enum value of command sender type to define who will
	 * use the command.
	 *
	 * @return enum value of {@link SenderType}
	 */
	SenderType senderType() default SenderType.BOTH;

	enum SenderType {
		BOTH, CONSOLE, PLAYER
	}
}