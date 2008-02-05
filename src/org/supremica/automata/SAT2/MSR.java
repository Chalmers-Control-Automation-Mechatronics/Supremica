/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.SAT2;

import org.supremica.automata.*;
import org.supremica.automata.IO.ProjectBuildFromXML;

/**
 *
 * @author voronov
 */
public class MSR {
    
    public int trueVariable;
    private Automata ats;
    private Alphabet abc;
    final static String MARKING_NAME = "marking";
    final static String FORBIDDEN_NAME = "forbidden";
    
    public MSR(Automata inputAutomata){
        ats = inputAutomata;
        abc = ats.setIndices();
    }
    
    private Convert.Expr g(State x, int i, Alphabet curAbc){
        if(i<1)
            return Convert.Lit(trueVariable);
        
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);

        for(LabeledEvent e: x.activeEvents(false)){
            res.add(Convert.Impl(
                    TranEqEvent(i, e),
                    g(x.nextState(e), i-1,curAbc)));
        }
        
        for(LabeledEvent e: curAbc){
            if(!x.doesDefine(e)){
                res.add(Convert.Not(TranEqEvent(i, e)));
            }
        }
        
        Convert.MOp notInAbc = new Convert.MOp(Convert.ExType.MAND);        
        for(LabeledEvent e: curAbc)
            notInAbc.add(Convert.Not(TranEqEvent(i, e)));        
        res.add(Convert.Impl(notInAbc, g(x, i-1,curAbc)));
        
        return res;
    }    
    private Convert.Expr TranEqEvent(int step, LabeledEvent e){
        
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);

        int bits = 1 + maxBit(abc.size());
        
        for(int bit = 0; bit < bits; bit++){
            int sign = (e.getIndex() >> bit) & 1;
            sign = (sign>0) ? 1 : -1; // 1/0  ->  1/-1
            int var = 1 + step*bits + bit; // var "0" should never be used!
            res.add(Convert.Lit(sign*var));
        }
        return res;
    }
    /**
     * Number of highest bit of the number (add one to get required bits)
     * @param v
     * @return
     */
    private int maxBit(int v){
        int maxOne = Integer.highestOneBit(v);
        for(int i = 0; i < 32; i++)
            if((maxOne >> i) == 1)
                return i;
        return 0;
    }
    
    public Convert.Expr full(int steps){
        trueVariable = 1 + (steps+1) * (1 + maxBit(abc.size()));
        
        Convert.MOp res = new Convert.MOp(Convert.ExType.MAND);        
        for(Automaton a: ats)
            res.add(g(a.getInitialState(), steps, a.getAlphabet()));
        
        Convert.MOp goal = new Convert.MOp(Convert.ExType.MOR);
        for(int step=0; step<steps; step++)
            goal.add(TranEqEvent(step, abc.getEvent(MARKING_NAME)));
        res.add(goal);
        
        return res;
    }
    
    public static void modifyMSR(Automata ats){
        LabeledEvent marking = new LabeledEvent(MARKING_NAME);
               
        for(Automaton a: ats){
            a.getAlphabet().addEvent(marking);
            for(State s: a)
                if(s.isAccepting())
                    a.addArc(new Arc(s, s, marking));
        }
    }

    public static void modifyCV(Automata ats){
        State forb = new State(FORBIDDEN_NAME);
        forb.setAccepting(true); // mark forbiden state
        for(Automaton a: ats){            
            for(State s: a)
                s.setAccepting(false); // remove all other "markings"
            a.addState(forb);                            
        }
        
        for(LabeledEvent e: ats.getUnionAlphabet())
            if(!e.isControllable())
                for(Automaton a: ats)
                    if(a.isSupervisor()||a.isSpecification())
                        for(State s: a)
                            if(!s.doesDefine(e))
                                a.addArc(new Arc(s, forb, e));
                                                
        modifyMSR(ats); // CV reduced to MSR
    }

    
    public static void main(String [] args) throws Exception{       
        int          steps = Integer.parseInt(args[0]);
        Project      ats   = (new ProjectBuildFromXML()).build(System.in);
        modifyCV(ats);
        MSR          msr   = new MSR(ats);
        Convert.Expr e     = msr.full(steps);
        Convert      conv  = new Convert(msr.trueVariable,msr.trueVariable);
        Convert.Clauses cs = conv.convert(e);
        String out         = Convert.toDimacsCnfString(cs, conv.varCounter);
        
        System.out.println(out);       
    }
}
