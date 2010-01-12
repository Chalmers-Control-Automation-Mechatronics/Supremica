//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.despot
//# CLASS:   DESpotImporterTest
//###########################################################################
//# $Id: DESpotImporterTest.java 5044 2009-12-30 20:14:13Z robi $
//###########################################################################

package net.sourceforge.waters.despot;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import net.sourceforge.waters.analysis.monolithic.MonolithicConflictChecker;
import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ConflictTraceProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.WatersUnmarshalException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class SICPropertyVBuilderTest extends AbstractWatersTest
{

  // #########################################################################
  // # tests the model built compared to a manually created model as well as
  // testing the integrity

  public void testBuild_hisc0_low1() throws Exception
  {
    testBuild("despot", "testHISC", "hisc0_low1");
  }

  // this model only has request and answer events so does not require the 3rd
  // state of the "test" automaton which must be created
  public void testBuild_hisc0_low2() throws Exception
  {
    testBuild("despot", "testHISC", "hisc0_low2");
  }

  public void testBuild_hisc1_low1() throws Exception
  {
    testBuild("despot", "testHISC", "hisc1_low1");
  }

  public void testIntegrity_parManEg_I_mfb_lowlevel() throws Exception
  {
    testIntegrity("tests", "hisc", "parManEg_I_mfb_lowlevel");
  }

  public void testIntegrity_rhone_subsystem1_ld_failsic5() throws Exception
  {
    testIntegrity("tests", "hisc", "rhone_subsystem1_ld_failsic5");
  }

  public void testIntegrity_rhone_subsystem1_ld() throws Exception
  {
    testIntegrity("tests", "hisc", "rhone_subsystem1_ld");
  }

  // #########################################################################

  void testIntegrity(final String group, final String subdir, final String name)
      throws Exception
  {
    final String wdesext = mProductDESMarshaller.getDefaultExtension();
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);
    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);
      final File outfilename =
          new File(mOutputDirectory, name + "_" + answer.getName() + wdesext);
      mProductDESMarshaller.marshal(modifiedDES, outfilename);
      ProductDESIntegrityChecker.getInstance().check(modifiedDES);
    }
  }

  void testConflictChecker(final String group, final String subdir,
                           final String name) throws Exception
  {
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);
    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);
      final EventProxy defaultMark = mBuilder.getMarkingProposition();
      final EventProxy preconditionMark = mBuilder.getGeneralisedPrecondition();
      final MonolithicConflictChecker conflictChecker =
          new MonolithicConflictChecker(modifiedDES, defaultMark,
              preconditionMark, mProductDESFactory);
      final boolean result = conflictChecker.run();

      if (result) {
        System.out.println("nonconflicting");
      } else {
        System.out.println("CONFLICTING");
        System.out.println("Counterexample:");
        final ConflictTraceProxy counterex =
            conflictChecker.getCounterExample();
        System.out.println(counterex.toString());
      }
    }
  }

  void testBuild(final String group, final String subdir, final String name)
      throws Exception
  {
    final File groupname = new File(mInputDirectory, group);
    final File indirname = new File(groupname, subdir);
    final String wmodext = mModuleMarshaller.getDefaultExtension();
    final String wdesext = mProductDESMarshaller.getDefaultExtension();
    final Collection<EventProxy> answerEvents =
        getModelAnswerEvents(group, subdir, name);

    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);

      final File outfilename =
          new File(mOutputDirectory, name + "_" + answer.getName() + wdesext);
      mProductDESMarshaller.marshal(modifiedDES, outfilename);
      ProductDESIntegrityChecker.getInstance().check(modifiedDES);

      final File expectfilename =
          new File(indirname, name + "_EXPECTED_" + answer.getName() + wmodext);
      final URI expecteddesURI = expectfilename.toURI();
      final ProductDESProxy expectedDES = loadProductDES(expecteddesURI);

      final File expectDESfilename =
          new File(mOutputDirectory, name + "_EXPECTED_" + answer.getName()
              + wdesext);
      mProductDESMarshaller.marshal(expectedDES, expectDESfilename);
      assertProductDESProxyEquals("Unexpected contents for product DES '"
          + name + "' with answer '" + answer.getName() + "'!", modifiedDES,
                                  expectedDES);
    }
  }

  private ProductDESProxy loadProductDES(final URI unmodifiedDESURI)
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

  private Collection<EventProxy> getModelAnswerEvents(final String group,
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

    return mBuilder.getAnswerEvents();
  }

  // #########################################################################
  // # Overrides for junit.framework.TestCase
  protected void setUp() throws Exception
  {
    super.setUp();
    mInputDirectory = getWatersInputRoot();
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mBuilder = new SICPropertyVBuilder(mProductDESFactory);
  }

  protected void tearDown() throws Exception
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mBuilder = null;
    super.tearDown();
  }

  // #########################################################################
  // # Data Members

  private File mInputDirectory;
  private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private SICPropertyVBuilder mBuilder;
}
