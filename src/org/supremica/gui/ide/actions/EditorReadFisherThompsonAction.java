//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AnalyzerSynthesizerAction
//###########################################################################
//# $Id: AnalyzerSynthesizerAction.java 4750 2009-09-01 00:33:54Z robi $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.JFileChooser;
import net.sourceforge.waters.model.base.Proxy;

import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.automata.*;
import org.supremica.gui.ide.DocumentContainerManager;

import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

public class EditorReadFisherThompsonAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.createLogger(IDE.class);


    public EditorReadFisherThompsonAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(true);

        putValue(Action.NAME, "Fisher-Thompson problem...");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_Y));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, "Read Fisher-Thompson problem from a file in form of the transpose of Table 7 in Liljenvall's Lic. thesis.");
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/synthesize16.gif")));
    }

    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }


    public void doAction()
    {
        final JFileChooser chooser = new JFileChooser();
        final int returnVal = chooser.showOpenDialog(ide.getFrame());

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
            final ExpressionParser parser = new ExpressionParser(factory, CompilerOperatorTable.getInstance());

            final File file = chooser.getSelectedFile();
            FileReader fis = null;
            BufferedReader bis = null;
            try
            {
               fis = new FileReader(file);
               bis = new BufferedReader(fis);
               String text = bis.readLine();
               StringTokenizer st = new StringTokenizer(text, " ");
               // First line represents the number of products (jobs) and machines: X Y
               int numberOfProducts = Integer.parseInt(st.nextToken());
               int numberOfMachines = Integer.parseInt(st.nextToken());              

               final Project project = new Project("Fisher-Thompson problem");
               project.setComment("Fisher-Thompson problem");
               Map<String , Map<String , GuardActionBlockSubject>> efa2Event2GuardAction =
                       new HashMap<String , Map<String , GuardActionBlockSubject>>();

               int[] maxValuesOfClocks = new int[numberOfProducts];
               for(int i=0;i<numberOfProducts;i++) maxValuesOfClocks[i] = 0;

               Map<String , GuardActionBlockSubject> event2GuardAction = new HashMap<String, GuardActionBlockSubject>();
               int nP=1;
               while((text = bis.readLine()) != null)
               {
                   st = new StringTokenizer(text, " ");
                   String automatonName = "P"+nP;
                   Automaton product = new Automaton(automatonName);
                   product.setType(AutomatonType.PLANT);
                   State sourceLocation = new State("s");
                   sourceLocation.setInitial(true);
                   product.addState(sourceLocation);
                   int processTime = 0;
                   int previousMachineID = -1;

                   LabeledEvent e;
//                   LabeledEvent e = new LabeledEvent("t");
//                   e.setControllable(true);
//                   product.getAlphabet().add(e);

                   while(st.hasMoreTokens())
                   {
                       int nM = Integer.parseInt(st.nextToken());                        
                       State targetLocation = new State(""+nM);
                       product.addState(targetLocation);
                       String eventName = "P"+nP+"UseM"+nM;
                       e = new LabeledEvent(eventName);
                       e.setControllable(true);
                       product.getAlphabet().add(e);
                       product.addArc(new Arc(sourceLocation, targetLocation, e));

                       //////////////// Optimal way
//                       product.addArc(new Arc(sourceLocation, sourceLocation, product.getAlphabet().getEvent("t")));

                       sourceLocation = targetLocation;
                       List<SimpleExpressionProxy> guards = new ArrayList<SimpleExpressionProxy>();
                       List<BinaryExpressionProxy> actions = new ArrayList<BinaryExpressionProxy>();
                       try
                       {
                           guards.add(parser.parse("c"+nP+">="+processTime+" & "+"m"+nM+"==0", Operator.TYPE_BOOLEAN));
                           actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                   parser.parse("c"+nP,Operator.TYPE_ARITHMETIC), parser.parse("0",Operator.TYPE_ARITHMETIC)));
                           actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                   parser.parse("m"+nM,Operator.TYPE_ARITHMETIC), parser.parse("1",Operator.TYPE_ARITHMETIC)));
                           if(previousMachineID != -1)
                           {
                                actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                   parser.parse("m"+previousMachineID,Operator.TYPE_ARITHMETIC), parser.parse("0",Operator.TYPE_ARITHMETIC)));
                           }

                       } catch(final ParseException pe){}
                       GuardActionBlockSubject guardAction= new GuardActionBlockSubject(guards, actions, null);
                       event2GuardAction.put(eventName, guardAction);

                       processTime = Integer.parseInt(st.nextToken());
                       previousMachineID = nM;
                       if(processTime > maxValuesOfClocks[nP-1])
                           maxValuesOfClocks[nP-1] = processTime;
                   }
                   final List<Proxy> propList = new LinkedList<Proxy>();
                   propList.add(ModuleSubjectFactory.getInstance().createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME));
                   State targetLocation = new State("f");
                   targetLocation.setAccepting(true);
                   product.addState(targetLocation);
                   String eventName = "P"+nP+"Finish";
                   e = new LabeledEvent(eventName);
                   e.setControllable(true);
                   product.getAlphabet().add(e);
                   product.addArc(new Arc(sourceLocation, targetLocation, e));

                   ////////////// Optimal way
