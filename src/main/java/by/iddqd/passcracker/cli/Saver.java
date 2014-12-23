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

package by.iddqd.passcracker.cli;

import by.iddqd.passcracker.utils.JSONArrayOfInt;
import by.iddqd.passcracker.sequence.workers.PassSupplier;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.util.Collections.singleton;
import static java.util.Objects.requireNonNull;

/**
 * Password saver.
 * 
 * It periodically saves the last used password to a file.
 * 
 * @author Sergey Protasevich
 */
class Saver extends Thread {
    
    public static final int DEFAULT_WAIT_TIME = 60;
    
    private volatile Path path = null;
    private volatile long wait = 0;
    private volatile PassSupplier passSupplier = null;
    private volatile Console console = null;
    
    public void setUpAndStart( Console console, Path path, long wait, PassSupplier passSupplier ) {
        if( wait <= 0 ) {
            throw new IllegalArgumentException( "Wait time must be > 0." );
        }
        this.wait = wait;
        
        try {
            this.path = requireNonNull( path ).toAbsolutePath();
            Files.createFile( this.path );
        } catch( IOException | IOError ex ) {
            throw new IllegalArgumentException(
                    "Fail to create file for storing last used password.", ex );
        }
        
        this.passSupplier = requireNonNull( passSupplier );
        this.console = requireNonNull( console );
        
        start();
    }

    @Override
    @SuppressWarnings( "SleepWhileInLoop" )
    public void run() {
        if( path == null || passSupplier == null || console == null || wait == 0 ) {
            throw new IllegalThreadStateException( "You must call Saver::setUpAndStart()." );
        }
        
        while( !isInterrupted() ) {
            try {
                Thread.sleep( 1000 * wait );
            } catch( InterruptedException ex ) {
                return;
            }
            
            JSONArrayOfInt lastUsedPassword = new JSONArrayOfInt( passSupplier.getLastUsedPassValue() );
            try {
                Files.write( path, singleton( lastUsedPassword.toString() ) );
            } catch( IOException ex ) {
                console.println( "Fail to save last used password: I/O error. Password: " + lastUsedPassword );
            }
        }
    }
}
