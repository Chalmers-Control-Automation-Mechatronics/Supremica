/*
 * AnalyzerGuardAction.java
 *
 * Created on May 7, 2008, 3:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.*;
import org.supremica.automata.algorithms.Guard.*;
import org.supremica.gui.ide.IDE;
import org.supremica.gui.GuardDialog;
import org.supremica.automata.*;
import org.supremica.log.*;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.*;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.automata.BDD.BDDAutomata;
import org.supremica.automata.BDD.BDDManager;
import org.supremica.gui.ide.EditorPanel;
import net.sf.javabdd.*;
import net.sourceforge.waters.model.module.NodeProxy;


/**
 *
 * @author Sajed
 */
public class AnalyzerGuardAction
    extends IDEAction
{

    /** Creates a new instance of AnalyzerGuardAction */

    private static final long serialVersionUID = 1L;
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    HashMap<String,String> aut2type;
    HashMap<String,String> aut2initState;

    EditorPanel editorPanel;
    ExpressionParser parser;
    HashSet<String> addedVariables;

    public AnalyzerGuardAction(List<IDEAction> actionList)
    {
        super(actionList);

        setAnalyzerActiveRequired(true);
        setMinimumNumberOfSelectedComponents(2);

        putValue(Action.NAME, "Generate Guard...");
        putValue(Action.SHORT_DESCRIPTION, "Generate a guard expression for a given event");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/expression.gif")));
    }

    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }

    public void doAction()
    {
        // Retrieve the selected automata and make a sanity check
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
//        if (selectedAutomata.nbrOfAutomata() > 1)
//        {
//            System.err.println("Just one automton should be selected!");
//            return;
//        }

        // Get the current options
        GuardOptions guardOptions = new GuardOptions();

 //       GuardGenerator gg = new GuardGenerator(selectedAutomata.getAutomatonAt(0),guardOptions.getExpressionType());
        editorPanel = ide.getActiveDocumentContainer().getEditorPanel();

        Vector events = new Vector();
        events.add("Generate guards for ALL events");

/*       for(AbstractSubject as: editorPanel.getModuleSubject().getComponentListModifiable())
        {
            System.out.println("--------------------");
            for(EdgeSubject edge:((SimpleComponentSubject)as).getGraph().getEdgesModifiable())
            {
                if(edge.getSource() == edge.getTarget())
                    System.out.println("YESSSSSSSSSSS");
            }

        }
 */

        for(EventDeclSubject sigmaS:  editorPanel.getModuleSubject().getEventDeclListModifiable())
        {
            if(sigmaS.getKind() == EventKind.CONTROLLABLE)
            {
                events.add(sigmaS.getName());
            }
        }
        // Start a dialog to allow the user changing the options
        GuardDialog guardDialog = new GuardDialog(ide.getFrame(), guardOptions,events);

        guardDialog.show();
        if (!guardOptions.getDialogOK())
        {
            return;
        }

        LabeledEvent sigma = new LabeledEvent(guardOptions.getEvent());
        BDDGuardGenerator bddgg = null;

        //Compute safe states
        BDDAutomata automataBDD = new BDDAutomata(selectedAutomata);
        BDDManager manager = automataBDD.getBDDManager();

        long time1 = System.currentTimeMillis();

        BDD prelUnconStates = manager.prelimUncontrollableStates(automataBDD);
        BDD forbiddenStates = prelUnconStates.or(automataBDD.getForbiddenStates());
//        forbiddenStates.printDot();
//        System.out.println("number of coreachable states: "+automataBDD.numberOfCoreachableStates());
        BDD safeStatesBDD = manager.safeStateSynthesis(automataBDD, forbiddenStates).and(automataBDD.getReachableAndCoreachableStates());

        long synthesisTime = System.currentTimeMillis()-time1;
        System.out.println("Synthesis time: "+synthesisTime+" millisecs");


/*        BufferedWriter out = null;
        try
        {
            out = new BufferedWriter(new FileWriter("C:/Users/sajed/Desktop/STS/examples/ResultsWithMyAlg/"+editorPanel.getModuleSubject().getName()+".doc"));
            out.write("Synthesis time: "+synthesisTime+" ms");
            out.newLine();
            out.newLine();
            out.write("Event \t BDD-size \t #terms \t guardGenTime(ms)");
            out.newLine();
        }
        catch (IOException e) {}
*/
        HashMap<String,BDDGuardGenerator> event2guard = new HashMap<String,BDDGuardGenerator>();
        boolean singleEventSelected = false;

        long time2 = System.currentTimeMillis();
        if(sigma.getName().equals(""))
        {
            for(EventDeclSubject sigmaS:  editorPanel.getModuleSubject().getEventDeclListModifiable())
            {
                if(sigmaS.getKind() == EventKind.CONTROLLABLE)
                {
    //                System.out.println("Generating guard for event "+ sigmaS.getName()+"...");
                    long runTime = System.currentTimeMillis();
                    bddgg = new BDDGuardGenerator(automataBDD, sigmaS.getName(), safeStatesBDD, guardOptions.getExpressionType());

//                    System.out.println("The guard was generated in "+(System.currentTimeMillis()-runTime)+" millisecs");

                    System.out.println("Number of terms in the expression: "+bddgg.getNbrOfTerms());
//                    try
//                    {
//                        out.write(sigmaS.getName()+"\t"+bddgg.getBDDSize()+"\t"+bddgg.getNbrOfTerms()+"\t"+bddgg.getRunTime());
//                        out.newLine();
//                    }
//                    catch (IOException e) {}

                    event2guard.put(sigmaS.getName(), bddgg);
                }
            }
        }
        else
        {
            singleEventSelected = true;
            bddgg = new BDDGuardGenerator(automataBDD, sigma.getName(), safeStatesBDD, guardOptions.getExpressionType());
        }

        long totalGuardGenTime = System.currentTimeMillis()-time2;

/*        try
        {
            out.newLine();
            out.write("Total time of generating guards for all events: "+totalGuardGenTime+" ms");
            out.close();
        }
        catch (IOException e) {}
*/


        //Add the guard to the automata
/*        ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
        parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());

        SimpleComponentSubject tempSubj =null;
        HashSet<SimpleComponentSubject> subjects = new HashSet();


        HashMap<String,String> tempAut2initState = new HashMap<String,String>();

        for(Automaton aut:selectedAutomata)
        {
            tempAut2initState.put(aut.getName(), aut.getInitialState().getName());
        }
        aut2type = new HashMap<String,String>();
        aut2initState = new HashMap<String,String>();
        String autName = "";
        for(AbstractSubject as: editorPanel.getModuleSubject().getComponentListModifiable())
        {
            tempSubj = ((SimpleComponentSubject)(as)).clone();
            autName = tempSubj.getName();
            tempSubj.setIdentifier(new SimpleIdentifierSubject (tempSubj.getName()+"_SUP"));
            aut2type.put(tempSubj.getName(), createType(tempSubj.getGraph().getNodes()));
            aut2initState.put(tempSubj.getName(), tempAut2initState.get(autName));
            subjects.add(tempSubj);
        }

        boolean changed = false;
        String guard = "";
        BDDGuardGenerator currBDDGG = null;
        addedVariables = new HashSet<String>();

        for(SimpleComponentSubject simSubj:subjects)
        {
            changed = false;
            for(EdgeSubject ep:simSubj.getGraph().getEdgesModifiable())
            {
                SimpleExpressionSubject ses = null;
                //&& simSubj.getKind().name().equals("SPEC")

                String currEvent = ep.getLabelBlock().getEventList().iterator().next().toString();
                currBDDGG = singleEventSelected ? bddgg : event2guard.get(currEvent);
                if( ((singleEventSelected && currEvent.equals(sigma.getName()) && !currBDDGG.guardIsTrueOrFalse()) ||
                        (!singleEventSelected && currBDDGG != null && !currBDDGG.guardIsTrueOrFalse())))
                {
                    try
                    {
                        guard = currBDDGG.getGuard();
//                        guard = "Q_R2 == 0 & Q_R2 == 1 | Q_P2 == 1";
                        ses = (SimpleExpressionSubject)(parser.parse(guard,Operator.TYPE_BOOLEAN));
                        addVariablesToModel((BinaryExpressionSubject)ses, simSubj);
                    }
                    catch(ParseException pe)
                    {
                        System.out.println(pe);
                        break;
                    }
                    GuardActionBlockSubject gab = new GuardActionBlockSubject();
                    ep.setGuardActionBlock(gab);
                    ep.getGuardActionBlock().getGuardsModifiable().add(ses);
                    changed = true;
                }
             }

             if(changed)
                editorPanel.addComponent(simSubj);
        }*/

    }

    public int addVariablesToModel(BinaryExpressionSubject bes, SimpleComponentSubject simSubj)
    {
        try{
            if(bes.getOperator().getName().equals("==") || bes.getOperator().getName().equals("!="))
            {
                String currAutomaton = new StringTokenizer(bes.getLeft().toString(),"Q_").nextToken()+"_SUP";
                VariableComponentSubject vcs = new  VariableComponentSubject(
                new SimpleIdentifierSubject(bes.getLeft().toString())
                , parser.parse(aut2type.get(currAutomaton),Operator.TYPE_RANGE)
                ,true
                ,parser.parse(bes.getLeft().toString()+"=="+aut2initState.get(currAutomaton),Operator.TYPE_BOOLEAN));

                if(!addedVariables.contains(vcs.getName()))
                {
                    editorPanel.getModuleSubject().getComponentListModifiable().add(vcs);
                    addedVariables.add(vcs.getName());
                }

                return 0;
            }
        }
        catch(ParseException pe)
        {
            System.out.println(pe);
            return 1;
        }

        addVariablesToModel((BinaryExpressionSubject)(bes.getLeft()), simSubj);
        addVariablesToModel((BinaryExpressionSubject)(bes.getRight()), simSubj);

        return 1;

    }

    public String createType(Set<NodeProxy> states)
    {
        String output = "{";
        for(NodeProxy state:states)
        {
            output += (state.getName()+",");
        }

        output = output.substring(0, output.length()-1);

        output += "}";

        return output;
    }

}