//                   product.addArc(new Arc(sourceLocation,sourceLocation, product.getAlphabet().getEvent("t")));
//                   product.addArc(new Arc(targetLocation, targetLocation, product.getAlphabet().getEvent("t")));

                   List<SimpleExpressionProxy> guards = new ArrayList<SimpleExpressionProxy>();
                   List<BinaryExpressionProxy> actions = new ArrayList<BinaryExpressionProxy>();
                   try
                   {
                       guards.add(parser.parse("c"+nP+">="+processTime, Operator.TYPE_BOOLEAN));

                       actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                               parser.parse("c"+nP,Operator.TYPE_ARITHMETIC), parser.parse("0",Operator.TYPE_ARITHMETIC)));
                       actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                               parser.parse("m"+previousMachineID,Operator.TYPE_ARITHMETIC), parser.parse("0",Operator.TYPE_ARITHMETIC)));
                   } catch(final ParseException pe){}
                   GuardActionBlockSubject guardAction= new GuardActionBlockSubject(guards, actions, null);
                   event2GuardAction.put(eventName, guardAction);

                   efa2Event2GuardAction.put(automatonName, event2GuardAction);

                   project.addAutomaton(product);

                   String efaName = "C"+(nP);
                   Automaton efa = new Automaton(efaName);
                   State location = new State("0");
                   location.setInitial(true);
                   location.setAccepting(true);
                   efa.addState(location);
                   e = new LabeledEvent("t");
                   e.setControllable(true);
                   efa.getAlphabet().add(e);
                   efa.addArc(new Arc(location, location, e));

                   project.addAutomaton(efa);

                   nP++;
               }

               //Add clock automata
                   Automaton clock = new Automaton("Clock");
                   State singlelocation = new State("0");
                   singlelocation.setInitial(true);
                   singlelocation.setAccepting(true);
                   clock.addState(singlelocation);
                   LabeledEvent e = new LabeledEvent("t");
                   e.setControllable(true);
                   clock.getAlphabet().add(e);
                   clock.addArc(new Arc(singlelocation, singlelocation, e));

                   project.addAutomaton(clock);

               final DocumentContainerManager manager = ide.getIDE().getDocumentContainerManager();
               manager.newContainer(project);


               final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

                try
                {
                    String varName = "time";
                    SimpleExpressionSubject initialStatePredicate = (SimpleExpressionSubject)(parser.parse(varName+"==0",Operator.TYPE_BOOLEAN));
                    SimpleExpressionSubject range = (SimpleExpressionSubject)(parser.parse("0..1",Operator.TYPE_RANGE));
                    VariableComponentSubject bookingVar = factory.createVariableComponentProxy(factory.createSimpleIdentifierProxy(varName), range, true, initialStatePredicate);
                    module.getComponentListModifiable().add(bookingVar);
                } catch(final ParseException pe){}

               Map<Integer,VariableComponentSubject> machine2Variable = new HashMap<Integer, VariableComponentSubject>();
               for(int nM= 0; nM< numberOfMachines; nM++)
               {
                    try
                    {
                        String varName = "m"+nM;
                        SimpleExpressionSubject initialStatePredicate = (SimpleExpressionSubject)(parser.parse(varName+"==0",Operator.TYPE_BOOLEAN));
                        SimpleExpressionSubject range = (SimpleExpressionSubject)(parser.parse("0..1",Operator.TYPE_RANGE));
                        VariableComponentSubject bookingVar = factory.createVariableComponentProxy(factory.createSimpleIdentifierProxy(varName), range, true, initialStatePredicate);
                        module.getComponentListModifiable().add(bookingVar);
                        machine2Variable.put(nM, bookingVar);
                    } catch(final ParseException pe){}
               }

               for(int i=0;i<numberOfProducts;i++)
               {
                   try
                   {
                       String clockName = "c"+(i+1);
                       SimpleExpressionSubject initialStatePredicate = (SimpleExpressionSubject)(parser.parse(clockName+"==0",Operator.TYPE_BOOLEAN));
                       SimpleExpressionSubject range = (SimpleExpressionSubject)(parser.parse("0.."+maxValuesOfClocks[i],Operator.TYPE_RANGE));
                       VariableComponentSubject clockVar = factory.createVariableComponentProxy(factory.createSimpleIdentifierProxy(clockName), range, true, initialStatePredicate);
                       module.getComponentListModifiable().add(clockVar);
                   } catch(final ParseException pe){}
               }

               for(AbstractSubject as:module.getComponentListModifiable())
               {
                   if(as instanceof SimpleComponentSubject)
                   {
                        SimpleComponentSubject efa = (SimpleComponentSubject)as;
                        String efaName = efa.getName();
                        if((""+efaName.charAt(0)).equals("P"))
                        {
                            for(EdgeSubject edge:efa.getGraph().getEdgesModifiable())
                            {
                                String eventName = ((SimpleIdentifierSubject)edge.getLabelBlock().getEventList().get(0)).getName();

                                edge.setGuardActionBlock(efa2Event2GuardAction.get(efaName).get(eventName));
                            }
                        }
                        else if((""+efaName.charAt(0)).equals("C"))
                        {
                            if(efaName.contains("lock"))
                            {
                                List<SimpleExpressionProxy> guards = new ArrayList<SimpleExpressionProxy>();
                                List<BinaryExpressionProxy> actions = new ArrayList<BinaryExpressionProxy>();
                                try
                                {
                                    guards.add(parser.parse("time <= 1", Operator.TYPE_BOOLEAN));
                                    actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                            parser.parse("time",Operator.TYPE_ARITHMETIC), parser.parse("time+1",Operator.TYPE_ARITHMETIC)));
                                } catch(final ParseException pe){}
                                GuardActionBlockSubject guardAction= new GuardActionBlockSubject(guards, actions, null);
                                efa.getGraph().getEdgesModifiable().get(0).setGuardActionBlock( guardAction);
                                
                            }
                            else
                            {
                                StringTokenizer sto = new StringTokenizer(efaName,"C");
                                int productID = Integer.parseInt(sto.nextToken());

                                List<SimpleExpressionProxy> guards = new ArrayList<SimpleExpressionProxy>();
                                List<BinaryExpressionProxy> actions = new ArrayList<BinaryExpressionProxy>();
                                try
                                {
                                    guards.add(parser.parse("c"+productID+"<"+maxValuesOfClocks[productID-1], Operator.TYPE_BOOLEAN));
                                    actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                            parser.parse("c"+productID,Operator.TYPE_ARITHMETIC), parser.parse("c"+productID+"+1",Operator.TYPE_ARITHMETIC)));
                                } catch(final ParseException pe){}
                                GuardActionBlockSubject guardAction= new GuardActionBlockSubject(guards, actions, null);
                                efa.getGraph().getEdgesModifiable().get(0).setGuardActionBlock( guardAction);


                                NodeProxy location = efa.getGraph().getNodes().iterator().next();
                                guards = new ArrayList<SimpleExpressionProxy>();
                                actions = new ArrayList<BinaryExpressionProxy>();
                                try
                                {
                                    guards.add(parser.parse("c"+productID+">="+maxValuesOfClocks[productID-1], Operator.TYPE_BOOLEAN));
                                    actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                            parser.parse("c"+productID,Operator.TYPE_ARITHMETIC), parser.parse("c"+productID,Operator.TYPE_ARITHMETIC)));
                                } catch(final ParseException pe){}
                                guardAction= new GuardActionBlockSubject(guards, actions, null);
                                EdgeSubject edge = new EdgeSubject(location, location, efa.getGraph().getEdgesModifiable().get(0).getLabelBlock().clone(), guardAction, null, null, null);
                                efa.getGraph().getEdgesModifiable().add(edge);
                            }
                        }
                   }
               }

               fis.close();
               bis.close();

            }
            catch (final FileNotFoundException e){}
            catch (final IOException e) { e.printStackTrace();}
        }


    }  
}
