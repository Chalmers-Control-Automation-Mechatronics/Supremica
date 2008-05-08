/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT3;

import org.supremica.automata.SAT3.*;
import java.io.File;
import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.supremica.automata.Automata;
import org.supremica.automata.IO.AutomataToXML;
import org.supremica.automata.IO.ProjectBuildFromXML;

/**
 *
 * @author voronov
 */
public class ModifyMSRToFMETest {

    public static void main(String[] args) throws Exception{
        test1();
    }
    
    public static void test1() throws Exception{
        // check sat reachability
        System.err.println("\n\n\n sat modify test:");
        
        Automata ats = (new ProjectBuildFromXML()).build(new File("two-b.xml"));
        ModifyMSRToFME.modify(ats);
        
        AutomataToXML xml = new AutomataToXML(ats);
        xml.serialize(new File("two-b-m.xml"));
               
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
        pf.go(ats, n, ModifyMSRToFME.marked);
        System.err.println(solver.isSatisfiable()?"SAT":"UNSAT");
    }    
    
}
