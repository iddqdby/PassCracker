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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Executor of shell commands.
 * 
 * @author Sergey Protasevich
 */
public class ShellExecutor {
    
    private static final Runtime RT = Runtime.getRuntime();
    
    private final File defaultWorkingDir;
    
    /**
     * Create new ShellExecutor.
     */
    public ShellExecutor() {
        defaultWorkingDir = null;
    }
    
    /**
     * Create new ShellExecutor with given default working directory.
     * 
     * @param dir the default working directory of the subprocess
     */
    public ShellExecutor( File dir ) {
        if( !dir.isDirectory() ) {
            throw new IllegalArgumentException( dir + " is not a directory" );
        }
        defaultWorkingDir = dir;
    }
    
    /**
     * Create new ShellExecutor with given default working directory.
     * 
     * @param dir the default working directory of the subprocess
     */
    public ShellExecutor( Path dir ) {
        this( dir.toFile() );
    }
    
    /**
     * Executes the specified shell command.
     * 
     * It waits, if necessary, until the execution has terminated.
     * 
     * @param command a specified shell command
     * @return result of the execution
     * @throws InterruptedException if the current thread is interrupted
     */
    public ExecutionResult execute( String command ) throws InterruptedException {
        return execute( command, null, defaultWorkingDir );
    }
    
    /**
     * Executes the specified shell command.
     * 
     * It waits, if necessary, until the execution has terminated.
     * 
     * @param command a specified shell command
     * @param envp array of strings, each element of which has environment
     * variable settings in the format name=value, or null if the subprocess
     * should inherit the environment of the current process
     * @return result of the execution
     * @throws InterruptedException if the current thread is interrupted
     */
    public ExecutionResult execute( String command, String[] envp ) throws InterruptedException {
        return execute( command, envp, defaultWorkingDir );
    }
    
    /**
     * Executes the specified shell command.
     * 
     * It waits, if necessary, until the execution has terminated.
     * 
     * @param command a specified shell command
     * @param envp array of strings, each element of which has environment
     * variable settings in the format name=value, or null if the subprocess
     * should inherit the environment of the current process
     * @param dir the working directory of the subprocess, or null if the
     * subprocess should use default working directory of the current process;
     * if default working directory is null, subprocess will inherit
     * the working directory of the current process
     * @return result of the execution
     * @throws InterruptedException if the current thread is interrupted
     */
    public ExecutionResult execute( String command, String[] envp, File dir ) throws InterruptedException {
        try {
            
            Process p = RT.exec( command, envp, dir );
            int exitValue = p.waitFor();
            
            List<String> stdOut = new ArrayList<>();
            List<String> stdErr = new ArrayList<>();
            
            try(
                BufferedReader isReader = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
                BufferedReader esReader = new BufferedReader( new InputStreamReader( p.getErrorStream() ) )
            ) {
                String line;
                while( null != ( line = isReader.readLine() ) ) {
                    stdOut.add( line );
                }
                while( null != ( line = esReader.readLine() ) ) {
                    stdErr.add( line );
                }
            }
            
            return new ExecutionResult( exitValue, stdOut, stdErr );
            
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }
}
