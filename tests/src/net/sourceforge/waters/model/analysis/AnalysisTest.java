//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   AlgorithmsTest
//###########################################################################
//# $Id: AnalysisTest.java,v 1.1 2005-11-04 02:21:17 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import net.sourceforge.waters.junit.WatersTestCase;


public class AnalysisTest extends WatersTestCase
{

  //#########################################################################
  //# Test Cases
  public void testCopy_machine()
    throws IOException, WatersException
  {
    testCopy("machine");
  }


  //#########################################################################
  //# Utilities
  void testCopy(final String modname)
    throws IOException, WatersException
  {
    final String inextname = modname + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(mInputDirectory, inextname);
    final ProductDESProxy des = compile(infilename);
    final ProductDESCopier copier =
      new ProductDESCopier(mProductDESFactory, des);
    final ProductDESResult result = (ProductDESResult) copier.run();
    assertTrue("Unexpected result value!", result.getSatisfied() == false);
    final ProductDESProxy copy = result.getProductDES();
    final String desname = des.getName();
    final String copyname = copy.getName();
    assertTrue("Unexpected result name!", desname.equals(copyname));
  }

  ProductDESProxy compile(final File infilename)
    throws IOException, WatersException
  {
    final ModuleProxy module =
      (ModuleProxy) mModuleMarshaller.unmarshal(infilename);
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    return compiler.compile();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  {
    mInputDirectory = new File(getInputRoot(), "handwritten");
    mOutputDirectory = getOutputDirectory();
    mModuleFactory = ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(mModuleFactory, optable);
    mDocumentManager = new DocumentManager<DocumentProxy>();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
  }

  protected void tearDown()
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mModuleFactory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private File mOutputDirectory;
  private ModuleProxyFactory mModuleFactory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private DocumentManager<DocumentProxy> mDocumentManager;

}
