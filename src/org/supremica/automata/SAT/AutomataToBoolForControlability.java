/*
 * AutomataToBool.java
 *
 * Created on den 22 oktober 2007, 18:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.SAT;

import  org.supremica.automata.SAT.expr.util.*;
import  org.supremica.automata.SAT.expr.*; 
import  org.supremica.automata.*;

import  org.sat4j.specs.ContradictionException;
import  org.sat4j.specs.ISolver;
import  org.sat4j.specs.IVecInt;
import  org.sat4j.core.VecInt;

import  java.io.PrintWriter;
import  java.util.*;

/**
 *
 * @author voronov
 */
public class AutomataToBoolForControlability implements IAutomataToBool
{
    /** total steps to analyze */
    int         totalSteps;
    
    /** input automata */
    Automata    ats;
    
    /** union alphabet of input automata */
    Alphabet    abc;
    
    /** full resulting formula for SAT-solving */
    mAnd        completeExpression = new mAnd();
    //Expr        completeExpression = null;
    
    /** variables for event of each step (No of vars equal to totalSteps */
    int[]         eventVariableIds;
    
    /** state variable for each timestep for each automaton*/    
    Map<String, int[]>  stateVariableIds;
            
    Environment env = new Environment();
    Environment envBool = new Environment();
    
    ConverterVarEqToBool vareq;
    
    /*
     * +add Init          
     * +add Transitions and stay 
     * +add goal
     * marking implemented as modification of automata, 
     * not as a step in conversion
     *
     */
    
    
    private void initExpr(){
        System.err.print("Generating expression...");
        //addGoal();        
        //addBlockingDetermination();
        addForbidden();
        addInit();
        addTransitions();
        System.err.println(" done");        
    }
    private Expr getBoolCnfFlatExpr(){
                        
        initExpr();
                
        System.err.print("Converting variables to boolean...");
        vareq = new ConverterVarEqToBool(env, envBool);        
        Expr nB = vareq.initConvert(completeExpression);
        System.err.println(" done");
                       
        System.err.print("Converting to CNF...");
        Expr nBC = ConverterBoolToCnfSatOld.convertAll(nB);
        System.err.println(" done");        
        
        System.err.print("Flattening expression tree...");
        Expr nBCF = ConverterCnfToMAnd.convert(nBC);
        System.err.println(" done");                        
        
        return nBCF;
    }
    
    public void printDimacsCnfStr(PrintWriter pwOut){                        
        initExpr();
                
        System.err.print("Converting variables to boolean...");
        vareq = new ConverterVarEqToBool(env, envBool);        
        Expr nB = vareq.initConvert(completeExpression);
        System.err.println(" done");
                       
        System.err.print("Converting to new expr...");
        org.supremica.automata.SAT2.Convert.Expr nBN = 
                ConverterOldExprToNewExpr.convert(nB);
        System.err.println(" done");

        System.err.print("Converting to cnf...");
        org.supremica.automata.SAT2.Convert conv = 
                new org.supremica.automata.SAT2.Convert(envBool.vars.size()+1,envBool.vars.size()+1);
        org.supremica.automata.SAT2.Convert.Clauses cs = conv.convert(nBN);
        System.err.println(" done");

        System.err.print("Producing DIMACS CNF...");
        pwOut.println(org.supremica.automata.SAT2.Convert.toDimacsCnfString(
                cs, conv.varCounter));
        pwOut.flush();
        System.err.println(" done");                                
    }
    public void printDimacsCnfStrOld(PrintWriter pwOut){
        Expr nBCF = getBoolCnfFlatExpr();
        System.err.print("Producing DIMACS CNF...");
        int numClauses = ((mAnd)nBCF).childs.size();
        pwOut.println("p cnf "+ envBool.vars.size() + " " + numClauses);
        PrinterDimacsCnf.print(nBCF, pwOut);
        pwOut.flush();
        System.err.println(" done");                                
    }
    private Expr getBoolFlatExpr()
    {
        initExpr();
                        
        System.err.print("Converting variables to boolean...");
        vareq = new ConverterVarEqToBool(env, envBool);        
        Expr nB = vareq.initConvert(completeExpression);
        System.err.println(" done");
                       
        System.err.print("Converting to non-negated...");
        Expr nBC = ConverterToNonNegatedOld.convert(nB);
        System.err.println(" done");        
        
        System.err.print("Flattening expression tree...");
        Expr nBCF = ConverterToFlattened.convert(nBC);
        System.err.println(" done");                        
        
        return nBCF;        
    }
    public void printDimacsSatStr(PrintWriter pwOut){
        Expr nBCF = getBoolFlatExpr();
        System.err.print("Producing DIMACS SAT...");
        pwOut.println("p sat "+ envBool.vars.size());
        pwOut.print(PrinterDimacsSat.Print2(nBCF));
        pwOut.flush();
        System.err.println(" done");                  
    }
    public void chargeSolver(ISolver solver)
    {
        Expr nBCF = getBoolCnfFlatExpr();
        System.err.print("Producing Solver...");
        solver.newVar(envBool.vars.size());
        for(Expr clause: (mAnd) nBCF)
        {
            try {
                IVecInt vi = new VecInt();
                switch(clause.type){
                    case LIT:
                        vi.push((((Literal) clause).isPositive ? 1 : -1) * 
                                ((Literal) clause).variable.id);
                        break;
                    case MOR:
                        for (Expr lit : (mOr) clause) {
                            vi.push((((Literal) lit).isPositive ? 1 : -1) * 
                                    ((Literal) lit).variable.id);
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Unexpected "+
                                clause.type.toString());
                }
                solver.addClause(vi);

            } catch (ContradictionException ex) {
                System.err.println("  contradiction found...");
            }
        }
        System.out.println(" done");                                
    }
    
