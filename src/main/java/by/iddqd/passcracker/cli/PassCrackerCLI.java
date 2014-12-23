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

import static java.util.Objects.requireNonNull;

/**
 * Command-line interface for PassCracker.
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
            .compile( "\\-\\-(?<OPTION>\\w+\\d*)\\=(?<VALUE>.+)" );


    private static final int THREADS = Runtime.getRuntime().availableProcessors() + 1;
    
    private final ExecutorService EXECUTOR = Executors.newCachedThreadPool();
    private final Console console = new Console();
    private final Watcher watcher = new Watcher();
    private final Saver saver = new Saver();
    private final Logger logger = new Logger();
    
    private final PassSequence passSequence;
    private final Alphabet alphabet;
    private final Path saveProgress;
    private final int saveProgressTime;
    private final Path log;
    private final Map<String, String> rawOptions;
    private final Path path;
    
    private volatile String password = null;
    
    private int exitCode;
    
    
    private PassCrackerCLI(
            int minLength, int maxLength,
            String sequenceType, String alphabetType,
            Path saveProgress, int saveProgressTime,
            Path log,
            Map<String, String> options,
            Path path )
            throws IllegalArgumentException {
        
        this.path = path;
        
        this.alphabet = AlphabetFactory.create( alphabetType, options );
        this.passSequence = PassSequenceFactory.create( minLength, maxLength, sequenceType, alphabet, options );
        
        this.saveProgress = saveProgress;
        this.saveProgressTime = saveProgressTime;
        this.log = log;
        
        rawOptions = options;
    }

    @Override
    public void run() {
        try {
            console.start();
            
            PassSupplier passSupplier = PassSupplier.create( passSequence );
            Cracker cracker = Cracker.loadCracker( path );
            
            console.println( "Type of file: " + cracker.getMimeType() + ", threads: " + THREADS );
            watcher.start( passSupplier, console );
            
            if( saveProgress != null && saveProgressTime > 0 ) {
                saver.setUpAndStart( console, saveProgress, saveProgressTime, passSupplier );
            }
            
            if( log != null ) {
                logger.setUpAndStart( console, rawOptions, watcher, passSupplier, log );
            }
            
            List<Future<Boolean>> futures = new ArrayList<>( THREADS );
            for( int i = 0; i < THREADS; i++ ) {
                futures.add( EXECUTOR
                        .submit( new PassConsumer( passSupplier, cracker::testPassword, this::savePassword ) ) );
            }
            
            Thread.sleep( 1000 );
            
            List<ExecutionException> executionExceptions = new ArrayList<>();
            
            boolean found = false;
            for( Future<Boolean> future : futures ) {
                boolean result;
                try {
                    result = future.get();
                } catch( ExecutionException ex ) {
                    executionExceptions.add( ex );
                    result = false;
                }
                if( result ) {
                    found = true;
                    break;
                }
            }
            
            watcher.interrupt();
            Thread.sleep( 1000 );
            
            if( found ) {
                console.terminate( "Success. The password is:", password );
                exitCode = EXIT_CODE_SUCCESS;
            } else {
                String message = "Fail to find password.";
                if( executionExceptions.isEmpty() ) {
                    console.terminate( message );
                } else {
                    console.terminate( executionExceptions.stream()
                            .reduce(
                                    new StringBuilder( message )
                                            .append( "\nExceptions while execution:\n" ),
                                    ( sb, ex ) -> sb
                                            .append( ex.getCause().getMessage() )
                                            .append( '\n' ),
                                    ( sb1, sb2 ) -> sb1.append( sb2 ) )
                            .toString() );
                }
                exitCode = EXIT_CODE_FAIL;
            }
        } catch( InterruptedException ex ) {
            console.terminate( "Execution was interrupted." );
            exitCode = EXIT_CODE_INTERRUPTED;
        } catch( IllegalArgumentException ex ) {
            console.terminate( "Illegal argument: " + ex.getMessage() );
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
            saver.interrupt();
            logger.interrupt();
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
            
            if( maxLength < minLength || minLength < 1 ) {
                throw new IllegalArgumentException();
            }
            
            String sequenceType = options.get( "sequenceType" );
            String alphabetType = options.get( "alphabetType" );
            
            if( sequenceType == null || alphabetType == null ) {
                throw new IllegalArgumentException();
            }
            
            Path saveProgress;
            int saveProgressTime;
            try {
                saveProgress = Paths.get( requireNonNull( options.get( "saveProgress" ) ) );
                String timeStr = options.get( "saveProgressTime" );
                saveProgressTime = timeStr == null ? Saver.DEFAULT_WAIT_TIME : Integer.parseInt( timeStr );
            } catch( NullPointerException | IllegalArgumentException ex ) {
                saveProgress = null;
                saveProgressTime = 0;
            }
            
            Path log;
            try {
                log = Paths.get( requireNonNull( options.get( "log" ) ) );
            } catch( NullPointerException | IllegalArgumentException ex ) {
                log = null;
            }
            
            return new PassCrackerCLI(
                    minLength, maxLength,
                    sequenceType, alphabetType,
                    saveProgress, saveProgressTime,
                    log,
                    options,
                    path );
            
        } catch( IllegalArgumentException | NullPointerException ex ) {
            System.err.println( getUsageInfo() );
            System.exit( EXIT_CODE_ILLEGAL_ARGS );
            return null;
        }
    }

    private static String getUsageInfo() {
        return "Usage:\n\n"
                + "java -jar PassCrackerCLI.jar [options...] [path to file]\n\n"
                + "where options are:\n\n"
                + "\t--minLength=[value] -- minimum amount of tokens in the generated passwords\n"
                + "\t--maxLength=[value] -- maximum amount of tokens in the generated passwords\n"
                + "\t--sequenceType=[value] -- type of sequence\n"
                + "\t--alphabetType=[value] -- type of alphabet\n"
                + "\t--saveProgress=[value] (optional) -- path to file for saving last used password\n"
                + "\t(the file will be owerwritten)\n"
                + "\t--saveProgressTime=[value] (optional) -- amount of seconds between savings\n"
                + "\t--log=[value] (optional) -- path to log file\n"
                + "\t(only progress will be saved, runtime errors and result will not)\n\n"
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
