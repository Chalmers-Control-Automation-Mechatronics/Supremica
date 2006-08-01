//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.analysis
//# CLASS:   ControllabilityCheckerTest
//###########################################################################
//# $Id: ControllabilityCheckerTest.java,v 1.6 2006-08-01 22:05:24 robi Exp $
//###########################################################################

package net.sourceforge.waters.analysis;

import java.io.File;
import java.util.Collection;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestSuite;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ControllabilityCheckerTest extends AbstractWatersTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public void setUp() throws Exception
  {    
    mFactory = ProductDESElementFactory.getInstance();
    mTraceMarshaller = new JAXBTraceMarshaller(mFactory);
    mDESMarshaller = new JAXBProductDESMarshaller(mFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerUnmarshaller(mDESMarshaller);
  } 
  
  public static Test suite() { 
    TestSuite testSuite = new TestSuite(ControllabilityCheckerTest.class);     
    return testSuite;
  }

  public static void main(String[] args) { 
    
    junit.textui.TestRunner.run(suite()); 
  }

  //#########################################################################
  //# Test Cases
  public void testBigFactory() throws Exception
  {
    String group = "valid";
    String dir  = "big_factory";
    String name = "bfactory.wdes";
    runControllabilityChecker(group, dir, name, false);
  }
  
  public void testBmw_fh() throws Exception
  {
    String group = "valid";
    String dir  = "bmw_fh";
    String name = "bmw_fh.wdes";
    runControllabilityChecker(group, dir, name, true);
  }
  
  public void testBorder_cases() throws Exception
  {
    String group = "valid";
    String dir  = "border_cases";
    String name = "never_blow_up.wdes";
    runControllabilityChecker(group, dir, name, false);
  }
 
  public void testDebounce() throws Exception
  {
    String group = "valid";
    String dir = "debounce";
    String name = "debounce.wdes";
    runControllabilityChecker(group, dir, name, true);
  }
  
  public void testFalko() throws Exception
  {
    String group = "valid";
    String dir = "falko";
    String name = "falko.wdes";
    runControllabilityChecker(group, dir, name, true);
  }
  
  public void testFtuer() throws Exception
  {
    String group = "valid";
    String dir  = "central_locking";
    String name = "ftuer.wdes";
    runControllabilityChecker(group, dir, name, true);
  } 
  
  public void testFischertechnik() throws Exception
  {
    String group = "valid";
    String dir = "fischertechnik";
    String name = "fischertechnik.wdes";
    runControllabilityChecker(group, dir, name, false);
  }
  
  public void testKoordwsp() throws Exception
  {
    String group = "valid";
    String dir  = "central_locking";
    String name = "koordwsp.wdes";
    runControllabilityChecker(group, dir, name, true);
  } 
  
  public void testMazes() throws Exception
  {
    String group = "valid";
    String dir = "mazes";
    String name = "mazes.wdes";
    runControllabilityChecker(group, dir, name, true);
  }
  
  public void testMx27() throws Exception
  {
    String group = "nasty_tests";
    String name = "mx27.wdes";
    runControllabilityChecker(group, name, false);
  }
  
  public void testSafetydisplay() throws Exception
  {
    String group = "valid";
    String dir = "safetydisplay";
    String name = "safetydisplay.wdes";
    runControllabilityChecker(group, dir, name, true);
  }
  
  public void testSmallFactory() throws Exception
  {
    String group = "valid";
    String dir = "small";
    String name = "small.wdes";
    runControllabilityChecker(group, dir, name, true);
  }
  
  public void testSmallFactoryUncont() throws Exception
  {
    String group = "valid";
    String dir = "small";
    String name = "small_uncont.wdes";
    runControllabilityChecker(group, dir, name, false);
  }

  public void testSmallFactory2() throws Exception
  {
    String group = "handwritten";
    String name = "small_factory_2.wdes";
    runControllabilityChecker(group, name, true);
  }
  
  public void testSmd() throws Exception
  {
    String group = "valid";
    String dir = "smd";
    String name = "smdreset.wdes";
    runControllabilityChecker(group, dir, name, true);
  }

  public void testTictactoe() throws Exception
  {
    String group = "handwritten";
    String name = "tictactoe.wdes";
    runControllabilityChecker(group, name, false);
  }

  public void testTline_0() throws Exception
  {
    String group = "valid";
    String dir = "tline_0";
    String name = "transferline_templ.wdes";
    runControllabilityChecker(group, dir, name, true);
  }
  
  public void testTline_1() throws Exception
  {
    String group = "valid";
    String dir = "tline_1";
    String name = "tline_1.wdes";
    runControllabilityChecker(group, dir, name, true);
  }

  public void testWeiche() throws Exception
  {
    String group = "valid";
    String dir = "vt";
    String name = "weiche.wdes";
    runControllabilityChecker(group, dir, name, true);
  }


  //#########################################################################
  //# Auxiliary Methods  
  private void runControllabilityChecker(final String group,
					 final String name,
					 final boolean expect)
    throws Exception
  {
    final File rootdir = getInputRoot();
    final File groupdir = new File(rootdir, group);
    runControllabilityChecker(groupdir, name, expect);
  }

  private void runControllabilityChecker(final String group,
					 final String subdir,
					 final String name,
					 final boolean expect)
    throws Exception
  {
    final File rootdir = getInputRoot();
    final File groupdir = new File(rootdir, group);
    runControllabilityChecker(groupdir, subdir, name, expect);
  }

  private void runControllabilityChecker(final File groupdir,
					 final String subdir,
					 final String name,
					 final boolean expect)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runControllabilityChecker(dir, name, expect);
  }

  private void runControllabilityChecker(final File dir,
					 final String name,
					 final boolean expect)
    throws Exception
  {
    final File filename = new File(dir, name);
    runControllabilityChecker(filename, expect);
  }


  private void runControllabilityChecker(final File filename,
					 final boolean expect)
    throws Exception
  {
    final ProductDESProxy des =
      (ProductDESProxy) mDocumentManager.load(filename);
    final ControllabilityChecker checker =
      new ControllabilityChecker(des, mFactory);
    final boolean result = checker.run();
    SafetyTraceProxy counterexample = null;
    if (!result) {
      counterexample = checker.getCounterExample();
      saveCounterExample(counterexample);
    }
    assertEquals("Wrong result from controllability checker: got " +
                 result + " but should have been " + expect + "!",
                 result, expect);
    if (!expect) {
      checkCounterExample(des, counterexample);
    }
  }

  private void saveCounterExample(final SafetyTraceProxy counterexample)
    throws Exception
  {
    final String name = counterexample.getName();
    final String ext = mTraceMarshaller.getDefaultExtension();
    final File dir = getOutputDirectory();
    final File filename = new File(dir, name + ext);
    ensureParentDirectoryExists(filename);
    mTraceMarshaller.marshal(counterexample, filename);
  }

  private void checkCounterExample(final ProductDESProxy des,
				   final SafetyTraceProxy counterexample)
  {
    assertNotNull(counterexample);
  	
    final List<EventProxy> eventlist = counterexample.getEvents();
    final int len = eventlist.size();
    assertTrue("Empty Counterexample!", len > 0);
  	  	
    final EventProxy last = eventlist.get(len-1);
    final EventKind ekind = last.getKind();
    assertEquals(ekind, EventKind.UNCONTROLLABLE);
  	
    final Collection<AutomatonProxy> automata = des.getAutomata();
    boolean rejected = false;
    for (final AutomatonProxy aut : automata){
      final ComponentKind akind =aut.getKind();
      final int accepted = checkCounterExample(aut, eventlist);
      if (akind.equals(ComponentKind.PLANT)){
	assertTrue("Counterexample not accepted by plant " +
		   aut.getName() + "!"+" accepted "+accepted+" len "+len,
		   accepted == len);
      } else if(akind.equals(ComponentKind.SPEC)){
	assertFalse("Counterexample rejected too early (step " + accepted +
		    ") by spec " + aut.getName() + "!",
		    accepted < len - 1);
	rejected |= (accepted == len - 1);
      }
    }
    assertTrue("Counterexample not rejected by any spec!", rejected);
  }
  
  private  int checkCounterExample(final AutomatonProxy aut,
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


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mFactory;
  private JAXBProductDESMarshaller mDESMarshaller;
  private JAXBTraceMarshaller mTraceMarshaller;
  private DocumentManager mDocumentManager;	

}
