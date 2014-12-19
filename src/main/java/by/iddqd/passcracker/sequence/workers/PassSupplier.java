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

import by.iddqd.passcracker.sequence.PassSequence;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;


/**
 * Password supplier.
 * 
 * It uses provided PassSequence to concurrently generate passwords and put them
 * into the queue.
 * 
 * @author Sergey Protasevich
 */
public class PassSupplier {
    
    public static final int DEFAULT_QUEUE_CAPACITY = 1024;
    
    private final PassSequence passSequence;
    private final BlockingQueue<String> queue;
    
    private volatile String lastUsedPassword = null;
    
    private final Thread thread;
    
    /**
     * Create new supplier.
     * 
     * @param passSequence sequence of passwords
     * @return supplier.
     */
    public static PassSupplier create( PassSequence passSequence ) {
        return create( passSequence, DEFAULT_QUEUE_CAPACITY );
    }
    
    /**
     * Create new supplier.
     * 
     * @param passSequence sequence of passwords
     * @param queueCapacity capacity of queue
     * @return supplier.
     * @throws IllegalArgumentException if capacity is less than 1
     */
    public static PassSupplier create( PassSequence passSequence, int queueCapacity ) {
        PassSupplier ps = new PassSupplier( passSequence, queueCapacity );
        ps.start();
        return ps;
    }
    
    private PassSupplier( PassSequence passSequence, int queueCapacity ) {
        this.passSequence = passSequence;
        queue = new ArrayBlockingQueue<>( queueCapacity );
        thread = new Thread() {
            @Override
            public void run() {
                for( String password : PassSupplier.this.passSequence ) {
                    
                    if( isInterrupted() ) {
                        return;
                    }
                    
                    try {
                        queue.put( password );
                    } catch( InterruptedException ex ) {
                        return;
                    }
                }
            }
        };
        thread.setDaemon( true );
    }
    
    private void start() {
        thread.start();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        thread.interrupt();
    }

    /**
     * Shutdown the supplier.
     */
    void shutdown() {
        thread.interrupt();
        queue.clear();
    }
    
    /**
     * Tests if this supplier is running.
     * 
     * @return true if this supplier is running; false otherwise.
     */
    public boolean isRunning() {
        return thread.isAlive();
    }

    /**
     * Get the queue.
     * 
     * @return the queue.
     */
    public BlockingQueue<String> getQueue() {
        return queue;
    }

    /**
     * Get the PassSequence.
     * 
     * @return the PassSequence
     */
    public PassSequence getPassSequence() {
        return passSequence;
    }

    /**
     * Get last used password.
     * 
     * @return last used password
     */
    public String getLastUsedPassword() {
        return lastUsedPassword;
    }

    /**
     * Set last used password.
     * 
     * @param lastUsedPassword last used password
     */
    void setLastUsedPassword( String lastUsedPassword ) {
        this.lastUsedPassword = lastUsedPassword;
    }
}
