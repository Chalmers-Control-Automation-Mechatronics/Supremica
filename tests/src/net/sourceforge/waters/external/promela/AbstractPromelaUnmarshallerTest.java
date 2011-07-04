//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters Analysis
//# PACKAGE: net.sourceforge.waters.external.promela
//# CLASS:   AbstractPromelaUnmarshallerTest
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


public abstract class AbstractPromelaUnmarshallerTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Entry points in junit.framework.TestCase
  public AbstractPromelaUnmarshallerTest()
  {
  }

  public AbstractPromelaUnmarshallerTest(final String name)
  {
    super(name);
  }


  //#########################################################################
  //# Successful Test Cases
  /**
   * <P>Tests the import of the Promela file
   * {supremica}<CODE>/examples/waters/promela/p101a.pml</CODE>.</P>
   *
   * <P>After running the test, the imported module is saved in
   * {supremica}<CODE>/logs/results/external/promela/PromelaUnmarshallerTest</CODE>
   * as a <CODE>.wmod</CODE> file that can be loaded into the IDE.
   * After saving, the output is compared to the expected result in
   * {supremica}<CODE>/examples/waters/promela/p101a.wmod</CODE>, and any
   * differences cause a test failure.</P>
   */
  public void testImport_p101a() throws Exception
  {
    testImport("p101a");
  }

  public void testImport_p101b() throws Exception
  {
    testImport("p101b");
  }

  public void testImport_p101c() throws Exception
  {
    testImport("p101c");
  }

  public void testImport_p101d() throws Exception
  {
    testImport("p101d");
  }

  public void testImport_p101e() throws Exception
  {
    testImport("p101e");
  }

  public void testImport_p102a() throws Exception
  {
    testImport("p102a");
  }

  public void testImport_p103a() throws Exception
  {
    testImport("p103a");
  }

  public void testImport_p103b() throws Exception
  {
    testImport("p103b");
  }

  public void testImport_p103c() throws Exception
  {
    testImport("p103c");
  }

  public void testImport_p103d() throws Exception
  {
    testImport("p103d");
  }

  public void testImport_p103e() throws Exception
  {
    testImport("p103e");
  }

  public void testImport_p104a() throws Exception
  {
    testImport("p104a");
  }

  public void testImport_p104b() throws Exception
  {
    testImport("p104b");
  }

  public void testImport_p104c() throws Exception
  {
    testImport("p104c");
  }

  public void testImport_p104d() throws Exception
  {
    testImport("p104d");
  }

  public void testImport_p104e() throws Exception
  {
    testImport("p104e");
  }

  public void testImport_p104f() throws Exception
  {
    testImport("p104f");
  }

  public void testImport_p104g() throws Exception
  {
    testImport("p104g");
  }

  public void testImport_p104h() throws Exception
  {
    testImport("p104h");
  }

  public void testImport_p104i() throws Exception
  {
    testImport("p104i");
  }

  public void testImport_p104j() throws Exception
  {
    testImport("p104j");
  }

  public void testImport_p104k() throws Exception
  {
    testImport("p104k");
  }

  public void testImport_p105a() throws Exception
  {
    testImport("p105a");
  }

  public void testImport_p105b() throws Exception
  {
    testImport("p105b");
  }

  public void testImport_p105c() throws Exception
  {
    testImport("p105c");
  }

  public void testImport_p105d() throws Exception
  {
    testImport("p105d");
  }

  public void testImport_p105e() throws Exception
  {
    testImport("p105e");
  }

  public void testImport_p105f() throws Exception
  {
    testImport("p105f");
  }

  public void testImport_p105g() throws Exception
  {
    testImport("p105g");
  }

  public void testImport_p106a() throws Exception
  {
    testImport("p106a");
  }

  public void testImport_p106b() throws Exception
  {
    testImport("p106b");
  }

  public void testImport_p106c() throws Exception
  {
    testImport("p106c");
  }


  //#########################################################################
  //# Hooks
  abstract ModuleProxyFactory getModuleProxyFactory();


  //#########################################################################
  //# Utilities
  void testException(final String subdir, final String name,
                     final Class<? extends Exception> exclass,
                     final String culprit)
  throws Exception
  {
    try {
      testImport(subdir, name);
      fail("Expected " + ProxyTools.getShortClassName(exclass) +
           " not caught!");
    } catch (final Exception exception) {
      if (exclass.isAssignableFrom(exception.getClass())) {
        final String msg = exception.getMessage();
        if (msg == null) {
          fail(ProxyTools.getShortClassName(exclass) +
               " caught as expected, but message is null!");
        } else if (msg.indexOf(culprit) < 0) {
          fail(ProxyTools.getShortClassName(exclass) +
               " caught as expected, but message '" + msg +
               "' does not mention culprit '" + culprit + "'!");
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
    mIntegrityChecker.check(module);
    mModuleMarshaller.marshal(module, wmodfilename);
    assertEquals("Unexpected module name in output!", module.getName(), name);
    mIdentifierChecker.check(module);
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
    final ModuleProxyFactory moduleFactory = getModuleProxyFactory();
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
