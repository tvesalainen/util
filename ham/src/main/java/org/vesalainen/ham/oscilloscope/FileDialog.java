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
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.vesalainen.ham.oscilloscope.ui.DoubleComboBox;
import org.vesalainen.ham.oscilloscope.ui.GroupLayoutBuilder;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class FileDialog extends JDialog
{

    private DoubleComboBox refreshCombo;
    private boolean accepted;
    private JTextField fileField;

    public FileDialog(Frame owner, String title)
    {
        super(owner, title);
        init();
    }

    private void init()
    {
        JLabel fileLabel = new JLabel("Audio File");
        add(fileLabel);
        fileField = new JTextField(50);
        add(fileField);
        JButton chooseButton = new JButton(new ChooseAction());
        add(chooseButton);
        // refresh
        JLabel refreshLabel = new JLabel("Refresh Interval");
        add(refreshLabel);
        refreshCombo = new DoubleComboBox(1.0, 0.5, 0.25, 0.01, 0.005);
        add(refreshCombo);
        JButton okButton = new JButton(new OkAction());
        add(okButton);
        JButton cancelButton = new JButton(new CancelAction());
        add(cancelButton);
        //layout
        GroupLayout layout = GroupLayoutBuilder.builder(getContentPane())
                .addLine(fileLabel, fileField, chooseButton)
                .addLine(refreshLabel, refreshCombo)
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
    public String getFilename()
    {
        return fileField.getText();
    }
    public double getRefreshInterval()
    {
        return refreshCombo.getSelectedItem();
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

    private class ChooseAction extends AbstractAction
    {

        public ChooseAction()
        {
            super("Choose");
        }

        @Override
        public void actionPerformed(ActionEvent e)
        {
            setVisible(false);
            try
            {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter(
                        "Wave Files", "wav");
                chooser.setFileFilter(filter);
                int rc = chooser.showOpenDialog(getParent());
                if (rc == JFileChooser.APPROVE_OPTION)
                {
                    fileField.setText(chooser.getSelectedFile().getPath());
                }
            }
            finally
            {
                setVisible(true);
            }
        }

    }
}
