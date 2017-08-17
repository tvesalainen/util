/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

/**
 * VirtualCircuit provides duplex communication over two channels
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface VirtualCircuit
{
    /**
     * Start VirtualCircuit with cached thread pool.
     * @throws IOException 
     */
    default void start() throws IOException
    {
        start(Executors::newCachedThreadPool);
    }
    /**
     * Start and wait VirtualCircuit with cached thread pool.
     * @throws IOException 
     */
    default void join() throws IOException
    {
        join(Executors::newCachedThreadPool);
    }
    /**
     * Start VirtualCircuit with given thread pool factory.
     * @param executorFactory
     * @throws IOException 
     */
    void start(Supplier<ExecutorService> executorFactory) throws IOException;
    /**
     * Start and wait VirtualCircuit with given thread pool factory.
     * @param executorFactory
     * @throws IOException 
     */
    void join(Supplier<ExecutorService> executorFactory) throws IOException;
    /**
     * Wait until both sides close communication.
     * @throws IOException 
     */
    void waitForFinish() throws IOException;
    /**
     * Interrupts communication.
     * @throws IOException 
     */
    void stop() throws IOException;
}
