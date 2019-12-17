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

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DBCFile
{
    private String version;
    private Map<String,Node> nodes = new HashMap<>();
    private Map<Integer,Message> messages = new HashMap<>();
    private String comment;

    void setVersion(String version)
    {
        this.version = version;
    }

    void addNode(String name)
    {
        Node node = new Node(name);
        nodes.put(name, node);
    }

    void addMessage(Message message)
    {
        messages.put(message.getId(), message);
    }

    void setComment(String comment)
    {
        this.comment = comment;
    }

    void setNodeComment(String name, String comment)
    {
        Node node = nodes.get(name);
        node.setComment(comment);
    }

    void setMessageComment(int id, String comment)
    {
        Message message = messages.get(id);
        message.setComment(comment);
    }

    void setSignalComment(int id, String signal, String comment)
    {
        Message message = messages.get(id);
        message.setSignalComment(signal, comment);
    }
    
}
