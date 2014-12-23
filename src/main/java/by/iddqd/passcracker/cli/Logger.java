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

import by.iddqd.passcracker.sequence.workers.PassSupplier;
import by.iddqd.passcracker.utils.JSONArrayOfInt;
import java.io.IOError;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import static java.lang.String.format;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.WRITE;
import static java.util.Objects.requireNonNull;
import static java.util.Collections.singleton;

/**
 * Logger of current progress.
 * 
 * @author Sergey Protasevich
 */
class Logger extends Thread {

    private final int WAIT_TIME = 60;
    
    private volatile Watcher watcher = null;
    private volatile PassSupplier passSupplier;
    private volatile Path log = null;
    private volatile Map<String, String> options = null;
    private volatile Console console = null;
    
    void setUpAndStart( Console console, Map<String, String> options,
            Watcher watcher, PassSupplier passSupplier, Path log ) {
        
        this.watcher = requireNonNull( watcher );
        this.passSupplier = requireNonNull( passSupplier );
        this.options = requireNonNull( options );
        this.console = requireNonNull( console );
        
        try {
            this.log = requireNonNull( log ).toAbsolutePath();
            Files.createFile( this.log );
        } catch( IOException | IOError ex ) {
            throw new IllegalArgumentException(
                    "Fail to create file for storing last used password.", ex );
        }
        
        start();
    }

    @Override
    @SuppressWarnings( "SleepWhileInLoop" )
    public void run() {
        if( watcher == null || passSupplier == null || console == null || options == null || log == null ) {
            throw new IllegalThreadStateException( "You must call Logger::setUpAndStart()." );
        }
        
        String line = options.entrySet().stream()
                .reduce( new StringBuilder( "Started: " )
                        .append( new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date() ) )
                        .append( "\nOptions:\n" ),
                        ( sb, entry ) -> sb
                                .append( entry.getKey() ).append( " = " )
                                .append( entry.getValue() ).append( '\n' ),
                        ( sb1, sb2 ) -> sb1.append( sb2 ) )
                .toString();
        
        do {
            try {
                Files.write( log, singleton( line ), WRITE, APPEND );
            } catch( IOException ex ) {
                console.println( "Fail to save progress to the log: I/O error. Info: " + line );
            }
            
            try {
                Thread.sleep( 1000 * WAIT_TIME );
            } catch( InterruptedException ex ) {
                break;
            }
            
            String dateTime = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" ).format( new Date() );
            String currentInfo = watcher.getCurrentInfo();
            String lastUsedPassword = new JSONArrayOfInt( passSupplier.getLastUsedPassValue() ).toString();
            
            line = format( "[%s] [%s] [Last used password: %s]", dateTime, currentInfo, lastUsedPassword );
            
        } while( !isInterrupted() );
    }
}
