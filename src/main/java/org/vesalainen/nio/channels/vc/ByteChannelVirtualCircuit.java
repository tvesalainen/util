/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.nio.channels.vc;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * VirtualCircuit implementation using two threads. Suitable for channels not
 * implementing SelectableChannel. For SelectableChannels use 
 * SelectableChannelVirtualCircuit.
 * @author tkv
 */
public class ByteChannelVirtualCircuit implements VirtualCircuit
{
    private ByteChannel ch1;
    private ByteChannel ch2;
    private int capacity;
    private boolean direct;
    private Future<Void> f1;
    private Future<Void> f2;

    public ByteChannelVirtualCircuit(ByteChannel ch1, ByteChannel ch2, int capacity, boolean direct)
    {
        this.ch1 = ch1;
        this.ch2 = ch2;
        this.capacity = capacity;
        this.direct = direct;
    }
    
    @Override
    public void start(ExecutorService executor) throws IOException
    {
        f1 = executor.submit(new Copier(ch1, ch2));
        f2 = executor.submit(new Copier(ch2, ch1));
    }

    @Override
    public void waitForFinish() throws IOException
    {
        try
        {
            f1.get();
            f2.get();
        }
        catch (InterruptedException ex)
        {
            throw new IOException(ex);
        }
        catch (ExecutionException ex)
        {
            Logger.getLogger(ByteChannelVirtualCircuit.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void stop() throws IOException
    {
        f1.cancel(true);
        f2.cancel(true);
    }

    private class Copier implements Callable<Void>
    {
        private ReadableByteChannel readChannel;
        private WritableByteChannel writeChannel;
        private ByteBuffer bb;

        public Copier(ReadableByteChannel rc, WritableByteChannel wc)
        {
            this.readChannel = rc;
            this.writeChannel = wc;
            if (direct)
            {
                this.bb = ByteBuffer.allocateDirect(capacity);
            }
            else
            {
                this.bb = ByteBuffer.allocate(capacity);
            }
        }


        @Override
        public Void call() throws Exception
        {
            try
            {
                while (true)
                {
                    bb.clear();
                    int rc = readChannel.read(bb);
                    if (rc == -1)
                    {
                        return null;
                    }
                    bb.flip();
                    while (bb.hasRemaining())
                    {
                        writeChannel.write(bb);
                    }
                }
            }
            finally
            {
                readChannel.close();
                writeChannel.close();
            }
        }
    }
}
