package game.arena;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;
import org.bukkit.scoreboard.Team;

import game.Main;
import game.user.User;

public class ArenaUtils {
	
	private static final Main plugin = (Main) JavaPlugin.getPlugin(Main.class);

	public static void updateNameTagsVisibility(User u) {
		for (Player player : plugin.getServer().getOnlinePlayers()) {
			User user = plugin.getUserManager().getUser(player);
			Arena arena = user.getArena();
			if (arena == null)
				continue;
			Scoreboard scoreboard = player.getScoreboard();
			ScoreboardManager scoreboardManager = plugin.getServer().getScoreboardManager();
			if (scoreboard == scoreboardManager.getMainScoreboard())
				scoreboard = scoreboardManager.getNewScoreboard();
			Team team = scoreboard.getTeam("MMHide");
			if (team == null)
				team = scoreboard.registerNewTeam("MMHide");
			team.setCanSeeFriendlyInvisibles(false);
			team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
			if (arena.isArenaState(new ArenaState[] { ArenaState.WAITING_FOR_PLAYERS, ArenaState.STARTING })
					|| arena.getArenaState() == ArenaState.IN_GAME) {
				team.addEntry(u.getName());
			} else {
				team.removeEntry(u.getName());
			}
			player.setScoreboard(scoreboard);
		}
	}
}