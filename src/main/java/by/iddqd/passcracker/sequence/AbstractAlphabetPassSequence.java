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
import java.util.Arrays;

/**
 * Abstract sequence of passwords generated on the basis of provided Alphabet.
 * 
 * @author Sergey Protasevich
 */
public abstract class AbstractAlphabetPassSequence extends AbstractPassSequence {

    protected final Alphabet alphabet;
    protected final int alphabetSize;
    
    protected final int[] initialValue;

    
    protected AbstractAlphabetPassSequence(
            Alphabet alphabet, int minLength, int maxLength, int[] startFrom ) {
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
            if( startFrom.length > 0 ) {
                for( int i = 0; i < startFrom.length && startFrom[ i ] != -1; i++ ) {
                    initialValue[ i ] = startFrom[ i ];
                }
            }
        }
    }

    @Override
    public final Alphabet getAlphabet() {
        return alphabet;
    }
}
