/*
 * Copyright (C) 2018 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.ham.filter;

import java.awt.Color;
import java.io.IOException;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.vesalainen.ham.AudioFileFilter.BAND_PASS;
import org.vesalainen.ham.fft.FrequencyDomain;
import org.vesalainen.ham.fft.TimeDomain;
import org.vesalainen.ham.fft.Waves;
import org.vesalainen.nio.IntArray;
import org.vesalainen.ui.Plotter;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FIRFilterTest
{
    
    public FIRFilterTest()
    {
    }

    @Test
    public void test0() throws IOException
    {
        double[] a = new double[]{3, -1, 2, 1};
        FIRFilter f = new FIRFilter(a);
        double[] x = new double[]{2, 4, 6, 4, 2, 0, 0, 0};
        double[] y = new double[]{6, 10, 18, 16, 18, 12, 8, 2};
        for (int ii=0;ii<8;ii++)
        {
            assertEquals("at "+ii, y[ii], f.filter(x[ii]), 1e-8);
        }
    }
    @Test
    public void test1() throws IOException
    {
        //double[] a = new double[]{0.0, 2.6911230374230087E-6, 6.8531414772195044E-6, 4.598430571415954E-6, -1.2610397492823773E-5, -5.1029177578800035E-5, -1.1181306606484084E-4, -1.8898691733299704E-4, -2.6878994264360615E-4, -3.3076545187841333E-4, -3.5066028616051925E-4, -3.0484739029516616E-4, -1.7564736162278363E-4, 4.333841572907357E-5, 3.429059681759098E-4, 6.959324603602001E-4, 0.0010578541052105746, 0.0013703597881355033, 0.001568236123271196, 0.0015887611331693778, 0.0013825331771233989, 9.242232494723081E-4, 2.2152725402053614E-4, -6.793760645341174E-4, -0.0016932637734990005, -0.002701065902062488, -0.0035609473034684857, -0.004124555879396856, -0.004256875808913662, -0.003857403504731494, -0.0028798795702865867, -0.0013476537411182498, 6.380138862671446E-4, 0.002898796293485619, 0.00519040883659394, 0.007223897170782275, 0.008695695722693908, 0.009323777217600908, 0.008886159774924036, 0.007257354049695598, 0.0044381351307012755, 5.743752301987314E-4, -0.004038428349993602, -0.008966920246584128, -0.01366822102113473, -0.017533222584336036, -0.01994141670971708, -0.020322279031762574, -0.01821715266271973, -0.013335119234427411, -0.005596594995819213, 0.004840649706049669, 0.017576755952789288, 0.031987964148701406, 0.047267028313777125, 0.06248281760304204, 0.07665397349859093, 0.08882988822791502, 0.09817134489244345, 0.10402302773649433, 0.10597079950932019, 0.1038780875803085, 0.09789777149410987, 0.08845840324282386, 0.07622615480448747, 0.062046298455019036, 0.0468700245775828, 0.03167377688333067, 0.017378894916904142, 0.00477914381139418, -0.005517281347987631, -0.013126341043783223, -0.017904508111690958, -0.019942417669108935, -0.019537636124714198, -0.01715039437929928, -0.013347655893519213, -0.008741776046066558, -0.003930186240435795, 5.579817714991016E-4, 0.004303537259574297, 0.007023912366788973, 0.008583476273246933, 0.008987921118051134, 0.0083648277919184, 0.0069338322498711516, 0.0049706438461168895, 0.0027694546151307412, 6.080283331302848E-4, -0.0012809603890401502, -0.002729833735141995, -0.0036458237644984206, -0.004011040619760106, -0.003873708102012371, -0.0033327832331832177, -0.002518634659717851, -0.0015726288659953572, -6.282764413792172E-4, 2.0391776422060855E-4, 8.464827796823621E-4, 0.001259299538262148, 0.0014384352483039324, 0.0014104150754147562, 0.0012233456797083016, 9.365513352329981E-4, 6.103734310271342E-4, 2.975453867961886E-4, 3.714409008946301E-5, -1.4838824673864292E-4, -2.5317852275414965E-4, -2.8529667700061994E-4, -2.6238325456292254E-4, -2.0651258886241494E-4, -1.3926833772223766E-4, -7.784692880147089E-5, -3.2727095404473005E-5, -7.1049001240826E-6, 2.046123023442111E-6, 1.7144800101279176E-6, 0.0};
        double[] a = CoeffReader.read("256 Tap Inv Cheby BPF.txt");
        IntArray samples = IntArray.getInstance(4096, 16, ByteOrder.LITTLE_ENDIAN);
        Waves.addWhiteNoise(samples, 10000);
        TimeDomain td = Waves.createTimeDomain(44100, samples);
        FrequencyDomain fd = Waves.fft(td);
        Waves.plot(fd, Paths.get("fd1.png"));
        FIRFilter f = new FIRFilter(a);
        f.update(samples);
        FrequencyDomain fd2 = Waves.fft(td);
        Waves.plot(fd2, 0.0, Paths.get("fd2.png"));
    }
}
