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
    
    private void init() throws InterruptedException {
        doInit( path );
    }
    
    private boolean testEnvironment() throws InterruptedException {
        return doTestEnvironment( path );
    }

    private void prepare() throws InterruptedException {
        doPrepare( path );
    }
    
    /**
     * Test password.
     * 
     * @param password a password
     * @return true if password fits, false otherwise
     */
    public final boolean testPassword( String password ){
        try {
            return doTestPassword( path, password );
        } catch( InterruptedException ex ) {
            Thread.currentThread().interrupt(); // thread interruption must be handled by PassConsumer
            return false;
        }
    }
    
    /*--- Override in implementations ---*/
    
    /**
     * Initialize the Cracker.
     * 
     * @param path a path to the file
     * @throws InterruptedException if the current thread is interrupted
     */
    protected abstract void doInit( Path path ) throws InterruptedException;
    
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
     * @throws InterruptedException if the current thread is interrupted
     */
    protected abstract boolean doTestEnvironment( Path path ) throws InterruptedException;

    /**
     * Prepare the Cracker.
     * 
     * @param path a path to the file
     * @throws InterruptedException if the current thread is interrupted
     */
    protected abstract void doPrepare( Path path ) throws InterruptedException;
    
    /**
     * Test password.
     * 
     * @param path a path to the file
     * @param password a password
     * @return true if password fits, false otherwise
     * @throws InterruptedException if the current thread is interrupted
     */
    protected abstract boolean doTestPassword( Path path, String password ) throws InterruptedException;
    
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
            cracker.setPath( path.toAbsolutePath() );
            
            try {
                cracker.init();

                if( !cracker.testEnvironment() ) {
                    throw new IllegalStateException(
                            "The cracker for MIME type " + mimeType
                            + " cannot operate under current environment." );
                }

                cracker.prepare();
                
            } catch( InterruptedException ex ) {
                throw new RuntimeException( "Unexpected interruption", ex );
            }
            
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
