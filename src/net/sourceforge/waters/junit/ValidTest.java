//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   ValidTest
//###########################################################################
//# $Id: ValidTest.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerConfigurationException;
import junit.framework.TestCase;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.ModelException;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESMarshaller;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.IndexOutOfRangeException;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.valid.ValidUnmarshaller;


public class ValidTest extends TestCase
{

  //#########################################################################
  //# Test Cases
  public void testImport_ftuer()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("central_locking", "ftuer");
  }

  public void testImport_debounce()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("debounce", "debounce");
  }

  public void testImport_falko()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("falko", "falko");
  }

  public void testImport_fischertechnik()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("fischertechnik", "fischertechnik");
  }

  public void testImport_fischertechnik_bad()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("fischertechnik", "fischertechnik_bad",
	       IndexOutOfRangeException.class, "'ST'");
  }

  public void testImport_mazes()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("mazes", "mazes");
  }

  public void testImport_safetydisplay()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("safetydisplay", "safetydisplay");
  }

  public void testImport_safetydisplay_uncont()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("safetydisplay", "safetydisplay_uncont");
  }

  public void testImport_small()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("small", "small");
  }

  public void testImport_smdreset()
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    testImport("smd", "smdreset");
  }


  //#########################################################################
  //# Utilities
  void testImport(final String subdir, final String name,
		  final Class exclass, final String culprit)
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    try {
      testImport(subdir, name);
      assertTrue("Expected " + exclass.getName() + " not caught!", false);
    } catch (final WatersException exception) {
      if (exception.getClass() == exclass) {
	if (culprit != null) {
	  final String msg = exception.getMessage();
	  assertTrue("Caught " + exclass.getName() +
		     " as expected, but message '" + msg +
		     " does not mention culprit " + culprit + "!",
		     msg.indexOf(culprit) >= 0);
	}
      } else {
	throw exception;
      }
    }
  }

  void testImport(final String subdir, final String name)
    throws IOException, JAXBException, WatersException,
	   TransformerConfigurationException
  {
    final String inextname = name + "_main.vmod";
    final File indirname = new File(mInputDirectory, subdir);
    final File infilename = new File(indirname, inextname);
    final String outextname = name + mModuleMarshaller.getDefaultExtension();
    final File outfilename = new File(mOutputDirectory, outextname);
    final ModuleProxy module = testImport(infilename, outfilename);
    final File modfilename2 = new File(indirname, outextname);
    final String desextname = name + mDESMarshaller.getDefaultExtension();
    final File desfilename = new File(mOutputDirectory, desextname);
    final ProductDESProxy des = testCompile(module, desfilename);
    final File desfilename2 = new File(indirname, desextname);
    testCompare(module, modfilename2, "module");
    testCompare(des, desfilename2, "DES");
  }

  ModuleProxy testImport(final File infilename, final File outfilename)
    throws IOException, JAXBException, ModelException,
	   TransformerConfigurationException
  {
    final ModuleProxy module = mUnmarshaller.unmarshal(infilename);
    mModuleMarshaller.marshal(module, outfilename);
    return module;
  }

  ProductDESProxy testCompile(final ModuleProxy module,
			      final File outfilename)
    throws JAXBException, WatersException, IOException
  {
    final ModuleCompiler compiler =
      new ModuleCompiler(module, mDocumentManager);
    final ProductDESProxy des = compiler.compile();
    mDESMarshaller.marshal(des, outfilename);
    return des;
  }

  void testCompare(final DocumentProxy doc,
		   final File filename,
		   final String kindname)
    throws JAXBException, ModelException
  {
    final DocumentProxy doc2 = mDocumentManager.load(filename);
    final ElementProxy elem  = (ElementProxy) doc;
    assertTrue("Identical documents from different sources!", elem != doc2);
    assertTrue("Unexpected " + kindname + " contents!",
	       elem.equalsWithGeometry(doc2));
  }



  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  {
    mUnmarshaller = new ValidUnmarshaller();
    mDESMarshaller = new ProductDESMarshaller();
    mModuleMarshaller = new ModuleMarshaller();
    mInputDirectory = new File("examples", "valid");
    mOutputDirectory = new File("logs", "valid");
    mDocumentManager = new DocumentManager();
    mDocumentManager.register(mDESMarshaller);
    mDocumentManager.register(mModuleMarshaller);
  }

  protected void tearDown()
  {
    mUnmarshaller = null;
    mDESMarshaller = null;
    mModuleMarshaller = null;
    mInputDirectory = null;
    mOutputDirectory = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private ValidUnmarshaller mUnmarshaller;
  private ProductDESMarshaller mDESMarshaller;
  private ModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private File mInputDirectory;
  private File mOutputDirectory;

}
