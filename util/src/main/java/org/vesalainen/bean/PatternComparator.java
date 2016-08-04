/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.bean;

import java.util.Comparator;

/**
 * A comparator that puts patterns in following order: assign, add, normal and remove.
 * Patterns which have same category are sorted in natural order. This means that
 * this comparator is consistent with equals.
 * 
 * <p>Intended use is to sort apply patterns so that bean changes are consistent.
 * @author tkv
 */
public class PatternComparator implements Comparator<String>
{
    public static final PatternComparator Comparator = new PatternComparator();
    
    @Override
    public int compare(String o1, String o2)
    {
        int t1 = type(o1);
        int t2 = type(o2);
        if (t1 == t2)
        {
            if (t1 == 2)
            {
                return o1.compareTo(o2);
            }
            else
            {
                return BeanHelper.applyPrefix(o2).compareTo(BeanHelper.applyPrefix(o1));
            }
        }
        else
        {
            return t1 - t2;
        }
    }
    
    private int type(String p)
    {
        if (BeanHelper.isAssign(p))
        {
            return 0;
        }
        if (BeanHelper.isAdd(p))
        {
            return 1;
        }
        if (BeanHelper.isRemove(p))
        {
            return 3;
        }
        return 2;
    }
}
