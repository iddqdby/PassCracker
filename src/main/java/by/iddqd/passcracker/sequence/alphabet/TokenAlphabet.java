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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;


/**
 * Alphabet, each element of which is a string token.
 * 
 * @author Sergey Protasevich
 */
public class TokenAlphabet extends ElementAlphabet {
    
    /**
     * Create new alphabet with specified tokens as its elements.
     * 
     * @param tokens tokens to use
     * @throws IllegalArgumentException if collection of tokens is empty.
     */
    public TokenAlphabet( Collection<String> tokens ) throws IllegalArgumentException  {
        
        if( tokens.isEmpty() ) {
            throw new IllegalArgumentException( "Collection of tokens must not be empty." );
        }
        
        // TreeSet sorts the collection of chunks and removes duplicates
        Set<String> elements = new TreeSet<>( tokens );
        
        alphabet = new char[ elements.size() ][];
        lastIndex = alphabet.length - 1;
        size = BigInteger.valueOf( alphabet.length );
        
        int p = 0;
        for( String element : elements ) {
            alphabet[ p++ ] = element.toCharArray();
        }
    }
    
    /**
     * TRY TO convert string to array of indices of this alphabet.
     * 
     * This method may return wrong value or throw an exception even on legal strings,
     * if tokens of the alphabet have identical subsequences of characters.
     * 
     * @param value a string
     * @return the array of indices of this alphabet
     * @throws IllegalArgumentException if the string contains illegal characters,
     * or if it is impossible to exactly determinate the initial tokens this value is built from
     */
    public int[] toElements( String value ) throws IllegalArgumentException {
        
        List<Integer> indices = new ArrayList<>();
        
        init: while( !value.isEmpty() ) {
            for( int i = 0; i < alphabet.length; i++ ) {
                String token = String.valueOf( alphabet[ i ] ).intern();
                if( value.startsWith( token ) ) {
                    value = value.substring( token.length() );
                    indices.add( i );
                    continue init;
                }
            }
            throw new IllegalArgumentException( "Value is illegal" );
        }
        
        int[] array = new int[ indices.size() ];
        int i = 0;
        for( int index : indices ) {
            array[ i++ ] = index;
        }
        return array;
    }
    
}
