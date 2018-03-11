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
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.vesalainen.util.HashMapSet;
import org.vesalainen.util.CollectionHelp;
import org.vesalainen.util.MapSet;

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
        return symmetricDifference(CollectionHelp.create(sets));
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
        MapSet<T,Integer> mapSet = new HashMapSet<>();
        int index = 0;
        for (Set<T> set : sets)
        {
            for (T t : set)
            {
                mapSet.add(t, index);
            }
            index++;
        }
        final Set<T> diff = new HashSet<>();
        mapSet.forEach((t,s)->{if (s.size() == 1) diff.add(t);});
        return diff;
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
     * Cartesian product of A and B, denoted A × B, is the set whose members are 
     * all possible ordered pairs (a, b) where a is a member of A and b is a 
     * member of B.
     * @param <A>
     * @param <B>
     * @param a
     * @param b
     * @return 
     */
    public static final <A,B> Set<OrderedPair<A,B>> cartesianProduct(Set<A> a, Set<B> b)
    {
        Set<OrderedPair<A,B>> set = new HashSet<>();
        for (A t : a)
        {
            for (B v : b)
            {
                set.add(new OrderedPair(t, v));
            }
        }
        return set;
    }
    /**
     * Power set of a set A is the set whose members are all possible subsets of A.
     * @param <T>
     * @param set
     * @return 
     */
    public static final <T> Set<Set<T>> powerSet(Set<T> set)
    {
        Set<Set<T>> powerSet = new HashSet<>();
        powerSet.add(Collections.EMPTY_SET);
        powerSet(powerSet, set);
        return powerSet;
    }
    private static <T> void powerSet(Set<Set<T>> powerSet, Set<T> set)
    {
        if (!set.isEmpty())
        {
            powerSet.add(set);
            for (T t : set)
            {
                Set<T> workSet = new HashSet<>(set);
                workSet.remove(t);
                powerSet(powerSet, workSet);
            }
        }
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
