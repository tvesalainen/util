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
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.management.InstanceNotFoundException;
import javax.management.IntrospectionException;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.html.DynamicElement;
import org.vesalainen.html.Element;
import org.vesalainen.html.Renderer;
import org.vesalainen.html.Tag;

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
                        content = bean;
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
        Element form = new Element("form");
        Element fieldSet = form.addElement("fieldset");
        DynamicElement<ObjectName,?> legend = DynamicElement.getFrom("legend", ()->Stream.of(getObjectName()));
        legend.setText((on)->on.toString());
        fieldSet.add(legend);
        DynamicElement<MBeanInfo,?> content = DynamicElement.getFrom("div", streamSupplier);
        content.setText((i)->"classname="+i.getClassName());
        DynamicElement<MBeanInfo, MBeanInfo> info = content.child("fieldset");
        info.child("legend").setText((t)->"Attributes");
        info.child("table")
                .childFromArray("tr", (t)->t.getAttributes())
                .child("td")
                .setText((t)->t.getName());
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

}
