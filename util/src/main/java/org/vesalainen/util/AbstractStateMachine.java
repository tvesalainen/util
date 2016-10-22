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

/**
 *
 * @author tkv
 */
public class AbstractStateMachine
{
    private String start;
    private Map<String,StateImpl> states = new HashMap<>();
    private List<PreTransition> preTransitions = new ArrayList<>();
    private StateImpl current;
    private Clock clock;
    private long startTime;
    private long stateStartTime;

    public AbstractStateMachine(String start)
    {
        this(start, Clock.systemDefaultZone());
    }

    public AbstractStateMachine(String start, Clock clock)
    {
        this.start = start;
        this.clock = clock;
    }
    public void addState(String name, State state)
    {
        states.put(name, new StateImpl(name, state));
    }
    public void addTransition(String from, BooleanSupplier condition, String to)
    {
        preTransitions.add(new PreTransition(from, condition, to));
    }
    public void evaluate() throws Exception
    {
        if (current == null)
        {
            compile();
        }
        StateImpl candidate = null;
        for (Entry<BooleanSupplier,State> e : current.transitions.entrySet())
        {
            if (e.getKey().getAsBoolean())
            {
                if (candidate != null)
                {
                    throw new IllegalArgumentException("transition from state "+current+" to two states at the same time: "+candidate.toString()+" and "+e.getValue());
                }
                candidate = (StateImpl) e.getValue();
            }
        }
        if (candidate != null)
        {
            current.exit();
            current = candidate;
            stateStartTime = clock.millis();
            current.enter();
        }
        current.run();
    }

    public long getStartTime()
    {
        return startTime;
    }
    public long getElapsedTime()
    {
        return clock.millis() - startTime;
    }
    public long getStateStartTime()
    {
        return stateStartTime;
    }
    public long getStateElapsedTime()
    {
        return clock.millis() - stateStartTime;
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
            StateImpl from = states.get(pt.from);
            if (from == null)
            {
                throw new IllegalArgumentException("state "+from+" missing");
            }
            StateImpl to = states.get(pt.to);
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
            throw new IllegalArgumentException("states "+inSet+" have no transion into");
        }
        start = null;
        preTransitions = null;
        states = null;
        startTime = stateStartTime = clock.millis();
        current.enter();
        current.run();
    }
    public interface State
    {
        void enter() throws Exception;
        void run() throws Exception;
        void exit() throws Exception;
    }
    public static class StateImpl implements State
    {
        private String name;
        private State inner;
        private Map<BooleanSupplier,State> transitions = new HashMap<>();

        public StateImpl(String name, State inner)
        {
            this.name = name;
            this.inner = inner;
        }

        public String name()
        {
            return name;
        }

        @Override
        public String toString()
        {
            return "State{" + "name=" + name + ", "+ inner+'}';
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
