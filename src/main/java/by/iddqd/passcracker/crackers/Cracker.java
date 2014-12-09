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
import java.nio.file.Path;


/**
 * Abstract cracker.
 * 
 * @author Sergey Protasevich
 */
public abstract class Cracker {
    
    protected final Path path;

    protected Cracker( Path path ) {
        this.path = path;
    }
    
    /**
     * Test if this Cracker can operate under current environment.
     * 
     * Cracker implementations may use external libraries or shell commands
     * which can be unavailable under some environments.
     * 
     * This method tests if current environment meet the needs of this Cracker.
     * 
     * @throws IllegalStateException if this Cracker cannot operate under current environment
     */
    public abstract void testEnvironment() throws IllegalStateException;
    
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
        return CrackerClassLoader.get().loadCracker( path );
    }
}
