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
import java.lang.reflect.Method;

/**
 * AbstractIntProvisioner is a feature to provision values to @IntSetting annotated methods. When 
 class T is attached, it's public methods are searched to find methods annotated
 with @IntSetting annotation. Method has to have one parameters and it is 
 practical to have return type void. In attach phase each annotated method is 
 called with a value returned from getValue method if that value is other than
 null.
 
 <p>After attach phase calls to setValue method will provision named values
 * to all attached methods.
 * 
 * @author Timo Vesalainen
 * @param <T>
 */
public abstract class AbstractIntProvisioner<T> extends AbstractProvisioner<T,Integer,Object>
{

    @Override
    public Integer getKey(Method method)
    {
        IntSetting setting = method.getAnnotation(IntSetting.class);
        if (setting != null)
        {
            return setting.value();
        }
        return null;
    }
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface IntSetting
    {
        int value();
    }
}
