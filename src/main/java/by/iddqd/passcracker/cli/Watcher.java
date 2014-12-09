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

import by.iddqd.passcracker.sequence.PassSequence;
import by.iddqd.passcracker.sequence.workers.PassSupplier;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.util.Objects.requireNonNull;

/**
 * Watcher periodically fetches information about current progress and sends it to the Console.
 * 
 * @author Sergey Protasevich
 */
class Watcher extends Thread {
    
    private volatile PassSupplier passSupplier = null;
    private volatile Console console = null;
    
    private volatile int minLength;
    private volatile int maxLength;
    private volatile int threads;
    

    /**
     * Start the Watcher.
     * 
     * @param passSupplier the PassSupplier
     * @param console the Console
     */
    public void start( PassSupplier passSupplier, Console console,
            int minLength, int maxLength, int threads ) {
        
        if( isAlive() ) {
            throw new IllegalThreadStateException( "Watcher is already started." );
        }
        
        this.minLength = minLength;
        this.maxLength = maxLength;
        this.threads = threads;
        
        this.passSupplier = requireNonNull( passSupplier );
        this.console = requireNonNull( console );
        start();
    }

    @Override
    @SuppressWarnings( "SleepWhileInLoop" )
    public void run() {
        
        if( passSupplier == null || console == null ) {
            throw new IllegalStateException(
                    "Watcher must be started through the method"
                    + " start(PassSupplier passSupplier, Console console)" );
        }
        
        PassSequence passSequence = passSupplier.getPassSequence();
        BlockingQueue<String> queue = passSupplier.getQueue();

        BigDecimal size = new BigDecimal( passSequence.size() );
        
        String prefix = "Parameters: min length = " + minLength
                + ", max length = " + maxLength
                + ", threads = " + threads + ". ";
        String format = prefix + "Progress: %.2f%%";

        while( !isInterrupted() && ( passSupplier.isRunning() || !queue.isEmpty() ) ) {

            String currentPassword = queue.peek();
            if( currentPassword == null ) {
                continue;
            }

            double percent = new BigDecimal( passSequence.indexOf( currentPassword ) )
                    .divide( size, 5, RoundingMode.DOWN ).doubleValue()
                    * 100;

            try {
                console.print( String.format( format, percent ) );
                Thread.sleep( 1000 );
            } catch( InterruptedException ex ) {
                break;
            }
        }
        
        try {
            console.print( String.format( format, 100. ) );
        } catch( InterruptedException ignore ) {}
    }
}
