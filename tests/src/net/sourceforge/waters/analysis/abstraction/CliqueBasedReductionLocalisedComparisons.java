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

  private void test2LinkAlt() throws Exception
  {
    runTransitionRelationSimplifier("2linkalt.wmod");
  }

  private void test2LinkAltBatch() throws Exception
  {
    runTransitionRelationSimplifier("2linkalt_batch.wmod");
  }

  private void testBigFactory1() throws Exception
  {
    runTransitionRelationSimplifier("big_factory_1.wmod");
  }

  private void testBigFactory2() throws Exception
  {
    runTransitionRelationSimplifier("big_factory_2.wmod");
  }

  private void testBigFactory3() throws Exception
  {
    runTransitionRelationSimplifier("big_factory_3.wmod");
  }

  private void testCatMouse() throws Exception
  {
    runTransitionRelationSimplifier("cat_mouse.wmod");
  }

  private void testCatMouseUnsup2() throws Exception
  {
    runTransitionRelationSimplifier("cat_mouse_unsup_2.wmod");
  }

  private void testCatMouseUnsup1() throws Exception
  {
    runTransitionRelationSimplifier("cat_mouse_unsup_1.wmod");
  }

  private void testCell() throws Exception
  {
    runTransitionRelationSimplifier("cell.wmod");
  }

  private void testCellSwitch() throws Exception
  {
    runTransitionRelationSimplifier("cell_switch.wmod");
  }

  private void testCertainUnsup() throws Exception
  {
    runTransitionRelationSimplifier("certain_unsup.wmod");
  }

  private void testCoffeeMachine() throws Exception
  {
    runTransitionRelationSimplifier("coffee_machine.wmod");
  }

  private void testCT3() throws Exception
  {
    runTransitionRelationSimplifier("CT3.wmod");
  }

  private void testDosingUnit() throws Exception
  {
    runTransitionRelationSimplifier("dosing_unit.wmod");
  }

  private void testIMS() throws Exception
  {
    runTransitionRelationSimplifier("ims.wmod");
  }

  private void testIPC() throws Exception
  {
    runTransitionRelationSimplifier("IPC.wmod");
  }

  private void testIPCcswitch() throws Exception
  {
    runTransitionRelationSimplifier("IPC_cswitch.wmod");
  }

  private void testIPClswitch() throws Exception
  {
    runTransitionRelationSimplifier("IPC_lswitch.wmod");
  }

  private void testIPCuswitch() throws Exception
  {
    runTransitionRelationSimplifier("IPC_uswitch.wmod");
  }

  private void testManWolf() throws Exception
  {
    runTransitionRelationSimplifier("man_wolf.wmod");
  }

  private void testManufacturingSystem() throws Exception
  {
    runTransitionRelationSimplifier("manufacturing_system.wmod");
  }

  private void testNoPlant1() throws Exception
  {
    runTransitionRelationSimplifier("no_plant_1.wmod");
  }

  private void testNoPlant2() throws Exception
  {
    runTransitionRelationSimplifier("no_plant_2.wmod");
  }

  private void testNoPlant3() throws Exception
  {
    runTransitionRelationSimplifier("no_plant_3.wmod");
  }

  private void testNoPlant4() throws Exception
  {
    runTransitionRelationSimplifier("no_plant_4.wmod");
  }

  private void testOneStateSup() throws Exception
  {
    runTransitionRelationSimplifier("one_state_sup.wmod");
  }

  private void testPathFinder() throws Exception
  {
    runTransitionRelationSimplifier("path_finder.wmod");
  }

  private void testPlantify() throws Exception
  {
    runTransitionRelationSimplifier("plantify.wmod");
  }

  private void testProfessorPen() throws Exception
  {
    runTransitionRelationSimplifier("professor_pen.wmod");
  }

  private void testRobotAssemblyCell() throws Exception
  {
    runTransitionRelationSimplifier("robot_assembly_cell.wmod");
  }

  private void testSajed() throws Exception
  {
    runTransitionRelationSimplifier("sajed.wmod");
  }

  private void testSelfloop1() throws Exception
  {
    runTransitionRelationSimplifier("selfloop_1.wmod");
  }

  private void testSelfloop2() throws Exception
  {
    runTransitionRelationSimplifier("selfloop_2.wmod");
  }

  private void testSmallFactory2() throws Exception
  {
    runTransitionRelationSimplifier("small_factory_2.wmod");
  }

  private void testSoeCont() throws Exception
  {
    runTransitionRelationSimplifier("soe_cont.wmod");
  }

  private void testSupRed3() throws Exception
  {
    runTransitionRelationSimplifier("sup_red_3.wmod");
  }

  private void testSupRed2() throws Exception
  {
    runTransitionRelationSimplifier("sup_red_2.wmod");
  }

  private void testSupRed1() throws Exception
  {
    runTransitionRelationSimplifier("sup_red_1.wmod");
  }

  private void testTankProcess() throws Exception
  {
    runTransitionRelationSimplifier("tank_process.wmod");
  }

  private void testTbedMinsync() throws Exception
  {
    runTransitionRelationSimplifier("tbed_minsync.wmod");
  }

  private void testTeleNetwork() throws Exception
  {
    runTransitionRelationSimplifier("tele_network.wmod");
  }

  private void testTicTacToe() throws Exception
  {
    runTransitionRelationSimplifier("tictactoe.wmod");
  }

  private void testTrafficlights() throws Exception
  {
    runTransitionRelationSimplifier("traffic_lights.wmod");
  }

  private void testTransferLine1() throws Exception
  {
    runTransitionRelationSimplifier("transfer_line_1.wmod");
  }

  private void testTransferLine2() throws Exception
  {
    runTransitionRelationSimplifier("transfer_line_2.wmod");
  }

  private void testTransferLine3() throws Exception
  {
    runTransitionRelationSimplifier("transfer_line_3.wmod");
  }

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
    System.out.println("Running on " + des.getName());
    final ListBufferTransitionRelation relation =
      getRelationFromAutomaton(des);
    for (int e = EventEncoding.NONTAU; e < relation
      .getNumberOfProperEvents(); e++) {
      final byte status = relation.getProperEventStatus(e);
      if (EventStatus.isControllableEvent(status)
          && isEverDisabledEvent(relation, e)) {
        AbstractSupervisorReductionTRSimplifier cliqueBasedSimplifier =
          createCliqueBasedSimplifier();
        cliqueBasedSimplifier
          .setTransitionRelation(new ListBufferTransitionRelation(relation,
                                                                  cliqueBasedSimplifier
                                                                    .getPreferredInputConfiguration()));
        cliqueBasedSimplifier.setSupervisedEvent(e);
        final TRSimplifierStatistics cliqueBasedStats =
          applySimplifier(cliqueBasedSimplifier);

        cliqueBasedSimplifier = null;

        AbstractSupervisorReductionTRSimplifier suWonhamSimplifier =
          createSuWonhamSimplifier();
        suWonhamSimplifier
          .setTransitionRelation(new ListBufferTransitionRelation(relation,
                                                                  suWonhamSimplifier
                                                                    .getPreferredInputConfiguration()));
        suWonhamSimplifier.setSupervisedEvent(e);
        final TRSimplifierStatistics suWonhamStats =
          applySimplifier(suWonhamSimplifier);

        suWonhamSimplifier = null;

        writeResults(des.getName() + ":" + e, cliqueBasedStats,
                     suWonhamStats);
      }
    }
  }

  private TRSimplifierStatistics applySimplifier(final TransitionRelationSimplifier simplifier)
  {
    simplifier.createStatistics();
    final ExecutorService singlePool = Executors.newSingleThreadExecutor();
    Optional<Boolean> isReduced = Optional.empty();
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
      simplifier.requestAbort();
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
      final TransitionIterator iter =
        rel.createAllTransitionsModifyingIterator();
      while (iter.advance()) {
        if (iter.getCurrentToState() == oldDumpState) {
          iter.setCurrentToState(newDumpState);
        }
      }
      rel.setReachable(oldDumpState, false);
      rel.setReachable(newDumpState, true);
    }
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

  private void runTransitionRelationSimplifier(final String name)
    throws Exception
  {
    runTransitionRelationSimplifier(GROUP, SUBDIR, name);
  }

  //#########################################################################
  //# Data Members
  private final CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy mCoverStrategy;
  private final int mMaxNumberOfCovers;
  private final long mTimeout; //seconds;
  private final String mFilename;
  private PrintWriter mPrintWriter;

  private static final String GROUP = "tests";
  private static final String SUBDIR = "supervisor_reduction";
  private static final String NO_ENTRY = "-";
}
