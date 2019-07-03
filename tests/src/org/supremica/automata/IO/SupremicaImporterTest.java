//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: org.supremica.automata.IO
//# CLASS:   SupremicaImporterTest
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.automata.IO;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.Arrays;

import net.sourceforge.waters.junit.AbstractWatersTest;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.marshaller.ProxyUnmarshaller;
import net.sourceforge.waters.model.marshaller.SAXModuleMarshaller;
import net.sourceforge.waters.model.marshaller.SAXProductDESMarshaller;
import net.sourceforge.waters.model.module.ModuleIntegrityChecker;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.apache.logging.log4j.Logger;

import org.supremica.automata.Project;


public class SupremicaImporterTest
  extends AbstractWatersTest
{

  //#########################################################################
  //# Test Cases
  public void testCh2Automaton()
  throws Exception
{
  testImport("CCSBookExamples", "Ch2_Automaton.xml");
}

  public void testAipAll()
    throws Exception
  {
    testImport("OtherExamples", "aip", "All.xml");
  }

  public void testImportAll()
    throws Exception
  {
    final FileFilter filter = new TestDirectoryFilter();
    final File root = getSupremicaInputRoot();
    testDirectory(root, filter);
  }


  //#########################################################################
  //# Utilities
  protected void testDirectory(final File file, final FileFilter filter)
    throws Exception
  {
    if (file.isDirectory()) {
      final File[] children = file.listFiles(filter);
      Arrays.sort(children);
      for (final File child : children) {
        testDirectory(child, filter);
      }
    } else {
      testImport(file);
    }
  }

  //#########################################################################
  //# Utilities
  private ModuleProxy testImport(final String dirname, final String name)
    throws Exception
  {
    final File dir = new File(getSupremicaInputRoot(), dirname);
    return testImport(dir, name);
  }

  private ModuleProxy testImport(final String dirname1,
                                 final String dirname2,
                                 final String name)
    throws Exception
  {
    final File dir1 = new File(getSupremicaInputRoot(), dirname1);
    final File dir2 = new File(dir1, dirname2);
    return testImport(dir2, name);
  }

  private ModuleProxy testImport(final File indir, final String filename)
    throws Exception
  {
    final File infile = new File(indir, filename);
    return testImport(infile);
  }

  private ModuleProxy testImport(final File infile)
    throws Exception
  {
    final Logger logger = getLogger();
    logger.debug(infile + " ...");
    final URI inuri = infile.toURI();
    final ProductDESProxy des = mSupremicaUnmarshaller.unmarshal(inuri);
    return testImport(des);
  }

  private ModuleProxy testImport(final ProductDESProxy des)
    throws Exception
  {
    final ModuleProxy module = mImporter.importModule(des);
    final File outdir = getOutputDirectory();
    final File location = des.getFileLocation();
    String name = location.getName();
    final int dotpos = name.indexOf('.');
    if (dotpos >= 0) {
      name = name.substring(0, dotpos);
    }
    final String modext = mModuleMarshaller.getDefaultExtension();
    final File outmodfile = new File(outdir, name + modext);
    mModuleMarshaller.marshal(module, outmodfile);
    mIntegrityChecker.check(module);
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESProxyFactory, module);
    compiler.setOptimizationEnabled(false);
    final ProductDESProxy compileddes = compiler.compile();
    final String desext = mProductDESMarshaller.getDefaultExtension();
    final File outdesfile = new File(outdir, name + desext);
    mProductDESMarshaller.marshal(compileddes, outdesfile);
    assertProductDESProxyEquals
      ("Compilation of imported module is not equal to original Project!",
       compileddes, des);
    return module;
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mProductDESProxyFactory = ProductDESElementFactory.getInstance();
    final ModuleProxyFactory modfactory = ModuleSubjectFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new SAXModuleMarshaller(modfactory, optable);
    mProductDESMarshaller =
      new SAXProductDESMarshaller(mProductDESProxyFactory);
    mSupremicaUnmarshaller = new SupremicaUnmarshaller(modfactory);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mSupremicaUnmarshaller);
    mImporter = new ProductDESImporter(modfactory, mDocumentManager);
    mIntegrityChecker =
      ModuleIntegrityChecker.getInstance();
  }


  @Override
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
  //# Auxiliary Methods
  private File getSupremicaInputRoot()
  {
    return new File(getInputRoot(), "includeInJarFile");
  }

  @Override
  protected File getOutputDirectory()
  {
    final String classname = getClass().getName();
    final String[] parts = classname.split("\\.");
    File result = getOutputRoot();
    for (int i = 1; i < parts.length; i++) {
      result = new File(result, parts[i]);
    }
    ensureDirectoryExists(result);
    return result;
  }


  //#########################################################################
  //# Inner Class TestDirectoryFilter
  private class TestDirectoryFilter implements FileFilter {

    //#######################################################################
    //# Interface java.io.FileFilter
    @Override
    public boolean accept(final File path)
    {
      final String name = path.getName();
      if (path.isDirectory()) {
        return true;
      } else {
        final int dotpos = name.lastIndexOf('.');
        if (dotpos < 0) {
          return false;
        }
        final String ext = name.substring(dotpos);
        final String dftext = mSupremicaUnmarshaller.getDefaultExtension();
        if (!ext.equals(dftext)) {
          return false;
        }
        final boolean anim = name.indexOf("Animation") > 0;
        return !anim;
      }
    }

  }


  //#########################################################################
  //# Data Members
  private ProductDESProxyFactory mProductDESProxyFactory;
  private DocumentManager mDocumentManager;
  private SAXModuleMarshaller mModuleMarshaller;
  private SAXProductDESMarshaller mProductDESMarshaller;
  private ProxyUnmarshaller<Project> mSupremicaUnmarshaller;
  private ProductDESImporter mImporter;
  private ModuleIntegrityChecker mIntegrityChecker;

}
