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

package by.iddqd.passcracker.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * JSON array of integer values.
 * 
 * This class is immutable.
 * 
 * @author Sergey Protasevich
 */
public final class JSONArrayOfInt {
    
    /**
     * Regex pattern of valid JSON array of integers.
     */
    public static final Pattern PATTERN = Pattern.compile( "\\[ *((\\-?\\d+, *)*(\\-?\\d+){1})? *\\]" );
    
    private final int[] arrayValue;
    private final String stringValue;

    /**
     * Create JSON array.
     * 
     * @param value array of integers
     */
    public JSONArrayOfInt( int[] value ) {
        
        arrayValue = Arrays.copyOf( requireNonNull( value ), value.length );
        
        List<String> list = new ArrayList<>();
        for( int p : arrayValue ) {
            list.add( String.valueOf( p ) );
        }
        
        stringValue = new StringBuilder( "[" )
                .append( String.join( ",", list ) )
                .append( "]" )
                .toString();
    }

    /**
     * Create JSON array.
     * 
     * @param value string representation of array
     */
    public JSONArrayOfInt( String value ) {
        
        if( !PATTERN.matcher( requireNonNull( value ) ).matches() ) {
            throw new IllegalArgumentException( "Fail to parse array of integers." );
        }
        
        stringValue = value;
        
        String[] array = value.replaceAll( "[ \\[\\]]+", "" ).split( "," );
        int[] intArray = new int[ array.length ];
        
        for( int i = 0; i < array.length; i++ ) {
            intArray[ i ] = Integer.parseInt( array[ i ] );
        }
        
        arrayValue = intArray;
    }
    
    /**
     * Get the array.
     * 
     * @return a copy of initial array
     */
    public int[] getArray() {
        return Arrays.copyOf( arrayValue, arrayValue.length );
    }

    /**
     * Get string representation of the array.
     * 
     * @return string representation of the array
     */
    @Override
    public String toString() {
        return stringValue;
    }
}
