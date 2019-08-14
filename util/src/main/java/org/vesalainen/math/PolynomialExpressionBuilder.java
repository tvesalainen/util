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
import java.util.List;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class PolynomialExpressionBuilder
{
    private String var;
    private String tmp;
    private List<String> expr = new ArrayList<>();

    public PolynomialExpressionBuilder(String var, String tmp)
    {
        this.var = var;
        this.tmp = tmp;
    }
    
    public Polynom mul(String coef, Polynom p)
    {
        int len = p.length();
        String[] r = new String[len];
        for (int ii=0;ii<len;ii++)
        {
            String ff = p.coef(ii);
            r[ii] = expr(coef, '+', ff);
        }
        return new Polynom(r);
    }
    public Polynom mul(Polynom p1, Polynom p2)
    {
        int l1 = p1.length();
        int l2 = p2.length();
        String[] r = new String[(l1-1)+(l2-1)+1];
        for (int ii=0;ii<l1;ii++)
        {
            String f1 = p1.coef(ii);
            for (int jj=0;jj<l2;jj++)
            {
                String f2 = p2.coef(jj);
                String ds = expr(f1, '*', f2);
                int g = ii+jj;
                String ff = r[g];
                if (ff == null)
                { 
                    r[g] = ds;
                }
                else
                {
                    r[g] = expr(ff, '+', ds);
                }
            }
        }
        return new Polynom(r);
    }
    public Polynom plus(Polynom p1, Polynom p2)
    {
        int l1 = p1.length();
        int l2 = p2.length();
        int l = Math.max(l1, l2);
        String[] r = new String[l];
        for (int ii=0;ii<l;ii++)
        {
            if (ii < l1)
            {
                String f1 = p1.coef(ii);
                if (ii < l2)
                { 
                    String f2 = p2.coef(ii);
                    r[ii] = expr(f1, '+', f2);
                }
                else
                {
                    r[ii] = f1;
                }
            }
            else
            {
                if (ii < l2)
                { 
                    String f2 = p2.coef(ii);
                    r[ii] = f2;
                }
            }
        }
        return new Polynom(r);
    }
    public Polynom minus(Polynom p1, Polynom p2)
    {
        int l1 = p1.length();
        int l2 = p2.length();
        int l = Math.max(l1, l2);
        String[] r = new String[l];
        for (int ii=0;ii<l;ii++)
        {
            if (ii < l1)
            {
                String f1 = p1.coef(ii);
                if (ii < l2)
                { 
                    String f2 = p2.coef(ii);
                    r[ii] = expr(f1, '-', f2);
                }
                else
                {
                    r[ii] = f1;
                }
            }
            else
            {
                if (ii < l2)
                { 
                    String f2 = p2.coef(ii);
                    r[ii] = expr('-', f2);
                }
            }
        }
        return new Polynom(r);
    }
    public Polynom derivative(Polynom p)
    {
        int len = p.length()-1;
        String[] r = new String[len];
        for (int ii=0;ii<len;ii++)
        {
            int g = ii+1;
            String ff = p.coef(g);
            r[ii] = expr(String.valueOf(g), '*', ff);
        }
        return new Polynom(r);
    }

    public Polynom create(String... coef)
    {
        return new Polynom(coef);
    }
    public String subVars(String type)
    {
        StringBuilder sb = new StringBuilder();
        int size = expr.size();
        for (int ii=0;ii<size;ii++)
        {
            sb.append(type)
                    .append(' ')
                    .append(tmp)
                    .append(ii)
                    .append(" = ")
                    .append(expr.get(ii))
                    .append(";\n");
        }
        return sb.toString();
    }
    private String expr(String e1, char op, String e2)
    {
        String sub = tmp+expr.size();
        expr.add(e1+op+e2);
        return sub;
    }
    private String expr(char op, String e2)
    {
        String sub = tmp+expr.size();
        expr.add(op+e2);
        return sub;
    }
    public class Polynom
    {
        private String[] coef;

        private Polynom(String... coef)
        {
            this.coef = coef;
        }

        public String coef(int index)
        {
            return coef[index];
        }
        public int length()
        {
            return coef.length;
        }
        @Override
        public String toString()
        {
            int length = coef.length;
            if (length == 0)
            {
                return "";
            }
            String sum = coef[length-1]+"*"+var;
            for (int ii=length-2;ii>0;ii--)
            {
                sum += "+"+coef[ii];
                sum = "("+sum+")*"+var;
            }
            sum += "+"+coef[0];
            return sum;
        }
        
    }
            
}
