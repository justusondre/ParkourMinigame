package game.commands;
import game.Main;

public abstract class AbstractCommand {
	
  protected final Main plugin;
    
  public AbstractCommand(Main plugin) {
    this.plugin = plugin;
    this.plugin.getCommandFramework().registerCommands(this);
    
  }
  
  public static void registerCommands(Main plugin) {
    Class<?>[] commandClasses = new Class[] { AdminCommands.class, PlayerCommands.class };
    for (Class<?> clazz : commandClasses) {
      try {
        clazz.getConstructor(new Class[] { Main.class }).newInstance(new Object[] { plugin });
      } catch (Exception exception) {
        exception.printStackTrace();
        
      } 
    } 
  }
}
