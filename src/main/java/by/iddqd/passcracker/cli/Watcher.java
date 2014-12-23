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

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * Watcher periodically fetches information about current progress and sends it to the Console.
 * 
 * @author Sergey Protasevich
 */
class Watcher extends Thread {
    
    private static final String FORMAT = "Progress: %.2f%% [%s, %s left]";
    private static final BigDecimal BD_100 = BigDecimal.valueOf( 100 );
    private static final BigDecimal BD_LONG_MAX_VALUE = BigDecimal.valueOf( Long.MAX_VALUE );
    
    private volatile PassSupplier passSupplier = null;
    private volatile Console console = null;
    
    private volatile PassSequence passSequence = null;
    private volatile BlockingQueue<int[]> queue = null;
    private volatile BigDecimal size = null;
    
    private volatile long startTimestamp;
    
    private volatile String currentInfo = "";
    

    /**
     * Start the Watcher.
     * 
     * @param passSupplier the PassSupplier
     * @param console the Console
     */
    public void start( PassSupplier passSupplier, Console console ) {
        
        if( isAlive() ) {
            throw new IllegalThreadStateException( "Watcher is already started." );
        }
        
        this.passSupplier = requireNonNull( passSupplier );
        this.console = requireNonNull( console );
        
        passSequence = passSupplier.getPassSequence();
        queue = passSupplier.getQueue();
        size = new BigDecimal( passSequence.size() );
        
        startTimestamp = System.currentTimeMillis();
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
        
        String timeElapsedString = "00:00:00";
        
        while( !isInterrupted() && ( passSupplier.isRunning() || !queue.isEmpty() ) ) {
            
            int[] lastUsedPassword = passSupplier.getLastUsedPassValue();
            if( lastUsedPassword == null ) {
                continue;
            }
            
            long timestamp = System.currentTimeMillis();
            long timeElapsed = timestamp - startTimestamp;
            timeElapsedString = timeString( timeElapsed );
            
            BigDecimal index;
            index = new BigDecimal( passSequence.indexOf( lastUsedPassword ) );
            
            BigDecimal left = size.subtract( index );
            
            double percent = index.multiply( BD_100 )
                    .divide( size, 2, RoundingMode.HALF_UP )
                    .doubleValue();
            
            BigDecimal speed = index.divide( BigDecimal.valueOf( timeElapsed ), 64, RoundingMode.HALF_UP );
            BigDecimal timeLeft;
            try {
                timeLeft = left.divide( speed, 64, RoundingMode.HALF_UP );
            } catch( ArithmeticException ex ) {
                timeLeft = null;
            }
            
            String timeLeftString = timeLeft == null || timeLeft.compareTo( BD_LONG_MAX_VALUE ) == 1
                    ? "undefined time"
                    : '~' + timeString( timeLeft.longValue() );

            try {
                currentInfo = format( FORMAT, percent, timeElapsedString, timeLeftString );
                console.print( currentInfo );
                Thread.sleep( 1000 );
            } catch( InterruptedException ex ) {
                break;
            }
        }
        
        try {
            console.print( format( FORMAT, 100., timeElapsedString, "00:00:00" ) );
        } catch( InterruptedException ignore ) {}
    }

    private String timeString( long timeInMills ) {
        
        long diffSeconds = timeInMills / 1000 % 60;
        long diffMinutes = timeInMills / (60 * 1000) % 60;
        long diffHours = timeInMills / (60 * 60 * 1000) % 24;
        long diffDays = timeInMills / (24 * 60 * 60 * 1000) % 365;
        long diffYears = timeInMills / (365 * 24 * 60 * 60 * 1000);
        
        StringBuilder sb = new StringBuilder();
        if( diffYears > 0 ) {
            sb.append( diffYears ).append( " years " );
        }
        if( diffDays > 0 ) {
            sb.append( diffDays ).append( " days " );
        }
        
        if( diffHours < 10 ) {
            sb.append( '0' ).append( diffHours );
        } else {
            sb.append( diffHours );
        }
        
        sb.append( ':' );
        
        if( diffMinutes < 10 ) {
            sb.append( '0' );
        }
        sb.append( diffMinutes );
        
        sb.append( ':' );
        
        if( diffSeconds < 10 ) {
            sb.append( '0' );
        }
        sb.append( diffSeconds );
        
        return sb.toString();
    }

    String getCurrentInfo() {
        return currentInfo;
    }
}
