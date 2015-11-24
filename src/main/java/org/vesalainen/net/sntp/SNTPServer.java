/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.net.sntp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.net.ntp.NtpV3Impl;

/**
 *
 * @author tkv
 */
public class SNTPServer implements Runnable
{
    private Thread thread;

    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void run()
    {
        try
        {
            DatagramSocket socket = new DatagramSocket(123);
            byte[] buffer = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            while (true)
            {
                socket.receive(packet);
                NtpV3Impl msg = new NtpV3Impl();
                msg.setDatagramPacket(packet);
                System.err.println(msg);
            }
        }
        catch (SocketException ex)
        {
            Logger.getLogger(SNTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
        catch (IOException ex)
        {
            Logger.getLogger(SNTPServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
