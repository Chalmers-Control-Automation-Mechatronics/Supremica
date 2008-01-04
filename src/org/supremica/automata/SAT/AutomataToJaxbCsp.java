/*
 * AutomataToJaxbCsp.java
 *
 * Created on den 17 oktober 2007, 13:17
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.SAT;


import org.supremica.automata.*;
import org.supremica.automata.SAT.generated.CSPbinding.*;
import java.math.BigInteger;

import java.util.*;
import java.util.HashMap.*;
import javax.xml.bind.*;
import java.io.*;

/**
 *
 * @author voronov
 */
public class AutomataToJaxbCsp
{
    private int totalSteps;
    Automata ats;
    Instance instance;

    Instance.Presentation presentation = new Instance.Presentation();
    Instance.Domains domains           = new Instance.Domains();
    Instance.Variables variables       = new Instance.Variables();
    Instance.Predicates predicates     = new Instance.Predicates();
    Instance.Constraints constraints   = new Instance.Constraints();

    /* to synchronize we need common list of events */
    List<String> globalEventsList      = new ArrayList<String>();
    
    
    
    /** Creates a new instance of AutomataToJaxbCsp */
    public AutomataToJaxbCsp(Automata iats, int itimestep)
    {        
        ats = new Automata(iats);
        totalSteps = itimestep;
        
        
        ObjectFactory f = new ObjectFactory();
        
        instance = f.createInstance ();
        
        presentation.setName("x");
        presentation.setFormat("XCSP 2.0");

        for(Automaton a : ats){
            a.setIndices();
        }

        
        generateGlobalEventsList();

    }
    public void generateXML(String fileName)
    {        

        addStatesDomains();
        addEventsDomain();
        addStatesVariables();
        addEventsVariables();
        
        addInitialPredicates();
        addTransitionPredicates();
        addMarkingPredicates();
        addGoalPredicate();
        
        addInitialConstraints();
        addTransitionsConstraints();
        addMarkingConstraints();
        addGoalConstraint();
        
        setSizes();
        setInstance();
        try
        {
            JAXBContext jContext=JAXBContext.newInstance(
                    "org.supremica.automata.SAT.generated.CSPbinding");
            Marshaller marshaller=jContext.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshaller.marshal(instance, new FileOutputStream(fileName));
        }
        catch(Exception e1)
        {
            System.out.println(""+e1);    
        }

    }
    
    
    void setSizes()
    {
        domains.setNbDomains(toCSPint(domains.getDomain().size()));
        variables.setNbVariables(toCSPint(variables.getVariable().size()));
        predicates.setNbPredicates(toCSPint(predicates.getPredicate().size()));
        constraints.setNbConstraints(toCSPint(constraints.getConstraint().size()));        
    }
    void setInstance()
    {
        instance.setPresentation (presentation);
        instance.setDomains      (domains);
        instance.setVariables    (variables);
        instance.setPredicates   (predicates);
        instance.setConstraints  (constraints);        
    }
    
    public static BigInteger toCSPint(long i)
    {
        return new BigInteger(""+i);
    }
    public static String variableName(String autName, long timestep){
        return "state_" + autName + "_step_" + timestep;
    }
    
    void addStatesDomains(){
        for(Automaton a : ats) {
            domains.getDomain().add(makeDomain("states"+a.getName(), a.nbrOfStates()));
        }
    }

    void addEventsDomain()
    {
        /* one extra event is Marking event */
        domains.getDomain().add(makeDomain("events", 1 + ats.getUnionAlphabet().size()));                
    }

    void addStatesVariables()
    {
        for(long timestep = 0; timestep < totalSteps+1; timestep++)
        {
            for(Automaton a : ats) 
            {
                variables.getVariable().add(makeVariable(
                        variableName(a.getName(), timestep), 
                        "states" + a.getName()
                        ));
            }
        }
    }
    void addEventsVariables()
    {
        for(long timestep = 0; timestep < totalSteps; timestep++)
        {
            variables.getVariable().add(makeVariable(
                    "event_step_" + timestep,
                    "events"
                    ));
        }
    }
    public String prettyPrint(String model)
    {
        String[] modelValues = model.split("\\s");
        String trace = "";
        int i = 0;
        for(Instance.Variables.Variable v : variables.getVariable()){
            trace += v.getName() + ": " + modelValues[i] + "\n";
            i++;
        }
        
        
        return trace;
    }
       
