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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.util.function.Supplier;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author tkv
 */
public class MinimalFormatter extends Formatter
{
    private Supplier<Clock> clockFactory;
    
    public MinimalFormatter()
    {
    }

    public MinimalFormatter(Supplier<Clock> clockFactory)
    {
        this.clockFactory = clockFactory;
    }
    
    @Override
    public String format(LogRecord record)
    {
        String l;
        switch (record.getLevel().getName())
        {
            case "SEVERE":
                l = "SE";
                break;
            case "WARNING":
                l = "WA";
                break;
            case "INFO":
                l = "IN";
                break;
            case "CONFIG":
                l = "CO";
                break;
            case "FINE":
                l = "F1";
                break;
            case "FINER":
                l = "F2";
                break;
            case "FINEST":
                l = "F3";
                break;
            default:
                l = "LEVEL("+record.getLevel().intValue()+")";
                break;
                
        }
        Throwable thrown = record.getThrown();
        if (thrown == null)
        {
            return String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS.%1$tL %2$s %3$s\r\n", 
                    getMillis(record),
                    l,
                    record.getMessage()
            );
        }
        else
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            pw.flush();
            thrown.printStackTrace(pw);
            return String.format("%1$tY-%1$tm-%1$tdT%1$tH:%1$tM:%1$tS.%1$tL %2$s %3$s\r\n%4$s\n\n", 
                    getMillis(record),
                    l,
                    record.getMessage(),
                    sw.toString()
            );
        }
    }
    
    private long getMillis(LogRecord record)
    {
        if (clockFactory != null)
        {
            long offset = Clock.systemUTC().millis() - clockFactory.get().millis();
            return record.getMillis()-offset;
        }
        else
        {
            return record.getMillis();
        }
    }
}
