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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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
        List<char[]> elements = new ArrayList<>();
        new TreeSet<>( tokens ).stream()
                .map( token -> token.toCharArray() )
                .forEachOrdered( array -> elements.add( array ) );
        
        alphabet = new char[ elements.size() ][];
        lastIndex = alphabet.length - 1;
        size = new BigInteger( String.valueOf( alphabet.length ) );
        
        int p = 0;
        for( char[] element : elements ) {
            alphabet[ p++ ] = element;
        }
    }
    
}
