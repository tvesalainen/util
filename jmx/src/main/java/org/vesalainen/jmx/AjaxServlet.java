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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.vesalainen.html.DynamicElement;
import org.vesalainen.html.Element;
import org.vesalainen.html.Tag;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AjaxServlet extends HttpServlet
{

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        log(req.toString());
        String id = req.getParameter("id");
        if (id != null)
        {
            Element content = null;
            if ("#".equals(id))
            {
                content = root();
            }
            else
            {
                try
                {
                    ObjectName objectName = ObjectName.getInstance(id);
                    if (objectName.isPropertyPattern())
                    {
                        content = pattern(objectName);
                    }
                    else
                    {
                        content = bean(objectName);
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

    private Element root()
    {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        Element ul = new Element("ul");
        DynamicElement content = new DynamicElement("li", platformMBeanServer.getDomains())
                .addClasses("jstree-closed")
                .setText((t)->t)
                .setAttr("id", (t)->t+":*");
        ul.add(content);
        return ul;
    }

    private Element pattern(ObjectName objectName)
    {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> queryNames = platformMBeanServer.queryNames(objectName, null);
        String canonicalKeyPropertyListString = objectName.getCanonicalKeyPropertyListString();
        long commas = canonicalKeyPropertyListString.chars().filter((i)->i=='=').count();
        Stream<String> stream = queryNames
                .stream()
                .sorted()
                .map((o)->o.toString())
                .map((s)->cut(s, (int) commas+1))
                .distinct();
        Element ul = new Element("ul");
        DynamicElement<String> content = new DynamicElement<String>("li", ()->stream)
                .addClasses("jstree-closed")
                .setText((t)->nodeName(t))
                .setAttr("id", (t)->t);
        ul.add(content);
        return ul;
    }

    private Element bean(ObjectName objectName)
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
}
