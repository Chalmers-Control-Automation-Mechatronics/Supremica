/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.SAT3.*;
import java.io.File;
import java.io.PrintWriter;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.IO.ProjectBuildFromXML;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;


/**
 *
 * @author voronov
 */
public class ProblemFMETest {
    public static void main(String[] args) throws Exception{
        System.err.println("ProblemFMETest");
        test1();
        //test2();
        //test3();
        test4();
        test5();
        test6();
    }
    public static void test1(){
        System.err.println("Test1");

        Automata ats = ats();
               
        ExprFactory ef        = new ExprFactoryArrayList();
        CoDecIntToBool coder  = new CoDecIntToBoolLinear();        
        VarsLayoutStepSeq vl  = new VarsLayoutStepSeq(ats, ef, coder);
        ExprAcceptorPlainPrint
                     acceptor = new ExprAcceptorPlainPrint(ef, new PrintWriter(System.out, true));
        
        ProblemBasics pb      = new ProblemBasics(ef, vl, acceptor);
        
        ProblemFME pf         = new ProblemFME(pb);
        pf.go(ats, 3, new LabeledEvent("e1"));
        
    }
    public static void test2(){

        System.err.println("Test2");
        
        Automata ats = ats();
               
        ExprFactory ef          = new ExprFactoryArrayList();
        CoDecIntToBool coder    = new CoDecIntToBoolLinear();        
        VarsLayout vl           = new VarsLayoutStepSeq(ats, ef, coder);
        CnfClauseAcceptor accCl = new CnfClauseAcceptorDimacsStream(new PrintWriter(System.err, true));
        ExprAcceptor accEx      = new ExprAcceptorToCnfStruct(vl.FirstFreeAfterNSteps(3), ef, accCl);
        
        ProblemBasics pb        = new ProblemBasics(ef, vl, accEx);
        
        ProblemFME pf           = new ProblemFME(pb);
        pf.go(ats, 3, new LabeledEvent("e1"));
        
    }
    public static void test3(){
        System.err.println("Test3: some cnf after variable-preserving transformation:");
        
        Automata ats = ats();
               
        ExprFactory ef          = new ExprFactoryArrayList();
        CoDecIntToBool coder    = new CoDecIntToBoolLinear();        
        VarsLayout vl           = new VarsLayoutStepSeq(ats, ef, coder);
        CnfClauseAcceptor accCl = new CnfClauseAcceptorDimacsStream(new PrintWriter(System.err, true));
        ExprAcceptor accEx      = new ExprAcceptorToCnfVar(ef, accCl);
        
        ProblemBasics pb        = new ProblemBasics(ef, vl, accEx);
        
        ProblemFME pf           = new ProblemFME(pb);
        pf.go(ats, 3, new LabeledEvent("e1"));
        
    }
    public static void test4() throws Exception{
        System.err.println("Test4: automaton from file test:");
        
        Automata ats = (new ProjectBuildFromXML()).build(new File("one.xml"));
        ats.setIndices();
               
        int n = 3;
        
        ExprFactory ef          = new ExprFactoryArrayList();
        CoDecIntToBool coder    = new CoDecIntToBoolLinear();        
        VarsLayout vl           = new VarsLayoutStepSeq(ats, ef, coder);
        //CnfClauseAcceptor accCl = new CnfClauseAcceptorDimacsStream(new PrintWriter(System.err, true));
        //CnfClauseAcceptorDimacsFile accCl = new CnfClauseAcceptorDimacsFile(new File("one.cnf"));
        //ExprAcceptor accEx      = new ExprAcceptorToCnfStruct(vl.FirstFreeAfterNSteps(n),ef, accCl);        
        ExprAcceptor accEx      = new ExprAcceptorPlainPrint(ef, new PrintWriter(System.err,true));
        ProblemBasics pb        = new ProblemBasics(ef, vl, accEx);        
        ProblemFME pf           = new ProblemFME(pb);
        pf.go(ats, n, ats.getUnionAlphabet().getEvent("m"));
        //accCl.flush();
    }    
    public static void test5() throws Exception{
        System.err.println("Test5: sat test:");
        
        Automata ats = (new ProjectBuildFromXML()).build(new File("one.xml"));
        ats.setIndices();
               
        int n = 3;
        ExprFactory ef          = new ExprFactoryArrayList();
        CoDecIntToBool coder    = new CoDecIntToBoolLinear();        
        VarsLayout vl           = new VarsLayoutStepSeq(ats, ef, coder);
        ISolver solver          = SolverFactory.newDefault();
        solver.newVar(2000);
        IVecInt v = new VecInt();
        v.push(1);
        solver.addClause(v);
        CnfClauseAcceptor accCl = new CnfClauseAcceptorSat4j(solver);
        ExprAcceptor accEx      = new ExprAcceptorToCnfStruct(vl.FirstFreeAfterNSteps(n),ef, accCl);
        ProblemBasics pb        = new ProblemBasics(ef, vl, accEx);        
        ProblemFME pf           = new ProblemFME(pb);
        pf.go(ats, n, ats.getUnionAlphabet().getEvent("m"));
        System.err.println(solver.isSatisfiable()?"SAT":"UNSAT");
    }    
    public static void test6() throws Exception{
        // check sat reachability
        System.err.println("test 6: sat test");
        
        Automata ats = (new ProjectBuildFromXML()).build(new File("two.xml"));
        ats.setIndices();
               
        int n = 3;
        ExprFactory ef          = new ExprFactoryArrayList();
        CoDecIntToBool coder    = new CoDecIntToBoolLinear();        
        VarsLayout vl           = new VarsLayoutStepSeq(ats, ef, coder);
        ISolver solver          = SolverFactory.newDefault();
        solver.newVar(2000);
        IVecInt v = new VecInt();
        v.push(1);
        solver.addClause(v);
        CnfClauseAcceptor accCl = new CnfClauseAcceptorSat4j(solver);
        ExprAcceptor accPrint   = new ExprAcceptorPlainPrint(ef, new PrintWriter(System.err, true));
        ExprAcceptor accCharge  = new ExprAcceptorToCnfStruct(vl.FirstFreeAfterNSteps(n),ef, accCl);
        ExprAcceptor accEx      = new ExprAcceptorSplit(accCharge, accPrint);
        ProblemBasics pb        = new ProblemBasics(ef, vl, accEx);        
        ProblemFME pf           = new ProblemFME(pb);
        pf.go(ats, n, ats.getUnionAlphabet().getEvent("m"));
        System.err.println(solver.isSatisfiable()?"SAT":"UNSAT");
    }    

    
    
    private static Automata ats(){
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
        return ats;
    }
}
