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

package by.iddqd.passcracker.sequence.workers;

import by.iddqd.passcracker.sequence.alphabet.Alphabet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Password consumer.
 * 
 * It uses provided PassSupplier to concurrently test passwords from the queue
 * and do something if one of them matches the criterion.
 * 
 * @author Sergey Protasevich
 */
public class PassConsumer implements Callable<Boolean> {

    private final PassSupplier ps;
    private final Alphabet alphabet;
    private final BlockingQueue<int[]> queue;
    
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
        this.alphabet = ps.getAlphabet();
        this.queue = ps.getQueue();
        this.predicate = predicate;
        this.consumer = consumer;
    }
    
    @Override
    public Boolean call() {
        while( !Thread.currentThread().isInterrupted() && ( ps.isRunning() || !queue.isEmpty() ) ) {
            
            try {
                
                int[] passValue = queue.poll( 1, TimeUnit.SECONDS );
                if( passValue == null ) {
                    continue;
                }
                
                String password = alphabet.buildString( passValue );
                
                if( predicate.test( password ) ) {
                    ps.shutdown();
                    consumer.accept( password );
                    return true;
                }
                
                ps.setLastUsedPassValue( passValue );
                
            } catch( InterruptedException ex ) {
                return false;
            }
        }
        
        return false;
    }
}
