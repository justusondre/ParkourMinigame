package commandframework;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Completer {

	/**
	 * The name of the command. If command would be a sub command then
	 * sub command's name must be separated by dot. For example like the
	 * {@code "command.subcommand"}
	 *
	 * @return name of the command or subcommand
	 */
	String name();

	/**
	 * The permission that sender must have to receive tab complete.
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
}