    public void decode(int[] answer)
    {
        for(int i: answer){
            if(i!=0){
                Variable v = envBool.vars.get(Math.abs(i));
                envBool.assign(v, i>0?1:0);
            } else {
                System.err.println(
                        "warning: variable number 0 (zero) in CNF to decode");
            }
        }
        
        vareq.envIntFromBool();
        for(int i = 0; i < totalSteps; i++)
            System.err.println(
                    "event " + i + ": " + 
                    eventIdToLabel(env.getValueFor(getEventVariable(i)))
                    );         
    }
    public void decode(String answer)
    {
        for(String s: answer.split("\\s")){
            int i = Integer.parseInt(s);
            if(i!=0){
                Variable v = envBool.vars.get(Math.abs(i));
                envBool.assign(v, i>0?1:0);
            }
        }
        vareq.envIntFromBool();
        System.err.println("Variables: ");
        for(Variable v: env.vars)
            System.out.println(v.Name + " " + env.getValueFor(v));
        
        System.err.println("Events only: ");        
        for(int i = 0; i < totalSteps; i++)
            System.err.println(
                    "event " + i + ": " + 
                    eventIdToLabel(env.getValueFor(getEventVariable(i)))
                    );         
    }
                 
    private String eventIdToLabel(int id)
    {
        for(LabeledEvent e: abc)
            if(e.getIndex()==id)
                return e.getLabel();
        throw new IllegalArgumentException("index not found");
    }
    /** Creates a new instance of AutomataToBool */
    public AutomataToBoolForControlability(Automata iats, int steps)
    {
        totalSteps = steps;
        ats = iats;

        //addMarking();
        abc = ats.setIndices(); // set indices for states and get union alphabet        
        addTraps();
        abc = ats.setIndices(); // addTraps added some events...
        
        eventVariableIds = new int[totalSteps];
        Domain eventsDomain = new Domain(abc.size());
        
        
        for(int i = 0; i < totalSteps; i++)
        {
            eventVariableIds[i] = env.add("event_"+i, eventsDomain);
            
        }
        
        stateVariableIds = new HashMap<String, int[]>();
        for(Automaton a: ats){
            int[] l = new int[totalSteps+1];
            Domain d = new Domain(a.nbrOfStates());
            for(int i = 0; i < totalSteps+1; i++)
                l[i] = env.add("state_"+a.getName()+"_"+i, d);
                        
            stateVariableIds.put(a.getName(), l);
        }        
        //vareq = new ConverterVarEqToBool(env, envBool);        
    }

