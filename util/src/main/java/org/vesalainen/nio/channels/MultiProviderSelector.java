/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import org.vesalainen.util.concurrent.ConcurrentArraySet;
import org.vesalainen.util.logging.JavaLogging;

/**
 * a bridge between selectors from different providers.
 * 
 * <p>Note! You cannot use SelectableChannel register method. Instead use 
 * MultiProviderSelector.register.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MultiProviderSelector extends AbstractSelector
{
    private Map<SelectorProvider,Selector> map = new HashMap<>();
    private Set<SelectionKey> keys = new ConcurrentArraySet<>();
    private final Set<SelectionKey> unmodifiableKeys = Collections.unmodifiableSet(keys);
    private Set<SelectionKey> selectedKeys = new ConcurrentArraySet<>();
    private final Map<SelectionKey,MultiProviderSelectionKey> keyMap = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final JavaLogging log;
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final List<SelectorWrapper> callables = new ArrayList<>();
    
    public MultiProviderSelector()
    {
        super(MultiSelectorProvider.provider());
        log = new JavaLogging(this.getClass());
    }

    @Override
    public Selector wakeup()
    {
        log.fine("wakeup(%s", this);
        for (Selector selector : map.values())
        {
            selector.wakeup();
        }
        return this;
    }

    @Override
    protected void implCloseSelector() throws IOException
    {
        log.fine("close(%s", this);
        for (Selector selector : map.values())
        {
            selector.close();
        }
        map = null;
        keys = null;
        selectedKeys = null;
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
                sk = ch.register(selector, ops);
                callables.add(new SelectorWrapper(selector));
            }
            else
            {
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
        int count = 0;
        lock.lock();
        try
        {
            handleCancelled();
            for (Selector selector : map.values())
            {
                selector.selectNow();
                Set<SelectionKey> sks = selector.selectedKeys();
                for (SelectionKey sk : sks)
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
                        log.warning("%s: MultiProviderSelectionKey=null", sk);
                    }
                }
                sks.clear();
            }
            log.finest("selectNow return keyCount= %d", count);
            return count;
        }
        finally
        {
            lock.unlock();
        }
    }

    @Override
    public int select(long timeout) throws IOException
    {
        log.fine("select: enter select(%d)", timeout);
        int sn = selectNow();
        if (sn > 0)
        {
            log.fine("select: satisfied with selectNow() returns %d keys", sn);
            return sn;
        }
        int keyCount = 0;
        try
        {
            lock.lock();
            try
            {
                handleCancelled();
                Iterator<SelectorWrapper> iterator = callables.iterator();
                while (iterator.hasNext())
                {
                    SelectorWrapper sw = iterator.next();
                    if (sw.selector.keys().isEmpty())
                    {
                        log.fine("removing selector %s because it has no keys anymore", sw.selector);
                        map.remove(sw.selector.provider());
                        iterator.remove();
                    }
                }
            }
            finally
            {
                lock.unlock();
            }
            log.fine("selector: invoking %d selectors", callables.size());
            Selector selector = executor.invokeAny(callables, timeout, TimeUnit.MILLISECONDS);
            log.fine("selector: %s success with %d keys", selector, selector.selectedKeys().size());
            Set<SelectionKey> sks = selector.selectedKeys();
            for (SelectionKey sk : sks)
            {
                MultiProviderSelectionKey mpsk = keyMap.get(sk);
                if (mpsk != null)
                {
                    if (!selectedKeys.contains(mpsk))
                    {
                        selectedKeys.add(mpsk);
                        keyCount++;
                    }
                }
                else
                {
                    log.warning("%s: MultiProviderSelectionKey=null", sk);
                }
            }
            sks.clear();
            return keyCount;
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
        catch (ExecutionException | TimeoutException ex)
        {
            return 0;
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
                    log.fine("select: cancel(%s)", sk);
                    deregister(sk);
                    if (!keys.remove(sk))
                    {
                        log.warning("%s not in %s", sk, keys);
                    }
                    if (keyMap.remove(sk.getRealSelectionKey()) == null)
                    {
                        log.warning("%s not in %s", sk.getRealSelectionKey(), keyMap);
                    }
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
    @Override
    public int select() throws IOException
    {
        return select(-1);
    }

    private class SelectorWrapper extends JavaLogging implements Callable<Selector>
    {
        Selector selector;

        public SelectorWrapper(Selector selector)
        {
            this.selector = selector;
            setLogger(this.getClass());
        }


        @Override
        public Selector call() throws Exception
        {
            try
            {
                log.fine("selector: start %s", selector);
                selector.select();
                log.fine("selector: end %s", selector);
                return selector;
            }
            catch (Exception ex)
            {
                log.log(Level.INFO, ex, "%s", ex.getMessage());
                throw ex;
            }
        }
        
        @Override
        public String toString()
        {
            return "SelectorWrapper{" + "selector=" + selector + '}';
        }

    }
}
