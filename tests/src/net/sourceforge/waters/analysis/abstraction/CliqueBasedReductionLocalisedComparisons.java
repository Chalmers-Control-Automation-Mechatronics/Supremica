package net.sourceforge.waters.analysis.abstraction;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.analysis.tr.StateEncoding;
import net.sourceforge.waters.analysis.tr.TransitionIterator;
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


public class CliqueBasedReductionLocalisedComparisons
  extends AbstractTRSimplifierTest
{

  public static void main(final String[] args) throws Exception
  {
    int[] maxNumberOfCoversValues = new int[] {2, 5};
    long timeout = 30; //s
    if (args.length > 0) {
      timeout = Long.parseLong(args[0]);
      if (args.length > 1) {
        maxNumberOfCoversValues = new int[args.length - 1];
        for (int i = 1; i < args.length; i++) {
          maxNumberOfCoversValues[i] = Integer.parseInt(args[i]);
        }
      }
    }
    runAllComparisons(timeout, maxNumberOfCoversValues);
  }

  private static void runAllComparisons(final long timeout,
                                        final int[] maxNumberOfCoversValues)
    throws Exception
  {
    final HeuristicCoverStrategy[] strategies =
      HeuristicCoverStrategy.values();
    for (final int maxNumberOfCovers : maxNumberOfCoversValues) {
      for (final HeuristicCoverStrategy coverStrategy : strategies) {
        final String baseOutputName =
          coverStrategy.name() + "_" + maxNumberOfCovers + ".csv";
        final CliqueBasedReductionLocalisedComparisons comparisons =
          new CliqueBasedReductionLocalisedComparisons(baseOutputName,
                                                       coverStrategy,
                                                       maxNumberOfCovers,
                                                       timeout);
        run(comparisons);
      }
    }
  }

  private static void run(final CliqueBasedReductionLocalisedComparisons comparisons)
    throws Exception
  {
    comparisons.setUp();
    comparisons.writeHeader();
    doTests(comparisons);
    comparisons.tearDown();
  }

  private static void doTests(final CliqueBasedReductionLocalisedComparisons comparisons)
    throws Exception
  {
    comparisons.test2LinkAlt();
    comparisons.test2LinkAltBatch();
    comparisons.testBigFactory1();
    comparisons.testBigFactory2();
    comparisons.testBigFactory3();
    comparisons.testCatMouse();
    comparisons.testCatMouseUnsup2();
    comparisons.testCatMouseUnsup1();
    comparisons.testCell();
    comparisons.testCellSwitch();
    comparisons.testCertainUnsup();
    comparisons.testCT3();
    comparisons.testCoffeeMachine();
    comparisons.testDosingUnit();
    comparisons.testIMS();
    comparisons.testIPC();
    comparisons.testIPCcswitch();
    comparisons.testIPClswitch();
    comparisons.testIPCuswicth();
    comparisons.testManufacturingSystem();
    comparisons.testManWolf();
    comparisons.testNoPlant1();
    comparisons.testNoPlant2();
    comparisons.testNoPlant3();
    comparisons.testNoPlant4();
    comparisons.testOneStateSup();
    comparisons.testPathFinder();
    comparisons.testPlantify();
    comparisons.testProfessorPen();
    comparisons.testRobotAssemblyCell();
    comparisons.testSajed();
    comparisons.testSelfloop1();
    comparisons.testSelfloop2();
    comparisons.testSmallFactory2();
    comparisons.testSoeCont();
    comparisons.testSupRed1();
    comparisons.testSupRed2();
    comparisons.testSupRed3();
    comparisons.testTankProcess();
    comparisons.testTbedMinsync();
    comparisons.testTictactoe();
    comparisons.testTeleNetwork();
    comparisons.testTrafficlights();
    comparisons.testTransferLine1();
    comparisons.testTransferLine2();
    //comparisons.testTransferLine3();
  }

  public CliqueBasedReductionLocalisedComparisons()
  {
    this.mFilename = getOutputDirectory() + "/comparisons.csv";
    this.mCoverStrategy = HeuristicCoverStrategy.NONE;
    this.mTimeout = 30; //seconds
    this.mMaxNumberOfCovers = 0; //unused with non-heuristic
  }

  public CliqueBasedReductionLocalisedComparisons(final String filename,
                                                  final HeuristicCoverStrategy coverStrategy,
                                                  final int maxNumberOfCovers,
                                                  final long timeout)
  {
    this.mFilename = getOutputDirectory() + "/" + filename;
    this.mCoverStrategy = coverStrategy;
    this.mTimeout = timeout;
    this.mMaxNumberOfCovers = maxNumberOfCovers;
  }

  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mPrintWriter =
      new PrintWriter(new BufferedWriter(new FileWriter(new File(mFilename))));
  }

  @Override
  protected void tearDown() throws Exception
  {
    mPrintWriter.close();
    mPrintWriter = null;
    super.tearDown();
  }

  //#########################################################################
  //# Test Cases --- synthesis
  public void testAip0Sub1P0() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "aip0sub1p0.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testAGVMF() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "agv_mf.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testBallProcess() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "ball_Process.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testBigFactory1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "big_factory_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testBigFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "big_factory_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testBigFactory3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "big_factory_3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCatMouse() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cat_mouse.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCatMouseUnsup1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cat_mouse_unsup1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCatMouseUnsup2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cat_mouse_unsup2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCell() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cell.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCellSwitch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "cell_switch.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCertainUnsup() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "certainUnsup.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCoffeeMachine() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "coffee_machine.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testCT3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "CT3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testDebounce() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "debounce.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testDosingUnit() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "dosing_unit.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testFalko() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "falko.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testFTechnik() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "incremental_suite", "ftechnik.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testIMS() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "ims", "ims_uncont.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testIPC() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testIPCcswitch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC_cswitch.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testIPClswitch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC_lswitch.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testIPCuswicth() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "IPC_uswitch.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testManufacturingSystem() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "manufacturing_system.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testManWolf() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "man_wolf.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testNoPlant1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testNoPlant2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testNoPlant3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testNoPlant4() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "no_plant4.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testOneStateSup() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "one_state_sup.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testParrow() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "parrow.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testPathFinder() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "path_finder.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testPlantify() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "plantify.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testProductionSystem() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "production_system.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testProfessorPen() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "professor_pen.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testPV35() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "pv35.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testRobotAssemblyCell() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "robot_assembly_cell.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSajed() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "sajed.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloop1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "selfloop1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSelfloop2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "selfloop2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSimpleManufacturingSystem() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis",
                     "simple_manufacturing_system.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSmallFactory2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "small_factory_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSoeCont() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "soe_cont.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSupRed1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "supred1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSupRed2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "supred2.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testSupRed3() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "supred3.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testTankProcess() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tank_process.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testTbedMinsync() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tbed_minsync.wmod");
    runTransitionRelationSimplifier(des);
  }

  //  public void testAGV() throws Exception
  //  {
  //    final ProductDESProxy des =
  //      getCompiledDES("tests", "incremental_suite", "agv.wmod");
  //    runTransitionRelationSimplifier(des, true);
  //  }

  public void testTeleNetwork() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tele_network.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testTrafficlights() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "trafficlights.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testTransferLine1() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "transferline_1.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testTransferLine2() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "transferline_2.wmod");
    runTransitionRelationSimplifier(des);
  }

  /*
   * public void testTransferLine3() throws Exception { checkTransferline(3);
   * }
   */

  public void testTictactoe() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "tictactoe.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testThreeRobot() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "three_robot.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void testZeroSup() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "test_zero_sup.wmod");
    runTransitionRelationSimplifier(des);
  }

  public void test2LinkAlt() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "2linkalt.wmod");
    runTransitionRelationSimplifier(des);
  }

  //#########################################################################
  //# Test Cases --- BIG
  /*
   * Too slow (20min) for supervisor reduction public void testAip0Sub1P1()
   * throws Exception { final ProductDESProxy des = getCompiledDES("tests",
   * "synthesis", "aip0sub1p1.wmod"); runTransitionRelationSimplifier(des,
   * true); }
   */

  public void test2LinkAltBatch() throws Exception
  {
    final ProductDESProxy des =
      getCompiledDES("tests", "synthesis", "2linkalt_batch.wmod");
    runTransitionRelationSimplifier(des);
  }

  /*
   * This one is too big for monolithic synthesis. public void
   * testKoordWspSynth() throws Exception { final ProductDESProxy des =
   * getCompiledDES("tests", "synthesis", "koordwsp_synth.wmod");
   * runTransitionRelationSimplifier(des, true); }
   */

  //#########################################################################
  //# Parametrised tests
  //TODO: add support for bindings
  /*
   * private void checkTransferline(final int n) throws Exception { final
   * ParameterBindingProxy binding = createBinding("N", n); final
   * List<ParameterBindingProxy> bindings =
   * Collections.singletonList(binding); final ProductDESProxy des =
   * getCompiledDES(bindings, "tests", "synthesis", "transferline_N.wmod");
   * runSynthesizer(des, bindings, true); }
   */

  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
    throws AnalysisException
  {
    return null;
  }

  @Override
  protected void runTransitionRelationSimplifier(final ProductDESProxy des)
    throws Exception
  {
    final ListBufferTransitionRelation relation =
      getRelationFromAutomaton(des);
    for (int e = EventEncoding.NONTAU; e < relation
      .getNumberOfProperEvents(); e++) {
      final byte status = relation.getProperEventStatus(e);
      if (EventStatus.isControllableEvent(status)
          && isEverDisabledEvent(relation, e)) {
        final AbstractSupervisorReductionTRSimplifier cliqueBasedSimplifier =
          createCliqueBasedSimplifier();
        cliqueBasedSimplifier
          .setTransitionRelation(new ListBufferTransitionRelation(relation,
                                                                  cliqueBasedSimplifier
                                                                    .getPreferredInputConfiguration()));
        cliqueBasedSimplifier.setSupervisedEvent(e);
        final TRSimplifierStatistics cliqueBasedStats =
          applySimplifier(cliqueBasedSimplifier);

        final AbstractSupervisorReductionTRSimplifier suWonhamSimplifier =
          createSuWonhamSimplifier();
        suWonhamSimplifier
          .setTransitionRelation(new ListBufferTransitionRelation(relation,
                                                                  suWonhamSimplifier
                                                                    .getPreferredInputConfiguration()));
        suWonhamSimplifier.setSupervisedEvent(e);
        final TRSimplifierStatistics suWonhamStats =
          applySimplifier(suWonhamSimplifier);

        writeResults(des.getName() + "_" + e, cliqueBasedStats,
                     suWonhamStats);
      }
    }
  }

  private TRSimplifierStatistics applySimplifier(final TransitionRelationSimplifier simplifier)
  {
    simplifier.createStatistics();
    final ExecutorService singlePool = Executors.newSingleThreadExecutor();
    Optional<Boolean> isReduced = null;
    try {
      isReduced = singlePool.submit(new Callable<Optional<Boolean>>() {

        @Override
        public Optional<Boolean> call() throws Exception
        {
          return Optional.of(simplifier.run());
        }
      }).get(mTimeout, TimeUnit.SECONDS);
    } catch (final TimeoutException ex) {
      System.err.println("Timeout");
      simplifier.requestAbort();
    } catch (final Exception ex) {
      System.err.println(ex);
    } finally {
      singlePool.shutdown();
    }

    if (isReduced.isPresent()) {
      return simplifier.getStatistics();
    } else {
      return null;
    }
  }

  private void writeResults(final String name,
                            final TRSimplifierStatistics cliqueBasedStats,
                            final TRSimplifierStatistics suWonhamStats)
  {
    mPrintWriter.write(name + ",");
    if (cliqueBasedStats == null && suWonhamStats == null) {
      for (int i = 0; i < 6; i++) {
        mPrintWriter.write(NO_ENTRY + ",");
      }
      mPrintWriter.write(NO_ENTRY);
    } else if (cliqueBasedStats == null) {
      mPrintWriter.write(suWonhamStats.getInputStates() + ",");
      mPrintWriter.write(NO_ENTRY + "," + suWonhamStats.getOutputStates()
                         + "," + NO_ENTRY + ",");
      mPrintWriter
        .write(NO_ENTRY + "," + suWonhamStats.getRunTime() + "," + NO_ENTRY);
    } else if (suWonhamStats == null) {
      mPrintWriter.write(cliqueBasedStats.getInputStates() + ",");
      mPrintWriter.write(cliqueBasedStats.getOutputStates() + "," + NO_ENTRY
                         + "," + NO_ENTRY + ",");
      mPrintWriter.write(cliqueBasedStats.getRunTime() + "," + NO_ENTRY + ","
                         + NO_ENTRY);
    } else {
      final int swStates = suWonhamStats.getOutputStates();
      final int cbStates = cliqueBasedStats.getOutputStates();
      final long cbRuntime = cliqueBasedStats.getRunTime();
      final long swRuntime = suWonhamStats.getRunTime();

      mPrintWriter.write(suWonhamStats.getInputStates() + ",");
      mPrintWriter
        .write(cbStates + "," + swStates + "," + (cbStates - swStates) + ",");
      mPrintWriter
        .write(cbRuntime + "," + swRuntime + "," + (cbRuntime - swRuntime));
    }

    mPrintWriter.println();
    mPrintWriter.flush();
  }

  @Override
  protected EventEncoding createEventEncoding(final ProductDESProxy des,
                                              final AutomatonProxy aut)
    throws OverflowException
  {
    final EventEncoding encoding = super.createEventEncoding(des, aut);
    encoding.sortProperEvents((byte) ~EventStatus.STATUS_LOCAL,
                              (byte) ~EventStatus.STATUS_CONTROLLABLE);
    return encoding;
  }

  @Override
  protected AutomatonProxy getAutomaton(final ProductDESProxy des,
                                        final String name)
  {
    final Set<AutomatonProxy> automata = des.getAutomata();
    if (automata.size() == 1) {
      return automata.iterator().next();
    } else {
      return null;
    }
  }

  public boolean isEverDisabledEvent(final ListBufferTransitionRelation relation,
                                     final int e)
  {
    final int numStates = relation.getNumberOfStates();
    final TransitionIterator iter = relation.createAnyReadOnlyIterator();
    final int dump = relation.getDumpStateIndex();
    for (int s = 0; s < numStates; s++) {
      if (relation.isReachable(s)) {
        iter.reset(s, e);
        if (iter.advance() && iter.getCurrentTargetState() == dump) {
          return true;
        }
      }
    }
    return false;
  }

  private ListBufferTransitionRelation getRelationFromAutomaton(final ProductDESProxy des)
    throws AnalysisException
  {
    final AutomatonProxy aut = getAutomaton(des, "");
    if (aut == null) {
      throw new AnalysisConfigurationException("DES contains "
                                               + des.getAutomata().size()
                                               + " automata");
    }
    final EventEncoding eventEnc = createEventEncoding(des, aut);
    final StateEncoding inputStateEnc = new StateEncoding(aut);
    final ListBufferTransitionRelation rel =
      new ListBufferTransitionRelation(aut, eventEnc, inputStateEnc,
                                       ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    rel.checkReachability();
    final int newDumpState = rel.getDumpStateIndex();
    int oldDumpState = -1;
    final TransitionIterator successorIterator =
      rel.createSuccessorsReadOnlyIterator();
    for (int s = 0; s < rel.getNumberOfStates(); s++) {
      if (s == newDumpState || !rel.isReachable(s)) {
        continue;
      }
      successorIterator.resetState(s);
      if (!successorIterator.advance()) {
        oldDumpState = s;
        break;
      }
    }
    if (oldDumpState == -1) {
      throw new AnalysisException("No dump state found");
    } else {
      final TransitionIterator predecessorIterator =
        rel.createPredecessorsReadOnlyIterator(oldDumpState);
      while (predecessorIterator.advance()) {
        rel.addTransition(predecessorIterator.getCurrentSourceState(),
                          predecessorIterator.getCurrentEvent(),
                          newDumpState);
      }
    }
    rel.setReachable(oldDumpState, false);
    return rel;
  }

  private CliqueBasedSupervisorReductionTRSimplifier createCliqueBasedSimplifier()
  {
    final CliqueBasedSupervisorReductionTRSimplifier simplifier =
      new CliqueBasedSupervisorReductionTRSimplifier();
    simplifier.setHeuristicCoverStrategy(mCoverStrategy);
    simplifier.setMaxHeuristicCovers(mMaxNumberOfCovers);
    return simplifier;
  }

  private SuWonhamSupervisorReductionTRSimplifier createSuWonhamSimplifier()
  {
    final SuWonhamSupervisorReductionTRSimplifier simplifier =
      new SuWonhamSupervisorReductionTRSimplifier();
    simplifier.setExperimentalMode(true);
    return simplifier;
  }

  private void writeHeader()
  {
    mPrintWriter.write("DES name,");
    mPrintWriter.write("input states,");

    mPrintWriter.write("CB output states,");
    mPrintWriter.write("SW output states,");
    mPrintWriter.write("(CB - SW) states,");

    mPrintWriter.write("CB runtime,");
    mPrintWriter.write("SW runtime,");
    mPrintWriter.write("(CB - SW) runtime");

    mPrintWriter.println();
    mPrintWriter.flush();
  }

  //#########################################################################
  //# Data Members
  private final CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy mCoverStrategy;
  private final int mMaxNumberOfCovers;
  private final long mTimeout; //seconds;
  private final String mFilename;
  private PrintWriter mPrintWriter;

  private static final String NO_ENTRY = "-";
}
