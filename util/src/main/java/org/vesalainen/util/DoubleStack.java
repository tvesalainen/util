/*
 * Copyright (C) 2016 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import org.vesalainen.math.BasicMath;
import java.util.Arrays;
import java.util.EmptyStackException;
import org.vesalainen.math.Arithmetic;
import org.vesalainen.math.Conditional;

/**
 * DoubleStack implements stack operations for primitive double values. It also 
 * implements basic arithmetic stack operations.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class DoubleStack implements Arithmetic, BasicMath, Conditional
{
    public static final double TRUE = 1;
    public static final double FALSE = 0;
    private double[] stack;
    private float growFactor;
    private int top;
    /**
     * Creates DoubleStack with initialSize=16 and growFactor=1.5.
     */
    public DoubleStack()
    {
        this(16, 1.5F);
    }
    /**
     * Creates DoubleStack with initialSize and growFactor. When inner array
     * grows the new size is growFactor*current size.
     * @param initialSize
     * @param growFactor 
     */
    public DoubleStack(int initialSize, float growFactor)
    {
        if (growFactor <= 1 || growFactor > 2)
        {
            throw new IllegalArgumentException("growFactor must be in range 1.0 - 2.0");
        }
        stack = new double[initialSize];
        this.growFactor = growFactor;
    }
    /**
     * Adds value to the top.
     * @param value 
     */
    public void push(double value)
    {
        if (top >= stack.length)
        {
            stack = Arrays.copyOf(stack, (int) (stack.length*growFactor));
        }
        stack[top++] = value;
    }
    /**
     * Returns and removes top.
     * @return 
     */
    public double pop()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        return stack[--top];
    }
    /**
     * Returns top but doesn't remove.
     * @return 
     */
    public double peek()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        return stack[top-1];
    }
    /**
     * Returns value at index. No checks!
     * @param index
     * @return 
     */
    public double get(int index)
    {
        return stack[index];
    }
    /**
     * Returns number of elements in stack.
     * @return 
     */
    public int size()
    {
        return top;
    }
    /**
     * Sets stack pointer to 0.
     */
    public void clear()
    {
        top = 0;
    }
    /**
     * Return true is stack doesn't have any elements.
     * @return 
     */
    @Override
    public boolean isEmpty()
    {
        return top == 0;
    }
    /**
     * Top element is pushed.
     */
    @Override
    public void dup()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        if (top >= stack.length)
        {
            stack = Arrays.copyOf(stack, (int) (stack.length*growFactor));
        }
        top++;
        stack[top-1] = stack[top-2];
    }
    /**
     * Top element is popped and added to the new top element.
     */
    @Override
    public void add()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] += stack[top-1];
        top--;
    }
    /**
     * Top element is popped and divides the new top element.
     */
    @Override
    public void div()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] /= stack[top-1];
        top--;
    }
    /**
     * Top element is popped and mods the new top element.
     */
    @Override
    public void mod()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] %= stack[top-1];
        top--;
    }
    /**
     * Top element is popped and multiples the new top element.
     */
    @Override
    public void mul()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] *= stack[top-1];
        top--;
    }
    /**
     * Top elements sign is changed.
     */
    @Override
    public void neg()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = -stack[top-1];
    }
    /**
     * Top element is popped and subtracted from the new top element.
     */
    @Override
    public void subtract()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] -= stack[top-1];
        top--;
    }
    /**
     * @see java.lang.Math#abs(double) 
     */
    @Override
    public void abs()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.abs(stack[top-1]);
    }
    /**
     * @see java.lang.Math#acos(double) 
     */
    @Override
    public void acos()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.acos(stack[top-1]);
    }
    /**
     * @see java.lang.Math#asin(double) 
     */
    @Override
    public void asin()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.asin(stack[top-1]);
    }
    /**
     * @see java.lang.Math#atan(double) 
     */
    @Override
    public void atan()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.atan(stack[top-1]);
    }
    /**
     * @see java.lang.Math#atan2(double) 
     */
    @Override
    public void atan2()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] = Math.atan2(stack[top-2], stack[top-1]);
        top--;
    }
    /**
     * @see java.lang.Math#cbrt(double) 
     */
    @Override
    public void cbrt()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.cbrt(stack[top-1]);
    }
    /**
     * @see java.lang.Math#ceil(double) 
     */
    @Override
    public void ceil()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.ceil(stack[top-1]);
    }
    /**
     * @see java.lang.Math#cos(double) 
     */
    @Override
    public void cos()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.cos(stack[top-1]);
    }
    /**
     * @see java.lang.Math#cosh(double) 
     */
    @Override
    public void cosh()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.cosh(stack[top-1]);
    }
    /**
     * @see java.lang.Math#exp(double) 
     */
    @Override
    public void exp()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.exp(stack[top-1]);
    }
    /**
     * @see java.lang.Math#expm1(double) 
     */
    @Override
    public void expm1()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.expm1(stack[top-1]);
    }
    /**
     * @see java.lang.Math#floor(double) 
     */
    @Override
    public void floor()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.floor(stack[top-1]);
    }
    /**
     * @see java.lang.Math#hypot(double, double) 
     */
    @Override
    public void hypot()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] = Math.hypot(stack[top-2], stack[top-1]);
        top--;
    }
    /**
     * @see java.lang.Math#log(double) 
     */
    @Override
    public void log()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.log(stack[top-1]);
    }
    /**
     * @see java.lang.Math#log10(double) 
     */
    @Override
    public void log10()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.log10(stack[top-1]);
    }
    /**
     * @see java.lang.Math#log1p(double) 
     */
    @Override
    public void log1p()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.log1p(stack[top-1]);
    }
    /**
     * @see java.lang.Math#max(double, double) 
     */
    @Override
    public void max()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] = Math.max(stack[top-2], stack[top-1]);
        top--;
    }
    /**
     * @see java.lang.Math#min(double, double) 
     */
    @Override
    public void min()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] = Math.min(stack[top-2], stack[top-1]);
        top--;
    }
    /**
     * @see java.lang.Math#pow(double, double) 
     */
    @Override
    public void pow()
    {
        if (top < 2)
        {
            throw new EmptyStackException();
        }
        stack[top-2] = Math.pow(stack[top-2], stack[top-1]);
        top--;
    }
    /**
     * @see java.lang.Math#sin(double) 
     */
    @Override
    public void sin()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.sin(stack[top-1]);
    }
    /**
     * @see java.lang.Math#sinh(double) 
     */
    @Override
    public void sinh()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.sinh(stack[top-1]);
    }
    /**
     * @see java.lang.Math#sqrt(double) 
     */
    @Override
    public void sqrt()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.sqrt(stack[top-1]);
    }
    /**
     * @see java.lang.Math#tan(double) 
     */
    @Override
    public void tan()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.tan(stack[top-1]);
    }
    /**
     * @see java.lang.Math#tanh(double) 
     */
    @Override
    public void tanh()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.tanh(stack[top-1]);
    }
    /**
     * @see java.lang.Math#toDegrees(double) 
     */
    @Override
    public void toDegrees()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.toDegrees(stack[top-1]);
    }
    /**
     * @see java.lang.Math#toRadians(double) 
     */
    @Override
    public void toRadians()
    {
        if (top < 1)
        {
            throw new EmptyStackException();
        }
        stack[top-1] = Math.toRadians(stack[top-1]);
    }

    @Override
    public void eq()
    {
        stack[top-2] = stack[top-2] == stack[top-1] ? TRUE : FALSE;
        top--;
    }

    @Override
    public void ne()
    {
        stack[top-2] = stack[top-2] != stack[top-1] ? TRUE : FALSE;
        top--;
    }

    @Override
    public void lt()
    {
        stack[top-2] = stack[top-2] < stack[top-1] ? TRUE : FALSE;
        top--;
    }

    @Override
    public void le()
    {
        stack[top-2] = stack[top-2] <= stack[top-1] ? TRUE : FALSE;
        top--;
    }

    @Override
    public void gt()
    {
        stack[top-2] = stack[top-2] > stack[top-1] ? TRUE : FALSE;
        top--;
    }

    @Override
    public void ge()
    {
        stack[top-2] = stack[top-2] >= stack[top-1] ? TRUE : FALSE;
        top--;
    }

    @Override
    public void not()
    {
        assert(stack[top-1] == TRUE || stack[top-1] == FALSE);
        stack[top-1] = stack[top-1] == FALSE ? TRUE : FALSE;
    }

    @Override
    public void and()
    {
        assert(stack[top-2] == TRUE || stack[top-2] == FALSE);
        assert(stack[top-1] == TRUE || stack[top-1] == FALSE);
        stack[top-2] = stack[top-2] == TRUE && stack[top-1] == TRUE ? TRUE : FALSE;
        top--;
    }

    @Override
    public void checkAnd() throws NoNeedToContinueException
    {
        if (top == 1 && stack[0] == FALSE)
        {
            throw new NoNeedToContinueException("false && ? == false");
        }
    }

    @Override
    public void or()
    {
        assert(stack[top-2] == TRUE || stack[top-2] == FALSE);
        assert(stack[top-1] == TRUE || stack[top-1] == FALSE);
        stack[top-2] = stack[top-2] == TRUE || stack[top-1] == TRUE ? TRUE : FALSE;
        top--;
    }

    @Override
    public void checkOr() throws NoNeedToContinueException
    {
        if (top == 1 && stack[0] == TRUE)
        {
            throw new NoNeedToContinueException("true || ? == true");
        }
    }
}
