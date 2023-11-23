package game.arena;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import game.Main;
import game.scoreboard.ScoreboardStatus;
import game.user.User;
import game.utility.FontConverter;
import game.utility.StringUtils;
import game.GameIdGenerator;

public final class ArenaManager {
	
	private final Main plugin;
	
    String formattedDate = new SimpleDateFormat("dd/MM/YY").format(new Date());
    String id = GameIdGenerator.generateUniqueArenaId();

	public ArenaManager(Main plugin) {
		this.plugin = plugin;
	}

	public Main plugin() {
		return this.plugin;
	}

	public void joinAttempt(User user, Arena arena) {
		if (!arena.isReady()) {
			user.sendMessage(ChatColor.RED + "The arena is not ready yet!");
			return;
		}
		
		if (user.isInArena()) {
			user.sendMessage(ChatColor.RED + "You are already in an arena!");
			return;
		}
		
		if (arena.getArenaState() == ArenaState.RESTARTING) {
			user.sendMessage(ChatColor.RED + "The arena is restarting!");
			return;
		}
		
		Player player = user.getPlayer();
		
		//ArenaUtils.updateNameTagsVisibility(user);
		arena.addUser(user);
		arena.teleportToLobby(user);
		ScoreboardStatus status = new ScoreboardStatus(user);
		Objective objective = status.getObjective();
		startGlitchingTitleAnimation(objective);
	    arena.getStatus().put(user.getUniqueId(), status);
		arena.updateSigns();
		player.setLevel(0);
		player.setExp(0.0F);
		player.setFoodLevel(20);
		player.getInventory().clear();
		player.getInventory().setHeldItemSlot(0);
		player.getInventory().setArmorContents(null);
		player.setGameMode(GameMode.ADVENTURE);
		player.setAllowFlight(false);
		player.setGlowing(false);
		user.heal();
		user.removePotionEffectsExcept(new PotionEffectType[0]);
		arena.broadcastMessage(ChatColor.AQUA + user.getPlayer().getDisplayName() + ChatColor.GRAY + " has joined the race to the Capitol!");
		
		if (arena.isArenaState(new ArenaState[] { ArenaState.IN_GAME, ArenaState.ENDING })) {
			return;
		}
	}

	public void leaveAttempt(User user, Arena arena) {
		Player player = user.getPlayer();
		arena.broadcastMessage(ChatColor.AQUA + user.getPlayer().getDisplayName() + ChatColor.GRAY + " has left the game!");
		arena.removeUser(user);
		arena.removeSpectator(user);
		arena.teleportToEndLocation(user);
		arena.getStatus().clear();
		arena.updateSigns();
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
		user.clearPlayerScoreboard(player);
		player.setFoodLevel(20);
		player.setLevel(0);
		player.setExp(0.0F);
		player.setFlying(false);
		player.setAllowFlight(false);
		player.setFlySpeed(0.1F);
		player.setWalkSpeed(0.2F);
		player.setFireTicks(0);
		player.setGameMode(GameMode.SURVIVAL);
		player.getInventory().setHeldItemSlot(0);
		user.heal();

		user.setSpectator(false);
		user.removePotionEffectsExcept(new PotionEffectType[0]);
		
		if (arena.getArenaState() == ArenaState.IN_GAME && arena.getPlayersLeft().size() <= 1) {
			stopGame(false, arena);	
		}
	}

	public void stopGame(boolean quickStop, Arena arena) {
		arena.setArenaState(ArenaState.ENDING);
		arena.setTimer(quickStop ? 2 : ArenaOption.LOBBY_ENDING_TIME.getIntegerValue());
		arena.showPlayers();
		
		for (User user : arena.getPlayers()) {
			user.removePotionEffectsExcept(new PotionEffectType[] { PotionEffectType.BLINDNESS });
			leaveAttempt(user, arena);
			
		}
		
		if (quickStop) 
			return;
		
	}
	
	public Scoreboard createScoreboard() {
        return org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard();
        
    }
    
