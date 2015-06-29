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

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A Bijection between T and S
 * @author tkv
 * @param <T>
 * @param <S>
 */
public interface Bijection<T,S>
{
    void put(T t, S s);
    T getFirst(S s);
    S getSecond(T t);
    boolean containsFirst(S s);
    boolean containsSecond(T t);
    Set<Map.Entry<T, S>> entrySet();
    Set<T> firstSet();
    Set<S> secondSet();
}
