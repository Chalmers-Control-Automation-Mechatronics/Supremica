//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractLanguageInclusionCheckerTest
//###########################################################################
//# $Id: AbstractLanguageInclusionCheckerTest.java,v 1.2 2006-11-15 05:20:02 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.LinkedList;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.model.analysis.AbstractModelVerifierTest;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.xsd.base.ComponentKind;


public abstract class AbstractLanguageInclusionCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy des = factory.createProductDESProxy("empty");
    runModelVerifier(des, true);
  }

  public void testReentrant()
    throws Exception
  {
    testEmpty();
    testSmallFactory2();
    testEmpty();
    testSmallFactory2();
  }


  //#########################################################################
  //# Test Cases --- no properties
  public void testSmallFactory2() throws Exception
  {
    final String group = "handwritten";
    final String name = "small_factory_2.wdes";
    runModelVerifier(group, name, true);
  }

  public void testTictactoe() throws Exception
  {
    final String group = "handwritten";
    final String name = "tictactoe.wdes";
    runModelVerifier(group, name, true);
  }


  //#########################################################################
  //# Test Cases --- ProfiSAFE
  public void testProfisafeI4Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeI4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_i4_slave.wmod";
    runModelVerifier(group, dir, name, true);
  }

  public void testProfisafeO4Host__fv_crc() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_host.wmod";
    final String propname = "HOST__fv_crc__property";
    runModelVerifier(group, dir, name, false, propname);
  }

  public void testProfisafeO4Slave() throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_o4_slave.wmod";
    runModelVerifier(group, dir, name, true);
  }


  //#########################################################################
  //# Test Cases -- Parameterised


  //#########################################################################
  //# Specialised Invocation Code to Check Particular Properties
  protected void runModelVerifier(final String group,
                                  final String name,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File rootdir = getInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, expect, propname);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File rootdir = getInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, subdir, name, expect, propname);
  }

  protected void runModelVerifier(final File groupdir,
                                  final String subdir,
                                  final String name,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelVerifier(dir, name, expect, propname);
  }

  protected void runModelVerifier(final File dir,
                                  final String name,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File filename = new File(dir, name);
    runModelVerifier(filename, expect, propname);
  }
    
  protected void runModelVerifier(final File filename,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(filename);
    final String name = des.getName() + ":" + propname;
    final Collection<EventProxy> events = des.getEvents();
    final Collection<AutomatonProxy> automata =
      new LinkedList<AutomatonProxy>();
    boolean propfound = false;
    for (final AutomatonProxy aut : des.getAutomata()) {
      final ComponentKind kind = aut.getKind();
      switch (kind) {
      case PLANT:
      case SPEC:
        automata.add(aut);
        break;
      case PROPERTY:
        if (aut.getName().equals(propname)) {
          automata.add(aut);
          propfound = true;
        }
        break;
      default:
        break;
      }
    }
    assertTrue("Property '" + propname + "' not found!", propfound);
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final ProductDESProxy propdes =
      factory.createProductDESProxy(name, events, automata);
    runModelVerifier(propdes, expect);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace)
  {
    assertNotNull(trace);
    final SafetyTraceProxy counterexample = (SafetyTraceProxy) trace;
  	
    final List<EventProxy> eventlist = counterexample.getEvents();
    final int len = eventlist.size();
    assertTrue("Empty Counterexample!", len > 0);
  	  	
    final Collection<AutomatonProxy> automata = des.getAutomata();
    boolean rejected = false;
    for (final AutomatonProxy aut : automata){
      final ComponentKind kind = aut.getKind();
      final int accepted = checkCounterExample(aut, eventlist);
      switch (kind) {
      case PLANT:
      case SPEC:
	assertTrue("Counterexample not accepted by automaton " +
		   aut.getName() + "!", accepted == len);
        break;
      case PROPERTY:
	assertFalse("Counterexample rejected too early (step " + accepted +
		    ") by property " + aut.getName() + "!",
		    accepted < len - 1);
	rejected |= (accepted == len - 1);
        break;
      default:
        break;
      }
    }
    assertTrue("Counterexample not rejected by any component!", rejected);
  }

  private int checkCounterExample(final AutomatonProxy aut,
                                  final List<EventProxy> counterexample)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();

    int steps = -1;
    StateProxy current = null;
    for (final StateProxy state : states){
      if (state.isInitial()){
        current = state;
        break;
      }
    }
    if (current == null){
      return steps;
    }
    for (final EventProxy event : counterexample){
      steps++;
      if (events.contains(event)){
        boolean found = false;
        for(final TransitionProxy trans : transitions){
          if (trans.getSource()== current && trans.getEvent() == event){
            current = trans.getTarget();
            found = true;
            break;
          }
        }
        if(!found){        	
          return steps;
        }
      }
    }
    return steps + 1;
  }

}
