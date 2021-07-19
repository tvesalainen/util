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
import static java.lang.Integer.max;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.MultiplexerIndicator;
import org.vesalainen.can.dbc.PGNClass;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.dbc.ValueDescription;
import org.vesalainen.can.dbc.ValueDescriptions;
import static org.vesalainen.can.dbc.ValueType.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.text.CamelCase;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.MapList;
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
    private MapList<Integer,PGNClass> msgMap = new HashMapList<>();
    private List<ValueDescriptions> valueDescriptions = new ArrayList<>();
    private Map<SignalClass,Integer> matches = new IdentityHashMap<>();
    
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
                msgMap.add(pgnCls.getId(), pgnCls);
            }
        }
        msgMap.forEach((canId, list)->
        {
            if (list.size() == 1)
            {
                addMessage(list.get(0));
            }
            else
            {
                addMultiplexingMessage(list);
            }
        });
        addValueDescriptions(valueDescriptions);
        addAttribute("BusType", "CAN", "");
        addAttribute("ProtocolType", "NMEA2000", "");
        addAttribute("MessageType", "", "Single");
        addAttribute("SignalType", "", "Integer");
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
        int match = -1;
        
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
                case "Match":
                    match = Integer.parseUnsignedInt(node.getTextContent());
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
                    CollectionHelp.create("Vector__XXX"));
            if (type != null)
            {
                sc.setAttributeValue("SignalType", type);
            }
            if (lookupNode != null)
            {
                List<ValueDescription> valDesc = parseLookup(lookupNode);
                valueDescriptions.add(new ValueDescriptions(canId, sc::getName, valDesc));
            }
            if (match != -1)
            {
                matches.put(sc, match);
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

    private void addMultiplexingMessage(List<PGNClass> list)
    {
        List<SignalClass> signals = new ArrayList<>();
        IntReference match = new IntReference(-1);
        IntReference canId = new IntReference(-1);
        IntReference max = new IntReference(-1);
        StringBuilder desc = new StringBuilder();
        StringBuilder name = new StringBuilder();
        StringBuilder type = new StringBuilder();
        list.forEach((m)->
        {
            match.setValue(-1);
            canId.setValue(m.getId());
            if (name.length() == 0)
            {
                name.append(m.getName());
            }
            if (type.length() == 0)
            {
                type.append(m.getType());
            }
            if (desc.length() > 0)
            {
                desc.append('\n');
            }
            desc.append(m.getComment());
            max.setValue(max(max.getValue(), m.getSize()));
            m.forEach((s)->
            {
                if (match.getValue() == -1)
                {
                    Integer mtc = matches.get(s);
                    match.setValue(mtc.intValue());
                    if (signals.isEmpty())
                    {
                        s.setMultiplexerIndicator(new MultiplexerIndicator());
                        signals.add(s);
                    }
                }
                else
                {
                    s.setName("M"+match.getValue()+"_"+s.getName());
                    s.setMultiplexerIndicator(new MultiplexerIndicator(match.getValue()));
                    signals.add(s);
                }
            });
        });
        PGNClass pgnCls = new PGNClass(canId.getValue(), name.toString(), max.getValue(), type.toString(), desc.toString(), signals);
        addMessage(pgnCls);
    }

}
