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
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import static javax.lang.model.element.ElementKind.*;
import javax.lang.model.element.ExecutableElement;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import static javax.lang.model.type.TypeKind.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 *
 * @author Timo Vesalainen
 */
@SupportedAnnotationTypes({
    "org.vesalainen.code.BeanProxyClass",
    "org.vesalainen.code.TransactionalSetterClass"
})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class Processor extends AbstractProcessor
{
    private static final Set<String> classnames = new HashSet<>();
    private static final AtomicInteger sequence = new AtomicInteger();
    private Elements elements;
    private Types types;

    @Override
    public synchronized void init(ProcessingEnvironment pe)
    {
        super.init(pe);
        elements = pe.getElementUtils();
        types = pe.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        Messager msg = processingEnv.getMessager();
        for (TypeElement te : annotations)
        {
            for (Element e : roundEnv.getElementsAnnotatedWith(te))
            {
                TypeElement type = (TypeElement) e;
                try
                {
                    msg.printMessage(Diagnostic.Kind.NOTE, "processing", type);
                    System.err.println("processing "+type);
                    switch (te.getQualifiedName().toString())
                    {
                        case "org.vesalainen.code.BeanProxyClass":
                            generateBeanProxy(type, processingEnv);
                            break;
                        case "org.vesalainen.code.TransactionalSetterClass":
                            generateTransactionalSetter(type, processingEnv);
                            break;
                        default:
                            throw new UnsupportedOperationException(te.getQualifiedName().toString()+" not supported");
                    }
                }
                catch (Exception ex)
                {
                    ex.printStackTrace();
                    String m = ex.getMessage();
                    m = m != null ? m : ex.toString();
                    msg.printMessage(Diagnostic.Kind.ERROR, m, e);
                }
            }
        }
        return true;
    }

    private void generateTransactionalSetter(TypeElement cls, ProcessingEnvironment processingEnv) throws IOException
    {
        TransactionalSetterClass annotation = cls.getAnnotation(TransactionalSetterClass.class);
        if (annotation == null)
        {
            throw new IllegalArgumentException("@"+TransactionalSetterClass.class.getSimpleName()+" missing in cls");
        }
        Filer filer = processingEnv.getFiler();
        String value = annotation.value();
        JavaFileObject sourceFile = filer.createSourceFile(value);
        try (Writer writer = sourceFile.openWriter())
        {
            CodePrinter mp = new CodePrinter(writer);
            int idx = value.lastIndexOf('.');
            String classname = value.substring(idx+1);
            String pgk = value.substring(0, idx);

            List<? extends ExecutableElement> methods = getMethods(cls);
            mp.println("package "+pgk+";");
            mp.println("import javax.annotation.Generated;");
            mp.println("@Generated(");
            mp.println("\tvalue=\""+Processor.class.getCanonicalName()+"\"");
            mp.println("\t, comments=\"Generated for "+cls+"\"");
            Date date = new Date();
            mp.println("\t, date=\""+date+"\"");
            mp.println(")");
            CodePrinter cp = mp.createClass(EnumSet.of(PUBLIC), classname, cls);
            Set<String> en = new TreeSet<>();
            for (ExecutableElement m : methods)
            {
                List<? extends VariableElement> parameters = m.getParameters();
                TypeMirror returnType = m.getReturnType();
                String name = m.getSimpleName().toString();
                if (
                        name.startsWith("set") && 
                        parameters.size() == 1 &&
                        returnType.getKind() == VOID
                        )
                {
                    en.add(getEnum(name));
                }
            }
            cp.print("private enum Prop {");
            cp.print(", ", en);
            cp.println("};");
            for (ExecutableElement m : methods)
            {
                List<? extends VariableElement> parameters = m.getParameters();
                TypeMirror returnType = m.getReturnType();
                cp.println("@Override");
                CodePrinter cm = cp.createMethod(EnumSet.of(PUBLIC), m);
                String name = m.getSimpleName().toString();
                String enumname = getEnum(name);
                if (
                        name.startsWith("set") && 
                        parameters.size() == 1 &&
                        returnType.getKind() == VOID
                        )
                {
                    VariableElement ve = parameters.get(0);
                    cm.println("set(Prop."+enumname+".ordinal(), "+ve.getSimpleName()+");");
                }
                else
                {
                    cm.println("throw new UnsupportedOperationException(\"not supported.\");");
                }
                cm.flush(cp);
            }
            cp.flush(mp);
        }
    }
    private void generateBeanProxy(TypeElement cls, ProcessingEnvironment processingEnv) throws IOException
    {
        BeanProxyClass annotation = cls.getAnnotation(BeanProxyClass.class);
        if (annotation == null)
        {
            throw new IllegalArgumentException("@"+BeanProxyClass.class.getSimpleName()+" missing in cls");
        }
        Filer filer = processingEnv.getFiler();
        String value = annotation.value();
        JavaFileObject sourceFile = filer.createSourceFile(value);
        try (Writer writer = sourceFile.openWriter())
        {
            CodePrinter mp = new CodePrinter(writer);
            int idx = value.lastIndexOf('.');
            String classname = value.substring(idx+1);
            String pgk = value.substring(0, idx);

            List<? extends ExecutableElement> methods = getMethods(cls);
            mp.println("package "+pgk+";");
            mp.println("import javax.annotation.Generated;");
            mp.println("@Generated(");
            mp.println("\tvalue=\""+Processor.class.getCanonicalName()+"\"");
            mp.println("\t, comments=\"Generated for "+cls+"\"");
            Date date = new Date();
            mp.println("\t, date=\""+date+"\"");
            mp.println(")");
            CodePrinter cp = mp.createClass(EnumSet.of(PUBLIC), classname, cls);
            for (ExecutableElement m : methods)
            {
                List<? extends VariableElement> parameters = m.getParameters();
                TypeMirror returnType = m.getReturnType();
                cp.println("@Override");
                CodePrinter cm = cp.createMethod(EnumSet.of(PUBLIC), m);
                String name = m.getSimpleName().toString();
                String property = getProperty(name);
                if (
                        name.startsWith("set") && 
                        parameters.size() == 1 &&
                        returnType.getKind() == VOID
                        )
                {
                    VariableElement ve = parameters.get(0);
                    cm.println("set(\""+property+"\", "+ve.getSimpleName()+");");
                }
                else
                {
                    if (
                            name.startsWith("get") && 
                            parameters.isEmpty() &&
                            returnType.getKind() != VOID
                            )
                    {
                        cm.println("return "+cast(returnType)+getter(returnType.getKind())+"(\""+property+"\");");
                    }
                    else
                    {
                        cm.println("throw new UnsupportedOperationException(\"not supported.\");");
                    }
                }
                cm.flush(cp);
            }
            cp.flush(mp);
        }
    }

    private List<? extends ExecutableElement> getMethods(TypeElement cls)
    {
        List<ExecutableElement> list = new ArrayList<>();
        for (TypeMirror intf : cls.getInterfaces())
        {
            DeclaredType dt = (DeclaredType) intf;
            TypeElement te = (TypeElement) dt.asElement();
            if (te.getKind() == INTERFACE)
            {
                for (ExecutableElement m : ElementFilter.methodsIn(elements.getAllMembers(te)))
                {
                    if (m.getModifiers().contains(ABSTRACT))
                    {
                        list.add(m);
                    }
                }
            }
        }
        return list;
    }

    private synchronized String getUniqueClassname(String name)
    {
        String res = name;
        while (classnames.contains(res))
        {
            res = name+sequence.incrementAndGet();
        }
        classnames.add(res);
        return res;
    }
    
    private Set<CharSequence> getImports(List<? extends ExecutableElement> methods)
    {
        Set<CharSequence> set = new TreeSet<>();
        for (ExecutableElement ee : methods)
        {
            add(set, ee.getReturnType());
            for (TypeMirror tm : ee.getThrownTypes())
            {
                add(set, tm);
            }
            for (VariableElement ve : ee.getParameters())
            {
                add(set, ve.asType());
            }
        }
        return set;
    }
    private static void add(Set<CharSequence> set, TypeMirror tm)
    {
        if (tm.getKind() == DECLARED)
        {
            String cl = tm.toString();
            int idx = cl.lastIndexOf('.');
            if (idx != -1)
            {
                if ("java.lang".equals(cl.substring(0, idx)))
                {
                    return;
                }
            }
            set.add(cl);
        }
    }

    private String getProperty(String name)
    {
        return Character.toLowerCase(name.charAt(3))+name.substring(4);
    }

    private String getEnum(String name)
    {
        return name.substring(3);
    }

    private String getter(TypeKind kind)
    {
        return "get"+getTypename(kind);
    }

    private String getTypename(TypeKind kind)
    {
        switch (kind)
        {
            case ARRAY:
            case DECLARED:
                return "Object";
            case BOOLEAN:
                return "Boolean";
            case BYTE:
                return "Byte";
            case CHAR:
                return "Char";
            case DOUBLE:
                return "Double";
            case FLOAT:
                return "Float";
            case INT:
                return "Int";
            case LONG:
                return "Long";
            case SHORT:
                return "Short";
            default:
                throw new IllegalArgumentException("not type "+kind);
        }
    }

    private String cast(TypeMirror tm)
    {
        switch (tm.getKind())
        {
            case ARRAY:
            case DECLARED:
                return "("+tm.toString()+")";
            default:
                return "";
        }
    }

}
