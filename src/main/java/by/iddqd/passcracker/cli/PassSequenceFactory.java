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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Pattern;

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
                    int[] startFrom = readStartFromValue( options.get( "startFrom" ) );
                    passSequence = new SimplePassSequence( alphabet, minLength, maxLength, startFrom );
                    break;
                }
                case "permutations": {
                    int[] startFrom = readStartFromValue( options.get( "startFrom" ) );
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
                + "\t--startFrom=[value] -- value to start sequence from (JSON array of alphabet indices)\n\n"
                + "\ttype \"permutations\":\n\n"
                + "\t--startFrom=[value] -- value to start sequence from (JSON array of alphabet indices)";
    }
    
    private static int[] readStartFromValue( String optionValue ) {
        
        if( optionValue == null ) {
            return null;
        }
        
        try {
            return JSONArrayToIntArray( optionValue );
        } catch( IllegalArgumentException ex ) {}
        
        try {
            return JSONArrayToIntArray( Files.readAllLines( Paths.get( optionValue ) ).get( 0 ) );
        } catch( IllegalArgumentException | IOException | IndexOutOfBoundsException ex ) {
            throw new IllegalArgumentException( "Value of option 'startFrom' is illegal.", ex );
        }
    }

    private static int[] JSONArrayToIntArray( String jsonArrayOfInt ) {
        if( !Pattern.matches( "\\[ *((\\-?\\d+, *)*(\\-?\\d+){1})* *\\]", jsonArrayOfInt ) ) {
            throw new IllegalArgumentException( "Fail to parce array of indices." );
        }
        
        String[] array = jsonArrayOfInt.replaceAll( "[ \\[\\]]+", "" ).split( "," );
        int[] intArray = new int[ array.length ];
        
        for( int i = 0; i < array.length; i++ ) {
            intArray[ i ] = Integer.parseInt( array[ i ] );
        }
        
        return intArray;
    }
}
