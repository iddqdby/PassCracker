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
        
        arrayValue = requireNonNull( value );
        
        StringBuilder sb = new StringBuilder( "[" );
        
        List<String> list = new ArrayList<>();
        for( int p : value ) {
            list.add( String.valueOf( p ) );
        }
        
        if( !list.isEmpty() ) {
            sb.append( list.remove( 0 ) );
            list.stream().forEachOrdered( index -> sb.append( ',' ).append( index ) );
        }
        
        stringValue = sb.append( "]" ).toString();
    }

    /**
     * Create JSON array.
     * 
     * @param value string representation of array
     */
    public JSONArrayOfInt( String value ) {
        
        stringValue = requireNonNull( value );
        
        if( !PATTERN.matcher( value ).matches() ) {
            throw new IllegalArgumentException( "Fail to parce array of integers." );
        }
        
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
