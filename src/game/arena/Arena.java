package game.arena;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import game.Main;
import game.scoreboard.ScoreboardStatus;
import game.sign.SignManager;
import game.song.Song;
import game.user.User;
import game.utility.MiscUtils;
import game.utility.NumberUtils;
import game.utility.Titles;
import game.utility.XSound;
import net.md_5.bungee.api.ChatColor;

public class Arena extends BukkitRunnable {
	
  private Song song;	
  private static final Main plugin = (Main)JavaPlugin.getPlugin(Main.class); 
  private final String id; 
  private final Map<ArenaOption, Integer> arenaOptions; 
  private final Map<GameLocation, Location> gameLocations;       
  private final List<User> players; 
  private final List<User> spectators; 
  private final List<User> deaths;
  private final GameBarManager gameBarManager;   
  private boolean ready;
  private boolean forceStart;
  private String mapName;
  private ArenaState arenaState = ArenaState.INACTIVE;
  private List<Location> playerSpawnPoints;
  private List<Song> songs = new LinkedList<>();
  private LinkedHashMap<UUID, ScoreboardStatus> status = new LinkedHashMap<>();
  private boolean isStarted;
  private List<User> completedPlayer = new LinkedList<>();
  
  public Arena(String id) {
    this.id = id;
    this.mapName = id;
    this.players = new ArrayList<>();
    this.spectators = new ArrayList<>();
    this.deaths = new ArrayList<>();
    this.playerSpawnPoints = new ArrayList<>();
    this.arenaOptions = new EnumMap<>(ArenaOption.class);
    this.gameLocations = new EnumMap<>(GameLocation.class);
    this.gameBarManager = new GameBarManager(this, plugin);
    
    for (ArenaOption option : ArenaOption.values())
      this.arenaOptions.put(option, Integer.valueOf(option.getIntegerValue())); 
  }
  
  public boolean isInArena(User user) {
    return (user != null && this.players.contains(user));
  }
  
  public boolean isArenaState(ArenaState... arenaStates) {
    for (ArenaState state : arenaStates) {
      if (this.arenaState == state)
        return true; 
    } 
    return false;
  }
  
  private void teleportToGameLocation(User user, GameLocation gameLocation) {
    if (!validateLocation(gameLocation)) {
      return; 
      
    }
    
    Player player = user.getPlayer();
    user.removePotionEffectsExcept(new PotionEffectType[] { PotionEffectType.BLINDNESS });
    player.setFoodLevel(20);
    player.setFlying(false);
    player.setAllowFlight(false);
    player.setFlySpeed(0.1F);
    player.setWalkSpeed(0.2F);
    player.teleport(this.gameLocations.get(gameLocation));
    
  }
  
  public void teleportToLobby(User user) {
    teleportToGameLocation(user, GameLocation.LOBBY);
  }
  
  public void teleportToEndLocation(User user) {
    teleportToGameLocation(user, GameLocation.END);
  }
  
  public LinkedHashMap<UUID, ScoreboardStatus> getStatus() {
	    return this.status;
  }
  
  public boolean isStarted() {
	   	return this.isStarted;
  }
  
  public GameBarManager getGameBar() {
    return this.gameBarManager;
  }
  
  public ArenaState getArenaState() {
    return this.arenaState;
  }
  
  public void setArenaState(ArenaState arenaState) {
    this.arenaState = arenaState;
    this.gameBarManager.handleGameBar();
    updateSigns();
  }
  
  public void addCompletedPlayer(User user) {
      completedPlayer.add(user);
  }

  public void removeCompletedPlayer(User user) {
      completedPlayer.remove(user);
  }
  
  public boolean isReady() {
    return this.ready;
  }
  
  public void setReady(boolean ready) {
    this.ready = ready;
  }
  
  public int getSetupProgress() {
    return this.ready ? 100 : 0;
  }
  
  public int getClassicGameplayTime() {
    return getOption(ArenaOption.GAMEPLAY_TIME);
  }
  
  public String getMapName() {
    return this.mapName;
  }
  
  public void setMapName(String mapName) {
    this.mapName = mapName;
  }
  
  public Song getSong() {
		return this.song;
  }

  public List<Song> getSongs() {
		return this.songs;
  }

  public void setSong(Song song) {
		this.song = song;
  }
  
  public int getTimer() {
    return getOption(ArenaOption.TIMER);
  }
  