    public Variable getStateVariable(String automatonName, int timestep){
        int id = stateVariableIds.get(automatonName)[timestep];
        Variable v = env.vars.get(id);
        return v;
    }
    public Variable getEventVariable(int timestep){
        int id = eventVariableIds[timestep];
        Variable v = env.vars.get(id);
        return v;
    }
    
    /**
     * Destructively replaces marked states with "marking" event
     */
    void addMarking(){
        LabeledEvent m = new LabeledEvent("marking");        
        for(Automaton a: ats){
            a.getAlphabet().addEvent(m);
            for(State s: a)
                if(s.isAccepting())
                    a.addArc(new Arc(s,s,m));            
        }           
    }   
    
    void addTraps(){
        Alphabet abcU = new Alphabet();
        for(LabeledEvent e: abc)
            if(!e.isControllable()){
                //System.err.println("uncontrolable event:" + e.getLabel());
                abcU.add(e);                
            }
        
        for(Automaton a: ats){
            if(a.isSupervisor() || a.isSpecification()){
                State trap = new State("cTrap");
                trap.setForbidden(true);               
                a.addState(trap);
                for(LabeledEvent eu: abcU){
                    if(a.getAlphabet().contains(eu.getLabel())){
                        for(State s: a){
                            if(!s.doesDefine(eu)){
                                a.addArc(new Arc(s, trap, eu));
                            }
                        }                     
                    }
                }
                /* this event to allow staying in the trap state */
                LabeledEvent dummy = 
                        new LabeledEvent(a.getName()+"_dummyTrap");
                a.getAlphabet().addEvent(dummy);
                a.addArc(new Arc(trap, trap, dummy));
            }
        }                
    }
    void addClause(Expr n){
        if(n==null)
            throw new IllegalArgumentException("adding null clause!");
        //completeExpression = (completeExpression==null)? n : new And(completeExpression, n);
        completeExpression.add(n);
    }
    
    void addInit()
    {
        int initTimeStep = 0;
        for(Automaton a: ats)
            addClause(new VarEqInt(
                    getStateVariable(a.getName(), initTimeStep),
                    a.getInitialState().getIndex()
                    ));        
    }
    
    Literal replaceNode(Expr oldExpr)
    {
        //return oldExpr;
        
        int b = env.addBool();
        Literal newVar = new Literal(env.vars.get(b), true);
        addClause(new And(
                new Or(new Not(newVar),        oldExpr  ),
                new Or(        newVar, new Not(oldExpr) )
                ));
        //addClause(new Or(
        //        new And(newVar, oldExpr),
        //        new And(new Not(newVar), new Not(oldExpr))));
        return newVar;
         
    }
    
    void addTransitions()
    {
        for(Automaton a : ats){       
            Map<String, List<int[]>> statesForEvents = 
                    getArcsForEvents(a);
            for(int i = 0; i < totalSteps; i++){

                for(LabeledEvent e: a.getAlphabet())
                    addTransitionClause(i, e.getLabel(), a.getName(), 
                            statesForEvents.get(e.getLabel()));
                                
                
                /* stay condition: either x=x' or event belong to the alphabet */
                Variable v1 = getStateVariable(a.getName(), i);
                Variable v2 = getStateVariable(a.getName(), i+1);  
                //Expr stayCondition = replaceNode(new VarEqVar(v1, v2));
                
                mOr stayCondition = new mOr();
                stayCondition.add(replaceNode(new VarEqVar(v1, v2)));
                
                for(LabeledEvent e: a.getAlphabet()){
                    stayCondition.add(replaceNode(new VarEqInt(
                            getEventVariable(i), 
                            e.getIndex())));
                    //stayCondition = new Or(
                    //        stayCondition,
                    //        replaceNode(new VarEqInt(
                    //            getEventVariable(i), 
                    //            e.getIndex())));
                    //stayCondition = new Or(stayCondition, new VarEqInt(
                    //        getEventVariable(i), e.getIndex()));
                }
                addClause(stayCondition);
            }
        }
    }
    /**
     * for every event in Automaton return a map of event names to 
     * the list of {from, to}.
     * @param a
     * @return
     */
    Map<String, List<int[]>> getArcsForEvents(Automaton a)
    {
        Map<String, List<int[]>> statesForEvents = 
                new HashMap<String, List<int[]>>();
        
        for(LabeledEvent e: a.getAlphabet()){
            statesForEvents.put(e.getLabel(), new ArrayList<int[]>());
        }
        
        for (Iterator arcIter = a.arcIterator(); arcIter.hasNext(); )
        {
            Arc arc = (Arc) arcIter.next();

            int from     = arc.getFromState().getIndex();
            int to       = arc.getToState().getIndex();
            String event = arc.getEvent().getLabel();

            List<int[]> states = statesForEvents.get(event);
            states.add(new int[] {from, to});
            statesForEvents.put(event, states);
        }        
        return statesForEvents;
    }

