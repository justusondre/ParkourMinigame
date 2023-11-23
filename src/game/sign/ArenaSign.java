package game.sign;

import org.bukkit.block.Sign;

import game.arena.Arena;

public final class ArenaSign {
  private final Sign sign;
  
  private final Arena arena;
  
  public ArenaSign(Sign sign, Arena arena) {
    this.sign = sign;
    this.arena = arena;
    
  }
  
  public Sign sign() {
    return this.sign;
  }
  
  public Arena arena() {
    return this.arena;
  }
}
