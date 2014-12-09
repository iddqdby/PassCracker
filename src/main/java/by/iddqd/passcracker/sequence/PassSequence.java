/**
 *  PassCracker
 *  Copyright (C) 2014  Sergey Protasevich
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package by.iddqd.passcracker.sequence;

import java.math.BigInteger;
import java.util.Iterator;

/**
 * Sequence of passwords to try out -- interface.
 * 
 * @author Sergey Protasevich
 */
public interface PassSequence extends Iterable<String> {

    /**
     * Returns an iterator over passwords.
     *
     * @return an Iterator.
     */
    @Override
    Iterator<String> iterator();

    /**
     * Returns the size of this sequence.
     *
     * @return the size of this sequence.
     */
    BigInteger size();
    
    /**
     * Returns an index of the given value (optional method).
     *
     * @param value a valid value from this sequence
     * @return index of the value.
     * @throws IllegalArgumentException if the value is not from this sequence.
     */
    default BigInteger indexOf( String value ) {
        throw new UnsupportedOperationException();
    }
    
}
