/**
 * PassSequence Copyright (C) 2014 Sergey Protasevich
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package by.iddqd.passcracker.functions;

import java.math.BigInteger;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.ZERO;

/**
 * Mathematical functions.
 *
 * @author Sergey Protasevich
 */
public final class Maths {

    private Maths() {}

    /**
     * Calculate factorial of BigInteger.
     * 
     * @param value a value
     * @return factorial of the value.
     */
    public static BigInteger factorial( BigInteger value ) {
        BigInteger result = ONE;

        while( !ZERO.equals( value ) ) {
            result = result.multiply( value );
            value = value.subtract( ONE );
        }

        return result;
    }
}
