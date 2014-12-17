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

package by.iddqd.passcracker.crackers;

import java.io.IOException;
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

    /**
     * Get the Path for this Cracker.
     * 
     * @return the Path
     */
    protected final Path getPath() {
        return path;
    }
    
    /**
     * Initialize the Cracker.
     */
    protected abstract void init();
    
    /**
     * Test if this Cracker can operate under current environment.
     * 
     * Cracker implementations may use external libraries or shell commands
     * which can be unavailable under some environments.
     * 
     * This method tests if current environment meet the needs of this Cracker.
     * 
     * @return true -- if this Cracker can operate under current environment, false -- otherwise
     */
    public abstract boolean testEnvironment();
    
    /**
     * Test password.
     * 
     * @param password a password
     * @return true if password fits, false otherwise
     */
    public abstract boolean testPassword( String password );
    
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
            Cracker cracker = CrackerClassLoader.get().loadClass( mimeType ).newInstance();
            
            cracker.setPath( path );
            cracker.init();
            
            if( !cracker.testEnvironment() ) {
                throw new IllegalStateException(
                        "The cracker for MIME type " + mimeType
                        + " cannot operate under current environment." );
            };
            
            return cracker;
            
        } catch( SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException ex ) {
            throw new RuntimeException(
                    "Cracker implementation for MIME type " + mimeType + " is illegal.", ex );
        }
    }
}