    public void updateTitle(User user, Scoreboard scoreboard) {
    	ScoreboardStatus scoreboardStatus = new ScoreboardStatus(user);
		Objective objective = scoreboardStatus.getObjective();
	    startGlitchingTitleAnimation(objective);
    }
    
	public void updateWaitingObjectives(Arena arena, ScoreboardStatus scoreboard) {
	
		User user = scoreboard.getPlayer();

		if (arena.getArenaState() == ArenaState.WAITING_FOR_PLAYERS || arena.getArenaState() == ArenaState.STARTING) {
			
			scoreboard.updateLine(8, ChatColor.DARK_GRAY + "" + ChatColor.BOLD + "ѕᴛᴀᴛᴜѕ: " + ChatColor.GRAY + "" + FontConverter.convertFont(arena.getArenaState().name.toString()));
			scoreboard.updateLine(7, "");
			scoreboard.updateLine(6, "ᴍᴀᴘ: " + ChatColor.GREEN + "ᴛʜᴇ ᴛʀᴀɪɴ");
			scoreboard.updateLine(5, "ᴘʟᴀʏᴇʀѕ: " + ChatColor.GREEN + arena.getPlayers().size());
			scoreboard.updateLine(4, "");
			scoreboard.updateLine(3, "ѕᴛᴀʀᴛɪɴɢ ɪɴ " + ChatColor.GREEN + arena.getTimer() + "ѕ");
			scoreboard.updateLine(2, "");
			scoreboard.updateLine(1, ChatColor.AQUA + "ᴍᴄ.ᴛᴀʟᴇѕᴏꜰᴘᴀɴᴇᴍ.ɴᴇᴛ");
		}

		if (arena.getArenaState() == ArenaState.IN_GAME) {
		    scoreboard.updateLine(12, ChatColor.GOLD + "" + ChatColor.BOLD + "ѕᴛᴀᴛᴜѕ: " + ChatColor.GRAY + "" + FontConverter.convertFont(arena.getArenaState().name.toString()));
		    scoreboard.updateLine(11, "");
		    scoreboard.updateLine(10, ChatColor.GOLD + "" + ChatColor.BOLD + "▶ " + ChatColor.GOLD + "" + ChatColor.BOLD + "ᴛᴏᴘ ᴘʟᴀʏᴇʀѕ" + ChatColor.GOLD + "" + ChatColor.BOLD + " ◀");
		    scoreboard.updateLine(9, "");

		    List<User> top3PlayersByDistance = arena.getTop3PlayersByDistanceToParkourEnd();
		    
		    if (arena.getCompletedPlayers().size() == 0) {
		        for (int i = 0; i < 3; i++) {
		            String playerName;
		            if (i < top3PlayersByDistance.size() && top3PlayersByDistance.get(i) != null) {
		                playerName = ChatColor.YELLOW + top3PlayersByDistance.get(i).getName();
		            } else {
		                playerName = "ᴇᴍᴘᴛʏ :(";
		            }
		            scoreboard.updateLine(8 - i, ChatColor.GRAY + "" + ChatColor.BOLD + " " + (i + 1) + ". " + playerName);
		        }
		    }
		    
		    if (arena.getCompletedPlayers().size() == 1) {
		        scoreboard.updateLine(8, ChatColor.AQUA + "" + ChatColor.BOLD + " " + (1) + ". " + ChatColor.YELLOW + arena.getCompletedPlayers().get(0).getName());

		        for (int i = 0; i < 2; i++) {
		            String playerName;
		            if (i < top3PlayersByDistance.size() && top3PlayersByDistance.get(i) != null) {
		                playerName = ChatColor.YELLOW + top3PlayersByDistance.get(i).getName();
		            } else {
		                playerName = "ᴇᴍᴘᴛʏ :(";
		            }
		            scoreboard.updateLine(7 - i, ChatColor.GRAY + "" + ChatColor.BOLD + " " + (2 + i) + ". " + playerName);
		        }
		    }
		    
		    if (arena.getCompletedPlayers().size() == 2) {
		        scoreboard.updateLine(8, ChatColor.AQUA + "" + ChatColor.BOLD + " " + (1) + ". " + ChatColor.YELLOW + arena.getCompletedPlayers().get(0).getName());
		        scoreboard.updateLine(7, ChatColor.GOLD + "" + ChatColor.BOLD + " " + (2) + ". " + ChatColor.YELLOW + arena.getCompletedPlayers().get(1).getName());

		        if (top3PlayersByDistance.size() > 0 && top3PlayersByDistance.get(0) != null) {
		            scoreboard.updateLine(6, ChatColor.GRAY + "" + ChatColor.BOLD + " " + (3) + ". " + ChatColor.YELLOW + top3PlayersByDistance.get(0).getName());
		        } else {
		            scoreboard.updateLine(6, ChatColor.GRAY + "" + ChatColor.BOLD + " " + (3) + ". " + "ᴇᴍᴘᴛʏ :(");
		        }
		    }
		    
		    if (arena.getCompletedPlayers().size() >= 3) {
		        scoreboard.updateLine(8, ChatColor.AQUA + "" + ChatColor.BOLD + " " + (1) + ". " + ChatColor.YELLOW + arena.getCompletedPlayers().get(0).getName());
		        scoreboard.updateLine(7, ChatColor.GOLD + "" + ChatColor.BOLD + " " + (2) + ". " + ChatColor.YELLOW + arena.getCompletedPlayers().get(1).getName());
		        scoreboard.updateLine(6, ChatColor.GREEN + "" + ChatColor.BOLD + " " + (3) + ". " + ChatColor.YELLOW + arena.getCompletedPlayers().get(2).getName());
		    }

		    scoreboard.updateLine(5, "");
		    scoreboard.updateLine(4, "ᴅɪѕᴛᴀɴᴄᴇ: " + ChatColor.RED + Math.round(arena.calculateDistanceToParkourEnd(user)) + FontConverter.convertFont("m"));
		    scoreboard.updateLine(3, "ᴛɪᴍᴇ ʀᴇᴍᴀɪɴɪɴɢ: " + ChatColor.GREEN + StringUtils.formatIntoMMSS(arena.getTimer()));
		    scoreboard.updateLine(2, "");
		    scoreboard.updateLine(1, ChatColor.AQUA + "ᴍᴄ.ᴛᴀʟᴇѕᴏꜰᴘᴀɴᴇᴍ.ɴᴇᴛ");
		}
	}

