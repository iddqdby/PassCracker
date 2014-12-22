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

package by.iddqd.passcracker.sequence.alphabet;

import java.math.BigInteger;

/**
 * Alphabet -- interface.
 * 
 * @author Sergey Protasevich
 */
public interface Alphabet {
    
    /**
     * Get the first element of this alphabet.
     * 
     * @return the first element of this alphabet as a char array.
     */
    char[] getFirstElement();
    
    /**
     * Get the last element of this alphabet.
     * 
     * @return the last element of this alphabet as a char array.
     */
    char[] getLastElement();
    
    /**
     * Get the element of this alphabet.
     * 
     * @param index an index of the element.
     * @return the element of this alphabet at the specified index as a char array.
     * @throws IndexOutOfBoundsException if index is out of the bounds.
     */
    char[] getElement( int index ) throws IndexOutOfBoundsException;
    
    /**
     * Get the size of this alphabet.
     * 
     * @return the size of this alphabet.
     */
    default int size() {
        return sizeBI().intValue();
    }
    
    /**
     * Get the size of this alphabet as BigInteger.
     * 
     * @return the size of this alphabet as BigInteger.
     */
    BigInteger sizeBI();

    /**
     * Build the string from the array of indices.
     * 
     * @param passValue array of indices
     * @return the string
     * @throws IllegalArgumentException if the array has illegal indices
     */
    String buildString( int[] passValue );
    
}
