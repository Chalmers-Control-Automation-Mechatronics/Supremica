//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MazeTest
//###########################################################################
//# $Id: MazeTest.java,v 1.2 2005-11-03 01:24:16 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
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

import net.sourceforge.waters.junit.WatersTestCase;


public class MazeTest extends WatersTestCase
{

  //#########################################################################
  //# Test Cases
  public void testMaze_maze1()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze1");
  }

  public void testMaze_maze2()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze2");
  }

  public void testMaze_maze3()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze3");
  }

  public void testMaze_maze5()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze5");
  }

  public void testMaze_maze6()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze6");
  }

  public void testMaze_maze7()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze7");
  }

  public void testMaze_maze8()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze8");
  }

  public void testMaze_maze9()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze9");
  }

  public void testMaze_maze14()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze14");
  }

  public void testMaze_maze14a()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze14a");
  }


  //#########################################################################
  //# Utilities
  void testMaze(final String name)
    throws IOException, JAXBException, WatersException
  {
    final String extname = name + mMazeCompiler.getDefaultExtension();
    final File filename = new File(mInputDirectory, extname);
    final ModuleProxy module = mMazeCompiler.unmarshal(filename);
    final File outmodulefile = module.getLocation();
    mModuleMarshaller.marshal(module, outmodulefile);
    final ModuleProxy read =
      (ModuleProxy) mModuleMarshaller.unmarshal(outmodulefile);
    assertTrue("Module changed after reading back in!",
               module.equalsWithGeometry(read));
    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    final ProductDESProxy des = compiler.compile();
    final File outdesfile = des.getLocation();
    mProductDESMarshaller.marshal(des, outdesfile);
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException
  {
    mInputDirectory = new File(getInputRoot(), "maze");
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mMazeCompiler = new MazeCompiler
      (mInputDirectory, mOutputDirectory, moduleFactory, mModuleMarshaller);
    mDocumentManager = new DocumentManager<DocumentProxy>();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mMazeCompiler);
  }

  protected void tearDown()
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mMazeCompiler = null;
    mDocumentManager = null;
  }


  //#########################################################################
  //# Data Members
  private File mInputDirectory;
  private File mOutputDirectory;
  private ProductDESProxyFactory mProductDESFactory;
  private JAXBModuleMarshaller mModuleMarshaller;
  private JAXBProductDESMarshaller mProductDESMarshaller;
  private MazeCompiler mMazeCompiler;
  private DocumentManager<DocumentProxy> mDocumentManager;

}