  public void setTimer(int timer) {
    setOptionValue(ArenaOption.TIMER, timer);
  }
  
  public int getMaximumPlayers() {
    return getOption(ArenaOption.MAXIMUM_PLAYERS);
  }
  
  public void setMaximumPlayers(int maximumPlayers) {
    setOptionValue(ArenaOption.MAXIMUM_PLAYERS, maximumPlayers);
  }
  
  public int getMinimumPlayers() {
    return getOption(ArenaOption.MINIMUM_PLAYERS);
  }
  
  public void setMinimumPlayers(int minimumPlayers) {
    setOptionValue(ArenaOption.MINIMUM_PLAYERS, minimumPlayers);
  }
  
  public List<Location> getPlayerSpawnPoints() {
    return this.playerSpawnPoints;
  }
  
  public void setPlayerSpawnPoints(List<Location> playerSpawnPoints) {
    this.playerSpawnPoints = playerSpawnPoints;
  }
  
  public Location getLobbyLocation() {
    return this.gameLocations.get(GameLocation.LOBBY);
  }
  
  public void setLobbyLocation(Location lobbyLocation) {
    this.gameLocations.put(GameLocation.LOBBY, lobbyLocation);
  }
  
  public Location getEndLocation() {
    return this.gameLocations.get(GameLocation.END);
  }
  
  public void setEndLocation(Location endLocation) {
    this.gameLocations.put(GameLocation.END, endLocation);
  }
  
  public Location getParkourEndLocation() {
	return this.gameLocations.get(GameLocation.PARKOUREND);
  }

  public void setParkourEndLocation(Location parkourEndLocation) {
	this.gameLocations.put(GameLocation.PARKOUREND, parkourEndLocation);
	}
  
  public void updateSigns() {
    SignManager signManager = plugin.getSignManager();
    if (signManager == null)
      return; 
    signManager.updateSign(this);
  }
  
  public List<User> getPlayers() {
    return this.players;
  }
  
  public void addUser(User user) {
    this.players.add(user);
  }
  
  public void removeUser(User user) {
    this.players.remove(user);
  }
  
  public List<User> getDeaths() {
    return this.deaths;
  }
  
  public boolean isForceStart() {
    return this.forceStart;
  }
  
  public void setForceStart(boolean forceStart) {
    this.forceStart = forceStart;
  }
  
  public List<User> getAllPlayers() {
    return this.players;
  }
  
  public boolean isPlayerAlive(User user) {
    for (User u : getPlayersLeft()) {
      if (u.equals(user) && this.players.contains(u) && !isDeathPlayer(u))
        return true; 
    } 
    return false;
  }
  
  public void addDeathPlayer(User user, boolean lastWord) {
    this.deaths.add(user);
    if (lastWord)
    addSpectator(user);
  }
  
  public void addDeathPlayer(User user) {
    addDeathPlayer(user, true);
  }
  
  public boolean isDeathPlayer(User user) {
    return this.deaths.contains(user);
  }
  
  public void addSpectator(User user) {
    this.spectators.add(user);
  }
  
  public void removeSpectator(User user) {
    this.spectators.remove(user);
  }
  
  public boolean isSpectator(User user) {
    return this.spectators.contains(user);
  }
  
  public void start() {
    runTaskTimer((Plugin)plugin, 20L, 20L);
    setArenaState(ArenaState.WAITING_FOR_PLAYERS);
    this.isStarted = true;
  }
  
  public void stop() {
    if (this.arenaState != ArenaState.INACTIVE) {
      cancel(); 
      this.isStarted = false;
      
    }
    cleanUpArena();
    this.songs.clear();
    this.players.clear();
    this.deaths.clear();
    this.completedPlayer.clear();
    this.players.forEach(user -> user.clearPlayerScoreboard(user.getPlayer()));
  }
  
  public List<User> getCompletedPlayers() {
	    return this.completedPlayer;
}
  
  public Set<User> getPlayersLeft() {
    return (Set<User>)this.players.stream().filter(user -> !user.isSpectator()).collect(Collectors.toSet());
  }
  
  public double calculateDistanceToParkourEnd(User user) {
	    Location parkourEndLocation = getParkourEndLocation();
	    if (parkourEndLocation == null) {
	        return -1.0; 
	    }

	    Location playerLocation = user.getLocation();
	    return parkourEndLocation.distance(playerLocation);
	}
	
