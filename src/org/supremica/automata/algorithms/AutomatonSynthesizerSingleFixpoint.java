package org.supremica.automata.algorithms;

import java.util.*;
import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.State;
import org.supremica.automata.LabeledEvent;

/**
 * This adds single fixpoint computation to the NBC algorithm AutomatonSynthesizer
 */
public class AutomatonSynthesizerSingleFixpoint
    extends AutomatonSynthesizer
{
    public AutomatonSynthesizerSingleFixpoint(Automaton theAutomaton, SynthesizerOptions synthesizerOptions)
    throws Exception
    {
        super(theAutomaton, synthesizerOptions);
    }
    
    // bit fields in the State::sethelper variable
    private static final int SET_EMPTY = 0, SET_QX = 1, SET_B1 = 2,
        SET_A1 = 4, SET_A2 = 8, SET_B2 = 16, SET_MARK = 32
        ;
    
    // ---------------------------------------------------
    private void remove(int mask)
    {
        int inv_bit = ~mask;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            currState.sethelper &= inv_bit;
        }
    }
    
    private void add(int mask)
    {
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            currState.sethelper |= mask;
        }
    }
    
    private void set(int value)
    {
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            currState.sethelper = value;
        }
    }
    
    // ---------------------------------------------------
    // if any elements are in mask, set to 'set' else remove 'remove'
    private long union(int mask, int set, int remove)
    {
        int inv_remove = ~remove;
        long count = 0;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if ((currState.sethelper & mask) == 0)
            {
                currState.sethelper &= inv_remove;
            }
            else
            {
                
                // currState.sethelper = set;
                currState.sethelper = (currState.sethelper & inv_remove) | set;
                
                count++;
            }
        }
        
        return count;
    }
    
    // if all elements are in mask, set to 'set' else remove 'remove'
    private long intersect(int mask, int set, int remove)
    {
        long count = 0;
        int inv_remove = ~remove;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if ((currState.sethelper & mask) == mask)
            {
                
                // currState.sethelper |= set;
                currState.sethelper = (currState.sethelper & inv_remove) | set;
                
                count++;
            }
            else
            {
                currState.sethelper &= inv_remove;
            }
        }
        
        return count;
    }
    
    private void invert(int mask)
    {
        int inv_mask = ~mask;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if ((currState.sethelper & mask) == mask)
            {
                currState.sethelper &= inv_mask;
            }
            else
            {
                currState.sethelper |= mask;
            }
        }
    }
    
    private long cardinality(int mask)
    {
        long count = 0;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if ((currState.sethelper & mask) == mask)
            {
                count++;
            }
        }
        
        return count;
    }
    
    // ---------------------------------------------------
    private long forbiddIfNot(int mask)
    {
        long count = 0;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if ((currState.sethelper & mask) == 0)
            {
                currState.setForbidden(true);
                
                count++;
            }
        }
        
        return count;
    }
    
    private long forbiddIf(int mask)
    {
        long count = 0;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if ((currState.sethelper & mask) == mask)
            {
                currState.setForbidden(true);
                
                count++;
            }
        }
        
        return count;
    }
    
    // -----------------------------------------------------------------------------
    // B(X) = { q | E s \in S_uncontrollable  . \delta(q, s) \in X }
    // B1 : doB(1, 2)
    private void doB(int mask, int set)
    {
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if ((currState.sethelper & mask) != 0)
            {
                Iterator arcIt = currState.incomingArcsIterator();
                
                while (arcIt.hasNext())
                {
                    Arc currArc = (Arc) arcIt.next();
                    LabeledEvent currEvent = currArc.getEvent();    // theAutomaton.getEvent(currArc.getEventId());
                    
                    if (!currEvent.isControllable())
                    {
                        State fromState = currArc.getFromState();
                        
                        fromState.sethelper |= set;
                    }
                }
            }
        }
    }
    
    // Same as doB(), but this one uses a least fixpoint instead
    // B_inverse(X) = { q | \lnot A s \in S_uncontrollable . \delta(q,s)! -> \delta(q,s) \in X   }
    private void doB_inverse(int mask, int set)
    {
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if (stayInsideUncontrollable(currState, mask))
            {
                currState.sethelper |= set;
            }
        }
    }
    
    // A(X) = { q | A s \in S . \delta(q,s)! -> \delta(q,s) \in X }
    // A1: doA(3,4)
    private void doA(int mask, int set)
    {
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if (stayInside(currState, mask))
            {
                currState.sethelper |= set;    // A( Q_x \cup B(Q_x));
            }
        }
    }
    
    /** see doA() */
    private boolean stayInside(State s, int mask)
    {
        Iterator arcIt = s.outgoingArcsIterator();
        
        while (arcIt.hasNext())
        {
            Arc currArc = (Arc) arcIt.next();
            State s2 = currArc.getToState();
            
            if ((s2.sethelper & mask) == 0)
            {
                return false;
            }
        }
        
        return true;
    }
    
    /** see doB_inverse() same as stayInside, but only for uncontrollable events */
    private boolean stayInsideUncontrollable(State s, int mask)
    {
        Iterator arcIt = s.outgoingArcsIterator();
        
        while (arcIt.hasNext())
        {
            Arc currArc = (Arc) arcIt.next();
            LabeledEvent currEvent = currArc.getEvent();
            
            if (!currEvent.isControllable())
            {
                State s2 = currArc.getToState();
                
                if ((s2.sethelper & mask) == 0)
                {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    /** single fixpoint Synthesize a controllable and nonblocking supervisor */
    protected boolean synthesizeControllableNonblocking_()
    throws Exception
    {
        
        // start clean:
        long old_count, count = 0, org_count;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if (currState.isAccepting() &&!currState.isForbidden())
            {
                currState.sethelper = SET_MARK;
                
                count++;
            }
            else
            {
                currState.sethelper = SET_EMPTY;
            }
        }
        
        org_count = count;
        
        do
        {
            old_count = count;
            
            long cc, old_cc;
            
            // cc = cardinality(SET_MARK);
            cc = count;
            
            do
            {
                old_cc = cc;
                
                doB_inverse(SET_MARK, SET_B1);
                
                cc = intersect(SET_MARK | SET_B1, SET_MARK, ~0);
            }
            while (cc != old_cc);
            
            long nbc, old_nbc;
            
            invert(SET_MARK);
            
            nbc = cardinality(SET_MARK);
            
            do
            {
                old_nbc = nbc;
                
                doA(SET_MARK, SET_A1);
                
                nbc = intersect(SET_MARK | SET_A1, SET_MARK, ~0);
            }
            while (nbc == old_nbc);
            
            invert(SET_MARK);
            
            count = cardinality(SET_MARK);
        }
        while (count == old_count);
        
        org_count -= count;
        
        forbiddIfNot(SET_MARK);
        
        return (org_count != 0);
        
/*
                                do {
                                                old_count = count;
 
                                                logger.info("| Q_X | = " + count);
 
                                                // B1 = B(Q_X)
                                                doB(SET_QX, SET_B1);
                                                // A1 = A( Q_X \cup B(Q_X) )
                                                doA(SET_QX | SET_B1, SET_A1);
                                                // A1 = Q_X \cap A( Q_X \cup B(Q_X) )
                                                intersect(SET_QX | SET_A1, SET_A1, SET_A1);
 
 
                                                // A2= A(Q_X)
                                                doA(SET_QX, SET_A2);
                                                // A2 = Q_x \cap A(Q_x)
                                                intersect(SET_QX | SET_A2, SET_A2, SET_A2);
                                                // B2 = B( Q_x \cap A(Q_x) )
                                                doB(SET_A2, SET_B2);
 
                                                // new Q_X = A2 \cup B2 (remove everything else)
                                                count = union(SET_A1 | SET_B2, SET_QX, ~0);
 
 
                                } while(old_count != count);
 
 
 
                                // ok, now forbid all states in Q_X:
                                stateIt = theAutomaton.stateIterator();
                                while (stateIt.hasNext())
                                {
                                                State currState = (State) stateIt.next();
                                                if( (currState.sethelper  & SET_QX) != 0)
                                                {
                                                                currState.setForbidden(true);
                                                }
                                }
 
                                theAutomaton.setType(AutomatonType.SUPERVISOR);
                                return true; // TODO: should use didSomething
 */
    }
    
    /* --------------------------------------------------------------------------------------------- */
    /*                  start of the inefficient "EDUCATION PURPOSE" algorithms                      */
    /* --------------------------------------------------------------------------------------------- */
    
    // THIS IS FOR EDUCATION PURPOSE  ONLY (no double fix point)
    // Synthesize a controllable supervisor
    protected boolean synthesizeControllable()
    throws Exception
    {
        
        // 1. setup
        long old_count, count = 0, org_count = 0;;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if (!currState.isForbidden())
            {
                currState.sethelper = SET_MARK;
                
                count++;
            }
            else
            {
                currState.sethelper = SET_EMPTY;
                
                org_count++;
            }
        }
        
        // 2. compute
        do
        {
            old_count = count;
            
            doB_inverse(SET_MARK, SET_B1);
            
            count = intersect(SET_MARK | SET_B1, SET_MARK, SET_MARK | SET_B1);
        }
        while (old_count != count);
        
        // 3. writeback
        org_count -= forbiddIfNot(SET_MARK);
        
        theAutomaton.setType(AutomatonType.SUPERVISOR);
        
        return (org_count != 0);    /* did Something */
    }
    
    // another "EDUCATION PURPOSE" algorithm (no double fix point)
    // Synthesize a non-blockingsupervisor
    protected boolean synthesizeNonblocking()
    throws Exception
    {
        
        // 1. setup
        long old_count, count = 0, org_count = 0;;
        Iterator stateIt = theAutomaton.stateIterator();
        
        while (stateIt.hasNext())
        {
            State currState = (State) stateIt.next();
            
            if (!currState.isInitial() || currState.isForbidden())
            {
                currState.sethelper = SET_QX;
                
                count++;    // we count the number of bad states for the convergence check
            }
            else
            {
                currState.sethelper = SET_EMPTY;
            }
        }
        
        org_count = count;
        
        // 2. compute
        do
        {
            old_count = count;
            
            doA(SET_QX, SET_A1);
            
            count = intersect(SET_QX | SET_A1, SET_QX, SET_QX | SET_A1);
        }
        while (old_count != count);
        
        // 3. write back
        org_count -= forbiddIf(SET_QX);
        
        theAutomaton.setType(AutomatonType.SUPERVISOR);
        
        return (org_count != 0);    /* did Something */
    }
    
    // ok, according to above, this would be three-fixpoing version of NBC
    protected boolean synthesizeControllableNonblocking_DONT_USE_ME()
    throws Exception
    {
        boolean didSomthing = synthesizeNonblocking();
        
        didSomthing |= synthesizeControllable();
        
        while (didSomthing)
        {
            if (!synthesizeNonblocking())
            {
                break;
            }
            
            if (!synthesizeControllable())
            {
                break;
            }
        }
        
        return didSomthing;
    }
}
