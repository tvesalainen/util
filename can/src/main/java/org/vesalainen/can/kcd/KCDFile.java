/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can.kcd;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.can.jaxb.NetworkDefinition;
import org.vesalainen.can.jaxb.ObjectFactory;
import org.vesalainen.util.AbstractConfigFile;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class KCDFile
{
    public static final String CONTEXT_PATH = "org.vesalainen.can.jaxb";
    
    protected ObjectFactory objectFactory;
    protected DatatypeFactory datatypeFactory;
    protected JAXBContext jaxbCtx;
    protected URL url;
    private NetworkDefinition content;

    public KCDFile(Path path) throws MalformedURLException
    {
        this(path.toUri().toURL());
    }

    public KCDFile(File path) throws MalformedURLException
    {
        this(path.toURI().toURL());
    }

    public KCDFile(URL url)
    {
        try
        {
            this.jaxbCtx = JAXBContext.newInstance(CONTEXT_PATH);
            this.datatypeFactory = DatatypeFactory.newInstance();
            this.objectFactory = new ObjectFactory();
            this.url = url;
            Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
            content =  (NetworkDefinition) unmarshaller.unmarshal(url);
        }
        catch (JAXBException | DatatypeConfigurationException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    
}
