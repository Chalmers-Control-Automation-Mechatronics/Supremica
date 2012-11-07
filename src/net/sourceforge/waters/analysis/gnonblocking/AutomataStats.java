//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   ProjectingControllabilityChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

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
 * @author Simon Ware
 */

class AutomataStats
{
  private final PrintStream mPrint;

  // #########################################################################
  // # Constructors
  public AutomataStats(final String file) throws IOException
  {
    mPrint = new PrintStream(new FileOutputStream(file, true));
  }

  public void flush()
  {
    mPrint.flush();
  }

  public void compautomata(final Set<EventProxy> hidden)
  {
    mPrint.println("Composition:Stats");
    mPrint.println("Hidden Events");
    for (final EventProxy e : hidden) {
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

  public void output(final Collection<AutomatonProxy> auts)
  {
    for (final AutomatonProxy aut : auts) {
      output(aut);
    }
  }

  public void output(final AutomatonProxy aut)
  {
    mPrint.println("Automata:Stats");
    mPrint.println(aut.getName());
    mPrint.println("states: " + aut.getStates().size());
    final TObjectIntHashMap<EventProxy> looped = new TObjectIntHashMap<EventProxy>();
    final TObjectIntHashMap<EventProxy> nonlooped = new TObjectIntHashMap<EventProxy>();
    for (final TransitionProxy t : aut.getTransitions()) {
      if (t.getTarget() == t.getSource()) {
        looped.put(t.getEvent(), looped.get(t.getEvent()) + 1);
      } else {
        nonlooped.put(t.getEvent(), nonlooped.get(t.getEvent()) + 1);
      }
    }
    for (final EventProxy e : aut.getEvents()) {
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
