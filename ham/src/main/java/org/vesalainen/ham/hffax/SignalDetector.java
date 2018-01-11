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
package org.vesalainen.ham.hffax;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.vesalainen.ham.DataListener;
import org.vesalainen.ham.fft.FFT;
import org.vesalainen.math.sliding.SlidingAverage;
import org.vesalainen.math.sliding.SlidingMax;
import org.vesalainen.nio.IntArray;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class SignalDetector implements DataListener
{
    private static final double LIMIT = 12.0;
    private FFT fft;
    private float sampleRate;
    private float[] freqs;
    private double signalNoiseRatio;
    private IntArray in;
    private IntArray out;
    private FaxStateListener stateListener;
    private SlidingMax slidingaMax;
    private boolean hasSignal;
    private ExecutorService executor;
    private Future<?> future;

    public SignalDetector(FaxStateListener stateListener, int n, float sampleRate, float... freqs)
    {
        this.stateListener = stateListener;
        this.fft = new FFT(n);
        this.sampleRate = sampleRate;
        this.freqs = freqs;
        this.in = IntArray.getInstance(n);
        this.out = IntArray.getInstance(n);
        this.slidingaMax = new SlidingMax(20);
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void data(IntArray sample)
    {
        if (future == null || future.isDone())
        {
            sample.copy(in);
            future = executor.submit(this::calc);
        }
    }
    public void calc()
    {
        fft.fft(true, in, out);
        double average = FFT.average(sampleRate, out);
        double strength = 0;
        for (float freq : freqs)
        {
            double frequencyStrength = FFT.frequencyStrength(sampleRate, freq, out);
            strength = Math.max(frequencyStrength, strength);
        }
        signalNoiseRatio = strength/average;
        System.err.println(signalNoiseRatio);
        slidingaMax.accept(signalNoiseRatio);
        double a = slidingaMax.getMax();
        boolean s = a > LIMIT;
        if (hasSignal && !s)
        {
            //stateListener.stop();
        }
        hasSignal = s;
    }
}
