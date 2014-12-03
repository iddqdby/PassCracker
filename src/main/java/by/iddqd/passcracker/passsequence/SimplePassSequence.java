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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Simple sequence of passwords to try out.
 * 
 * @author Sergey Protasevich
 */
public class SimplePassSequence implements PassSequence {
    
    public static final int KEY_DIGITS =             0b000001;
    public static final int KEY_LATIN =              0b000010;
    public static final int KEY_CYRILLIC =           0b000100;
    public static final int KEY_SPECIAL_CHARACTERS = 0b001000;
    public static final int KEY_SPACE =              0b010000;
    public static final int KEY_TAB =                0b100000;
    
    private static final Map<Integer, char[]> CHARACTERS = new HashMap<Integer, char[]>() {{
        put( KEY_DIGITS, new char[] {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
        } );
        put( KEY_LATIN, new char[] {
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l',
            'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x',
            'y', 'z',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L',
            'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X',
            'Y', 'Z'
        } );
        put( KEY_CYRILLIC, new char[] {
            'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к',
            'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц',
            'ч', 'ш', 'щ', 'ь', 'ы', 'ъ', 'э', 'ю', 'я', 'і', 'ў',
            'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К',
            'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц',
            'Ч', 'Ш', 'Щ', 'Ь', 'Ы', 'Ъ', 'Э', 'Ю', 'Я', 'І', 'Ў'
        } );
        put( KEY_SPECIAL_CHARACTERS, new char[] {
            '`', '~', '!', '@', '#', '№', '$', '%', '^', '&', '*', '(',
            ')', '_', '-', '+', '=', '{', '[', '}', ']', '|', '\\', ':',
            ';', '"', '\'', '<', ',', '>', '.', '?', '/'
        } );
        put( KEY_SPACE, new char[] {
            ' '
        } );
        put( KEY_TAB, new char[] {
            '\t'
        } );
    }};
    
    
    private final int minLength;
    private final int maxLength;
    private final char[] alphabet;
    private final BigInteger alphabetLength;
    
    // bijective base-k numeration
    private final int[] initialValue;
    
    private final BigInteger minBaseIndex;
    private final BigInteger size;
    
    
    /**
     * Create new sequence of passwords.
     * 
     * @param characterSets sets of characters to use; a sum of some of the next values:<br/>
     * PassSequence.KEY_DIGITS -- digits,<br/>
     * PassSequence.KEY_LATIN -- latin characters,<br/>
     * PassSequence.KEY_CYRILLIC -- cyrillic characters,<br/>
     * PassSequence.KEY_SPECIAL_CHARACTERS -- special characters,<br/>
     * PassSequence.KEY_SPACE -- space,<br/>
     * PassSequence.KEY_TAB -- tab
     * @param additionalCharacters additional characters to use
     * @param minLength minimum length of generated passwords
     * @param maxLength maximum length of generated passwords
     * @throws IllegalArgumentException if any of the arguments is illegal
     */
    public SimplePassSequence( int characterSets, char[] additionalCharacters,
            int minLength, int maxLength ) {
        this( characterSets, additionalCharacters, minLength, maxLength, null );
    }
    
    /**
     * Create new sequence of passwords.
     * 
     * @param characterSets sets of characters to use; a sum of some of the next values:
     * PassSequence.KEY_DIGITS -- digits,
     * PassSequence.KEY_LATIN -- latin characters,
     * PassSequence.KEY_CYRILLIC -- cyrillic characters,
     * PassSequence.KEY_SPECIAL_CHARACTERS -- special characters,
     * PassSequence.KEY_SPACE -- space,
     * PassSequence.KEY_TAB -- tab
     * @param additionalCharacters additional characters to use
     * @param minLength minimum length of generated passwords
     * @param maxLength maximum length of generated passwords
     * @param startFrom password to start from
     * @throws IllegalArgumentException if any of the arguments is illegal
     */
    public SimplePassSequence( int characterSets, char[] additionalCharacters,
            int minLength, int maxLength, String startFrom ) {
        
        if( maxLength < minLength || minLength < 0 ) {
            throw new IllegalArgumentException( "'maxLength' and/or 'minLength' are illegal" );
        }
        
        this.minLength = minLength;
        this.maxLength = maxLength;
        
        Set<Character> charSet = new TreeSet<>();
        
        CHARACTERS.entrySet().stream().filter( e -> {
            int key = e.getKey();
            return ( characterSets & key ) == key;
        } ).forEach( e -> {
            for( char c : e.getValue() ) {
                charSet.add( c );
            }
        } );
        
        if( additionalCharacters != null ) {
            for( char c : additionalCharacters ) {
                charSet.add( c );
            }
        }
        
        alphabet = new char[ charSet.size() ];
        alphabetLength = new BigInteger( Integer.toString( alphabet.length ) );
        
        int p = 0;
        for( char c : charSet ) {
            alphabet[ p++ ] = c;
        }
        
        if( startFrom == null ) {
            initialValue = new int[ maxLength ];
            for( int i = 0; i < minLength; i++ ) {
                initialValue[ i ] = 0;
            }
            for( int i = minLength; i < maxLength; i++ ) {
                initialValue[ i ] = -1;
            }
        } else {
            initialValue = toIntArray( startFrom.toCharArray() );
        }
        
        this.minBaseIndex = calculateBaseIndex( initialValue );
        int[] maxValue = new int[ maxLength ];
        for( int i = 0; i < maxLength; i++ ) {
            maxValue[ i ] = alphabet.length - 1;
        }
        this.size = calculateBaseIndex( maxValue ).subtract( minBaseIndex ).add( ONE );
    }

    private BigInteger calculateBaseIndex( int[] value ) {
        BigInteger index = ZERO;
        for( int i = 0; i < value.length; i++ ) {
            // index += ( value[ i ] + 1 ) * (long)pow( alphabet.length, i )
            index = index.add( new BigInteger( Integer.toString( value[ i ] + 1 ) )
                    .multiply( alphabetLength.pow( i ) ) );
        }
        return index;
    }

    private int[] toIntArray( char[] charArray ) {
        
        int[] intArray = new int[ maxLength ];
        for( int i = 0; i < maxLength; i++ ) {
            intArray[ i ] = -1;
        }
        
        if( charArray.length < minLength || charArray.length > maxLength ) {
            throw new IllegalArgumentException( "Value has illegal size" );
        }

        init: for( int i = 0; i < charArray.length; i++ ) {
            for( int j = 0; j < alphabet.length; j++ ) {
                if( charArray[ i ] == alphabet[ j ] ) {
                    intArray[ i ] = j;
                    continue init;
                }
            }
            throw new IllegalArgumentException( "Value has illegal character(s)" );
        }
        
        return intArray;
    }

    /**
     * Returns an index of the given value.
     * 
     * @param value a valid value from this sequence
     * @return index of the value.
     * @throws IllegalArgumentException if the value is not from this sequence.
     */
    @Override
    public BigInteger indexOf( String value ) {
        return calculateBaseIndex( toIntArray( value.toCharArray() ) ).subtract( minBaseIndex );
    }
    
    /**
     * Returns the size of this sequence.
     * 
     * @return the size of this sequence.
     */
    @Override
    public BigInteger size() {
        return size;
    }

    /**
     * Returns an iterator over passwords.
     * 
     * @return an Iterator.
     */
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
