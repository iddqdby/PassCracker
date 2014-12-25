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

package by.iddqd.utils;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

/**
 * JSON array of integer values.
 * 
 * This class is immutable.
 * 
 * @author Sergey Protasevich
 */
public final class JSONArrayOfInt implements Serializable {
    
    /**
     * Regex pattern of valid JSON array of integers.
     */
    public static final Pattern PATTERN = Pattern.compile( "\\[\\s*((\\-?\\d+,\\s*)*(\\-?\\d+){1})?\\s*\\]" );
    
    private final int[] arrayValue;
    private final String stringValue;

    /**
     * Create empty JSON array.
     */
    public JSONArrayOfInt() {
        arrayValue = new int[0];
        stringValue = "[]";
    }
    
    /**
     * Create JSON array.
     * 
     * @param value array of integers
     */
    public JSONArrayOfInt( int[] value ) {
        arrayValue = Arrays.copyOf( value, value.length );
        stringValue = intArrayToString( value );
    }

    /**
     * Create JSON array.
     * 
     * @param value string representation of array
     */
    public JSONArrayOfInt( String value ) {
        arrayValue = stringToIntArray( value );
        stringValue = intArrayToString( arrayValue ); // save normalized string
    }
    
    private String intArrayToString( int[] array ) throws NullPointerException {
        
        List<String> list = new ArrayList<>();
        for( int p : requireNonNull( array ) ) {
            list.add( String.valueOf( p ) );
        }
        
        return new StringBuilder( "[" )
                .append( String.join( ",", list ) )
                .append( "]" )
                .toString()
                .intern();
    }
    
    private int[] stringToIntArray( String string ) throws NullPointerException, IllegalArgumentException {
        
        if( !PATTERN.matcher( requireNonNull( string ) ).matches() ) {
            throw new IllegalArgumentException( "Fail to parse array of integers." );
        }
        
        String[] stringArray = string.replaceAll( "[\\s\\[\\]]+", "" ).split( "," );
        int[] intArray = new int[ stringArray.length ];
        
        for( int i = 0; i < stringArray.length; i++ ) {
            intArray[ i ] = Integer.parseInt( stringArray[ i ] );
        }
        
        return intArray;
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

    @Override
    public boolean equals( Object obj ) {
        if( !( obj instanceof JSONArrayOfInt ) ) {
            return false;
        }
        return Arrays.equals( arrayValue, ((JSONArrayOfInt)obj).arrayValue );
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 67 * hash + Arrays.hashCode( this.arrayValue );
        hash = 67 * hash + Objects.hashCode( this.stringValue );
        return hash;
    }

    /*--- Serialization --- */
    
    private void writeObject( ObjectOutputStream out ) throws IOException {
        out.writeUTF( intArrayToString( arrayValue ) );
    }

    private void readObject( ObjectInputStream in ) throws IOException, ClassNotFoundException {
        try {
            int[] intArray = stringToIntArray( in.readUTF() );
            String string = intArrayToString( intArray ); // normalize the initial string
            
            Field arrayValueField = getClass().getDeclaredField( "arrayValue" );
            Field stringValueField = getClass().getDeclaredField( "stringValue" );
            
            arrayValueField.setAccessible( true );
            stringValueField.setAccessible( true );
            
            arrayValueField.set( this, intArray );
            stringValueField.set( this, string );
            
        } catch( IllegalArgumentException ex ) {
            throw new ClassNotFoundException( "String is not a valid JSON array of integers.", ex );
        } catch( NoSuchFieldException | SecurityException | IllegalAccessException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
