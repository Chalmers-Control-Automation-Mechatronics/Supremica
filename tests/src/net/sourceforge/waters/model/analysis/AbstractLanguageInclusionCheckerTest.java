//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractLanguageInclusionCheckerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;

import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;


public abstract class AbstractLanguageInclusionCheckerTest
  extends AbstractModelVerifierTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractLanguageInclusionCheckerTest()
  {
  }

  public AbstractLanguageInclusionCheckerTest(final String name)
  {
    super(name);
  }

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
  //# Test Cases --- nasty
  public void testVerriegel4Counter1() throws Exception
  {
    final String group = "tests";
    final String dir = "nasty";
    final String name = "verriegel4counter1.wmod";
    runModelVerifier(group, dir, name, false);
  }


  //#########################################################################
  //# Test Cases --- ProfiSAFE
  public void testProfisafeI3HostEFA__neversend0() throws Exception
  {
    testProfisafeI3HostEFA("never_send[0]", false);
  }

  public void testProfisafeI3HostEFA__neversend1() throws Exception
  {
    testProfisafeI3HostEFA("never_send[1]", false);
  }

  public void testProfisafeI3HostEFA__neversend2() throws Exception
  {
    testProfisafeI3HostEFA("never_send[2]", false);
  }

  public void testProfisafeI3HostEFA__neversend3() throws Exception
  {
    testProfisafeI3HostEFA("never_send[3]", false);
  }

  public void testProfisafeI3HostEFA__host_sets_fv_after_host_timeout()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_timeout", true);
  }

  public void testProfisafeI3HostEFA__host_sets_fv_after_host_crc_fault()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_crc_fault", false);
  }

  public void
    testProfisafeI3HostEFA__host_sets_fv_after_host_crc_fault_notinit()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_crc_fault_notinit", true);
  }

  public void
    testProfisafeI3HostEFA__host_sets_fv_after_host_seq_fault_0()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_seq_fault[0]", true);
  }

  public void
    testProfisafeI3HostEFA__host_sets_fv_after_host_seq_fault_3()
    throws Exception
  {
    testProfisafeI3HostEFA("host_sets_fv_after_host_seq_fault[3]", false);
  }

  private void testProfisafeI3HostEFA(final String propname,
                                      final boolean expect)
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_ihost_efa.wmod";
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", 3);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    runModelVerifier(group, dir, name, bindings, expect, propname);
  }

  public void testProfisafeI4SlaveEFA__neversend()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int isfv = 0; isfv <= 1; isfv++) {
      final String vname = isfv == 0 ? "PV" : "FV";
      for (int seqno = 0; seqno <= maxseqno; seqno++) {
        for (int s4 = 0; s4 <= 1; s4++) {
          for (int s3 = 0; s3 <= 1; s3++) {
            for (int s2 = 0; s2 <= 1; s2++) {
              final String propname =
                String.format("never_send[%s][%d][%d][%d][%d]",
                              vname, s4, s3, s2, seqno);
              final boolean cansend =
                (s4 == isfv) && (s4 == 1 || (s3 == 0 && s2 == 0));
              runModelVerifier(group, dir, name, bindings, !cansend, propname);
            }
          }
        }
      }
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_crc_fault_2()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 0; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_crc_fault_2[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, true, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_crc_fault_3()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 0; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_crc_fault_3[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, seqno == 0, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_timeout_2()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 0; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_timeout_2[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, true, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_timeout_3()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno = 0; seqno <= maxseqno; seqno++) {
      final String propname =
        String.format("slave_sets_fv_after_slave_timeout_3[%d]", seqno);
      runModelVerifier(group, dir, name, bindings, seqno == 0, propname);
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_seq_fault_2()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno1 = 0; seqno1 <= maxseqno; seqno1++) {
      for (int seqno2 = 0; seqno2 <= maxseqno; seqno2++) {
        if (seqno1 != seqno2 && seqno1 % maxseqno + 1 != seqno2) {
          final String propname =
            String.format("slave_sets_fv_after_slave_seq_fault_2[%d][%d]",
                          seqno1, seqno2);
          runModelVerifier(group, dir, name, bindings, true, propname);
        }
      }
    }
  }

  public void testProfisafeI4SlaveEFA__slave_sets_fv_after_slave_seq_fault_3()
    throws Exception
  {
    final String group = "tests";
    final String dir = "profisafe";
    final String name = "profisafe_islave_efa.wmod";
    final int maxseqno = 4;
    final ParameterBindingProxy binding = createBinding("MAXSEQNO", maxseqno);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    for (int seqno1 = 0; seqno1 <= maxseqno; seqno1++) {
      for (int seqno2 = 0; seqno2 <= maxseqno; seqno2++) {
        if (seqno1 != seqno2 && seqno1 % maxseqno + 1 != seqno2) {
          final String propname =
            String.format("slave_sets_fv_after_slave_seq_fault_3[%d][%d]",
                          seqno1, seqno2);
          runModelVerifier(group, dir, name, bindings, seqno2 == 0, propname);
        }
      }
    }
  }

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
    runModelVerifier(group, name, null, expect, propname);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    runModelVerifier(group, subdir, name, null, expect, propname);
  }

  protected void runModelVerifier(final String group,
                                  final String name,
                                  final boolean expect,
                                  final List<ParameterBindingProxy> bindings,
                                  final String propname)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, bindings, expect, propname);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, subdir, name, bindings, expect, propname);
  }

  protected void runModelVerifier(final File groupdir,
                                  final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelVerifier(dir, name, bindings, expect, propname);
  }

  protected void runModelVerifier(final File dir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final File filename = new File(dir, name);
    runModelVerifier(filename, bindings, expect, propname);
  }

  protected void runModelVerifier(final File filename,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect,
                                  final String propname)
    throws Exception
  {
    final ProductDESProxy des = getCompiledDES(filename, bindings, propname);
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
    runModelVerifier(propdes, bindings, expect);
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractAnalysisTest
  protected ProductDESProxy getCompiledDES
    (final File filename,
     final List<ParameterBindingProxy> bindings,
     final String propname)
    throws Exception
  {
    try {
      mPropertyName = propname;
      return getCompiledDES(filename, bindings);
    } finally {
      mPropertyName = null;
    }
  }

  protected void configure(final ModuleCompiler compiler)
  {
    final Collection<String> empty = Collections.emptyList();
    compiler.setEnabledPropositionNames(empty);
    if (mPropertyName != null) {
      final Collection<String> prop = Collections.singletonList(mPropertyName);
      compiler.setEnabledPropertyNames(prop);
    }
  }


  //#########################################################################
  //# Overrides for abstract base class
  //# net.sourceforge.waters.analysis.AbstractModelVerifierTest
  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace)
    throws Exception
  {
    super.checkCounterExample(des, trace);
    final SafetyTraceProxy counterexample = (SafetyTraceProxy) trace;
  	
    final List<EventProxy> eventlist = counterexample.getEvents();
    final int len = eventlist.size();
    assertTrue("Empty Counterexample!", len > 0);
  	  	
    final Collection<AutomatonProxy> automata = des.getAutomata();
    boolean rejected = false;
    for (final AutomatonProxy aut : automata) {
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
    for (final StateProxy state : states) {
      if (state.isInitial()) {
        current = state;
        break;
      }
    }
    if (current == null) {
      return steps;
    }
    for (final EventProxy event : counterexample) {
      steps++;
      if (events.contains(event)) {
        boolean found = false;
        for (final TransitionProxy trans : transitions) {
          if (trans.getSource()== current && trans.getEvent() == event) {
            current = trans.getTarget();
            found = true;
            break;
          }
        }
        if (!found) {        	
          return steps;
        }
      }
    }
    return steps + 1;
  }


  //#########################################################################
  //# Data Members
  private String mPropertyName;

}
