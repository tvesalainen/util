/*
 * Copyright (C) 2019 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.vesalainen.lang.Primitives;
import org.vesalainen.text.Unicodes;
import org.vesalainen.util.CharSequences;
import org.vesalainen.util.CollectionHelp;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolynomialExpressionBuilder
{
    private String var;

    public PolynomialExpressionBuilder(String var)
    {
        this.var = var;
    }
    
    public Polynom create(String expr)
    {
        return new Polynom(expr);
    }
    public Polynom mul(String c, Polynom p)
    {
        Polynom cp = new Polynom(c);
        return mul(cp, p);
    }
    public Polynom mul(Polynom p1, Polynom p2)
    {
        return new Polynom(p1.asSum().mul(p2.asSum()));
    }
    public Polynom plus(Polynom p1, Polynom p2)
    {
        return new Polynom(p1.asSum().add(p2.asSum()));
    }
    public Polynom minus(Polynom p1, Polynom p2)
    {
        return new Polynom(p1.asSum().add(p2.asSum().negative()));
    }

    public static Sum parseSum(String expr)
    {
        String[] mulExprs = splitTerms(expr);
        Mul[] muls = new Mul[mulExprs.length];
        for (int ii=0;ii<muls.length;ii++)
        {
            muls[ii] = parseMul(mulExprs[ii]);
        }
        return new Sum(muls);
    }
    private static String[] splitTerms(String expr)
    {
        List<String> list = new ArrayList<>();
        int from = 1;
        int idx = CharSequences.indexOf(expr, (c)->c=='+'||c=='-');
        while (idx != -1)
        {
            list.add(expr.subSequence(from-1, idx).toString());
            from = idx+1;
            idx = CharSequences.indexOf(expr, (c)->c=='+'||c=='-', from);
        }
        list.add(expr.substring(from-1));
        return list.toArray(new String[list.size()]);
    }
    private static Mul parseMul(String expr)
    {
        if (expr.length() > 1 && ((expr.startsWith("+") || expr.startsWith("-")) && !Character.isDigit(expr.charAt(1))))
        {
            expr = expr.charAt(0)+"1*"+expr.substring(1);
        }
        String[] terms = expr.split("\\*");
        try
        {
            int multiplier = Primitives.parseInt(terms[0]);
            return new Mul(multiplier, Arrays.copyOfRange(terms, 1, terms.length));
        }
        catch (NumberFormatException ex)
        {
            return new Mul(terms);
        }
    }
    private static void checkMul(String... terms)
    {
        for (String term : terms)
        {
            checkMul(term);
        }
    }
    private static void checkMul(String term)
    {
        if (term.isEmpty())
        {
            throw new IllegalArgumentException("empty");
        }
        if (Character.isDigit(term.codePointAt(0)))
        {
            throw new IllegalArgumentException("not proper start");
        }
        for (int ii=1;ii<term.length();ii++)
        {
            switch (term.charAt(ii))
            {
                case '*':
                case '+':
                case '-':
                    throw new IllegalArgumentException(term+" not proper");
            }
        }
    }
    public static class Mul implements Comparable<Mul>
    {
        private int multiplier;
        private String[] terms;

        public Mul(int multiplier)
        {
            this(multiplier, new String[]{});
        }

        public Mul(String... terms)
        {
            this(1, terms);
        }

        public Mul(int multiplier, String... terms)
        {
            this.multiplier = multiplier;
            this.terms = terms;
            checkMul(this.terms);
            Arrays.sort(this.terms);
        }
        
        public Mul(Mul... muls)
        {
            this.multiplier = 1;
            List<String> list = new ArrayList<>();
            for (Mul m : muls)
            {
                this.multiplier *= m.multiplier;
                CollectionHelp.addAll(list, m.terms);
            }
            this.terms = list.toArray(new String[list.size()]);
            checkMul(this.terms);
            Arrays.sort(this.terms);
        }

        public int getMultiplier()
        {
            return multiplier;
        }

        public void forEach(Consumer<String> act)
        {
            for (String term : terms)
            {
                act.accept(term);
            }
        }
        
        public Mul negative()
        {
            return new Mul(-multiplier, terms);
        }
        
        public Mul mul(int coef)
        {
            return new Mul(coef*multiplier, terms);
        }
        public boolean sameBase(Mul o)
        {
            return Arrays.equals(terms, o.terms);
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 97 * hash + this.multiplier;
            hash = 97 * hash + Arrays.deepHashCode(this.terms);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Mul other = (Mul) obj;
            if (this.multiplier != other.multiplier)
            {
                return false;
            }
            if (!Arrays.deepEquals(this.terms, other.terms))
            {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(Mul o)
        {
            if (multiplier != o.multiplier)
            {
                return multiplier - o.multiplier;
            }
            int min = Math.min(terms.length, o.terms.length);
            for (int ii=0;ii<min;ii++)
            {
                int c = terms[ii].compareTo(o.terms[ii]);
                if (c != 0)
                {
                    return c;
                }
            }
            return terms.length - o.terms.length;
        }
        
        @Override
        public String toString()
        {
            StringBuilder sb = new StringBuilder();
            switch (multiplier)
            {
                case 1:
                    sb.append('+');
                    break;
                case -1:
                    sb.append('-');
                    break;
                default:
                    if (multiplier > 0)
                    {
                        sb.append('+');
                    }
                    sb.append(multiplier);
                    break;
            }
            String prev = null;
            int pow = 1;
            for (String m : terms)
            {
                if (!m.equals(prev))
                {
                    if (pow > 1)
                    {
                        Unicodes.toSuperScript(String.valueOf(pow), sb);
                    }
                    sb.append(m);
                    pow = 1;
                    prev = m;
                }
                else
                {
                    pow++;
                }
            }
            if (pow > 1)
            {
                Unicodes.toSuperScript(String.valueOf(pow), sb);
            }
            return sb.toString();
        }
        public String toCode()
        {
            StringBuilder sb = new StringBuilder();
            switch (multiplier)
            {
                case 1:
                    sb.append('+');
                    break;
                case -1:
                    sb.append('-');
                    break;
                default:
                    if (multiplier > 0)
                    {
                        sb.append('+');
                    }
                    sb.append(multiplier);
                    sb.append('*');
                    break;
            }
            boolean star = false;
            for (String m : terms)
            {
                if (star)
                {
                    sb.append('*');
                }
                star = true;
                sb.append(m);
            }
            return sb.toString();
        }

    }
    public static class Sum
    {
        private Mul[] terms;

        public Sum(Mul... terms)
        {
            List<Mul> list = new ArrayList<>();
            for (Mul m : terms)
            {
                boolean found = false;
                for (int ii=0;ii<list.size();ii++)
                {
                    Mul l = list.get(ii);
                    if (m.sameBase(l))
                    {
                        int s = m.multiplier+l.multiplier;
                        list.remove(ii);
                        if (s != 0)
                        {
                            list.add(ii, new Mul(s, m.terms));
                        }
                        found = true;
                        break;
                    }
                }
                if (!found)
                {
                    list.add(m);
                }
            }
            list.sort(null);
            this.terms = list.toArray(new Mul[list.size()]);
        }
        public void forEach(Consumer<Mul> act)
        {
            for (Mul mul : terms)
            {
                act.accept(mul);
            }
        }
        public Sum add(Mul o)
        {
            List<Mul> list = new ArrayList<>();
            CollectionHelp.addAll(list, terms);
            list.add(o);
            return new Sum(list.toArray(new Mul[list.size()]));
        }
        public Sum add(Sum o)
        {
            List<Mul> list = new ArrayList<>();
            CollectionHelp.addAll(list, terms);
            CollectionHelp.addAll(list, o.terms);
            return new Sum(list.toArray(new Mul[list.size()]));
        }
        public Sum mul(Sum o)
        {
            List<Mul> list = new ArrayList<>();
            for (Mul m : terms)
            {
                for (Mul l : o.terms)
                {
                    list.add(new Mul(m, l));
                }
            }
            return new Sum(list.toArray(new Mul[list.size()]));
        }
        public Sum negative()
        {
            return mul(-1);
        }
        public Sum mul(int coef)
        {
            Mul[] t = new Mul[terms.length];
            for (int ii=0;ii<terms.length;ii++)
            {
                t[ii] = terms[ii].mul(coef);
            }
            return new Sum(t);
        }
        public boolean zero()
        {
            return terms.length == 0;
        }

        @Override
        public int hashCode()
        {
            int hash = 7;
            hash = 37 * hash + Arrays.deepHashCode(this.terms);
            return hash;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            final Sum other = (Sum) obj;
            if (!Arrays.deepEquals(this.terms, other.terms))
            {
                return false;
            }
            return true;
        }
        
        @Override
        public String toString()
        {
            if (!zero())
            {
                StringBuilder sb = new StringBuilder();
                for (Mul m : terms)
                {
                    sb.append(m);
                }
                if (terms.length > 1)
                {
                    return "+("+sb.toString().substring(1)+')';
                }
                else
                {
                    return sb.toString();
                }
            }
            else
            {
                return "0";
            }
        }
        public String toCode()
        {
            if (!zero())
            {
                StringBuilder sb = new StringBuilder();
                for (Mul m : terms)
                {
                    sb.append(m.toCode());
                }
                if (terms.length > 1)
                {
                    return "+("+sb.toString().substring(1)+')';
                }
                else
                {
                    return sb.toString();
                }
            }
            else
            {
                return "0";
            }
        }

    }
    public class Polynom
    {
        private Sum[] coef;

        public Polynom(String expr)
        {
            this(parseSum(expr));
        }

        public Polynom(Sum expr)
        {
            if (!expr.zero())
            {
                int max = 0;
                Map<Integer,Sum> map = new HashMap<>();
                for (Mul m : expr.terms)
                {
                    List<String> terms = new ArrayList<>();
                    int pow = 0;
                    for (String t : m.terms)
                    {
                        if (var.equals(t))
                        {
                            pow++;
                        }
                        else
                        {
                            terms.add(t);
                        }
                    }
                    Mul mul = new Mul(m.multiplier, terms.toArray(new String[terms.size()]));
                    Sum sum = map.get(pow);
                    if (sum == null)
                    {
                        map.put(pow, new Sum(mul));
                    }
                    else
                    {
                        map.put(pow, sum.add(mul));
                    }
                    max = Math.max(max, pow);
                }
                this.coef = new Sum[max+1];
                map.forEach((p,s)->this.coef[p] = s);
            }
            else
            {
                this.coef = new Sum[0];
            }
        }

        public Polynom derivative()
        {
            Sum sum = new Sum();
            for (int ii=1;ii<coef.length;ii++)
            {
                sum = sum.add(coef[ii].mul(new Sum(grade(ii-1).mul(ii))));
            }
            return new Polynom(sum);
        }
        public Sum asSum()
        {
            Sum sum = new Sum();
            for (int ii=0;ii<coef.length;ii++)
            {
                if (coef[ii] != null)
                {
                    sum = sum.add(coef[ii].mul(new Sum(grade(ii))));
                }
            }
            return sum;
        }
        public Sum coef(int index)
        {
            return coef[index];
        }
        public int length()
        {
            return coef.length;
        }
        public boolean zero()
        {
            return coef.length == 0;
        }
        @Override
        public String toString()
        {
            if (!zero())
            {
                StringBuilder sb = new StringBuilder();
                int length = coef.length;
                for (int ii=length-1;ii>=0;ii--)
                {
                    if (coef[ii] != null)
                    {
                        sb.append(coef[ii]);
                        if (ii>0)
                        {
                            sb.append(var);
                        }
                        if (ii > 1)
                        {
                            Unicodes.toSuperScript(String.valueOf(ii), sb);
                        }
                    }
                }
                switch (sb.charAt(sb.length()-1))
                {
                    case '+':
                    case '-':
                        sb.append('1');
                }
                if (sb.charAt(0) != '+')
                {
                    return sb.toString();
                }
                else
                {
                    return sb.toString().substring(1);
                }
            }
            else
            {
                return "0";
            }
        }
        public String toCode()
        {
            if (!zero())
            {
                StringBuilder sb = new StringBuilder();
                int length = coef.length;
                for (int ii=length-1;ii>=0;ii--)
                {
                    sb.append(coef[ii].toCode());
                    for (int jj=0;jj<ii;jj++)
                    {
                        sb.append('*');
                        sb.append(var);
                    }
                }
                switch (sb.charAt(sb.length()-1))
                {
                    case '+':
                    case '-':
                        sb.append('1');
                }
                if (sb.charAt(0) != '+')
                {
                    return sb.toString();
                }
                else
                {
                    return sb.toString().substring(1);
                }
            }
            else
            {
                return "0";
            }
        }
    }
    private Mul grade(int pow)
    {
        String[] terms = new String[pow];
        Arrays.fill(terms, var);
        return new Mul(terms);
    }
            
}
