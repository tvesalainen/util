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

package org.vesalainen.code;

/**
 * TransactionalSetter can be used to add transactions to setter type interface.
 * 
 * <p>Extend TransactionalSetter and declare implementing the target interface,
 * but don't implement the methods. Annotate the class with @TransactionalSetterClass.
 * Make the class abstract.
 * 
 * <p>Create class instance with getInstance method.
 * 
 * <p>Target interface doesn't have to support Transactional interface. However 
 * if it does, its commit/rollback methods are called.
 * 
 * <p>Example:
 * <code>
 * &#64;TransactionalSetterClass("org.vesalainen.code.TSImpl")
 * public abstract class TS extends TransactionalSetter implements TrIntf
 * {
 * 
 *     public TS(int[] sizes)
 *     {
 *         super(sizes);
 *     }
 * }
 * </code>
 * <code>
 *     TrIntfImpl tri = new TrIntfImpl();
 *     TS ts = TS.getInstance(TS.class, tri);
 * </code>
 * @author Timo Vesalainen
 * @see org.vesalainen.util.Transactional
 */
public abstract class TransactionalSetter extends AbstractDispatcher
{
    /**
     * Target interface.
     */
    protected Object intf;
    /**
     * Creates a TransactionalSetter. This is called by generated sub class.
     * @param sizes Defines how many method class per type can be stored. Default
     * implementation allows interfaces all methods being called once. Size per type
     * is in the same order as in JavaType enum.
     * @see org.vesalainen.code.JavaType
     */
    protected TransactionalSetter(int[] sizes)
    {
        super(sizes);
    }
    /**
     * Creates a instance of a class TransactionalSetter subclass.
     * @param <T> Type of TransactionalSetter subclass
     * @param cls TransactionalSetter subclass class
     * @param intf Interface implemented by TransactionalSetter subclass
     * @return 
     */
    public static <T extends TransactionalSetter> T getInstance(Class<T> cls, Object intf)
    {
        Class<?>[] interfaces = cls.getInterfaces();
        if (interfaces.length != 1)
        {
            throw new IllegalArgumentException(cls+" should implement exactly one interface");
        }
        boolean ok = false;
        if (!interfaces[0].isAssignableFrom(intf.getClass()))
        {
            throw new IllegalArgumentException(cls+" doesn't implement "+intf);
        }
        try
        {
            TransactionalSetterClass annotation = cls.getAnnotation(TransactionalSetterClass.class);
            if (annotation == null)
            {
                throw new IllegalArgumentException("@"+TransactionalSetterClass.class.getSimpleName()+" missing in cls");
            }
            Class<?> c = Class.forName(annotation.value());
            T t =(T) c.newInstance();
            t.intf = intf;
            return t;
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new IllegalArgumentException(ex);
        }
    }
    
}
