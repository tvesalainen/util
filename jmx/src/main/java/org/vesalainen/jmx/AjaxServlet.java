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
package org.vesalainen.jmx;

import java.io.IOException;
import java.io.Writer;
import java.lang.management.ManagementFactory;
import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.html.BooleanAttribute;
import org.vesalainen.html.DynamicElement;
import org.vesalainen.html.Element;
import org.vesalainen.html.InputTag;
import org.vesalainen.html.Renderer;
import org.vesalainen.html.Tag;
import org.vesalainen.util.ConvertUtility;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AjaxServlet extends HttpServlet
{

    private Renderer root;
    private Renderer pattern;
    private Renderer bean;
    private ObjectName objectName;

    @Override
    public void init() throws ServletException
    {
        root = createRoot();
        pattern = createPattern();
        bean = createMBean();
    }

    @Override
    public void destroy()
    {
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        log(req.toString());
        String id = req.getParameter("id");
        if (id != null)
        {
            Renderer content = null;
            if ("#".equals(id))
            {
                content = root;
            }
            else
            {
                try
                {
                    objectName = ObjectName.getInstance(id);
                    if (objectName.isPropertyPattern())
                    {
                        content = pattern;
                    }
                    else
                    {
                        String attribute = req.getParameter("attribute");
                        if (attribute != null)
                        {
                            String value = req.getParameter("value");
                            String type = req.getParameter("type");
                            if (type != null)
                            {
                                boolean ok = setValue(attribute, type, value);
                                if (ok)
                                {
                                    resp.setStatus(HttpServletResponse.SC_OK);
                                }
                                else
                                {
                                    resp.sendError(HttpServletResponse.SC_CONFLICT, "setting "+attribute+" failed");
                                }
                                return;
                            }
                            content = getAttributeValue(objectName, attribute);
                        }
                        else
                        {
                            content = bean;
                        }
                    }
                }
                catch (MalformedObjectNameException | NullPointerException ex)
                {
                    resp.setStatus(HttpServletResponse.SC_CONFLICT);
                    return;
                }
            }
            if (content != null)
            {
                resp.setContentType("text/html");
                resp.setCharacterEncoding("UTF-8");
                resp.setHeader("Cache-Control", "no-store");
                resp.setStatus(HttpServletResponse.SC_OK);
                Writer writer = resp.getWriter();
                content.append(writer);
                writer.flush();
            }
            else
            {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
        }
    }

    private Renderer createRoot()
    {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        Element element = new Element("ul");
        DynamicElement<String,?> content = DynamicElement.getFromArray("li", platformMBeanServer::getDomains);
        content.addClasses("jstree-closed")
            .setText((t)->t)
            .setAttr("id", (t)->t+":*");
        element.add(content);
        return element;
    }
    private Renderer createPattern()
    {
        Supplier<Stream<String>> streamSupplier = ()->
        {
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            Set<ObjectName> queryNames = platformMBeanServer.queryNames(getObjectName(), null);
            String canonicalKeyPropertyListString = objectName.getCanonicalKeyPropertyListString();
            long commas = canonicalKeyPropertyListString.chars().filter((i)->i=='=').count();
            return queryNames
                    .stream()
                    .sorted()
                    .map((o)->o.toString())
                    .map((s)->cut(s, (int) commas+1))
                    .distinct();
        };
        Element ul = new Element("ul");
        DynamicElement<String,?> content = DynamicElement.getFrom("li", streamSupplier);
        content.addClasses((t)->t.endsWith("*") ? "jstree-closed" : "mbean")
            .setText((t)->nodeName(t))
            .setAttr("id", (t)->t);
        ul.add(content);
        return ul;
    }

    private Renderer createMBean()
    {
        Supplier<Stream<MBeanInfo>> streamSupplier = ()->
        {
            try
            {
                MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
                MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(getObjectName());
                return Stream.of(mBeanInfo);
            }
            catch (InstanceNotFoundException | IntrospectionException | ReflectionException ex)
            {
                throw new RuntimeException(ex);
            }
        };
        Element form = new Element("div");
        Element fieldSet = form.addElement("fieldset");
        DynamicElement<ObjectName,?> legend = DynamicElement.getFrom("legend", ()->Stream.of(getObjectName()));
        legend.setText((on)->on.toString());
        fieldSet.add(legend);
        DynamicElement<MBeanInfo,?> content = DynamicElement.getFrom("div", streamSupplier);
        content.setText((i)->"classname="+i.getClassName());
        DynamicElement<MBeanInfo, MBeanInfo> info = content.child("fieldset");
        info.child("legend").setText((t)->"Attributes");
        DynamicElement<MBeanAttributeInfo, MBeanInfo> tr = info.child("table").childFromArray("tr", (t)->t.getAttributes());
        tr.child("td")
            .setText((t)->t.getName());
        tr.child("td")
            .setAttr("id", (a)->a.getName())
            .setDataAttr("objectname", (t)->getObjectName())
            .addClasses("attributeValue");
        fieldSet.add(content);
        return form;
    }

    void setObjectName(ObjectName objectName)
    {
        this.objectName = objectName;
    }

    private ObjectName getObjectName()
    {
        return objectName;
    }
    
    private String cut(String str, int commas)
    {
        int idx = -1;
        for (int ii=0;ii<commas;ii++)
        {
            idx = str.indexOf(',', idx+1);
        }
        if (idx != -1)
        {
            return str.substring(0, idx)+",*";
        }
        else
        {
            return str;
        }
    }
    private String nodeName(String name)
    {
        int idx = name.lastIndexOf('=');
        if (idx != -1)
        {
            name = name.substring(idx+1);
        }
        if (name.endsWith(",*"))
        {
            name = name.substring(0, name.length()-2);
        }
        return name;
    }

    Renderer getBean()
    {
        return bean;
    }

    private Renderer getAttributeValue(ObjectName objectName, String name)
    {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        try
        {
            MBeanInfo mBeanInfo = platformMBeanServer.getMBeanInfo(objectName);
            MBeanAttributeInfo info = getAttribute(mBeanInfo, name);
            Object value = null;
            if (info.isReadable())
            {
                value = platformMBeanServer.getAttribute(objectName, name);
            }
            Class<?> type = getClass(info.getType());
            if (info.isWritable())
            {
                Element form = new Element("form");
                form.setAttr("action", "ajax_nodes.html");
                form.setAttr("method", "post");
                Renderer input = getInputFor(type, value);
                form.add(new InputTag(form, "hidden", "id", objectName));
                form.add(new InputTag(form, "hidden", "attribute", name));
                form.add(new InputTag(form, "hidden", "type", info.getType()));
                form.add(input);
                return form;
            }
            else
            {
                return getOutputFor(type, value);
            }
        }
        catch (Exception ex)
        {
            return new Element("span").addText(ex.getMessage());
        }
    }

    private MBeanAttributeInfo getAttribute(MBeanInfo mBeanInfo, String name)
    {
        for (MBeanAttributeInfo info : mBeanInfo.getAttributes())
        {
            if (name.equals(info.getName()))
            {
                return info;
            }
        }
        throw new IllegalArgumentException(name+" attribute not found");
    }

    private Renderer getInputFor(Class<?> type, Object value)
    {
        switch (type.getSimpleName())
        {
            case "Boolean":
                return new InputTag("checkbox", "value")
                        .addClasses("attributeInput")
                        .setAttr(new BooleanAttribute("checked", value));
            case "Integer":
                return new InputTag("number", "value")
                        .addClasses("attributeInput")
                        .setAttr("min", Integer.MIN_VALUE)
                        .setAttr("max", Integer.MAX_VALUE)
                        .setAttr("pattern", "[\\-\\+]?[0-9]+")
                        .setAttr("value", value);
            case "Short":
                return new InputTag("number", "value")
                        .addClasses("attributeInput")
                        .setAttr("min", Short.MIN_VALUE)
                        .setAttr("max", Short.MAX_VALUE)
                        .setAttr("pattern", "[\\-\\+]?[0-9]+")
                        .setAttr("value", value);
            case "Long":
                return new InputTag("number", "value")
                        .addClasses("attributeInput")
                        .setAttr("min", Long.MIN_VALUE)
                        .setAttr("max", Long.MAX_VALUE)
                        .setAttr("pattern", "[\\-\\+]?[0-9]+")
                        .setAttr("value", value);
            case "Float":
                return new InputTag("number", "value")
                        .addClasses("attributeInput")
                        .setAttr("min", Float.MIN_VALUE)
                        .setAttr("max", Float.MAX_VALUE)
                        .setAttr("value", value);
            case "Double":
                return new InputTag("number", "value")
                        .addClasses("attributeInput")
                        .setAttr("min", Double.MIN_VALUE)
                        .setAttr("max", Double.MAX_VALUE)
                        .setAttr("value", value);
            case "String":
                return new InputTag("text", "value")
                        .addClasses("attributeInput")
                        .setAttr("value", value);
            default:
                return new Element("span").addText(type+" input not supported");
        }
    }

    private Renderer getOutputFor(Class<?> type, Object value)
    {
        if (value != null)
        {
            if (!type.isArray())
            {
                if (type.isAssignableFrom(CompositeData.class))
                {
                    return getOutputForCompositeData((CompositeData) value);
                }
                if (type.isAssignableFrom(TabularData.class))
                {
                    return getOutputForTabularData((TabularData) value);
                }
                return new Element("div").addText(value.toString());
            }
            else
            {
                Class<?> componentType = type.getComponentType();
                Element div = new Element("div");
                int length = Array.getLength(value);
                for (int ii=0;ii<length;ii++)
                {
                    Object v = Array.get(value, ii);
                    Renderer e = getOutputFor(componentType, v);
                    div.add(e);
                }
                return div;
            }
        }
        else
        {
            return new Element("div");
        }
    }

    private Renderer getOutputForCompositeData(CompositeData data)
    {
        CompositeType type = data.getCompositeType();
        Element table = new Element("table");
        table.add(titleRow(data.getCompositeType()));
        table.add(dataRow(data));
        return table;
    }
    private Element dataRow(CompositeData data)
    {
        CompositeType type = data.getCompositeType();
        Element tr = new Element("tr");
        for (String key : type.keySet())
        {
            Element td = tr.addElement("td");
            OpenType<?> ot = type.getType(key);
            Class<?> cls = getClass(ot.getClassName());
            Renderer e = getOutputFor(cls, data.get(key));
            td.add(e);
        }
        return tr;
    }
    private Element titleRow(CompositeType type)
    {
        Element tr = new Element("tr");
        for (String key : type.keySet())
        {
            Element th = tr.addElement("th");
            th.addText(key);
        }
        return tr;
    }
    private Element getOutputForTabularData(TabularData data)
    {
        Element table = new Element("table");
        CompositeType prev = null;
        for (Object value : data.values())
        {
            CompositeData cd = (CompositeData) value;
            CompositeType type = cd.getCompositeType();
            if (prev == null || !prev.equals(type))
            {
                Element hr = titleRow(type);
                table.add(hr);
                prev = type;
            }
            Element dr = dataRow(cd);
            Object[] ci = data.calculateIndex(cd);
            table.add(dr);
        }
        return table;
    }

    private Class<?> getClass(String type)
    {
        try
        {
            return Class.forName(type);
        }
        catch (ClassNotFoundException ex)
        {
            switch (type)
            {
                case "boolean":
                    return Boolean.class;
                case "char":
                    return Character.class;
                case "byte":
                    return Byte.class;
                case "short":
                    return Short.class;
                case "int":
                    return Integer.class;
                case "long":
                    return Long.class;
                case "float":
                    return Float.class;
                case "double":
                    return Double.class;
                default:
                    throw new IllegalArgumentException(type+" not supported");
            }
        }
    }

    private boolean setValue(String name, String type, String value)
    {
        MBeanServer server = ManagementFactory.getPlatformMBeanServer();
        Class<?> cls = getClass(type);
        Object obj = null;
        switch (cls.getSimpleName())
        {
            case "Boolean":
                obj = Boolean.valueOf(value != null);
                break;
            default:
                obj = ConvertUtility.convert(cls, value);
                break;
        }
        try
        {
            server.setAttribute(objectName, new Attribute(name, obj));
            return true;
        }
        catch (MBeanException | ReflectionException | InstanceNotFoundException | AttributeNotFoundException | InvalidAttributeValueException ex)
        {
            Logger.getLogger(AjaxServlet.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

}
