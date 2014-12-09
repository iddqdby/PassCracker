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

package by.iddqd.passcracker.cli;

import by.iddqd.passcracker.sequence.PassSequence;
import by.iddqd.passcracker.sequence.PermutationsWithoutRepetitionsPassSequence;
import by.iddqd.passcracker.sequence.SimplePassSequence;
import by.iddqd.passcracker.sequence.alphabet.Alphabet;
import java.util.Map;

/**
 * PassSequence factory.
 * 
 * This class provides static factory method to create an instance of PassSequence
 * on the basis of provided command-line arguments.
 * 
 * @author Sergey Protasevich
 */
class PassSequenceFactory {

    /**
     * Create an instance of PassSequence on the basis of provided command-line arguments.
     * 
     * @param minLength minimum amount of tokens in the generated passwords of the sequence
     * @param maxLength maximum amount of tokens in the generated passwords of the sequence
     * @param sequenceType type of the sequence
     * @param alphabet the Alphabet
     * @param options command-line arguments
     * @return the instance of PassSequence
     * @throws IllegalArgumentException if some of the arguments are illegal
     */
    static PassSequence create(
            int minLength, int maxLength, String sequenceType, Alphabet alphabet,
            Map<String, String> options )
            throws IllegalArgumentException {
        
        PassSequence passSequence;
        
        try {
            switch( sequenceType ) {
                case "simple": {
                    String startFrom = options.get( "startFrom" );
                    passSequence = new SimplePassSequence( alphabet, minLength, maxLength, startFrom );
                    break;
                }
                case "permutations": {
                    String startFrom = options.get( "startFrom" );
                    passSequence = new PermutationsWithoutRepetitionsPassSequence(
                            alphabet, minLength, maxLength, startFrom );
                    break;
                }
                default:
                    throw new IllegalArgumentException(
                            String.format( "Sequence type \"%s\" is not supported.", sequenceType ) );
            }
        } catch( NullPointerException ex ) {
            throw new IllegalArgumentException( "Required argument is missing.", ex );
        }
        
        return passSequence;
    }

    /**
     * Get information about legal command-line arguments.
     * 
     * @return information about legal command-line arguments
     */
    static String getOptionsInfo() {
        return "\ttype \"simple\":\n\n"
                + "\t--startFrom=[value] -- value to start sequence from\n\n"
                + "\ttype \"permutations\":\n\n"
                + "\t--startFrom=[value] -- value to start sequence from";
    }
    
}
