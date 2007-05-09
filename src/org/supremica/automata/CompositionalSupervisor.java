
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

import java.lang.String;
import java.util.Map;

import org.supremica.util.SupremicaException;
import org.supremica.automata.Automata;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.KripkeLabel;

/**
 * A compositional supervisor (see the "Supervision Equivalence"
 * Wodes-paper) contains a systemModel used to observe the current
 * global state of the system but makes the "isEnabled(sigma)"
 * decision based on a KripkeLabel-mapping for the state that is
 * reached by "alpha" from the current global state.
 */
public class CompositionalSupervisor
    extends ModularSupervisor
    implements Supervisor
{
    /**
     * Creates a compositional supervisor from a system model, a set
     * of maps mapping KripkeLabel:s and a description of the
     * composition scheme.
     *
     * @param model is an Automata containing a model of the
     * plant and the specification (may be plantified)
     * @param mapArray array of maps, the order in which to use them
     * is described by <code>compositionScheme</code>
     * @param compositionScheme describes the flow of the mapping,
     * from a state (array) to a YES/NO verdict on whether the state
     * is allowed by the supervisor. Used by the isEnabled-method.
     */
    public CompositionalSupervisor(Automata model, Map<KripkeLabel,KripkeLabel>[] mapArray, String compositionScheme)
    throws SupremicaException
    {
        super(model);
    }
    
    ////////////////////////////////////////
    // Supervisor interface methods       //
    // (inherited from ModularSupervisor  //
    // and overridden here)               //
    ////////////////////////////////////////
    
    // We need to override this one
    public boolean isEnabled(LabeledEvent event)
    {
        // Find out which state the system would be in if event was execuded.
        // Save the current global state
        State[] currentStateSave = (State[]) currentGlobalState.clone();
        // We would end up (hypotetically) in some other state...
        State[] hypotheticalState;
        try
        {
            // Try executing the event
            executeEvent(event);
            hypotheticalState = currentGlobalState;
            // Restore order
            currentGlobalState = currentStateSave;
        }
        catch (EventDisabledException ex)
        {
            // Restore order
            currentGlobalState = currentStateSave;
            // The event is disabled by the model (it was a stupid
            // question, it should not matter whether the supervisor
            // enables or disables the event)
            return false;
        }
        
        // ... examine if hypotheticalState maps to "OK" or "BAD".
        
        // INSERT CODE HERE
        
        return true;
    }

    /**
     * Returns a "flat" automata model representing the compositional supervisor.
     */
    public Automata getAsAutomata()
    {
        throw new UnsupportedOperationException("The algorithm for transforming the compositional supervisor to a 'flat' automata is not implemented.");
    }
}
