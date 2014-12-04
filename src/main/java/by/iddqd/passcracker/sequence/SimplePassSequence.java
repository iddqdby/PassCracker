/**
 *  PassSequence
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

package by.iddqd.passcracker.passsequence;

import by.iddqd.passcracker.passsequence.alphabet.Alphabet;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;


/**
 * Simple sequence of passwords to try out.
 * 
 * This sequence contains passwords, generated on the basis of the provided alphabet.
 * 
 * @author Sergey Protasevich
 */
public class SimplePassSequence extends AbstractPassSequence {

    private final Alphabet alphabet;
    private final int alphabetSize;
    
    // bijective base-k numeration
    private final int[] initialValue;
    
    private final BigInteger minBaseIndex;
    private final BigInteger size;

    
    public SimplePassSequence( Alphabet alphabet, int minLength, int maxLength, String startFrom ) {
        super( minLength, maxLength );
        
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
        
        initialValue = new int[ maxLength ];
        Arrays.fill( initialValue, -1 );
        if( startFrom == null ) {
            for( int i = 0; i < minLength; i++ ) {
                initialValue[ i ] = 0;
            }
        } else {
            int[] startFromElements = alphabet.toElements( startFrom );
            System.arraycopy( startFromElements, 0, initialValue, 0, startFromElements.length );
        }
        
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
            index = index.add( new BigInteger( Integer.toString( elements[ i ] + 1 ) )
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
    public BigInteger indexOf( String value ) {
        return calculateBaseIndex( alphabet.toElements( value ) ).subtract( minBaseIndex );
    }

    @Override
    public BigInteger size() {
        return size;
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>() {
            
            private final int[] value = Arrays.copyOf( initialValue, initialValue.length );
            private boolean hasNext = true;

            @Override
            public boolean hasNext() {
                return hasNext;
            }

            @Override
            public String next() {

                if( !hasNext() ) {
                    throw new NoSuchElementException();
                }

                StringBuilder sb = new StringBuilder();

                for( int i = 0; i < maxLength && value[ i ] != -1; i++ ) {
                    sb.append( alphabet.getElement( value[ i ] ) );
                }

                // incrementing the value
                int pos = 0;
                try {
                    while( true ) {
                        if( value[ pos ] == -1 ) {
                            value[ pos ] = 0;
                            break;
                        }
                        if( value[ pos ] == alphabetSize - 1 ) {
                            value[ pos ] = 0;
                            pos++;
                            continue;
                        }
                        value[ pos ]++;
                        break;
                    }
                } catch( IndexOutOfBoundsException ex ) {
                    hasNext = false;
                }

                return sb.toString();
            }
        };
    }
    
}
