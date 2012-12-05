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

import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
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
 * @author Robi Malik
 */

public abstract class AbstractSupervisorSynthesizerTest
  extends AbstractAnalysisTest
{

  //#########################################################################
  //# Overrides for base class junit.framework.TestCase
  public AbstractSupervisorSynthesizerTest()
  {
  }

  public AbstractSupervisorSynthesizerTest(final String name)
  {
    super(name);
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSynthesizer = createSynthesizer(factory);
    mControllabilityChecker = new NativeControllabilityChecker(factory);
    mLanguageInclusionChecker = new NativeLanguageInclusionChecker(factory);
    mConflictChecker = new NativeConflictChecker(factory);
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
      final SupervisorSynthesizer synthesizer = getSynthesizer();
      synthesizer.setNodeLimit(2);
      testSmallFactory2();
      fail("Expected overflow not caught!");
    } catch (final OverflowException exception) {
      // O.K.
    }
  }


  //#########################################################################
  //# Test Cases --- synthesis
  public void testAip0Sub1P0() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "aip0sub1p0";
    runSynthesizer(group, subdir, name, true);
  }

  public void testBallProcess() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "ball_Process";
    runSynthesizer(group, subdir, name, false);
  }

  public void testBigFactory() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "big_factory";
    runSynthesizer(group, subdir, name, true);
  }

  public void testCatMouse() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "cat_mouse";
    runSynthesizer(group, subdir, name, true);
  }

  public void testCell() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "cell";
    runSynthesizer(group, subdir, name, true);
  }

  public void testCoffeeMachine() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "coffee_machine";
    runSynthesizer(group, subdir, name, true);
  }

  public void testDebounce() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "debounce";
    runSynthesizer(group, subdir, name, true);
  }

  public void testDosingUnit() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "dosing_unit";
    runSynthesizer(group, subdir, name, true);
  }

  public void testFalko() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "falko";
    runSynthesizer(group, subdir, name, true);
  }

  public void testIPC() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "IPC";
    runSynthesizer(group, subdir, name, true);
  }

  public void testManufacturingSystem() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "manufacturing_system";
    runSynthesizer(group, subdir, name, true);
  }

  public void testManWolf() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "man_wolf";
    runSynthesizer(group, subdir, name, true);
  }

  public void testNoPlant() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "no_plant";
    runSynthesizer(group, subdir, name, true);
  }

  public void testNoPlant2() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "no_plant2";
    runSynthesizer(group, subdir, name, true);
  }

  public void testNoPlant3() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "no_plant3";
    runSynthesizer(group, subdir, name, true);
  }

  public void testNoPlant4() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "no_plant4";
    runSynthesizer(group, subdir, name, true);
  }

  public void testParrow() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "parrow";
    runSynthesizer(group, subdir, name, true);
  }

  public void testPathFinder() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "path_finder";
    runSynthesizer(group, subdir, name, true);
  }

  public void testPlantify() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "plantify";
    runSynthesizer(group, subdir, name, true);
  }

  public void testProductionSystem() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "production_system";
    runSynthesizer(group, subdir, name, true);
  }

  public void testProfessorPen() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "professor_pen";
    runSynthesizer(group, subdir, name, true);
  }

  public void testPV35() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "pv35";
    runSynthesizer(group, subdir, name, true);
  }

  public void testRobotAssemblyCell() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "robot_assembly_cell";
    runSynthesizer(group, subdir, name, true);
  }

  public void testSajed() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "sajed";
    runSynthesizer(group, subdir, name, true);
  }

  public void testSimpleManufacturingSystem() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "simple_manufacturing_system";
    runSynthesizer(group, subdir, name, true);
  }

  public void testSmallFactory2() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "small_factory_2";
    runSynthesizer(group, subdir, name, true);
  }

  public void testSoeCont() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "soe_cont";
    runSynthesizer(group, subdir, name, true);
  }

  public void testTankProcess() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "tank_process";
    runSynthesizer(group, subdir, name, true);
  }

  public void testTbedMinsync() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "tbed_minsync";
    runSynthesizer(group, subdir, name, true);
  }

  public void testTeleNetwork() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "tele_network";
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

  public void testThreeRobot() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "three_robot";
    runSynthesizer(group, subdir, name, true);
  }

  public void testZeroSup() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "test_zero_sup";
    runSynthesizer(group, subdir, name, false);
  }


  //#########################################################################
  //# Test Cases --- BIG
  public void testKoordWspSynth() throws Exception
  {
    final String group = "tests";
    final String subdir = "synthesis";
    final String name = "koordwsp_synth";
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

  protected SupervisorSynthesizer getSynthesizer()
  {
    return mSynthesizer;
  }


  //#########################################################################
  //# To be Provided by Subclasses
  /**
   * Creates an instance of the synthesiser under test. This method
   * instantiates the class of the synthesiser tested by the particular
   * subclass of this test, and configures it as needed.
   * @param factory
   *          The factory used by the synthesiser to create its output.
   * @return An instance of the synthesiser.
   */
  protected abstract SupervisorSynthesizer createSynthesizer
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
    configureSynthesizer(des);
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
      final Collection<AutomatonProxy> computedSupervisors =
        result.getComputedAutomata();
      final int numAutomata = plants.size() + computedSupervisors.size();
      final Collection<AutomatonProxy> automata =
        new ArrayList<AutomatonProxy>(numAutomata);
      final Collection<AutomatonProxy> expectedSupervisors =
        new ArrayList<AutomatonProxy>(plants.size());
      for (final AutomatonProxy aut : plants) {
        switch (aut.getKind()) {
        case PLANT:
        case SPEC:
          automata.add(aut);
          break;
        case SUPERVISOR:
          expectedSupervisors.add(aut);
          break;
        default:
          break;
        }
      }
      automata.addAll(computedSupervisors);
      final ProductDESProxyFactory factory = getProductDESProxyFactory();
      final ProductDESProxy replaced =
        factory.createProductDESProxy(name, comment, null, events, automata);
      saveDES(replaced, basename);
      assertTrue("Expected failed synthesis, but got a result!", expect);
      verifySupervisor(replaced, mControllabilityChecker,
                       null, "controllable");
      verifySupervisor(replaced, mConflictChecker, null, "nonconflicting");
      automata.addAll(expectedSupervisors);
      final ProductDESProxy combined =
        factory.createProductDESProxy(name, comment, null, events, automata);
      final KindTranslator ltrans =
        new LeastRestrictivenessKindTranslator(computedSupervisors);
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
    if (translator != null) {
      verifier.setKindTranslator(translator);
    }
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
  private SupervisorSynthesizer mSynthesizer;
  private ControllabilityChecker mControllabilityChecker;
  private LanguageInclusionChecker mLanguageInclusionChecker;
  private ConflictChecker mConflictChecker;
  private JAXBTraceMarshaller mTraceMarshaller;
  private List<ParameterBindingProxy> mBindings;

}
