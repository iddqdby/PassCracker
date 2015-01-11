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

import by.iddqd.passcracker.cracker.MIMEtype;
import by.iddqd.passcracker.cracker.implementations.nativelibs.NativeCracker;
import net.sf.sevenzipjbinding.ArchiveFormat;

/**
 * Cracker for 7z archives.
 * 
 * @author Sergey Protasevich
 */
@MIMEtype( "application/x-7z-compressed" )
class Cracker7z extends NativeCracker {

    @Override
    protected ArchiveFormat getArchiveFormat() {
        return ArchiveFormat.SEVEN_ZIP;
    }
}
