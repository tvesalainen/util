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
package org.vesalainen.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Path;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import static javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import org.vesalainen.bean.BeanHelper;

/**
 * AbstractConfigFile is to be used as base class of xml-configuration binding.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @param <T> Xml type
 * @param <F> ObjectFactory
 */
public abstract class AbstractConfigFile<T,F> extends AbstractProvisioner
{
    protected String contextPath;
    protected JAXBElement<T> content;
    protected F objectFactory;
    protected DatatypeFactory datatypeFactory;
    protected JAXBContext jaxbCtx;
    protected URL url;
    /**
     * Creates AbstractConfigFile, sets contextPath and file path.
     * Either call createNew or load next.
     * @param contextPath
     * @param file 
     */
    public AbstractConfigFile(String contextPath, Path file)
    {
        this(contextPath, toURL(file));
    }
    /**
     * Creates AbstractConfigFile, sets contextPath and file path.
     * Either call createNew or load next.
     * @param contextPath
     * @param file 
     */
    public AbstractConfigFile(String contextPath, File file)
    {
        this(contextPath, toURL(file));
    }
    /**
     * Creates AbstractConfigFile, sets contextPath and url.
     * Either call createNew or load next.
     * @param contextPath
     * @param url 
     */
    public AbstractConfigFile(String contextPath, URL url)
    {
        try
        {
            this.contextPath = contextPath;
            this.jaxbCtx = JAXBContext.newInstance(contextPath);
            this.datatypeFactory = DatatypeFactory.newInstance();
            this.objectFactory = createObjectFactory();
            this.url = url;
        }
        catch (JAXBException | DatatypeConfigurationException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    /**
     * Returns JAXB content
     * @return 
     */
    public T getContent()
    {
        return content.getValue();
    }
    
    /**
     * Creates new xml-structure.
     */
    public void createNew()
    {
        content = create();
    }
    /**
     * Loads xml-content from url/file
     * @throws IOException
     * @throws JAXBException 
     */
    public void load() throws IOException, JAXBException
    {
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        content =  (JAXBElement<T>) unmarshaller.unmarshal(url);
    }
    /**
     * Stores contents if url is a file otherwise throws IllegalArgumentException
     * @throws IOException 
     */
    public void store() throws IOException
    {
        try {
            File file = new File(url.toURI());
            storeAs(file);
        }
        catch (URISyntaxException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    /**
     * Stores xml-content to file. This doesn't change stored url/file.
     * @param file
     * @throws IOException 
     */
    public void storeAs(Path file) throws IOException
    {
        storeAs(file.toFile());
    }
    /**
     * Stores xml-content to file. This doesn't change stored url/file.
     * @param file
     * @throws IOException 
     */
    public void storeAs(File file) throws IOException
    {
        try (FileOutputStream fos = new FileOutputStream(file);
                OutputStreamWriter osw = new OutputStreamWriter(fos, UTF_8);
                BufferedWriter bw = new BufferedWriter(osw))
        {
            store(bw);
        }
    }
    /**
     * Stores xml-content to stored url/file.
     * @param writer
     * @throws IOException 
     */
    public synchronized void store(Writer writer) throws IOException
    {
        try
        {
            Marshaller marshaller = jaxbCtx.createMarshaller();
            marshaller.setProperty(JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(content, writer);
        }
        catch (JAXBException ex)
        {
            throw new IOException(ex);
        }
    }
    /**
     * Sub-class must implement create method using objectFactory.
     * @return 
     */
    protected abstract JAXBElement<T> create();
    private static URL toURL(Path file)
    {
        try
        {
            return file.toUri().toURL();
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private static URL toURL(File file)
    {
        try
        {
            return file.toURI().toURL();
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    private F createObjectFactory()
    {
        try
        {
            Class<F> cls = (Class<F>) Class.forName(contextPath+".ObjectFactory");
            return cls.newInstance();
        }
        catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public Object getValue(String name)
    {
        if (content != null)
        {
            return BeanHelper.getValue(content.getValue(), name);
        }
        return null;
    }

}
