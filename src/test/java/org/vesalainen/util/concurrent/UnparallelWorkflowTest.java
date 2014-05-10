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
import org.junit.Before;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 *
 * @author tkv
 */
public class UnparallelWorkflowTest
{
    
    public UnparallelWorkflowTest()
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
     * Test of switchTo method, of class UnparallelWorkflow.
     */
    @org.junit.Test
    public void testSwitchThread()
    {
        System.out.println("switchThread");
        UnparallelWorkflowImpl instance = new UnparallelWorkflowImpl(0);
        assertEquals(1, instance.getThreadCount());
        instance.switchTo(5);
        assertEquals(6, instance.getThreadCount());
        instance.switchTo(5);
        assertEquals(6, instance.getThreadCount());
        instance.kill(4);
        assertEquals(5, instance.getThreadCount());
    }

    public class UnparallelWorkflowImpl extends UnparallelWorkflow<Integer>
    {

        public UnparallelWorkflowImpl(int number)
        {
            super(number);
        }

        @Override
        protected Runnable create(Integer key)
        {
            return new Worker(key, this);
        }
    }
    public class Worker implements Runnable
    {
        private int number;
        private UnparallelWorkflowImpl wf;

        public Worker(int number, UnparallelWorkflowImpl wf)
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
}
