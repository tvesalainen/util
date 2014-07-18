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
import java.util.EnumSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import static javax.lang.model.element.Modifier.*;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
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
        Elements elements = processingEnv.getElementUtils();
        Types types = processingEnv.getTypeUtils();
        BeanProxyClass annotation = cls.getAnnotation(BeanProxyClass.class);
        if (annotation == null)
        {
            throw new IllegalArgumentException("@"+BeanProxyClass.class.getSimpleName()+" missing in cls");
        }
        Filer filer = processingEnv.getFiler();
        String value = annotation.value();
        JavaFileObject sourceFile = filer.createSourceFile(value);
        CodePrinter mp = new CodePrinter(sourceFile.openWriter());
        int idx = value.lastIndexOf('.');
        String classname = value.substring(idx+1);
        String pgk = value.substring(0, idx);
        
        mp.println("package "+pgk+";");
        CodePrinter cp = mp.createClass(EnumSet.of(PUBLIC), classname, cls);
        
        for (TypeMirror intf : cls.getInterfaces())
        {
            DeclaredType dt = (DeclaredType) intf;
            TypeElement te = (TypeElement) dt.asElement();
            for (ExecutableElement m : ElementFilter.methodsIn(elements.getAllMembers(te)))
            {
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
        }
        cp.flush(mp);
    }

}
