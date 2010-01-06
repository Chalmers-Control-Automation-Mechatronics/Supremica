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
import java.util.List;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESIntegrityChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;
import net.sourceforge.waters.valid.ValidUnmarshaller;


public class SICPropertyVBuilderTest extends AbstractWatersTest
{

  // #########################################################################
  // # tests integrity of the model built
  public void testIntegrity() throws Exception
  {
    final List<EventProxy> answerEvents =
        (List<EventProxy>) mBuilder.getAnswerEvents();
    for (final EventProxy answer : answerEvents) {
      final ProductDESProxy modifiedDES = mBuilder.createModelForAnswer(answer);
      System.out.print(modifiedDES);
      ProductDESIntegrityChecker.getInstance().check(modifiedDES);
    }
  }

  // #########################################################################
  // # Exception Throwing Test Cases
  public void testException_nonexist() throws Exception
  {
    /*
     * testException("testSimple", "nonexist", FileNotFoundException.class,
     * "nonexist.des");
     */
  }

  // #########################################################################
  // # Utilities
  void testException(final String subdir, final String name,
                     final Class<? extends Exception> exclass,
                     final String culprit) throws Exception
  {
    try {
      testBuild(subdir, name);
      fail("Expected " + ProxyTools.getShortClassName(exclass) + " not caught!");
    } catch (final Exception exception) {
      if (exclass.isAssignableFrom(exception.getClass())) {
        final String msg = exception.getMessage();
        if (msg == null) {
          fail(ProxyTools.getShortClassName(exclass)
              + " caught as expected, but message is null!");
        } else if (msg.indexOf(culprit) < 0) {
          fail(ProxyTools.getShortClassName(exclass)
              + " caught as expected, but message '" + msg
              + "' does not mention culprit '" + culprit + "'!");
        }
      } else {
        throw exception;
      }
    }
  }

  void testBuild(final String subdir, final String name) throws Exception
  {
    try {
      final ModuleProxyFactory moduleFactory =
          ModuleElementFactory.getInstance();
      final ProductDESProxyFactory desFactory =
          ProductDESElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      final ValidUnmarshaller importer =
          new ValidUnmarshaller(moduleFactory, optable);
      final JAXBModuleMarshaller moduleMarshaller =
          new JAXBModuleMarshaller(moduleFactory, optable, false);
      final JAXBProductDESMarshaller desMarshaller =
          new JAXBProductDESMarshaller(desFactory);
      final DocumentManager docManager = new DocumentManager();
      docManager.registerUnmarshaller(desMarshaller);
      docManager.registerUnmarshaller(moduleMarshaller);
      docManager.registerUnmarshaller(importer);

      final File filename = new File(name);
      final DocumentProxy doc = docManager.load(filename);
      final ProductDESProxy des;
      if (doc instanceof ProductDESProxy) {
        des = (ProductDESProxy) doc;
      } else {
        final ModuleProxy module = (ModuleProxy) doc;
        final ModuleCompiler compiler =
            new ModuleCompiler(docManager, desFactory, module);
        des = compiler.compile();
      }
      mBuilder = new SICPropertyVBuilder(des, desFactory);
      System.out.print(des.getName() + " ... ");
      System.out.flush();

      /*
       * if (result) { System.out.println("nonconflicting"); } else {
       * System.out.println("CONFLICTING");
       * System.out.println("Counterexample:"); final ConflictTraceProxy
       * counterex = builder.getCounterExample();
       * System.out.println(counterex.toString()); }
       */

    } catch (final Throwable exception) {
      System.err.println("FATAL ERROR !!!");
      System.err.println(exception.getClass().getName() + " caught in main()!");
      exception.printStackTrace(System.err);
    }
  }

  // #########################################################################
  // # Overrides for junit.framework.TestCase
  protected void setUp() throws Exception
  {
    super.setUp();

    mProductDESFactory = ProductDESElementFactory.getInstance();
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);

  }

  protected void tearDown() throws Exception
  {
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;

    super.tearDown();
  }

  // #########################################################################
  // # Data Members

  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private SICPropertyVBuilder mBuilder;

}
