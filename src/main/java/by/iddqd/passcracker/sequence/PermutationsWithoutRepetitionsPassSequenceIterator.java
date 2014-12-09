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
import java.util.HashSet;
import java.util.Set;

/**
 * Iterator for PermutationsWithoutRepetitionsPassSequence.
 * 
 * @author Sergey Protasevich
 */
class PermutationsWithoutRepetitionsPassSequenceIterator extends SimplePassSequenceIterator {
    
    private final Set<Integer> elements;

    protected PermutationsWithoutRepetitionsPassSequenceIterator(
            int[] initialValue, int maxLength, Alphabet alphabet ) {
        super( initialValue, maxLength, alphabet );
        
        elements = new HashSet<>( value.length );
        
        if( value.length > 0 && value[ 0 ] != -1 && hasRepetitions() ) {
            increment();
        }
    }

    @Override
    protected final void increment() {
        do {
            super.increment();
        } while( hasRepetitions() );
    }

    private boolean hasRepetitions() {
        elements.clear();
        for( int i = 0; i < value.length && value[ i ] != -1; i++ ) {
            if( elements.contains( value[ i ] ) ) {
                return true;
            }
            elements.add( value[ i ] );
        }
        return false;
    }
}
