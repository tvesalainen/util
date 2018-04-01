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

import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import org.vesalainen.comm.channel.SerialChannel;
import org.vesalainen.ham.oscilloscope.ui.DoubleComboBox;
import org.vesalainen.ham.oscilloscope.ui.GroupLayoutBuilder;
import org.vesalainen.ham.oscilloscope.ui.IntegerComboBox;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class LineDialog extends JDialog implements ActionListener
{
    private DoubleComboBox rateCombo;
    private IntegerComboBox bitsCombo;
    private JComboBox<Mixer.Info> mixerCombo;
    private JComboBox<String> agcCombo;
    private DoubleComboBox refreshCombo;
    private boolean accepted;
    public LineDialog(Frame owner, String title)
    {
        super(owner, title);
        init();
    }
    private void init()
    {
        // sample frequency
        JLabel rateLabel = new JLabel("Sample Rate");
        add(rateLabel);
        rateCombo = new DoubleComboBox(48000.0, 41000.0, 32000.0, 22050.0, 11025.0, 8000.0);
        rateCombo.setEditable(true);
        add(rateCombo);
        rateCombo.addActionListener(this);
        // bits
        JLabel bitsLabel = new JLabel("Bit Count");
        add(bitsLabel);
        bitsCombo = new IntegerComboBox(8, 16, 32);
        bitsCombo.setSelectedIndex(1);
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
        refreshCombo = new DoubleComboBox(1.0, 0.5, 0.25, 0.01, 0.005);
        add(refreshCombo);
        populateMixerCombo();
        // agc
        JLabel agcLabel = new JLabel("AGC");
        add(agcLabel);
        agcCombo = new JComboBox();
        add(agcCombo);
        populateAGCCombo();
        
        JButton okButton = new JButton(new OkAction());
        add(okButton);
        JButton cancelButton = new JButton(new CancelAction());
        add(cancelButton);
        //layout
        GroupLayout layout = GroupLayoutBuilder.builder(getContentPane())
                .addLine(rateLabel, rateCombo)
                .addLine(bitsLabel, bitsCombo)
                .addLine(mixerLabel, mixerCombo)
                .addLine(refreshLabel, refreshCombo)
                .addLine(agcLabel, agcCombo)
                .addLine(cancelButton, okButton)
                .build();
        setLayout(layout);
        getRootPane().setDefaultButton(okButton);
        setModalityType(Dialog.ModalityType.TOOLKIT_MODAL);
        setLocation(100, 100);
        pack();
    }
    public boolean edit()
    {
        accepted = false;
        setVisible(true);
        return accepted;
    }

    private void populateMixerCombo()
    {
        mixerCombo.removeAllItems();
        AudioFormat audioFormat = getAudioFormat();
        DataLine.Info info = new DataLine.Info(TargetDataLine.class, audioFormat);
        for (Mixer.Info mixerInfo : AudioSystem.getMixerInfo())
        {
            try (Mixer mixer = AudioSystem.getMixer(mixerInfo))
            {
                int maxLines = mixer.getMaxLines(info);
                if (maxLines != 0)
                {
                    mixerCombo.addItem(mixerInfo);
                }
            }
        }
    }

    public AudioFormat getAudioFormat()
    {
        return new AudioFormat((float) getSampleFrequency(), getBitCount(), 1, true, false);
    }
    public double getSampleFrequency()
    {
        return rateCombo.getSelectedItem();
    }

    public int getBitCount()
    {
        return bitsCombo.getSelectedItem();
    }

    public double getRefreshInterval()
    {
        return refreshCombo.getSelectedItem();
    }
    
    public String getAGCPort()
    {
        return (String) agcCombo.getSelectedItem();
    }
    public Mixer.Info getMixerInfo()
    {
        return (Mixer.Info) mixerCombo.getSelectedItem();
    }
    @Override
    public void actionPerformed(ActionEvent e)
    {
        populateMixerCombo();
    }

    private void populateAGCCombo()
    {
        agcCombo.addItem("----");
        for (String port : SerialChannel.getFreePorts())
        {
            agcCombo.addItem(port);
        }
    }

    private class OkAction extends AbstractAction
    {

        public OkAction()
        {
            super("Ok");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            accepted = true;
            setVisible(false);
        }
        
    }
    private class CancelAction extends AbstractAction
    {

        public CancelAction()
        {
            super("Cancel");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            setVisible(false);
        }
        
    }
}
