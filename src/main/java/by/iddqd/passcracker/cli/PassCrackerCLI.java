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

import by.iddqd.passcracker.cracker.Cracker;
import by.iddqd.passcracker.sequence.PassSequence;
import by.iddqd.passcracker.sequence.alphabet.Alphabet;
import by.iddqd.passcracker.sequence.workers.PassConsumer;
import by.iddqd.passcracker.sequence.workers.PassSupplier;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Command-line interface for PassCracker
 * 
 * @author Sergey Protasevich
 */
public class PassCrackerCLI implements Runnable {

    public static final int EXIT_CODE_SUCCESS = 0;
    public static final int EXIT_CODE_FAIL = 1;
    public static final int EXIT_CODE_INTERRUPTED = 2;
    public static final int EXIT_CODE_ILLEGAL_ARGS = 3;
    public static final int EXIT_CODE_ERROR = 5;
    
    private static final Pattern OPTION = Pattern
            .compile( "\\-\\-(?<OPTION>\\w+\\d*)\\=(?<VALUE>[\\d\\w]+)" );


    private static final int THREADS = Runtime.getRuntime().availableProcessors() + 1;
    
    private final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private final Console console = new Console();
    private final Watcher watcher = new Watcher();
    
    private final PassSequence passSequence;
    private final Alphabet alphabet;
    private final Path path;
    
    private volatile String password;
    
    private int exitCode;
    
    
    private PassCrackerCLI(
            int minLength, int maxLength,
            String sequenceType, String alphabetType,
            Map<String, String> options,
            Path path )
            throws IllegalArgumentException {
        
        this.path = path;
        
        alphabet = AlphabetFactory.create( alphabetType, options );
        passSequence = PassSequenceFactory.create( minLength, maxLength, sequenceType, alphabet, options );
    }

    @Override
    public void run() {
        try {
            console.start();
            
            PassSupplier passSupplier = PassSupplier.create( passSequence );
            Cracker cracker = Cracker.loadCracker( path );
            
            watcher.start( passSupplier, console, THREADS );
            
            List<Future<Boolean>> futures = new ArrayList<>( THREADS );
            for( int i = 0; i < THREADS; i++ ) {
                futures.add( EXECUTOR
                        .submit( new PassConsumer( passSupplier, cracker::testPassword, this::savePassword ) ) );
            }
            
            Thread.sleep( 1000 );
            
            boolean found = false;
            for( Future<Boolean> future : futures ) {
                boolean result;
                try {
                    result = future.get();
                } catch( ExecutionException ex ) {
                    result = false;
                }
                if( result ) {
                    found = true;
                    break;
                }
            }
            
            if( found ) {
                watcher.interrupt();
                console.terminate( "Success. The password is:", password );
                exitCode = EXIT_CODE_SUCCESS;
            } else {
                watcher.interrupt();
                console.terminate( "Fail to find password." );
                exitCode = EXIT_CODE_FAIL;
            }
        } catch( InterruptedException ex ) {
            console.terminate( "Execution was interrupted." );
            exitCode = EXIT_CODE_INTERRUPTED;
        } catch( IllegalArgumentException ex ) {
            console.terminate( "The file is not a regular file or is not readable." );
            exitCode = EXIT_CODE_ERROR;
        } catch( IllegalStateException ex ) {
            console.terminate( "Can not process this file under current environment." );
            exitCode = EXIT_CODE_ERROR;
        } catch( ClassNotFoundException ex ) {
            console.terminate( "File format is not supported." );
            exitCode = EXIT_CODE_ERROR;
        } catch( IOException ex ) {
            console.terminate( "Unable to read file: I/O error." );
            exitCode = EXIT_CODE_ERROR;
        } catch( Exception ex ) {
            console.terminate( "Unexpected error: " + ex );
            exitCode = EXIT_CODE_ERROR;
        } finally {
            watcher.interrupt();
            console.interrupt();
            System.exit( exitCode );
        }
    }
    
    private void savePassword( String password ) {
        this.password = password;
    }

    private static PassCrackerCLI createInstance( String[] args ) {
        try {
            if( args.length < 5 ) {
                throw new IllegalArgumentException();
            }

            Path path = null;
            Map<String, String> options = new HashMap<>();
            
            for( String arg : args ) {
                Matcher optionMatcher = OPTION.matcher( arg );
                if( optionMatcher.matches() ) {
                    options.put( optionMatcher.group( "OPTION" ), optionMatcher.group( "VALUE" ) );
                } else if( path == null ) {
                    path = Paths.get( arg );
                } else {
                    throw new IllegalArgumentException();
                }
            }
            
            int minLength = Integer.parseInt( options.get( "minLength" ) );
            int maxLength = Integer.parseInt( options.get( "maxLength" ) );
            
            if( maxLength < minLength || minLength < 0 ) {
                throw new IllegalArgumentException();
            }
            
            String sequenceType = options.get( "sequenceType" );
            String alphabetType = options.get( "alphabetType" );
            
            if( sequenceType == null || alphabetType == null ) {
                throw new IllegalArgumentException();
            }
            
            return new PassCrackerCLI( minLength, maxLength, sequenceType, alphabetType, options, path );
            
        } catch( IllegalArgumentException | NullPointerException ex ) {
            System.err.println( getUsageInfo() );
            System.exit( EXIT_CODE_ILLEGAL_ARGS );
            return null;
        }
    }

    private static String getUsageInfo() {
        return "Usage:\n\n"
                + "java -jar PassCrackerCLI.jar [options...]\n\n"
                + "where options are:\n\n"
                + "\t--minLength=[value] -- minimum amount of tokens in the generated passwords\n"
                + "\t--maxLength=[value] -- maximum amount of tokens in the generated passwords\n"
                + "\t--sequenceType=[value] -- type of sequence\n"
                + "\t--alphabetType=[value] -- type of alphabet\n\n"
                + "Supported types of sequence and its options:\n\n"
                + PassSequenceFactory.getOptionsInfo()
                + "\n\n"
                + "Supported types of alphabet and its options:\n\n"
                + AlphabetFactory.getOptionsInfo();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main( String[] args ) {
        PassCrackerCLI.createInstance( args ).run();
    }
}
