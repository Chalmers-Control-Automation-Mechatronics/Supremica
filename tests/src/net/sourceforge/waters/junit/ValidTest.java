//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   ValidTest
//###########################################################################
//# $Id: ValidTest.java,v 1.3 2005-05-08 00:27:15 robi Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.ElementProxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESMarshaller;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.expr.IndexOutOfRangeException;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.valid.ValidUnmarshaller;


public class ValidTest extends WatersTestCase
{

  //#########################################################################
  //# Test Cases
  public void testImport_ftuer()
    throws Exception
  {
    testImport("central_locking", "ftuer");
  }

  public void testImport_debounce()
    throws Exception
  {
    testImport("debounce", "debounce");
  }

  public void testImport_falko()
    throws Exception
  {
    testImport("falko", "falko");
  }

  public void testImport_fischertechnik()
    throws Exception
  {
    testImport("fischertechnik", "fischertechnik");
  }

  public void testImport_fischertechnik_bad()
    throws Exception
  {
    testImport("fischertechnik", "fischertechnik_bad",
	       IndexOutOfRangeException.class, "'ST'");
  }

  public void testImport_mazes()
    throws Exception
  {
    testImport("mazes", "mazes");
  }

  public void testImport_safetydisplay()
    throws Exception
  {
    testImport("safetydisplay", "safetydisplay");
  }

  public void testImport_safetydisplay_uncont()
    throws Exception
  {
    testImport("safetydisplay", "safetydisplay_uncont");
  }

  public void testImport_small()
    throws Exception
  {
    testImport("small", "small");
  }

  public void testImport_smdreset()
    throws Exception
  {
    testImport("smd", "smdreset");
  }


  //#########################################################################
  //# Utilities
  void testImport(final String subdir, final String name,
		  final Class exclass, final String culprit)
    throws Exception
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
    throws Exception
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
    throws Exception
  {
    final ModuleProxy module = (ModuleProxy) mDocumentManager.load(infilename);
    mModuleMarshaller.marshal(module, outfilename);
    return module;
  }

  ProductDESProxy testCompile(final ModuleProxy module,
			      final File outfilename)
    throws Exception
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
    throws Exception
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
    final ValidUnmarshaller importer = new ValidUnmarshaller();
    mDESMarshaller = new ProductDESMarshaller();
    mModuleMarshaller = new ModuleMarshaller();
    mInputDirectory = new File(getInputRoot(), "valid");
    mOutputDirectory = new File(getOutputRoot(), "valid");
    mDocumentManager = new DocumentManager();
    mDocumentManager.register(mDESMarshaller);
    mDocumentManager.register(mModuleMarshaller);
    mDocumentManager.register(importer);
  }

  protected void tearDown()
  {
    mDESMarshaller = null;
    mModuleMarshaller = null;
    mInputDirectory = null;
    mOutputDirectory = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private ProductDESMarshaller mDESMarshaller;
  private ModuleMarshaller mModuleMarshaller;
  private DocumentManager mDocumentManager;
  private File mInputDirectory;
  private File mOutputDirectory;

}
