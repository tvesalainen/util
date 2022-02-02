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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.vesalainen.can.j1939.PGN;
import org.vesalainen.io.AppendablePrinter;
import org.vesalainen.util.IntRange;
import org.vesalainen.util.LinkedMap;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCFile extends DBCBase
{
    protected String version = "";
    protected Map<String,Node> nodes = new LinkedMap<>();
    protected Map<Integer,MessageClass> messages = new LinkedMap<>();
    protected Map<String,List<ValueDescription>> valueTables = new LinkedMap<>();
    protected List<MessageTransmitter> messageTransmitters = Collections.EMPTY_LIST;
    protected Map<String, Attribute> attributes = new HashMap<>();

    public DBCFile()
    {
        super(null);
        dbcFile = this;
    }

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
        Comparator<? super Map.Entry<String, List<ValueDescription>>> comparator = (e1, e2)->e1.getKey().compareTo(e2.getKey());
        valueTables.entrySet().stream().sorted(comparator).forEach((e)->
        {
            out.format("VAL_TABLE_ %s", e.getKey());
            e.getValue().forEach((vd)->out.format(" %d \"%s\"", vd.getValue(), vd.getDescription()));
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
                        Integer.toUnsignedLong(t.getId()),
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
                out.format("CM_ BO_ %s \"%s\" ;\n", m.getPrintId(), m.getComment());
            }
            m.forEach((s)->
            {
                if (!s.getComment().isEmpty())
                {
                    out.format("CM_ SG_ %d %s \"%s\" ;\n", m.getPrintId(), s.getName(), s.getComment());
                }
            });
        });
        out.println();
        if (!attributes.isEmpty())
        {
            attributes.forEach((n, a)->a.printDefinition(out));
            attributes.forEach((n, a)->a.printDefault(out));
            attributeValues.forEach((n,v)->
            {
                Attribute at = attributes.get(n);
                out.format("BA_ \"%s\" %s;\n", n, at.getValue(v));
            });
            nodes.forEach((s,nn)->
            {
                nn.getAttributeValues().forEach((n,v)->
                {
                    Attribute at = attributes.get(n);
                    out.format("BA_ \"%s\" BU_ %s %s;\n", n, nn.getName(), at.getValue(v));
                });
            });
            messages.forEach((i, m)->
            {
                m.getAttributeValues().forEach((n,v)->
                {
                    Attribute at = attributes.get(n);
                    out.format("BA_ \"%s\" BO_ %d %s;\n", n, m.getPrintId(), at.getValue(v));
                });
                m.forEach((SignalClass s)->
                {
                    s.getAttributeValues().forEach((n,v)->
                    {
                        Attribute at = attributes.get(n);
                        out.format("BA_ \"%s\" SG_ %d %s %s;\n", n, m.getPrintId(), s.getName(), at.getValue(v));
                    });
                });
            });
            out.println();
        }
        messages.forEach((i, m)->
        {
            m.forEach((SignalClass s)->
            {
                List<ValueDescription> valueDescriptions = s.getValueDescriptions();
                if (valueDescriptions != null)
                {
                    out.format("VAL_ %d %s ", m.getPrintId(), s.getName());
                    valueDescriptions.forEach((vd)->out.format(" %d \"%s\"", vd.getValue(), vd.getDescription()));
                    out.println(";");
                }
            });
        });
        messages.forEach((i, m)->
        {
            m.forEach((SignalClass s)->
            {
                MultiplexerIndicator multiplexerIndicator = s.getMultiplexerIndicator();
                if (multiplexerIndicator != null)
                {
                    SignalClass multiplexor = multiplexerIndicator.getMultiplexor();
                    if (multiplexor != null)
                    {
                        out.format("SG_MUL_VAL_%d %s %s %s ;\n",
                                m.getPrintId(),
                                s.getName(),
                                multiplexor.getName(),
                                multiplexerIndicator.getRanges().stream().map((r)->r.getFrom()+"-"+r.getToInclusive()).collect(Collectors.joining(", ", "", ""))
                                );
                    }
                }
            });
        });
    }
    public void setVersion(String version)
    {
        this.version = version;
    }

    public final void addNode(String name)
    {
        Node node = new Node(this, name);
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

    public void setDefault(String name, Object def)
    {
        Attribute attr = attributes.get(name);
        attr.setDef(def);
    }
    public Object getDefault(String name)
    {
        Attribute attr = attributes.get(name);
        if (attr == null)
        {
            return null;
        }
        return attr.getDef();
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

    public void addMultiplexedSignal(int id, String multiplexedSignalName, String multiplexorSwitchName, List<IntRange> multiplexorValueRanges)
    {
        MessageClass message = messages.get(id);
        message.addMultiplexedSignal(multiplexedSignalName, multiplexorSwitchName, multiplexorValueRanges);
    }

    public final void addAttribute(String name, int value, int def)
    {
        addAttributeDefinition(name, new IntAttributeValueType(0, 0));
        setAttributeDefault(name, def);
        setAttributeValue(name, value);
    }
    public final void addAttribute(String name, String value, String def)
    {
        addAttributeDefinition(name, StringAttributeValueType.STRING_ATTRIBUTE_VALUE_TYPE);
        setAttributeDefault(name, def);
        setAttributeValue(name, value);
    }
    public final void addAttributeDefinition(String name, AttributeValueType type)
    {
        addAttributeDefinition(ObjectType.GLOBAL, name, type);
    }

    public void addAttributeDefinition(ObjectType objectType, String name, AttributeValueType type)
    {
        if (attributes.put(name, new Attribute(objectType, name, type)) != null)
        {
            throw new IllegalArgumentException("duplicate attribute "+name);
        };
    }

    public final void setAttributeDefault(String name, Object value)
    {
        value = checkAttributeType(name, value);
        setDefault(name, value);
    }

    public final void setAttributeValue(String name, Object value)
    {
        value = checkAttributeType(name, value);
        super.setAttributeValue(name, value);
    }

    public final void setNodeAttributeValue(String name, String nodeName, Object value)
    {
        value = checkAttributeType(name, value);
        Node node = nodes.get(nodeName);
        node.setAttributeValue(name, value);
        
    }

    public final void setMessageAttributeValue(String name, int id, Object value)
    {
        value = checkAttributeType(name, value);
        MessageClass message = messages.get(id);
        message.setAttributeValue(name, value);
    }

    public final void setSignalAttributeValue(String name, int id, String signalName, Object value)
    {
        value = checkAttributeType(name, value);
        MessageClass message = messages.get(id);
        message.setSignalAttribute(signalName, name, value);
    }

    public void addValueTable(String name, List<ValueDescription> valueDescriptions)
    {
        valueTables.put(name, valueDescriptions);
    }
    /**
     * Returns MessageClass for pgn
     * @param pgn
     * @return 
     * @see org.vesalainen.can.j1939.PGN#canId(int) 
     */
    public MessageClass getMessageForPgn(int pgn)
    {
        return getMessage(PGN.canId(pgn));
    }
    /**
     * Returns MessageClass
     * @param id As in DBC file
     * @return 
     */
    public MessageClass getMessage(int id)
    {
        return messages.get(id);
    }
    
    public void forEach(Consumer<? super MessageClass> action)
    {
        messages.values().forEach(action);
    }

    public final void addValueDescriptions(List<ValueDescriptions> valDesc)
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
            if (message == null)
            {
                throw new IllegalArgumentException("no message for "+id);
            }
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
        if (!super.equals(obj))
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
        if (!Objects.equals(this.attributes, other.attributes))
        {
            return false;
        }
        return true;
    }

    private Object checkAttributeType(String name, Object value)
    {
        Attribute at = attributes.get(name);
        if (at == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        return at.getType().convType(value);
    }

}
