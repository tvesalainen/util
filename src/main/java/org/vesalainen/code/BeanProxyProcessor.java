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
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import static javax.lang.model.element.ElementKind.INTERFACE;
import javax.lang.model.element.ExecutableElement;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import static javax.lang.model.type.TypeKind.DECLARED;
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
@SupportedAnnotationTypes("org.vesalainen.code.BeanProxyClass")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class BeanProxyProcessor extends AbstractProcessor
{
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
                    generate(type, processingEnv);
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

    private void generate(TypeElement cls, ProcessingEnvironment processingEnv) throws IOException
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
            /*
            Set<CharSequence> imports = getImports(methods);
            for (CharSequence im : imports)
            {
                mp.println("import "+im+";");
            }
                    */
            mp.println("import javax.annotation.Generated;");
            mp.println("@Generated(\""+BeanProxyProcessor.class.getCanonicalName()+"\")");
            CodePrinter cp = mp.createClass(EnumSet.of(PUBLIC), classname, cls);
            for (ExecutableElement m : methods)
            {
                mp.println("@Override");
                CodePrinter cm = cp.createMethod(EnumSet.of(PUBLIC), m);
                String name = m.getSimpleName().toString();
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
                    TypeElement ee = (TypeElement) m.getEnclosingElement();
                    if (!ee.getQualifiedName().contentEquals("java.lang.Object"))
                    {
                        list.add(m);
                    }
                }
            }
        }
        return list;
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
}
