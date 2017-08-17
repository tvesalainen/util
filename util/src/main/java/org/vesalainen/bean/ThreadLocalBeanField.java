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
package org.vesalainen.bean;

/**
 * BeanField implementation where base object comes from  ThreadLocal
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T> Base class type
 * @param <D> Field type
 */
public class ThreadLocalBeanField<T,D> extends AbstractBeanField<T,D>
{
    private final ThreadLocal<T> local;

    public ThreadLocalBeanField(ThreadLocal<T> local, T obj, String fieldname)
    {
        super(obj, fieldname);
        this.local = local;
    }

    public ThreadLocalBeanField(ThreadLocal<T> local, Class<? extends T> cls, String fieldname)
    {
        super(cls, fieldname);
        this.local = local;
    }
    
    @Override
    protected T getBase()
    {
        return local.get();
    }
    
}
