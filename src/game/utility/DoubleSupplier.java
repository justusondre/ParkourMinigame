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

import java.util.Objects;

/**
 * @author Despical
 * <p>
 * Created at 1.10.2022
 */
@FunctionalInterface
public interface DoubleSupplier<T, R> {

	R accept(T t);

	default DoubleSupplier<T, R> andThen(final DoubleSupplier<T, R> doubleSupplier) {
		Objects.requireNonNull(doubleSupplier);

		return (t) -> {
			this.accept(t);
			return doubleSupplier.accept(t);
		};
	}
}