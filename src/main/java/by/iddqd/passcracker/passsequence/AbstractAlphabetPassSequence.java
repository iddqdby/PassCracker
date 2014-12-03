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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Abstract sequence of passwords to try out.
 * 
 * @author Sergey Protasevich
 */
public abstract class AbstractAlphabetPassSequence implements PassSequence {

    protected volatile int minLength;
    protected volatile int maxLength;
    
    private volatile Object alphabet;
    protected volatile BigInteger alphabetLength;
    
    // bijective base-k numeration
    @SuppressWarnings( "VolatileArrayField" )
    protected volatile int[] initialValue;
    
    protected volatile BigInteger minBaseIndex;
    protected volatile BigInteger size;
    
    private volatile boolean isInitialized = false;
    
    /**
     * Initialize the sequence.
     * 
     * @param minLength minimum amount of alphabet elements in generated passwords
     * @param maxLength maximum amount of alphabet elements in generated passwords
     * @param options options for initialization the sequence
     * @param startFrom password to start from, or null
     * @throws IllegalArgumentException if any of the arguments is illegal
     */
    public final void init( int minLength, int maxLength, Object options, String startFrom ) {
        
        if( maxLength < minLength || minLength < 0 ) {
            throw new IllegalArgumentException( "'maxLength' and/or 'minLength' are illegal" );
        }
        
        this.minLength = minLength;
        this.maxLength = maxLength;
        
        alphabet = createAlphabet( options );
        alphabetLength = calculateAlphabetLength( alphabet );
        
        if( startFrom == null ) {
            initialValue = new int[ maxLength ];
            for( int i = 0; i < minLength; i++ ) {
                initialValue[ i ] = 0;
            }
            for( int i = minLength; i < maxLength; i++ ) {
                initialValue[ i ] = -1;
            }
        } else {
            initialValue = toIntArray( startFrom );
        }
        
        minBaseIndex = calculateBaseIndex( initialValue );
        int[] maxValue = new int[ maxLength ];
        for( int i = 0; i < maxLength; i++ ) {
            maxValue[ i ] = alphabetLength.subtract( ONE ).intValue();
        }
        size = calculateBaseIndex( maxValue ).subtract( minBaseIndex ).add( ONE );
        
        isInitialized = true;
    }

    
    protected abstract Object createAlphabet( Object options );
    protected abstract BigInteger calculateAlphabetLength( Object alphabet );
    protected abstract int[] toIntArray( String value );
    

    protected final BigInteger calculateBaseIndex( int[] value ) {
        BigInteger index = ZERO;
        for( int i = 0; i < value.length; i++ ) {
            // index += ( value[ i ] + 1 ) * (long)pow( alphabet.length, i )
            index = index.add( new BigInteger( Integer.toString( value[ i ] + 1 ) )
                    .multiply( alphabetLength.pow( i ) ) );
        }
        return index;
    }
    
    protected final Object getAlphabet() {
        return alphabet;
    }
    
    protected final void checkInitialized() {
        if( !isInitialized ) {
            throw new IllegalStateException( "Sequence is not initialized" );
        }
    }
    
    /**
     * Returns the size of this sequence.
     *
     * @return the size of this sequence.
     */
    @Override
    public final BigInteger size() {
        checkInitialized();
        return size;
    }
    
    /**
     * Returns an index of the given value.
     *
     * @param value a valid value from this sequence
     * @return index of the value.
     * @throws IllegalArgumentException if the value is not from this sequence.
     */
    @Override
    public final BigInteger indexOf( String value ) {
        checkInitialized();
        return calculateBaseIndex( toIntArray( value ) ).subtract( minBaseIndex );
    }

    /**
     * Returns an iterator over passwords.
     *
     * @return an Iterator.
     */
    @Override
    public final Iterator<String> iterator() {
        checkInitialized();
        return new Iterator<String>() {
            
            private final String[] alphabet = (String[])getAlphabet();
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
                    sb.append( alphabet[ value[ i ] ] );
                }

                // incrementing the value
                int pos = 0;
                try {
                    while( true ) {
                        if( value[ pos ] == -1 ) {
                            value[ pos ] = 0;
                            break;
                        }
                        if( value[ pos ] == alphabet.length - 1 ) {
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
