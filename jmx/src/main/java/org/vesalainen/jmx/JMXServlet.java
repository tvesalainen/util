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

import java.lang.management.ManagementFactory;
import javax.management.MBeanServer;
import org.vesalainen.html.Document;
import org.vesalainen.html.DynamicElement;
import org.vesalainen.html.Element;
import org.vesalainen.html.jstree.JsTree;
import org.vesalainen.web.servlet.AbstractDocumentServlet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class JMXServlet extends AbstractDocumentServlet<Document>
{

    @Override
    protected Document createDocument()
    {
        setTitle("JMX");
        Document doc = new Document();
        doc.use(new JsTree());
        doc.getHead().addElement("script")
                .setAttr("src", "jstree.js");
        Element body = doc.getBody();
        Element div = body.addElement("div");
        div.setAttr("id", "jstree");
        Element ul = div.addElement("ul");
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        DynamicElement content = new DynamicElement("li", platformMBeanServer.getDomains())
                .setText((t)->t)
                .setAttr("id", (t)->t);
        ul.add(content);
        return doc;
    }
    
}
