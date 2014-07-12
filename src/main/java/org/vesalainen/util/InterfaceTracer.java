/*
 * Copyright (C) 2014 Timo Vesalainen
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

package org.vesalainen.util;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * A class able to trace interface execution.
 * 
 * <p>Creates a proxy class for interface, with or without class instance. Method
 * trace is printed either System.err or a given class implementing Appendable.
 * 
 * <p>This class is thread safe.
 * 
 * @author Timo Vesalainen
 * @see java.lang.Appendable
 * @see java.lang.reflect.Proxy
 */
public class InterfaceTracer implements InvocationHandler
{
    /**
     * Class instance for given interface or null
     */
    protected Object ob;
    /**
     * Printer
     */
    protected AppendablePrinter printer;
    /**
     * Creates a InterfaceTracer
     * @param ob Class instance for given interface or null
     */
    protected InterfaceTracer(Object ob)
    {
        this.ob = ob;
    }
    /**
     * Creates a tracer for intf
     * @param <T>
     * @param intf Implemented interface
     * @param ob Class instance for given interface or null
     * @return 
     */
    public static <T> T getTracer(Class<T> intf, T ob)
    {
        return getTracer(intf, new InterfaceTracer(ob), ob);
    }
    /**
     * Creates a tracer for intf. This is meant to be used by subclass.
     * @param <T>
     * @param intf Implemented interface
     * @param ob Class instance for given interface or null
     * @return 
     */
    protected static <T> T getTracer(Class<T> intf, InterfaceTracer tracer, T ob)
    {
        tracer.setAppendable(System.err);
        return (T) Proxy.newProxyInstance(
                intf.getClassLoader(), 
                new Class<?>[] {intf}, 
                tracer);
    }
    /**
     * Creates a tracer for intf
     * @param <T>
     * @param intf Implemented interface
     * @param ob Class instance for given interface or null
     * @param appendable Output for trace
     * @return 
     */
    public static <T> T getTracer(Class<T> intf, T ob, Appendable appendable)
    {
        return getTracer(intf, new InterfaceTracer(ob), ob, appendable);
    }
    /**
     * Creates a tracer for intf. This is meant to be used by subclass.
     * @param <T>
     * @param intf Implemented interface
     * @param ob Class instance for given interface or null
     * @param appendable Output for trace
     * @return 
     */
    protected static <T> T getTracer(Class<T> intf, InterfaceTracer tracer, T ob, Appendable appendable)
    {
        tracer.setAppendable(appendable);
        return (T) Proxy.newProxyInstance(
                intf.getClassLoader(), 
                new Class<?>[] {intf}, 
                tracer);
    }

    private void setAppendable(Appendable appendable)
    {
        this.printer = new AppendablePrinter(appendable);
    }

    @Override
    public synchronized Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        Object res = null;
        if (ob != null)
        {
            res = method.invoke(ob, args);
        }
        printer.print(method.getName());
        printer.print("(");
        boolean first = true;
        for (Object arg : args)
        {
            if (!first)
            {
                printer.print(", ");
            }
            printer.print(arg);
            first = false;
        }
        printer.print(")");
        if (ob != null && !Void.TYPE.equals(method.getReturnType()))
        {
            printer.println(" = "+res);
        }
        else
        {
            printer.println();
        }
        return res;
    }
    
}
