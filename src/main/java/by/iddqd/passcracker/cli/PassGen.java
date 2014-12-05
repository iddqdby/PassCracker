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
import by.iddqd.passcracker.sequence.alphabet.CharacterAlphabet;
import by.iddqd.passcracker.sequence.alphabet.TokenAlphabet;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.math.BigInteger.ONE;

/**
 * Password generator.
 * 
 * @author Sergey Protasevich
 */
public final class PassGen {

    private PassGen() {}
    
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) {
        try {
            
            int minLength = Integer.parseInt( args[0] );
            int maxLength = Integer.parseInt( args[1] );
            
            Matcher matcher = Pattern.compile( "^\\[(?<VALUE>.*)\\]$" ).matcher( args[2] );
            String startFrom = matcher.matches() ? matcher.group( "VALUE" ) : null;
            
            String alphabetType = args[3];
            
            Alphabet alphabet;
            
            switch( alphabetType ) {
                case "c":
                    
                    int characterSets = Integer.parseInt( args[4] );
                    char[] additionalCharacters = args[5].toCharArray();
                    
                    alphabet = new CharacterAlphabet( characterSets, additionalCharacters );
                    
                    break;
                case "t":
                    
                    List<String> tokens = new ArrayList<>();
                    for( int i = 4; i < args.length; i++ ) {
                        tokens.add( args[i] );
                    }
                    
                    alphabet = new TokenAlphabet( tokens );
                    
                    break;
                default:
                    throw new IllegalArgumentException();
            }
            
            SimplePassSequence ps = new SimplePassSequence( alphabet, minLength, maxLength, startFrom );
            
            BigInteger maxDisplayedValue = new BigInteger( Long.toString( Long.MAX_VALUE ) ).pow( 8 );
            String maxDisplayedValueStr = maxDisplayedValue.toString();
            
            BigInteger maxIndex = ps.size().subtract( ONE );
            String maxIndexStr = maxIndex.compareTo( maxDisplayedValue ) == 1
                    ? ">" + maxDisplayedValueStr
                    : maxIndex.toString();
            
            boolean printIndex = true;
            for( String password : ps ) {
                if( printIndex ) {
                    BigInteger index = ps.indexOf( password );
                    System.out.printf( "%s/%s:\t%s\n", index.toString(), maxIndexStr, password );
                    printIndex = index.compareTo( maxDisplayedValue ) < 1;
                } else {
                    System.out.printf( "%s:\t%s\n", maxIndexStr, password );
                }
            }
        } catch( Exception ex ) {
            System.err.println( "An exception occurred:\n\n" + ex + "\n" );
            System.err.println( "Usage:\n"
                    + "java -jar PassGen.jar [minLength] [maxLength] [startFrom]"
                    + " [alphabetType] [options...]\n"
                    + "\tminLength (integer) -- minimum length of generated passwords\n"
                    + "\tmaxLength (integer) -- maximum length of generated passwords\n"
                    + "\tstartFrom (string) -- value to start from, in square brackets"
                    + " (for ex. \"[mypass]\"), or \"-\", if there is no starting value\n"
                    + "\talphabetType -- type of alphabet: \"c\" for CharacterAlphabet"
                    + " or \"t\" for TokenAlphabet\n"
                    + "\toptions -- options for given alphabet:\n"
                    + "\t\tCharacterAlphabet:\n"
                    + "\t\t\tcharacterSets (integer) -- sets of characters to use;"
                    + " a sum of some of the next values:\n"
                    + "\t\t\t\t0b000001 (1) -- digits,\n"
                    + "\t\t\t\t0b000010 (2) -- latin characters,\n"
                    + "\t\t\t\t0b000100 (4) -- cyrillic characters,\n"
                    + "\t\t\t\t0b001000 (8) -- special characters,\n"
                    + "\t\t\t\t0b010000 (16) -- space,\n"
                    + "\t\t\t\t0b100000 (32) -- tab\n"
                    + "\t\t\tadditionalCharacters (string) -- additional characters to use\n"
                    + "\t\tTokenAlphabet:\n"
                    + "\t\t\ttokens -- space-separated list of tokens to use as alphabet elements" );
            System.exit( 1 );
        }
    }
}
