package game.user;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.entity.Player;

import game.Main;

public class UserManager {
	
	private final Set<User> users;

	public UserManager(Main plugin) {
		this.users = new HashSet<>();
	}

	
	public User addUser(Player player) {
		User user = new User(player.getUniqueId());
		this.users.add(user);
		return user;
	}

	public void removeUser(Player player) {
		this.users.remove(getUser(player));
	}
	
	public User getUser(Player player) {
		UUID uuid = player.getUniqueId();
		for (User user1 : this.users) {
			if (uuid.equals(user1.getUniqueId())) {
				user1.setPlayer();
				return user1;
			}
		}
		User user = addUser(player);
		return user;
	}

	
	public Set<User> getUsers() {
		this.users.forEach(User::setPlayer);
		return Set.copyOf(this.users);
	}
}
