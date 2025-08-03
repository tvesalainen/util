/*
 * Copyright (C) 2014 Timo Vesalainen <timo.vesalainen@iki.fi>
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

import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vesalainen.util.concurrent.SimpleWorkflow.ContextAccess;

/**
 *
 * @author Timo Vesalainen <timo.vesalainen@iki.fi>
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
        String msg = instance.switchTo(5, "msg5");
        assertEquals("msg5", msg);
        assertEquals(6, instance.getThreadCount());
        msg = instance.switchTo(5, "msg6");
        assertEquals("msg6", msg);
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
    @Test
    public void testTimeout()
    {
        try
        {
            System.out.println("Timeout");
            UnparallelWorkflowImpl2 instance = new UnparallelWorkflowImpl2(0);
            assertEquals(1, instance.getThreadCount());
            Thread.sleep(1000);
            assertEquals(1, instance.getThreadCount());
            instance.switchTo(1);
            assertEquals(2, instance.getThreadCount());
            Thread.sleep(2000);
            assertEquals(1, instance.getThreadCount());
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
        catch (InterruptedException ex)
        {
            ex.printStackTrace();
            fail(ex.getMessage());
        }
    }
    @Test
    public void testEndTo()
    {
        System.out.println("endTo");
        Counter counter = new Counter();
        UnparallelWorkflowImpl3 instance = new UnparallelWorkflowImpl3(0, counter);
        instance.fork(1);
        assertEquals(2, instance.getThreadCount());
        instance.join();
        assertEquals(1, counter.number);
        assertEquals(1, instance.getThreadCount());
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
    public class UnparallelWorkflowImpl1 extends SimpleWorkflow<Integer,String,Object>
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
                String m = wf.getMessage();
                wf.switchTo(number-1, m);
            }
        }
        
    }
    public class UnparallelWorkflowImpl2 extends SimpleWorkflow<Integer,String,Object>
    {

        public UnparallelWorkflowImpl2(int number)
        {
            super(number, null, 1, 500, TimeUnit.MILLISECONDS);
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
    public class UnparallelWorkflowImpl3 extends SimpleWorkflow<Integer,String,Counter>
    {

        public UnparallelWorkflowImpl3(int number, Counter counter)
        {
            super(number, counter);
        }

        @Override
        protected Runnable create(Integer key)
        {
            return new Worker3(key, this);
        }
    }
    public class Worker3 implements Runnable
    {
        private final int number;
        private final UnparallelWorkflowImpl3 wf;

        public Worker3(int number, UnparallelWorkflowImpl3 wf)
        {
            this.number = number;
            this.wf = wf;
        }

        @Override
        public void run()
        {
            try
            {
                ContextAccess<Counter,Void> ca = new ContextAccess<Counter,Void>() 
                {
                    @Override
                    public Void access(Counter counter)
                    {
                        counter.number++;
                        return null;
                    }
                };
                wf.accessContext(ca);
                Thread.sleep(1000);
                switch (number)
                {
                    case 1:
                        wf.endTo(0);
                        break;
                }
            }
            catch (InterruptedException ex)
            {
                throw new IllegalArgumentException(ex);
            }
        }
        
    }
    public class Counter
    {
        int number;
    }
}
