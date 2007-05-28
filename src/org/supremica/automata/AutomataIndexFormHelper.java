
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.automata;

import org.supremica.log.*;
import java.util.StringTokenizer;

public final class AutomataIndexFormHelper
{
    private static Logger logger = LoggerFactory.createLogger(AutomataIndexFormHelper.class);
    
    public static final int STATE_EXTRA_DATA = 2;
    public static final int STATE_STATUS_FROM_END = 1;
    public static final int STATE_PREVSTATE_FROM_END = 2;
    public static final int STATE_NO_PREVSTATE = -1;
    
    /**
     * Allocate a new state + extra data fields
     */
    public static int[] createState(int nbrOfAutomata)
    {
        int[] newState = new int[nbrOfAutomata + STATE_EXTRA_DATA];
        
        newState[nbrOfAutomata + STATE_EXTRA_DATA - STATE_PREVSTATE_FROM_END] = STATE_NO_PREVSTATE;
        
        return newState;
    }
    
    /**
     * Create a copy of an existing state
     */
    public static int[] createCopyOfState(int[] state)
    {
        int[] newState = new int[state.length];
        
        System.arraycopy(state, 0, newState, 0, state.length);
        
        return newState;
    }
    
    /**
     * Set the previous state index of an existing state
     */
    public static void setPrevStateIndex(int[] state, int stateIndex)
    {
        state[state.length - STATE_PREVSTATE_FROM_END] = stateIndex;
    }
    
    /**
     * Get the previous state index
     */
    public static int getPrevStateIndex(int[] state)
    {
        return state[state.length - STATE_PREVSTATE_FROM_END];
    }
    
    /**
     * bit
     * 0: initial
     * 1: accepting
     * 2: forbidden
     * 3: first
     * 4: last
     * 5: fastClearStatus
     * 6: deadlocked
     **/
    public static int createStatus(State state)
    {
        int status = 0;
        
        if (state.isInitial())
        {
            status |= 1;
        }
        
        if (state.isAccepting())
        {
            status |= (1 << 1);
        }
        
        if (state.isForbidden())
        {
            status |= (1 << 2);
        }
        
        if (state.isFirst())
        {
            status |= (1 << 3);
        }
        
        if (state.isLast())
        {
            status |= (1 << 4);
        }
        
        return status;
    }
    
    public static boolean isInitial(int status)
    {
        return (status & 1) == 1;
    }
    
    public static boolean isAccepting(int status)
    {
        return ((status >> 1) & 1) == 1;
    }
    
    public static boolean isForbidden(int status)
    {
        return ((status >> 2) & 1) == 1;
    }
    
    public static boolean isFirst(int status)
    {
        return ((status >> 3) & 1) == 1;
    }
    
    public static boolean isLast(int status)
    {
        return ((status >> 4) & 1) == 1;
    }
    
    public static boolean isDeadlocked(int status)
    {
        return ((status >> 6) & 1) == 1;
    }
    
    public static boolean isInitial(int[] state)
    {
        return isInitial(state[state.length - STATE_STATUS_FROM_END]);
    }
    
    public static boolean isAccepting(int[] state)
    {
        return isAccepting(state[state.length - STATE_STATUS_FROM_END]);
    }
    
    public static boolean isForbidden(int[] state)
    {
        return isForbidden(state[state.length - STATE_STATUS_FROM_END]);
    }
    
    public static boolean isFirst(int[] state)
    {
        return isFirst(state[state.length - STATE_STATUS_FROM_END]);
    }
    
    public static boolean isLast(int[] state)
    {
        return isLast(state[state.length - STATE_STATUS_FROM_END]);
    }
    
    public static boolean isDeadlocked(int[] state)
    {
        return isDeadlocked(state[state.length - STATE_STATUS_FROM_END]);
    }
    
    /**
     * Build a state from a string.
     * The string must be of the form
     * [state1 state2 state3 state4 state5 status]
     */
    public static int[] buildStateFromString(String stringState)
    {
        String trimmedString = stringState.trim();
        int indexOfLeft = trimmedString.indexOf('[');
        
        if (indexOfLeft == -1)
        {
            return null;
        }
        
        int indexOfRight = trimmedString.indexOf(']');
        
        if (indexOfRight == -1)
        {
            return null;
        }
        
        if (!(indexOfLeft < indexOfRight))
        {
            return null;
        }
        
        StringTokenizer st = new StringTokenizer(trimmedString.substring(indexOfLeft + 1, indexOfRight));
        int nbrOfTokens = st.countTokens();
        
        if (nbrOfTokens < 1)
        {
            return null;
        }
        
        int[] newState = new int[nbrOfTokens];
        int i = 0;
        
        while (st.hasMoreTokens())
        {
            String currToken = st.nextToken();
            
            try
            {
                int tmpInt = Integer.parseInt(currToken);
                
                newState[i++] = tmpInt;
            }
            catch (NumberFormatException ex)
            {
                
                // logger.debug(ex.getStackTrace());
                return null;
            }
        }
        
        if (i == nbrOfTokens)
        {
            return newState;
        }
        else
        {
            return null;
        }
    }
    
    public static String dumpState(int[] state)
    {
        if (state == null)
        {
            return "[null]";
        }
        
        StringBuffer sb = new StringBuffer("[");
        
        for (int i = 0; i < state.length; i++)
        {
            sb.append(state[i]);
            
            if (i != (state.length - 1))
            {
                sb.append(" ");
            }
        }
        
        sb.append("]");
        
        return sb.toString();
    }
    
    /**
     * Automaton index: 1 name:"aut1", State: index:2 name:"q2"
     */
    public static String dumpVerboseState(int[] state, AutomataIndexForm theForm)
    {
        StringBuffer sb = new StringBuffer("[\n");
        //State[][] stateTable = theForm.getStateTable();
        
        for (int i = 0; i < state.length - STATE_EXTRA_DATA; i++)
        {
            Automaton currAutomaton = theForm.getAutomaton(i);
            State currState = theForm.getState(i, state[i]);
            
            sb.append("Automaton index:" + i + " name: \"" + currAutomaton.getName() + "\", State: index:" + state[i] + " name:\"" + currState.getName() + "\"\n");
        }
        
        sb.append("Previous state index: " + state[state.length - STATE_PREVSTATE_FROM_END] + "\n");
        sb.append("Status: " + state[state.length - STATE_STATUS_FROM_END] + "\n");
        sb.append("]");
        
        return sb.toString();
    }
    
    public static void main(String[] args)
    {
        String orgString = "[10]";
        int[] state = buildStateFromString(orgString);
        
        if (state == null)
        {
            System.err.println("state == null");
        }
        
        logger.info("org: " + orgString);
        logger.info("parsed: " + dumpState(state));
    }
}
