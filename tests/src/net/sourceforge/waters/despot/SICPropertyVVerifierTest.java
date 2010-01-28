package net.sourceforge.waters.despot;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.model.analysis.AbstractConflictCheckerTest;
import net.sourceforge.waters.model.analysis.ModelVerifier;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.TraceProxy;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class SICPropertyVVerifierTest extends AbstractConflictCheckerTest
{

  public void testConflictChecker_parManEg_I_mfb_lowlevel() throws Exception
  {
    testConflictChecker("tests", "hisc", "parManEg_I_mfb_lowlevel", true);
  }

  public void testConflictChecker_rhone_subsystem1_ld_failsic5()
      throws Exception
  {
    testConflictChecker("tests", "hisc", "rhone_subsystem1_ld_failsic5", false);
  }

  public void testConflictChecker_rhone_subsystem1_ld() throws Exception
  {
    testConflictChecker("tests", "hisc", "rhone_subsystem1_ld", true);
  }

  // SimpleManufacturingExample
  public void testConflictChecker_Manuf_Cells() throws Exception
  {
    testConflictChecker("despot", "simpleManufacturingExample", "Manuf-Cells",
                        true);
  }

  // testHISC
  public void testConflictChecker_hisc0_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc0_low1", true);
  }

  public void testConflictChecker_hisc0_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc0_low2", true);
  }

  // testHISC1
  public void testConflictChecker_hisc1_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc1_low1", true);
  }

  public void testConflictChecker_hisc1_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc1_low2", true);
  }

  // testHISC10
  public void testConflictChecker_hisc10_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc10_low1", true);
  }

  public void testConflictChecker_hisc12_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc12_low1", true);
  }

  public void testConflictChecker_hisc12_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc12_low2", true);
  }

  /*
   * public void testConflictChecker_hisc13_low1() throws Exception {
   * testConflictChecker("despot", "testHISC", "hisc13_low1", false); }
   *
   * public void testConflictChecker_hisc13_low2() throws Exception {
   * testConflictChecker("despot", "testHISC", "hisc13_low2", true); }
   */

  public void testConflictChecker_hisc14_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc14_low1", true);
  }

  public void testConflictChecker_hisc14_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc14_low2", true);
  }

  public void testConflictChecker_hisc2_low1() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc2_low1", true);
  }

  public void testConflictChecker_hisc2_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc2_low2", true);
  }

  public void testConflictChecker_hisc3_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc3_low2", true);
  }

  public void testConflictChecker_hisc7_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc7_low2", true);
  }

  public void testConflictChecker_hisc8_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc8_low2", false);
  }

  public void testConflictChecker_hisc9_low2() throws Exception
  {
    testConflictChecker("despot", "testHISC", "hisc9_low2", false);
  }

  public void testConflictChecker_aip3_syn_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "as1", true);
  }

  public void testConflictChecker_aip3_syn_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "as2", true);
  }

  public void testConflictChecker_aip3_syn_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "as3", true);
  }

  public void testConflictChecker_aip3_syn_io() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "io", true);
  }

  public void testConflictChecker_aip3_syn_tu1() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu1", true);
  }

  public void testConflictChecker_aip3_syn_tu2() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu2", true);
  }

  public void testConflictChecker_aip3_syn_tu3() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu3", true);
  }

  public void testConflictChecker_aip3_syn_tu4() throws Exception
  {
    testConflictChecker("despot", "song_aip/aip3_syn", "tu4", true);
  }

  public void testConflictChecker_maip3_syn_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_syn", "aip1", true);
  }

  public void testConflictChecker_maip3_syn_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_syn", "aip2", true);
  }

  public void testConflictChecker_maip3_syn_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_syn", "aip3", true);
  }

  public void testConflictChecker_maip3_veri_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_veri", "aip1", true);
  }

  public void testConflictChecker_maip3_veri_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_veri", "aip2", true);
  }

  public void testConflictChecker_maip3_veri_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip3_veri", "aip3", true);
  }

  public void testConflictChecker_maip5_syn_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_syn", "aip1", true);
  }

  public void testConflictChecker_maip5_syn_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_syn", "aip2", true);
  }

  public void testConflictChecker_maip5_syn_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_syn", "aip3", true);
  }

  public void testConflictChecker_maip5_veri_as1() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_veri", "aip1", true);
  }

  public void testConflictChecker_maip5_veri_as2() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_veri", "aip2", true);
  }

  public void testConflictChecker_maip5_veri_as3() throws Exception
  {
    testConflictChecker("despot", "song_aip/maip5_veri", "aip3", true);
  }

  void testConflictChecker(final String group, final String subdir,
                           final String name, final boolean expectedResult)
      throws Exception
  {
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);
    boolean falseFound = false;
    MonolithicConflictChecker conflictChecker = null;

    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);
      final EventProxy defaultMark = mBuilder.getMarkingProposition();
      final EventProxy preconditionMark = mBuilder.getGeneralisedPrecondition();
      conflictChecker =
          new MonolithicConflictChecker(modifiedDES, defaultMark,
              preconditionMark, mProductDESFactory);
      final boolean result = conflictChecker.run();

      if (!result && expectedResult) {
        final ConflictTraceProxy counterexample =
            conflictChecker.getCounterExample();
        precheckCounterExample(counterexample);
        saveCounterExample(counterexample);
        assertEquals(
                     "Wrong result from model checker: the answer "
                         + answer.getName()
                         + " gives "
                         + result
                         + " but this model should have satisified SICPropertyV, therefore all "
                         + "answer events should produce true as a result!",
                     expectedResult, result);

        final ConflictTraceProxy convertedTrace =
            mBuilder.convertTraceToOriginalModel(counterexample, answer);
        // tests whether the counter example trace is correctly converted
        checkCounterExample(mBuilder.getUnchangedModel(), convertedTrace);
        break;
      } else if (!result && !expectedResult) {
        falseFound = true;
        break;
      }
    }
    if (!expectedResult && !falseFound) {
      final ConflictTraceProxy counterexample =
          conflictChecker.getCounterExample();
      precheckCounterExample(counterexample);
      saveCounterExample(counterexample);
      assertEquals(
                   "Wrong result from model checker: all answers in the model give true as a result "
                       + " but this model should not satisify SICPropertyV, therefore atleast one answer "
                       + "event should have produced a false result!",
                   !expectedResult, !falseFound);
    }
    mPropertyVerifier.setConflictChecker(conflictChecker);
    final boolean finalResult = mPropertyVerifier.run();
    assertEquals(
                 "Wrong result from SIC Property V Verifier: the expected result was "
                     + expectedResult + " but got " + finalResult + ".",
                 finalResult, expectedResult);
  }

  protected File saveCounterExample(final TraceProxy counterexample)
      throws Exception
  {
    assertNotNull(counterexample);
    final String name = counterexample.getName();
    final String ext = mTraceMarshaller.getDefaultExtension();
    final StringBuffer buffer = new StringBuffer(name);
    buffer.append(ext);
    final String extname = buffer.toString();
    assertTrue("File name '" + extname + "' contains colon, "
        + "which does not work on all platforms!", extname.indexOf(':') < 0);
    final File dir = getOutputDirectory();
    final File filename = new File(dir, extname);
    ensureParentDirectoryExists(filename);
    mTraceMarshaller.marshal(counterexample, filename);
    return filename;
  }

  protected ProductDESProxy loadProductDES(final URI unmodifiedDESURI)
      throws WatersUnmarshalException, IOException, EvalException
  {
    final DocumentProxy doc = mDocumentManager.load(unmodifiedDESURI);
    ProductDESProxy des;
    if (doc instanceof ProductDESProxy) {
      des = (ProductDESProxy) doc;
    } else {
      final ModuleProxy module = (ModuleProxy) doc;
      final ModuleCompiler compiler =
          new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
      des = compiler.compile();
    }
    return des;
  }

  protected Collection<EventProxy> getModelAnswerEvents(final String group,
                                                        final String subdir,
                                                        final String name)
      throws Exception
  {
    final File groupname = new File(mInputDirectory, group);
    final File indirname = new File(groupname, subdir);
    final String wmodext = mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(indirname, name + wmodext);
    final URI unmodifiedDESURI = infilename.toURI();

    final ProductDESProxy originalDES = loadProductDES(unmodifiedDESURI);
    mBuilder.setInputModel(originalDES);
    mPropertyVerifier.setModel(originalDES);
    return mBuilder.getAnswerEvents();
  }

  protected ModelVerifier createModelVerifier(
                                              final ProductDESProxyFactory factory)
  {
    mPropertyVerifier = new SICPropertyVVerifier(factory);
    return mPropertyVerifier;
  }

  // #########################################################################
  // # Overrides for junit.framework.TestCase
  protected void setUp() throws Exception
  {
    super.setUp();
    mInputDirectory = getWatersInputRoot();
    // mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mTraceMarshaller = new JAXBTraceMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerMarshaller(mTraceMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mBuilder = new SICPropertyVBuilder(mProductDESFactory);
    mPropertyVerifier = new SICPropertyVVerifier(mProductDESFactory);
  }

  protected void tearDown() throws Exception
  {
    mInputDirectory = null;
    // mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mTraceMarshaller = null;
    mDocumentManager = null;
    mBuilder = null;
    super.tearDown();
  }

  // #########################################################################
  // # Data Members

  private File mInputDirectory;
  // private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private JAXBTraceMarshaller mTraceMarshaller;
  private DocumentManager mDocumentManager;
  private SICPropertyVBuilder mBuilder;
  private SICPropertyVVerifier mPropertyVerifier;

}
