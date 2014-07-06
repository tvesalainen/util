/*
 * Copyright (C) 2014 tkv
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

package org.vesalainen.util.concurrent;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author tkv
 */
public class SimpleWorkflowTest
{
    
    public SimpleWorkflowTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of switchTo method, of class SimpleWorkflow.
     */
    @Test
    public void testSwitchThread1()
    {
        System.out.println("switchThread");
        UnparallelWorkflowImpl1 instance = new UnparallelWorkflowImpl1(0);
        assertEquals(1, instance.getThreadCount());
        instance.switchTo(5);
        assertEquals(6, instance.getThreadCount());
        instance.switchTo(5);
        assertEquals(6, instance.getThreadCount());
        instance.kill(4);
        assertEquals(5, instance.getThreadCount());
        instance.waitAndStopThreads();
        try
        {
            instance.switchTo(5);
            fail("should throw exception");
        }
        catch (IllegalStateException ex)
        {

        }
    }

    @Test
    public void testSwitchThread2()
    {
        System.out.println("switchThread2");
        UnparallelWorkflowImpl1 instance = new UnparallelWorkflowImpl1(0);
        assertEquals(1, instance.getThreadCount());
        instance.switchTo(1);
        assertEquals(2, instance.getThreadCount());
        instance.waitAndStopThreads();
        try
        {
            instance.switchTo(1);
            fail("should throw exception");
        }
        catch (IllegalStateException ex)
        {

        }
    }
    @Test
    public void testForkJoin1()
    {
        System.out.println("ForkJoin1");
        UnparallelWorkflowImpl2 instance = new UnparallelWorkflowImpl2(0);
        assertEquals(1, instance.getThreadCount());
        instance.switchTo(1);
        assertEquals(2, instance.getThreadCount());
        instance.waitAndStopThreads();
        try
        {
            instance.switchTo(1);
            fail("should throw exception");
        }
        catch (IllegalStateException ex)
        {

        }
    }
    public class UnparallelWorkflowImpl1 extends SimpleWorkflow<Integer>
    {

        public UnparallelWorkflowImpl1(int number)
        {
            super(number);
        }

        @Override
        protected Runnable create(Integer key)
        {
            return new Worker1(key, this);
        }
    }
    public class Worker1 implements Runnable
    {
        private final int number;
        private final UnparallelWorkflowImpl1 wf;

        public Worker1(int number, UnparallelWorkflowImpl1 wf)
        {
            this.number = number;
            this.wf = wf;
        }

        @Override
        public void run()
        {
            while (true)
            {
                wf.switchTo(number-1);
            }
        }
        
    }
    public class UnparallelWorkflowImpl2 extends SimpleWorkflow<Integer>
    {

        public UnparallelWorkflowImpl2(int number)
        {
            super(number);
        }

        @Override
        protected Runnable create(Integer key)
        {
            return new Worker2(key, this);
        }
    }
    public class Worker2 implements Runnable
    {
        private final int number;
        private final UnparallelWorkflowImpl2 wf;

        public Worker2(int number, UnparallelWorkflowImpl2 wf)
        {
            this.number = number;
            this.wf = wf;
        }

        @Override
        public void run()
        {
            wf.fork(0);
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException ex)
            {
                throw new IllegalArgumentException(ex);
            }
            wf.join();
        }
        
    }
}
