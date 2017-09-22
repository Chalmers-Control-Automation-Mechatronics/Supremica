
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

import java.util.StringTokenizer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public final class AutomataIndexFormHelper
{
    private static Logger logger = LogManager.getLogger(AutomataIndexFormHelper.class);

    public static final int STATE_EXTRA_DATA = 2;
    public static final int STATE_STATUS_FROM_END = 1;
    public static final int STATE_PREVSTATE_FROM_END = 2;
    public static final int STATE_NO_PREVSTATE = -1;

    /**
     * Allocate a new state + extra data fields
     */
    public static int[] createState(final int nbrOfAutomata)
    {
        final int[] newState = new int[nbrOfAutomata + STATE_EXTRA_DATA];

        newState[nbrOfAutomata + STATE_EXTRA_DATA - STATE_PREVSTATE_FROM_END] = STATE_NO_PREVSTATE;

        return newState;
    }

    /**
     * Create a copy of an existing state
     */
    public static int[] createCopyOfState(final int[] state)
    {
        final int[] newState = new int[state.length];

        System.arraycopy(state, 0, newState, 0, state.length);

        return newState;
    }

    /**
     * Set the previous state index of an existing state
     */
    public static void setPrevStateIndex(final int[] state, final int stateIndex)
    {
        state[state.length - STATE_PREVSTATE_FROM_END] = stateIndex;
    }

    /**
     * Get the previous state index
     */
    public static int getPrevStateIndex(final int[] state)
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
    public static int createStatus(final State state)
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

    public static boolean isInitial(final int status)
    {
        return (status & 1) == 1;
    }

    public static boolean isAccepting(final int status)
    {
        return ((status >> 1) & 1) == 1;
    }

    public static boolean isForbidden(final int status)
    {
        return ((status >> 2) & 1) == 1;
    }

    public static boolean isFirst(final int status)
    {
        return ((status >> 3) & 1) == 1;
    }

    public static boolean isLast(final int status)
    {
        return ((status >> 4) & 1) == 1;
    }

    public static boolean isDeadlocked(final int status)
    {
        return ((status >> 6) & 1) == 1;
    }

    public static boolean isInitial(final int[] state)
    {
        return isInitial(state[state.length - STATE_STATUS_FROM_END]);
    }

    public static boolean isAccepting(final int[] state)
    {
        return isAccepting(state[state.length - STATE_STATUS_FROM_END]);
    }

    public static boolean isForbidden(final int[] state)
    {
        return isForbidden(state[state.length - STATE_STATUS_FROM_END]);
    }

    public static boolean isFirst(final int[] state)
    {
        return isFirst(state[state.length - STATE_STATUS_FROM_END]);
    }

    public static boolean isLast(final int[] state)
    {
        return isLast(state[state.length - STATE_STATUS_FROM_END]);
    }

    public static boolean isDeadlocked(final int[] state)
    {
        return isDeadlocked(state[state.length - STATE_STATUS_FROM_END]);
    }

    /**
     * Build a state from a string.
     * The string must be of the form
     * [state1 state2 state3 state4 state5 status]
     */
    public static int[] buildStateFromString(final String stringState)
    {
        final String trimmedString = stringState.trim();
        final int indexOfLeft = trimmedString.indexOf('[');

        if (indexOfLeft == -1)
        {
            return null;
        }

        final int indexOfRight = trimmedString.indexOf(']');

        if (indexOfRight == -1)
        {
            return null;
        }

        if (!(indexOfLeft < indexOfRight))
        {
            return null;
        }

        final StringTokenizer st = new StringTokenizer(trimmedString.substring(indexOfLeft + 1, indexOfRight));
        final int nbrOfTokens = st.countTokens();

        if (nbrOfTokens < 1)
        {
            return null;
        }

        final int[] newState = new int[nbrOfTokens];
        int i = 0;

        while (st.hasMoreTokens())
        {
            final String currToken = st.nextToken();

            try
            {
                final int tmpInt = Integer.parseInt(currToken);

                newState[i++] = tmpInt;
            }
            catch (final NumberFormatException ex)
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

    public static String dumpState(final int[] state)
    {
        if (state == null)
        {
            return "[null]";
        }

        final StringBuilder sb = new StringBuilder("[");

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
    public static String dumpVerboseState(final int[] state, final AutomataIndexForm theForm)
    {
        final StringBuilder sb = new StringBuilder("[\n");
        //State[][] stateTable = theForm.getStateTable();

        for (int i = 0; i < state.length - STATE_EXTRA_DATA; i++)
        {
            final Automaton currAutomaton = theForm.getAutomaton(i);
            final State currState = theForm.getState(i, state[i]);

            sb.append("Automaton index:" + i + " name: \"" + currAutomaton.getName() + "\", State: index:" + state[i] + " name:\"" + currState.getName() + "\"\n");
        }

        sb.append("Previous state index: " + state[state.length - STATE_PREVSTATE_FROM_END] + "\n");
        sb.append("Status: " + state[state.length - STATE_STATUS_FROM_END] + "\n");
        sb.append("]");

        return sb.toString();
    }

    public static void main(final String[] args)
    {
        final String orgString = "[10]";
        final int[] state = buildStateFromString(orgString);

        if (state == null)
        {
            System.err.println("state == null");
        }

        logger.info("org: " + orgString);
        logger.info("parsed: " + dumpState(state));
    }
}
