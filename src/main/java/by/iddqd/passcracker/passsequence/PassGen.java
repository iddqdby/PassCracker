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

package by.iddqd.passcracker.passsequence;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;

/**
 * Password generator.
 * 
 * @author Sergey Protasevich
 */
public class PassGen {

    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) {
        try {
            
            int characterSets = Integer.parseInt( args[0] );
            char[] additionalCharacters = args[1].toCharArray();
            int minLength = Integer.parseInt( args[2] );
            int maxLength = Integer.parseInt( args[3] );
            String startFrom = args.length == 5 ? args[4] : null;
            
            SimplePassSequence ps =
                    new SimplePassSequence( characterSets, additionalCharacters, minLength, maxLength, startFrom );
            
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
                    + "java -jar PassGen.jar [characterSets] [additionalCharacters]"
                    + " [minLength] [maxLength] [startFrom]\n"
                    + "\tcharacterSets (integer) -- sets of characters to use; a sum of some of the next values:\n"
                    + "\t\t0b000001 (1) -- digits,\n"
                    + "\t\t0b000010 (2) -- latin characters,\n"
                    + "\t\t0b000100 (4) -- cyrillic characters,\n"
                    + "\t\t0b001000 (8) -- special characters,\n"
                    + "\t\t0b010000 (16) -- space,\n"
                    + "\t\t0b100000 (32) -- tab\n"
                    + "\tadditionalCharacters (string) -- additional characters to use\n"
                    + "\tminLength (integer) -- minimum length of generated passwords\n"
                    + "\tmaxLength (integer) -- maximum length of generated passwords\n"
                    + "\tstartFrom (string) [optional argument] -- password to start from\n" );
            System.exit( 1 );
        }
    }
}
