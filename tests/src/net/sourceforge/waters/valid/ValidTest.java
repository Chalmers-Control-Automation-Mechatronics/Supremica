//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.valid
//# CLASS:   ValidTest
//###########################################################################
//# $Id: ValidTest.java,v 1.4 2006-07-25 22:06:07 robi Exp $
//###########################################################################

package net.sourceforge.waters.valid;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.IndexOutOfRangeException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.compiler.TypeMismatchException;
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

import net.sourceforge.waters.junit.AbstractWatersTest;

import org.xml.sax.SAXException;


public class ValidTest extends AbstractWatersTest
{

  //#########################################################################
  //# Successful Test Cases
  public void testImport_bfactory()
    throws IOException, WatersException
  {
    testImport("big_factory", "bfactory");
  }

  public void testImport_bmw_fh()
    throws IOException, WatersException
  {
    testImport("bmw_fh", "bmw_fh");
  }

  public void testImport_bmw_fh_bad()
    throws IOException, WatersException
  {
    testImport("bmw_fh", "bmw_fh_bad",
               TypeMismatchException.class, "'CLOSE'");
  }

  public void testImport_debounce()
    throws IOException, WatersException
  {
    testImport("debounce", "debounce");
  }

  public void testImport_falko()
    throws IOException, WatersException
  {
    testImport("falko", "falko");
  }

  public void testImport_fischertechnik()
    throws IOException, WatersException
  {
    testImport("fischertechnik", "fischertechnik");
  }

  public void testImport_fischertechnik_bad()
    throws IOException, WatersException
  {
    testImport("fischertechnik", "fischertechnik_bad",
               IndexOutOfRangeException.class, "'ST'");
  }

  public void testImport_ftuer()
    throws IOException, WatersException
  {
    testImport("central_locking", "ftuer");
  }

  public void testImport_koordwsp()
    throws IOException, WatersException
  {
    testImport("central_locking", "koordwsp");
  }

  public void testImport_mazes()
    throws IOException, WatersException
  {
    testImport("mazes", "mazes");
  }

  public void testImport_never_blow_up()
    throws IOException, WatersException
  {
    testImport("border_cases", "never_blow_up");
  }

  public void testImport_safetydisplay()
    throws IOException, WatersException
  {
    testImport("safetydisplay", "safetydisplay");
  }

  public void testImport_safetydisplay_uncont()
    throws IOException, WatersException
  {
    testImport("safetydisplay", "safetydisplay_uncont");
  }

  public void testImport_small()
    throws IOException, WatersException
  {
    testImport("small", "small");
  }

  public void testImport_small_uncont()
    throws IOException, WatersException
  {
    testImport("small", "small_uncont");
  }

  public void testImport_smdreset()
    throws IOException, WatersException
  {
    testImport("smd", "smdreset");
  }

  public void testImport_tline_1()
    throws IOException, WatersException
  {
    testImport("tline_1", "tline_1");
  }

  public void testImport_transferline_templ()
    throws IOException, WatersException
  {
    testImport("tline_0", "transferline_templ");
  }

  public void testImport_weiche()
    throws IOException, WatersException
  {
    testImport("vt", "weiche");
  }

  public void testImport_wonham_templ()
    throws IOException, WatersException
  {
    testImport("tline_0", "wonham_templ");
  }


  //#########################################################################
  //# Utilities
  void testImport(final String subdir,
                  final String name,
                  final Class<? extends WatersException> exclass,
                  final String culprit)
    throws IOException, WatersException
  {
    try {
      testImport(subdir, name, false);
      fail("Expected " + exclass.getName() + " not caught!");
    } catch (final WatersException exception) {
      if (exception.getClass() == exclass) {
        if (culprit != null) {
          final String msg = exception.getMessage();
          assertTrue("Caught " + exclass.getName() +
                     " as expected, but message '" + msg +
                     "' does not mention culprit: " + culprit + "!",
                     msg != null && msg.indexOf(culprit) >= 0);
        }
        if (exception instanceof EvalException) {
          final EvalException evalException = (EvalException) exception;
          final Proxy location = evalException.getLocation();
          assertNotNull("Caught " + exception.getClass().getName() + " <" +
                        exception.getMessage() + "> provides no location!",
                        location);
        }
      } else {
        throw exception;
      }
    }
  }

  void testImport(final String subdir, final String name)
    throws IOException, WatersException
  {
    testImport(subdir, name, true);
  }

  void testImport(final String subdir,
                  final String name,
                  final boolean compareDES)
    throws IOException, WatersException
  {
    final String inextname = name + "_main.vmod";
    final File indirname = new File(mInputDirectory, subdir);
    final File infilename = new File(indirname, inextname);
    final String outextname = name + mModuleMarshaller.getDefaultExtension();
    final File outfilename = new File(mOutputDirectory, outextname);
    final ModuleProxy module = testImport(infilename, outfilename);
    final File modfilename2 = new File(indirname, outextname);
    final String desextname =
      name + mProductDESMarshaller.getDefaultExtension();
    final File desfilename = new File(mOutputDirectory, desextname);
    final ProductDESProxy des = testCompile(module, desfilename);
    final File desfilename2 = new File(indirname, desextname);
    testCompare(module, modfilename2, "module");
    if (compareDES) {
      testCompare(des, desfilename2, "DES");
    }
  }

  ModuleProxy testImport(final File infilename, final File outfilename)
    throws IOException, WatersException
  {
    final ModuleProxy module =
      (ModuleProxy) mDocumentManager.load(infilename);
    ensureParentDirectoryExists(outfilename);
    mModuleMarshaller.marshal(module, outfilename);
    return module;
  }

  ProductDESProxy testCompile(final ModuleProxy module,
                              final File outfilename)
    throws IOException, WatersException
  {
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    final ProductDESProxy des = compiler.compile();
    ensureParentDirectoryExists(outfilename);
    mProductDESMarshaller.marshal(des, outfilename);
    return des;
  }

  void testCompare(final DocumentProxy doc,
                   final File filename,
                   final String kindname)
    throws IOException, WatersUnmarshalException
  {
    final DocumentProxy doc2 = mDocumentManager.load(filename);
    assertTrue("Identical documents from different sources!", doc != doc2);
    assertTrue("Unexpected " + kindname + " contents!",
               doc.equalsByContents(doc2));
    assertTrue("Unexpected " + kindname + " geometry!",
               doc.equalsWithGeometry(doc2));
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException, SAXException
  {
    mInputDirectory = new File(getInputRoot(), "valid");
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final ValidUnmarshaller importer =
      new ValidUnmarshaller(moduleFactory, optable);
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(importer);
  }

  protected void tearDown()
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private DocumentManager mDocumentManager;

}
