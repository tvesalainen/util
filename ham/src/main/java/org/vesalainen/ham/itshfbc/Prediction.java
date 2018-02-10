/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.itshfbc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Prediction
{
    private File itshfbc = new File("C:\\itshfbc");
    private String module = "voacapw";
    private List<CommandLine> input = new ArrayList<>();
    
    private File exePath()
    {
        return new File(itshfbc, "bin_win/"+module);
    }
    private File runPath()
    {
        return new File(itshfbc, "run");
    }
}
