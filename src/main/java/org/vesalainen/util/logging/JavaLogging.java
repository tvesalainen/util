/*
 * Copyright (C) 2015 tkv
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
package org.vesalainen.util.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author tkv
 */
public class JavaLogging extends BaseLogging
{
    private Logger logger;

    public JavaLogging()
    {
    }

    public JavaLogging(Logger logger)
    {
        this.logger = logger;
    }

    public void setLogger(Logger logger)
    {
        this.logger = logger;
    }
    
    public Logger getLogger()
    {
        return logger;
    }
    
    @Override
    protected boolean isLoggable(Level level)
    {
        return logger.isLoggable(level);
    }

    @Override
    protected void logIt(Level level, String msg)
    {
        logger.log(level, msg);
    }

    @Override
    protected void logIt(Level level, String msg, Throwable thrown)
    {
        logger.log(level, msg, thrown);
    }
    
}
