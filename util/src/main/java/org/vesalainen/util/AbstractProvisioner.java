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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.vesalainen.bean.BeanHelper;

/**
 * AbstractProvisioner provisions values to @Setting annotated methods. When 
 class T is attached it's public methods are searched to find methods annotated
 with @Setting annotation. Method has to have one parameters and it is 
 practical to have return type void. In attachInstant phase each annotated method is 
 called with a value returned from getValue method if that value is other than
 null.
 
 <p>After attachInstant phase calls to setValue method will provision named values
 to all attached methods.
 * 
 * @author Timo Vesalainen
 */
public abstract class AbstractProvisioner
{
    private final MapList<String,InstanceMethod> map = new HashMapList<>();

    public boolean isEmpty()
    {
        return map.isEmpty();
    }
    /**
     * Attaches class instances all methods which are annotated with @Setting for automatic
     * provisioning.
     * @param ob 
     */
    public void attachInstant(Object ob)
    {
        attach(ob.getClass(), ob);
    }
    public void attachStatic(Class<?> cls)
    {
        attach(cls, null);
    }                
    private void attach(Class<?> cls, Object ob)
    {
        for (Method method : cls.getMethods())
        {
            Setting setting = method.getAnnotation(Setting.class);
            if (setting != null)
            {
                String name = setting.value();
                if (name.isEmpty())
                {
                    name = BeanHelper.property(method.getName());
                }
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 1)
                {
                    throw new IllegalArgumentException("@Setting("+name+") argument count != 1");
                }
                InstanceMethod instanceMethod = new InstanceMethod(ob, method, setting.mandatory());
                Object value = getValue(name);
                if (value != null)
                {
                    instanceMethod.invoke(value);
                }
                map.add(name, instanceMethod);
            }
        }
    }
    /**
     * Removes all previously made attachments to given object.
     * @param ob 
     */
    public void detach(Object ob)
    {
        Iterator<Map.Entry<String, List<InstanceMethod>>> ki = map.entrySet().iterator();
        while (ki.hasNext())
        {
            Map.Entry<String, List<InstanceMethod>> entry = ki.next();
            Iterator<InstanceMethod> li = entry.getValue().iterator();
            while (li.hasNext())
            {
                InstanceMethod im = li.next();
                if (im.instance.equals(ob))
                {
                    li.remove();
                }
            }
            if (entry.getValue().isEmpty())
            {
                ki.remove();
            }
        }
    }
    /**
     * Removes all previously made attachments to static class.
     * @param cls 
     */
    public void detach(Class<?> cls)
    {
        Iterator<Map.Entry<String, List<InstanceMethod>>> ki = map.entrySet().iterator();
        while (ki.hasNext())
        {
            Map.Entry<String, List<InstanceMethod>> entry = ki.next();
            Iterator<InstanceMethod> li = entry.getValue().iterator();
            while (li.hasNext())
            {
                InstanceMethod im = li.next();
                if (im.type.equals(cls))
                {
                    li.remove();
                }
            }
            if (entry.getValue().isEmpty())
            {
                ki.remove();
            }
        }
    }
    /**
     * Provides value used in attachInstant process.
     * @param name
     * @return 
     */
    public abstract Object getValue(String name);
    /**
     * Provides value for attached method
     * @param name From @Setting
     * @param value 
     */
    public void setValue(String name, Object value)
    {
        map.get(name).stream().forEach((im) ->
        {
            im.invoke(value);
        });
    }
    /**
     * Checks that all mandatory values are assigned
     * @throws IllegalArgumentException if mandatory value is not set.
     */
    public void checkMandatory()
    {
        map.allValues().forEach((i)->i.check());
    }
    
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Setting
    {
        String value() default "";
        boolean mandatory() default false;
    }
    private class InstanceMethod
    {
        private final Object instance;
        private final Method method;
        private final Class<?> type;
        private final boolean mandatory;
        private boolean assigned;

        private InstanceMethod(Object instance, Method method, boolean mandatory)
        {
            this.instance = instance;
            this.method = method;
            this.mandatory = mandatory;
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != 1)
            {
                throw new IllegalArgumentException(method+" parameter count != 0");
            }
            this.type = parameterTypes[0];
        }
        
        private void check()
        {
            if (mandatory && !assigned)
            {
                throw new IllegalArgumentException("mandatory @Setting at "+instance+" "+method+" not assigned");
            }
        }
        private void invoke(Object arg)
        {
            try
            {
                method.invoke(instance, ConvertUtility.convert(type, arg));
                assigned = true;
            }
            catch (IllegalAccessException | InvocationTargetException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
    }
}
