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

package by.iddqd.passcracker.sequence.alphabet;

import java.math.BigInteger;

/**
 * Abstract alphabet of elements.
 * 
 * @author Sergey Protasevich
 */
public abstract class ElementAlphabet implements Alphabet {
    
    protected char[][] alphabet;
    protected int lastIndex;
    protected BigInteger size;
    
    @Override
    public char[] getFirstElement() {
        return alphabet[0];
    }

    @Override
    public char[] getLastElement() {
        return alphabet[ lastIndex ];
    }

    @Override
    public char[] getElement( int index ) throws IndexOutOfBoundsException {
        return alphabet[ index ];
    }

    @Override
    public BigInteger sizeBI() {
        return size;
    }

    @Override
    public String buildString( int[] passValue ) {
        try {
            StringBuilder sb = new StringBuilder();
            if( passValue.length > 0 ) {
                for( int i = 0; i < passValue.length && passValue[ i ] != -1; i++ ) {
                    sb.append( alphabet[ passValue[ i ] ] );
                }
            }
            return sb.toString();
        } catch( IndexOutOfBoundsException ex ) {
            throw new IllegalArgumentException( "Illegal 'passValue' value." );
        }
    }
}
