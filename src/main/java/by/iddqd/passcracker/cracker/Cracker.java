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

package by.iddqd.passcracker.cracker;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;


/**
 * Abstract cracker.
 * 
 * @author Sergey Protasevich
 */
public abstract class Cracker {
    
    private Path path;

    private void setPath( Path path ) {
        this.path = path;
    }
    
    private void init() {
        doInit( path );
    }
    
    private boolean testEnvironment() {
        return doTestEnvironment( path );
    }

    private void prepare() {
        doPrepare( path );
    }
    
    /**
     * Test password.
     * 
     * @param password a password
     * @return true if password fits, false otherwise
     */
    public final boolean testPassword( String password ) {
        return doTestPassword( path, password );
    }
    
    /*--- Override in implementations ---*/
    
    /**
     * Initialize the Cracker.
     * 
     * @param path a path to the file
     */
    protected abstract void doInit( Path path );
    
    /**
     * Test if this Cracker can operate under current environment.
     * 
     * Cracker implementations may use external libraries or shell commands
     * which can be unavailable under some environments.
     * 
     * This method tests if current environment meet the needs of this Cracker.
     * 
     * @param path a path to the file
     * @return true -- if this Cracker can operate under current environment, false -- otherwise
     */
    protected abstract boolean doTestEnvironment( Path path );

    /**
     * Prepare the Cracker.
     * 
     * @param path a path to the file
     */
    protected abstract void doPrepare( Path path );
    
    /**
     * Test password.
     * 
     * @param path a path to the file
     * @param password a password
     * @return true if password fits, false otherwise
     */
    protected abstract boolean doTestPassword( Path path, String password );
    
    /*--- Factory method ---*/
    
    /**
     * Get cracker for given file.
     * 
     * @param path path to file
     * @return the Cracker for given file
     * @throws IllegalArgumentException if the file is not a regular file or is not readable
     * @throws ClassNotFoundException if there is no suitable Cracker
     * for the MIME type of the given file
     * @throws IllegalStateException if this Cracker cannot operate
     * under current environment
     * @throws IOException if an I/O error occurs
     */
    public static Cracker loadCracker( Path path )
            throws IllegalArgumentException, ClassNotFoundException, IllegalStateException, IOException {
        
        if( !Files.isRegularFile( path ) || !Files.isReadable( path ) ) {
            throw new IllegalArgumentException( path.toString()
                    + " is not a regular file or is not readable." );
        }
        
        String mimeType = Files.probeContentType( path );
        
        try {
            Class<? extends Cracker> crackerClass = CrackerClassLoader.get().loadClass( mimeType );
            
            Constructor<? extends Cracker> constructor = crackerClass.getDeclaredConstructor();
            constructor.setAccessible( true );
            
            Cracker cracker = constructor.newInstance();
            
            cracker.setPath( path );
            cracker.init();
            
            if( !cracker.testEnvironment() ) {
                throw new IllegalStateException(
                        "The cracker for MIME type " + mimeType
                        + " cannot operate under current environment." );
            }
            
            cracker.prepare();
            
            return cracker;
            
        } catch( SecurityException
                | InvocationTargetException
                | InstantiationException
                | NoSuchMethodException
                | IllegalAccessException
                | IllegalArgumentException ex ) {
            throw new RuntimeException(
                    "Cracker implementation for MIME type \"" + mimeType + "\" is not valid.", ex );
        }
    }
}
