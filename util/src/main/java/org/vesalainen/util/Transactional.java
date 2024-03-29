/*
 * Copyright (C) 2013 Timo Vesalainen
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

import java.util.Collection;
import java.util.function.Predicate;

/**
 * An interface for transactional changes
 * 
 * <p>Start of transaction is by start method
 * 
 * @author Timo Vesalainen
 */
public interface Transactional
{
    /**
     * @deprecated Use begin
     * Start transaction that will end in commit or rollback methods.
     * @param reason
     */
    default void start(String reason){begin(reason);}
    /**
     * Start transaction that will end in commit or rollback methods.
     * @param reason
     */
    default void begin(String reason){}
    /**
     * Undo changes after start.
     * @param reason 
     */
    default void rollback(String reason){}
    /**
     * Confirm changes after start. This is convenience method and should not
     * be over ridden.
     * @param reason 
     * @param updatedProperties 
     */
    default void commit(String reason, String... updatedProperties)
    {
        commit(reason, CollectionHelp.create(updatedProperties));
    }
    /**
     * Confirm changes after start.
     * @param reason 
     * @param updatedProperties 
     */
    default void commit(String reason, Collection<String> updatedProperties)
    {
        commit(reason);
    }
    default void commit(String reason, Collection<String> updatedProperties, Predicate<String> isModified)
    {
        commit(reason, updatedProperties);
    }
    /**
     * Confirm changes after start.
     * @param reason 
     */
    default void commit(String reason){}
            

}
