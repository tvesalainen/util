/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.vesalainen.util;

/**
 * Interface for classes supporting conversion between T and C types.
 * T type is usually the type of implementing class and type C is most
 * often String
 * @author tkv
 */
public interface Convertable<T,C>
{
    /**
     * Convert this object to object type of C
     * @return
     */
    C convertTo();
}
