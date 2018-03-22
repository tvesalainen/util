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
package org.vesalainen.ham.oscilloscope;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LineDialog extends JDialog implements ActionListener
{
    private double sampleFrequency;
    private int bitCount;
    private double refreshInterval;
    private JComboBox rateCombo;
    private JComboBox bitsCombo;
    private JComboBox mixerCombo;
    private JComboBox refreshCombo;
    public LineDialog()
    {
        init();
    }
    private void init()
    {
        JPanel panel = new JPanel();
        add(panel);
        // sample frequency
        JLabel rateLabel = new JLabel("Sample Rate");
        add(rateLabel);
        rateCombo = new JComboBox(new String[]{"44000", "22000"});
        add(rateCombo);
        rateCombo.addActionListener(this);
        // bits
        JLabel bitsLabel = new JLabel("Bit Count");
        add(bitsLabel);
        bitsCombo = new JComboBox(new String[]{"8", "16", "32"});
        add(bitsCombo);
        bitsCombo.addActionListener(this);
        // mixer
        JLabel mixerLabel = new JLabel("Mixer");
        add(mixerLabel);
        mixerCombo = new JComboBox();
        add(mixerCombo);
        // refresh
        JLabel refreshLabel = new JLabel("Refresh Interval");
        add(refreshLabel);
        refreshCombo = new JComboBox(new String[]{"1", "0.5", "0.25"});
        add(refreshCombo);
        populateMixerCombo();
    }
    private void populateMixerCombo()
    {
        mixerCombo.removeAllItems();
        AudioFormat audioFormat = new AudioFormat((float) getSampleFrequency(), getBitCount(), 1, true, false);
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
        {
            Mixer mixer = AudioSystem.getMixer(mixerInfo);
            int maxLines = mixer.getMaxLines(info);
            if (maxLines != 0)
            {
                mixerCombo.addItem(mixer);
            }
        }
    }

    public double getSampleFrequency()
    {
        return Double.parseDouble(rateCombo.getSelectedItem().toString());
    }

    public int getBitCount()
    {
        return Integer.parseInt(bitsCombo.getSelectedItem().toString());
    }

    public double getRefreshInterval()
    {
        return Double.parseDouble(refreshCombo.getSelectedItem().toString());
    }
    
    @Override
    public void actionPerformed(ActionEvent e)
    {
        populateMixerCombo();
    }
    
}
