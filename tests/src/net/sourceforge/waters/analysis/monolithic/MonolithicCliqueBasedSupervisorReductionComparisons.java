package net.sourceforge.waters.analysis.monolithic;

import gnu.trove.set.hash.THashSet;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.analysis.abstraction.AbstractSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy;
import net.sourceforge.waters.analysis.abstraction.SuWonhamSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.cpp.analysis.NativeConflictChecker;
import net.sourceforge.waters.cpp.analysis.NativeControllabilityChecker;
import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.AbstractAnalysisTest;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.OverflowException;
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
import net.sourceforge.waters.model.des.CounterExampleProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.marshaller.JAXBCounterExampleMarshaller;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class MonolithicCliqueBasedSupervisorReductionComparisons
  extends AbstractAnalysisTest
{
  public static void main(final String[] args) throws Exception
  {
    final MonolithicCliqueBasedSupervisorReductionComparisons comparisons =
      args.length == 0
        ? new MonolithicCliqueBasedSupervisorReductionComparisons()
        : new MonolithicCliqueBasedSupervisorReductionComparisons(args[0]);
    comparisons.setUp();
    comparisons.writeHeader();

    comparisons.testEmpty();
    comparisons.testAip0Sub1P0();
    comparisons.testAGVMF();
    comparisons.testBallProcess();
    comparisons.testBigFactory1();
    comparisons.testBigFactory2();
    comparisons.testBigFactory3();
    comparisons.testCatMouse();
    comparisons.testCatMouseUnsup1();
    //comparisons.testCatMouseUnsup2();
    comparisons.testCell();
    //comparisons.testCellSwitch();
    comparisons.testCertainUnsup();
    comparisons.testCoffeeMachine();
    //comparisons.testCT3();
    comparisons.testDebounce();
    comparisons.testDosingUnit();
    comparisons.testFalko();
    comparisons.testFTechnik();
    //comparisons.testIMS();
    //comparisons.testIPC();
    //comparisons.testIPCcswitch();
    //comparisons.testIPClswitch();
    //comparisons.testIPCuswicth();
    comparisons.testManufacturingSystem();
    comparisons.testManWolf();
    comparisons.testNoPlant1();
    comparisons.testNoPlant2();
    comparisons.testNoPlant3();
    comparisons.testNoPlant4();
    comparisons.testOneStateSup();
    comparisons.testParrow();
    comparisons.testPathFinder();
    comparisons.testPlantify();
    comparisons.testProductionSystem();
    comparisons.testProfessorPen();
    comparisons.testPV35();
    //comparisons.testRobotAssemblyCell();
    comparisons.testSajed();
    comparisons.testSelfloop1();
    comparisons.testSelfloop2();
    comparisons.testSimpleManufacturingSystem();
    comparisons.testSmallFactory2();
    comparisons.testSoeCont();
    comparisons.testSupRed1();
    comparisons.testSupRed2();
    comparisons.testSupRed3();
    comparisons.testTankProcess();
    comparisons.testTbedMinsync();
    comparisons.testTeleNetwork();
    comparisons.testTransferLine1();
    //comparisons.testTransferLine2();
    //comparisons.testTransferLine3();
    //comparisons.testTictactoe();
    comparisons.testThreeRobot();
    comparisons.testZeroSup();
    //comparisons.test2LinkAlt();
    //comparisons.test2LinkAltBatch();
    comparisons.tearDown();
  }

  public MonolithicCliqueBasedSupervisorReductionComparisons()
  {
    this.filename = getOutputDirectory() + "/comparisons.csv";
  }

  public MonolithicCliqueBasedSupervisorReductionComparisons(final String filename)
  {
    this.filename = filename;
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    final ProductDESProxyFactory factory = getProductDESProxyFactory();
    mSuWonhamSynthesizer =
      createSynthesizer(factory, createSuWonhamSimplifier());
    mCliqueBasedSynthesizer =
      createSynthesizer(factory, createCliqueBasedSimplifier());
    mPrintWriter =
      new PrintWriter(new BufferedWriter(new FileWriter(new File(filename))));

    mControllabilityChecker = new NativeControllabilityChecker(factory);
    mLanguageInclusionChecker = new NativeLanguageInclusionChecker(factory);
    mConflictChecker = new NativeConflictChecker(factory);
    mTraceMarshaller = new JAXBCounterExampleMarshaller(factory);
    setNodeLimit(mSuWonhamSynthesizer);
    setNodeLimit(mCliqueBasedSynthesizer);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mPrintWriter = null;
    mSuWonhamSynthesizer = null;
    mCliqueBasedSynthesizer = null;
    mControllabilityChecker = null;
    mConflictChecker = null;
    mLanguageInclusionChecker = null;
    mTraceMarshaller = null;
    mBindings = null;
    super.tearDown();
  }

  //#########################################################################
  //# Test Cases --- handcrafted
  public void testEmpty() throws Exception
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

  public void testBigFactory1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "big_factory_1.wmod");
    runSynthesizer(des, true);
  }

  public void testBigFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "big_factory_2.wmod");
    runSynthesizer(des, true);
  }

  public void testBigFactory3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "big_factory_3.wmod");
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

  public void testCT3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "CT3.wmod");
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

  public void testSelfloop1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "selfloop1.wmod");
    runSynthesizer(des, true);
  }

  public void testSelfloop2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "selfloop2.wmod");
    runSynthesizer(des, true);
  }

  public void testSimpleManufacturingSystem() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis",
                     "simple_manufacturing_system.wmod");
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

  //  public void testAGV() throws Exception
  //  {
  //    final ProductDESProxy des =
  //      getCompiledDES("tests", "incremental_suite", "agv.wmod");
  //    runSynthesizer(des, true);
  //  }

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

  public void test2LinkAltBatch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "2linkalt_batch.wmod");
    runSynthesizer(des, true);
  }

  /*
   * This one is too big for monolithic synthesis. public void
   * testKoordWspSynth() throws Exception { final ProductDESProxy des =
   * getCompiledDES("tests", "synthesis", "koordwsp_synth.wmod");
   * runSynthesizer(des, true); }
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
    runSynthesizersAndPrint(des, bindings, true);
  }

  private void writeHeader()
  {
    mPrintWriter.write("DES name");
    mPrintWriter.write(",");

    mPrintWriter.write("CB Number of input states");
    mPrintWriter.write(",");
    mPrintWriter.write("CB Number of output states");
    mPrintWriter.write(",");
    mPrintWriter.write("CB Number of input transitions");
    mPrintWriter.write(",");
    mPrintWriter.write("CB Number of output transitions");
    mPrintWriter.write(",");
    mPrintWriter.write("CB Total runtime");
    mPrintWriter.write(",");

    mPrintWriter.write("SW Number of input states");
    mPrintWriter.write(",");
    mPrintWriter.write("SW Number of output states");
    mPrintWriter.write(",");
    mPrintWriter.write("SW Number of input transitions");
    mPrintWriter.write(",");
    mPrintWriter.write("SW Number of output transitions");
    mPrintWriter.write(",");
    mPrintWriter.write("SW Total runtime");
    mPrintWriter.println();
    mPrintWriter.flush();
  }

  private MonolithicSynthesizer createSynthesizer(final ProductDESProxyFactory factory,
                                                  final AbstractSupervisorReductionTRSimplifier simplifier)
  {
    final MonolithicSynthesizer synthesizer =
      new MonolithicSynthesizer(factory);
    synthesizer.setSupervisorReductionSimplifier(simplifier);
    synthesizer.setSupervisorLocalizationEnabled(true);
    return synthesizer;
  }

  private CliqueBasedSupervisorReductionTRSimplifier createCliqueBasedSimplifier()
  {
    final CliqueBasedSupervisorReductionTRSimplifier simplifier =
      new CliqueBasedSupervisorReductionTRSimplifier();
    simplifier.setIsFindFirst(true);
    simplifier.setHeuristicCoverStrategy(HeuristicCoverStrategy.RANDOM);
    simplifier.setMaxHeuristicCovers(5);
    return simplifier;
  }

  private SuWonhamSupervisorReductionTRSimplifier createSuWonhamSimplifier()
  {
    final SuWonhamSupervisorReductionTRSimplifier simplifier =
      new SuWonhamSupervisorReductionTRSimplifier();
    simplifier.setExperimentalMode(true);
    return simplifier;
  }

  /**
   * Configures the automaton builder under test for a given product DES. This
   * method is called just before the automaton builder is started for each
   * model to be tested. Subclasses that override this method should call the
   * superclass method first.
   *
   * @param des
   *          The model to be analysed for the current test case.
   */
  protected void configureSynthesizer(final ProductDESProxy des,
                                      final SupervisorSynthesizer synthesizer)
    throws EventNotFoundException
  {
    synthesizer.setModel(des);
    final EventProxy marking =
      AbstractConflictChecker.getMarkingProposition(des);
    synthesizer.setConfiguredDefaultMarking(marking);
  }

  //#########################################################################
  //# Testing Procedure
  protected void runSynthesizer(final ProductDESProxy des,
                                final boolean expect)
    throws Exception
  {
    runSynthesizersAndPrint(des, null, expect);
  }

  protected void runSynthesizersAndPrint(final ProductDESProxy des,
                                         final List<ParameterBindingProxy> bindings,
                                         final boolean expect)
    throws Exception
  {
    mBindings = bindings;

    mPrintWriter.write(des.getName());
    mPrintWriter.write(",");
    runSynthesizerAndPrint(des, expect, mCliqueBasedSynthesizer,
                           CliqueBasedSupervisorReductionTRSimplifier.class);
    mPrintWriter.write(",");
    runSynthesizerAndPrint(des, expect, mSuWonhamSynthesizer,
                           SuWonhamSupervisorReductionTRSimplifier.class);

    mPrintWriter.println();
    mPrintWriter.flush();
  }

  protected void runSynthesizerAndPrint(final ProductDESProxy des,
                                        final boolean expect,
                                        final MonolithicSynthesizer synthesizer,
                                        final Class<? extends AbstractSupervisorReductionTRSimplifier> simplifierClazz)
  {
    try {
      configureSynthesizer(des, synthesizer);
      synthesizer.run();
      final MonolithicSynthesisResult result =
        synthesizer.getAnalysisResult();
      checkResult(des, result, expect, synthesizer);
      final List<TRSimplifierStatistics> allStatistics =
        result.getSimplifierStatistics();
      for (int i = 0; i < allStatistics.size(); i++) {
        final TRSimplifierStatistics statisticsForSimplifier =
          allStatistics.get(i);
        if (statisticsForSimplifier.getSimplifierClass()
          .equals(simplifierClazz)) {
          mPrintWriter.write("" + statisticsForSimplifier.getInputStates());
          mPrintWriter.write(",");
          mPrintWriter.write("" + statisticsForSimplifier.getOutputStates());
          mPrintWriter.write(",");
          mPrintWriter
            .write("" + statisticsForSimplifier.getInputTransitions());
          mPrintWriter.write(",");
          mPrintWriter
            .write("" + statisticsForSimplifier.getOutputTransitions());
          mPrintWriter.write(",");
          mPrintWriter.write("" + statisticsForSimplifier.getRunTime());
          break;
        }
      }
    } catch (final Exception ex) {
      mPrintWriter.write(",");
      mPrintWriter.write(",");
      mPrintWriter.write(",");
      mPrintWriter.write(",");
      System.err.println(ex);
    }
  }

  protected void checkResult(final ProductDESProxy des,
                             final ProductDESResult result,
                             final boolean expect,
                             final SupervisorSynthesizer synthesizer)
    throws Exception
  {
    if (result.isSatisfied()) {
      final String name = des.getName();
      final String basename = appendSuffixes(name, mBindings);
      final String comment =
        "Test output from " + ProxyTools.getShortClassName(synthesizer) + '.';
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
      for (final AutomatonProxy aut : computedSupervisors) {
        verifyReachability(aut);
      }
      verifySupervisorControllability(replaced);
      verifySupervisorNonblocking(replaced);
      automata.addAll(expectedSupervisors);
      final ProductDESProxy combined =
        factory.createProductDESProxy(name, comment, null, events, automata);
      final KindTranslator ltrans =
        new LeastRestrictivenessKindTranslator(computedSupervisors);
      verifySupervisor(combined, mLanguageInclusionChecker, ltrans,
                       "least restrictive");
    } else {
      assertFalse("Synthesis failed, but the problem has a solution!",
                  expect);
    }
  }

  protected void verifySupervisorControllability(final ProductDESProxy des)
    throws Exception
  {
    verifySupervisor(des, mControllabilityChecker, null, "controllable");
  }

  protected void verifySupervisorNonblocking(final ProductDESProxy des)
    throws Exception
  {
    verifySupervisor(des, mConflictChecker, null, "nonconflicting");
  }

  private void verifyReachability(final AutomatonProxy aut)
    throws OverflowException
  {
    final TRAutomatonProxy tr = TRAutomatonProxy.createTRAutomatonProxy(aut);
    final ListBufferTransitionRelation rel = tr.getTransitionRelation();
    if (rel.checkReachability()) {
      for (int s = 0; s < rel.getNumberOfStates(); s++) {
        if (!rel.isReachable(s) && s != rel.getDumpStateIndex()) {
          final StateProxy state = tr.getState(s);
          fail("Synthesised supervisor '" + aut.getName()
               + "' contains unreachable state '" + state.getName() + "'!");
        }
      }
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
      final CounterExampleProxy counterexample = verifier.getCounterExample();
      final File file = saveCounterExample(counterexample);
      fail("Synthesis result is not " + propname
           + "!\nCounterexample saved to " + file);
    }
  }

  private File saveCounterExample(final CounterExampleProxy counterexample)
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

  private void setNodeLimit(final SupervisorSynthesizer synthesizer)
  {
    final String prop = System.getProperty("waters.analysis.statelimit");
    if (prop != null) {
      final int limit = Integer.parseInt(prop);
      synthesizer.setNodeLimit(limit);
    }
  }


  //#########################################################################
  //# Inner Class LeastRestrictivenessKindTranslator
  private static class LeastRestrictivenessKindTranslator
    implements KindTranslator
  {
    //#######################################################################
    //# Constructor
    private LeastRestrictivenessKindTranslator(final Collection<? extends AutomatonProxy> computedSupervisors)
    {
      mComputedSupervisors =
        new THashSet<AutomatonProxy>(computedSupervisors);
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
  private final String filename;

  private PrintWriter mPrintWriter;
  private MonolithicSynthesizer mCliqueBasedSynthesizer;
  private MonolithicSynthesizer mSuWonhamSynthesizer;
  private ControllabilityChecker mControllabilityChecker;
  private LanguageInclusionChecker mLanguageInclusionChecker;
  private ConflictChecker mConflictChecker;
  private JAXBCounterExampleMarshaller mTraceMarshaller;
  private List<ParameterBindingProxy> mBindings;
}