  public List<User> getTop3PlayersByDistanceToParkourEnd() {
	    List<User> topPlayers = new ArrayList<>(this.getAllPlayers());
	    topPlayers.removeIf(player -> completedPlayer.contains(player));
	    topPlayers.removeIf(player -> spectators.contains(player));
	    topPlayers.sort(Comparator.comparingDouble(this::calculateDistanceToParkourEnd));

	    if (topPlayers.size() > 3) {
	        topPlayers = topPlayers.subList(0, 3);
	    }

	    return topPlayers;
	}
  
  public void playDeathSound() {
    this.players.forEach(user -> XSound.ENTITY_PLAYER_DEATH.play(user.getLocation(), 1.0F, 1.0F));
  }
  
  public void teleportToStartLocation(User user) {
    Location location = getRandomLocation();
    if (location == null) {
      return; 
    }
    user.getPlayer().teleport(location);
  }
  
  public void teleportAllToStartLocation() {
    int i = 0, size = this.playerSpawnPoints.size();
    for (User user : this.players) {
      if (i + 1 > size) {
        plugin.getLogger().warning("There aren't enough spawn points to teleport players!");
        plugin.getLogger().warning("We are teleporting player to a random location for now!");
        user.getPlayer().teleport(getRandomLocation());
        break;
      } 
      user.getPlayer().teleport(this.playerSpawnPoints.get(i++));
    } 
  }
  
  public Location getRandomLocation() {
    return this.playerSpawnPoints.get(ThreadLocalRandom.current().nextInt(this.playerSpawnPoints.size()));
  }
  
  public User getRandomPlayer() {
    Set<User> players = getPlayersLeft();
    return players.stream().skip(ThreadLocalRandom.current().nextInt(players.size())).findFirst().orElse(null);
  }
  
  public int getGameplayTime() {
    return getOption(ArenaOption.GAMEPLAY_TIME);
  }
  
  public void setGameplayTime(int gameplayTime) {
    setOptionValue(ArenaOption.GAMEPLAY_TIME, gameplayTime);
  }
  
  public void cleanUpArena() {
    this.players.clear();
    this.deaths.clear();
    this.completedPlayer.clear();
    this.spectators.clear();
    this.forceStart = false;
  }
  
  public void showPlayers() {
    for (User user : this.players) {
      Player player = user.getPlayer();
      user.removePotionEffectsExcept(new PotionEffectType[] { PotionEffectType.BLINDNESS });
      for (User u : this.players) {
        player.showPlayer((Plugin)plugin, u.getPlayer());
        u.getPlayer().showPlayer((Plugin)plugin, player);
      } 
    } 
  }
  
  public void showUserToArena(User user) {
    Player player = user.getPlayer();
    for (User targetUser : this.players) {
      Player targetPlayer = targetUser.getPlayer();
      targetPlayer.showPlayer((Plugin)plugin, player);
      player.showPlayer((Plugin)plugin, targetPlayer);
    } 
  }
  
  private int getOption(ArenaOption option) {
    return ((Integer)this.arenaOptions.get(option)).intValue();
  }
  
  private void setOptionValue(ArenaOption option, int value) {
    this.arenaOptions.put(option, Integer.valueOf(value));
  }
  
  private boolean validateLocation(GameLocation gameLocation) {
    Location location = this.gameLocations.get(gameLocation);
    if (location == null) {
      plugin.getLogger().log(Level.WARNING, "Lobby location isn't initialized for arena {0}!", this.id);
      return false;
    } 
    return true;
  }
  
