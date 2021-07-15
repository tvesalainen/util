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
package org.vesalainen.can.dbc.n2k;

import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.junit.Test;
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.PGNClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.dbc.ValueDescription;
import org.vesalainen.can.dbc.ValueDescriptions;
import static org.vesalainen.can.dbc.ValueType.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.text.CamelCase;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PGNDefinitions extends DBCFile
{
    private List<ValueDescriptions> valueDescriptions = new ArrayList<>();
    
    public PGNDefinitions(Path path) throws ParserConfigurationException, SAXException, IOException
    {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(path.toFile());
        NodeList pgns = doc.getElementsByTagName("PGNInfo");
        int length = pgns.getLength();
        for (int nn = 0;nn<length;nn++)
        {
            Node item = pgns.item(nn);
            PGNClass pgnCls = parsePGN(item);
            if (pgnCls != null)
            {
                addMessage(pgnCls);
            }
        }
        addValueDescriptions(valueDescriptions);
    }

    private PGNClass parsePGN(Node item)
    {
        int pgn = 0;
        String id = null;
        String description = null;
        String type = null;
        boolean complete;
        int size = 0;
        int repeatingFields;
        Node fields = null;
        NodeList childNodes = item.getChildNodes();
        int length = childNodes.getLength();
        for (int nn = 0;nn<length;nn++)
        {
            Node node = childNodes.item(nn);
            switch (node.getNodeName())
            {
                case "PGN":
                pgn = Integer.parseUnsignedInt(node.getTextContent());
                break;
                case "Id":
                id = node.getTextContent();
                break;
                case "description":
                description = node.getTextContent();
                break;
                case "Type":
                type = node.getTextContent();
                break;
                case "Complete":
                complete = Boolean.parseBoolean(node.getTextContent());
                break;
                case "Length":
                size = Integer.parseUnsignedInt(node.getTextContent());
                break;
                case "RepeatingFields":
                repeatingFields = Integer.parseUnsignedInt(node.getTextContent());
                break;
                case "Fields":
                fields = node;
                break;
            }
        }
        int canId = PGN.canId(pgn);
        N2KData.PGNInfo pgnInfo = N2KData.N2K.getPGNInfo(pgn);
        if (fields != null && pgnInfo != null)
        {
            List<SignalClass> signals = new ArrayList<>();
            NodeList fieldNodes = fields.getChildNodes();
            int fldLen = fieldNodes.getLength();
            for (int ff=0;ff<fldLen;ff++)
            {
                Node fieldNode = fieldNodes.item(ff);
                SignalClass sc = parseField(canId, fieldNode);
                if (sc != null)
                {
                    signals.add(sc);
                }
            }
            PGNClass msg = new PGNClass(canId, CamelCase.delimited(pgnInfo.getName(), "_"), size, type, pgnInfo.getDescription(), signals);
            
            //msg.setAttribute("MessageCategory", pgnInfo.getCategory());
            return msg;
        }
        return null;
  }

    private SignalClass parseField(int canId, Node fieldNode)
    {
        String name = null;
        int length = 0;
        int offset = 0;
        String type = null;
        String unit = "";
        boolean signed = false;
        double resolution = 1.0;
        Node lookupNode = null;
        
        NodeList childNodes = fieldNode.getChildNodes();
        int len = childNodes.getLength();
        for (int nn = 0;nn<len;nn++)
        {
            Node node = childNodes.item(nn);
            switch (node.getNodeName())
            {
                case "Name":
                    name = node.getTextContent();
                    break;
                case "BitLength":
                    length = Integer.parseUnsignedInt(node.getTextContent());
                    break;
                case "BitOffset":
                    offset = Integer.parseUnsignedInt(node.getTextContent());
                    break;
                case "Type":
                    type = node.getTextContent();
                    break;
                case "Units":
                    unit = node.getTextContent();
                    break;
                case "Signed":
                    signed = Boolean.parseBoolean(node.getTextContent());
                    break;
                case "Resolution":
                    resolution = Double.parseDouble(node.getTextContent());
                    break;
                case "EnumValues":
                    lookupNode = node;
                    break;

            }
        }
        if (name != null && len > 0)
        {
            String nam = CamelCase.delimited(name, "_");
            SignalClass sc = new SignalClass(
                    nam, 
                    null, 
                    offset, 
                    length, 
                    ByteOrder.LITTLE_ENDIAN, 
                    signed ? SIGNED : UNSIGNED, 
                    resolution, 
                    Double.valueOf(0), 
                    Double.valueOf(0), 
                    Double.valueOf(0), 
                    unit, 
                    Collections.EMPTY_LIST);
            sc.setAttribute("SignalType", type);
            if (lookupNode != null)
            {
                List<ValueDescription> valDesc = parseLookup(lookupNode);
                valueDescriptions.add(new ValueDescriptions(canId, nam, valDesc));
            }
            return sc;
        }
        return null;
    }

    private List<ValueDescription> parseLookup(Node lookupNode)
    {
        List<ValueDescription> list = new ArrayList<>();
        NodeList childNodes = lookupNode.getChildNodes();
        int len = childNodes.getLength();
        for (int nn = 0;nn<len;nn++)
        {
            Node node = childNodes.item(nn);
            if ("EnumPair".equals(node.getNodeName()))
            {
                NamedNodeMap attributes = node.getAttributes();
                Node value = attributes.getNamedItem("Value");
                Node name = attributes.getNamedItem("Name");
                int v = Integer.parseInt(value.getTextContent());
                String n = name.getTextContent();
                list.add(new ValueDescription(v, n));
            }
        }
        return list;
    }

}
