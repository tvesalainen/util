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

package org.vesalainen.navi;

import java.io.IOException;
import java.net.URL;
import org.vesalainen.parser.GenClassFactory;
import static org.vesalainen.parser.ParserFeature.UseDirectBuffer;
import org.vesalainen.parser.annotation.GenClassname;
import org.vesalainen.parser.annotation.GrammarDef;
import org.vesalainen.parser.annotation.ParseMethod;
import org.vesalainen.parser.annotation.ParserContext;
import org.vesalainen.parser.annotation.Rule;
import org.vesalainen.parser.annotation.Terminal;

/**
 *
 * @author Timo Vesalainen
 */
@GenClassname("org.vesalainen.navi.CoordinateParserImpl")
@GrammarDef()
public abstract class CoordinateParser
{
    public static CoordinateParser newInstance()
    {
        return (CoordinateParser) GenClassFactory.loadGenInstance(CoordinateParser.class);
    }
    @ParseMethod(start = "coords", size = 1024, charSet = "US-ASCII",
            features={UseDirectBuffer}, whiteSpace="whiteSpace")
    protected abstract void parse(URL url, @ParserContext("aw") AnchorWatch aw) throws IOException;
    
    @Rule("coord+")
    protected void coords()
    {
    }
    @Rule("decimal decimal")
    protected void coord(double longitude, double latitude, @ParserContext("aw") AnchorWatch aw)
    {
        aw.update(longitude, latitude);
    }
    @Terminal(expression="[\\-]?[0-9]+\\.[0-9]*")
    protected abstract double decimal(double d);

    @Terminal(expression="[ \\,\r\n]+")
    protected abstract void whiteSpace();
    
}
