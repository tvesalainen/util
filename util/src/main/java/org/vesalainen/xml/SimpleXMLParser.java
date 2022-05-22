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
package org.vesalainen.xml;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SimpleXMLParser parses simple xml content. For example namespaces are not supported
 * <p>Implemented using SAXParser.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SimpleXMLParser
{
    private Element root;

    public SimpleXMLParser(Path path) throws IOException
    {
        this(Files.newBufferedReader(path));
    }
    
    public SimpleXMLParser(String input) throws IOException
    {
        this(new InputSource(new StringReader(input)));
    }

    public SimpleXMLParser(InputStream input) throws IOException
    {
        this(new InputSource(input));
    }

    public SimpleXMLParser(Reader input) throws IOException
    {
        this(new InputSource(input));
    }

    public SimpleXMLParser(InputSource input) throws IOException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            parser.parse(input, new Handler());
        }
        catch (ParserConfigurationException | SAXException ex)
        {
            throw new IOException(ex);
        }
        
    }
    /**
     * Returns the root element
     * @return 
     */
    public Element getRoot()
    {
        return root;
    }
    public static class Element
    {
        private String qName;
        private Map<String,String> attributes;
        private String text;
        private StringBuilder sb = new StringBuilder();
        private Map<String,Element> childMap = new HashMap<>();
        private List<Element> childList = new ArrayList<>();
        private boolean ambiquous;

        private Element(String qName, Map<String, String> attributes)
        {
            this.qName = qName;
            this.attributes = attributes;
        }
        /**
         * Returns elements at path
         * @param tags
         * @return 
         */
        public Collection<Element> getElements(String... tags)
        {
            List<Element> list = new ArrayList<>();
            getElements(list, 0, tags);
            return list;
        }
        private void getElements(List<Element> list, int index, String[] tags)
        {
            if (index == tags.length)
            {
                list.add(this);
            }
            else
            {
                String tag = tags[index];
                forEachChild((e)->
                {
                    if (tag.equals(e.getTag()))
                    {
                        e.getElements(list, index+1, tags);
                    }
                });
            }
        }
        public String getText(String... tags)
        {
            Element element = getElement(tags);
            if (element != null)
            {
                return element.getText();
            }
            return null;
        }
        /**
         * Returns element at path tags[0]/... or null if not found.
         * @param tags
         * @return 
         * @throws IllegalArgumentException if path contains several enries.
         */
        public Element getElement(String... tags)
        {
            return getElement(0, tags);
        }
        private Element getElement(int offset, String[] tags)
        {
            if (offset == tags.length)
            {
                return this;
            }
            Element element = childMap.get(tags[offset]);
            if (element == null)
            {
                return null;
            }
            if (element.ambiquous)
            {
                throw new IllegalArgumentException(tags[offset]+" ambiguous");
            }
            return element.getElement(offset+1, tags);
        }
        /**
         * Returns QName
         * @return 
         */
        public String getTag()
        {
            return qName;
        }
        /**
         * Return named attribute value
         * @param qName
         * @return 
         */
        public String getAttributeValue(String qName)
        {
            return attributes.get(qName);
        }
        /**
         * Returns unmodifiable map of attributes
         * @return 
         */
        public Map<String, String> getAttributes()
        {
            return attributes;
        }
        /**
         * Executes action for each attribute qName and value
         * @param action 
         */
        public void forEachAttribute(BiConsumer<? super String, ? super String> action)
        {
            attributes.forEach(action);
        }
        /**
         * Returns element text
         * @return 
         */
        public String getText()
        {
            return text;
        }
        /**
         * Executes action for each direct child element
         * @param action 
         */
        public void forEachChild(Consumer<? super Element> action)
        {
            childList.forEach((e)->action.accept(e));
        }
        /**
         * Executes action for all elements in hierarchy.
         * @param action 
         */
        public void walk(Consumer<? super Element> action)
        {
            action.accept(this);
            childList.forEach((e)->
            {
                e.walk(action);
            });
        }
        /**
         * Returns a stream of all elements in hierarchy.
         * <p>Note! Not lazy implementation!
         * @return 
         */
        public Stream<Element> stream()
        {
            return stream((e)->true);
        }
        /**
         * Returns a stream of all elements that fulfill predicate in hierarchy.
         * <p>Note! Not lazy implementation!
         * @return 
         */
        public Stream<Element> stream(Predicate<Element> predicate)
        {
            Stream.Builder<Element> builder = Stream.builder();
            walk((e)->
            {
                if (predicate.test(e))
                {
                    builder.accept(e);
                }
            });
            return builder.build();
        }
        /**
         * Returns unmodifiable list of direct child's
         * @return 
         */
        public List<Element> getChilds()
        {
            return childList;
        }

        private void addChild(Element element)
        {
            Element old = childMap.put(element.getTag(), element);
            if (old != null)
            {
                element.ambiquous = true;
                old.ambiquous = true;
            }
            childList.add(element);
        }
        private void addText(char[] ch, int start, int length)
        {
            sb.append(ch, start, length);
        }
        private void finish()
        {
            text = sb.toString().trim();
            sb = null;
            attributes = Collections.unmodifiableMap(attributes);
            childMap = Collections.unmodifiableMap(childMap);
            childList = Collections.unmodifiableList(childList);
        }
    }
    private class Handler extends DefaultHandler
    {
        private Deque<Element> stack = new ArrayDeque<>();
        
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
        {
            check(uri, localName);
            Element element = new Element(qName, toMap(attributes));
            if (stack.isEmpty())
            {
                root = element;
            }
            else
            {
                stack.peek().addChild(element);
            }
            stack.push(element);
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException
        {
            Element element = stack.pop();
            element.finish();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException
        {
            stack.peek().addText(ch, start, length);
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException
        {
            throw e;
        }

        @Override
        public void error(SAXParseException e) throws SAXException
        {
            throw e;
        }

        @Override
        public void warning(SAXParseException e) throws SAXException
        {
            throw e;
        }

        private Map<String, String> toMap(Attributes attributes)
        {
            Map<String,String> map = new HashMap<>();
            int length = attributes.getLength();
            for (int ii=0;ii<length;ii++)
            {
                //check(attributes.getURI(ii), attributes.getLocalName(ii));
                map.put(attributes.getQName(ii), attributes.getValue(ii));
            }
            return map;
        }

        private void check(String uri, String localName)
        {
            if (!uri.isEmpty() || !localName.isEmpty())
            {
                throw new UnsupportedOperationException("Namespace not supported.");
            }
        }
        
    }
}
