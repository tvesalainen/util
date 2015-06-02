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
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Semaphore;

/**
 *
 * @author tkv
 */
public class MultiProviderSelector extends AbstractSelector
{
    private final Map<SelectorProvider,Selector> map = new HashMap<>();
    private final Map<Selector,SelectorWrapper> wrapperMap = new HashMap<>();
    private final Map<Selector,Thread> threadMap = new HashMap<>();
    private final UnionSet<SelectionKey> keys = new UnionSet<>();
    private final UnionSet<SelectionKey> selectedKeys = new UnionSet<>();
    private final Semaphore semaphore = new Semaphore(0);
    
    public MultiProviderSelector()
    {
        super(MultiSelectorProvider.provider());
    }

    public Selector getSelectorFor(SelectableChannel channel)
    {
        return getSelectorFor(channel.provider());
    }
    public Selector getSelectorFor(SelectorProvider provider)
    {
        return map.get(provider);
    }
    @Override
    protected void implCloseSelector() throws IOException
    {
        for (Selector selector : map.values())
        {
            keys.remove(selector.keys());
            selectedKeys.remove(selector.selectedKeys());
            wrapperMap.remove(selector);
            Thread thread = threadMap.get(selector);
            thread.interrupt();
            threadMap.remove(selector);
            selector.close();
        }
    }

    @Override
    protected SelectionKey register(AbstractSelectableChannel ch, int ops, Object att)
    {
        try
        {
            SelectorProvider provider = ch.provider();
            Selector selector = map.get(provider);
            if (selector == null)
            {
                selector = provider.openSelector();
                map.put(provider, selector);
                keys.add(selector.keys());
                selectedKeys.add(selector.selectedKeys());
                SelectorWrapper sw = new SelectorWrapper(selector);
                wrapperMap.put(selector, sw);
                Thread thread = new Thread(sw);
                threadMap.put(selector, thread);
                thread.start();
            }
            return ch.register(selector, ops, att);
        }
        catch (IOException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public Set<SelectionKey> keys()
    {
        return keys;
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
        try
        {
            int res = 0;
            for (SelectorWrapper sw : wrapperMap.values())
            {
                sw.timeout = timeout;
                sw.selectorSemaphore.release();
            }
            semaphore.acquire();
            for (SelectorWrapper sw : wrapperMap.values())
            {
                sw.wakeup();
            }
            semaphore.acquire(wrapperMap.size()-1);
            for (SelectorWrapper sw : wrapperMap.values())
            {
                res += sw.returnValue;
            }
            return res;
        }
        catch (InterruptedException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public int select() throws IOException
    {
        return select(-1);
    }

    @Override
    public Selector wakeup()
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private class SelectorWrapper implements Runnable
    {
        Semaphore selectorSemaphore = new Semaphore(0);
        Selector selector;
        long timeout;
        int returnValue;
        volatile boolean selecting;

        public SelectorWrapper(Selector selector)
        {
            this.selector = selector;
        }

        public void wakeup()
        {
            if (selecting)
            {
              selector.wakeup();
            }   
        }
        @Override
        public void run()
        {
            while (true)
            {
                try
                {
                    selectorSemaphore.acquire();
                    selecting = true;
                    if (timeout > 0)
                    {
                        returnValue = selector.select(timeout);
                    }
                    else
                    {
                        returnValue = selector.select();
                    }
                    semaphore.release();
                    selecting = false;
                }
                catch (InterruptedException ex)
                {
                    return;
                }
                catch (IOException ex)
                {
                    throw new IllegalArgumentException(ex);
                }
            }
        }
    }
    public class UnionSet<T> implements Set<T>
    {
        private final Set<Set<T>> sets = new HashSet<>();
        
        private void add(Set<T> set)
        {
            sets.add(set);
        }
        private void remove(Set<T> set)
        {
            sets.remove(set);
        }
        @Override
        public int size()
        {
            int size = 0;
            for (Set<T> set : sets)
            {
                size += set.size();
            }
            return size;
        }

        @Override
        public boolean isEmpty()
        {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o)
        {
            for (Set<T> set : sets)
            {
                if (set.contains(o))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Iterator<T> iterator()
        {
            return new UnionIterator(sets.iterator());
        }

        @Override
        public Object[] toArray()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public <T> T[] toArray(T[] a)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean add(T e)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean remove(Object o)
        {
            for (Set<T> set : sets)
            {
                if (set.remove(o))
                {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean addAll(Collection<? extends T> c)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean retainAll(Collection<?> c)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeAll(Collection<?> c)
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public void clear()
        {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        
    }
    public class UnionIterator<T> implements Iterator<T>
    {
        private Iterator<Set<T>> setIterator;
        private Iterator<T> iterator;

        public UnionIterator(Iterator<Set<T>> setIterator)
        {
            this.setIterator = setIterator;
            if (setIterator.hasNext())
            {
                iterator = setIterator.next().iterator();
            }
        }
        
        @Override
        public boolean hasNext()
        {
            if (iterator != null)
            {
                if (iterator.hasNext())
                {
                    return true;
                }
                else
                {
                    if (setIterator.hasNext())
                    {
                        iterator = setIterator.next().iterator();
                        return hasNext();
                    }
                    else
                    {
                        iterator = null;
                        return false;
                    }
                }
            }
            else
            {
                return false;
            }
        }

        @Override
        public T next()
        {
            if (iterator != null)
            {
                return iterator.next();
            }
            else
            {
                throw new NoSuchElementException();
            }
        }

        @Override
        public void remove()
        {
            iterator.remove();
        }
        
    }
    
}
