package game.events;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import game.Main;
import game.arena.Arena;
import game.arena.ArenaState;
import game.user.User;

public class JoinQuitEvents implements Listener {

	@EventHandler
	public void onLogin(PlayerLoginEvent e) {
		e.setResult(PlayerLoginEvent.Result.ALLOWED);
	}

	@EventHandler
	public void onJoinEvent(PlayerJoinEvent event) {
		Player eventPlayer = event.getPlayer();
		User user = Main.getInstance().getUserManager().getUser(event.getPlayer());
		
		for (User targetUser : Main.getInstance().getUserManager().getUsers()) {
			if (!targetUser.isInArena())
				continue;
			
		}
	}

	@EventHandler
	public void onQuitEvent(PlayerQuitEvent event) {
		handleQuitEvent(event.getPlayer());
	}

	@EventHandler
	public void onKickEvent(PlayerKickEvent event) {
		handleQuitEvent(event.getPlayer());
	}

	private void handleQuitEvent(Player player) {
		User user = Main.getInstance().getUserManager().getUser(player);
		Arena arena = user.getArena();
		
		if (arena != null && arena.isArenaState(new ArenaState[] { ArenaState.IN_GAME, ArenaState.WAITING_FOR_PLAYERS })) {
			Main.getInstance().getArenaManager().leaveAttempt(user, arena);
		}
		Main.getInstance().getUserManager().removeUser(player);
	}
}
