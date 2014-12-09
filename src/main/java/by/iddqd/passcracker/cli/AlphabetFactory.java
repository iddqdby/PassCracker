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

import by.iddqd.passcracker.sequence.alphabet.Alphabet;
import by.iddqd.passcracker.sequence.alphabet.CharacterAlphabet;
import by.iddqd.passcracker.sequence.alphabet.TokenAlphabet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static java.util.Objects.requireNonNull;

/**
 * Alphabet factory.
 * 
 * This class provides static factory method to create an instance of Alphabet
 * on the basis of provided command-line arguments.
 * 
 * @author Sergey Protasevich
 */
class AlphabetFactory {

    /**
     * Create an instance of Alphabet on the basis of provided command-line arguments.
     * 
     * @param alphabetType type of the alphabet
     * @param options command-line arguments
     * @return the instance of Alphabet
     * @throws IllegalArgumentException if some of the arguments are illegal
     */
    static Alphabet create( String alphabetType, Map<String, String> options )
            throws IllegalArgumentException {
        
        Alphabet alphabet;
        
        try {
            switch( alphabetType ) {
                case "characters": {
                    
                    int characterSets =
                            Integer.parseInt( requireNonNull( options.get( "characterSets" ) ) );
                    String additionalCharactersString =
                            options.get( "additionalCharacters" );
                    
                    char[] additionalCharacters = additionalCharactersString == null
                            ? new char[] {}
                            : additionalCharactersString.toCharArray();
                    
                    alphabet = new CharacterAlphabet( characterSets, additionalCharacters );
                    
                    break;
                }
                case "tokens": {
                    
                    Set<String> tokens = new HashSet<>();
                    
                    int i = 1;
                    while( options.containsKey( "t" + i ) ) {
                        tokens.add( options.get( "t" + i++ ) );
                    }
                    
                    alphabet = new TokenAlphabet( tokens );
                    
                    break;
                }
                default:
                    throw new IllegalArgumentException(
                            String.format( "Alphabet type \"%s\" is not supported.", alphabetType ) );
            }
        } catch( NullPointerException | NumberFormatException ex ) {
            throw new IllegalArgumentException( "Required argument is missing.", ex );
        }
        
        return alphabet;
    }

    /**
     * Get information about legal command-line arguments.
     * 
     * @return information about legal command-line arguments
     */
    static String getOptionsInfo() {
        return "\ttype \"characters\":\n\n"
                + "\t--characterSets=[value] -- sets of characters to use as"
                + " a sum of some of the next values:\n"
                + "\t\t0b000001 (1) -- digits,\n"
                + "\t\t0b000010 (2) -- latin characters,\n"
                + "\t\t0b000100 (4) -- cyrillic characters,\n"
                + "\t\t0b001000 (8) -- special characters,\n"
                + "\t\t0b010000 (16) -- space,\n"
                + "\t\t0b100000 (32) -- tab;\n"
                + "\tfor example, 19 (1 + 2 + 16) means"
                + " \"use digits and latin characters and space\"\n"
                + "\t--additionalCharacters=[value] -- additional characters to use\n\n"
                + "\ttype \"tokens\":\n\n"
                + "\t--t[n]=[value] where n = 1,2,3,... -- elements of the alphabet;\n"
                + "\tfor example: --t1=foo --t2=bar --t3=baz";
    }
    
}
