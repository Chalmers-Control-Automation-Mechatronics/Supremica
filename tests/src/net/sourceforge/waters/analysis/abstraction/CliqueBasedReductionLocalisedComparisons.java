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
import net.sourceforge.waters.model.analysis.AnalysisConfigurationException;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;


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

  private static void runAllComparisons(final long timeout, final int[] maxNumberOfCoversValues) throws Exception {
    final HeuristicCoverStrategy[] strategies = HeuristicCoverStrategy.values();
    for (final int maxNumberOfCovers : maxNumberOfCoversValues) {
      for (final HeuristicCoverStrategy coverStrategy : strategies) {
        final String baseOutputName = coverStrategy.name() + "_" + maxNumberOfCovers + ".csv";
        final CliqueBasedReductionLocalisedComparisons comparisons =
          new CliqueBasedReductionLocalisedComparisons(baseOutputName, coverStrategy, maxNumberOfCovers, timeout);
        run(comparisons);
      }
    }
  }

  private static void run(final CliqueBasedReductionLocalisedComparisons comparisons) throws Exception {
    comparisons.setUp();
    comparisons.writeHeader();
    doTests(comparisons);
    comparisons.tearDown();
  }

  private static void doTests(final CliqueBasedReductionLocalisedComparisons comparisons) throws Exception {
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
    comparisons.testIPCuswitch();
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
    comparisons.testTicTacToe();
    comparisons.testTeleNetwork();
    comparisons.testTrafficlights();
    comparisons.testTransferLine1();
    comparisons.testTransferLine2();
    comparisons.testTransferLine3();
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

  private void test2LinkAlt() throws Exception {
    runTransitionRelationSimplifier("2linkalt_5.wmod");
    runTransitionRelationSimplifier("2linkalt_6.wmod");
    runTransitionRelationSimplifier("2linkalt_7.wmod");
  }

  private void test2LinkAltBatch() throws Exception {
    runTransitionRelationSimplifier("2linkalt_batch_5.wmod");
    runTransitionRelationSimplifier("2linkalt_batch_6.wmod");
    runTransitionRelationSimplifier("2linkalt_batch_7.wmod");
  }

  private void testBigFactory1() throws Exception {
    runTransitionRelationSimplifier("big_factory_1_4.wmod");
    runTransitionRelationSimplifier("big_factory_1_5.wmod");
  }

  private void testBigFactory2() throws Exception {
    runTransitionRelationSimplifier("big_factory_2_10.wmod");
    runTransitionRelationSimplifier("big_factory_2_11.wmod");
  }

  private void testBigFactory3() throws Exception {
    runTransitionRelationSimplifier("big_factory_3_4.wmod");
    runTransitionRelationSimplifier("big_factory_3_5.wmod");
  }

  private void testCatMouse() throws Exception {
    runTransitionRelationSimplifier("cat_mouse_4.wmod");
    runTransitionRelationSimplifier("cat_mouse_12.wmod");
  }

  private void testCatMouseUnsup2() throws Exception {
    runTransitionRelationSimplifier("cat_mouse_unsup_2_530.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_2_531.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_2_536.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_2_540.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_2_546.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_2_547.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_2_552.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_2_556.wmod");
  }

  private void testCatMouseUnsup1() throws Exception {
    runTransitionRelationSimplifier("cat_mouse_unsup_1_530.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_1_531.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_1_536.wmod");
    runTransitionRelationSimplifier("cat_mouse_unsup_1_540.wmod");
  }

  private void testCell() throws Exception {
    runTransitionRelationSimplifier("cell_4.wmod");
    runTransitionRelationSimplifier("cell_6.wmod");
  }

  private void testCellSwitch() throws Exception {
    runTransitionRelationSimplifier("cell_switch_8.wmod");
    runTransitionRelationSimplifier("cell_switch_11.wmod");
    runTransitionRelationSimplifier("cell_switch_12.wmod");
  }

  private void testCertainUnsup() throws Exception {
    runTransitionRelationSimplifier("certain_unsup_100.wmod");
  }

  private void testCoffeeMachine() throws Exception {
    runTransitionRelationSimplifier("coffee_machine_5.wmod");
    runTransitionRelationSimplifier("coffee_machine_7.wmod");
  }

  private void testCT3() throws Exception {
    runTransitionRelationSimplifier("CT3_12.wmod");
    runTransitionRelationSimplifier("CT3_14.wmod");
    runTransitionRelationSimplifier("CT3_16.wmod");
    runTransitionRelationSimplifier("CT3_17.wmod");
    runTransitionRelationSimplifier("CT3_18.wmod");
  }

  private void testDosingUnit() throws Exception {
    runTransitionRelationSimplifier("dosing_unit_8.wmod");
    runTransitionRelationSimplifier("dosing_unit_9.wmod");
    runTransitionRelationSimplifier("dosing_unit_12.wmod");
  }

  private void testIMS() throws Exception {
    runTransitionRelationSimplifier("ims_14.wmod");
    runTransitionRelationSimplifier("ims_15.wmod");
    runTransitionRelationSimplifier("ims_16.wmod");
    runTransitionRelationSimplifier("ims_17.wmod");
  }

  private void testIPC() throws Exception {
    runTransitionRelationSimplifier("IPC_7.wmod");
    runTransitionRelationSimplifier("IPC_8.wmod");
    runTransitionRelationSimplifier("IPC_9.wmod");
    runTransitionRelationSimplifier("IPC_10.wmod");
    runTransitionRelationSimplifier("IPC_11.wmod");
    runTransitionRelationSimplifier("IPC_14.wmod");
    runTransitionRelationSimplifier("IPC_15.wmod");
    runTransitionRelationSimplifier("IPC_16.wmod");
  }

  private void testIPCcswitch() throws Exception {
    runTransitionRelationSimplifier("IPC_cswitch_8.wmod");
    runTransitionRelationSimplifier("IPC_cswitch_9.wmod");
    runTransitionRelationSimplifier("IPC_cswitch_10.wmod");
    runTransitionRelationSimplifier("IPC_cswitch_11.wmod");
    runTransitionRelationSimplifier("IPC_cswitch_12.wmod");
    runTransitionRelationSimplifier("IPC_cswitch_15.wmod");
    runTransitionRelationSimplifier("IPC_cswitch_16.wmod");
    runTransitionRelationSimplifier("IPC_cswitch_17.wmod");
  }

  private void testIPClswitch() throws Exception {
    runTransitionRelationSimplifier("IPC_lswitch_11.wmod");
    runTransitionRelationSimplifier("IPC_lswitch_13.wmod");
    runTransitionRelationSimplifier("IPC_lswitch_18.wmod");
    runTransitionRelationSimplifier("IPC_lswitch_19.wmod");
    runTransitionRelationSimplifier("IPC_lswitch_20.wmod");
  }

  private void testIPCuswitch() throws Exception {
    runTransitionRelationSimplifier("IPC_uswitch_10.wmod");
    runTransitionRelationSimplifier("IPC_uswitch_12.wmod");
    runTransitionRelationSimplifier("IPC_uswitch_17.wmod");
    runTransitionRelationSimplifier("IPC_uswitch_18.wmod");
  }

  private void testManWolf() throws Exception {
    runTransitionRelationSimplifier("man_wolf_5.wmod");
    runTransitionRelationSimplifier("man_wolf_6.wmod");
    runTransitionRelationSimplifier("man_wolf_7.wmod");
    runTransitionRelationSimplifier("man_wolf_8.wmod");
    runTransitionRelationSimplifier("man_wolf_11.wmod");
    runTransitionRelationSimplifier("man_wolf_12.wmod");
  }

  private void testManufacturingSystem() throws Exception {
    runTransitionRelationSimplifier("manufacturing_system_7.wmod");
  }

  private void testNoPlant1() throws Exception {
    runTransitionRelationSimplifier("no_plant_1_2.wmod");
    runTransitionRelationSimplifier("no_plant_1_3.wmod");
  }

  private void testNoPlant2() throws Exception {
    runTransitionRelationSimplifier("no_plant_2_2.wmod");
    runTransitionRelationSimplifier("no_plant_2_3.wmod");
  }

  private void testNoPlant3() throws Exception {
    runTransitionRelationSimplifier("no_plant_3_2.wmod");
  }

  private void testNoPlant4() throws Exception {
    runTransitionRelationSimplifier("no_plant_4_2.wmod");
    runTransitionRelationSimplifier("no_plant_4_3.wmod");
  }

  private void testOneStateSup() throws Exception {
    runTransitionRelationSimplifier("one_state_sup_3.wmod");
    runTransitionRelationSimplifier("one_state_sup_4.wmod");
  }

  private void testPathFinder() throws Exception {
    runTransitionRelationSimplifier("path_finder_4.wmod");
  }

  private void testPlantify() throws Exception {
    runTransitionRelationSimplifier("plantify_2.wmod");
  }

  private void testProfessorPen() throws Exception {
    runTransitionRelationSimplifier("professor_pen_3.wmod");
    runTransitionRelationSimplifier("professor_pen_4.wmod");
    runTransitionRelationSimplifier("professor_pen_5.wmod");
    runTransitionRelationSimplifier("professor_pen_6.wmod");
  }

  private void testRobotAssemblyCell() throws Exception {
    runTransitionRelationSimplifier("robot_assembly_cell_15.wmod");
    runTransitionRelationSimplifier("robot_assembly_cell_27.wmod");
  }

  private void testSajed() throws Exception {
    runTransitionRelationSimplifier("sajed_5.wmod");
  }

  private void testSelfloop1() throws Exception {
    runTransitionRelationSimplifier("selfloop_1_4.wmod");
  }

  private void testSelfloop2() throws Exception {
    runTransitionRelationSimplifier("selfloop_2_3.wmod");
  }

  private void testSmallFactory2() throws Exception {
    runTransitionRelationSimplifier("small_factory_2_7.wmod");
  }

  private void testSoeCont() throws Exception {
    runTransitionRelationSimplifier("soe_cont_3.wmod");
    runTransitionRelationSimplifier("soe_cont_4.wmod");
  }

  private void testSupRed3() throws Exception {
    runTransitionRelationSimplifier("sup_red_3_2.wmod");
  }

  private void testSupRed2() throws Exception {
    runTransitionRelationSimplifier("sup_red_2_3.wmod");
  }

  private void testSupRed1() throws Exception {
    runTransitionRelationSimplifier("sup_red_1_3.wmod");
  }

  private void testTankProcess() throws Exception {
    runTransitionRelationSimplifier("tank_process_6.wmod");
    runTransitionRelationSimplifier("tank_process_8.wmod");
    runTransitionRelationSimplifier("tank_process_10.wmod");
  }

  private void testTbedMinsync() throws Exception {
    runTransitionRelationSimplifier("tbed_minsync_123.wmod");
    runTransitionRelationSimplifier("tbed_minsync_124.wmod");
  }

  private void testTeleNetwork() throws Exception {
    runTransitionRelationSimplifier("tele_network_4.wmod");
    runTransitionRelationSimplifier("tele_network_5.wmod");
  }

  private void testTicTacToe() throws Exception {
    runTransitionRelationSimplifier("tictactoe_27.wmod");
    runTransitionRelationSimplifier("tictactoe_28.wmod");
    runTransitionRelationSimplifier("tictactoe_29.wmod");
    runTransitionRelationSimplifier("tictactoe_30.wmod");
    runTransitionRelationSimplifier("tictactoe_31.wmod");
    runTransitionRelationSimplifier("tictactoe_32.wmod");
    runTransitionRelationSimplifier("tictactoe_33.wmod");
    runTransitionRelationSimplifier("tictactoe_34.wmod");
    runTransitionRelationSimplifier("tictactoe_35.wmod");
  }

  private void testTrafficlights() throws Exception {
    runTransitionRelationSimplifier("traffic_lights_7.wmod");
    runTransitionRelationSimplifier("traffic_lights_8.wmod");
  }

  private void testTransferLine1() throws Exception {
    runTransitionRelationSimplifier("transfer_line_1_5.wmod");
    runTransitionRelationSimplifier("transfer_line_1_6.wmod");
  }

  private void testTransferLine2() throws Exception {
    runTransitionRelationSimplifier("transfer_line_2_8.wmod");
    runTransitionRelationSimplifier("transfer_line_2_9.wmod");
    runTransitionRelationSimplifier("transfer_line_2_10.wmod");
    runTransitionRelationSimplifier("transfer_line_2_11.wmod");
  }

  private void testTransferLine3() throws Exception {
    runTransitionRelationSimplifier("transfer_line_3_11.wmod");
    runTransitionRelationSimplifier("transfer_line_3_12.wmod");
    runTransitionRelationSimplifier("transfer_line_3_13.wmod");
    runTransitionRelationSimplifier("transfer_line_3_14.wmod");
    runTransitionRelationSimplifier("transfer_line_3_15.wmod");
    runTransitionRelationSimplifier("transfer_line_3_16.wmod");
  }

  @Override
  protected TransitionRelationSimplifier createTransitionRelationSimplifier()
    throws AnalysisException
  {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  protected void runTransitionRelationSimplifier(final ProductDESProxy des)
    throws Exception
  {
    mSimplifier = createCliqueBasedSimplifier(getRelationFromAutomaton(des));
    final TRSimplifierStatistics cliqueBasedStats = applySimplifier(mSimplifier);
    mSimplifier = createSuWonhamSimplifier(getRelationFromAutomaton(des));
    final TRSimplifierStatistics suWonhamStats = applySimplifier(mSimplifier);

    mPrintWriter.write(des.getName() + ",");
    if (cliqueBasedStats == null && suWonhamStats == null) {
      for (int i = 0; i < 6; i++) {
        mPrintWriter.write(NO_ENTRY + ",");
      }
      mPrintWriter.write(NO_ENTRY);
    } else if (cliqueBasedStats == null) {
     mPrintWriter.write(suWonhamStats.getInputStates() + ",");
     mPrintWriter.write(NO_ENTRY + "," + suWonhamStats.getOutputStates() + "," + NO_ENTRY +",");
     mPrintWriter.write(NO_ENTRY + "," + suWonhamStats.getRunTime() + "," + NO_ENTRY);
    } else if (suWonhamStats == null) {
      mPrintWriter.write(cliqueBasedStats.getInputStates() + ",");
      mPrintWriter.write(cliqueBasedStats.getOutputStates() + "," + NO_ENTRY + "," + NO_ENTRY + ",");
      mPrintWriter.write(cliqueBasedStats.getRunTime() + "," + NO_ENTRY + "," + NO_ENTRY);
    } else {
      final int swStates = suWonhamStats.getOutputStates();
      final int cbStates = cliqueBasedStats.getOutputStates();
      final long cbRuntime = cliqueBasedStats.getRunTime();
      final long swRuntime = suWonhamStats.getRunTime();

      mPrintWriter.write(suWonhamStats.getInputStates() + ",");
      mPrintWriter.write(cbStates + "," + swStates + "," + (cbStates - swStates) + ",");
      mPrintWriter.write(cbRuntime + "," + swRuntime + "," + (cbRuntime - swRuntime));
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

  private ListBufferTransitionRelation getRelationFromAutomaton(final ProductDESProxy des)
    throws AnalysisConfigurationException, OverflowException
  {
    final AutomatonProxy aut = getAutomaton(des, "");
    if (aut == null) {
      throw new AnalysisConfigurationException("DES contains "
                                               + des.getAutomata().size()
                                               + " automata");
    }
    final EventEncoding eventEnc = createEventEncoding(des, aut);
    final StateEncoding inputStateEnc = new StateEncoding(aut);
    final StateProxy dumpState = getState(aut, DUMP);
    ListBufferTransitionRelation rel;
    if (dumpState == null) {
      rel =
        new ListBufferTransitionRelation(aut, eventEnc, inputStateEnc,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    } else {
      rel =
        new ListBufferTransitionRelation(aut, eventEnc, inputStateEnc,
                                         dumpState,
                                         ListBufferTransitionRelation.CONFIG_SUCCESSORS);
    }
    rel.checkReachability();
    return rel;
  }

  private TRSimplifierStatistics applySimplifier(final TransitionRelationSimplifier simplifier)
  {
    final TRSimplifierStatistics simplifierStatistics = new TRSimplifierStatistics(simplifier, true, true, false);
    simplifierStatistics.recordStart(simplifier.getTransitionRelation());
    final ExecutorService singlePool = Executors.newSingleThreadExecutor();
    final long startTime = System.currentTimeMillis();
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
    final long finishTIme = System.currentTimeMillis();

    if (isReduced.isPresent()) {
      simplifierStatistics.recordFinish(simplifier.getTransitionRelation(), isReduced.get());
      simplifierStatistics.setRunTime(finishTIme - startTime);
      return simplifierStatistics;
    } else {
      return null;
    }
  }

  private CliqueBasedSupervisorReductionTRSimplifier createCliqueBasedSimplifier(final ListBufferTransitionRelation rel)
  {
    final CliqueBasedSupervisorReductionTRSimplifier simplifier =
      new CliqueBasedSupervisorReductionTRSimplifier(rel);
    simplifier.setHeuristicCoverStrategy(mCoverStrategy);
    simplifier.setMaxHeuristicCovers(mMaxNumberOfCovers);
    return simplifier;
  }

  private SuWonhamSupervisorReductionTRSimplifier createSuWonhamSimplifier(final ListBufferTransitionRelation rel)
  {
    final SuWonhamSupervisorReductionTRSimplifier simplifier =
      new SuWonhamSupervisorReductionTRSimplifier(rel);
    simplifier.setExperimentalMode(true);
    return simplifier;
  }

  private void runTransitionRelationSimplifier(final String desName) throws Exception {
    runTransitionRelationSimplifier(GROUP, SUBDIR, desName);
  }

  //#########################################################################
  //# Data Members
  private final CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy mCoverStrategy;
  private final int mMaxNumberOfCovers;
  private final long mTimeout; //seconds;
  private final String mFilename;
  private PrintWriter mPrintWriter;

  private static final String NO_ENTRY = "-";
  private static final String GROUP = "tests";
  private static final String SUBDIR = "supervisor_reduction";
}
