/*
 * Copyright (C) 2015 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.vesalainen.util.logging.AttachedLogger;
import org.vesalainen.util.logging.JavaLogging;

/**
 * CmdArgs handles command argument processing. Command line has options and 
 * arguments.
 * <p>E.g.
 * <p>
 * <code> -f filename -s description argument1 argument2</code>
 * <p>Options 1 is named '-f'. If it's type is java.io.File it is converted to
 * file.
 * <p>Option 2 is names '-s'
 * <p>Argument 1 
 * <p>
 * Options can be grouped in exclusive group. E.g. hostname and port can be in one group and 
 * filename in another. Only options of one exclusive group are allowed.
 * <p>
 * Last argument can be array. In that case any number of arguments are 
 * allowed.
 * <p>
 * Option and argument values are available after setArgs method. Options are 
 * also available for attachments.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class CmdArgs extends AbstractProvisioner implements AttachedLogger
{
    private final Map<String,Option> map = new HashMap<>();
    private final MapList<String,Option> groups = new HashMapList<>();
    private final List<Class<?>> types = new ArrayList<>();
    private final List<String> names = new ArrayList<>();
    private Map<String,Object> options;
    private Map<String,Object> arguments;
    private String effectiveGroup;
    private boolean hasArrayArgument;
    private JavaLogging log;
    /**
     * Creates CmdArgs instance. 
     */
    public CmdArgs()
    {
        log = new JavaLogging(CmdArgs.class);
    }
    /**
     * Returns named option or argument value
     * @param name
     * @return 
     */
    @Override
    public Object getValue(String name)
    {
        if (arguments == null)
        {
            throw new IllegalStateException("setArgs not called");
        }
        Object ob = options.get(name);
        if (ob != null)
        {
            return ob;
        }
        return arguments.get(name);
    }
    /**
     * Initializes options and arguments. Reports error and exits on error.
     * Called usually from main method.
     * @param args
     */
    public void command(String... args)
    {
        try
        {
            setArgs(args);
        }
        catch (CmdArgsException ex)
        {
            ex.printStackTrace();
            Logger logger = Logger.getLogger(CmdArgs.class.getName());
            logger.log(Level.SEVERE, Arrays.toString(args));
            logger.log(Level.SEVERE, ex.getMessage(), ex);
            logger.log(Level.SEVERE, ex.usage());
            System.exit(-1);
        }
    }
    /**
     * Initializes options and arguments. Called usually from command or main method.
     * @param args
     * @throws CmdArgsException 
     */
    public final void setArgs(String... args) throws CmdArgsException
    {
        try
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
                            if (effectiveGroup != null && !effectiveGroup.equals(opt.exclusiveGroup))
                            {
                                throw new CmdArgsException(opt.exclusiveGroup+" mix with "+effectiveGroup, this);
                            }
                            effectiveGroup = opt.exclusiveGroup;
                        }
                    }
                    else
                    {
                        break;
                    }
                }
                index++;
            }
            // defaults
            for (Option o : map.values())
            {
                if (!o.mandatory && !options.containsKey(o.name))
                {
                    options.put(o.name, o.defValue);
                }
            }
            int len = args.length-index;
            if (hasArrayArgument)
            {
                if (len < types.size())
                {
                    throw new CmdArgsException("too few arguments", this);
                }
            }
            else
            {
                if (len != types.size())
                {
                    throw new CmdArgsException("wrong number of arguments", this);
                }
            }
            arguments = new HashMap<>();
            int arraySize = len - types.size() + 1;
            int arrayIndex = Integer.MAX_VALUE;
            Object array = null;
            Class<?> componenType = null;
            if (hasArrayArgument)
            {
                arrayIndex = types.size()-1;
                Class<?> arrayType = types.get(arrayIndex);
                componenType = arrayType.getComponentType();
                array = Array.newInstance(componenType, arraySize);
                arguments.put(names.get(arrayIndex), array);
            }
            for (int ii=0;ii<len;ii++)
            {
                if (ii < arrayIndex)
                {
                    Object value = ConvertUtility.convert(types.get(ii), args[ii+index]);
                    arguments.put(names.get(ii), value);
                }
                else
                {
                    Object value = ConvertUtility.convert(componenType, args[ii+index]);
                    Array.set(array, ii - arrayIndex, value);
                }
            }
            for (Option o : map.values())
            {
                if (o.mandatory && !options.containsKey(o.name))
                {
                    throw new CmdArgsException("mandatory option "+o.name+": "+o.description+" missing", this);
                }
            }
            if (effectiveGroup != null)
            {
                for (Option o : groups.get(effectiveGroup))
                {
                    if (o.mandatory && !options.containsKey(o.name))
                    {
                        throw new CmdArgsException(effectiveGroup+" option "+o.name+": "+o.description+" missing", this);
                    }
                }
            }
        }
        catch (Exception ex)
        {
            throw new CmdArgsException(ex, this);
        }
    }
    /**
     * Returns the effective group from exclusive groups which has values or null
     * if none has values.
     * @return 
     */
    public final String getEffectiveGroup()
    {
        if (arguments == null)
        {
            throw new IllegalStateException("setArgs not called");
        }
        return effectiveGroup;
    }
    /**
     * Returns named argument value
     * @param <T>
     * @param name
     * @return 
     */
    public final <T> T getArgument(String name)
    {
        if (arguments == null)
        {
            throw new IllegalStateException("setArgs not called");
        }
        return (T) arguments.get(name);
    }
    /**
     * Return named option value.
     * @param <T>
     * @param name
     * @return 
     */
    public final <T> T getOption(String name)
    {
        if (arguments == null)
        {
            throw new IllegalStateException("setArgs not called");
        }
        Object value = options.get(name);
        if (value == null)
        {
            Option opt = map.get(name);
            if (opt == null)
            {
                throw new IllegalArgumentException("option "+name+" not found");
            }
            if (opt.defValue != null)
            {
                return (T) opt.defValue;
            }
            if (!opt.mandatory)
            {
                return null;
            }
            throw new IllegalArgumentException(name+" not found");
        }
        return (T) value;
    }
    /**
     * Add String argument
     * @param name 
     */
    public final void addArgument(String name)
    {
        addArgument(String.class, name);
    }
    /**
     * Add typed argument
     * @param <T>
     * @param cls
     * @param name 
     */
    public final <T> void addArgument(Class<T> cls, String name)
    {
        if (map.containsKey(name))
        {
            throw new IllegalArgumentException(name+" is already added as option");
        }
        if (hasArrayArgument)
        {
            throw new IllegalArgumentException("no argument allowed after array argument");
        }
        types.add(cls);
        names.add(name);
        if (cls.isArray())
        {
            hasArrayArgument = true;
        }
    }
    /**
     * Add a mandatory string option
     * @param <T> Type of option
     * @param name Option name Option name without
     * @param description Option description
     */
    public final <T> void addOption(String name, String description)
    {
        addOption(String.class, name, description, null);
    }
    /**
     * Add a mandatory option
     * @param <T> Type of option
     * @param cls Option type class
     * @param name Option name Option name without
     * @param description Option description
     */
    public final <T> void addOption(Class<T> cls, String name, String description)
    {
        addOption(cls, name, description, null);
    }
    /**
     * Add a mandatory option
     * @param <T> Type of option
     * @param cls Option type class
     * @param name Option name Option name without
     * @param description Option description
     * @param exclusiveGroup A group of options. Only options of a single group 
     * are accepted.
     */
    public final <T> void addOption(Class<T> cls, String name, String description, String exclusiveGroup)
    {
        addOption(cls, name, description, exclusiveGroup, true);
    }
    /**
     * Add an option
     * @param <T> Type of option
     * @param cls Option type class
     * @param name Option name Option name without
     * @param description Option description
     * @param exclusiveGroup A group of options. Only options of a single group 
     * are accepted.
     * @param mandatory 
     */
    public final <T> void addOption(Class<T> cls, String name, String description, String exclusiveGroup, boolean mandatory)
    {
        if (names.contains(name))
        {
            throw new IllegalArgumentException(name+" is already added as argument");
        }
        Option opt = new Option(cls, name, description, exclusiveGroup, mandatory);
        Option old = map.put(name, opt);
        if (old != null)
        {
            throw new IllegalArgumentException(name+" was already added");
        }
        if (exclusiveGroup != null)
        {
            groups.add(exclusiveGroup, opt);
        }
    }
    /**
     * Add an option
     * @param <T>
     * @param name Option name Option name without
     * @param description Option description
     * @param exclusiveGroup A group of options. Only options of a single group 
     * are accepted.
     * @param defValue Option default value
     */
    public final <T> void addOption(String name, String description, String exclusiveGroup, T defValue)
    {
        Option opt = new Option(name, description, exclusiveGroup, defValue);
        Option old = map.put(name, opt);
        if (old != null)
        {
            throw new IllegalArgumentException(name+" was already added");
        }
        if (exclusiveGroup != null)
        {
            groups.add(exclusiveGroup, opt);
        }
    }
    /**
     * Returns  usage string.
     * @return 
     */
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
        for (int ii=0;ii<names.size();ii++)
        {
            sb.append(" <").append(names.get(ii)).append(">");
            if (types.get(ii).isArray())
            {
                sb.append("...");
            }
        }
        return sb.toString();
    }
    private void append(StringBuilder sb, Option opt)
    {
        sb.append(opt.name).append(" ");
        sb.append("<").append(opt.description).append(">");
    }

    private void throwIt(Throwable thr) throws Throwable
    {
        log.severe("cmd: %s", thr.getMessage());
        throw thr;
    }
    private class Option<T>
    {
        private final Class<T> cls;
        private final String name;
        private final String description;
        private final String exclusiveGroup;
        private boolean mandatory;
        private T defValue;

        public Option(Class<T> cls, String name, String description, String exclusiveGroup, boolean mandatory)
        {
            this.cls = cls;
            this.name = name;
            this.description = description;
            this.exclusiveGroup = exclusiveGroup;
            this.mandatory = mandatory;
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
