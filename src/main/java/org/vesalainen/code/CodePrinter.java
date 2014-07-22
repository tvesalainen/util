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
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 *
 * @author Timo Vesalainen
 */
public class CodePrinter
{
    private final String Indent = "    ";
    
    private final CharSequence indent;
    private final Appendable appendable;
    private final CharSequence suffix;
    private boolean indented;
    private boolean flushed;
    private CodePrinter parent;
    private CodePrinter sub;

    public CodePrinter(Appendable appendable)
    {
        this(null, "", appendable, "");
    }

    private CodePrinter(CodePrinter parent, CharSequence indent, Appendable appendable, CharSequence suffix)
    {
        this.parent = parent;
        this.indent = indent;
        this.appendable = appendable;
        this.suffix = suffix;
    }

    public CodePrinter createMethod(EnumSet<Modifier> modifiers, ExecutableElement method) throws IOException
    {
        println();
        for (Modifier m : modifiers)
        {
            print(m.name().toLowerCase()+" ");
        }
        TypeMirror returnType = method.getReturnType();
        print(returnType+" ");
        print(method.getSimpleName()+"(");
        ListPrinter<VariableElement> vl = new ListPrinter<VariableElement>(this, ", ", method.getParameters())
        {
            @Override
            protected void print(int index, VariableElement item) throws IOException
            {
                print(item.asType().toString()+" ");
                print(item.getSimpleName());
            }
        };
        vl.print();
        print(")");
        List<? extends TypeMirror> thrownTypes = method.getThrownTypes();
        if (!thrownTypes.isEmpty())
        {
            print(" throws ");
            print(", ", thrownTypes);
        }
        println();
        println("{");
        return createSub("}");
    }
    public CodePrinter createClass(EnumSet<Modifier> modifiers, CharSequence name, TypeElement superClass, TypeElement... interfaces) throws IOException
    {
        println();
        for (Modifier m : modifiers)
        {
            print(m.name().toLowerCase()+" ");
        }
        print("class ");
        print(name+" ");
        if (superClass != null)
        {
            print("extends "+superClass.getQualifiedName());
        }
        if (interfaces != null && interfaces.length > 0)
        {
            print(" implements ");
            ListPrinter<TypeElement> elp = new ListPrinter<TypeElement>(this, ", ", interfaces) 
            {
                @Override
                protected void print(int index, TypeElement item) throws IOException
                {
                    print(item.getSimpleName());
                }
            };
            elp.print();
        }
        println();
        println("{");
        return createSub("}");
    }
    public CodePrinter createSub(CharSequence suffix)
    {
        sub = new CodePrinter(this, indent+Indent, appendable, suffix);
        return sub;
    }
    void flush() throws IOException
    {
        if (!flushed)
        {
            if (parent != null)
            {
                parent.println();
                parent.println(suffix);
                flushed = true;
            }
        }
    }
    public <T> void print(CharSequence separator, Collection<T> items) throws IOException
    {
        boolean first = true;
        for (T c : items)
        {
            if (!first)
            {
                print(separator);
            }
            first = false;
            print(c.toString());
        }
    }
    public <T> void print(CharSequence separator, T[] items) throws IOException
    {
        boolean first = true;
        for (T c : items)
        {
            if (!first)
            {
                print(separator);
            }
            first = false;
            print(c.toString());
        }
    }
    public void println(CharSequence str) throws IOException
    {
        print(str);
        appendable.append('\n');
        indented = false;
    }

    public void print(CharSequence str) throws IOException
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
