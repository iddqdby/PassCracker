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

import by.iddqd.passcracker.cracker.Cracker;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.SortedMap;
import java.util.TreeMap;
import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.SevenZipNativeInitializationException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

/**
 * Abstract cracker that uses native libraries through JNI.
 * 
 * @author Sergey Protasevich
 */
public abstract class NativeCracker extends Cracker<NativeCracker> {

    private boolean initialized;
    private RandomAccessFile randomAccessFile;
    private ISevenZipInArchive archive;
    private int[] smallestFileId;

    @Override
    protected void doInit( Path path ) throws InterruptedException {
        try {
            SevenZip.initSevenZipFromPlatformJAR();
            initialized = true;
        } catch( SevenZipNativeInitializationException ex ) {
            initialized = false;
        }
    }

    @Override
    protected boolean doTestEnvironment( Path path ) throws InterruptedException {
        return initialized;
    }

    @Override
    protected void doPrepare( Path path ) throws InterruptedException {
        
        randomAccessFile = getRandomAccessFileFromPath();
        archive = getArchiveFromRandomAccessFile( randomAccessFile );
            
        try {
            int count = archive.getNumberOfItems();
            SortedMap<Long, Integer> sizeToIdMap = new TreeMap<>();
            
            for( int i = 0; i < count; i++ ) {
                
                if( (Boolean)archive.getProperty( i, PropID.IS_FOLDER ) ) {
                    continue;
                }
                
                Long packedSize = (Long)archive.getProperty( i, PropID.PACKED_SIZE );
                
                if( packedSize == null || packedSize <= 0 ) {
                    continue;
                }
                
                sizeToIdMap.put( packedSize, i );
            }
            
            if( sizeToIdMap.isEmpty() ) {
                smallestFileId = null;
            } else {
                smallestFileId = new int[] { sizeToIdMap.get( sizeToIdMap.firstKey() ) };
            }
        } catch( SevenZipException ex ) {
            smallestFileId = null;
        }
    }

    @Override
    protected boolean doTestPassword( Path path, String password ) throws InterruptedException {
        
        Result result = new Result();
        IArchiveExtractCallback callback = new TestCallback( password, result );
        
        try {
            archive.extract( smallestFileId, true, callback );
        } catch( SevenZipException ex ) {
            throw new RuntimeException( ex );
        }
        
        switch( result.get() ) {
            case FAIL:
                return false;
            case OK:
                return true;
            default:
                throw new RuntimeException( "Unexpected exception" );
        }
    }

    private RandomAccessFile getRandomAccessFileFromPath() {
        try {
            return new RandomAccessFile( getPath().toFile(), "r" );
        } catch( FileNotFoundException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private ISevenZipInArchive getArchiveFromRandomAccessFile( RandomAccessFile randomAccessFile ) {
        try {
            return SevenZip
                    .openInArchive( getArchiveFormat(), new RandomAccessFileInStream( randomAccessFile ) );
        } catch( SevenZipException ex ) {
            throw new RuntimeException( ex );
        }
    }
    
    @Override
    protected void cloneThis( NativeCracker clone ) {
        clone.initialized = initialized;
        clone.randomAccessFile = getRandomAccessFileFromPath();
        clone.archive = getArchiveFromRandomAccessFile( clone.randomAccessFile );
        clone.smallestFileId = smallestFileId;
    }
    
    protected abstract ArchiveFormat getArchiveFormat();

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            archive.close();
        } finally {
            randomAccessFile.close();
        }
    }
}
