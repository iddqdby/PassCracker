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
import java.util.Iterator;

/**
 * Sequence of passwords -- permutations without repetition, generated on the basis
 * of provided Alphabet.
 * 
 * @author Sergey Protasevich
 */
public class PermutationsWithoutRepetitionsPassSequence extends SimplePassSequence {

    public PermutationsWithoutRepetitionsPassSequence(
            Alphabet alphabet, int minLength, int maxLength, String startFrom ) {
        super( alphabet, minLength, maxLength, startFrom );
        if( maxLength > alphabetSize ) {
            throw new IllegalArgumentException(
                    "Impossible to create permutations without repetitions:"
                    + " maximum length is greater than the size of alphabet." );
        }
    }

    @Override
    public Iterator<String> iterator() {
        return new PermutationsWithoutRepetitionsPassSequenceIterator( initialValue, maxLength, alphabet );
    }
}
