//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   AlgorithmsTest
//###########################################################################
//# $Id: AnalysisTest.java,v 1.2 2005-02-18 01:32:42 robi Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.analysis.ProductDESCopier;
import net.sourceforge.waters.model.analysis.ProductDESResult;
import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESMarshaller;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;


public class AnalysisTest extends WatersTestCase
{

  //#########################################################################
  //# Test Cases
  public void testCopy_machine()
    throws IOException, JAXBException, WatersException
  {
    testCopy("machine");
  }


  //#########################################################################
  //# Utilities
  void testCopy(final String modname)
    throws IOException, JAXBException, WatersException
  {
    final String inextname = modname + mModuleMarshaller.getDefaultExtension();
    final File infilename = new File(mInputDirectory, inextname);
    final ProductDESProxy des = compile(infilename);
    final ProductDESCopier copier = new ProductDESCopier(des);
    final ProductDESResult result = (ProductDESResult) copier.run();
    assertTrue("Unexpected result value!", result.getSatisfied() == false);
    final ProductDESProxy copy = result.getProductDES();
    final String desname = des.getName();
    final String copyname = copy.getName();
    assertTrue("Unexpected result name!", desname.equals(copyname));
  }

  ProductDESProxy compile(final File infilename)
    throws JAXBException, WatersException, IOException
  {
    final ModuleProxy module =
      (ModuleProxy) mModuleMarshaller.unmarshal(infilename);
    final ModuleCompiler compiler =
      new ModuleCompiler(module, mDocumentManager);
    return compiler.compile();
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  {
    mModuleMarshaller = new ModuleMarshaller();
    mDESMarshaller = new ProductDESMarshaller();
    mDocumentManager = new DocumentManager();
    mDocumentManager.register(mModuleMarshaller);
    mDocumentManager.register(mDESMarshaller);
    mInputDirectory = new File(getInputRoot(), "handwritten");
    mOutputDirectory = new File(getOutputRoot(), "analysis");
  }

  protected void tearDown()
  {
    mModuleMarshaller = null;
    mDESMarshaller = null;
    mDocumentManager = null;
    mInputDirectory = null;
    mOutputDirectory = null;
  }


  //#########################################################################
  //# Data Members
  private ModuleMarshaller mModuleMarshaller;
  private ProductDESMarshaller mDESMarshaller;
  private DocumentManager mDocumentManager;
  private File mInputDirectory;
  private File mOutputDirectory;

}
