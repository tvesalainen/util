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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tkv
 */
public class CmdArgs
{
    private Map<String,Option> map = new HashMap<>();
    private String[] args;
    
    public void setArgs(String... args)
    {
        this.args = args;
    }
    public String[] getRest()
    {
        boolean found = false;
        int index = 0;
        for (String arg : args)
        {
            if (found)
            {
                found = false;
            }
            else
            {
                if (map.containsKey(arg))
                {
                    found = true;
                }
                else
                {
                    break;
                }
            }
            index++;
        }
        return Arrays.copyOfRange(args, index, args.length);
    }
    public <T> T getOption(char letter)
    {
        if (args == null)
        {
            throw new IllegalStateException("setArgs not called");
        }
        Option opt = map.get("-"+letter);
        if (opt == null)
        {
            throw new IllegalArgumentException(letter+" not found");
        }
        return (T) opt.getValue(getArg("-"+letter));
    }
    private String getArg(String flag)
    {
        boolean found = false;
        for (String arg : args)
        {
            if (found)
            {
                return arg;
            }
            if (flag.equals(arg))
            {
                found = true;
            }
        }
        throw new IllegalArgumentException(flag+" not found");
    }
    /**
     * Add a option
     * @param <T> Type of option
     * @param cls Option type class
     * @param letter Option letter
     * @param name Option name
     * @param description Option description
     */
    public <T> void addOption(Class<T> cls, char letter, String name, String description)
    {
        Option old = map.put("-"+letter, new Option(cls, letter, name, description));
        if (old != null)
        {
            throw new IllegalArgumentException(letter+" was already added");
        }
    }
    /**
     * Add a option
     * @param <T>
     * @param letter Option letter
     * @param name Option name
     * @param description Option description
     * @param defValue Option default value
     */
    public <T> void addOption(char letter, String name, String description, T defValue)
    {
        Option old = map.put("-"+letter, new Option(letter, name, description, defValue));
        if (old != null)
        {
            throw new IllegalArgumentException(letter+" was already added");
        }
    }
    public class Option<T>
    {
        private final Class<T> cls;
        private final char letter;
        private final String name;
        private final String description;
        private boolean mandatory;
        private T defValue;

        public Option(Class<T> cls, char letter, String name, String description)
        {
            this.cls = cls;
            this.name = name;
            this.letter = letter;
            this.description = description;
            mandatory = true;
        }

        public Option(char letter, String name, String description, T defValue)
        {
            this.letter = letter;
            this.name = name;
            this.description = description;
            this.defValue = defValue;
            this.cls = (Class<T>) defValue.getClass();
        }
        
        public T getValue(String str)
        {
            return (T) ConvertUtility.convert(cls, str);
        }
    }
}
