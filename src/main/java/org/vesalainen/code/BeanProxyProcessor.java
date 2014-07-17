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

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.EnumSet;
import static javax.lang.model.element.Modifier.*;

/**
 *
 * @author Timo Vesalainen
 */
public class BeanProxyProcessor
{
    public void process(Class<? extends BeanProxy> cls) throws IOException
    {
        CodePrinter mp = new CodePrinter(System.err);
        BeanProxyClass annotation = cls.getAnnotation(BeanProxyClass.class);
        if (annotation == null)
        {
            throw new IllegalArgumentException("@"+BeanProxyClass.class.getSimpleName()+" missing in cls");
        }
        String value = annotation.value();
        int idx = value.lastIndexOf('.');
        String classname = value.substring(idx+1);
        String pgk = value.substring(0, idx);
        
        mp.println("package "+pgk+";");
        CodePrinter cp = mp.createClass(EnumSet.of(PUBLIC), classname, cls);
        
        for (Class<?> intf : cls.getInterfaces())
        {
            for (Method m : intf.getDeclaredMethods())
            {
                CodePrinter cm = cp.createMethod(EnumSet.of(PUBLIC), m);
                String name = m.getName();
                if (name.startsWith("get") || name.startsWith("set"))
                {
                    
                }
                else
                {
                    cm.println("// ");
                    cm.println("throw new UnsupportedOperationException(\"not supported.\");");
                }
                cm.flush(cp);
            }
        }
        cp.flush(mp);
    }
    
    @BeanProxyClass("org.vesalainen.code.BImpl")
    public abstract class B extends BeanProxy implements I
    {
        
    }
    public interface I
    {
        void setXYZ(int i);
        void setXYZ(double d);
        
        boolean getFlag();
        void test(String s) throws Exception;
    }
    public static void main(String... args)
    {
        try
        {
            BeanProxyProcessor bpp = new BeanProxyProcessor();
            bpp.process(B.class);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
