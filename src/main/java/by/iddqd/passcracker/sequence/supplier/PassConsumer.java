/**
 *  PassSequence
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

package by.iddqd.passcracker.sequence.supplier;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Password supplier.
 * 
 * It uses provided PassSupplier to concurrently test passwords from the queue
 * and do something if one of them matches the criterion.
 * 
 * @author Sergey Protasevich
 */
public class PassConsumer implements Runnable {

    private final PassSupplier ps;
    private final BlockingQueue<String> queue;
    
    private final Predicate<String> predicate;
    private final Consumer<String> consumer;
    
    /**
     * Create new PassConsumer.
     * 
     * @param ps supplier of passwords
     * @param predicate predicate to test each password
     * @param consumer consumer to do something with password that matches the predicate
     */
    public PassConsumer( PassSupplier ps, Predicate<String> predicate, Consumer<String> consumer ) {
        this.ps = ps;
        this.queue = ps.getQueue();
        this.predicate = predicate;
        this.consumer = consumer;
    }
    
    @Override
    public void run() {
        while( ps.isRunning() || !queue.isEmpty() ) {
            
            if( Thread.currentThread().isInterrupted() ) {
                return;
            }
            
            try {
                
                String password = queue.poll( 60, TimeUnit.SECONDS );
                if( password == null ) {
                    continue;
                }
                
                if( predicate.test( password ) ) {
                    ps.shutdown();
                    consumer.accept( password );
                    return;
                }
                
            } catch( InterruptedException ex ) {
                return;
            }
        }
    }
}
