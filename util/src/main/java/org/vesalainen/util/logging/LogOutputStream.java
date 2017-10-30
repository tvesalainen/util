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
package org.vesalainen.util.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import static java.nio.charset.StandardCharsets.*;
import java.util.logging.Level;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LogOutputStream extends PrintStream
{
    
    public LogOutputStream(String format, JavaLogging log, Level level) throws UnsupportedEncodingException
    {
        super(new Out(format, log, level), true, UTF_8.name());
    }
    
    private static class Out extends ByteArrayOutputStream
    {
        private JavaLogging log;
        private Level level;
        private String format;

        public Out(String format, JavaLogging log, Level level)
        {
            this.log = log;
            this.level = level;
            this.format = format;
            log.getLogger().setUseParentHandlers(false);
        }

        @Override
        public void flush() throws IOException
        {
            if (log.isLoggable(level))
            {
                String msg = new String(toByteArray(), UTF_8).trim();
                reset();
                if (!msg.isEmpty())
                {
                    log.log(level, format, msg);
                }
            }
        }
        
    }
}
