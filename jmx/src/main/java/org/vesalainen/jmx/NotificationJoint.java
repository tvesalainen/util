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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationFilter;
import javax.management.NotificationListener;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class NotificationJoint implements NotificationListener
{
    private final ReferenceQueue<NotificationListener> queue;
    private final NotificationBroadcaster notificationBroadcaster;
    private final WeakReference<NotificationListener> listener;

    public NotificationJoint(NotificationBroadcaster notificationBroadcaster, NotificationListener listener, NotificationFilter filter, Object handback)
    {
        this.queue = new ReferenceQueue<>();
        this.notificationBroadcaster = notificationBroadcaster;
        this.listener = new WeakReference<>(listener, queue);
        notificationBroadcaster.addNotificationListener(this, filter, handback);
    }
    
    
    @Override
    public void handleNotification(Notification notification, Object handback)
    {
        NotificationListener nl = listener.get();
        if (nl != null)
        {
            nl.handleNotification(notification, handback);
        }
        else
        {
            Reference<? extends NotificationListener> poll = queue.poll();
            if (poll != null)
            {
                nl = poll.get();
                if (nl != null)
                {
                    try
                    {
                        notificationBroadcaster.removeNotificationListener(nl);
                    }
                    catch (ListenerNotFoundException ex)
                    {
                        throw new RuntimeException(ex);
                    }
                }
            }
        }
    }
    
}
