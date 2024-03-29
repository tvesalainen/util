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
package org.vesalainen.modbus;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Service
{
    private final String name;
    private final Map<String,Register> map = new TreeMap<>();

    public Service(String name)
    {
        this.name = name;
    }
    
    void add(Register register)
    {
        map.put(register.getPath(), register);
    }
    
    public Register getRegister(String path)
    {
        return map.get(path);
    }
    
    public void forEach(BiConsumer<? super String, ?super Register> act)
    {
        map.forEach(act);
    }
}
