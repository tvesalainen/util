/*
 * Copyright (C) 2017 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.math;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 * @see <a href="https://en.wikipedia.org/wiki/Set_theory">Set theory</a>
 */
public final class Sets
{
    /**
     * Union of the sets A and B, denoted A ∪ B, is the set of all objects that 
     * are a member of A, or B, or both
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> Set<T> union(Set<T>... sets)
    {
        Set<T> set = new HashSet<>();
        for (Set<T> s : sets)
        {
            set.addAll(s);
        }
        return set;
    }
    /**
     * Intersection of the sets A and B, denoted A ∩ B, is the set of all 
     * objects that are members of both A and B
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> Set<T> intersection(Set<T>... sets)
    {
        Set<T> set = union(sets);
        for (Set<T> s : sets)
        {
            set.retainAll(s);
        }
        return set;
    }
    /**
     * return true if intersection is not empty
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> boolean intersect(Set<T>... sets)
    {
        Set<T> set = intersection(sets);
        return !set.isEmpty();
    }
    /**
     * Set difference of U and A, denoted U \ A, is the set of all members of U 
     * that are not members of A
     * @param <T>
     * @param u
     * @param a
     * @return 
     */
    public static final <T> Set<T> difference(Set<T> u, Set<T> a)
    {
        Set<T> set = new HashSet<>(u);
        set.removeAll(a);
        return set;
    }
    /**
     * Symmetric difference of sets A and B, denoted A △ B or A ⊖ B, 
     * is the set of all objects that are a member of exactly one of A and B 
     * (elements which are in one of the sets, but not in both)
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> Set<T> symmetricDifference(Set<T>... sets)
    {
        return difference(union(sets), intersection(sets));
    }
    /**
     * Union of the sets A and B, denoted A ∪ B, is the set of all objects that 
     * are a member of A, or B, or both
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> Set<T> union(Collection<Set<T>> sets)
    {
        Set<T> set = new HashSet<>();
        for (Set<T> s : sets)
        {
            set.addAll(s);
        }
        return set;
    }
    /**
     * Intersection of the sets A and B, denoted A ∩ B, is the set of all 
     * objects that are members of both A and B
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> Set<T> intersection(Collection<Set<T>> sets)
    {
        Set<T> set = union(sets);
        for (Set<T> s : sets)
        {
            set.retainAll(s);
        }
        return set;
    }
    /**
     * return true if intersection is not empty
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> boolean intersect(Collection<Set<T>> sets)
    {
        Set<T> set = intersection(sets);
        return !set.isEmpty();
    }
    /**
     * Symmetric difference of sets A and B, denoted A △ B or A ⊖ B, 
     * is the set of all objects that are a member of exactly one of A and B 
     * (elements which are in one of the sets, but not in both)
     * @param <T>
     * @param sets
     * @return 
     */
    public static final <T> Set<T> symmetricDifference(Collection<Set<T>> sets)
    {
        return difference(union(sets), intersection(sets));
    }
    /**
     * Sets target content to be the same as source without clearing the target.
     * @param <T>
     * @param source
     * @param target 
     */
    public static final <T> void assign(Set<T> source, Set<T> target)
    {
        target.retainAll(source);
        target.addAll(source);
    }
}
