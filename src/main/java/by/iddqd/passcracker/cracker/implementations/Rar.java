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

package by.iddqd.passcracker.cracker.implementations;

import by.iddqd.passcracker.cracker.Cracker;
import by.iddqd.passcracker.cracker.MIMEtype;
import by.iddqd.passcracker.shell.ExecutionResult;
import by.iddqd.passcracker.shell.ShellExecutor;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.regex.Pattern;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Cracker for RAR archives.
 * 
 * @author Sergey Protasevich
 */
@MIMEtype( "application/x-rar" )
class Rar extends Cracker {
    
    // TODO Add support of WinRAR on Win
    
    private ShellExecutor executor;
    private String smallestFile;
    
    @Override
    protected void doInit( Path path ) {
        executor = new ShellExecutor( path.getParent() );
    }

    @Override
    protected boolean doTestEnvironment( Path path ) throws InterruptedException {
        try {
            return Pattern.matches(
                    "unrar: /[\\w/]*unrar.*",
                    executor.execute( "whereis", "unrar" )
                            .getStdOut().get( 0 ) );
        } catch( IndexOutOfBoundsException ex ) {
            // if the output is empty
            return false;
        }
    }

    @Override
    protected void doPrepare( Path path ) throws InterruptedException {
        
        // Find the smallest file in the archive, if it is possible
        
        // Inner invocation of process.waitFor() may get stuck if the archive is very large,
        // even if subprocess had read all the output (it takes less than 1 second even on archives >= 5 Gb),
        // so we wait 6 seconds and then forcibly terminate the subprocess
        ExecutionResult result = executor.execute( 6, SECONDS, "unrar", "v", "-p-", path.toString() );
        
        Pattern filePattern = Pattern.compile(
                "[A-Z\\.\\* ]{11} +" // Attributes
                + " *(?<SIZE>\\d+) +" // Size
                + " *\\d+ +" // Packed
                + " *\\d+% *" // Ratio
                + "\\d{2}\\-\\d{2}\\-\\d{2} +" // Date
                + "\\d{2}:\\d{2} +" // Time
                + "[\\dA-Z]{8} +" // Checksum
                + "(?<NAME>.+)" ); // Name
        
        try {
            smallestFile = result
                    .getStdOut()
                    .stream()
                    .map( line -> filePattern.matcher( line ) )
                    .filter( matcher -> matcher.matches() )
                    .collect(
                            () -> new TreeMap<Integer, String>(), // map size to filename
                            ( map, matcher ) -> map.put(
                                    Integer.parseInt( matcher.group( "SIZE" ) ),
                                    matcher.group( "NAME" ) ),
                            ( map1, map2 ) -> map1.putAll( map2 ) )
                    .entrySet()
                    .stream()
                    .filter( entry -> entry.getKey() > 0 ) // filter out directoy enty (if any)
                    .min( ( entry1, entry2 ) -> entry1.getKey().compareTo( entry2.getKey() ) )
                    .get() // throws NoSuchElementException if there are no files in the stdOut
                           // (archive has encrypted filenames)
                    .getValue(); // get the filename
        } catch( NoSuchElementException ex ) {
            smallestFile = "";
        }
    }

    @Override
    protected boolean doTestPassword( Path path, String password ) throws InterruptedException {
        
        /*
         * Exit statuses of unrar:
         *     0      Successful operation.
         *     1      Warning. Non fatal error(s) occurred.
         *     2      A fatal error occurred.
         *     3      Invalid checksum. Data is damaged.
         *     5      Write error.
         *     6      File open error.
         *     7      Wrong command line option.
         *     8      Not enough memory.
         *     9      File create error.
         *     10     No files matching the specified mask and options were found.
         *     11     Wrong password.
         *     255    User break.
         * 
         * Return true:
         * 0 -- the password is correct
         * 1 -- the password is correct (just a warning while extraction/testing)
         * 
         * Return false:
         * 3 -- if the password is wrong (for archives with unencrypted filenames)
         * 10 -- if the password is wrong (for archives with encrypted filenames)
         * 11 -- if the password is wrong (for RAR5 archives with encrypted filenames)
         * 
         * Throw exception:
         * any other exit code
         */
        
        int exitValue = executor.execute( "unrar", "t", "-p" + password, path.toString(), smallestFile )
                .getExitValue();
        
        switch( exitValue ) {
            case 3:
            case 10:
            case 11:
                return false;
            case 0:
            case 1:
                return true;
            default:
                throw new RuntimeException( "Error while executing unrar:"
                        + " exit status = " + exitValue
                        + ", tested password = " + password );
        }
    }
}
