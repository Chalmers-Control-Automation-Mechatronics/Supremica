/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.SAT3.*;
import java.io.PrintStream;
import java.io.PrintWriter;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;

/**
 *
 * @author voronov
 */
public class VarsLayoutStepSeqTest {

    public static void main(String[] args){
        ExprFactory ef = new ExprFactoryArrayList();
        
        LabeledEvent e1 = new LabeledEvent("e1");
        LabeledEvent e2 = new LabeledEvent("e2");
        LabeledEvent e3 = new LabeledEvent("e3");

        Automaton a = new Automaton("one");
        a.getAlphabet().addEvent(e1);
        a.getAlphabet().addEvent(e2);
        a.getAlphabet().addEvent(e3);
        State s1 = new State("s1");
        State s2 = new State("s2");
        a.addState(s1);
        a.addState(s2);
        Arc arc = new Arc(s1,s2, e1);
        a.addArc(arc);

        Automaton b = new Automaton("two");
        b.getAlphabet().addEvent(e2);
        b.getAlphabet().addEvent(e3);
        State sb1 = new State("sb1");
        State sb2 = new State("sb2");
        State sb3 = new State("sb3");
        b.addState(sb1);
        b.addState(sb2);
        b.addState(sb3);

        Automata ats = new Automata();
        ats.addAutomaton(a);
        ats.addAutomaton(b);
        
        
        ats.setIndices();
               
        CoDecIntToBool coder = new CoDecIntToBoolLinear();
        
        VarsLayoutStepSeq vl = new VarsLayoutStepSeq(ats, ef, coder);
        
        // 2 -3 -4
        Expr e = vl.TransitionEqLabel(0, e1);
        print(e, ef);
        
        // -10 11 -12
        e = vl.TransitionEqLabel(1, e2);
        print(e, ef);
        
        // 5 -6
        e = vl.StateEqState(a, 0, s1);
        print(e, ef);

        // -13 14
        e = vl.StateEqState(a, 1, s2);
        print(e, ef);

        // -7 -8 9
        e = vl.StateEqState(b, 0, sb3);
        print(e, ef);
        
        
        // 
        e = vl.EnsureOneEventThisStep(0);
        e = vl.EnsureOneStateThisStep(0);
        print(e, ef);
        
        e = vl.KeepState(a, 0);
        print(e, ef);
        
    }
        
    private static void print(Expr e, ExprFactory ef){
        System.err.println(e.size());
        (new ExprAcceptorPlainPrint(ef, new PrintWriter(System.err, true))).accept(e);
        System.err.println("\ndone");
        System.err.flush();
        
    }

}
