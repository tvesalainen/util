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

/**
 * AbstractStringProvisioner is a feature to provision values to @StringSetting annotated methods. When 
 class T is attached, it's public methods are searched to find methods annotated
 with @StringSetting annotation. Method has to have one parameters and it is 
 practical to have return type void. In attach phase each annotated method is 
 called with a value returned from getValue method if that value is other than
 null.
 
 <p>After attach phase calls to setValue method will provision named values
 * to all attached methods.
 * 
 * @author Timo Vesalainen
 * @param <T>
 */
public abstract class AbstractStringProvisioner<T> extends AbstractProvisioner<T,String,Object>
{

    @Override
    public String getKey(Method method)
    {
        StringSetting setting = method.getAnnotation(StringSetting.class);
        if (setting != null)
        {
            return setting.value();
        }
        return null;
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface StringSetting
    {
        String value();
    }
}
