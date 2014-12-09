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
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterator for SimplePassSequence.
 * 
 * @author Sergey Protasevich
 */
class SimplePassSequenceIterator implements Iterator<String> {
    
    protected final int[] value;
    protected final int maxLength;
    protected final Alphabet alphabet;
    protected final int alphabetSize;
    
    private boolean hasNext = true;

    
    protected SimplePassSequenceIterator( int[] initialValue, int maxLength, Alphabet alphabet ) {
        this.value = Arrays.copyOf( initialValue, initialValue.length );
        this.maxLength = maxLength;
        this.alphabet = alphabet;
        this.alphabetSize = alphabet.size();
    }
    
    
    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public String next() {
        if( !hasNext() ) {
            throw new NoSuchElementException();
        }

        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < maxLength && value[ i ] != -1; i++ ) {
            sb.append( alphabet.getElement( value[ i ] ) );
        }

        increment();

        return sb.toString();
    }

    // incrementing the value
    protected void increment() {
        int pos = 0;
        try {
            while( true ) {
                if( value[ pos ] == -1 ) {
                    value[ pos ] = 0;
                    break;
                }
                if( value[ pos ] == alphabetSize - 1 ) {
                    value[ pos ] = 0;
                    pos++;
                    continue;
                }
                value[ pos ]++;
                break;
            }
        } catch( IndexOutOfBoundsException ex ) {
            hasNext = false;
        }
    }
}
