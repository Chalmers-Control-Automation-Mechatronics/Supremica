//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.marshaller
//# CLASS:   AbstractProductDESImporterTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.marshaller;

import java.net.URI;
import java.io.File;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.module.ModuleIntegrityChecker;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


public abstract class AbstractProductDESImporterTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Test Cases
  public void testEmpty()
    throws Exception
  {
    final ProductDESProxy des =
      mProductDESProxyFactory.createProductDESProxy("empty");
    testImport(des);
  }

  public void testBuffer()
    throws Exception
  {
    testImport("handwritten", "buffer_sf1.wdes");
  }

  public void testSmallFactory()
    throws Exception
  {
    testImport("handwritten", "small_factory_2.wdes");
  }

  public void testTictactoe()
    throws Exception
  {
    testImport("handwritten", "tictactoe.wdes");
  }

  public void testTransferline2()
    throws Exception
  {
    testImport("handwritten", "transferline-2.wdes");
  }

  public void testJustProperty()
    throws Exception
  {
    testImport("tests", "nasty", "just_property.wdes");
  }

  public void testProfisafeI4Slave()
    throws Exception
  {
    testImport("tests", "profisafe", "profisafe_i4_slave.wdes");
  }

  public void testProfisafeO4Host()
    throws Exception
  {
    testImport("tests", "profisafe", "profisafe_o4_host.wdes");
  }

  public void testDebounce()
    throws Exception
  {
    testImport("valid", "debounce", "debounce.wdes");
  }


  //#########################################################################
  //# Utilities
  protected ModuleProxy testImport(final String name)
    throws Exception
  {
    return testImport(getWatersInputRoot(), name);
  }

  protected ModuleProxy testImport(final String dirname, final String name)
    throws Exception
  {
    final File dir = new File(getWatersInputRoot(), dirname);
    return testImport(dir, name);
  }

  protected ModuleProxy testImport(final String dirname1,
                                   final String dirname2,
                                   final String name)
    throws Exception
  {
    final File dir1 = new File(getWatersInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testImport(dir2, name);
  }

  protected ModuleProxy testImport(final File indir, final String filename)
    throws Exception
  {
    final File infile = new File(indir, filename);
    final URI inuri = infile.toURI();
    final ProductDESProxy des = mProductDESMarshaller.unmarshal(inuri);
    return testImport(des);
  }

  protected ModuleProxy testImport(final ProductDESProxy des)
    throws Exception
  {
    final String name = des.getName();
    final ModuleProxy module = mImporter.importModule(des);
    final File outdir = getOutputDirectory();
    final String modext = mModuleMarshaller.getDefaultExtension();
    final File outmodfile = new File(outdir, name + modext);
    mModuleMarshaller.marshal(module, outmodfile);
    mIntegrityChecker.check(module);
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
    final ProductDESProxy compileddes = compiler.compile();
    final String desext = mProductDESMarshaller.getDefaultExtension();
    final File outdesfile = new File(outdir, name + desext);
    mProductDESMarshaller.marshal(compileddes, outdesfile);
    assertProductDESProxyEquals
      ("Compilation of imported module does not yield original DES!",
       compileddes, des);
    return module;
  }


  //#########################################################################
  //# Provided by Subclasses
  protected abstract ModuleProxyFactory getModuleProxyFactory();


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    final ModuleProxyFactory modfactory = getModuleProxyFactory();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(modfactory, optable);
    mProductDESMarshaller =
      new JAXBProductDESMarshaller(mProductDESProxyFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mImporter = new ProductDESImporter(modfactory, mDocumentManager);
    mIntegrityChecker = ModuleIntegrityChecker.getInstance();
  }


  protected void tearDown()
    throws Exception
  {
    mDocumentManager = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mImporter = null;
    super.tearDown();
  }


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mProductDESProxyFactory;
  private DocumentManager mDocumentManager;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private ProductDESImporter mImporter;
  private ModuleIntegrityChecker mIntegrityChecker;

}
