/*
 * Copyright (C) 2022 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.can;

import java.util.function.Consumer;
import java.util.logging.Level;
import org.vesalainen.util.logging.JavaLogging;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Transaction extends JavaLogging implements Runnable
{
    
    private Runnable begin;
    private Runnable action;
    private Consumer<Throwable> end;

    public Transaction()
    {
        this(null, null, null);
    }

    public Transaction(Runnable action)
    {
        this(null, action, null);
    }

    public Transaction(Runnable begin, Runnable action, Consumer<Throwable> end)
    {
        super(Transaction.class);
        this.begin = begin != null ? begin : () ->
        {
        };
        this.action = action != null ? action : () ->
        {
        };
        this.end = end != null ? end : e ->
        {
        };
    }

    @Override
    public void run()
    {
        try
        {
            Throwable thr = null;
            begin.run();
            try
            {
                action.run();
            }
            catch (Throwable ex)
            {
                thr = ex;
                log(Level.SEVERE, ex, "%s", ex.getMessage());
            }
            finally
            {
                end.accept(thr);
            }
        }
        catch (Exception ex)
        {
            log(Level.WARNING, ex, "execute %s", ex.getMessage());
        }
    }
    
}
