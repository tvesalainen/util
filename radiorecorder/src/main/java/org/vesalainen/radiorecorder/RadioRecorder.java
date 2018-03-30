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
package org.vesalainen.radiorecorder;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import static java.util.logging.Level.*;
import javax.sound.sampled.LineUnavailableException;
import org.vesalainen.ham.AudioRecorder;
import org.vesalainen.ham.HfFax;
import org.vesalainen.ham.LocationParser;
import org.vesalainen.ham.Schedule;
import org.vesalainen.ham.TimeUtils;
import org.vesalainen.ham.bc.BroadcastOptimizer;
import org.vesalainen.ham.bc.BroadcastOptimizer.BestStation;
import org.vesalainen.ham.hffax.FaxDecoder;
import org.vesalainen.ham.hffax.FaxRectifier;
import org.vesalainen.ham.itshfbc.Noise;
import org.vesalainen.nmea.icommanager.IcomManager;
import org.vesalainen.util.LoggingCommandLine;
import org.vesalainen.util.concurrent.CachedScheduledThreadPool;
import org.vesalainen.util.navi.Location;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class RadioRecorder extends LoggingCommandLine
{
    private CachedScheduledThreadPool pool;
    private Path dataDirectory;
    private OffsetDateTime lastSchedule;
    private LocationService locationService;
    private String nmeaGroup;
    private int nmeaPort;
    private Location location;
    private int radioId;
    private String radioPort;
    private IcomManager icomManager;
    private String mixerName;
    private float sampleRate;
    private int sampleSizeInBits;
    private Path sunSpotNumberPath;
    private URL broadcastStationsPath;
    private Path transmitterAntennaPath;
    private Path receiverAntennaPath;
    private double minSNR;
    private Noise noise;
    private BroadcastOptimizer optimizer;
    private AudioRecorder audioRecorder;
    
    public RadioRecorder()
    {
        addArgument(File.class, "configuration file");
    }
    public Location getLocation()
    {
        if (locationService != null)
        {
            return locationService.getLocation();
        }
        else
        {
            return location;
        }
    }
    public void start() throws IOException, LineUnavailableException, InterruptedException
    {
        init();
        scheduleNext();
        pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
    private void scheduleNext()
    {
        try
        {
            BestStation bestStation = optimizer.bestStation(getLocation(), lastSchedule);
            OffsetDateTime start = TimeUtils.next(lastSchedule, bestStation.getFrom());
            OffsetDateTime end = TimeUtils.next(lastSchedule, bestStation.getTo());
            lastSchedule = end;
            OffsetDateTime nextSchedule = start.plusMinutes(1);
            String filename = String.format("%s_%s_%s_%02d_%02d_%02d",
                    bestStation.getEmissionClass(),
                    bestStation.getStation().getName(),
                    bestStation.getContent().replace('*', '_').replace('/', '_').replace('?', '_').replace('\\', '_').replace(':', '_'),
                    start.getDayOfMonth(),
                    start.getHour(),
                    start.getMinute()
            );
            Path wav = dataDirectory.resolve(filename+".wav");
            Path png = dataDirectory.resolve(filename+".png");
            Path cor = dataDirectory.resolve(filename+"_cor.png");
            Schedule schedule = bestStation.getSchedule();
            double frequency = bestStation.getFrequency();
            Runnable starter = null;
            if (schedule instanceof HfFax)
            {
                HfFax fax = (HfFax) schedule;
                int rpm = fax.getRpm();
                int ioc = fax.getIoc();
                starter = pool.concat(
                        ()->startRecording(frequency*1000-1.9, wav),
                        ()->startFax(rpm, ioc, wav, png),
                        ()->rectifyFax(png, cor)
                );
            }
            Runnable ender = pool.concat(
                    ()->stopRecording()
            );
            pool.schedule(starter, start);
            pool.schedule(ender, end);
            pool.schedule(this::scheduleNext, nextSchedule);
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "scheduleNext %s", ex.getMessage());
        }
    }
    private void startFax(int lpm, int ioc, Path in, Path out)
    {
        try
        {
            fine("startFax(%d, %d, %s, %s", lpm, ioc, in, out);
            FaxDecoder decoder = new FaxDecoder(lpm, ioc, in, out);
            decoder.parse();
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "startFax %s", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    private void rectifyFax(Path in, Path out)
    {
        try
        {
            fine("rectifyFax(%s, %s", in, out);
            FaxRectifier rectifier = new FaxRectifier(in, out);
            rectifier.rectify();
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "rectifyFax %s", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    private void startRecording(double kHz, Path file)
    {
        try
        {
            fine("startRecording(%f KHz %s)", kHz, file);
            icomManager.setRemote(true);
            icomManager.setReceiverFrequency(kHz/1000);
            audioRecorder.record(file);
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "startRecording %s", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    private void stopRecording()
    {
        try
        {
            fine("stopRecording()");
            audioRecorder.close();
        }
        catch (Exception ex)
        {
            log(SEVERE, ex, "stopRecording %s", ex.getMessage());
            throw new RuntimeException(ex);
        }
    }
    private void init() throws IOException, LineUnavailableException, InterruptedException
    {
        pool = new CachedScheduledThreadPool();
        pool.setLogLevel(Level.FINE);
        config("using %s as data directory", dataDirectory);
        if (location == null)
        {
            config("starting LocationService(%s, %d)", nmeaGroup, nmeaPort);
            locationService = new LocationService(nmeaGroup, nmeaPort, pool);
            locationService.start();
            locationService.getLocation();  // to wait until service is running
            Clock clock = LocationService.getClock();
            pool.setClock(clock);
            lastSchedule = OffsetDateTime.now(clock);
        }
        else
        {
            lastSchedule = OffsetDateTime.now(ZoneOffset.UTC);
            config("using %s as location", location);
        }
        if (radioPort != null && !radioPort.isEmpty())
        {
            config("starting IcomManager(%d, %s)", radioId, radioPort);
            icomManager = IcomManager.getInstance(radioId, radioPort);
        }
        else
        {
            try
            {
                config("starting IcomManager(%d)", radioId);
                icomManager = IcomManager.getInstance(radioId);
            }
            catch (InterruptedException ex)
            {
                throw new RuntimeException(ex);
            }
        }
        config("starting BroadcastOptimizer");
        optimizer = new BroadcastOptimizer();
        if (sunSpotNumberPath != null)
        {
            config("using SunSpotNumberPath(%s)", sunSpotNumberPath);
            optimizer.setSunSpotNumberPath(sunSpotNumberPath);
        }
        if (broadcastStationsPath != null)
        {
            config("using BroadcastStationsPath(%s)", broadcastStationsPath);
            optimizer.setBroadcastStationsPath(broadcastStationsPath);
        }
        if (transmitterAntennaPath != null)
        {
            config("using transmitterAntennaPath(%s)", transmitterAntennaPath);
            optimizer.setTransmitterAntennaPath(transmitterAntennaPath);
        }
        if (receiverAntennaPath != null)
        {
            config("using receiverAntennaPath(%s)", receiverAntennaPath);
            optimizer.setReceiverAntennaPath(receiverAntennaPath);
        }
        if (noise != null)
        {
            config("using Noise(%s)", noise);
            optimizer.setNoise(noise);
        }
        optimizer.setMinSNR(minSNR);
        config("starting AudioRecorder(%s, %f, %d)", mixerName, sampleRate, sampleSizeInBits);
        audioRecorder = new AudioRecorder(mixerName, sampleRate, sampleSizeInBits);
        AGC agc = new AGC(icomManager, 0.9, 0.5);
        audioRecorder.addListener(agc);
    }
    public void stop()
    {
        try
        {
            pool.shutdownNow();
            locationService.stop();
            icomManager.close();
            audioRecorder.close();
        }
        catch (IOException | InterruptedException ex)
        {
            log(Level.SEVERE, ex, "%s", ex.getMessage());
        }
    }

    @Setting("files.directory")
    public void setDataDirectory(String dataDirectory)
    {
        this.dataDirectory = Paths.get(dataDirectory);
    }

    @Setting("hfPropagationPrediction.minSnr")
    public void setMinSNR(double minSNR)
    {
        this.minSNR = minSNR;
    }

    @Setting("hfPropagationPrediction.sunSpotNumberPath")
    public void setSunSpotNumberPath(String sunSpotNumberPath)
    {
        this.sunSpotNumberPath = Paths.get(sunSpotNumberPath);
    }

    @Setting("hfPropagationPrediction.broadcastStationsPath")
    public void setBroadcastStationsPath(String BroadcastStationsPath)
    {
        try
        {
            this.broadcastStationsPath = new URL(BroadcastStationsPath);
        }
        catch (MalformedURLException ex)
        {
            throw new RuntimeException(ex);
        }
    }

    @Setting("hfPropagationPrediction.transmitterAntennaPath")
    public void setTransmitterAntennaPath(String transmitterAntennaPath)
    {
        this.transmitterAntennaPath = Paths.get(transmitterAntennaPath);
    }

    @Setting("hfPropagationPrediction.receiverAntennaPath")
    public void setReceiverAntennaPath(String receiverAntennaPath)
    {
        this.receiverAntennaPath = Paths.get(receiverAntennaPath);
    }

    @Setting("hfPropagationPrediction.noise")
    public void setNoise(String noise)
    {
        this.noise = Noise.valueOf(noise);
    }

    @Setting("audioCapture.mixer")
    public void setMixerName(String mixerName)
    {
        this.mixerName = mixerName;
    }

    @Setting("audioCapture.sampleRate")
    public void setSampleRate(float sampleRate)
    {
        this.sampleRate = sampleRate;
    }

    @Setting("audioCapture.sampleSize")
    public void setSampleSizeInBits(int sampleSizeInBits)
    {
        this.sampleSizeInBits = sampleSizeInBits;
    }

    @Setting("radioControl.icomHfMarine.id")
    public void setRadioId(int radioId)
    {
        this.radioId = radioId;
    }

    @Setting("radioControl.icomHfMarine.port")
    public void setRadioPort(String radioPort)
    {
        this.radioPort = radioPort;
    }

    @Setting("receiverLocation.coordinates.coordinates")
    public void setLocation(String location)
    {
        this.location = LocationParser.parse(location);
    }
    
    @Setting("receiverLocation.nmeaMulticast.nmeaGroup")
    public void setNmeaGroup(String nmeaGroup)
    {
        this.nmeaGroup = nmeaGroup;
    }
    @Setting("receiverLocation.nmeaMulticast.nmeaPort")
    public void setNmeaPort(int nmeaPort)
    {
        this.nmeaPort = nmeaPort;
    }

    public static void main(String... args)
    {
        try
        {
            RadioRecorder recorder = new RadioRecorder();
            recorder.command(args);
            File configfile = (File) recorder.getArgument("configuration file");
            ConfigFile config = new ConfigFile(configfile);
            config.load();
            config.attachInstant(recorder);
            Runtime.getRuntime().addShutdownHook(new Thread(recorder::stop));
            recorder.start();
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }
    }
}
