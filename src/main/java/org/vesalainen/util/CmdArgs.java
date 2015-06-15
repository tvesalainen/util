/*
 * Copyright (C) 2015 tkv
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author tkv
 */
public class CmdArgs
{
    private final Map<String,Option> map = new HashMap<>();
    private final MapList<String,Option> groups = new HashMapList<>();
    private final List<Class<?>> types = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    private Map<String,Object> options;
    private Object[] rest;
    private String activeGroup;
    /**
     * Creates CmdArgs instance. 
     */
    public CmdArgs()
    {
    }
    
    public void setArgs(String... args)
    {
        options = new HashMap<>();
        Option opt = null;
        int index = 0;
        for (String arg : args)
        {
            if (opt != null)
            {
                options.put(opt.name, ConvertUtility.convert(opt.cls, arg));
                opt = null;
            }
            else
            {
                if (map.containsKey(arg))
                {
                    opt = map.get(arg);
                    if (opt.exclusiveGroup != null)
                    {
                        if (activeGroup != null && !activeGroup.equals(opt.exclusiveGroup))
                        {
                            throw new IllegalArgumentException(opt.exclusiveGroup+" mix with "+activeGroup);
                        }
                        activeGroup = opt.exclusiveGroup;
                    }
                }
                else
                {
                    break;
                }
            }
            index++;
        }
        int len = args.length-index;
        if (len != types.size())
        {
            throw new IllegalArgumentException("wrong number of arguments");
        }
        rest = new Object[len];
        for (int ii=0;ii<len;ii++)
        {
            rest[ii] = ConvertUtility.convert(types.get(ii), args[ii+index]);
        }
        for (Option o : map.values())
        {
            if (o.mandatory && !options.containsKey(o.name))
            {
                throw new IllegalArgumentException("mandatory option "+o.name+": "+o.description+" missing");
            }
        }
    }
    public Object[] getRest()
    {
        return rest;
    }
    public <T> T getOption(String name)
    {
        if (rest == null)
        {
            throw new IllegalStateException("setArgs not called");
        }
        Object value = options.get(name);
        if (value == null)
        {
            throw new IllegalArgumentException(name+" not found");
        }
        return (T) value;
    }
    /**
     * Add String argument
     * @param name 
     */
    public void addArgument(String name)
    {
        addArgument(String.class, name);
    }
    /**
     * Add typed argument
     * @param <T>
     * @param cls
     * @param name 
     */
    public <T> void addArgument(Class<T> cls, String name)
    {
        types.add(cls);
        names.add(name);
    }
    public <T> void addOption(Class<T> cls, String name, String description)
    {
        addOption(cls, name, description, null);
    }
    /**
     * Add a mandatory option
     * @param <T> Type of option
     * @param cls Option type class
     * @param name Option name Option name without '-'
     * @param description Option description
     * @param exclusiveGroup A group of options. Only options of a single group 
     * are accepted.
     */
    public <T> void addOption(Class<T> cls, String name, String description, String exclusiveGroup)
    {
        Option opt = new Option(cls, name, description, exclusiveGroup);
        Option old = map.put("-"+name, opt);
        if (old != null)
        {
            throw new IllegalArgumentException(name+" was already added");
        }
        if (exclusiveGroup != null)
        {
            groups.add(exclusiveGroup, opt);
        }
    }
    public <T> void addOption(String name, String description, T defValue)
    {
        addOption(name, description, null, defValue);
    }
    /**
     * Add a option
     * @param <T>
     * @param name Option name Option name without '-'
     * @param description Option description
     * @param exclusiveGroup A group of options. Only options of a single group 
     * are accepted.
     * @param defValue Option default value
     */
    public <T> void addOption(String name, String description, String exclusiveGroup, T defValue)
    {
        Option opt = new Option(name, description, exclusiveGroup, defValue);
        Option old = map.put("-"+name, opt);
        if (old != null)
        {
            throw new IllegalArgumentException(name+" was already added");
        }
        if (exclusiveGroup != null)
        {
            groups.add(exclusiveGroup, opt);
        }
    }

    public String getUsage()
    {
        Set<Option> set = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        sb.append("usage: ");
        boolean n1 = false;
        for (Entry<String,List<Option>> e : groups.entrySet())
        {
            if (n1)
            {
                sb.append("|");
            }
            n1 = true;
            sb.append("[");
            boolean n2 = false;
            for (Option opt : e.getValue())
            {
                if (n2)
                {
                    sb.append(" ");
                }
                n2 = true;
                append(sb, opt);
                set.add(opt);
            }
            sb.append("]");
        }
        for (Option opt : map.values())
        {
            if (!set.contains(opt))
            {
                sb.append(" ");
                append(sb, opt);
            }
        }
        for (String n : names)
        {
            sb.append(" <").append(n).append(">");
        }
        return sb.toString();
    }
    private void append(StringBuilder sb, Option opt)
    {
        sb.append("-").append(opt.name).append(" ");
        sb.append("<").append(opt.description).append(">");
    }
    public class Option<T>
    {
        private final Class<T> cls;
        private final String name;
        private final String description;
        private final String exclusiveGroup;
        private boolean mandatory;
        private T defValue;

        public Option(Class<T> cls, String name, String description, String exclusiveGroup)
        {
            this.cls = cls;
            this.name = name;
            this.description = description;
            this.exclusiveGroup = exclusiveGroup;
            mandatory = true;
        }

        public Option(String name, String description, String exclusiveGroup, T defValue)
        {
            this.name = name;
            this.description = description;
            this.defValue = defValue;
            this.exclusiveGroup = exclusiveGroup;
            this.cls = (Class<T>) defValue.getClass();
        }
        
        public T getValue(String str)
        {
            return (T) ConvertUtility.convert(cls, str);
        }
    }
}
