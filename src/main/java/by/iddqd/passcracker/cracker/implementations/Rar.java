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
import java.nio.file.Path;

/**
 * Cracker for RAR archives.
 * 
 * @author Sergey Protasevich
 */
@MIMEtype( "application/x-rar" )
class Rar extends Cracker {

    @Override
    protected void doInit( Path path ) {
        // TODO
    }

    @Override
    protected boolean doTestEnvironment( Path path ) {
        // TODO
        return true;
    }

    @Override
    protected boolean doTestPassword( Path path, String password ) {
        
        // TODO
        
        return "rh345".equals( password );
    }
}
