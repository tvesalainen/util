/*
 * Copyright (C) 2016 tkv
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
package org.vesalainen.util;

import java.time.Clock;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import org.vesalainen.util.AbstractStateMachine.State;

/**
 *
 * @author tkv
 * @param <B>
 * @param <S>
 */
public class AbstractStateMachine<B extends BooleanSupplier,S extends State>
{
    private String start;
    private Map<String,AbstractState> states = new HashMap<>();
    private List<PreTransition> preTransitions = new ArrayList<>();
    private AbstractState current;
    private Supplier<Clock> clockSupplier;
    private long startTime;
    private long stateStartTime;
    /**
     * Creates new state machine.
     * @param start Name of start state.
     */
    public AbstractStateMachine(String start)
    {
        this(start, Clock::systemDefaultZone);
    }
    /**
     * Creates new state machine.
     * @param start Name of start state.
     * @param clockSupplier 
     */
    public AbstractStateMachine(String start, Supplier<Clock> clockSupplier)
    {
        this.start = start;
        this.clockSupplier = clockSupplier;
    }
    /**
     * Creates named state with functional interface.
     * @param name
     * @param enter Called when state is entered.
     */
    public void addState(String name, Runnable enter)
    {
        addState(name, (S) new AdhocState(name, enter, null, null));
    }
    /**
     * Creates named state with functional interface.
     * @param name
     * @param enter Called when state is entered.
     * @param exit Called when state is exited.
     */
    public void addState(String name, Runnable enter, Runnable exit)
    {
        addState(name, (S) new AdhocState(name, enter, null, exit));
    }
    /**
     * Creates named state with functional interface.
     * @param name
     * @param enter Called when state is entered.
     * @param run Called when state is entered, when evaluated but not exited and when exited.
     * @param exit Called when state is exited.
     */
    public void addState(String name, Runnable enter, Runnable run, Runnable exit)
    {
        addState(name, (S) new AdhocState(name, enter, run, exit));
    }
    /**
     * Creates named state
     * @param name
     * @param state 
     */
    public void addState(String name, S state)
    {
        AbstractState old = states.put(name, new StateWrapper(name, state));
        if (old != null)
        {
            throw new IllegalArgumentException("state "+name+" exists already");
        }
    }
    public void addTransition(String from, B condition, String to)
    {
        preTransitions.add(new PreTransition(from, condition, to));
    }
    public String getCurrentState() throws Exception
    {
        if (current == null)
        {
            compile();
        }
        return current.name;
    }
    protected Set<B> getCurrentConditions() throws Exception
    {
        if (current == null)
        {
            compile();
        }
        return (Set<B>) current.transitions.keySet();
    }
    /**
     * Evaluates current states conditions and transit to new state depending on
     * conditions.
     * <p>If transition to new state: calls exit for old state and enter and run
     * for new state.
     * <p>If no transition: calls run for current state.
     * @throws IllegalArgumentException if more than one condition is true.
     * @throws Exception From states enter, run or exit methods.
     */
    public void evaluate() throws Exception
    {
        if (current == null)
        {
            compile();
        }
        StateWrapper candidate = null;
        for (Entry<BooleanSupplier,State> e : current.transitions.entrySet())
        {
            if (e.getKey().getAsBoolean())
            {
                if (candidate != null)
                {
                    throw new IllegalArgumentException("transition from state "+current+" to two states at the same time: "+candidate.toString()+" and "+e.getValue());
                }
                candidate = (StateWrapper) e.getValue();
            }
        }
        if (candidate != null)
        {
            current.exit();
            current = candidate;
            stateStartTime = clockSupplier.get().millis();
            current.enter();
        }
        current.run();
    }
    /**
     * Returns state machines start time in milli seconds from epoch.
     * @return 
     */
    public long getStartTime()
    {
        return startTime;
    }
    /**
     * Returns the time since start of state machine in milli seconds.
     * @return 
     */
    public long getElapsedTime()
    {
        return clockSupplier.get().millis() - startTime;
    }
    /**
     * Returns current states start time in milli seconds from epoch.
     * @return 
     */
    public long getStateStartTime()
    {
        return stateStartTime;
    }
    /**
     * Returns current states elapsed time in milli seconds.
     * @return 
     */
    public long getStateElapsedTime()
    {
        return clockSupplier.get().millis() - stateStartTime;
    }
    
    private void compile() throws Exception
    {
        Set<String> inSet = new HashSet<>();
        inSet.addAll(states.keySet());
        current = states.get(start);
        if (current == null)
        {
            throw new IllegalArgumentException("start state "+start+" missing");
        }
        inSet.remove(start);
        for (PreTransition pt : preTransitions)
        {
            AbstractState from = states.get(pt.from);
            if (from == null)
            {
                throw new IllegalArgumentException("state "+from+" missing");
            }
            AbstractState to = states.get(pt.to);
            if (to == null)
            {
                throw new IllegalArgumentException("state "+to+" missing");
            }
            State old = from.transitions.put(pt.condition, to);
            if (old != null)
            {
                throw new IllegalArgumentException("transition "+pt.condition+" from state "+pt.from+" to state "+pt.to+" ambiguous");
            }
            inSet.remove(pt.to);
        }
        if (!inSet.isEmpty())
        {
            throw new IllegalArgumentException("states "+inSet+" have no transition into");
        }
        start = null;
        preTransitions = null;
        states = null;
        startTime = stateStartTime = clockSupplier.get().millis();
        current.enter();
        current.run();
    }
    public interface State
    {
        void enter() throws Exception;
        void run() throws Exception;
        void exit() throws Exception;
    }
    private static abstract class AbstractState implements State
    {
        private String name;
        protected Map<BooleanSupplier,State> transitions = new HashMap<>();

        public AbstractState(String name)
        {
            this.name = name;
        }
        
        public String name()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return "State{" + "name=" + name + '}';
        }

    }
    private static class AdhocState extends AbstractState
    {
        private Runnable enter;
        private Runnable run;
        private Runnable exit;

        public AdhocState(String name, Runnable enter, Runnable run, Runnable exit)
        {
            super(name);
            this.enter = enter;
            this.run = run;
            this.exit = exit;
        }

        @Override
        public void enter() throws Exception
        {
            if (enter != null)
            {
                enter.run();
            }
        }

        @Override
        public void run() throws Exception
        {
            if (run != null)
            {
                run.run();
            }
        }

        @Override
        public void exit() throws Exception
        {
            if (exit != null)
            {
                exit.run();
            }
        }
    }
    private static class StateWrapper extends AbstractState
    {
        private State inner;

        public StateWrapper(String name, State inner)
        {
            super(name);
            this.inner = inner;
        }

        @Override
        public void enter() throws Exception
        {
            inner.enter();
        }

        @Override
        public void run() throws Exception
        {
            inner.run();
        }

        @Override
        public void exit() throws Exception
        {
            inner.exit();
        }
        
    }
    private class PreTransition
    {

        public PreTransition(String from, BooleanSupplier condition, String to)
        {
            this.from = from;
            this.condition = condition;
            this.to = to;
        }
        
        String from;
        BooleanSupplier condition;
        String to;
    }
}
