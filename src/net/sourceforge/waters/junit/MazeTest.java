//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.junit
//# CLASS:   MazeTest
//###########################################################################
//# $Id: MazeTest.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################


package net.sourceforge.waters.junit;

import java.io.File;
import java.io.IOException;
import javax.xml.bind.JAXBException;
import junit.framework.TestCase;

import net.sourceforge.waters.model.base.DocumentManager;
import net.sourceforge.waters.model.base.ProxyMarshaller;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.ProductDESMarshaller;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.module.ModuleMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.samples.maze.MazeCompiler;


public class MazeTest extends TestCase
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


  //#########################################################################
  //# Utilities
  void testMaze(final String name)
    throws IOException, JAXBException, WatersException
  {
    final ModuleProxy module = mMazeCompiler.compile(name);
    final File outmodulefile = module.getLocation();
    mModuleMarshaller.marshal(module, outmodulefile);
    final ModuleProxy read =
      (ModuleProxy) mModuleMarshaller.unmarshal(outmodulefile);
    assertTrue("Module changed after reading back in!",
	       module.equalsWithGeometry(read));
    final ModuleCompiler compiler =
      new ModuleCompiler(module, mDocumentManager);
    final ProductDESProxy des = compiler.compile();
    final File outdesfile = des.getLocation();
    mDESMarshaller.marshal(des, outdesfile);
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
    mInputDirectory = new File("examples", "maze");
    mOutputDirectory = new File("logs", "maze");
    mMazeCompiler = new MazeCompiler
      (mInputDirectory, mOutputDirectory, true, mModuleMarshaller);
  }

  protected void tearDown()
  {
    mModuleMarshaller = null;
    mDESMarshaller = null;
    mDocumentManager = null;
    mInputDirectory = null;
    mOutputDirectory = null;
    mMazeCompiler = null;
  }


  //#########################################################################
  //# Data Members
  private ModuleMarshaller mModuleMarshaller;
  private ProductDESMarshaller mDESMarshaller;
  private DocumentManager mDocumentManager;
  private File mInputDirectory;
  private File mOutputDirectory;
  private MazeCompiler mMazeCompiler;

}
