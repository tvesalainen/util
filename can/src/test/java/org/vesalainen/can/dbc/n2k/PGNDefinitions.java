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
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import static org.vesalainen.can.SignalType.*;
import org.vesalainen.can.dbc.DBCFile;
import org.vesalainen.can.dbc.MultiplexerIndicator;
import org.vesalainen.can.dbc.SignalClass;
import org.vesalainen.can.dbc.ValueDescription;
import org.vesalainen.can.dbc.ValueDescriptions;
import static org.vesalainen.can.dbc.ValueType.*;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.text.CamelCase;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.HashMapList;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.IntReference;
import org.vesalainen.util.MapList;
import org.vesalainen.util.MapSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import static org.vesalainen.can.SignalType.ASCIIZ;
import org.vesalainen.can.dbc.MessageClass;
import org.vesalainen.can.dbc.ValueType;
import static org.vesalainen.math.UnitType.*;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PGNDefinitions extends DBCFile
{
    private final static String RECEIVER = "MFD";
    private MapList<Integer,MessageClass> msgMap = new HashMapList<>();
    private List<ValueDescriptions> valueDescriptions = new ArrayList<>();
    private Map<SignalClass,Integer> matches = new IdentityHashMap<>();
    
    public PGNDefinitions(Path path) throws ParserConfigurationException, SAXException, IOException
    {
        setComment("This DBC is based on https://github.com/canboat/canboat");
        addNode(RECEIVER);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc = builder.parse(path.toFile());
        NodeList pgns = doc.getElementsByTagName("PGNInfo");
        int length = pgns.getLength();
        for (int nn = 0;nn<length;nn++)
        {
            Node item = pgns.item(nn);
            MessageClass pgnCls = parsePGN(item);
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
        optimizeValueDescriptions();
        addValueDescriptions(valueDescriptions);
        addAttribute("BusType", "CAN", "");
        addAttribute("ProtocolType", "N2K", "");
        addAttribute("MessageType", "", "Single");
        addAttribute("SignalType", "", "Integer");
    }

    private void optimizeValueDescriptions()
    {
        MapSet<String,List<ValueDescription>> ms = new HashMapSet<>();
        valueDescriptions.forEach((vds)->ms.add(vds.getName(), vds.getValDesc()));
        ms.forEach((String n,Set<List<ValueDescription>> sl)->
        {
            if (sl.size() == 1)
            {
                valueTables.put(n, sl.iterator().next());
            }
        });
        Iterator<ValueDescriptions> it = valueDescriptions.iterator();
        while (it.hasNext())
        {
            ValueDescriptions vds = it.next();
            String name = vds.getName();
            if (valueTables.containsKey(name))
            {
                it.remove();
            }
        }
    }
    private MessageClass parsePGN(Node item)
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
            String transmitter = CamelCase.delimited(pgnInfo.getCategory(), "_");
            MessageClass msg = new MessageClass(this, canId, CamelCase.delimited(pgnInfo.getName(), "_"), size, transmitter, signals);
            msg.setAttributeValue("MessageType", type.toString());
            msg.setComment(pgnInfo.getDescription());
            addNode(transmitter);
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
            SignalClass sc = createSignal(
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
                    CollectionHelp.create(RECEIVER));
            if (type != null)
            {
                setSignalType(sc, type);
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

    private SignalClass createSignal(String name, MultiplexerIndicator multiplexerIndicator, int startBit, int size, ByteOrder byteOrder, ValueType valueType, double factor, double offset, double min, double max, String unit, List<String> receivers)
    {
        switch (unit)
        {
            case "Rad":
                factor = RADIAN.convertTo(factor, DEGREE);
                unit = "deg";
                break;
        }
        SignalClass sc = new SignalClass(
                this,
                name, 
                multiplexerIndicator, 
                startBit, 
                size, 
                byteOrder, 
                valueType, 
                factor, 
                offset, 
                min, 
                max, 
                unit, 
                receivers);
            return sc;
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

    private void addMultiplexingMessage(List<MessageClass> list)
    {
        MessageClass first = list.get(0);
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
                type.append(m.getAttributeValue("MessageType"));
            }
            if (desc.length() == 0)
            {
                desc.append(m.getComment());
            }
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
        MessageClass pgnCls = new MessageClass(this, canId.getValue(), name.toString(), max.getValue(), first.getTransmitter(), signals);
        pgnCls.setAttributeValue("MessageType", type.toString());
        pgnCls.setComment(desc.toString());
        addMessage(pgnCls);
    }

    private void setSignalType(SignalClass sc, String type)
    {
        String signalType;
        switch (type)
        {
            case "Integer":
                signalType = INT.toString();
                break;
            case "Latitude":
            case "Longitude":
            case "Time":
            case "Temperature":
            case "Temperature (hires)":
            case "Pressure":
            case "Pressure (hires)":
                signalType = DOUBLE.toString();;
                break;
            case "Lookup table":
                signalType = LOOKUP.toString();;
                break;
            case "Binary data":
            case "Manufacturer code":
                signalType = BINARY.toString();;
                break;
            case "Date":
                signalType = INT.toString();;
                break;
            case "ASCII text":
                signalType = ASCIIZ.toString();
                break;
            case "ASCII string starting with length byte and terminated by zero byte":
                signalType = STRINGLZ.toString();
                break;
            case "ASCII or UNICODE string starting with length and control byte":
                signalType = STRINGLAU.toString();
                break;
            case "String with start/stop byte":
                signalType = STRING.toString();
                break;
            case "Bitfield":
                signalType = BITFIELD.toString();
                break;
            case "IEEE Float":
                signalType = IEEE_FLOAT.toString();
                break;
            default:
                throw new UnsupportedOperationException(type+" not supported");
        }
        sc.setAttributeValue("SignalType", signalType);
    }

}
