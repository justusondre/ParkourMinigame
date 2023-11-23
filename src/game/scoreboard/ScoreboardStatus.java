package game.scoreboard;

import java.util.LinkedHashMap;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import game.user.User;

public class ScoreboardStatus {
	private User user;
	private Objective obj;
	private Scoreboard board;
	private LinkedHashMap<Integer, ScoreboardLine> entries;

	@SuppressWarnings("deprecation")
	public ScoreboardStatus(User user) {
		this.user = user;
		this.board = Bukkit.getScoreboardManager().getNewScoreboard();
		this.obj = this.board.registerNewObjective("status", "dummy");
		this.obj.setDisplaySlot(DisplaySlot.SIDEBAR);
		this.entries = new LinkedHashMap<>();
		user.getPlayer().setScoreboard(this.board);
	}

	public User getPlayer() {
		return this.user;
	}

	public Objective getObjective() {
		return this.obj;
	}

	public void setTitle(String title) {
		this.obj.setDisplayName(title);
	}

	public void reset() {
		this.entries.values().forEach(ScoreboardLine::unregister);
		this.entries.clear();
	}

	public void updateLine(int line, String text) {
		if (this.entries.get(Integer.valueOf(line)) != null) {
			((ScoreboardLine) this.entries.get(Integer.valueOf(line))).update(text);
		} else {
			this.entries.put(Integer.valueOf(line), new ScoreboardLine(this.board, this.obj, text, line, line));
		}
	}

	public void updateLine(int line, int score, String text) {
		if (this.entries.get(Integer.valueOf(line)) != null) {
			((ScoreboardLine) this.entries.get(Integer.valueOf(line))).update(text);
		} else {
			this.entries.put(Integer.valueOf(line), new ScoreboardLine(this.board, this.obj, text, line, score));
		}
	}
}
