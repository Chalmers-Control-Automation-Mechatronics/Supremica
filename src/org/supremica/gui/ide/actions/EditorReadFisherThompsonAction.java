//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.Action;
import javax.swing.JFileChooser;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;

import org.supremica.automata.Arc;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.Project;
import org.supremica.automata.State;
import org.supremica.gui.ide.DocumentContainerManager;


public class EditorReadFisherThompsonAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;


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

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }


    @Override
    public void doAction()
    {
        final JFileChooser chooser = new JFileChooser();
        final int returnVal = chooser.showOpenDialog(ide.getFrame());

        if (returnVal == JFileChooser.APPROVE_OPTION)
        {
            final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
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
               final int numberOfProducts = Integer.parseInt(st.nextToken());
               final int numberOfMachines = Integer.parseInt(st.nextToken());

               final Project project = new Project("Fisher-Thompson problem");
               project.setComment("Fisher-Thompson problem");
               final Map<String , Map<String , GuardActionBlockSubject>> efa2Event2GuardAction =
                       new HashMap<String , Map<String , GuardActionBlockSubject>>();

               final int[] maxValuesOfClocks = new int[numberOfProducts];
               for(int i=0;i<numberOfProducts;i++) maxValuesOfClocks[i] = 0;

               final Map<String , GuardActionBlockSubject> event2GuardAction = new HashMap<String, GuardActionBlockSubject>();
               int nP=1;
               while((text = bis.readLine()) != null)
               {
                   st = new StringTokenizer(text, " ");
                   final String automatonName = "P"+nP;
                   final Automaton product = new Automaton(automatonName);
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
                       final int nM = Integer.parseInt(st.nextToken());
                       final State targetLocation = new State(""+nM);
                       product.addState(targetLocation);
                       final String eventName = "P"+nP+"UseM"+nM;
                       e = new LabeledEvent(eventName);
                       e.setControllable(true);
                       product.getAlphabet().add(e);
                       product.addArc(new Arc(sourceLocation, targetLocation, e));

                       //////////////// Optimal way
//                       product.addArc(new Arc(sourceLocation, sourceLocation, product.getAlphabet().getEvent("t")));

                       sourceLocation = targetLocation;
                       final List<SimpleExpressionProxy> guards = new ArrayList<SimpleExpressionProxy>();
                       final List<BinaryExpressionProxy> actions = new ArrayList<BinaryExpressionProxy>();
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
                       final GuardActionBlockSubject guardAction= new GuardActionBlockSubject(guards, actions, null);
                       event2GuardAction.put(eventName, guardAction);

                       processTime = Integer.parseInt(st.nextToken());
                       previousMachineID = nM;
                       if(processTime > maxValuesOfClocks[nP-1])
                           maxValuesOfClocks[nP-1] = processTime;
                   }
                   final List<Proxy> propList = new LinkedList<Proxy>();
                   propList.add(ModuleSubjectFactory.getInstance().createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME));
                   final State targetLocation = new State("f");
                   targetLocation.setAccepting(true);
                   product.addState(targetLocation);
                   final String eventName = "P"+nP+"Finish";
                   e = new LabeledEvent(eventName);
                   e.setControllable(true);
                   product.getAlphabet().add(e);
                   product.addArc(new Arc(sourceLocation, targetLocation, e));

                   ////////////// Optimal way
//                   product.addArc(new Arc(sourceLocation,sourceLocation, product.getAlphabet().getEvent("t")));
//                   product.addArc(new Arc(targetLocation, targetLocation, product.getAlphabet().getEvent("t")));

                   final List<SimpleExpressionProxy> guards = new ArrayList<SimpleExpressionProxy>();
                   final List<BinaryExpressionProxy> actions = new ArrayList<BinaryExpressionProxy>();
                   try
                   {
                       guards.add(parser.parse("c"+nP+">="+processTime, Operator.TYPE_BOOLEAN));

                       actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                               parser.parse("c"+nP,Operator.TYPE_ARITHMETIC), parser.parse("0",Operator.TYPE_ARITHMETIC)));
                       actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                               parser.parse("m"+previousMachineID,Operator.TYPE_ARITHMETIC), parser.parse("0",Operator.TYPE_ARITHMETIC)));
                   } catch(final ParseException pe){}
                   final GuardActionBlockSubject guardAction= new GuardActionBlockSubject(guards, actions, null);
                   event2GuardAction.put(eventName, guardAction);

                   efa2Event2GuardAction.put(automatonName, event2GuardAction);

                   project.addAutomaton(product);

                   final String efaName = "C"+(nP);
                   final Automaton efa = new Automaton(efaName);
                   final State location = new State("0");
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
                   final Automaton clock = new Automaton("Clock");
                   final State singlelocation = new State("0");
                   singlelocation.setInitial(true);
                   singlelocation.setAccepting(true);
                   clock.addState(singlelocation);
                   final LabeledEvent e = new LabeledEvent("t");
                   e.setControllable(true);
                   clock.getAlphabet().add(e);
                   clock.addArc(new Arc(singlelocation, singlelocation, e));

                   project.addAutomaton(clock);

               final DocumentContainerManager manager = ide.getIDE().getDocumentContainerManager();
               manager.newContainer(project);


               final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

                try
                {
                    final String varName = "time";
                    final SimpleExpressionSubject initialStatePredicate = (SimpleExpressionSubject)(parser.parse(varName+"==0",Operator.TYPE_BOOLEAN));
                    final SimpleExpressionSubject range = (SimpleExpressionSubject)(parser.parse("0..1",Operator.TYPE_RANGE));
                    final VariableComponentSubject bookingVar = factory.createVariableComponentProxy(factory.createSimpleIdentifierProxy(varName), range, initialStatePredicate);
                    module.getComponentListModifiable().add(bookingVar);
                } catch(final ParseException pe){}

               final Map<Integer,VariableComponentSubject> machine2Variable = new HashMap<Integer, VariableComponentSubject>();
               for(int nM= 0; nM< numberOfMachines; nM++)
               {
                    try
                    {
                        final String varName = "m"+nM;
                        final SimpleExpressionSubject initialStatePredicate = (SimpleExpressionSubject)(parser.parse(varName+"==0",Operator.TYPE_BOOLEAN));
                        final SimpleExpressionSubject range = (SimpleExpressionSubject)(parser.parse("0..1",Operator.TYPE_RANGE));
                        final VariableComponentSubject bookingVar = factory.createVariableComponentProxy(factory.createSimpleIdentifierProxy(varName), range, initialStatePredicate);
                        module.getComponentListModifiable().add(bookingVar);
                        machine2Variable.put(nM, bookingVar);
                    } catch(final ParseException pe){}
               }

               for(int i=0;i<numberOfProducts;i++)
               {
                   try
                   {
                       final String clockName = "c"+(i+1);
                       final SimpleExpressionSubject initialStatePredicate = (SimpleExpressionSubject)(parser.parse(clockName+"==0",Operator.TYPE_BOOLEAN));
                       final SimpleExpressionSubject range = (SimpleExpressionSubject)(parser.parse("0.."+maxValuesOfClocks[i],Operator.TYPE_RANGE));
                       final VariableComponentSubject clockVar = factory.createVariableComponentProxy(factory.createSimpleIdentifierProxy(clockName), range, initialStatePredicate);
                       module.getComponentListModifiable().add(clockVar);
                   } catch(final ParseException pe){}
               }

               for(final AbstractSubject as:module.getComponentListModifiable())
               {
                   if(as instanceof SimpleComponentSubject)
                   {
                        final SimpleComponentSubject efa = (SimpleComponentSubject)as;
                        final String efaName = efa.getName();
                        if((""+efaName.charAt(0)).equals("P"))
                        {
                            for(final EdgeSubject edge:efa.getGraph().getEdgesModifiable())
                            {
                                final String eventName = ((SimpleIdentifierSubject)edge.getLabelBlock().getEventIdentifierList().get(0)).getName();

                                edge.setGuardActionBlock(efa2Event2GuardAction.get(efaName).get(eventName));
                            }
                        }
                        else if((""+efaName.charAt(0)).equals("C"))
                        {
                            if(efaName.contains("lock"))
                            {
                                final List<SimpleExpressionProxy> guards = new ArrayList<SimpleExpressionProxy>();
                                final List<BinaryExpressionProxy> actions = new ArrayList<BinaryExpressionProxy>();
                                try
                                {
                                    guards.add(parser.parse("time <= 1", Operator.TYPE_BOOLEAN));
                                    actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                            parser.parse("time",Operator.TYPE_ARITHMETIC), parser.parse("time+1",Operator.TYPE_ARITHMETIC)));
                                } catch(final ParseException pe){}
                                final GuardActionBlockSubject guardAction= new GuardActionBlockSubject(guards, actions, null);
                                efa.getGraph().getEdgesModifiable().get(0).setGuardActionBlock( guardAction);

                            }
                            else
                            {
                                final StringTokenizer sto = new StringTokenizer(efaName,"C");
                                final int productID = Integer.parseInt(sto.nextToken());

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


                                final NodeProxy location = efa.getGraph().getNodes().iterator().next();
                                guards = new ArrayList<SimpleExpressionProxy>();
                                actions = new ArrayList<BinaryExpressionProxy>();
                                try
                                {
                                    guards.add(parser.parse("c"+productID+">="+maxValuesOfClocks[productID-1], Operator.TYPE_BOOLEAN));
                                    actions.add(new BinaryExpressionSubject(CompilerOperatorTable.getInstance().getAssignmentOperator(),
                                            parser.parse("c"+productID,Operator.TYPE_ARITHMETIC), parser.parse("c"+productID,Operator.TYPE_ARITHMETIC)));
                                } catch(final ParseException pe){}
                                guardAction= new GuardActionBlockSubject(guards, actions, null);
                                final EdgeSubject edge = new EdgeSubject(location, location, efa.getGraph().getEdgesModifiable().get(0).getLabelBlock().clone(), guardAction, null, null, null);
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
