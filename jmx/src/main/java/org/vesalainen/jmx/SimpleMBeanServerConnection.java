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
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.TimeUnit;
import static java.util.logging.Level.SEVERE;
import javax.management.InstanceNotFoundException;
import javax.management.ListenerNotFoundException;
import javax.management.MBeanServer;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.vesalainen.management.SimpleNotificationEmitter;
import org.vesalainen.util.concurrent.RefreshableTimer;
import org.vesalainen.web.servlet.JarServlet;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleMBeanServerConnection extends AbstractMBeanServerConnection
{
    private final SimpleNotificationEmitter emitter = new SimpleNotificationEmitter(null, null, null);
    
    public SimpleMBeanServerConnection(MBeanServer server)
    {
        super(server);
    }

    void run(ServerSocketChannel channel)
    {
        try
        {
            Server server = new Server();
            ServerConnector connector = new ServerConnector(server);
            connector.open(channel);
            server.addConnector(connector);
            HandlerList handlers = new HandlerList();
            ServletContextHandler context = new ServletContextHandler();
            context.getServletContext().setAttribute(SimpleMBeanServerConnection.class.getName(), this);
            context.addServlet(IndexServlet.class, "/");
            context.addServlet(JmxScriptServlet.class, "/jmx.js");
            ServletHolder holder = new ServletHolder(JmxServlet.class);
            holder.setAsyncSupported(true);
            context.addServlet(holder, "/jmx");
            context.addServlet(JarServlet.class, "*.js");
            context.addServlet(JarServlet.class, "*.css");
            context.addServlet(JarServlet.class, "*.gif");
            context.addServlet(JarServlet.class, "*.png");
            context.addServlet(JarServlet.class, "*.ico");
            RefreshableTimer timer = new RefreshableTimer();
            timer.setCondition(emitter::hasListeners);
            TimeoutHandler timeoutHandler = new TimeoutHandler(timer);
            handlers.addHandler(timeoutHandler);
            handlers.addHandler(context);
            server.setHandler(handlers);
            server.start();
            server.setStopTimeout(0);
            info("started %s", server);
            timer.wait(60, TimeUnit.SECONDS);
            info("stopping %s", server);
            server.stop();
            info("stopped %s", server);
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "server connection");
        }
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException, ListenerNotFoundException
    {
        super.removeNotificationListener(name, listener, filter, handback);
        emitter.removeNotificationListener(listener, filter, handback);
    }

    @Override
    public void removeNotificationListener(ObjectName name, NotificationListener listener) throws InstanceNotFoundException, ListenerNotFoundException
    {
        super.removeNotificationListener(name, listener);
        emitter.removeNotificationListener(listener);
    }

    @Override
    public void addNotificationListener(ObjectName name, NotificationListener listener, NotificationFilter filter, Object handback) throws InstanceNotFoundException
    {
        super.addNotificationListener(name, listener, filter, handback);
        emitter.addNotificationListener(listener, filter, handback);
    }
    
    private class TimeoutHandler extends  AbstractHandler
    {
        private final RefreshableTimer timer;

        public TimeoutHandler(RefreshableTimer timer)
        {
            this.timer = timer;
        }
        
        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException
        {
            timer.refresh();
        }
    }
}
