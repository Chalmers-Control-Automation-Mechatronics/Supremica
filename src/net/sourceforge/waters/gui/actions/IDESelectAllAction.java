//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: net.sourceforge.waters.gui.actions
//# CLASS:   IDESelectAllAction
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDBitVector;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.BDD.EFA.BDDExtendedAutomata;
import org.supremica.automata.BDD.EFA.BDDExtendedManager;
import org.supremica.gui.ide.EditorPanel;
import org.supremica.gui.ide.IDE;


/**
 * <P>The action associated with the 'select all' menu buttons.</P>
 *
 * <P>This selects all items the panel that currently owns the focus to the
 * system clipboard. To support this action, components including editable
 * items must implement the {@link SelectionOwner#canSelectMore()
 * canSelectMore()}, {@link SelectionOwner#getAllSelectableItems()
 * getAllSelectableItems()}, {@link SelectionOwner#canSelectMore()
 * canSelectMore()}, {@link SelectionOwner#replaceSelection(List<?
 * extends Proxy>) replaceSelection()} methods of the {@link SelectionOwner}
 * interface.</P>
 *
 * @author Robi Malik
 */

public class IDESelectAllAction
  extends IDEAction
{

    IDE ide;

  //#########################################################################
  //# Constructors
  IDESelectAllAction(final IDE ide)
  {
    super(ide);
    this.ide = ide;
    putValue(Action.NAME, "Select All");
    putValue(Action.SHORT_DESCRIPTION, "Select all items in the panel");
    putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
    putValue(Action.ACCELERATOR_KEY,
	     KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
    setEnabled(false);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  public void actionPerformed(final ActionEvent event)
  {
    final FocusTracker tracker = getFocusTracker();
    final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
    final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
    if (watersOwner != null) {
      final List<? extends Proxy> all = watersOwner.getAllSelectableItems();
      watersOwner.replaceSelection(all);
    } else if (swingOwner != null) {
      final int len = swingOwner.getText().length();
      swingOwner.setCaretPosition(0);
      swingOwner.moveCaretPosition(len);
    }

    //////////////////////////////////////////////////////////////TESSSSSSSSSSSSSSSSSSSSSSSSSSSSTTTTTTTTTT
    EditorPanel editorPanel = ide.getActiveDocumentContainer().getEditorPanel();

    ExtendedAutomata exAutomata = new ExtendedAutomata(editorPanel.getModuleSubject());
    BDDExtendedAutomata exAutomataBDD = new BDDExtendedAutomata(exAutomata);
    BDDExtendedManager manager = exAutomataBDD.getBDDManager();

    int[] xDom = {1,0};
    int[] yDom = {3,2};

    BDDBitVector x = manager.getFactory().buildVector(xDom);
    BDDBitVector y = manager.getFactory().buildVector(yDom);
    BDDBitVector c1 = manager.getFactory().constantVector(2, 3);
    BDDBitVector c2 = manager.getFactory().constantVector(2, 2);

    BDD result = manager.getOneBDD();
    for(int i=0;i<c1.size();i++)
    {
        result.andWith(x.getBit(i).biimp(c1.getBit(i)));
    }

    BDD result2 = manager.getOneBDD();
    for(int i=0;i<c2.size();i++)
    {
        result2.andWith(y.getBit(i).biimp(c2.getBit(i)));
    }

//    System.out.println(result.and(result2).exist(result.support()).equals(result2));


//    exAutomataBDD.getBDDExAutomaton("S1").getEdgeForwardBDD().printDot();
//    exAutomataBDD.getBDDExAutomaton("Inlet_Valve").getEdgeForwardBDD().printDot();
    System.out.println("number of reachable states: "+exAutomataBDD.numberOfReachableStates());
    System.out.println("number of coreachable states: "+exAutomataBDD.numberOfCoreachableStates());
    System.out.println("number of safe states: "+exAutomataBDD.numberOfReachableAndCoreachableStates());
//    x.equ(y.add(c1)).printDot();

/*
    BDDBitVector sum = bVec.add(c1);

    bVec = manager.getFactory().buildVector(arg);
    System.out.println("value: "+c1.mulfixed(4).val());
*/

/*    BDD prelUnconStates = manager.prelimUncontrollableStates(exAutomataBDD);
    BDD forbiddenStates = prelUnconStates.or(exAutomataBDD.getForbiddenStates());
//    BDD safeStatesBDD = manager.safeStateSynthesis(exAutomataBDD, forbiddenStates).and(exAutomataBDD.getReachableAndCoreachableStates());
    BDD safeStatesBDD = exAutomataBDD.getReachableAndCoreachableStates();
    exAutomataBDD.generateStates(safeStatesBDD);    
*/
/*
        SynchronizationOptions synchOps = new SynchronizationOptions();
        AutomataSynchronizer synch = new AutomataSynchronizer(editorPanel.getModuleSubject().getComponentListModifiable(),synchOps);

        ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject().getComponentListModifiable().add((SimpleComponentSubject)synch.getSynchronizedComponent());
        ide.getActiveDocumentContainer().getAnalyzerPanel().addAutomaton(synch.getAutomaton());
*/
    //////////////////////////////////////////////////////////////TESSSSSSSSSSSSSSSSSSSSSSSSSSSSTTTTTTTTTT
        
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.gui.observer.Observer
  public void update(final EditorChangedEvent event)
  {
    if (event.getKind() == EditorChangedEvent.Kind.SELECTION_CHANGED) {
      final FocusTracker tracker = getFocusTracker();
      final SelectionOwner watersOwner = tracker.getWatersSelectionOwner();
      final JTextComponent swingOwner = tracker.getSwingSelectionOwner();
      final boolean enabled;
      if (watersOwner != null) {
        enabled = watersOwner.canSelectMore();
      } else if (swingOwner != null) {
        if (swingOwner.getSelectionStart() > 0) {
          enabled = true;
        } else {
          final int len = swingOwner.getText().length();
          enabled = swingOwner.getSelectionEnd() < len;
        }
      } else {
        enabled = false;
      }
      setEnabled(enabled);
    }
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
