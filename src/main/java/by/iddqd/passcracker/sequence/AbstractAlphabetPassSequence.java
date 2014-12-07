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

    
    public AbstractAlphabetPassSequence(
            Alphabet alphabet, int minLength, int maxLength, String startFrom ) {
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
    }
    
}
