//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractSynthesizerTest
//###########################################################################
//# $Id: 18f19951f0cf41f83ba1b7a947c558cf6453c57a $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import gnu.trove.THashSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.analysis.monolithic.MonolithicControllabilityChecker;
import net.sourceforge.waters.analysis.monolithic.MonolithicLanguageInclusionChecker;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


/**
 * <P>A test for synthesis algorithms.</P>
 *
 * <P>This test is used to check whether a synthesis algorithm is
 * implemented correctly. It uses a fixed set of modules containing
 * simple synthesis problems, runs synthesis on each of them and checks
 * whether the result is correct.</P>
 *
 * <P>All test modules contain a model consisting of plants and
 * specifications, plus a supervisor representing the expected result.
 * The test checks whether a supervisor is obtained by the synthesis
 * algorithm under test if a solution exists, or whether a failure is
 * correctly reported where no solution exists. If a solution exists,
 * it furthermore checks whether the returned supervisor ensures
 * controllability and nonblocking, and whether it is least restrictive
 * according to the expected result given in the test module.</P>
 *
 * <P>After running each test, the automata created by synthesis saved in
 * the <CODE>logs/</CODE> directory, along with any counterexamples
 * produced in verification for failed tests.</P>
 *
 * <P>This test class can be subclassed to test different synthesis
 * implementations. The synthesis implementation only needs to implement the
 * {@link ProductDESBuilder} interface to be tested. Monolithic and
 * compositional synthesis are both supported.</P>
 *
 * <P>TODO. Presently, the test performs plantification. This should
 * probably be done by individual synthesisers instead, as not all
 * algorithms will require this step.</P>
 *
 * @author Robi Malik
 */

