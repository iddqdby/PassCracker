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

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.ICryptoGetTextPassword;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.SevenZipException;

import static net.sf.sevenzipjbinding.ExtractAskMode.TEST;

/**
 * Callback for testing files from an archive.
 * 
 * @author Sergey Protasevich
 */
class TestCallback implements IArchiveExtractCallback, ICryptoGetTextPassword {

    private final String password;
    private final Result result;

    
    TestCallback( String password, Result result ) {
        this.password = password;
        this.result = result;
    }
    
    @Override
    public ISequentialOutStream getStream( int index, ExtractAskMode extractAskMode ) throws SevenZipException {
        if( !TEST.equals( extractAskMode ) ) {
            throw new SevenZipException( "This Callback is written only for testing files." );
        }
        return null;
    }

    @Override
    public void prepareOperation( ExtractAskMode extractAskMode ) throws SevenZipException {}

    @Override
    public void setOperationResult( ExtractOperationResult extractOperationResult ) throws SevenZipException {
        switch( extractOperationResult ) {
            case OK:
                result.setOk();
                break;
            case CRCERROR:
                result.setFail();
                break;
            default:
                result.setError();
        }
    }

    @Override
    public void setTotal( long total ) throws SevenZipException {}

    @Override
    public void setCompleted( long completeValue ) throws SevenZipException {}

    @Override
    public String cryptoGetTextPassword() throws SevenZipException {
        return password;
    }
}
