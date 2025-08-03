/*
 * Copyright (C) 2021 Timo Vesalainen <timo.vesalainen@iki.fi>
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
package org.vesalainen.navi;

import static java.lang.Math.*;

/**
 * Calculates different types of true wind.
 * <p>For traditional boat referenced true wind:
 * <code>
 * setZeroAngle(trueHeading);
 * setTrueHeading(trueHeading);
 * setSpeed(waterSpeed);
 * setSpeedAngle(trueHeading);
 * </code>
 * <p>For ground referenced true wind:
 * <code>
 * setZeroAngle(trueHeading); ??? TODO
 * setTrueHeading(trueHeading);
 * setSpeed(sog);
 * setSpeedAngle(trackMadeGoode);
 * </code>
 * <p>Other combinations are possible.
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class TrueWindCalculator
{
    private double zeroAngle;
    private double relativeWindAngle;
    private double relativeWindSpeed;
    private double trueHeading;
    private double speedAngle;
    private double speed;
    private boolean needCalc;
    private double cx;
    private double cy;
    /**
     * Set the reference angle. Mostly same as true heading.
     * @param zeroAngle 
     */
    public void setZeroAngle(double zeroAngle)
    {
        this.zeroAngle = zeroAngle;
    }
    /**
     * Set relative wind angle in degrees
     * @param relativeWindAngle 
     */
    public void setRelativeWindAngle(double relativeWindAngle)
    {
        this.relativeWindAngle = toRadians(relativeWindAngle);
        needCalc = true;
    }
    /**
     * Set relative wind speed in knots
     * @param relativeWindSpeed 
     */
    public void setRelativeWindSpeed(double relativeWindSpeed)
    {
        this.relativeWindSpeed = relativeWindSpeed;
        needCalc = true;
    }
    /**
     * Set true heading in degrees.
     * @param trueHeading 
     */
    public void setTrueHeading(double trueHeading)
    {
        this.trueHeading = toRadians(trueHeading);
        needCalc = true;
    }
    /**
     * Set speed angle in degrees. 
     * @param speedAngle 
     */
    public void setSpeedAngle(double speedAngle)
    {
        this.speedAngle = toRadians(speedAngle);
        needCalc = true;
    }
    /**
     * Set speed in knots.
     * @param speed 
     */
    public void setSpeed(double speed)
    {
        this.speed = speed;
        needCalc = true;
    }
    
    private void correctSpeed()
    {
        if (needCalc)
        {
            double trueWindAngle = trueHeading + relativeWindAngle;
            double a = speedAngle;
            cx = cos(trueWindAngle)*relativeWindSpeed - cos(a)*speed;
            cy = sin(trueWindAngle)*relativeWindSpeed - sin(a)*speed;
            needCalc = false;
        }
    }
    /**
     * Returns true wind angle in degrees
     * @return 
     */
    public double getTrueWindAngle()
    {
        correctSpeed();
        return Navis.normalizeAngle(toDegrees(atan2(cy, cx)) - zeroAngle);
    }
    /**
     * Returns true wind speed in knots
     * @return 
     */
    public double getTrueWindSpeed()
    {
        correctSpeed();
        return hypot(cx, cy);
    }
}
