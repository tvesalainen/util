/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util;

/**
 *
 * @author tkv
 */
public class ConvertUtilityException extends RuntimeException
{

    /**
     * Creates a new instance of <code>ConvertUtilityException</code> without detail message.
     */
    public ConvertUtilityException()
    {
    }
    public ConvertUtilityException(Class<?> expectedReturnType, Object object, Throwable thr)
    {
        super("expectedReturnType="+expectedReturnType.getName()+" object="+object.getClass().getName(), thr);
    }

    /**
     * Constructs an instance of <code>ConvertUtilityException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public ConvertUtilityException(String msg)
    {
        super(msg);
    }
}
