/*
 * Copyright (C) 2017 tkv
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
package org.vesalainen.pm.deb;

import java.nio.file.Path;
import static org.vesalainen.pm.deb.Field.*;

/**
 *
 * @author tkv
 */
public class Copyright extends ControlBase
{
    private Paragraph paragraph1;
    private Paragraph paragraph2;
    
    public Copyright(Path dir)
    {
        super(dir, "copyright", new Paragraph(), new Paragraph());
        paragraph1 = this.paragraphs.get(0);
        paragraph2 = this.paragraphs.get(1);
        paragraph1.add(Format_Specification, "http://svn.debian.org/wsvn/dep/web/deps/dep5.mdwn?op=file&rev=135");
    }
    public Copyright setName(String v)
    {
        paragraph1.add(Name, v);
        return this;
    }
    public Copyright setMaintainer(String v)
    {
        paragraph1.add(Maintainer, v);
        return this;
    }
    public Copyright setSource(String v)
    {
        paragraph1.add(Source, v);
        return this;
    }
    public Copyright setCopyright(String v)
    {
        paragraph2.add(Copyright, v);
        return this;
    }
    public Copyright setLicense(String v)
    {
        paragraph2.add(License, v);
        return this;
    }
    public Copyright addFile(String file, String copyright, String license)
    {
        Paragraph p = new Paragraph();
        paragraphs.add(p);
        p.add(Files, file);
        p.add(Copyright, copyright);
        p.add(License, license);
        return this;
    }
    
}