    /** 
     * e -> x&x'  
     * !ei | xi = from1 & xi+1 =to1 | xi = from2 & xi+1 = to2
     * 
     * @param timestep        i in formula
     * @param eventName       e in formula
     * @param automatonName   x in formula (another automaton is "y" or "z")
     * @param allFromTo       list of {from, to}. [0] is from, [1] is to.
     */
    void addTransitionClause(
            int timestep
            , String eventName
            , String automatonName
            , List<int[]> allFromTo)
    {
        
        Expr n = new Not(replaceNode(new VarEqInt(
                getEventVariable(timestep),
                abc.getEvent(eventName).getIndex() )));
        
//        Expr n = new Not(new VarEqInt(
//                getEventVariable(timestep),
//                abc.getEvent(eventName).getIndex() ));

        
//        mOr n1 = new mOr();
        //n1.add(replaceNode(new Not(replaceNode(new VarEqInt(
        //        getEventVariable(timestep),
        //        abc.getEvent(eventName).getIndex() )))));
        

        for(int[] fromTo : allFromTo){        
//            n1.add(replaceNode (new And(
//                    new VarEqInt(
//                        getStateVariable(automatonName, timestep), 
//                        fromTo[0]),
//                    new VarEqInt(getStateVariable(automatonName, timestep+1), 
//                        fromTo[1])
//                    )));
            
            n = new Or(n, replaceNode (new And(
                    new VarEqInt(
                        getStateVariable(automatonName, timestep), 
                        fromTo[0]),
                    new VarEqInt(getStateVariable(automatonName, timestep+1), 
                        fromTo[1])
                    )));
//            n = new Or(n, new And(
//                    new VarEqInt(
//                        getStateVariable(automatonName, timestep), 
//                        fromTo[0]),
//                    new VarEqInt(getStateVariable(automatonName, timestep+1), 
//                        fromTo[1])
 //                   ));
        }
        //n = n1.childs.size()>0?new Or(n, n1):n;
        addClause(n);        
    }
            
    void addGoal(){
        //Expr n = /*replaceNode*/(new VarEqInt(
        //        getEventVariable(0), 
        //        abc.getEvent("marking").getIndex()));
        
        //Expr res = null;
        mOr res = new mOr();
        for(int i = 0; i < totalSteps; i++)
        {
            Expr n = replaceNode(new VarEqInt(
                    getEventVariable(i), 
                    abc.getEvent("marking").getIndex()));
            //res = (res==null)? n : new Or(res,n); 
            res.add(n);
        }
        addClause(res);        
    }
    void addForbidden(){
        Expr forbCond = null;
        for(int timeStep = 0; timeStep < totalSteps; timeStep++){
            for(Automaton a: ats){
                for(State s: a){
                    if(s.isForbidden()){
                        Expr e = replaceNode(new VarEqInt(
                                getStateVariable(a.getName(), timeStep), 
                                s.getIndex()));
                        forbCond = forbCond==null ? e : new Or(forbCond,e);
                    }
                }
            }            
        }
        if(forbCond==null)
            System.err.println("warning: no clauses for forbidden states were added");
        else
            addClause(forbCond);
    }
   
}