public abstract class AbstractSynthesizerTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractSynthesizerTest()
  {
  }

  public AbstractSynthesizerTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSynthesizer = createSynthesizer(factory);
    mControllabilityChecker = new MonolithicControllabilityChecker(factory);
    mLanguageInclusionChecker =
      new MonolithicLanguageInclusionChecker(factory);
    mConflictChecker = new MonolithicConflictChecker(factory);
    mTraceMarshaller = new JAXBTraceMarshaller(factory);
    setNodeLimit();
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mSynthesizer = null;
    mControllabilityChecker = null;
    mConflictChecker = null;
    mLanguageInclusionChecker = null;
    mTraceMarshaller = null;
    mBindings = null;
    super.tearDown();
  }


  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final EventProxy marking =
        factory.createEventProxy(EventDeclProxy.DEFAULT_MARKING_NAME,
                                 EventKind.PROPOSITION);
    final Collection<EventProxy> events = Collections.singletonList(marking);
    final ProductDESProxy des =
        factory.createProductDESProxy("empty", events, null);
    runSynthesizer(des, true);
  }

  public void testReentrant()
    throws Exception
  {
    testEmpty();
    testSmallFactory2();
    testSajed();
    testEmpty();
    testSajed();
    testSmallFactory2();
    testTransferLine1();
  }

  public void testOverflowException()
    throws Exception
  {
    try {
      final ProductDESBuilder synthesizer = getSynthesizer();
      synthesizer.setNodeLimit(2);
      testSmallFactory2();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Test Cases --- synthesis
  public void testBigFactory() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "big_factory";
    runSynthesizer(group, subdir, name, true);
  }

  public void testCell() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "cell";
    runSynthesizer(group, subdir, name, true);
  }

  public void testSajed() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "sajed";
    runSynthesizer(group, subdir, name, true);
  }

  public void testSmallFactory2() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "small_factory_2";
    runSynthesizer(group, subdir, name, true);
  }

  public void testTrafficlights() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "trafficlights";
    runSynthesizer(group, subdir, name, true);
  }

  public void testTransferLine1() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "transferline_1";
    runSynthesizer(group, subdir, name, true);
  }

  public void testTransferLine2() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "transferline_2";
    runSynthesizer(group, subdir, name, true);
  }

  public void testCatMouse() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "cat_mouse";
    runSynthesizer(group, subdir, name, true);
  }

  public void testProductionSystem() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "production_system";
    runSynthesizer(group, subdir, name, true);
  }

  public void testManufacturingSystem() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "manufacturing_system";
    runSynthesizer(group, subdir, name, true);
  }

  public void testDosingUnit() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "dosing_unit";
    runSynthesizer(group, subdir, name, true);
  }

  public void testIPC() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "IPC";
    runSynthesizer(group, subdir, name, true);
  }

  public void testRobotAssemblyCell() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "robot_assembly_cell";
    runSynthesizer(group, subdir, name, true);
  }

  public void testTankProcess() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "tank_process";
    runSynthesizer(group, subdir, name, true);
  }

  public void testParrow() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "parrow";
    runSynthesizer(group, subdir, name, true);
  }

  public void testManWolf() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "man_wolf";
    runSynthesizer(group, subdir, name, true);
  }

  public void testProfessorPen() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "professor_pen";
    runSynthesizer(group, subdir, name, true);
  }

  //#########################################################################
  //# Instantiating and Checking Modules
  protected void runSynthesizer(final String group,
                                final String name,
                                final List<ParameterBindingProxy> bindings,
                                final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runSynthesizer(groupdir, name, bindings, expect);
  }

  protected void runSynthesizer(final String group,
                                final String subdir,
                                final String name,
                                final List<ParameterBindingProxy> bindings,
                                final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runSynthesizer(groupdir, subdir, name, bindings, expect);
  }

  protected void runSynthesizer(final File groupdir,
                                final String subdir,
                                final String name,
                                final List<ParameterBindingProxy> bindings,
                                final boolean expect)
    throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runSynthesizer(dir, name, bindings, expect);
  }

  protected void runSynthesizer(final File dir,
                                final String name,
                                final List<ParameterBindingProxy> bindings,
                                final boolean expect)
    throws Exception
  {
    final String ext = getTestExtension();
    final File filename = new File(dir, name + ext);
    runSynthesizer(filename, bindings, expect);
  }


  //#########################################################################
  //# Checking Instantiated Product DES problems
  protected void runSynthesizer(final String group,
                                final String name,
                                final boolean expect)
    throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runSynthesizer(groupdir, name, expect);
  }

  protected void runSynthesizer(final String group,
                                final String subdir,
                                final String name,
                                final boolean expect)
      throws Exception
  {
    final File rootdir = getWatersInputRoot();
    final File groupdir = new File(rootdir, group);
    runSynthesizer(groupdir, subdir, name, expect);
  }

  protected void runSynthesizer(final File groupdir,
                                final String subdir,
                                final String name,
                                final boolean expect)
      throws Exception
  {
    final File dir = new File(groupdir, subdir);
    runSynthesizer(dir, name, expect);
  }

  protected void runSynthesizer(final File dir,
                                final String name,
                                final boolean expect)
    throws Exception
  {
    final String ext = getTestExtension();
    final File filename = new File(dir, name + ext);
    runSynthesizer(filename, expect);
  }

  protected void runSynthesizer(final File filename,
                                final boolean expect)
    throws Exception
  {
    runSynthesizer(filename, (List<ParameterBindingProxy>) null, expect);
  }

  protected void runSynthesizer(final File filename,
                                final List<ParameterBindingProxy> bindings,
                                final boolean expect)
    throws Exception
  {
    mBindings = bindings;
    final ProductDESProxy des = getCompiledDES(filename, bindings);
    runSynthesizerWithBindings(des, expect);
  }

  protected void runSynthesizer(final ProductDESProxy des,
                                final boolean expect)
    throws Exception
  {
    runSynthesizer(des, null, expect);
  }

  protected void runSynthesizer(final ProductDESProxy des,
                                final List<ParameterBindingProxy> bindings,
                                final boolean expect)
    throws Exception
  {
    mBindings = bindings;
    runSynthesizerWithBindings(des, expect);
  }

  protected ProductDESBuilder getSynthesizer()
  {
    return mSynthesizer;
  }


  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the synthesizer under test. This method
   * instantiates the class of the synthesizer tested by the particular
   * subclass of this test, and configures it as needed.
   * @param factory
   *          The factory used by the synthesizer to create its output.
   * @return An instance of the synthesizer.
   */
  protected abstract ProductDESBuilder createSynthesizer
    (ProductDESProxyFactory factory);

  /**
   * Configures the automaton builder under test for a given product DES. This
   * method is called just before the automaton builder is started for each
   * model to be tested. Subclasses that override this method should call the
   * superclass method first.
   * @param des
   *          The model to be analysed for the current test case.
   */
  protected void configureSynthesizer(final ProductDESProxy des)
  {
    mSynthesizer.setModel(des);
  }

  /**
   * Returns the extension used for all test files
   */
  protected String getTestExtension()
  {
    return ".wmod";
  }


  //#########################################################################
  //# Testing Procedure
  protected void runSynthesizerWithBindings(final ProductDESProxy des,
                                            final boolean expect)
      throws Exception
  {
    getLogger().info("Checking " + des.getName() + " ...");
    final ProductDESProxy plantified = plantify(des);
    configureSynthesizer(plantified);
    mSynthesizer.run();
    final ProductDESResult result = mSynthesizer.getAnalysisResult();
    checkResult(des, result, expect);
    getLogger().info("Done " + des.getName());
  }

  private void checkResult(final ProductDESProxy des,
                           final ProductDESResult result,
                           final boolean expect)
    throws Exception
  {
    if (result.isSatisfied()) {
      final String name = des.getName();
      final String basename = appendSuffixes(name, mBindings);
      final String comment = "Test output from " +
        ProxyTools.getShortClassName(mSynthesizer) + '.';
      final Collection<EventProxy> events = des.getEvents();
      final Collection<AutomatonProxy> plants = des.getAutomata();
      final Collection<AutomatonProxy> supervisors =
        result.getComputedAutomata();
      final int numAutomata = plants.size() + supervisors.size();
      final Collection<AutomatonProxy> automata =
        new ArrayList<AutomatonProxy>(numAutomata);
      automata.addAll(plants);
      automata.addAll(supervisors);
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      final ProductDESProxy combined =
        factory.createProductDESProxy(name, comment, null, events, automata);
      saveDES(combined, basename);
      assertTrue("Expected failed synthesis, but got a result!", expect);
      final KindTranslator vtrans =
        new VerificationKindTranslator(supervisors);
      verifySupervisor(combined, mControllabilityChecker,
                       vtrans, "controllable");
      verifySupervisor(combined, mConflictChecker, vtrans, "nonconflicting");
      final KindTranslator ltrans =
        new LeastRestrictivenessKindTranslator(supervisors);
      verifySupervisor(combined, mLanguageInclusionChecker,
                       ltrans, "least restrictive");
    } else {
      assertFalse("Synthesis failed, but the problem has a solution!",
                  expect);
    }
  }

  private void verifySupervisor(final ProductDESProxy des,
                                final ModelVerifier verifier,
                                final KindTranslator translator,
                                final String propname)
    throws Exception
  {
    verifier.setModel(des);
    verifier.setKindTranslator(translator);
    verifier.run();
    if (!verifier.isSatisfied()) {
      final TraceProxy counterexample = verifier.getCounterExample();
      final File file = saveCounterExample(counterexample);
      fail("Synthesis result is not " + propname +
           "!\nCounterexample saved to " + file);
    }
  }

  private File saveCounterExample(final TraceProxy counterexample)
    throws Exception
  {
    assertNotNull(counterexample);
    final String name = counterexample.getName();
    final String basename = appendSuffixes(name, mBindings);
    final String ext = mTraceMarshaller.getDefaultExtension();
    final String extname = basename + ext;
    final File dir = getOutputDirectory();
    final File filename = new File(dir, extname);
    ensureParentDirectoryExists(filename);
    mTraceMarshaller.marshal(counterexample, filename);
    return filename;
  }

  private void setNodeLimit()
  {
    final String prop = System.getProperty("waters.analysis.statelimit");
    if (prop != null) {
      final int limit = Integer.parseInt(prop);
      mSynthesizer.setNodeLimit(limit);
    }
  }


  //#########################################################################
  //# Plantification
  /**
   * Returns a plantified version of the given product DES.
   * Plantification means that all specification automata are replaced
   * by plants with uncontrollable transitions to a dump state added to
   * all states where the uncontrollable event in question is not defined.
   * This method implements simple plantification.
   * All uncontrollable events are considered.
   */
  private ProductDESProxy plantify(final ProductDESProxy des)
    throws OverflowException
  {
    final Collection<AutomatonProxy> automata = des.getAutomata();
    final int numAutomata = automata.size();
    final Collection<AutomatonProxy> plantified =
      new ArrayList<AutomatonProxy>(numAutomata);
    for (final AutomatonProxy aut : automata) {
      switch (aut.getKind()) {
      case PLANT:
        plantified.add(aut);
        break;
      case SPEC:
        final AutomatonProxy plant = plantify(aut);
        plantified.add(plant);
        break;
      default:
        break;
      }
    }
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    final String name = des.getName();
    final String comment = "Plantified version of " + name;
    final Collection<EventProxy> events = des.getEvents();
    return factory.createProductDESProxy(name, comment, null,
                                         events, plantified);
  }

  private AutomatonProxy plantify(final AutomatonProxy spec)
    throws OverflowException
  {
    final Collection<EventProxy> events = spec.getEvents();
    final int numEvents = events.size();
    final Collection<EventProxy> uncontrollables =
      new ArrayList<EventProxy>(numEvents);
    for (final EventProxy event : events) {
      if (event.getKind() == EventKind.UNCONTROLLABLE) {
        uncontrollables.add(event);
      }
    }
    final KindTranslator translator = IdenticalKindTranslator.getInstance();
    final EventEncoding eventEnc =
      new EventEncoding(uncontrollables, translator);
    final StateEncoding stateEnc = new StateEncoding(spec);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(spec, eventEnc, stateEnc,
                                       ListBufferTransitionRelation.
                                       CONFIG_SUCCESSORS);
    final int numStates = rel.getNumberOfStates();
    final Collection<StateProxy> states = new ArrayList<StateProxy>(numStates + 1);
    states.addAll(spec.getStates());
    StateProxy dump = null;
    final Collection<TransitionProxy> transitions =
      new ArrayList<TransitionProxy>(spec.getTransitions());
    final TransitionIterator iter = rel.createSuccessorsReadOnlyIterator();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    for (int s = 0; s < numStates; s++) {
      final StateProxy state = stateEnc.getState(s);
      for (final EventProxy event : uncontrollables) {
        final int e = eventEnc.getEventCode(event);
        iter.reset(s, e);
        if (!iter.advance()) {
          if (dump == null) {
            dump = factory.createStateProxy(":dump");
            states.add(dump);
          }
          final TransitionProxy trans =
            factory.createTransitionProxy(state, event, dump);
          transitions.add(trans);
        }
      }
    }
    final String name = spec.getName();
    return factory.createAutomatonProxy(name, ComponentKind.PLANT,
                                        events, states, transitions);
  }


  //#########################################################################
  //# Inner Class VerificationKindTranslator
  private static class VerificationKindTranslator
    implements KindTranslator
  {
    //#######################################################################
    //# Constructor
    private VerificationKindTranslator
      (final Collection<AutomatonProxy> computedSupervisors)
    {
      mComputedSupervisors = new THashSet<AutomatonProxy>(computedSupervisors);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      switch (aut.getKind()) {
      case PLANT:
        return ComponentKind.PLANT;
      case SPEC:
        return ComponentKind.SPEC;
      case SUPERVISOR:
        if (mComputedSupervisors.contains(aut)) {
          return ComponentKind.SPEC;
        } else {
          return null;
        }
      default:
        return null;
      }
    }

    public EventKind getEventKind(final EventProxy event)
    {
      return event.getKind();
    }

    //#######################################################################
    //# Data Members
    private final Collection<AutomatonProxy> mComputedSupervisors;
  }


  //#########################################################################
  //# Inner Class LeastRestrictivenessKindTranslator
  private static class LeastRestrictivenessKindTranslator
    implements KindTranslator
  {
    //#######################################################################
    //# Constructor
    private LeastRestrictivenessKindTranslator
      (final Collection<AutomatonProxy> computedSupervisors)
    {
      mComputedSupervisors = new THashSet<AutomatonProxy>(computedSupervisors);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    public ComponentKind getComponentKind(final AutomatonProxy aut)
    {
      switch (aut.getKind()) {
      case PLANT:
      case SPEC:
        return ComponentKind.PLANT;
      case SUPERVISOR:
        if (mComputedSupervisors.contains(aut)) {
          return ComponentKind.SPEC;
        } else {
          return ComponentKind.PLANT;
        }
      default:
        return null;
      }
    }

    public EventKind getEventKind(final EventProxy event)
    {
      switch (event.getKind()) {
      case CONTROLLABLE:
      case UNCONTROLLABLE:
        return EventKind.UNCONTROLLABLE;
      case PROPOSITION:
        return EventKind.PROPOSITION;
      default:
        return null;
      }
    }

    //#######################################################################
    //# Data Members
    private final Collection<AutomatonProxy> mComputedSupervisors;
  }


  //#########################################################################
  //# Data Members
  private ProductDESBuilder mSynthesizer;
  private ControllabilityChecker mControllabilityChecker;
  private LanguageInclusionChecker mLanguageInclusionChecker;
  private ConflictChecker mConflictChecker;
  private JAXBTraceMarshaller mTraceMarshaller;
  private List<ParameterBindingProxy> mBindings;

}
