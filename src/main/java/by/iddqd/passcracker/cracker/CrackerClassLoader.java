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

package by.iddqd.passcracker.cracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    
    private CrackerClassLoader() {
        super( CrackerClassLoader.class.getClassLoader() );
    }

    /**
     * Loads the Cracker subclass for the specified MIME type.
     *
     * @param name the MIME type, or the binary name of the subclass
     * @return the resulting <tt>Class</tt> object
     * @throws ClassNotFoundException if the class was not found
     */
    @Override
    public Class<? extends Cracker> loadClass( String name ) throws ClassNotFoundException {
        return (Class<? extends Cracker>)super.loadClass( name );
    }
    
    @Override
    protected Class<?> findClass( String mimeType ) throws ClassNotFoundException {
        try {
            
            ClassLoader parentClassLoader = getParent();
            List<String> classNames = getClassNames( getClass().getPackage() );
            
            for( String className : classNames ) {
                
                Class<? extends Cracker> aClass;
                MIMEtype[] mimeTypeAnnotations;
                
                try {
                    aClass = parentClassLoader.loadClass( className ).asSubclass( Cracker.class );
                    mimeTypeAnnotations = aClass.getAnnotationsByType( MIMEtype.class );
                } catch( ClassNotFoundException | ClassCastException ex ) {
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
            
            throw new ClassNotFoundException( "No class found for value \"" + mimeType + "\"" );
            
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }
    
    private List<String> getClassNames( Package aPackage ) throws IOException {
        
        String packageName = aPackage.getName();
        List<String> classNames = new ArrayList<>();
        
        File file =
                new File( getClass().getProtectionDomain().getCodeSource().getLocation().getPath() );
        
        if( file.isFile() ) { // run with JAR file
            try( JarFile jarFile = new JarFile( file ) ) {

                String packageUrl = packageName.replace( '.', '/' );

                Enumeration<JarEntry> entries = jarFile.entries(); // gives all entries in jar
                while( entries.hasMoreElements() ) {

                    String entryName = entries.nextElement().getName();

                    if( entryName.startsWith( packageUrl ) && entryName.endsWith( ".class" ) ) {
                        String className = packageName + "." + entryName
                                .substring( entryName.lastIndexOf( '/' ) + 1, entryName.lastIndexOf( '.' ) );
                        classNames.add( className );
                    }
                }
            }
        } else { // run with IDE
            try( BufferedReader br = new BufferedReader( new InputStreamReader( (InputStream)getClass()
                    .getResource( '/' + packageName.replace( '.', '/' ) + '/' ).getContent() ) ) ) {

                String line;
                while( ( line = br.readLine() ) != null ) {
                    if( line.endsWith( ".class" ) ) {
                        String className = packageName + "." + line.substring( 0, line.lastIndexOf( '.' ) );
                        classNames.add( className );
                    }
                }
            }
        }
        
        return classNames;
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
