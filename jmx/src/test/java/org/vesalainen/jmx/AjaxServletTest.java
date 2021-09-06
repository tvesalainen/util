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
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.vesalainen.html.PrettyPrinter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class AjaxServletTest
{
    
    public AjaxServletTest()
    {
    }

    @Test
    public void test() throws MalformedObjectNameException, ServletException, IOException
    {
        ObjectName on = ObjectName.getInstance("java.lang:type=ClassLoading");
        AjaxServlet as = new AjaxServlet();
        as.init();
        as.setObjectName(on);
        as.getBean().append(new PrettyPrinter(System.err));
    }
    
}
