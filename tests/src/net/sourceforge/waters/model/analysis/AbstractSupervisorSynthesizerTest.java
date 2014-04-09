//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractSupervisorSynthesizerTest
//###########################################################################
//# $Id: 18f19951f0cf41f83ba1b7a947c558cf6453c57a $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import gnu.trove.set.hash.THashSet;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.des.ModelVerifier;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
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
 * {@link SupervisorSynthesizer} interface to be tested. Monolithic and
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
    testCertainUnsup();
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
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "aip0sub1p0.wmod");
    runSynthesizer(des, true);
  }

  public void testAGVMF() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "agv_mf.wmod");
    runSynthesizer(des, true);
  }

  public void testBallProcess() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "ball_Process.wmod");
    runSynthesizer(des, false);
  }

  public void testBigFactory() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "big_factory.wmod");
    runSynthesizer(des, true);
  }

  public void testCatMouse() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cat_mouse.wmod");
    runSynthesizer(des, true);
  }

  public void testCatMouseUnsup1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cat_mouse_unsup1.wmod");
    runSynthesizer(des, true);
  }

  public void testCatMouseUnsup2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cat_mouse_unsup2.wmod");
    runSynthesizer(des, true);
  }

  public void testCell() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cell.wmod");
    runSynthesizer(des, true);
  }

  public void testCellSwitch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cell_switch.wmod");
    runSynthesizer(des, true);
  }

  public void testCertainUnsup() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "certainUnsup.wmod");
    runSynthesizer(des, false);
  }

  public void testCoffeeMachine() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "coffee_machine.wmod");
    runSynthesizer(des, true);
  }

  public void testDebounce() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "debounce.wmod");
    runSynthesizer(des, true);
  }

  public void testDosingUnit() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "dosing_unit.wmod");
    runSynthesizer(des, true);
  }

  public void testFalko() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "falko.wmod");
    runSynthesizer(des, true);
  }

  public void testFTechnik() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "incremental_suite", "ftechnik.wmod");
    runSynthesizer(des, false);
  }

  public void testIMS() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ims", "ims_uncont.wmod");
    runSynthesizer(des, true);
  }

  public void testIPC() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC.wmod");
    runSynthesizer(des, true);
  }

  public void testIPCcswitch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC_cswitch.wmod");
    runSynthesizer(des, true);
  }

  public void testIPClswitch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC_lswitch.wmod");
    runSynthesizer(des, true);
  }

  public void testIPCuswicth() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC_uswitch.wmod");
    runSynthesizer(des, true);
  }

  public void testManufacturingSystem() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "manufacturing_system.wmod");
    runSynthesizer(des, true);
  }

  public void testManWolf() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "man_wolf.wmod");
    runSynthesizer(des, true);
  }

  public void testNoPlant1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant1.wmod");
    runSynthesizer(des, true);
  }

  public void testNoPlant2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant2.wmod");
    runSynthesizer(des, true);
  }

  public void testNoPlant3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant3.wmod");
    runSynthesizer(des, true);
  }

  public void testNoPlant4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant4.wmod");
    runSynthesizer(des, true);
  }


  public void testOneStateSup() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "one_state_sup.wmod");
    runSynthesizer(des, true);
  }

  public void testParrow() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "parrow.wmod");
    runSynthesizer(des, true);
  }

  public void testPathFinder() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "path_finder.wmod");
    runSynthesizer(des, true);
  }

  public void testPlantify() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "plantify.wmod");
    runSynthesizer(des, true);
  }

  public void testProductionSystem() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "production_system.wmod");
    runSynthesizer(des, true);
  }

  public void testProfessorPen() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "professor_pen.wmod");
    runSynthesizer(des, true);
  }

  public void testPV35() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "pv35.wmod");
    runSynthesizer(des, true);
  }

  public void testRobotAssemblyCell() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "robot_assembly_cell.wmod");
    runSynthesizer(des, true);
  }

  public void testSajed() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "sajed.wmod");
    runSynthesizer(des, true);
  }

  public void testSimpleManufacturingSystem() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "simple_manufacturing_system.wmod");
    runSynthesizer(des, true);
  }

  public void testSmallFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "small_factory_2.wmod");
    runSynthesizer(des, true);
  }

  public void testSoeCont() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "soe_cont.wmod");
    runSynthesizer(des, true);
  }

  public void testSupRed1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "supred1.wmod");
    runSynthesizer(des, true);
  }

  public void testSupRed2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "supred2.wmod");
    runSynthesizer(des, true);
  }

  public void testSupRed3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "supred3.wmod");
    runSynthesizer(des, true);
  }

  public void testTankProcess() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tank_process.wmod");
    runSynthesizer(des, true);
  }

  public void testTbedMinsync() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tbed_minsync.wmod");
    runSynthesizer(des, true);
  }

  public void testTeleNetwork() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tele_network.wmod");
    runSynthesizer(des, true);
  }

  public void testTrafficlights() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "trafficlights.wmod");
    runSynthesizer(des, true);
  }

  public void testTransferLine1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "transferline_1.wmod");
    runSynthesizer(des, true);
  }

  public void testTransferLine2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "transferline_2.wmod");
    runSynthesizer(des, true);
  }

  public void testTransferLine3() throws Exception
  {
    checkTransferline(3);
  }

  public void testTictactoe() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tictactoe.wmod");
    runSynthesizer(des, true);
  }

  public void testThreeRobot() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "three_robot.wmod");
    runSynthesizer(des, true);
  }

  public void testZeroSup() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "test_zero_sup.wmod");
    runSynthesizer(des, false);
  }

  public void test2LinkAlt() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "2linkalt.wmod");
    runSynthesizer(des, true);
  }


  //#########################################################################
  //# Test Cases --- BIG
  /* Too slow (20min) for supervisor reduction
  public void testAip0Sub1P1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "aip0sub1p1.wmod");
    runSynthesizer(des, true);
  }
  */

  public void test2LinkAltBatch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "2linkalt_batch.wmod");
    runSynthesizer(des, true);
  }

  /* This one is too big for monolithic synthesis.
  public void testKoordWspSynth() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "koordwsp_synth.wmod");
    runSynthesizer(des, true);
  }
  */


  //#########################################################################
  //# Parametrised tests
  private void checkTransferline(final int n) throws Exception
  {
    final ParameterBindingProxy binding = createBinding("N", n);
    final List<ParameterBindingProxy> bindings =
      Collections.singletonList(binding);
    final ProductDESProxy des =
      getCompiledDES(bindings, "tests", "synthesis", "transferline_N.wmod");
    runSynthesizer(des, bindings, true);
  }


  //#########################################################################
  //# Simple Access
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
    throws EventNotFoundException
  {
    mSynthesizer.setModel(des);
    final EventProxy marking =
      AbstractConflictChecker.getMarkingProposition(des);
    mSynthesizer.setConfiguredDefaultMarking(marking);
  }


  //#########################################################################
  //# Testing Procedure
  protected ProductDESResult runSynthesizer(final ProductDESProxy des,
                                            final boolean expect)
    throws Exception
  {
    return runSynthesizer(des, null, expect);
  }

  protected ProductDESResult runSynthesizer(final ProductDESProxy des,
                                            final List<ParameterBindingProxy> bindings,
                                            final boolean expect)
    throws Exception
  {
    mBindings = bindings;
    getLogger().info("Checking " + des.getName() + " ...");
    configureSynthesizer(des);
    mSynthesizer.run();
    final ProductDESResult result = mSynthesizer.getAnalysisResult();
    checkResult(des, result, expect);
    getLogger().info("Done " + des.getName());
    return result;
  }

  protected void checkResult(final ProductDESProxy des,
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
      final Collection<? extends AutomatonProxy> computedSupervisors =
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
      (final Collection<? extends AutomatonProxy> computedSupervisors)
    {
      mComputedSupervisors = new THashSet<AutomatonProxy>(computedSupervisors);
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.analysis.KindTranslator
    @Override
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

    @Override
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

