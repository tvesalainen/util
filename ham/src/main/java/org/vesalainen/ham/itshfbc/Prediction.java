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
package org.vesalainen.ham.itshfbc;

import java.io.File;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import static org.vesalainen.ham.itshfbc.Command.*;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Prediction
{

    private File itshfbc = new File("C:\\itshfbc");
    private String module = "voacapw";
    private List<CommandLine> input = new ArrayList<>();

    /**
     * The METHOD command defines the analysis task to be performed for a
     * particular system configuration.
     *
     * @param method
     * @return
     */
    public Prediction method(int method)
    {
        input.add(new CommandLine<>(METHOD, 5, method));
        return this;
    }

    /**
     * The EXECUTE command causes the program to perform the indicated analysis
     * task for the currently defined system configuration.
     *
     * @return
     */
    public Prediction execute()
    {
        input.add(new CommandLine<>(EXECUTE, 5));
        return this;
    }

    /**
     * The EXECUTE command causes the program to perform the indicated analysis
     * task for the currently defined system configuration.
     *
     * @param krun
     * @return
     */
    public Prediction execute(KRun krun)
    {
        input.add(new CommandLine<>(EXECUTE, 5, krun.ordinal()));
        return this;
    }

    /**
     * The QUIT command causes termination of the ICEPAC program. It must,
     * therefore, be the last physical command line on the user-defined input
     * file.
     *
     * @return
     */
    public Prediction quit()
    {
        input.add(new CommandLine<>(QUIT, 5));
        return this;
    }

    /**
     * The TIME command indicates the time of day for which the analysis and
     * predictions are to be performed.
     *
     * @param ihro indicates the starting hour in universal time or in local
     * mean time at the transmitter.
     * @param ihre indicates the ending hour in universal time or in local mean
     * time at the transmitter.
     * @param ihrs indicates the hourly increment. The hourly increment is added
     * to the starting hour to determine the next hour. This incremental process
     * continues until the ending hour is reached.
     * @param ut indicates that the specified time is universal time or local
     * mean time.
     * @return
     */
    public Prediction time(int ihro, int ihre, int ihrs, boolean ut)
    {
        input.add(new CommandLine<>(TIME, 5, ihro, ihre, ihrs, ut ? 1 : -1));
        return this;
    }

    /**
     * The MONTH command indicates the year and months for which the analysis
     * and prediction are to be performed.
     *
     * @param nyear indicates the year, but has no effect on the program
     * calculations.
     * @param months The program analysis and predictions are performed for each
     * of the months the user specifies. The desired month can be specified in
     * any order.
     * @return
     */
    public Prediction month(int nyear, Month... months)
    {
        int[] arr = new int[months.length + 1];
        arr[0] = nyear;
        int index = 1;
        for (Month m : months)
        {
            arr[index++] = m.getValue();
        }
        input.add(new CommandLine<>(MONTH, 5, arr));
        return this;
    }

    /**
     * The sunspot command line indicates the sunspot numbers of the solar
     * activity period of interest and is the 12-month smoothed mean for each of
     * the months specified.
     *
     * @param sunspot
     * @return
     */
    public Prediction sunspot(double... sunspot)
    {
        input.add(new CommandLine<>(SUNSPOT, 5, sunspot));
        return this;
    }

    /**
     * The LABEL command contains alphanumeric information used to describe the
     * system location on both the input and output.
     *
     * @param itran is an array of 20 alphanumeric characters used to describe
     * the transmitter location.
     * @param ircvr is an array of 20 alphanumeric characters used to describe
     * the receiver location.
     * @return
     */
    public Prediction label(String itran, String ircvr)
    {
        input.add(new CommandLine<>(LABEL, 20, itran, ircvr));
        return this;
    }

    /**
     * The CIRCUIT command contains the geographic coordinates of the
     * transmitter and receiver and a variable to indicate the user's choice
     * between shorter or longer great circle paths from the transmitter to the
     * receiver.
     *
     * @param transmitter
     * @param receiver
     * @param shorter
     * @return
     */
    public Prediction circuit(Location transmitter, Location receiver, boolean shorter)
    {
        input.add(new CommandLine<>(CIRCUIT, 5,
                Math.abs(transmitter.getLatitude()),
                transmitter.getLatitudeNS(),
                Math.abs(transmitter.getLongitude()),
                transmitter.getLongitudeWE(),
                Math.abs(receiver.getLatitude()),
                receiver.getLatitudeNS(),
                Math.abs(receiver.getLongitude()),
                receiver.getLongitudeWE(),
                shorter ? 0 : 1
        ));
        return this;
    }

    /**
     * The SYSTEM command line includes parameters necessary to define the
     * system configuration.
     *
     * @param pwr PWR indicates the transmitter power in kilowatts.
     * @param noise XNOISE indicates the expected man-made noise level.
     * @param rsn RSN indicates the required signal-to-noise and is the ratio of
     * the hourly median signal power in the occupied bandwidth relative to the
     * hourly median noise in a 1 Hz bandwidth, which is necessary to provide
     * the type and quality of service required (expressed in decibels).
     * @return
     */
    public Prediction system(double pwr, Noise noise, RSN rsn)
    {
        return system(pwr, noise.getValue(), 3, 90, rsn.getSnRatio(), 3, 0.1);
    }

    /**
     * The SYSTEM command line includes parameters necessary to define the
     * system configuration.
     *
     * @param pwr PWR indicates the transmitter power in kilowatts.
     * @param xnoise XNOISE indicates the expected man-made noise level at the
     * receiver in dBW (decibels below lW) in a 1 Hz bandwidth at 3 MHz.
     * @param amind AMIND indicates the minimum takeoff angle in degrees.
     * @param xlufp XLUFP indicates the required circuit reliability, which is
     * an estimate of the percentage of days within the month that the signal
     * quality will be acceptable, and should be specified for calculation of
     * the LUF or time availability for service probability. (NOTE: XLUFP is
     * expressed as a percentage.)
     * @param rsn RSN indicates the required signal-to-noise and is the ratio of
     * the hourly median signal power in the occupied bandwidth relative to the
     * hourly median noise in a 1 Hz bandwidth, which is necessary to provide
     * the type and quality of service required (expressed in decibels).
     * @param pmp PMP indicates the maximum difference in delayed signal power
     * in decibels between sky-wave modes to permit satisfactory system
     * performance in the presence of multiple signals. If PMP is blank or zero,
     * multi-path is not considered.
     * @param dmpx DMPX indicates the maximum difference in delay time in
     * milliseconds between sky-wave propagation modes to permit satisfactory
     * system performance in the presence of multiple signals.
     * @return
     */
    public Prediction system(double pwr, double xnoise, double amind, double xlufp, double rsn, double pmp, double dmpx)
    {
        input.add(new CommandLine<>(SYSTEM, 5, pwr, xnoise, amind, xlufp, rsn, pmp, dmpx));
        return this;
    }

    /**
     * The FREQUENCY complement command line contains up to 11 user-defined
     * frequencies that are used in the calculation.
     *
     * @param frel FREL is the array of up to 11 user-defined frequencies in
     * megahertz.
     * @return
     */
    public Prediction frequency(double... frel)
    {
        input.add(new CommandLine<>(SYSTEM, 5, frel));
        return this;
    }

    private File exePath()
    {
        return new File(itshfbc, "bin_win/" + module);
    }

    private File runPath()
    {
        return new File(itshfbc, "run");
    }
}
