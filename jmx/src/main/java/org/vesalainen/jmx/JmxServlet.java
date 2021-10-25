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
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
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
import org.vesalainen.html.AbstractDynamicInput;
import org.vesalainen.html.AttributedContent;
import org.vesalainen.html.BooleanAttribute;
import org.vesalainen.html.DynamicElement;
import org.vesalainen.html.Element;
import org.vesalainen.html.EntityReferences;
import org.vesalainen.html.InputTag;
import org.vesalainen.html.Renderer;
import org.vesalainen.html.Tag;
import org.vesalainen.lang.Primitives;
import org.vesalainen.util.CharSequences;
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
    private MBeanServer serverConnection;
    private MBeanData mBeanData = new MBeanData();
    private Supplier<ObjectName> objectName = mBeanData::getObjectName;
    private Supplier<MBeanInfo> mBeanInfo = mBeanData::getMBeanInfo;
    private Function<String,MBeanAttributeInfo> attributeInfo = mBeanData::getAttributeInfo;
    private Function<String,MBeanOperationInfo> operationInfo = mBeanData::getOperationInfo;
    private Function<String,MBeanNotificationInfo> notificationInfo = mBeanData::getNotificationInfo;
    private Function<String,Object> attributeValue = mBeanData::getAttributeValue;
    private Function<String,Class<?>> attributeType = mBeanData::getAttributeType;
    private BiConsumer<String,Object> setAttribute = mBeanData::setAttributeValue;

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
        Renderer content = null;
        try
        {
            String id = req.getParameter("id");
            if (id != null)
            {
                if ("#".equals(id))
                {
                    content = root;
                }
                else
                {
                    mBeanData.setName(id);
                    if (objectName.get().isPropertyPattern())
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
                            //content = getAttributeValue(attribute);
                        }
                        else
                        {
                            String operation = req.getParameter("operation");
                            if (operation != null)
                            {
                                content = getOperationResult(operation, req.getParameterMap());
                            }
                            else
                            {
                                String subscribe = req.getParameter("subscribe");
                                if (subscribe != null)
                                {
                                    subscribeNotification(subscribe, req, resp);
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
        catch (Exception ex)
        {
            throw new ServletException(ex);
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
            Set<ObjectName> queryNames = serverConnection.queryNames(objectName.get(), null);
            String canonicalKeyPropertyListString = objectName.get().getCanonicalKeyPropertyListString();
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
            return Stream.of(mBeanInfo.get());
        };
        Element div = new Element("div");
        Element fieldSet = div.addElement("fieldset");
        Element legend = fieldSet.addElement("legend");
        legend.addText(()->objectName.get().toString());
        DynamicElement<MBeanInfo,?> content = DynamicElement.getFrom("div", streamSupplier);
        fieldSet.add(content);
        content.child("div").setText((i)->"classname="+i.getClassName());
        content.child("div").setText((i)->i.getDescription());
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
        DynamicElement<MBeanAttributeInfo, MBeanAttributeInfo> td = tr.child("td")
            .setAttr("id", (a)->a.getName())
            .setDataAttr("objectname", (t)->objectName.get())
            .addClasses("attributeValue");

        DynamicElement<MBeanAttributeInfo, MBeanAttributeInfo> form = td.child("form")
                .setAttr("action", "ajax_nodes.html")
                .setAttr("method", "post");
        form.child("input")
                .setAttr("type", "hidden")
                .setAttr("name", "id")
                .setAttr("value", objectName);
        form.child("input")
                .setAttr("type", "hidden")
                .setAttr("name", "attribute")
                .setAttr("value", (t)->t.getName());
        form.child("input")
                .setAttr("type", "hidden")
                .setAttr("name", "type")
                .setAttr("value", (t)->t.getType());
        AttributeInput attributeInput = new AttributeInput();
        attributeInput.setAttr("name", "value");
        attributeInput.addClasses("attributeInput");
        form.addContent(attributeInput);
    }
    private void createOperations(DynamicElement<MBeanInfo, MBeanInfo> mBeanInfo)
    {
        mBeanInfo.child("legend").setText((t)->"Operations");
        DynamicElement<MBeanOperationInfo, MBeanInfo> tr = mBeanInfo.child("table").childFromArray("tr", (t)->t.getOperations());
        DynamicElement<MBeanOperationInfo, MBeanOperationInfo> form = tr.child("td").child("form");
        tr.child("td")
            .setAttr("id", (t)->getOperationIdentifier(t))
            .setDataAttr("objectname", (t)->objectName.get())
            .addClasses("operationResult");
        form.child("input")
            .setAttr("type", "hidden")
            .setAttr("name", "id")
            .setAttr("value", ()->objectName.get());
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
        ParameterInput parameterInput = new ParameterInput();
        parameterInput.setAttr("name", (p)->p.getName());
        parameterInput.setAttr("title", (p)->p.getType());
        form.childFromArray(null, (i)->i.getSignature())
            .addContent(parameterInput);
    }

    private void createNotifications(DynamicElement<MBeanInfo, MBeanInfo> mBeanInfo)
    {
        mBeanInfo.child("legend").setText((t)->"Notifications");
        DynamicElement<MBeanNotificationInfo, MBeanInfo> tr = mBeanInfo.child("table").childFromArray("tr", (t)->t.getNotifications());
        DynamicElement<MBeanNotificationInfo, MBeanNotificationInfo> form = tr.child("td").child("form");
        form.child("input")
            .setAttr("type", "hidden")
            .setAttr("name", "id")
            .setAttr("value", ()->objectName.get());
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

    private Renderer getOperationResult(String name, Map<String, String[]> parameters)
    {
        try
        {
            MBeanOperationInfo info = null;
            for (MBeanOperationInfo i : mBeanInfo.get().getOperations())
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
                Class<?> type = Primitives.getClass(signature[ii].getType());
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
            Object result = mBeanData.getOperationResult(info.getName(), params, sig);
            return new ValueRenderer(result);
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
        return sb.toString().replace('.', '-');
    }

    private boolean setValue(String name, String type, String value)
    {
        Class<?> cls = Primitives.getClass(type);
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
        setAttribute.accept(name, obj);
        return true;
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

    private void subscribeNotification(String name, HttpServletRequest req, HttpServletResponse resp) throws IOException
    {
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("text/event-stream");
        resp.setHeader("Cache-Control", "no-store");
        resp.flushBuffer();
        AsyncContext asyncContext = req.startAsync();
        asyncContext.setTimeout(-1);
        
        try
        {
            MBeanNotificationInfo info = notificationInfo.apply(name);
            int commonPrefixLength = CharSequences.commonPrefixLength(info.getNotifTypes());
            NotificationFilterSupport filter = new NotificationFilterSupport();
            boolean notAllSet = false;
            for (String type : info.getNotifTypes())
            {
                String parameter = req.getParameter(type);
                if (parameter != null)
                {
                    filter.enableType(type);
                }
                else
                {
                    notAllSet = true;
                }
            }
            if (!notAllSet)
            {
                filter = null;
            }
            
            Listener listener = new Listener(objectName.get(), asyncContext, filter, commonPrefixLength);
            asyncContext.addListener(listener);
            serverConnection.addNotificationListener(objectName.get(), listener, filter, null);

        }
        catch (IllegalArgumentException | InstanceNotFoundException  ex)
        {
            throw new IOException(ex);
        }
    }

    private class Listener implements NotificationListener, AsyncListener
    {
        private final ObjectName objectName;
        private final AsyncContext asyncContext;
        private final NotificationFilter filter;
        private final int commonPrefixLength;
        private final ReentrantLock lock = new ReentrantLock();

        public Listener(ObjectName objectName, AsyncContext asyncContext, NotificationFilter filter, int commonPrefixLength)
        {
            this.objectName = objectName;
            this.asyncContext = asyncContext;
            this.filter = filter;
            this.commonPrefixLength = commonPrefixLength;
        }

        @Override
        public void handleNotification(Notification n, Object handback)
        {
            PrintWriter writer = null;
            lock.lock();
            try
            {
                writer = asyncContext.getResponse().getWriter();
                writer.append("data:");
                writer.append("<tr class=\"notification\"><td>");
                writeTimestamp(writer, n.getTimeStamp());
                writer.append("</td><td>");
                writer.append(n.getType().substring(commonPrefixLength));
                writer.append("</td><td>");
                writer.append(EntityReferences.encode(n.getMessage().trim()));
                writer.append("</td><td>");
                Object u = n.getUserData();
                writer.append(u != null ? EntityReferences.encode(u.toString()) : "");
                writer.append("</td><td>");
                writer.append(Long.toString(n.getSequenceNumber()));
                writer.append("</td></tr>");
                writer.append("\n\n");
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
                serverConnection.removeNotificationListener(objectName, this, filter, null);
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
    private class MBeanData
    {
        private ObjectName objectName;
        private MBeanInfo mBeanInfo;
        
        public void setName(String name)
        {
            try
            {
                this.objectName = ObjectName.getInstance(name);
                if (!objectName.isPattern())
                {
                    this.mBeanInfo = serverConnection.getMBeanInfo(objectName);
                }
                else
                {
                    this.mBeanInfo = null;
                }
            }
            catch (InstanceNotFoundException | IntrospectionException | ReflectionException | MalformedObjectNameException | NullPointerException ex)
            {
                throw new RuntimeException(ex);
            }
        }

        public ObjectName getObjectName()
        {
            return objectName;
        }

        public MBeanInfo getMBeanInfo()
        {
            return mBeanInfo;
        }

        public MBeanAttributeInfo getAttributeInfo(String attribute)
        {
            return getFeatureInfo(attribute, mBeanInfo.getAttributes());
        }

        public MBeanOperationInfo getOperationInfo(String operation)
        {
            return getFeatureInfo(operation, mBeanInfo.getOperations());
        }

        public MBeanNotificationInfo getNotificationInfo(String notification)
        {
            return getFeatureInfo(notification, mBeanInfo.getNotifications());
        }
        public Class<?> getAttributeType(String attribute)
        {
            MBeanAttributeInfo info = getAttributeInfo(attribute);
            return Primitives.getClass(info.getType());
        }
        public Object getAttributeValue(String attribute)
        {
            try
            {
                MBeanAttributeInfo info = getAttributeInfo(attribute);
                if (info.isReadable())
                {
                    return serverConnection.getAttribute(objectName, attribute);
                }
                else
                {
                    return null;
                }
            }
            catch (InstanceNotFoundException | ReflectionException | MBeanException | AttributeNotFoundException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        public void setAttributeValue(String attribute, Object value)
        {
            try
            {
                serverConnection.setAttribute(objectName, new Attribute(attribute, value));
            }
            catch (InstanceNotFoundException | AttributeNotFoundException | InvalidAttributeValueException | MBeanException | ReflectionException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        public Object getOperationResult(String operation, Object[] params, String[] sig)
        {
            try
            {
                return serverConnection.invoke(objectName, operation, params, sig);
            }
            catch (InstanceNotFoundException | MBeanException | ReflectionException ex)
            {
                throw new RuntimeException(ex);
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
    }
    private class ParameterInput extends AbstractDynamicInput<MBeanParameterInfo>
    {

        @Override
        public void append(Appendable out, MBeanParameterInfo t) throws IOException
        {
            try
            {
                append(out, t, Primitives.getClass(t.getType()), null);
            }
            catch (Exception ex)
            {
                out.append("<code>");
                out.append(ex.getMessage());
                out.append("</code>");
            }
        }
        
    }
    private class AttributeInput extends AbstractDynamicInput<MBeanAttributeInfo>
    {

        @Override
        public void append(Appendable out, MBeanAttributeInfo t) throws IOException
        {
            try
            {
                if (t.isWritable())
                {
                    append(out, t, Primitives.getClass(t.getType()), attributeValue.apply(t.getName()));
                }
                else
                {
                    OpenTypeAppendable.append(out, Primitives.getClass(t.getType()), attributeValue.apply(t.getName()));
                }
            }
            catch (Exception ex)
            {
                out.append("<code>");
                out.append(ex.getMessage());
                out.append("</code>");
            }
        }
        
    }
    private class ValueRenderer implements Renderer
    {
        private final Object value;

        public ValueRenderer(Object value)
        {
            this.value = value;
        }
        
        @Override
        public void append(Appendable out) throws IOException
        {
            OpenTypeAppendable.append(out, out != null ? out.getClass() : Object.class, value);
        }
        
    }
}
