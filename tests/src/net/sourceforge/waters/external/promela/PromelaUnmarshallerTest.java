//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   PromelaUnmarshallerTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.external.promela;

import java.io.File;
import java.net.URI;
import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleIdentifierChecker;
import net.sourceforge.waters.model.module.ModuleIntegrityChecker;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class PromelaUnmarshallerTest extends AbstractWatersTest
{

  //#########################################################################
  //# Successful Test Cases
  public void testImport_output_aip_leducversion_old() throws Exception
  {
    testImport("p101");
  }


  //#########################################################################
  //# Utilities
  void testException(final String subdir, final String name,
                     final Class<? extends Exception> exclass,
                     final String culprit)
  throws Exception
  {
    try {
      testImport(subdir, name);
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

  void testImport(final String name)
  throws Exception
  {
    testImport(null, name);
  }

  void testImport(final String subdir, final String name) throws Exception
  {
    final String inextname = name + mImporter.getDefaultExtension();
    getLogger().info("Unmarshalling " + inextname + " ...");
    final File indirname =
      subdir == null ? mInputDirectory : new File(mInputDirectory, subdir);
    final File infilename = new File(indirname, inextname);
    final URI promelaURI = infilename.toURI();
    final File outdirname = getOutputDirectory();
    final ModuleProxy module = mImporter.unmarshal(promelaURI);
    final String wmodextname = name + mModuleMarshaller.getDefaultExtension();
    final File wmodfilename = new File(outdirname, wmodextname);
    mModuleMarshaller.marshal(module, wmodfilename);
    assertEquals("Unexpected module name in output!", module.getName(), name);
    mIdentifierChecker.check(module);
    mIntegrityChecker.check(module);
    final File expectfile = new File(indirname, wmodextname);
    if (expectfile.exists()) {
      final URI expecturi = expectfile.toURI();
      final ModuleProxy expectmodule =
          mModuleMarshaller.unmarshal(expecturi);
      assertProxyEquals(mEqualityChecker,
                        "Unexpected module contents in output!",
                        module, expectmodule);
    }
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp() throws Exception
  {
    super.setUp();
    mInputDirectory = new File(getWatersInputRoot(), "promela");
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
    mImporter = new PromelaUnmarshaller(moduleFactory, mDocumentManager);
    mIdentifierChecker =
      ModuleIdentifierChecker.getModuleIdentifierCheckerInstance();
    mIntegrityChecker =
      ModuleIntegrityChecker.getModuleIntegrityCheckerInstance();
    mEqualityChecker = new ModuleEqualityVisitor(true, false);
  }

  @Override
  protected void tearDown() throws Exception
  {
    mInputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
    mImporter = null;
    mIdentifierChecker = null;
    mIntegrityChecker = null;
    mEqualityChecker = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;
  private PromelaUnmarshaller mImporter;
  private ModuleIdentifierChecker mIdentifierChecker;
  private ModuleIntegrityChecker mIntegrityChecker;
  private ModuleEqualityVisitor mEqualityChecker;

}
