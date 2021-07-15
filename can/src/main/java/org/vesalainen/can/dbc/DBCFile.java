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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.util.LinkedMap;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCFile extends DBCBase
{
    private String version = "";
    private Map<String,Node> nodes = new LinkedMap<>();
    private Map<Integer,MessageClass> messages = new LinkedMap<>();
    private Map<String,List<ValueDescription>> valueTables = new LinkedMap<>();
    private List<MessageTransmitter> messageTransmitters = Collections.EMPTY_LIST;

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
        for (String s : new String[]{"NS_DESC_", "CM_", "BA_DEF_", "BA_DEF_", "BA_", "VAL_", "CAT_DEF_", "CAT_", "FILTER", "BA_DEF_DEF_", "EV_DATA_", "ENVVAR_DATA_", "SGTYPE_", "SGTYPE_VAL_", "BA_DEF_SGTYPE_", "BA_SGTYPE_", "SIG_TYPE_REF_", "VAL_TABLE_", "SIG_GROUP_", "SIG_VALTYPE_", "SIGTYPE_VALTYPE_", "BO_TX_BU_", "BA_DEF_REL_", "BA_REL_", "BA_DEF_DEF_REL_", "BU_SG_REL_", "BU_EV_REL_", "BU_BO_REL_", "SG_MUL_VAL_"})
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
            out.println(" ;");
        });
        out.println();
        messages.forEach((i, m)->m.print(out));
        out.println();
        if (!messageTransmitters.isEmpty())
        {
            messageTransmitters.forEach((t)->
            {
                out.format("BO_TX_BU_ %d %s", 
                        t.getId(),
                        t.getTransmitters().stream().collect(Collectors.joining(",", " : ", ";\n"))
                );
            });
            out.println();
        }
        if (!comment.isEmpty())
        {
            out.format("CM_ \"%s\" ;\n", comment);
            out.println();
        }
        nodes.values().forEach((n)->
        {
            if (!n.getComment().isEmpty())
            {
                out.format("CM_ BU_ %s \"%s\" ;\n", n.getName(), n.getComment());
            }
        });
        out.println();
        messages.forEach((i, m)->
        {
            if (!m.getComment().isEmpty())
            {
                out.format("CM_ BO_ %s \"%s\" ;\n", m.getId(), m.getComment());
            }
            m.forEach((s)->
            {
                if (!s.getComment().isEmpty())
                {
                    out.format("CM_ SG_ %d %s \"%s\" ;\n", m.getId(), s.getName(), s.getComment());
                }
            });
        });
        out.println();
        if (!attributes.isEmpty())
        {
            attributes.forEach((n, a)->a.printDefinition(out));
            attributes.forEach((n, a)->a.printDefault(out));
            attributes.forEach((n, a)->a.printValue(out));
            out.println();
        }
        messages.forEach((i, m)->
        {
            m.forEach((SignalClass s)->
            {
                List<ValueDescription> valueDescriptions = s.getValueDescriptions();
                if (valueDescriptions != null)
                {
                    out.format("VAL_ %d %s ", m.getId(), s.getName());
                    valueDescriptions.forEach((vd)->out.format(" %d \"%s\"", vd.getValue(), vd.getDescription()));
                    out.println(";");
                }
            });
        });
    }
    public void setVersion(String version)
    {
        this.version = version;
    }

    public void addNode(String name)
    {
        Node node = new Node(name);
        nodes.put(name, node);
    }

    public void addMessage(MessageClass message)
    {
        messages.put(message.getId(), message);
        message.forEach((SignalClass s)->
        {
            List<ValueDescription> l = valueTables.get(s.getName());
            if (l != null)
            {
                s.setValueDescription(l);
            }
        });
    }

    public void setNodeComment(String name, String comment)
    {
        Node node = nodes.get(name);
        node.setComment(comment);
    }

    public void setMessageComment(int id, String comment)
    {
        MessageClass message = messages.get(id);
        message.setComment(comment);
    }

    public void setSignalComment(int id, String signal, String comment)
    {
        MessageClass message = messages.get(id);
        message.setSignalComment(signal, comment);
    }

    public void addAttribute(String name, AttributeValueType type)
    {
        if (attributes.put(name, new Attribute(name, type)) != null)
        {
            throw new IllegalArgumentException("duplicate attribute "+name);
        };
    }

    public void addAttribute(ObjectType objectType, String name, AttributeValueType type)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setAttributeDefault(String name, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setDefault(value);
    }

    public void setAttributeValue(String name, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
    }

    public void setNodeAttributeValue(String name, String nodeName, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
        Node node = nodes.get(nodeName);
        node.setAttribute(attribute);
        
    }

    public void setMessageAttributeValue(String name, int id, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
        MessageClass message = messages.get(id);
        message.setAttribute(attribute);
    }

    public void setSignalAttributeValue(String name, int id, String signalName, Object value)
    {
        Attribute attribute = attributes.get(name);
        attribute.setValue(value);
        MessageClass message = messages.get(id);
        message.setSignalAttribute(signalName, attribute);
    }

    public void addValueTable(String name, List<ValueDescription> valueDescriptions)
    {
        valueTables.put(name, valueDescriptions);
    }

    public void forEach(Consumer<? super MessageClass> action)
    {
        messages.values().forEach(action);
    }

    public void addValueDescriptions(List<ValueDescriptions> valDesc)
    {
        for (ValueDescriptions vd : valDesc)
        {
            addValueDescriptions(vd);
        }
    }
    public void addValueDescriptions(ValueDescriptions vd)
    {
        String name = vd.getName();
        List<ValueDescription> valueDescription = vd.getValDesc();
        int id = vd.getId();
        if (id != 0)
        {
            MessageClass message = messages.get(id);
            message.setSignalValueDescription(name, valueDescription);
        }
        else
        {
            valueTables.put(name, valueDescription);
        }
    }

    public void setMessageTransmitters(List<MessageTransmitter> list)
    {
        this.messageTransmitters = list;
    }

    @Override
    public int hashCode()
    {
        int hash = 5;
        hash = 71 * hash + Objects.hashCode(this.version);
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        final DBCFile other = (DBCFile) obj;
        if (!Objects.equals(this.version, other.version))
        {
            return false;
        }
        if (!Objects.equals(this.nodes, other.nodes))
        {
            return false;
        }
        if (!Objects.equals(this.messages, other.messages))
        {
            return false;
        }
        if (!Objects.equals(this.valueTables, other.valueTables))
        {
            return false;
        }
        if (!Objects.equals(this.messageTransmitters, other.messageTransmitters))
        {
            return false;
        }
        return true;
    }

}
