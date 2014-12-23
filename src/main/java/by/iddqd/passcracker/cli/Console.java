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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.lang.System.err;
import static java.lang.System.out;

/**
 * Console is used for printing information to STDOUT and STDERR.
 * 
 * @author Sergey Protasevich
 */
class Console extends Thread {
    
    private static final int LINE_WIDTH = 80;

    private final BlockingQueue<String> queue = new ArrayBlockingQueue<>( 8 );
    private volatile String terminationMessage = "Terminated.";
    private volatile String result = null;
    
    
    @Override
    public void run() {
        StringBuilder whitespace = new StringBuilder();
        StringBuilder erase = new StringBuilder();
        for( int i = 0; i < LINE_WIDTH; i++ ) {
            whitespace.append( ' ' );
            erase.append( '\r' );
        }
        err.print( whitespace );
        
        String eraseString = erase.toString();
        
        while( !isInterrupted() ) {
            
            StringBuilder line;
            
            try {
                line = new StringBuilder( queue.take() );
            } catch( InterruptedException ex ) {
                break;
            }
            
            for( int i = line.length(); i < LINE_WIDTH; i++ ) {
                line.append( ' ' );
            }
            
            err.print( eraseString + line.toString() );
        }
        
        err.println( '\n' + terminationMessage );
        if( result != null ) {
            out.println( result );
        }
    }
    
    void print( String string ) throws InterruptedException {
        queue.put( string );
    }
    
    void terminate( String message, String result ) {
        this.terminationMessage = message;
        this.result = result;
        interrupt();
    }

    void terminate( String message ) {
        terminate( message, null );
    }

    void println( String string ) {
        err.println( '\n' + string );
    }
}
