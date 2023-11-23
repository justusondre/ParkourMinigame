package game;

import java.util.Iterator;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import game.arena.Arena;
import game.arena.ArenaState;
import game.song.Instrument;
import game.song.Layer;
import game.song.Note;
import game.song.NotePitch;
import game.song.Song;
import game.user.User;
import game.utility.NumberUtils;

public class UpdateTask extends BukkitRunnable {
	private Main main;

	private long ticks = 0L;

	public UpdateTask(Main main) {
		this.main = main;
		runTaskTimer((Plugin) main, 1L, 1L);

	}

	public void run() {
		for (Iterator<Arena> iterator = this.main.getArenaRegistry().getArenas().iterator(); iterator.hasNext();) {
			Arena a = iterator.next();
			Song song = a.getSong();
			if (song != null) {
				song.setTick(song.getTick() + 1);
				if (song.getTick() >= 0 && song.getTick() % song.getDelay() == 0) {
					song.setFrame(song.getFrame() + 1);
					if (song.getFrame() > song.getLength()) {
						a.getSongs().remove(song);
						a.setSong(a.getSongs().get(NumberUtils.random().nextInt(a.getSongs().size())));
						return;
					}

					for (User user : a.getPlayers()) {
						for (Layer l : song.getLayer().values()) {
							Note note = l.getNote(song.getFrame());
							if (note == null)
								continue;

							user.getPlayer().playSound(user.getLocation(),
									Instrument.getInstrument(note.getInstrument().byteValue()),
									(l.getVolume() * 10000) / 1000000.0F,
									NotePitch.getPitch(note.getKey().byteValue() - 33));
						}
					}
				}
			}

			try {
			    if (a.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || this.ticks % 20L == 0L) {
		            a.getStatus().values().forEach(s -> this.main.getArenaManager().updateWaitingObjectives(a, s));
			    }
			    
			} catch (Exception e) {
				a.stop();
			    e.printStackTrace();
			}
			this.ticks++;
		}
	}
}