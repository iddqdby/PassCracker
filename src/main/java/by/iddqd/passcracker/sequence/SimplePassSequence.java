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

import by.iddqd.passcracker.sequence.alphabet.Alphabet;
import java.math.BigInteger;
import java.util.Iterator;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;


/**
 * Simple sequence of passwords to try out.
 * 
 * This sequence contains passwords, generated on the basis of the provided alphabet.
 * Sequence is bijective base-k numeration.
 * 
 * @author Sergey Protasevich
 */
public class SimplePassSequence extends AbstractAlphabetPassSequence {
    
    private final BigInteger minBaseIndex;
    private final BigInteger size;

    
    public SimplePassSequence( Alphabet alphabet, int minLength, int maxLength, int[] startFrom ) {
        super( alphabet, minLength, maxLength, startFrom );
        
        this.minBaseIndex = calculateBaseIndex( initialValue );
        int[] maxValue = new int[ maxLength ];
        for( int i = 0; i < maxLength; i++ ) {
            maxValue[ i ] = alphabet.size() - 1;
        }
        this.size = calculateBaseIndex( maxValue ).subtract( minBaseIndex ).add( ONE );
    }

    private BigInteger calculateBaseIndex( int[] elements ) {
        BigInteger index = ZERO;
        for( int i = 0; i < elements.length; i++ ) {
            // index += ( value[ i ] + 1 ) * (long)pow( alphabet.length, i )
            index = index.add( BigInteger.valueOf( elements[ i ] + 1 )
                    .multiply( alphabet.sizeBI().pow( i ) ) );
        }
        return index;
    }
    
    /**
     * Returns an index of the given value.
     *
     * @param value a valid value from this sequence
     * @return index of the value.
     * @throws IllegalArgumentException if the value is not from this sequence.
     */
    @Override
    public BigInteger indexOf( int[] value ) {
        return calculateBaseIndex( value ).subtract( minBaseIndex );
    }

    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public Iterator<int[]> iterator() {
        return new SimplePassSequenceIterator( initialValue, maxLength, alphabet );
    }
}
