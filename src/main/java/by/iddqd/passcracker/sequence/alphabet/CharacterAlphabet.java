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

package by.iddqd.passcracker.passsequence.alphabet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


/**
 * Alphabet, each element of which is a single character.
 * 
 * @author Sergey Protasevich
 */
public class CharacterAlphabet extends ElementAlphabet {
    
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

    
    /**
     * Create new alphabet with elements from the specified sets.
     * 
     * @param characterSets sets of characters to use; a sum of some of the next values:<br/>
     * CharacterAlphabet.KEY_DIGITS -- digits,<br/>
     * CharacterAlphabet.KEY_LATIN -- latin characters,<br/>
     * CharacterAlphabet.KEY_CYRILLIC -- cyrillic characters,<br/>
     * CharacterAlphabet.KEY_SPECIAL_CHARACTERS -- special characters,<br/>
     * CharacterAlphabet.KEY_SPACE -- space,<br/>
     * CharacterAlphabet.KEY_TAB -- tab
     * @param additionalCharacters additional characters to use
     * @throws IllegalArgumentException if no character sets and additional characters are specified
     */
    public CharacterAlphabet( int characterSets, char[] additionalCharacters )
            throws IllegalArgumentException {
        
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
        
        if( charSet.isEmpty() ) {
            throw new IllegalArgumentException( "Alphabet cannot be empty." );
        }
        
        alphabet = new char[ charSet.size() ][1];
        lastIndex = alphabet.length - 1;
        size = new BigInteger( Integer.toString( alphabet.length ) );
        
        int p = 0;
        for( char c : charSet ) {
            alphabet[ p++ ][0] = c;
        }
    }

    @Override
    public int[] toElements( String value ) throws IllegalArgumentException {
        
        char[] charArray = value.toCharArray();
        
        int[] intArray = new int[ charArray.length ];
        for( int i = 0; i < charArray.length; i++ ) {
            intArray[ i ] = -1;
        }

        init: for( int i = 0; i < charArray.length; i++ ) {
            for( int j = 0; j < alphabet.length; j++ ) {
                if( charArray[ i ] == alphabet[ j ][0] ) {
                    intArray[ i ] = j;
                    continue init;
                }
            }
            throw new IllegalArgumentException( "Value has illegal character(s)" );
        }
        
        return intArray;
    }
    
}
