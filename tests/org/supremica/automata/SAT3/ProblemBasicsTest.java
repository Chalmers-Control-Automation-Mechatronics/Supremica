/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.SAT3.*;
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
public class ProblemBasicsTest {

    public static void main(String[] args){
        System.err.println("ProblemBasicsTest");
        test1();
    }
    
    public static void test1(){
        System.err.println("Test 1");
        
        LabeledEvent e1 = new LabeledEvent("e1");
        LabeledEvent e2 = new LabeledEvent("e2");
        LabeledEvent e3 = new LabeledEvent("e3");

        Automaton a = new Automaton("one");
        a.getAlphabet().addEvent(e1);
        a.getAlphabet().addEvent(e2);
        a.getAlphabet().addEvent(e3);
        State s1 = new State("s1");
        State s2 = new State("s2");
        s1.setInitial(true);
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

        b.setInitialState(sb2);
        
        Automata ats = new Automata();
        ats.addAutomaton(a);
        ats.addAutomaton(b);
                
        ats.setIndices();
               
        ExprFactory ef        = new ExprFactoryArrayList();
        CoDecIntToBool coder           = new CoDecIntToBoolLinear();        
        VarsLayoutStepSeq vl  = new VarsLayoutStepSeq(ats, ef, coder);
        ExprAcceptorPlainPrint
                     acceptor = new ExprAcceptorPlainPrint(ef, new PrintWriter(System.out, true));
        
        ProblemBasics pb      = new ProblemBasics(ef, vl, acceptor);

        // t0=e1 -> q0=s1 & q1=s2
        pb.transition(1, a, e1);
        System.err.println();
        
        // t0=e1 -> TRUE // nothing
        System.err.println("should be nothing...");
        pb.transition(1, a, e2);
        System.err.println("...should have been nothing");
        
        System.err.println("Stay:");
        pb.stay(1, a, e3);
        
        System.err.println("fire marking:");        
        pb.fireMarkingEvent(e1, 3);

        System.err.println("one var:");        
        pb.ensureOneEventThisStep(1);
        pb.ensureOneStateThisStep(1);
        
        System.err.println("initial:");        
        pb.initialCondition(a);
    }
    
}
