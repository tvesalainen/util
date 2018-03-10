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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.vesalainen.ham.itshfbc.Command.*;
import org.vesalainen.parsers.printf.FormatFactory;
import org.vesalainen.parsers.printf.FormatFactory.FormatPart;
import org.vesalainen.parsers.printf.PrintfParser;
import org.vesalainen.util.OSProcess;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class Predictor
{

    private Path itshfbc = Paths.get("C:\\itshfbc");
    private String module = "voacapw";
    private List<CommandLine> input = new ArrayList<>();
    private FormatFactory formatFactory;
    private PrintfParser prinfParser;
    private List<FormatPart> monthLine;
    private List<FormatPart> freqLine;
    private List<FormatPart> modeLine;
    private List<FormatPart> dataLine;
    private Prediction prediction;
    private Location receiverLocation;

    public Predictor()
    {
        formatFactory = new FormatFactory();
        prinfParser = PrintfParser.getInstance();
        monthLine = prinfParser.parse("  %3.3s    %4d          SSN =  %3f                Minimum Angle= %5.3f degrees", formatFactory);
        freqLine = prinfParser.parse(" %5f%5f%5f%5f%5f%5f%5f%5f%5f%5f%5f%5f%5f FREQ", formatFactory);
        modeLine = prinfParser.parse("      %5s%5s%5s%5s%5s%5s%5s%5s%5s%5s%5s%5s %10.10s", formatFactory);
        dataLine = prinfParser.parse("      %5f%5f%5f%5f%5f%5f%5f%5f%5f%5f%5f%5f %10.10s", formatFactory);
    }

    public Prediction predict() throws IOException
    {
        Path runPath = runPath();
        Path exePath = exePath();
        Path dat = runPath.resolve("hffax.dat");
        Path out = runPath.resolve("hffax.out");
        Files.deleteIfExists(dat);
        Files.deleteIfExists(out);
        Files.createFile(dat);
        Files.createFile(out);
        try (BufferedWriter outw = Files.newBufferedWriter(dat))
        {
            for (CommandLine line : input)
            {
                outw.append(line.toString()).append("\r\n");
            }
        }
        try
        {
            int rc = OSProcess.call(exePath.toString(), "SILENT", itshfbc.toString(), dat.getFileName().toString(), out.getFileName().toString());
            parseOutput(out);
        }
        catch (InterruptedException ex)
        {
            Logger.getLogger(Predictor.class.getName()).log(Level.SEVERE, null, ex);
        }
        return prediction;
    }
    private void parseOutput(Path out) throws IOException
    {
        try (BufferedReader reader = Files.newBufferedReader(out))
        {
            String line = reader.readLine();
            while (line != null)
            {
                if (line.endsWith("degrees"))
                {
                    if (prediction == null)
                    {
                        Object[] arr = formatFactory.parse(monthLine, line);
                        prediction = new Prediction(receiverLocation, (String)arr[0]);
                    }
                }
                else
                {
                    if (line.endsWith("FREQ"))
                    {
                        Object[] arr = formatFactory.parse(freqLine, line);
                        HourPrediction hp = new HourPrediction(arr);
                        prediction.addHour(hp);
                        line = reader.readLine();
                        arr = formatFactory.parse(modeLine, line);
                        hp.addAttribute(arr);
                        line = reader.readLine();
                        while (line.length() > 10 && line.charAt(0) != '\f' && !line.startsWith(" *****END OF RUN*****"))
                        {
                            arr = formatFactory.parse(dataLine, line.replace('-', '0'));
                            hp.addAttribute(arr);
                            line = reader.readLine();
                        }
                    }
                }
                line = reader.readLine();
            }
        }
    }
    /**
     * The METHOD command defines the analysis task to be performed for a
     * particular system configuration.
     *
     * @param method
     * @return
     */
    public Predictor method(int method, int startPage)
    {
        input.add(new CommandLine<>(METHOD, 5, method, startPage));
        return this;
    }

    /**
     * The EXECUTE command causes the program to perform the indicated analysis
     * task for the currently defined system configuration.
     *
     * @return
     */
    public Predictor execute()
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
    public Predictor execute(KRun krun)
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
    public Predictor quit()
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
    public Predictor time(int ihro, int ihre, int ihrs, boolean ut)
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
    public Predictor month(int nyear, Month... months)
    {
        Object[] arr = new Object[months.length + 1];
        arr[0] = nyear;
        int index = 1;
        for (Month m : months)
        {
            arr[index++] = Double.valueOf(m.getValue());
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
    public Predictor sunspot(double... sunspot)
    {
        CommandLine<Double> line = new CommandLine<>(SUNSPOT, 5);
        for (double s : sunspot)
        {
            line.add(s);
        }
        input.add(line);
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
    public Predictor label(String itran, String ircvr)
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
    public Predictor circuit(Location transmitter, Location receiver, boolean shorter)
    {
        this.receiverLocation = receiver;
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
    public Predictor system(double pwr, Noise noise, RSN rsn)
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
    public Predictor system(double pwr, double xnoise, double amind, double xlufp, double rsn, double pmp, double dmpx)
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
    public Predictor frequency(double... frel)
    {
        CommandLine<Double> line = new CommandLine<>(FREQUENCY, 5);
        for (double f : frel)
        {
            line.add(f);
        }
        input.add(line);
        return this;
    }
    public Predictor antenna(boolean transmitter, int number, int minFreq, int maxFreq, int designFreq, int mainBeam, double txPower, Path antenna)
    {
        input.add(new AntennaLine(transmitter, number, minFreq, maxFreq, designFreq, mainBeam, txPower, antenna));
        return this;
    }
    public Predictor comment(String text)
    {
        input.add(new CommandLine<>(COMMENT, 65, text));
        return this;
    }
    public Predictor lineMax(int lineMax)
    {
        input.add(new CommandLine<>(LINEMAX, 5, lineMax));
        return this;
    }
    public Predictor coeffs(Coeffs coeffs)
    {
        input.add(new CommandLine<>(COEFFS, 5, coeffs));
        return this;
    }

    private Path exePath()
    {
        return itshfbc.resolve("bin_win").resolve(module);
    }

    private Path runPath()
    {
        return itshfbc.resolve("run");
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        for (CommandLine cmd : input)
        {
            sb.append(cmd.toString()).append("\r\n");
        }
        return sb.toString();
    }
    
    public class AntennaLine extends CommandLine<Object>
    {
        private double designFreq;
        private double mainBeam;
        private double txPower;
        private Path antenna;

        public AntennaLine(boolean transmitter, int number, int minFreq, int maxFreq, double designFreq, double mainBeam, double txPower, Path antenna)
        {
            super(ANTENNA, 5);
            add(transmitter ? 1 : 2);
            add(number);
            add(minFreq);
            add(maxFreq);
            this.designFreq = designFreq;
            this.mainBeam = mainBeam;
            this.txPower = txPower;
            this.antenna = antenna;
        }

        @Override
        public String toString()
        {
            return String.format(Locale.US, "%s%10.3f[%-21.21s]%5.1f%10.4f", 
                    super.toString(),
                    designFreq,
                    antenna,
                    mainBeam, 
                    txPower
                    );
        }

    }
}