  public void displayFinalResults() {
	    int numCompletedPlayers = completedPlayer.size();
	    String[] placeNames = {"1ѕᴛ ᴘʟᴀᴄᴇ", "2ɴᴅ ᴘʟᴀᴄᴇ", "3ʀᴅ ᴘʟᴀᴄᴇ"};
	    ChatColor[] placeColors = {ChatColor.AQUA, ChatColor.GOLD, ChatColor.GREEN};
	    
	    if (numCompletedPlayers > 0) {
	        broadcastMessage("");
	        broadcastCenteredMessage(ChatColor.WHITE + "" + ChatColor.BOLD + ">" + ChatColor.YELLOW + "" + ChatColor.BOLD + ">" + ChatColor.WHITE + "" + ChatColor.BOLD + "> " + "" + ChatColor.GOLD + "" + ChatColor.BOLD + "ᴛᴏᴘ 3 ᴘʟᴀʏᴇʀѕ" + ChatColor.WHITE + "" + ChatColor.BOLD + " <" + ChatColor.YELLOW + "" + ChatColor.BOLD + "<" + ChatColor.WHITE + "" + ChatColor.BOLD + "<");
	        broadcastMessage("");
	    }
	    
	    for (int i = 0; i < Math.min(numCompletedPlayers, 3); i++) {
	        String placeName = placeNames[i];
	        ChatColor placeColor = placeColors[i];
	        broadcastCenteredMessage(placeColor + "" + ChatColor.BOLD + "" + placeName + ChatColor.GRAY + " - " + completedPlayer.get(i).getName());
	    }

	    for (int i = Math.min(numCompletedPlayers, 3); i < 3; i++) {
	        String placeName = placeNames[i];
	        ChatColor placeColor = placeColors[i];
	        broadcastCenteredMessage(placeColor + "" + ChatColor.BOLD + "" + placeName + ChatColor.GRAY + " - " + "ᴇᴍᴘᴛʏ :(");
	    }
	    
	    if (numCompletedPlayers > 0) {
	        broadcastMessage("");
	    }
	}
  
