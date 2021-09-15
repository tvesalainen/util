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
import java.io.PrintWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.InvalidAttributeValueException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.Notification;
import javax.management.NotificationFilter;
import javax.management.NotificationFilterSupport;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.management.openmbean.CompositeData;
import javax.management.openmbean.CompositeType;
import javax.management.openmbean.OpenType;
import javax.management.openmbean.TabularData;
import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.html.AttributedContent;
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
public class JmxServlet extends HttpServlet
{
    private static final long MILLIS_IN_A_DAY = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
    private Renderer root;
    private Renderer pattern;
    private Renderer bean;
    private ObjectName objectName;
    private MBeanServer serverConnection;

    @Override
    public void init() throws ServletException
    {
        serverConnection = (MBeanServer) getServletContext().getAttribute(SimpleMBeanServerConnection.class.getName());
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
                            String operation = req.getParameter("operation");
                            if (operation != null)
                            {
                                content = getOperationResult(objectName, operation, req.getParameterMap());
                            }
                            else
                            {
                                String subscribe = req.getParameter("subscribe");
                                if (subscribe != null)
                                {
                                    subscribeNotification(objectName, subscribe, req, resp);
                                    return;
                                }
                                else
                                {
                                    content = bean;
                                }
                            }
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
        Element element = new Element("ul");
        DynamicElement<String,?> content = DynamicElement.getFromArray("li", serverConnection::getDomains);
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
            Set<ObjectName> queryNames = serverConnection.queryNames(getObjectName(), null);
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
                MBeanInfo mBeanInfo = serverConnection.getMBeanInfo(getObjectName());
                return Stream.of(mBeanInfo);
            }
            catch (InstanceNotFoundException | IntrospectionException | ReflectionException ex)
            {
                throw new RuntimeException(ex);
            }
        };
        Element div = new Element("div");
        Element fieldSet = div.addElement("fieldset");
        Element legend = fieldSet.addElement("legend");
        legend.addText(()->getObjectName().toString());
        DynamicElement<MBeanInfo,?> content = DynamicElement.getFrom("div", streamSupplier);
        fieldSet.add(content);
        content.setText((i)->"classname="+i.getClassName());
        createAttributes(content.child("fieldset"));
        createOperations(content.child("fieldset"));
        createNotifications(content.child("fieldset"));
        return div;
    }

    private void createAttributes(DynamicElement<MBeanInfo, MBeanInfo> mBeanInfo)
    {
        mBeanInfo.child("legend").setText((t)->"Attributes");
        DynamicElement<MBeanAttributeInfo, MBeanInfo> tr = mBeanInfo.child("table").childFromArray("tr", (t)->t.getAttributes());
        tr.child("th")
            .setText((t)->t.getName());
        tr.child("td")
            .setAttr("id", (a)->a.getName())
            .setDataAttr("objectname", (t)->getObjectName())
            .addClasses("attributeValue");
    }

    private void createOperations(DynamicElement<MBeanInfo, MBeanInfo> mBeanInfo)
    {
        mBeanInfo.child("legend").setText((t)->"Operations");
        DynamicElement<MBeanOperationInfo, MBeanInfo> tr = mBeanInfo.child("table").childFromArray("tr", (t)->t.getOperations());
        DynamicElement<MBeanOperationInfo, MBeanOperationInfo> form = tr.child("td").child("form");
        form.child("input")
            .setAttr("type", "hidden")
            .setAttr("name", "id")
            .setAttr("value", ()->objectName);
        form.child("input")
            .setAttr("type", "hidden")
            .setAttr("name", "operation")
            .setAttr("value", (t)->getOperationIdentifier(t))
            .addClasses("operationId");
        form.child("input")
            .addClasses("operationInvoke")
            .setAttr("type", "button")
            .setAttr("name", "operation")
            .setAttr("value", (t)->t.getName());
        form.childFromArray("input", (i)->i.getSignature())
            .attribute((t,p)->setInputAttributes(getClass(t.getType()), p))
            .setAttr("name", (p)->p.getName())
            .setAttr("title", (p)->p.getType());
        tr.child("td")
            .setAttr("id", (t)->getOperationIdentifier(t))
            .setDataAttr("objectname", (t)->getObjectName())
            .addClasses("operationResult");
    }

    private void createNotifications(DynamicElement<MBeanInfo, MBeanInfo> mBeanInfo)
    {
        mBeanInfo.child("legend").setText((t)->"Notifications");
        DynamicElement<MBeanNotificationInfo, MBeanInfo> tr = mBeanInfo.child("table").childFromArray("tr", (t)->t.getNotifications());
        DynamicElement<MBeanNotificationInfo, MBeanNotificationInfo> form = tr.child("td").child("form");
        form.child("input")
            .setAttr("type", "hidden")
            .setAttr("name", "id")
            .setAttr("value", ()->objectName);
        form.child("input")
            .setAttr("type", "hidden")
            .setAttr("name", "subscribe")
            .setAttr("value", (t)->t.getName());
        form.child("input")
            .addClasses("subscribeNotification")
            .setAttr("type", "button")
            .setAttr("name", "subscribe")
            .setAttr("value", (t)->t.getDescription());
        DynamicElement<String, MBeanNotificationInfo> div = form.childFromArray("div", (i)->i.getNotifTypes());
        div.child("input")
            .setAttr("type", "checkbox")
            .setAttr("name", (p)->p)
            .setAttr("id", (p)->p)
            .setAttr("value", (p)->p)
            .setAttr(new BooleanAttribute("checked", true));
        div.child("label")
            .setAttr("for", (p)->p)
            .setText((p)->p.toString());
    }

    private Renderer getOperationResult(ObjectName objectName, String name, Map<String, String[]> parameters)
    {
        try
        {
            MBeanInfo mBeanInfo = serverConnection.getMBeanInfo(objectName);
            MBeanOperationInfo info = null;
            for (MBeanOperationInfo i : mBeanInfo.getOperations())
            {
                if (name.equals(getOperationIdentifier(i)))
                {
                    info = i;
                    break;
                }
            }
            if (info == null)
            {
                throw new IllegalArgumentException(name+" not found");
            }
            MBeanParameterInfo[] signature = info.getSignature();
            Object[] params = new Object[signature.length];
            String[] sig = new String[signature.length];
            for (int ii=0;ii<signature.length;ii++)
            {
                Class<?> type = getClass(signature[ii].getType());
                String[] arr = parameters.get(signature[ii].getName());
                if (arr == null || arr.length != 1)
                {
                    if (Boolean.class.equals(type))
                    {
                        arr = new String[]{"false"};
                    }
                    else
                    {
                        throw new IllegalArgumentException(signature[ii].getName()+" not found");
                    }
                }
                sig[ii] = signature[ii].getType();
                params[ii] = ConvertUtility.convert(type, arr[0]);
            }
            Object result = serverConnection.invoke(objectName, info.getName(), params, sig);
            return getOutputFor(getClass(info.getReturnType()), result);
        }
        catch (Exception ex)
        {
            return new Element("span").addText(ex.getMessage());
        }
    }

    private String getOperationIdentifier(MBeanOperationInfo t)
    {
        StringBuilder sb = new StringBuilder();
        sb.append(t.getName());
        for (MBeanParameterInfo info : t.getSignature())
        {
            sb.append(info.getType());
        }
        return sb.toString();
    }

    private Renderer getAttributeValue(ObjectName objectName, String name)
    {
        try
        {
            MBeanInfo mBeanInfo = serverConnection.getMBeanInfo(objectName);
            MBeanAttributeInfo info = getFeatureInfo(name, mBeanInfo.getAttributes());
            Object value = null;
            if (info.isReadable())
            {
                value = serverConnection.getAttribute(objectName, name);
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

    private <I extends MBeanFeatureInfo> I getFeatureInfo(String name, I... mBeanFeatureInfo)
    {
        for (I info : mBeanFeatureInfo)
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
        Tag input = new Tag("input")
            .setAttr("name", "value")
            .addClasses("attributeInput");
        switch (type.getSimpleName())
        {
            case "Boolean":
                input.setAttr(new BooleanAttribute("checked", value));
            case "Integer":
            case "Short":
            case "Long":
            case "Float":
            case "Double":
            case "String":
                input.setAttr("value", value);
        }
        return setInputAttributes(type, input);
    }
    private Renderer setInputAttributes(Class<?> type, AttributedContent input)
    {
        switch (type.getSimpleName())
        {
            case "Boolean":
                return  input.setAttr("type", "checkbox");
            case "Integer":
                return  input.setAttr("type", "number")
                        .setAttr("min", Integer.MIN_VALUE)
                        .setAttr("max", Integer.MAX_VALUE)
                        .setAttr("pattern", "[\\-\\+]?[0-9]+");
            case "Short":
                return  input.setAttr("type", "number")
                        .setAttr("min", Short.MIN_VALUE)
                        .setAttr("max", Short.MAX_VALUE)
                        .setAttr("pattern", "[\\-\\+]?[0-9]+");
            case "Long":
                return  input.setAttr("type", "number")
                        .setAttr("min", Long.MIN_VALUE)
                        .setAttr("max", Long.MAX_VALUE)
                        .setAttr("pattern", "[\\-\\+]?[0-9]+");
            case "Float":
                return  input.setAttr("type", "number")
                        .setAttr("min", Float.MIN_VALUE)
                        .setAttr("max", Float.MAX_VALUE);
            case "Double":
                return  input.setAttr("type", "number")
                        .setAttr("min", Double.MIN_VALUE)
                        .setAttr("max", Double.MAX_VALUE);
            case "String":
                return  input.setAttr("type", "text");
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
                case "void":
                    return Void.class;
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
            serverConnection.setAttribute(objectName, new Attribute(name, obj));
            return true;
        }
        catch (MBeanException | ReflectionException | InstanceNotFoundException | AttributeNotFoundException | InvalidAttributeValueException ex)
        {
            Logger.getLogger(JmxServlet.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
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

    private void subscribeNotification(ObjectName objectName, String name, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/event-stream");
        resp.setHeader("Cache-Control", "no-store");
        resp.flushBuffer();
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(-1);
        NotificationFilterSupport filter = new NotificationFilterSupport();
        Listener listener = new Listener(objectName, asyncContext, filter, null);
        asyncContext.addListener(listener);
        try
        {
            MBeanInfo mBeanInfo = serverConnection.getMBeanInfo(objectName);
            MBeanNotificationInfo info = getFeatureInfo(name, mBeanInfo.getNotifications());
            
            for (String type : info.getNotifTypes())
            {
                String parameter = req.getParameter(type);
                if (parameter != null)
                {
                    filter.enableType(type);
                }
            }
            
            serverConnection.addNotificationListener(objectName, listener, filter, null);

        }
        catch (IllegalArgumentException | InstanceNotFoundException | IntrospectionException | ReflectionException ex)
        {
            throw new IOException(ex);
        }
    }

    private class Listener implements NotificationListener, AsyncListener
    {
        private final ObjectName objectName;
        private final AsyncContext asyncContext;
        private final NotificationFilter filter;
        private final Object handback;
        private final ReentrantLock lock = new ReentrantLock();

        public Listener(ObjectName objectName, AsyncContext asyncContext, NotificationFilter filter, Object handback)
        {
            this.objectName = objectName;
            this.asyncContext = asyncContext;
            this.filter = filter;
            this.handback = handback;
        }

        @Override
        public void handleNotification(Notification n, Object handback)
        {
            PrintWriter writer = null;
            lock.lock();
            try
            {
                writer = asyncContext.getResponse().getWriter();
                writer.write("data:");
                writer.append("<tr class=\"notification\"><td>");
                writeTimestamp(writer, n.getTimeStamp());
                writer.append("</td><td>");
                writer.append(n.getMessage());
                writer.append("</td><td>");
                Object u = n.getUserData();
                writer.append(u != null ? u.toString() : "");
                writer.append("</td><td>");
                writer.append(Long.toString(n.getSequenceNumber()));
                writer.append("</td></tr>");
                writer.write("\n\n");
                if (writer.checkError())
                {
                    removeNotificationListener();
                    log("sse quit");
                    asyncContext.complete();
                }
            }
            catch (Exception ex)
            {
                removeNotificationListener();
                log("sse quit", ex);
                asyncContext.complete();
            }
            finally
            {
                lock.unlock();
            }
        }
        @Override
        public void onComplete(AsyncEvent event) throws IOException
        {
            log("onComplete");
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException
        {
            removeNotificationListener();
            log("onTimeout");
        }

        @Override
        public void onError(AsyncEvent event) throws IOException
        {
            removeNotificationListener();
            Throwable throwable = event.getThrowable();
            if (throwable != null)
            {
                log("onError", throwable);
            }
            else
            {
                log("onError");
            }
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException
        {
            log("onStartAsync");
        }

        private void removeNotificationListener()
        {
            try
            {
                serverConnection.removeNotificationListener(objectName, this, filter, handback);
            }
            catch (InstanceNotFoundException | ListenerNotFoundException ex1)
            {
            }
        }

        private void writeTimestamp(PrintWriter w, long t)
        {
            long d = t % MILLIS_IN_A_DAY;
            long ms = d % 1000;
            d /= 1000;
            long s = d % 60;
            d /= 60;
            long m = d % 60;
            d /= 60;
            long h = d % 24;
            w.printf("%02d:%02d:%02d.%03d", h, m, s, ms);
        }

        
    }
}