    void addTransitionPredicates()
    {        
        for(Automaton a : ats){
            Map<String,String> eventsMap = 
                    getEventPredicatesMap(a, globalEventsList);
            for(LabeledEvent e : a.getAlphabet()){
                predicates.getPredicate().add(makePredicate(
                        a.getName()+"_"+e.getLabel(),
                        "int E int X int X1",
                        eventsMap.get(e.getLabel()) 
                        ));
            }            
        }    
        for(Automaton a : ats){
            /* E in A.abc  OR  X=X1 */
            String stayExpr = "eq(X,X1)";
            for(LabeledEvent e : a.getAlphabet()){
                stayExpr = 
                        "or("+stayExpr+", "+
                          "eq(E,"+ globalEventsList.indexOf(e.getLabel()) +")"
                        +")";
            }
            /*
            boolean canStay = false;
            for(String ev : globalEventsList){
                if(!a.getAlphabet().contains(ev)){
                    stayExpr = 
                        "or("+stayExpr+", "+
                          "eq(E,"+ globalEventsList.indexOf(ev) +")"
                        +")";
                    canStay = true;
                }
            }
            if(!canStay)
                stayExpr = "eq(1,1)";
            */
            predicates.getPredicate().add(makePredicate(
                    a.getName()+"_stay",
                    "int E int X int X1",
                    stayExpr 
                    ));
        }
    }
    
    int markingEventID()
    {
        return globalEventsList.size();
    }
    void addMarkingPredicates()
    {
        for(Automaton a : ats){
            String expr = "eq(0,1)";
            for(State s : a){
                if(s.isAccepting()){
                    expr = "or("+expr+", "+
                            "and("+
                              "eq(X,"  + s.getIndex() + "),"+
                              "eq(X1," + s.getIndex() + ")))";
                }
            }
            predicates.getPredicate().add(makePredicate(
                    a.getName()+"_marking",
                    "int E int X int X1",
                    "or(not(eq(E,"+markingEventID()+")),"+expr+")"
                    ));
        }
    }
    void addGoalPredicate()
    {
        // or(or(eq(E_step1, marking),eq(E_step2, marking),eq(E_step3,marking)))
        String expr = "eq(event_step_0, " + markingEventID()+")";
        String params = "int event_step_0";
        for(int timestep = 1; timestep < totalSteps; timestep++){
            expr = "or("+expr+",eq(event_step_"+timestep+", "+markingEventID() +"))";            
            params = params + " int event_step_"+timestep;
        }
        predicates.getPredicate().add(makePredicate(
                "goal",
                params,
                expr
                ));        
    }
    
    void addInitialPredicates()
    {
        for(Automaton a : ats){
            predicates.getPredicate().add(makePredicate(
                    a.getName()+"_init",
                    "int X",
                    "eq(X,"+a.getInitialState().getIndex()+")"
                    ));
        }                    
    }
    
    void generateGlobalEventsList()
    {
        for (LabeledEvent e : ats.getUnionAlphabet()) {
            globalEventsList.add(e.getLabel());
        }                      
    }
    
    /** returns Map: event -> (string)predicate */
    public static Map<String,String> getEventPredicatesMap(
            Automaton a, 
            List<String> globalEventsIDs)
    {
        Alphabet abc = a.getAlphabet();
        Map<String, String> eventsMap = new HashMap<String, String>(); 
        
        for (LabeledEvent e : abc) { 
            String  evLbl = e.getLabel(); 
            int     evID  = globalEventsIDs.indexOf(evLbl);
            eventsMap.put(evLbl, "not(eq(E, " + evID + "))");
        }
        
        for (Iterator arcIter = a.arcIterator(); arcIter.hasNext(); )
        {
            Arc arc = (Arc) arcIter.next();

            int from     = arc.getFromState().getIndex();
            int to       = arc.getToState().getIndex();
            String event = arc.getEvent().getLabel();
            String exp   = eventsMap.get(event);
            eventsMap.put(event, 
                    "or("+
                      exp +", "+
                      "and("+
                        "eq(X, "  + from +"),"+
                        "eq(X1, " + to   +"))"+ 
                    ")");
        }                             
        return eventsMap;
    }        
    
