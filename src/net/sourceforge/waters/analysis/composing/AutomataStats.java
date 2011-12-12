//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis.modular
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id: ProjectingControllabilityChecker.java 4468 2008-11-01 21:54:58Z robi $
//###########################################################################

package net.sourceforge.waters.analysis.composing;

import net.sourceforge.waters.model.des.AutomatonProxy;
import java.io.PrintStream;
import net.sourceforge.waters.model.des.EventProxy;
import gnu.trove.TObjectIntHashMap;
import net.sourceforge.waters.model.des.TransitionProxy;
import java.util.Set;
import java.io.IOException;
import java.io.FileOutputStream;
import java.util.Collection;

/**
 * The projecting controllability check algorithm.
 *
 * @author Simon Ware
 */

public class AutomataStats
{
  private final PrintStream mPrint;

  // #########################################################################
  // # Constructors
  public AutomataStats(String file) throws IOException
  {
    mPrint = new PrintStream(new FileOutputStream(file, true));
  }
  
  public void flush()
  {
    mPrint.flush();
  }
  
  public void compautomata(Set<EventProxy> hidden)
  {
    mPrint.println("Composition:Stats");
    mPrint.println("Hidden Events");
    for (EventProxy e : hidden) {
      mPrint.println(e.getName());
    }
  }
  
  public void resultautomata()
  {
    mPrint.println("result:automaton");
  }
  
  public void abstractedautomata()
  {
    mPrint.println("abstracted:automaton");
  }
  
  public void output(Collection<AutomatonProxy> auts)
  {
    for (AutomatonProxy aut : auts) {
      output(aut);
    }
  }
  
  public void output(AutomatonProxy aut)
  {
    mPrint.println("Automata:Stats");
    mPrint.println(aut.getName());
    mPrint.println("states: " + aut.getStates().size());
    TObjectIntHashMap<EventProxy> looped = new TObjectIntHashMap<EventProxy>();
    TObjectIntHashMap<EventProxy> nonlooped = new TObjectIntHashMap<EventProxy>();
    for (TransitionProxy t : aut.getTransitions()) {
      if (t.getTarget() == t.getSource()) {
        looped.put(t.getEvent(), looped.get(t.getEvent()) + 1);
      } else {
        nonlooped.put(t.getEvent(), nonlooped.get(t.getEvent()) + 1);
      }
    }
    for (EventProxy e : aut.getEvents()) {
      mPrint.println(e.getName() + " : " + looped.get(e) + " : " + nonlooped.get(e)
                     + " : " + (looped.get(e) + nonlooped.get(e)));
    }
    mPrint.flush();
  }
  
  public void close() throws IOException
  {
    mPrint.flush();
    mPrint.close();
  }
}
