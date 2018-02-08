/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.hffax;

import java.net.URL;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class HFFaxT
{
    
    public HFFaxT()
    {
    }

    @Test
    public void test()
    {
        URL url = HFFaxT.class.getResource("/hffax2.wav");
        HFFax.main("-fd","c:\\temp" ,"-info", "true", "-u", url.toString());
        //HFFax.main("-info", "true", "-freq", "12789.9","-fd","c:\\temp" ,"-l", "qqqPort Microphone (5- USB PnP Sound");
        //HFFax.main("-info", "true","-fd","c:\\temp" ,"-l", "qqqPort Microphone (5- USB PnP Sound");

    }
    
}