    void addTransitionsConstraints()
    {
        for(Automaton a : ats){
            for(LabeledEvent e : a.getAlphabet()){
                for (int timestep = 0; timestep < totalSteps; timestep++){
                    constraints.getConstraint().add(makeConstraint(
                            "c_"+a.getName()+"_"+e.getLabel()+timestep
                            , a.getName()+"_"+e.getLabel()
                            , "event_step_" + timestep + " " +
                              variableName(a.getName(), timestep) + " " + 
                              variableName(a.getName(), timestep+1)
                            , 3
                            ));
                }
            }
        }
        for(Automaton a : ats){
            for (int timestep = 0; timestep < totalSteps; timestep++){
                constraints.getConstraint().add(makeConstraint(
                        "c_"+a.getName()+"_stay"+timestep
                        , a.getName()+"_stay"
                        , "event_step_" + timestep + " " +
                          variableName(a.getName(), timestep) + " " + 
                          variableName(a.getName(), timestep+1)
                        , 3
                        ));
            }
        }       
    }
    
    void addInitialConstraints()
    {
        for(Automaton a : ats){
            constraints.getConstraint().add(makeConstraint(
                    "c_"+a.getName()+"_init",
                    a.getName() + "_init",
                    "state_"+a.getName() + "_step_0",
                    1));            
        }
    }
    void addMarkingConstraints()
    {
        for(Automaton a : ats){
            for(int timestep = 0; timestep < totalSteps; timestep++)
                constraints.getConstraint().add(makeConstraint(
                        "c_" + a.getName() + "_marking_step_" + timestep
                        , a.getName()+"_marking"
                        , "event_step_" + timestep + " " +
                           variableName(a.getName(), timestep) + " " + 
                           variableName(a.getName(), timestep+1)
                       , 3
                        ));            
        }
    }
    void addGoalConstraint()
    {
        String params = "";
        for(int timestep = 0; timestep < totalSteps; timestep++){
            params = params + " event_step_"+timestep;
        }
        constraints.getConstraint().add(makeConstraint(
                "c_goal",
                "goal",
                params,
                totalSteps
                ));
        
    }
    

    
    /* * * xml binding adapter * * */
    public static Instance.Domains.Domain makeDomain(String name, long length)
    {
        Instance.Domains.Domain d = new Instance.Domains.Domain();
        d.setName(name);
        d.setNbValues(toCSPint(length));
        d.setValue("0.."+(length-1));
        return d;
    }
    public static Instance.Variables.Variable makeVariable(String name, String domain)
    {
        Instance.Variables.Variable v = new Instance.Variables.Variable();
        v.setName(name);
        v.setDomain(domain);
        return v;
    }
    
    public static Instance.Predicates.Predicate makePredicate(
            String name
            , String params
            , String expr)
    {
        Instance.Predicates.Predicate p = new Instance.Predicates.Predicate();
        p.setName(name);
        p.getParameters().add(params);

        ExpressionType et = new ExpressionType();        
        et.setFunctional(expr);
        
        p.setExpression(et);
        
        return p;
    }
    
    public static Instance.Constraints.Constraint makeConstraint(
            String name
            , String ref
            , String params
            , int arity)
    {
        Instance.Constraints.Constraint c 
                = new Instance.Constraints.Constraint();
        
        c.setName(name);
        c.setReference(ref);
        c.setScope(params);
        c.setArity(toCSPint(arity));
        /* our effective params are the same as scope (no constants etc.) */
        EffectiveParametersType ept 
                = new EffectiveParametersType();
        ept.getContent().add(params); 
        c.setParameters(ept);
        return c;
    }    

}
