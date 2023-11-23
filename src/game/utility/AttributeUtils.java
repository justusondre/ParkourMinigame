/*
 * Commons - Box of the common utilities.
 * Copyright (C) 2023 Despical
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package game.utility;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * @author Despical
 * @since 1.1.6
 * <p>
 * Created at 13.10.2020
 */
public class AttributeUtils {

	/**
	 * Makes player can collides with other entities to avoid using deprecated
	 * methods.
	 *
	 * @param player to set collidable
	 * @param collidable value to set
	 */
	public static void setCollidable(Player player, boolean collidable) {
		if (!ReflectionUtils.supports(9)) {
			player.spigot().setCollidesWithEntities(collidable);
		} else {
			player.setCollidable(collidable);
		}
	}

	/**
	 * Change player's glowing to avoid using deprecated methods.
	 *
	 * @param player to be glowed
	 * @param glowing value to set
	 */
	public static void setGlowing(Player player, boolean glowing) {
		if (ReflectionUtils.supports(9)) {
			player.setGlowing(glowing);
		}
	}

	public static void setAttackCooldown(Player player, double value) {
		if (ReflectionUtils.supports(9)) {
			player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).setBaseValue(value);
		}
	}

	public static void resetAttackCooldown(Player player) {
		setAttackCooldown(player, 4);
	}

	public static void healPlayer(Player player) {
		if (ReflectionUtils.supports(9)) {
			player.setHealth(player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
			return;
		}

		player.setHealth(player.getMaxHealth());
	}
}