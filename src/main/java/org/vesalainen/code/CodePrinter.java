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
import javax.lang.model.element.Modifier;
import org.vesalainen.io.AppendablePrinter;

/**
 *
 * @author Timo Vesalainen
 */
public class CodePrinter
{
    private final String indent;
    private final Appendable appendable;
    private final String suffix;
    private boolean indented;
    private boolean flushed;
    private CodePrinter sub;

    public CodePrinter(Appendable appendable)
    {
        this("", appendable, "");
    }

    private CodePrinter(String indent, Appendable appendable, String suffix)
    {
        this.indent = indent;
        this.appendable = appendable;
        this.suffix = suffix;
    }

    public CodePrinter createMethod(EnumSet<Modifier> modifiers, Method method) throws IOException
    {
        println();
        for (Modifier m : modifiers)
        {
            print(m.name().toLowerCase()+" ");
        }
        print(method.getReturnType().getSimpleName()+" ");
        print(method.getName()+" ");
        Class<?>[] thr = method.getExceptionTypes();
        if (thr.length > 0)
        {
            print("throws ");
            print(", ", thr);
        }
        println();
        println("{");
        return createSub(indent+"    ", "}");
    }
    public CodePrinter createClass(EnumSet<Modifier> modifiers, CharSequence name, Class<?>... sup) throws IOException
    {
        println();
        for (Modifier m : modifiers)
        {
            print(m.name().toLowerCase()+" ");
        }
        print(name+" ");
        if (sup.length > 0)
        {
            print("extends ");
            print(", ", sup);
        }
        println();
        println("{");
        return createSub(indent+"    ", "}");
    }
    public CodePrinter createSub(String indention, String suffix)
    {
        sub = new CodePrinter(indention, appendable, suffix);
        return sub;
    }
    void flush(CodePrinter cp) throws IOException
    {
        if (!flushed)
        {
            cp.println();
            cp.println(suffix);
            flushed = true;
        }
    }
    public void print(String separator, Class<?>... classes) throws IOException
    {
        boolean first = true;
        for (Class<?> c : classes)
        {
            if (!first)
            {
                print(separator);
            }
            first = false;
            print(c.getSimpleName());
        }
    }
    public void println(String str) throws IOException
    {
        print(str);
        appendable.append('\n');
        indented = false;
    }

    public void print(String str) throws IOException
    {
        indent();
        appendable.append(str);
    }

    private void indent() throws IOException
    {
        if (!indented)
        {
            appendable.append(indent);
            indented = true;
        }
    }

    private void println() throws IOException
    {
        if (indented)
        {
            println("");
        }
    }
    
}
