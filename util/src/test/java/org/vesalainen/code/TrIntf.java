/*
 * Copyright (C) 2014 Timo Vesalainen
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.vesalainen.code;

import java.io.Writer;
import org.vesalainen.util.Transactional;

/**
 *
 * @author Timo Vesalainen
 */
public interface TrIntf extends Transactional
{
    void setZ(boolean x);
    void setB(byte x);
    void setC(char x);
    void setS(short x);
    void setI(int x);
    void setJ(long x);
    void setF(float x);
    void setD(double x);
    void setWriter(Writer writer);
    void setString(String s);
}
