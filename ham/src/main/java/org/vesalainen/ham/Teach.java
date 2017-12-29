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
package org.vesalainen.ham;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import javax.sound.sampled.LineUnavailableException;
import org.vesalainen.util.CmdArgs;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Teach extends CmdArgs
{
    private List<Character> list = new ArrayList<>();
    public Teach()
    {
        addOption("-wpm", "Words  per Minute", null, 15);
        addOption("-pitch", "Pitch", null, 700);
    }
    public void teach() throws LineUnavailableException, IOException, InterruptedException
    {
        try (MorseCode mc = MorseCode.getInstance(getOption("-wpm"), getOption("-pitch")))
        {
            MorseTeacher scanner = new MorseTeacher();
            long space = mc.key(" ");
            scanner.input(" ", (cc)->{});
            for (int count=1;count<20;count++)
            {
                for (int ii=0;ii<count;ii++)
                {
                    list.addAll(mc.getLetters());
                }
                long start = System.currentTimeMillis();
                int errors = 0;
                Random rand = new Random(start);
                Collections.shuffle(list, rand);
                while (!list.isEmpty())
                {
                    List<Character> sub = list.subList(0, count);
                    String keyed = toString(sub);
                    long begin = System.currentTimeMillis();
                    long time = mc.key(keyed);
                    boolean ok = scanner.input(keyed, (cc)->list.add(cc));
                    long elapsed = System.currentTimeMillis() - begin;
                    if (!ok)
                    {
                        System.err.println(keyed+" = "+mc.toString(keyed));
                        Collections.shuffle(list, rand);
                        errors++;
                    }
                    else
                    {
                        if (elapsed > time + 2*space)
                        {
                            System.err.println("you are too slow!");
                            list.addAll(sub);
                            Collections.shuffle(list, rand);
                        }
                        else
                        {
                            list.removeAll(sub);
                        }
                    }
                }
                System.out.printf("Time %f sec errors %d", (double)(System.currentTimeMillis()-start)/1000.0, errors);
            }
            System.out.println("PASSED!!!");
        }
    }
    public static void main(String... args)
    {
        try
        {
            Teach t = new Teach();
            t.command(args);
            t.teach();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private String toString(List<Character> sub)
    {
        StringBuilder sb = new StringBuilder();
        for (char cc : sub)
        {
            sb.append(cc);
        }
        return sb.toString();
    }
}
