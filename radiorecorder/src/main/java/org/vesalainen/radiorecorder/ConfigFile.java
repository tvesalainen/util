/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.radiorecorder;

import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import javax.xml.bind.JAXBElement;
import org.vesalainen.radiorecorder.jaxb.ObjectFactory;
import org.vesalainen.radiorecorder.jaxb.RadioRecorderType;
import org.vesalainen.util.AbstractConfigFile;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class ConfigFile extends AbstractConfigFile<RadioRecorderType,ObjectFactory>
{

    public ConfigFile(Path file)
    {
        super("org.vesalainen.radiorecorder.jaxb", file);
    }

    public ConfigFile(File file)
    {
        super("org.vesalainen.radiorecorder.jaxb", file);
    }

    public ConfigFile(URL url)
    {
        super("org.vesalainen.radiorecorder.jaxb", url);
    }

    @Override
    protected JAXBElement<RadioRecorderType> create()
    {
        return objectFactory.createRadioRecorder(objectFactory.createRadioRecorderType());
    }

}
