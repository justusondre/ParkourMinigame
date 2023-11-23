package game.arena;

public enum ArenaState {
	WAITING_FOR_PLAYERS("waiting"), 
	STARTING("starting"),
	IN_GAME("playing"), 
	ENDING("ending"),
	RESTARTING("restarting"), 
	INACTIVE("inactive");

	public final String name;

	ArenaState(String name) {
		this.name = name;
	}
}
