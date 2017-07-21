//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.NondeterministicDESException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TraceStepProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public abstract class AbstractModelVerifierTest extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractModelVerifierTest()
  {
  }

  public AbstractModelVerifierTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mTraceMarshaller = new JAXBTraceMarshaller(factory);
    mModelVerifier = createModelVerifier(factory);
    setNodeLimit(mModelVerifier);
  }


  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runModelVerifier(final String group, final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect) throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, bindings, expect);
  }

  protected void runModelVerifier(final String group, final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect) throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, subdir, name, bindings, expect);
  }

  protected void runModelVerifier(final File groupdir, final String subdir,
                                  final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect) throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelVerifier(dir, name, bindings, expect);
  }

  protected void runModelVerifier(final File dir, final String name,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect) throws Exception
  {
    final File filename = new File(dir, name);
    runModelVerifier(filename, bindings, expect);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runModelVerifier(final String group, final String name,
                                  final boolean expect) throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, name, expect);
  }

  protected void runModelVerifier(final String group, final String subdir,
                                  final String name, final boolean expect)
      throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runModelVerifier(groupdir, subdir, name, expect);
  }

  protected void runModelVerifier(final String group,
                                  final String subdir1,
                                  final String subdir2,
                                  final String name,
                                  final boolean expect)
      throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir1 = new File(rootdir, group);
    final File groupdir2 = new File(groupdir1, subdir1);
    runModelVerifier(groupdir2, subdir2, name, expect);
  }

  protected void runModelVerifier(final File groupdir, final String subdir,
                                  final String name, final boolean expect)
      throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runModelVerifier(dir, name, expect);
  }

  protected void runModelVerifier(final File dir, final String name,
                                  final boolean expect) throws Exception
  {
    final File filename = new File(dir, name);
    runModelVerifier(filename, expect);
  }

  protected void runModelVerifier(final File filename, final boolean expect)
      throws Exception
  {
    runModelVerifier(filename, (List<ParameterBindingProxy>) null, expect);
  }

  protected void runModelVerifier(final File filename,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect) throws Exception
  {
    mBindings = bindings;
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runModelVerifierWithBindings(des, expect);
  }

  protected void runModelVerifier(final ProductDESProxy des,
                                  final boolean expect) throws Exception
  {
    runModelVerifier(des, null, expect);
  }

  protected void runModelVerifier(final ProductDESProxy des,
                                  final List<ParameterBindingProxy> bindings,
                                  final boolean expect) throws Exception
  {
    mBindings = bindings;
    runModelVerifierWithBindings(des, expect);
  }

  protected ModelVerifier getModelVerifier()
  {
    return mModelVerifier;
  }


  //#########################################################################
  //# To be Overridden/Used by Subclasses
  /**
   * Performs preliminary checks on the counterexample.
   * This method performs some simple checks on the counterexample to make
   * sure that it can be saved. The more advanced semantic checks are
   * performed by {@link #checkCounterExample(ProductDESProxy,TraceProxy)
   * checkCounterExample()} after the counterexample has been written to a file.
   * @param trace The counterexample to be checked.
   * @throws junit.framework.AssertionFailedError to indicate that the
   *   counterexample does not pass the test.
   */
  protected void precheckCounterExample(final TraceProxy trace)
  {
    assertNotNull("Counterexample is NULL!", trace);
    assertNotNull("NULL product DES in trace!", trace.getProductDES());
    assertFalse("NULL automaton in trace!", trace.getAutomata().contains(null));
    assertFalse("NULL event in trace!", trace.getEvents().contains(null));
  }

  /**
   * Checks whether the given counterexample is indeed a counterexample for the
   * property considered in this test. This method should perform a detailed
   * semantic check to confirm that the given counterexample is indeed correct.
   * The default implementation merely checks whether the automata and events
   * contained in the counterexample can be found in the model. All subclasses
   * should override this method to implement specific tests.
   *
   * @param des
   *          The model to be verified.
   * @param trace
   *          The counterexample obtained by from the model checker under test.
   * @throws junit.framework.AssertionFailedError
   *           to indicate that the counterexample is not a correct
   *           counterexample for the property.
   */
  protected void checkCounterExample(final ProductDESProxy des,
                                     final TraceProxy trace) throws Exception
  {
    assertSame("Product DES in trace is not the original model!",
               des, trace.getProductDES());
    final String desName = des.getName();
    final String traceName = trace.getName();
    assertTrue("Trace name '" + traceName + "' does not match model name '" +
               desName + "'!", traceName.startsWith(desName));
    final Collection<AutomatonProxy> automata = des.getAutomata();
    for (final AutomatonProxy aut : trace.getAutomata()) {
      if (!automata.contains(aut)) {
        fail("Trace automaton '" + aut.getName()
            + "' does not match any in product DES!");
      }
    }
    final Collection<EventProxy> events = des.getEvents();
    for (final EventProxy event : trace.getEvents()) {
      if (!events.contains(event)) {
        fail("Trace event '" + event.getName()
            + "' does not match any in product DES!");
      }
    }
  }

  protected StateProxy checkCounterExample(final AutomatonProxy aut,
                                           final TraceProxy trace)
  {
    return checkCounterExample(aut, trace, false);
  }

  protected StateProxy checkCounterExample(final AutomatonProxy aut,
                                           final TraceProxy trace,
                                           final boolean spec)
  {
    final Collection<EventProxy> events = aut.getEvents();
    final Collection<StateProxy> states = aut.getStates();
    final Collection<TransitionProxy> transitions = aut.getTransitions();
    final List<TraceStepProxy> traceSteps = trace.getTraceSteps();
    final Iterator<TraceStepProxy> iter = traceSteps.iterator();
    final TraceStepProxy initStep = iter.next();
    final Map<AutomatonProxy,StateProxy> initMap = initStep.getStateMap();
    StateProxy current = initMap.get(aut);
    if (current == null) {
      for (final StateProxy state : states) {
        if (state.isInitial()) {
          if (current == null) {
            current = state;
          } else {
            fail("Trace specifies no initial state for automaton " +
                 aut.getName() + ", which has more than one initial state!");
          }
        }
      }
      assertNotNull("The automaton " + aut.getName() +
                    " has no initial state!", current);
    } else {
      assertTrue("Trace initial state " + current.getName() +
                 " for automaton " + aut.getName() +
                 " is not an initial state of the automaton!",
                 current.isInitial());
    }
    while (iter.hasNext()) {
      final TraceStepProxy traceStep = iter.next();
      final EventProxy event = traceStep.getEvent();
      final Map<AutomatonProxy,StateProxy> stepMap = traceStep.getStateMap();
      final StateProxy target = stepMap.get(aut);
      if (target == null) {
        if (events.contains(event)) {
          StateProxy next = null;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource() == current && trans.getEvent() == event) {
              if (next == null) {
                next = trans.getTarget();
              } else {
                fail("The counterexample trace does not contain a " +
                     "successor state for the nondeterministic transition" +
                     " in automaton " + aut.getName() + " from source state " +
                     current.getName() + " with event " + event.getName() +
                     "!");
              }
            }
          }
          if (!spec || iter.hasNext()) {
            assertNotNull("The automaton " + aut.getName() +
                          " has no successor state for event " +
                          event.getName() + " from state " +
                          current.getName() + "!",
                          next);
          }
          current = next;
        }
      } else {
        if (events.contains(event)) {
          boolean found = false;
          for (final TransitionProxy trans : transitions) {
            if (trans.getSource() == current && trans.getEvent() == event &&
                trans.getTarget() == target) {
              found = true;
            }
          }
          assertTrue("There is no transition from state " + current.getName() +
                     " to state " + target.getName() + " with event " +
                     event.getName() + " in automaton " + aut.getName() +
                     " as specified in the counterexample trace!", found);
          current = target;
        } else {
          assertSame("The target state specified in the counterexample " +
                     "for the selflooped event " + event.getName() +
                     " is different from the current state of automaton " +
                     aut.getName() + "!", current, target);
        }
      }
    }
    // returns the end state of the counterexample trace
    return current;
  }

  protected File saveCounterExample(final TraceProxy counterexample)
      throws Exception
  {
    assertNotNull(counterexample);
    final String name = counterexample.getName();
    final String ext = mTraceMarshaller.getDefaultExtension();
    final StringBuilder buffer = new StringBuilder(name);
    if (mBindings != null) {
      for (final ParameterBindingProxy binding : mBindings) {
        buffer.append('-');
        buffer.append(binding.getExpression().toString());
      }
    }
    String bindingString = buffer.toString();
    bindingString += ext;
    final String extname = bindingString;
    assertTrue("File name '" + extname + "' too long!",
               bindingString.length() < 255);
    assertTrue("File name '" + extname + "' contains a colon, " +
               "which does not work on all platforms!",
               extname.indexOf(':') < 0);
    final File dir = getOutputDirectory();
    final File filename = new File(dir, extname);
    mTraceMarshaller.marshal(counterexample, filename);
    return filename;
  }

  protected void checkStatistics(final VerificationResult stats)
  {
  }


  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the model verifier under test. This method
   * instantiates the class of the model verifier tested by the particular
   * subclass of this test, and configures it as needed.
   *
   * @param factory
   *          The factory used by the model verifier for trace construction.
   * @return An instance of the model verifier
   */
  protected abstract ModelVerifier createModelVerifier
    (ProductDESProxyFactory factory);

  /**
   * Configures the model verifier under test for a given product DES. This
   * method is called just before the model verifier is started for each model
   * to be tested. Subclasses that override this method should call the
   * superclass method first.
   *
   * @param des
   *          The model to be verified for the current test case.
   */
  protected void configureModelVerifier(final ProductDESProxy des)
  {
    mModelVerifier.setModel(des);
  }


  //#########################################################################
  //# Auxiliary Methods
  protected void runModelVerifierWithBindings(final ProductDESProxy des,
                                              final boolean expect)
      throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    try {
      configureModelVerifier(des);
      final boolean result = mModelVerifier.run();
      TraceProxy counterexample = null;
      if (!result && mModelVerifier.isCounterExampleEnabled()) {
        counterexample = mModelVerifier.getCounterExample();
        precheckCounterExample(counterexample);
        if (counterexample != null) {
          saveCounterExample(counterexample);
        }
      }
      assertEquals("Wrong result from model checker: got " + result +
                   " but should have been " + expect + "!", expect, result);
      final VerificationResult stats = mModelVerifier.getAnalysisResult();
      assertNotNull("No verification result!", stats);
      assertEquals("Wrong result from model checker: got " +
                   stats.isSatisfied() + " but should have been " +
                   expect + "!", expect, stats.isSatisfied());
      if (!expect && mModelVerifier.isCounterExampleEnabled()) {
        checkCounterExample(des, counterexample);
      }
      checkStatistics(stats);
    } catch (final NondeterministicDESException exception) {
      if (mModelVerifier.supportsNondeterminism() ||
          isProductDESDeterministic()) {
        throw exception;
      }
    }
    getLogger().info("Done " + des.getName());
  }


  //#########################################################################
  //# Data Members
  private JAXBTraceMarshaller mTraceMarshaller;
  private ModelVerifier mModelVerifier;
  private List<ParameterBindingProxy> mBindings;

}
