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

import static java.awt.BorderLayout.*;
import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingWorker;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
 */
public class MorseTeacher extends JDialog implements KeyListener, ActionListener, ChangeListener
{
    private volatile CountDownLatch latch;
    private volatile Consumer<Character> errorConsumer;
    private volatile CharSequence seq;
    private volatile int count;
    private volatile boolean ok;
    private JSpinner wpmSpinner;
    private JLabel infoLabel;
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> future;
    private SwingWorker<Void, Void> task;
    
    public MorseTeacher()
    {
        super((Dialog)null, "Key Scanner");
        init();
    }

    private void init()
    {
        JButton startButton = new JButton("Start");
        add(startButton, WEST);
        startButton.addActionListener(this);
        startButton.addKeyListener(this);
        JButton cancelButton = new JButton("Cancel");
        add(cancelButton, EAST);
        cancelButton.addActionListener(this);
        cancelButton.addKeyListener(this);
        infoLabel = new JLabel("blaa blaa blaa blaa blaa");
        add(infoLabel, CENTER);
        wpmSpinner = new JSpinner(new SpinnerNumberModel(15, 5, 60, 1));
        add(wpmSpinner, NORTH);
        wpmSpinner.addChangeListener(this);
        wpmSpinner.addKeyListener(this);
        pack();
        setVisible(true);
        addKeyListener(this);
    }
    public void teach()
    {
        try (MorseCode mc = MorseCode.getInstance((int) wpmSpinner.getValue(), 700))
        {
            List<Character> list = new ArrayList<>();
            long space = mc.key(" ");
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
                    boolean ok = input(keyed, (cc)->list.add(cc));
                    long elapsed = System.currentTimeMillis() - begin;
                    if (!ok)
                    {
                        infoLabel.setText(keyed+" = "+mc.toString(keyed));
                        Collections.shuffle(list, rand);
                        errors++;
                    }
                    else
                    {
                        if (elapsed > time + 2*space)
                        {
                            infoLabel.setText("you are too slow!");
                            list.addAll(sub);
                            Collections.shuffle(list, rand);
                        }
                        else
                        {
                            list.removeAll(sub);
                            infoLabel.setText("Ok!");
                        }
                    }
                }
            }
            infoLabel.setText("PASSED!!!");
        }
        catch (LineUnavailableException | InterruptedException ex)
        {
            throw new RuntimeException(ex);
        }
    }
    public boolean input(CharSequence seq, Consumer<Character> errorConsumer) throws InterruptedException
    {
        this.seq = seq;
        this.count = 0;
        this.ok = true;
        this.errorConsumer = errorConsumer;
        latch = new CountDownLatch(seq.length());
        latch.await();
        latch = null;
        return ok;
    }
    @Override
    public void keyTyped(KeyEvent e)
    {
        if (latch != null)
        {
            char got = Character.toUpperCase(e.getKeyChar());
            char exp = seq.charAt(count++);
            if (got != exp)
            {
                errorConsumer.accept(got);
                errorConsumer.accept(exp);
                ok = false;
            }
            latch.countDown();
        }
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
    }

    @Override
    public void actionPerformed(ActionEvent e)
    {
        switch (e.getActionCommand())
        {
            case "Start":
                if (task == null)
                {
                    wpmSpinner.setEnabled(false);
                    task = new SwingWorker<Void, Void>()
                    {

                        @Override
                        protected Void doInBackground() throws Exception
                        {
                            teach();
                            return null;
                        }
                    };
                    task.execute();
                }
                break;
            case "Cancel":
                if (task != null)
                {
                    wpmSpinner.setEnabled(true);
                    task.cancel(true);
                    task = null;
                }
                break;
        }
    }

    @Override
    public void stateChanged(ChangeEvent e)
    {
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
