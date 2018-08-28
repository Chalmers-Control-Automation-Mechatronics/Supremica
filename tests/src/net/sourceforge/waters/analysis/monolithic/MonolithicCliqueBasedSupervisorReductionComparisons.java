package net.sourceforge.waters.analysis.monolithic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import net.sourceforge.waters.analysis.abstraction.AbstractSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy;
import net.sourceforge.waters.analysis.abstraction.SuWonhamSupervisorReductionTRSimplifier;
import net.sourceforge.waters.analysis.abstraction.TRSimplifierStatistics;
import net.sourceforge.waters.model.analysis.AbstractSupervisorSynthesizerTest;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ProductDESResult;
import net.sourceforge.waters.model.analysis.des.SupervisorSynthesizer;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


public class MonolithicCliqueBasedSupervisorReductionComparisons
  extends AbstractSupervisorSynthesizerTest
{
  public static void main(final String[] args) throws Exception
  {
    if (args.length > 0) {
      final long timeout = Long.parseLong(args[0]);
      int[] maxNumberOfCoversValues = null;
      if (args.length > 1) {
        maxNumberOfCoversValues = new int[args.length - 1];
        for (int i = 1; i < args.length; i++) {
          maxNumberOfCoversValues[i] = Integer.parseInt(args[i]);
        }
      }
      else {
        maxNumberOfCoversValues = new int[] {2, 5};
      }
      final HeuristicCoverStrategy[] strategies = HeuristicCoverStrategy.values();
      for (final int maxNumberOfCovers : maxNumberOfCoversValues) {
        for (final HeuristicCoverStrategy coverStrategy : strategies) {
          final String baseOutputName = coverStrategy.name() + "_" + maxNumberOfCovers + ".csv";
          final MonolithicCliqueBasedSupervisorReductionComparisons comparisons =
            new MonolithicCliqueBasedSupervisorReductionComparisons(baseOutputName, coverStrategy, maxNumberOfCovers, timeout);
          run(comparisons);
        }
      }

    } else {
      System.err.println("Incorrect number of parameters");
    }
  }

  private static void run(final MonolithicCliqueBasedSupervisorReductionComparisons comparisons) throws Exception {
    comparisons.setUp();
    comparisons.writeHeader();
    doTests(comparisons);
    comparisons.tearDown();
  }

  private static void doTests(final MonolithicCliqueBasedSupervisorReductionComparisons comparisons) throws Exception {
    comparisons.testAip0Sub1P0();
    comparisons.testBallProcess();
    comparisons.testBigFactory1();
    comparisons.testBigFactory2();
    comparisons.testBigFactory3();
    comparisons.testCatMouse();
    comparisons.testCatMouseUnsup1();

    comparisons.testCell();

    comparisons.testCoffeeMachine();

    comparisons.testDebounce();
    comparisons.testDosingUnit();

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
    comparisons.testPV35();

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
    comparisons.testTeleNetwork();
    comparisons.testTrafficlights();
    comparisons.testTransferLine1();

    comparisons.testCellSwitch();
    comparisons.testTransferLine2();

    comparisons.testCatMouseUnsup2();
    comparisons.testCT3();
    comparisons.testIMS();
    comparisons.testIPC();
    comparisons.testIPCcswitch();
    comparisons.testIPClswitch();
    comparisons.testIPCuswicth();
    comparisons.testRobotAssemblyCell();
    comparisons.testTransferLine3();
    comparisons.testTictactoe();
    comparisons.test2LinkAlt();
    comparisons.test2LinkAltBatch();
  }

  public MonolithicCliqueBasedSupervisorReductionComparisons()
  {
    this.mFilename = getOutputDirectory() + "/comparisons.csv";
    this.mCoverStrategy = HeuristicCoverStrategy.NONE;
    this.mTimeout = 30; //seconds
    this.mMaxNumberOfCovers = 0; //unused with non-heuristic
  }

  public MonolithicCliqueBasedSupervisorReductionComparisons(final String filename, final HeuristicCoverStrategy coverStrategy, final int maxNumberOfCovers, final long timeout)
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

  @Override
  protected SupervisorSynthesizer createSynthesizer(final ProductDESProxyFactory factory)
  {
    final MonolithicSynthesizer synthesizer =
      new MonolithicSynthesizer(factory);
    synthesizer.setSupervisorLocalizationEnabled(true);
    return synthesizer;
  }

  private AbstractSupervisorReductionTRSimplifier createSimplifier(final Class<? extends AbstractSupervisorReductionTRSimplifier> simplifierClazz) {
    if (simplifierClazz.equals(CliqueBasedSupervisorReductionTRSimplifier.class)) {
      return createCliqueBasedSimplifier();
    }
    else if (simplifierClazz.equals(SuWonhamSupervisorReductionTRSimplifier.class)) {
      return createSuWonhamSimplifier();
    }
    else {
      return null;
    }
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
                                      final AbstractSupervisorReductionTRSimplifier simplifier)
    throws EventNotFoundException
  {
    ((MonolithicSynthesizer)mSynthesizer).setSupervisorReductionSimplifier(simplifier);
    super.configureSynthesizer(des);
  }

  //#########################################################################
  //# Testing Procedure
  @Override
  protected ProductDESResult runSynthesizer(final ProductDESProxy des,
                                final boolean expect)
    throws Exception
  {
    return runSynthesizer(des, null, expect);
  }

  @Override
  protected ProductDESResult runSynthesizer(final ProductDESProxy des,
                                            final List<ParameterBindingProxy> bindings,
                                            final boolean expect)
    throws Exception
  {
    runSynthesizersAndPrint(des, bindings, expect);
    return null;
  }

  protected void runSynthesizersAndPrint(final ProductDESProxy des,
                                         final List<ParameterBindingProxy> bindings,
                                         final boolean expect)
    throws Exception
  {
    mBindings = bindings;

    final TRSimplifierStatistics cliqueBasedStats = runSynthesizerAndValidate(des, expect, CliqueBasedSupervisorReductionTRSimplifier.class);
    final TRSimplifierStatistics suWonhamStats = runSynthesizerAndValidate(des, expect, SuWonhamSupervisorReductionTRSimplifier.class);

    mPrintWriter.write(des.getName() + ",");
    if (cliqueBasedStats == null && suWonhamStats == null) {
      for (int i = 0; i < 6; i++) {
        mPrintWriter.write(mNoEntryValue + ",");
      }
      mPrintWriter.write(mNoEntryValue);
    } else if (cliqueBasedStats == null) {
     mPrintWriter.write(suWonhamStats.getInputStates() + ",");
     mPrintWriter.write(mNoEntryValue + "," + suWonhamStats.getChangedOutputStates() + "," + mNoEntryValue +",");
     mPrintWriter.write(mNoEntryValue + "," + suWonhamStats.getRunTime() + "," + mNoEntryValue);
    } else if (suWonhamStats == null) {
      mPrintWriter.write(cliqueBasedStats.getInputStates() + ",");
      mPrintWriter.write(cliqueBasedStats.getChangedOutputStates() + "," + mNoEntryValue + "," + mNoEntryValue + ",");
      mPrintWriter.write(cliqueBasedStats.getRunTime() + "," + mNoEntryValue + "," + mNoEntryValue);
    } else {
      final int swStates = suWonhamStats.getChangedOutputStates();
      final int cbStates = cliqueBasedStats.getChangedOutputStates();
      final long cbRuntime = cliqueBasedStats.getRunTime();
      final long swRuntime = suWonhamStats.getRunTime();

      mPrintWriter.write(suWonhamStats.getInputStates() + ",");
      mPrintWriter.write(cbStates + "," + swStates + "," + (cbStates - swStates) + ",");
      mPrintWriter.write(cbRuntime + "," + swRuntime + "," + (cbRuntime - swRuntime));
    }

    mPrintWriter.println();
    mPrintWriter.flush();
  }

  protected TRSimplifierStatistics runSynthesizerAndValidate(final ProductDESProxy des,
                                        final boolean expect,
                                        final Class<? extends AbstractSupervisorReductionTRSimplifier> simplifierClazz)
  {
    try {

      mSynthesizer = createSynthesizer(getProductDESProxyFactory());
      configureSynthesizer(des, createSimplifier(simplifierClazz));

      final ExecutorService singlePool = Executors.newSingleThreadExecutor();
      MonolithicSynthesisResult result = null;
      try {
        result = singlePool.submit(new Callable<MonolithicSynthesisResult>() {

          @Override
          public MonolithicSynthesisResult call() throws Exception
          {
            mSynthesizer.run();
            return ((MonolithicSynthesizer)mSynthesizer).getAnalysisResult();
          }
        }).get(mTimeout, TimeUnit.SECONDS);
      } catch (final TimeoutException ex) {
        System.err.println(des.getName() + " " + ex);
        mSynthesizer.requestAbort();
      } catch (final Exception ex) {
        System.err.println(ex);
      }
      finally {
        mSynthesizer.requestAbort();
        singlePool.shutdown();
      }

      if (result == null) { return null; }

      super.checkResult(des, result, expect);

      final List<TRSimplifierStatistics> allStatistics =
        result.getSimplifierStatistics();
      for (int i = 0; i < allStatistics.size(); i++) {
        final TRSimplifierStatistics statisticsForSimplifier =
          allStatistics.get(i);
        if (statisticsForSimplifier.getSimplifierClass()
          .equals(simplifierClazz)) {
          return statisticsForSimplifier;
        }
      }
    } catch (final Exception ex) {
      System.err.println(ex);
    }
    return null;
  }

  //#########################################################################
  //# Data Members
  private final CliqueBasedSupervisorReductionTRSimplifier.HeuristicCoverStrategy mCoverStrategy;
  private final int mMaxNumberOfCovers;
  private final long mTimeout; //seconds;
  private final String mFilename;
  private PrintWriter mPrintWriter;

  private final static String mNoEntryValue = "-";
}
