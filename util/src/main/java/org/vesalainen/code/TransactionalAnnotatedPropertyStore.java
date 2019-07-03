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
package org.vesalainen.code;

import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import org.vesalainen.util.Transactional;
import org.vesalainen.util.logging.AttachedLogger;

/**
 * TransactionalAnnotatedPropertyStore extends AnnotatedPropertyStore and implements
 * Transactional. It handles logging of start, commit and rollback in case
 * of exception. User must implement doStart, doCommit and doRollback.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public abstract class TransactionalAnnotatedPropertyStore extends AnnotatedPropertyStore implements Transactional, AttachedLogger
{

    public TransactionalAnnotatedPropertyStore(AnnotatedPropertyStore aps)
    {
        super(aps);
    }

    public TransactionalAnnotatedPropertyStore(Path path) throws IOException
    {
        super(path);
    }

    public TransactionalAnnotatedPropertyStore()
    {
    }

    @Override
    public final void start(String reason)
    {
        try
        {
            doStart(reason);
        }
        catch (Throwable ex)
        {
            log(Level.SEVERE, ex, reason);
        }
    }

    @Override
    public final void rollback(String reason)
    {
        try
        {
            doRollback(reason);
        }
        catch (Throwable ex)
        {
            log(Level.SEVERE, ex, reason);
        }
    }

    @Override
    public final void commit(String reason)
    {
        try
        {
            doCommit(reason);
        }
        catch (Throwable ex)
        {
            log(Level.SEVERE, ex, reason);
        }
    }

    protected abstract void doStart(String reason);

    protected abstract void doRollback(String reason);

    protected abstract void doCommit(String reason);
    
}
