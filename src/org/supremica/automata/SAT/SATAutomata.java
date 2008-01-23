/*
 * SATAutomata.java
 *
 * Created on den 19 september 2007, 16:50
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.SAT;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.reader.ParseFormatException;
import org.sat4j.reader.XMLCSPReader;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IProblem;
import org.sat4j.specs.TimeoutException;
import org.supremica.automata.*;
import org.supremica.automata.IO.ProjectBuildFromXML;
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
public class SATAutomata {

    /** Creates a new instance of SATAutomata */
    public SATAutomata(Automata theAutomata)
    {

    }
    
    public static void main(String[] args) 
            throws FileNotFoundException, 
                   IOException,
                   Exception
    {
        /*String atsFileName = "c:\\alex\\coding\\ats-sup\\ft06.xml";        
        int totalSteps = 71;

        //Automata ats = getAts();                
        Automata ats = readAts(atsFileName);
        solveCNF2(ats, totalSteps);       
        //solveCSP(ats, totalSteps);       
         * */
        
        // Default options:
        int totalSteps = 10;
        String problem = "MSR";
        String action = "makeCnf";
        String answerFile = "out.txt";
        
        // Options parsingIAutomataToBool atb;                
        for(int i = 0; i < args.length; i++){
            if(args[i].equalsIgnoreCase("--steps"))
                totalSteps = Integer.parseInt(args[++i]);
            else if(args[i].equalsIgnoreCase("--problem"))
                problem = args[++i];
            else if(args[i].equalsIgnoreCase("--action"))
                action = args[++i];
            else if(args[i].equalsIgnoreCase("--answerFile"))
                answerFile = args[++i];
            else 
                System.err.println("usage: --steps <> --problem {CV,DV,MSR} --action {makeCnf,makeSat}");
        }
        
        Project ats = null;
        ProjectBuildFromXML builder = new ProjectBuildFromXML();
        ats = builder.build(System.in);
        
        
        IAutomataToBool atb;
        
        if(problem.equalsIgnoreCase("MSR")){
            atb = new AutomataToBoolForReachability(ats, totalSteps);
        }
        else if (problem.equalsIgnoreCase("CV")) {
            atb = new AutomataToBoolForControlability(ats, totalSteps);
        }
        else if (problem.equalsIgnoreCase("DV")) {
            atb = new AutomataToBoolForDeadlock(ats, totalSteps);
        }
        else {
            throw new IllegalArgumentException(
                    "no task (MSR or CV or DV expected)");
        }

        
        PrintWriter cnfFile = new PrintWriter(System.out);
        
        if(action.equalsIgnoreCase("makeSat"))
            atb.printDimacsSatStr(cnfFile);
        else if(action.equalsIgnoreCase("makeCnf"))
            atb.printDimacsCnfStr(cnfFile);     
        else if(action.equalsIgnoreCase("decode")){
            BufferedReader br = new BufferedReader(
                    new FileReader(answerFile));
            String line = br.readLine();
            if(line.contains("SAT"))
                line = br.readLine();
            System.err.println("line to decode: " + line);
            atb.printDimacsCnfStr(cnfFile);
            atb.decode(line);            
        } else if(action.equalsIgnoreCase("solveSat4j")){
            org.sat4j.specs.ISolver solver = SolverFactory.newDefault();            
            atb.chargeSolver(solver);
            System.out.println("Start solving...");
            if(solver.isSatisfiable()) {
                System.out.println("Satisfiable !");
                atb.decode(solver.model());
            } else {
                System.out.println("Unsatisfiable !");
            }
            
        }

    }


    public static Automata readAts(String atsFileName)
    {
        Project ats = null;
        try {
            
            ProjectBuildFromXML builder = new ProjectBuildFromXML();

            ats = builder.build(new File(atsFileName));
        } catch (Exception ex) {            
            Logger.getLogger(SATAutomata.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ats;
    }
    
    public static Automata getAts()
    {
        Automaton a = new   Automaton("A");
        Automaton b = new Automaton("B");

        State a0 = new State("A0");
        State a1 = new State("A1");
        State a2 = new State("A2");
        State b0 = new State("B0");
        State b1 = new State("B1");
        a0.setInitial(true);
        //a2.setAccepting(true); // UNSAT
        a1.setAccepting(true); // SAT
        b0.setInitial(true);
        b1.setAccepting(true);
        a.addState(a0);
        a.addState(a1);
        a.addState(a2);
        b.addState(b0);
        b.addState(b1);
        LabeledEvent e1 = new LabeledEvent("e0");
        LabeledEvent e2 = new LabeledEvent("e1");
        a.getAlphabet().addEvent(e1);
        a.getAlphabet().addEvent(e2);
        b.getAlphabet().addEvent(e1);
        a.addArc (new Arc(a0, a1, e1));
        a.addArc (new Arc(a0, a2, e2));
        b.addArc (new Arc(b0, b1, e1));

        Automata ats = new Automata(a);
        ats.addAutomaton(b);
        return ats;
    }

    public static void solveCNF(Automata ats, int totalSteps)
    {
        
        try{
            
            String cnfName = "c:\\alex\\coding\\ats-sup-sat-cnf\\ats_"+ats.getName()+".cnf";
            
            AutomataToBoolForReachability atb = new AutomataToBoolForReachability(ats, totalSteps);        
            
            //BufferedWriter out = new BufferedWriter(new FileWriter(cnfName));
            //out.write(atb.getDimacsCnfStr());
            //out.close();
            PrintWriter cnfFile = 
                    new PrintWriter(
                    new BufferedWriter(
                    new FileWriter(cnfName)));        
            atb.printDimacsCnfStr(cnfFile);                    
            System.out.println("file "+cnfName +" written");
            
            
            org.sat4j.specs.ISolver solver = SolverFactory.newDefault();
            solver.setTimeout(3600); // 1 hour timeout
            org.sat4j.reader.Reader reader = new org.sat4j.reader.DimacsReader(solver);
            // CNF filename is given on the command line 

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            System.out.println("Parsing cnf file...");            
            IProblem problem = reader.parseInstance(cnfName);
            System.out.println(" done");
            System.out.println("Start solving...");
            if (problem.isSatisfiable()) {
                System.out.println("Satisfiable !");
                //System.out.println(reader.decode(problem.model()));
                reader.decode(problem.model(), pw);
                System.out.println(sw.toString());
                atb.decode(sw.toString());
            } else {
                System.out.println("Unsatisfiable !");
            }
              
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage() );
            e.printStackTrace();
        }        
        
    }
    public static void solveCNF2(Automata ats, int totalSteps)
    {
        
        try{                        
            AutomataToBoolForReachability atb = new AutomataToBoolForReachability(ats, totalSteps);        
            
                       
            org.sat4j.specs.ISolver solver = SolverFactory.newDefault();
            solver.setTimeout(3600); // 1 hour timeout

            atb.chargeSolver(solver);                    

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            System.out.println("Start solving...");
            if(solver.isSatisfiable()) {
                System.out.println("Satisfiable !");
                //System.out.println(reader.decode(problem.model()));
//                reader.decode(problem.model(), pw);
//                System.out.println(sw.toString());
//                atb.decode(sw.toString());
                atb.decode(solver.model());
            } else {
                System.out.println("Unsatisfiable !");
            }
              
        }catch (Exception e){
            System.err.println("Error: " + e.getMessage() );
            e.printStackTrace();
        }        
        
    }
    public static void solveCSP(Automata ats, int totalSteps)
    {

        String instFileName = "c:\\temp\\inst_"+ats.getName()+".xml";
        try
        {
            
        AutomataToJaxbCsp ajb = new AutomataToJaxbCsp(ats,totalSteps);
        ajb.generateXML(instFileName);

        org.sat4j.specs.ISolver solver = SolverFactory.newDefault();
        solver.setTimeout(3600); // 1 hour timeout
        org.sat4j.reader.Reader reader = new XMLCSPReader(solver);

            IProblem problem = reader.parseInstance(instFileName);
            boolean sat = false;
            if (problem.isSatisfiable()) { //while
               sat = true;
               System.out.println("Satisfiable !");
               StringWriter sw = new StringWriter();
               PrintWriter  pw = new PrintWriter(sw);

               reader.decode(problem.model(),pw);               
               System.out.println(ajb.prettyPrint((sw.toString())));
               //System.out.println(reader.decode(model));
               //reader.decode(model, new PrintWriter(System.out));
            }
            if (!sat){
                // do something for unsat case
                System.out.println("Unsatisfiable !");
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
        } catch (ParseFormatException e) {
            // TODO Auto-generated catch block
        } catch (IOException e) {
            // TODO Auto-generated catch block
        } catch (ContradictionException e) {
            System.out.println("Unsatisfiable (trivial)!");
        } catch (TimeoutException e) {
            System.out.println("Timeout, sorry!");
        } catch (Exception ex) {
            System.out.println(ex);
            ex.printStackTrace();
        }
        
    }
    public static void createXMLmanually()
    {
        ObjectFactory f = new ObjectFactory();

        org.supremica.automata.SAT.generated.CSPbinding.Instance inst = f.createInstance ();

        int TotalSteps = 5;

        Instance.Presentation presentation = new Instance.Presentation();
        Instance.Domains domains           = new Instance.Domains();
        Instance.Variables variables       = new Instance.Variables();
        Instance.Predicates predicates     = new Instance.Predicates();
        Instance.Constraints constraints   = new Instance.Constraints();

        /* * * Presentation * * */
        presentation.setName("x");
        presentation.setFormat("XCSP 2.0");

        /* * * Domains * * */
        Instance.Domains.Domain domainOfStates = new Instance.Domains.Domain();
        Instance.Domains.Domain domainOfEvents = new Instance.Domains.Domain();

        domainOfStates.setName("statesA");
        domainOfStates.setNbValues(new BigInteger("2"));
        domainOfStates.setValue("0 1");

        domainOfEvents.setName("events");
        domainOfEvents.setNbValues(new BigInteger("1"));
        domainOfEvents.setValue("1");

        domains.getDomain().add(domainOfStates);
        domains.getDomain().add(domainOfEvents);

        domains.setNbDomains(new BigInteger("2"));

        /* * * Variables * * */
        Instance.Variables.Variable v;
        for(int timestep = 0; timestep < TotalSteps; timestep++){
            v = new Instance.Variables.Variable();
            v.setDomain("statesA");
            v.setName("stateAStep" + timestep);
            variables.getVariable().add(v);

            v = new Instance.Variables.Variable();
            v.setDomain("events");
            v.setName("eventStep" + timestep);
            variables.getVariable().add(v);
        }
        variables.setNbVariables(new BigInteger(""+TotalSteps*2));

        /* * * Predicates * * */
        Instance.Predicates.Predicate p;
        ExpressionType et;

        p = new Instance.Predicates.Predicate();
        et = new ExpressionType();
        et.setFunctional("or(not(eq(E,1)),and(eq(X,0),eq(X1,1)))");
        p.setExpression(et);
        p.getParameters().add("int E int X int X1");
        p.setName("pe1");
        predicates.getPredicate().add(p);

        p = new Instance.Predicates.Predicate();
        et = new ExpressionType();
        et.setFunctional("eq(X,0)");
        p.setExpression(et);
        p.getParameters().add("int X");
        p.setName("pinit");
        predicates.getPredicate().add(p);

        p = new Instance.Predicates.Predicate();
        et = new ExpressionType();
        et.setFunctional("eq(X,1)");
        p.setExpression(et);
        p.getParameters().add("int X");
        p.setName("pgoal");
        predicates.getPredicate().add(p);

        predicates.setNbPredicates(new BigInteger("3"));

        /* * * Constraints * * */
        Instance.Constraints.Constraint c;
        EffectiveParametersType ept;
        /* initial */
        c = new Instance.Constraints.Constraint();
        c.setName("cinit");
        c.setScope("stateAStep0");
        c.setArity(new BigInteger("1"));
        c.setReference("pinit");
        ept = new EffectiveParametersType();
        ept.getContent().add("stateAStep0");
        c.setParameters(ept);
        constraints.getConstraint().add(c);

        /* transitions */
        for(int timestep = 0; timestep < TotalSteps-1; timestep++){
            c = new Instance.Constraints.Constraint();
            c.setName("ce1_"+timestep);
            c.setScope("eventStep"+timestep+" stateAStep"+timestep+" stateAStep"+(timestep+1));
            c.setArity(new BigInteger("3"));
            c.setReference("pe1");
            ept = new EffectiveParametersType();
            ept.getContent().add("eventStep"+timestep+" stateAStep"+timestep+" stateAStep"+(timestep+1));
            c.setParameters(ept);
            constraints.getConstraint().add(c);
        }

        /* goal */
        c = new Instance.Constraints.Constraint();
        c.setName("cgoal");
        c.setScope("stateAStep1");
        c.setArity(new BigInteger("1"));
        c.setReference("pgoal");
        ept = new EffectiveParametersType();
        ept.getContent().add("stateAStep1");
        c.setParameters(ept);
        constraints.getConstraint().add(c);

        constraints.setNbConstraints(new BigInteger(""+(2+TotalSteps)));

        inst.setPresentation (presentation);
        inst.setDomains      (domains);
        inst.setVariables    (variables);
        inst.setPredicates   (predicates);
        inst.setConstraints  (constraints);

        try
        {
            JAXBContext jContext=JAXBContext.newInstance(
                    "org.supremica.automata.SAT.generated.CSPbinding");
            Marshaller marshaller=jContext.createMarshaller();

            marshaller.setProperty(
                    Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
            marshaller.marshal(inst, new FileOutputStream("inst.xml"));
        }
        catch(Exception e1)
        {
            System.out.println(""+e1);
        }

    }

}
