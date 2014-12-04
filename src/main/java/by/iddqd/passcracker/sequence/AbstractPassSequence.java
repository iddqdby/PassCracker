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


/**
 * Abstract sequence of passwords.
 * 
 * @author Sergey Protasevich
 */
public abstract class AbstractPassSequence implements PassSequence {
    
    protected final int minLength;
    protected final int maxLength;

    /**
     * Create the sequence.
     * 
     * Depending on implementation, "length" may mean the amount of characters
     * in generated passwords or the amount of simple tokens (for ex. dictionary words)
     * in the generated passwords.
     * 
     * @param minLength minimum length of passwords in this sequence
     * @param maxLength maximum length of passwords in this sequence
     * @throws IllegalArgumentException if minLength or maxLenght are illegal.
     */
    public AbstractPassSequence( int minLength, int maxLength ) throws IllegalArgumentException {
        
        if( maxLength < minLength || minLength < 0 ) {
            throw new IllegalArgumentException( "'maxLength' and/or 'minLength' are illegal" );
        }
        
        this.minLength = minLength;
        this.maxLength = maxLength;
    }

    /**
     * Get the minimum length.
     * 
     * @return the minimum length.
     */
    public final int getMinLength() {
        return minLength;
    }

    /**
     * Get the maximum length.
     * 
     * @return the maximum length.
     */
    public final int getMaxLength() {
        return maxLength;
    }
}