  public void run() {
	  
	    if (this.players.isEmpty() && this.arenaState == ArenaState.WAITING_FOR_PLAYERS) {
	        return;
	    }

	    int waitingTime = getOption(ArenaOption.LOBBY_WAITING_TIME), startingTime = getOption(ArenaOption.LOBBY_STARTING_TIME),
	    ingameTime = getOption(ArenaOption.GAMEPLAY_TIME), endingTime = getOption(ArenaOption.GAME_ENDING_TIME);

	    switch (this.arenaState) {
	        case WAITING_FOR_PLAYERS:
	            if (this.players.size() >= getMinimumPlayers()) {
	                setArenaState(ArenaState.STARTING);
	                setTimer(startingTime);
	                
	            } else {
	            	
	                if (getTimer() <= 0) {
	                    setTimer(waitingTime);
						this.players.forEach(user -> user.sendMessage(ChatColor.RED + "Waiting for more players to join!"));
	                    
	                }
	            }

	            setTimer(getTimer() - 1);
	            break;

			case STARTING:
				if (this.players.size() < getMinimumPlayers()) {
					setArenaState(ArenaState.WAITING_FOR_PLAYERS);
					setTimer(waitingTime);
					this.players.forEach(user -> user.getPlayer().sendMessage(ChatColor.RED + "Game cancelled, waiting for more players!"));
					break;
				}

				if (getTimer() == 45 || getTimer() == 30 || getTimer() <= 5 && getTimer() != 0) {
					this.players.forEach(user -> XSound.UI_BUTTON_CLICK.play((Entity) user.getPlayer()));
					this.players.forEach(user -> user.sendMessage(ChatColor.GRAY + "The gates will open in " + ChatColor.RED + getTimer() + 
					ChatColor.GRAY + " seconds!"));
				}
				
				if(getTimer() == 55) {
	        	    this.players.forEach(user -> Titles.sendTitle(user.getPlayer(), 0, 20*5, 20*2, ChatColor.RED + "" + ChatColor.BOLD + "Chapter 2:", 
	        	    		ChatColor.GRAY + "The Capitol Train"));
					this.players.forEach(user -> user.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20*5, 20*2)));	        	    
	        	    this.players.forEach(player -> player.getPlayer().playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1F, 1F));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), ChatColor.RED + "" + ChatColor.BOLD + "Chapter 2: The Capitol Train"));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), ChatColor.GRAY + "Welcome tributes, and welcome aboard the Capitol Train!"));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), ChatColor.GRAY + "In order to enter the Capitol, you must navigate through the"));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), ChatColor.GRAY + "train, each cart representing a district. The top 3 players"));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), ChatColor.GRAY + "will be rewarded with capitol gifts, and sponsor tokens,"));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), ChatColor.GRAY + "which will be essential in the arena! Good luck tributes!"));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), " "));
	        	    this.players.forEach(user -> MiscUtils.sendCenteredMessage(user.getPlayer(), ChatColor.RED + "The games will begin momentarily!"));

	            }

				if (getTimer() == 0) {
					
					setArenaState(ArenaState.IN_GAME);
					setTimer(ingameTime);
					
					World world = Bukkit.getWorld("TalesOfPanemContinentCORRECT"); // Replace "world" with your world's name
			        double x = -4689.420; // X-coordinate
			        double y = 185.0; // Y-coordinate
			        double z = -713.582; // Z-coordinate
					
			        removeGlassInRadius(new Location(world, x, y, z), 5);
					playCoolParticleEffect(new Location(world, x, y, z));
					
					for(User user : this.players) {
						user.getPlayer().playSound(user.getLocation(), Sound.BLOCK_GLASS_BREAK, 1F, 1F);
					}

					for (Song s : Main.getPlugin(Main.class).getSongs()) {
						this.songs.add(new Song(s));
					}

					if (this.songs.size() > 0) {
						this.song = this.songs.get(NumberUtils.random().nextInt(this.songs.size()));
					}
				}
				setTimer(getTimer() - 1);
				break;

			case IN_GAME:
				if (getTimer() == 60 || getTimer() == 30 || getTimer() == 10 || getTimer() <= 5 && getTimer() != 0) {
					this.players.forEach(user -> XSound.UI_BUTTON_CLICK.play((Entity) user.getPlayer()));
					this.players.forEach(user -> user.getPlayer().sendMessage(ChatColor.GRAY + "The game will end in " + ChatColor.RED + getTimer() + 
							ChatColor.GRAY + " seconds!"));
				}
				
				if (getTimer() == 0) {
					setArenaState(ArenaState.ENDING);
					setTimer(endingTime);
				}

				setTimer(getTimer() - 1);
				break;

			case ENDING:
				if (getTimer() == 60 || getTimer() == 30 || getTimer() == 10 || getTimer() <= 5) {
					this.players.forEach(user -> XSound.UI_BUTTON_CLICK.play((Entity) user.getPlayer()));
					this.players.forEach(user -> user.getPlayer().sendMessage(ChatColor.GRAY + "The game will end in " + ChatColor.RED + getTimer() + ChatColor.GRAY + " seconds!"));
				}

				if (getTimer() == 0) {
					setArenaState(ArenaState.RESTARTING);
					this.players.forEach(user -> user.getPlayer().sendMessage(ChatColor.GRAY + "The game will end in " + ChatColor.RED + getTimer() + ChatColor.GRAY + " seconds!"));
					showPlayers();
					
					displayFinalResults();
					this.players.forEach(user -> user.clearPlayerScoreboard(user.getPlayer()));

				}

				setTimer(getTimer() - 1);
				break;

			case RESTARTING:
				cleanUpArena();
				setArenaState(ArenaState.WAITING_FOR_PLAYERS);
				this.players.forEach(user -> user.getPlayer().sendMessage(ChatColor.RED + "The arena has restarted!"));
				this.players.forEach(user -> showPlayers());
				break;

			case INACTIVE:
				break;

			default:
				break;
			}
	}
  
  public void playCoolParticleEffect(Location location) {
	    World world = location.getWorld();

	    Particle particleType = Particle.CLOUD;

	    int particleCount = 100;
	    double offsetX = 0.5;
	    double offsetY = 0.5;
	    double offsetZ = 0.5;
	    double extra = 0.1;

	    world.spawnParticle(
	        particleType, 
	        location, 
	        particleCount, 
	        offsetX, 
	        offsetY, 
	        offsetZ, 
	        extra
	    );
	}
  
  public void removeGlassInRadius(Location centerLocation, int radius) {
	  
	    World world = centerLocation.getWorld();
	    
	    int centerX = centerLocation.getBlockX();
	    int centerY = centerLocation.getBlockY();
	    int centerZ = centerLocation.getBlockZ();
	    
	    Material glassType = Material.WHITE_STAINED_GLASS; 

	    for (int x = centerX - radius; x <= centerX + radius; x++) {
	        for (int y = centerY - radius; y <= centerY + radius; y++) {
	            for (int z = centerZ - radius; z <= centerZ + radius; z++) {
	                Location location = new Location(world, x, y, z);
	                Block block = location.getBlock();

	                if (block.getType() == glassType) {
	                    block.setType(Material.AIR);
	                }
	            }
	        }
	    }
	}
  
  public void broadcastMessage(String message) {
      for (User user : players) {
          user.sendMessage(message);
      }
  }
  
  public void broadcastCenteredMessage(String message) {
      for (User user : players) {
          MiscUtils.sendCenteredMessage(user.getPlayer(), message);
      }
  }
  
  public String getId() {
    return this.id;
  }
  
  public String toString() {
    return this.id;
  }
  
  public enum GameLocation {
    LOBBY, END, PARKOUREND;
  }
}