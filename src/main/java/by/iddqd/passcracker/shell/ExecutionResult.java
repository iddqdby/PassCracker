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

package by.iddqd.passcracker.shell;

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Result of execution of a shell command.
 * 
 * @author Sergey Protasevich
 */
public class ExecutionResult {

    private final int exitValue;
    private final List<String> stdOut;
    private final List<String> stdErr;
    
    ExecutionResult( int exitValue, List<String> stdOut, List<String> stdErr ) {
        this.exitValue = exitValue;
        this.stdOut = unmodifiableList( stdOut );
        this.stdErr = unmodifiableList( stdErr );
    }

    /**
     * Get the exit value of the executed shell command.
     * 
     * @return the exit value
     */
    public int getExitValue() {
        return exitValue;
    }

    /**
     * Get the normal output of the executed shell command.
     * 
     * @return the normal output of the executed shell command
     */
    public List<String> getStdOut() {
        return stdOut;
    }

    /**
     * Get the error output of the executed shell command.
     * 
     * @return the error output of the executed shell command
     */
    public List<String> getStdErr() {
        return stdErr;
    }
}
