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

package by.iddqd.passcracker.crackers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * ClassLoader for Cracker implementations.
 * 
 * @author Sergey Protasevich
 */
class CrackerClassLoader extends ClassLoader {

    private static final CrackerClassLoader instance = new CrackerClassLoader();
    
    private CrackerClassLoader() {}
    
    /**
     * Get cracker for given file.
     * 
     * @param path path to file
     * @return the Cracker for given file
     * @throws IllegalArgumentException if the file is not a regular file or is not readable
     * @throws ClassNotFoundException if there is no suitable Cracker
     * for the MIME type of the given file
     * @throws IllegalStateException if this Cracker cannot operate
     * under current environment
     * @throws IOException if an I/O error occurs
     */
    Cracker loadCracker( Path path )
            throws IllegalArgumentException, ClassNotFoundException, IllegalStateException, IOException {
        
        if( !Files.isRegularFile( path ) || !Files.isReadable( path ) ) {
            throw new IllegalArgumentException( path.toString()
                    + " is not a regular file or is not readable." );
        }
        
        String mimeType = Files.probeContentType( path );
        
        try {
            
            Constructor<? extends Cracker> constructor =
                    ((Class<? extends Cracker>)findClass( mimeType )).getDeclaredConstructor( Path.class );
            
            constructor.setAccessible( true );
            Cracker cracker = constructor.newInstance( path );
            cracker.testEnvironment();
            
            return cracker;
            
        } catch( NoSuchMethodException
                | SecurityException
                | InstantiationException
                | IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException ex ) {
            throw new RuntimeException(
                    "Cracker implementation for MIME type " + mimeType
                    + " has illegal declaration of constructor."
                    + " Check the documentation of class Cracker"
                    + " to properly declare the constructor.", ex );
        }
    }
    
    @Override
    protected Class<?> findClass( String mimeType ) throws ClassNotFoundException {
        try {
            
            String packageName = getClass().getPackage().getName();
            List<Class<?>> classes = new ArrayList<>();
            
            File file = new File( getClass()
                    .getProtectionDomain().getCodeSource().getLocation().getPath() );
            
            if( file.isFile() ) { // run with JAR file
                try( JarFile jar = new JarFile( file ) ) {
                    
                    String packageUrl = packageName.replace( '.', '/' );
                    
                    Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
                    while( entries.hasMoreElements() ) {
                        
                        String entryName = entries.nextElement().getName();
                        
                        if( entryName.startsWith( packageUrl ) && entryName.endsWith( ".class" ) ) {
                            classes.add( Class.forName(
                                    packageName + "." + entryName.substring(
                                            entryName.lastIndexOf( '/' ) + 1,
                                            entryName.lastIndexOf( '.' ) ) ) );
                        }
                    }
                }
            } else { // run with IDE

                BufferedReader br = new BufferedReader( new InputStreamReader( (InputStream)getClass()
                        .getResource( '/' + packageName.replace( '.', '/' ) + '/' ).getContent()));

                String line;
                while( ( line = br.readLine() ) != null ) {
                    if( line.endsWith( ".class" ) ) {
                        classes.add( Class.forName(
                                packageName + "." + line.substring( 0, line.lastIndexOf( '.' ) ) ) );
                    }
                }
            }
            
            for( Class<?> aClass : classes ) {
                
                MIMEtype[] mimeTypeAnnotations;
                
                try {
                    mimeTypeAnnotations = aClass
                            .asSubclass( Cracker.class )
                            .getAnnotationsByType( MIMEtype.class );
                } catch( ClassCastException ex ) {
                    continue;
                }
                
                if( mimeTypeAnnotations == null || mimeTypeAnnotations.length == 0 ) {
                    continue;
                }
                
                for( MIMEtype mimeTypeAnnotation : mimeTypeAnnotations ) {
                    if( mimeType.equals( mimeTypeAnnotation.value() ) ) {
                        return aClass;
                    }
                }
            }
            
            throw new ClassNotFoundException( "No class found for MIME type " + mimeType );
            
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }
    
    /**
     * Get an instance of CrackerClassLoader.
     * 
     * @return the instance of CrackerClassLoader
     */
    static CrackerClassLoader get() {
        return instance;
    }
}
