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

package by.iddqd.passcracker.cracker.implementations.nativelibs;

/**
 * Result of archive testing.
 * 
 * @author Sergey Protasevich
 */
final class Result {
    
    public static enum Value {
        UNDEFINED,
        OK,
        FAIL,
        ERROR;
    }

    private volatile Value value = Value.UNDEFINED;

    synchronized void setOk() {
        value = Value.OK;
        notifyAll();
    }

    synchronized void setFail() {
        value = Value.FAIL;
        notifyAll();
    }

    synchronized void setError() {
        value = Value.ERROR;
        notifyAll();
    }

    synchronized Value get() {
        while( Value.UNDEFINED.equals( value ) ) {
            try {
                wait();
            } catch( InterruptedException ex ) {}
        }
        return value;
    }
}