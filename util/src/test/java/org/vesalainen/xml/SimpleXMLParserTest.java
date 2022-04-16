/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.xml;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.xml.SimpleXMLParser.Element;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleXMLParserTest
{

    public SimpleXMLParserTest()
    {
    }

    @Test
    public void test1() throws IOException
    {
        SimpleXMLParser parser = new SimpleXMLParser("<CANBeacon name=\"HeartOfGold\" type=\"SocketCAN\" description=\"A human readable description\">\n"
                + "    <URL>can://127.0.0.1:29536</URL>\n"
                + "    <Bus name=\"vcan0\"/>\n"
                + "    <Bus name=\"vcan1\"/>\n"
                + "</CANBeacon>");
        SimpleXMLParser.Element root = parser.getRoot();
        assertEquals("CANBeacon", root.getTag());
        assertEquals(4, root.stream().count());
        assertEquals(2, root.stream((e)->e.getTag().equals("Bus")).count());
        assertEquals(3, root.getAttributes().size());
        assertEquals("HeartOfGold", root.getAttributeValue("name"));
        assertEquals("SocketCAN", root.getAttributeValue("type"));
        assertEquals("A human readable description", root.getAttributeValue("description"));
        List<Element> childs = root.getChilds();
        assertEquals(3, childs.size());
        Element url = childs.get(0);
        Element vcan0 = childs.get(1);
        Element vcan1 = childs.get(2);
        assertEquals("URL", url.getTag());
        assertEquals("can://127.0.0.1:29536", url.getText());
        assertEquals("vcan0", vcan0.getAttributeValue("name"));
        assertEquals("vcan1", vcan1.getAttributeValue("name"));
        Element u2 = root.getElement("URL");
        assertEquals("can://127.0.0.1:29536", u2.getText());
        Collection<Element> bs = root.getElements("Bus");
        assertEquals(2, bs.size());
    }
    @Test
    public void testPOM() throws IOException
    {
        SimpleXMLParser parser = new SimpleXMLParser(Paths.get("pom.xml"));
        Element root = parser.getRoot();
        Element a = root.getElement("parent", "artifactId");
        assertEquals("utilities", a.getText());
    }    
}
