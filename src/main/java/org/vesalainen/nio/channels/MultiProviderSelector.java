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
package org.vesalainen.nio.channels;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.logging.JavaLogging;

/**
 * a bridge between selectors from different providers.
 * 
 * <p>Note! You cannot use SelectableChannel register method. Instead use 
 * MultiProviderSelector.register.
 * @author tkv
 */
public class MultiProviderSelector extends AbstractSelector
{
    private Map<SelectorProvider,Selector> map = new HashMap<>();
    private Map<Selector,SelectorWrapper> wrapperMap = new HashMap<>();
    private Map<Selector,Thread> threadMap = new HashMap<>();
    private Set<SelectionKey> keys = new HashSet<>();
    private final Set<SelectionKey> unmodifiableKeys = Collections.unmodifiableSet(keys);
    private Set<SelectionKey> selectedKeys = new HashSet<>();
    private final Map<SelectionKey,MultiProviderSelectionKey> keyMap = new HashMap<>();
    private Set<SelectionKey> keyPool = new HashSet<>();
    private IOException ioException;
    private final Semaphore semaphore = new Semaphore(0);
    private final Semaphore wrapperSemaphore = new Semaphore(0);
    private AtomicInteger wrapperPermissions = new AtomicInteger();
    private final ReentrantLock lock = new ReentrantLock();
    private boolean wait;
    private final JavaLogging log;
    
    public MultiProviderSelector()
    {
        super(MultiSelectorProvider.provider());
        log = new JavaLogging(this.getClass());
    }

    @Override
    public Selector wakeup()
    {
        log.fine("wakeup(%s", this);
        for (SelectorWrapper sw : wrapperMap.values())
        {
            sw.selector.wakeup();
        }
        return this;
    }

    @Override
    protected void implCloseSelector() throws IOException
    {
        log.fine("close(%s", this);
        for (Selector selector : map.values())
        {
            Thread thread = threadMap.get(selector);
            thread.interrupt();
            selector.close();
        }
        map = null;
        keys = null;
        selectedKeys = null;
        keyPool = null;
        wrapperMap = null;
        threadMap = null;
    }

    @Override
    protected SelectionKey register(AbstractSelectableChannel ch, int ops, Object att)
    {
        log.fine("register(%s)", this);
        lock.lock();
        try
        {
            SelectionKey sk = null;
            SelectorProvider provider = ch.provider();
            Selector selector = map.get(provider);
            if (selector == null)
            {
                selector = provider.openSelector();
                map.put(provider, selector);
                SelectorWrapper sw = new SelectorWrapper(selector);
                wrapperMap.put(selector, sw);
                Thread thread = new Thread(sw);
                threadMap.put(selector, thread);
                sk = ch.register(selector, ops);
                wrapperPermissions.incrementAndGet();
                thread.start();
            }
            else
            {
                selector.wakeup();
                sk = ch.register(selector, ops);
            }
            MultiProviderSelectionKey mpsk = new MultiProviderSelectionKey(this, sk);
            mpsk.attach(att);
            keys.add(mpsk);
            keyMap.put(sk, mpsk);
            return mpsk;
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public Set<SelectionKey> keys()
    {
        return unmodifiableKeys;
    }

    @Override
    public Set<SelectionKey> selectedKeys()
    {
        return selectedKeys;
    }

    @Override
    public int selectNow() throws IOException
    {
        int res = 0;
        for (Selector selector : map.values())
        {
            res += selector.selectNow();
        }
        return res;
    }

    @Override
    public int select(long timeout) throws IOException
    {
        int keyCount = 0;
        try
        {
            lock.lock();
            try
            {
                wrapperSemaphore.release(wrapperPermissions.getAndSet(0));
                if (ioException != null)
                {
                    throw ioException;
                }
                handleCancelled();
                if (keyPool.isEmpty())
                {
                    wait = true;
                }
                else
                {
                    keyCount = provision();
                }
            }
            finally
            {
                lock.unlock();
            }
            if (wait)
            {
                long nanoTime1 = System.nanoTime();
                if (semaphore.tryAcquire(timeout, TimeUnit.MILLISECONDS))
                {
                    long nanoTime2 = System.nanoTime();
                    log.fine("waited %d nanos", nanoTime2-nanoTime1);
                    keyCount = provision();
                    handleCancelled();
                }
                wait = false;
            }
            log.fine("select() -> %d", keyCount);
            return keyCount;
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    private void handleCancelled()
    {
        lock.lock();
        try
        {
            Set<SelectionKey> cancelledKeys = cancelledKeys();
            synchronized(cancelledKeys)
            {
                Iterator<SelectionKey> iterator = cancelledKeys.iterator();
                while (iterator.hasNext())
                {
                    MultiProviderSelectionKey sk = (MultiProviderSelectionKey) iterator.next();
                    deregister(sk);
                    keys.remove(sk);
                    keyMap.remove(sk.getRealSelectionKey());
                    sk.doCancel();
                    iterator.remove();
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }
    private int provision()
    {
        int count = 0;
        lock.lock();
        try
        {
            for (SelectionKey sk : keyPool)
            {
                MultiProviderSelectionKey mpsk = keyMap.get(sk);
                if (mpsk != null)
                {
                    if (!selectedKeys.contains(mpsk))
                    {
                        selectedKeys.add(mpsk);
                        count++;
                    }
                }
                else
                {
                    log.fine("selectionKey=null");
                }
            }
            keyPool.clear();
            return count;
        }
        finally
        {
            lock.unlock();
        }
    }
    @Override
    public int select() throws IOException
    {
        return select(-1);
    }

    private class SelectorWrapper extends JavaLogging implements Runnable
    {
        Selector selector;

        public SelectorWrapper(Selector selector)
        {
            this.selector = selector;
            setLogger(this.getClass());
        }

        @Override
        public void run()
        {
            while (isOpen())
            {
                lock.lock();
                try
                {
                    if (selector.keys().isEmpty())
                    {
                        info("stop selector thread");
                        map.remove(selector.provider());
                        wrapperMap.remove(selector);
                        threadMap.remove(selector);
                        return;
                    }
                }
                finally
                {
                    lock.unlock();
                }
                int count = 0;
                IOException ioExc = null;
                try
                {
                    wrapperSemaphore.acquire();
                    count = selector.select();
                    fine("select(%s)=%d", this, count);
                }
                catch (IOException ex)
                {
                    log(Level.SEVERE, ex, "IOException(%s)", toString());
                    ioExc = ex;
                }
                catch (InterruptedException ex)
                {
                    Logger.getLogger(MultiProviderSelector.class.getName()).log(Level.SEVERE, null, ex);
                }
                lock.lock();
                try
                {
                    if (ioExc == null)
                    {
                        Set<SelectionKey> keys = selector.selectedKeys();
                        keyPool.addAll(keys);
                        keys.clear();
                        wrapperPermissions.incrementAndGet();
                    }
                    else
                    {
                        ioException = ioExc;
                    }
                }
                finally
                {
                    if (wait)
                    {
                        semaphore.release();
                    }
                    lock.unlock();
                }
            }
        }

        @Override
        public String toString()
        {
            return "SelectorWrapper{" + "selector=" + selector + '}';
        }
        
    }
}