    public void clearScoreboard(Player player) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(this.plugin, new Runnable() {
            public void run() {
                player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            }
        }, 0L);
    }
    
    public void startGlitchingTitleAnimation(Objective objective) {
        BukkitRunnable animationTask = new BukkitRunnable() {
            int tick = 0;
            boolean isGlitching = false;

            @Override
            public void run() {
                tick++;

                if (tick % 200 == 0) {
                    isGlitching = true;
                }

                if (isGlitching) {
                    if (tick % 20 >= 0 && tick % 20 < 10) { // Glitch for 1 second (20 ticks)
                        if (tick % 2 == 0) { // Switch every 2 ticks
                            objective.setDisplayName(ChatColor.WHITE + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + " ᴛᴀʟᴇѕ ᴏꜰ ᴘᴀɴᴇᴍ ");
                        } else {
                            objective.setDisplayName(ChatColor.RED + "" + ChatColor.ITALIC + "" + ChatColor.BOLD + " ᴛᴀʟᴇѕ ᴏꜰ ᴘᴀɴᴇᴍ ");
                        }
                    } else {
                        objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + " ᴛᴀʟᴇѕ ᴏꜰ ᴘᴀɴᴇᴍ ");
                        isGlitching = false;
                    }
                } else {
                    objective.setDisplayName(ChatColor.GOLD + "" + ChatColor.BOLD + " ᴛᴀʟᴇѕ ᴏꜰ ᴘᴀɴᴇᴍ ");
                }
            }
        };
        animationTask.runTaskTimer(this.plugin, 0L, 1L); // Run every tick
    }
}
