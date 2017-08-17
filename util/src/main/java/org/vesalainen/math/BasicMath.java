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
package org.vesalainen.math;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public interface BasicMath
{

    /**
     * @see java.lang.Math#abs(double)
     */
    void abs();

    /**
     * @see java.lang.Math#acos(double)
     */
    void acos();

    /**
     * Top element is popped and added to the new top element.
     */
    void add();

    /**
     * @see java.lang.Math#asin(double)
     */
    void asin();

    /**
     * @see java.lang.Math#atan(double)
     */
    void atan();

    /**
     * @see java.lang.Math#atan2(double)
     */
    void atan2();

    /**
     * @see java.lang.Math#cbrt(double)
     */
    void cbrt();

    /**
     * @see java.lang.Math#ceil(double)
     */
    void ceil();

    /**
     * @see java.lang.Math#cos(double)
     */
    void cos();

    /**
     * @see java.lang.Math#cosh(double)
     */
    void cosh();

    /**
     * Top element is popped and divides the new top element.
     */
    void div();

    /**
     * Top element is pushed.
     */
    void dup();

    /**
     * @see java.lang.Math#exp(double)
     */
    void exp();

    /**
     * @see java.lang.Math#expm1(double)
     */
    void expm1();

    /**
     * @see java.lang.Math#floor(double)
     */
    void floor();

    /**
     * @see java.lang.Math#hypot(double, double)
     */
    void hypot();

    /**
     * Return true is stack doesn't have any elements.
     * @return
     */
    boolean isEmpty();

    /**
     * @see java.lang.Math#log(double)
     */
    void log();

    /**
     * @see java.lang.Math#log10(double)
     */
    void log10();

    /**
     * @see java.lang.Math#log1p(double)
     */
    void log1p();

    /**
     * @see java.lang.Math#max(double, double)
     */
    void max();

    /**
     * @see java.lang.Math#min(double, double)
     */
    void min();

    /**
     * Top element is popped and mods the new top element.
     */
    void mod();

    /**
     * Top element is popped and multiples the new top element.
     */
    void mul();

    /**
     * Top elements sign is changed.
     */
    void neg();

    /**
     * @see java.lang.Math#pow(double, double)
     */
    void pow();

    /**
     * @see java.lang.Math#sin(double)
     */
    void sin();

    /**
     * @see java.lang.Math#sinh(double)
     */
    void sinh();

    /**
     * @see java.lang.Math#sqrt(double)
     */
    void sqrt();

    /**
     * @see java.lang.Math#tan(double)
     */
    void tan();

    /**
     * @see java.lang.Math#tanh(double)
     */
    void tanh();

    /**
     * @see java.lang.Math#toDegrees(double)
     */
    void toDegrees();

    /**
     * @see java.lang.Math#toRadians(double)
     */
    void toRadians();
    
}
