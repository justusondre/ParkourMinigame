package game.commands;

import java.util.List;

import org.bukkit.entity.Player;

import commandframework.Command;
import commandframework.CommandArguments;
import game.Main;
import game.arena.Arena;
import game.arena.ArenaState;
import game.user.User;

public class PlayerCommands extends AbstractCommand {
	public PlayerCommands(Main plugin) {
		super(plugin);
	}
	
	@Command(name = "top.join", senderType = Command.SenderType.PLAYER)
	public void mmJoinCommand(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		if (arguments.isArgumentsEmpty()) {
			user.sendMessage("admin-commands.provide-an-arena-name");
			return;
		}
		Arena arena = this.plugin.getArenaRegistry().getArena(arguments.getArgument(0));
		if (arena == null) {
			user.sendMessage("admin-commands.no-arena-found-with-that-name");
			return;
		}
		this.plugin.getArenaManager().joinAttempt(user, arena);
	}

	@Command(name = "top.randomjoin", senderType = Command.SenderType.PLAYER)
	public void mmRandomJoinCommand(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		List<Arena> arenas = this.plugin.getArenaRegistry().getArenas().stream().filter(
				arena -> (arena.isArenaState(new ArenaState[] { ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING })
						&& arena.getPlayers().size() < arena.getMaximumPlayers()))
				.toList();
		if (!arenas.isEmpty()) {
			Arena arena = arenas.get(0);
			this.plugin.getArenaManager().joinAttempt(user, arena);
			return;
		}
		user.sendMessage("player-commands.no-free-arenas");
	}

	@Command(name = "top.leave", senderType = Command.SenderType.PLAYER)
	public void mmLeaveCommand(CommandArguments arguments) {
		User user = this.plugin.getUserManager().getUser((Player) arguments.getSender());
		Arena arena = user.getArena();
		if (arena == null) {
			user.sendMessage("messages.arena.not-playing");
			return;
		}
		this.plugin.getArenaManager().leaveAttempt(user, arena);
	}
}
