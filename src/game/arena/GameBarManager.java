package game.arena;

import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

import game.Main;
import game.user.User;

public class GameBarManager {
	
	private BossBar gameBar;

	private final Arena arena;

	private final Main plugin;

	public GameBarManager(Arena arena, Main plugin) {
		this.arena = arena;
		this.plugin = plugin;
		this.gameBar = plugin.getServer().createBossBar("", BarColor.BLUE, BarStyle.SOLID, new org.bukkit.boss.BarFlag[0]);
	}

	public void doBarAction(User user, int action) {
		Player player = user.getPlayer();
		if (action == 1) {
			this.gameBar.addPlayer(player);
		} else {
			this.gameBar.removePlayer(player);
		}
	}

	public void removeAll() {
		if (this.gameBar != null) {
			this.gameBar.removeAll();
		}
	}

	public void handleGameBar() {
		if (this.gameBar == null) {
			return;
		}
		
		switch (this.arena.getArenaState()) {
		case WAITING_FOR_PLAYERS:
			setTitle("game-bar.waiting-for-players");
			break;
		case STARTING:
			setTitle("game-bar.starting");
			break;
		case IN_GAME:
			setTitle("game-bar.in-game");
			break;
		case ENDING:
			setTitle("game-bar.ending");
			break;
		default:
			break;
		}
		this.gameBar.setVisible(!this.gameBar.getTitle().isEmpty());
	}

	private void setTitle(String path) {
		this.gameBar.setTitle("");
	}
}
