/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.dbc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.vesalainen.io.AppendablePrinter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCFile extends DBCBase
{
    private String version = "";
    private Map<String,Node> nodes = new HashMap<>();
    private Map<Integer,MessageClass> messages = new HashMap<>();
    private Map<String,List<ValueDescription>> valueTables = new HashMap<>();

    public void print(Appendable out)
    {
        print(new AppendablePrinter(out));
    }
    void print(AppendablePrinter out)
    {
        out.format("VERSION \"%s\"\n", version);
        out.println();
        out.println();
        out.println("NS_ :");
        for (String s : new String[]{"NS_DESC", "CM", "BA_DEF", "BA_DEF", "BA", "VAL", "CAT_DEF", "CAT", "FILTER", "BA_DEF_DEF", "EV_DATA", "ENVVAR_DATA", "SGTYPE", "SGTYPE_VAL", "BA_DEF_SGTYPE", "BA_SGTYPE", "SIG_TYPE_REF", "VAL_TABLE", "SIG_GROUP", "SIG_VALTYPE", "SIGTYPE_VALTYPE", "BO_TX_BU", "BA_DEF_REL", "BA_REL", "BA_DEF_DEF_REL", "BU_SG_REL", "BU_EV_REL", "BU_BO_REL", "SG_MUL_VAL"})
        {
            out.println("\t"+s);
        }
        out.println();
        out.println("BS_ :");
        out.println();
        out.println(nodes.keySet().stream().collect(Collectors.joining(" ", "BU_: ", "\n")));
        out.println();
        valueTables.forEach((n, l)->
        {
            out.format("VAL_TABLE_ %s", n);
            l.forEach((vd)->out.format(" %d \"%s\"", vd.getValue(), vd.getDescription()));
            out.println();
        });
        out.println();
        messages.forEach((i, m)->m.print(out));
        out.println();
    }
    void setVersion(String version)
    {
        this.version = version;
    }

    void addNode(String name)
    {
        Node node = new Node(name);
        nodes.put(name, node);
    }

    void addMessage(MessageClass message)
    {
        messages.put(message.getId(), message);
    }

    void setNodeComment(String name, String comment)
    {
        Node node = nodes.get(name);
        node.setComment(comment);
    }

    void setMessageComment(int id, String comment)
    {
        MessageClass message = messages.get(id);
        message.setComment(comment);
    }

    void setSignalComment(int id, String signal, String comment)
    {
        MessageClass message = messages.get(id);
        message.setSignalComment(signal, comment);
    }

    void addAttribute(String name, AttributeValueType type)
    {
        if (attributes.put(name, new Attribute(name, type)) != null)
        {
            throw new IllegalArgumentException("duplicate attribute "+name);
        };
    }

    void setAttributeDefault(String name, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setDefault(value);
    }

    void setAttributeValue(String name, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
    }

    void setNodeAttributeValue(String name, String nodeName, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
        Node node = nodes.get(nodeName);
        node.setAttribute(attribute);
        
    }

    void setMessageAttributeValue(String name, int id, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
        MessageClass message = messages.get(id);
        message.setAttribute(attribute);
    }

    void setSignalAttributeValue(String name, int id, String signalName, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
        MessageClass message = messages.get(id);
        message.setSignalAttribute(signalName, attribute);
    }

    void addValueTable(String name, List<ValueDescription> valueDescriptions)
    {
        valueTables.put(name, valueDescriptions);
    }

    public void forEach(Consumer<? super MessageClass> action)
    {
        messages.values().forEach(action);
    }

    void addValueDescriptions(List<ValueDescriptions> valDesc)
    {
        for (ValueDescriptions vd : valDesc)
        {
            int id = vd.getId();
            if (id != 0)
            {
                MessageClass message = messages.get(id);
                message.setSignalValueDescription(vd.getName(), vd.getValDesc());
            }
        }
    }

}
