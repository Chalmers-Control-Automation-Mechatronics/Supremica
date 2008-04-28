/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 *
 * @author voronov
 */
public interface VarsLayout {

    public Expr StateEqState(Automaton a, int step, State s);
    public Expr TransitionEqLabel(int step, LabeledEvent e);
    public Expr KeepState(Automaton a, int currentStep);
    public Expr EnsureOnlyOneValueThisStep(int step);
}
