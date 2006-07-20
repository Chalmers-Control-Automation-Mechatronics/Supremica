//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MazeTest
//###########################################################################
//# $Id: MazeTest.java,v 1.4 2006-07-20 02:28:38 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.base.DocumentProxy;
import net.sourceforge.waters.model.base.WatersException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.ModuleCompiler;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.SafetyTraceProxy;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.marshaller.DocumentManager;
import net.sourceforge.waters.model.marshaller.JAXBModuleMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBProductDESMarshaller;
import net.sourceforge.waters.model.marshaller.JAXBTraceMarshaller;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import net.sourceforge.waters.junit.AbstractWatersTest;

import org.xml.sax.SAXException;


public class MazeTest extends AbstractWatersTest
{

  //#########################################################################
  //# Test Cases
  public void testMaze_matthew()
    throws IOException, JAXBException, WatersException
  {
    testMaze("matthew", true);
  }

  public void testMaze_maze1()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze1", true);
  }

  public void testMaze_maze2()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze2", true);
  }

  public void testMaze_maze3()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze3", true);
  }

  public void testMaze_maze4_3()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze4_3", true);
  }

  public void testMaze_maze4_6()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze4_6", true);
  }

  public void testMaze_maze5()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze5", true);
  }

  public void testMaze_maze6()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze6", true);
  }

  public void testMaze_maze7()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze7", true);
  }

  public void testMaze_maze8()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze8", true);
  }

  public void testMaze_maze9()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze9", true);
  }

  public void testMaze_maze10()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze10", true);
  }

  public void testMaze_maze11()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze11", true);
  }

  public void testMaze_maze12()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze12", true);
  }

  public void testMaze_maze13()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze13", true);
  }

  public void testMaze_maze14()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze14", true);
  }

  public void testMaze_maze14a()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze14a", true);
  }

  public void testMaze_maze16()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze16", true);
  }

  public void testMaze_mx01()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx01", true);
  }

  public void testMaze_mx02()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx02", true);
  }

  public void testMaze_mx03()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx03", true);
  }

  public void testMaze_mx04()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx04", true);
  }

  public void testMaze_mx05()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx05", true);
  }

  public void testMaze_mx06()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx06", true);
  }

  public void testMaze_mx07()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx07", true);
  }

  public void testMaze_mx09()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx09", true);
  }

  public void testMaze_mx10()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx10", true);
  }

  public void testMaze_mx12()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx12", true);
  }

  public void testMaze_mx13()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx13", true);
  }

  public void testMaze_mx14()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx14", true);
  }

  public void testMaze_mx15()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx15", true);
  }

  public void testMaze_mx16()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx16", true);
  }

  public void testMaze_mx18()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx18", true);
  }

  public void testMaze_mx20()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx20", true);
  }

  public void testMaze_mx21()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx21", true);
  }

  public void testMaze_mx22()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx22", false);
  }

  public void testMaze_mx23()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx23", true);
  }

  public void testMaze_mx24()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx24", true);
  }

  public void testMaze_mx25()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx25", true);
  }

  /*
  public void testMaze_mx26a()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx26a", false);
  }
  */

  public void testMaze_peter()
    throws IOException, JAXBException, WatersException
  {
    testMaze("peter", true);
  }

  public void testMaze_unsolvable1()
    throws IOException, JAXBException, WatersException
  {
    testMaze("unsolvable1", false);
  }

  public void testMaze_unsolvable2()
    throws IOException, JAXBException, WatersException
  {
    testMaze("unsolvable2", false);
  }

  public void testMaze_unsolvable3()
    throws IOException, JAXBException, WatersException
  {
    testMaze("unsolvable3", false);
  }

  public void testMaze_unsolvable4()
    throws IOException, JAXBException, WatersException
  {
    testMaze("unsolvable4", false);
  }


  //#########################################################################
  //# Utilities
  private void testMaze(final String name, final boolean solvable)
    throws IOException, JAXBException, WatersException
  {
    final String extname = name + mMazeCompiler.getDefaultExtension();
    final File filename = new File(mInputDirectory, extname);
    final URI uri = filename.toURI();
    final ModuleProxy module = mMazeCompiler.unmarshal(uri);
    final File outmodulefile = module.getFileLocation();
    final URI outmoduleuri = outmodulefile.toURI();
    mModuleMarshaller.marshal(module, outmodulefile);
    final ModuleProxy read =
      (ModuleProxy) mModuleMarshaller.unmarshal(outmoduleuri);
    assertTrue("Module changed after reading back in!",
               module.equalsWithGeometry(read));

    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    final ProductDESProxy des = compiler.compile();
    final File outdesfile = des.getFileLocation();
    mProductDESMarshaller.marshal(des, outdesfile);

    final ControllabilityChecker solver =
      new ControllabilityChecker(des, mProductDESFactory);
    final boolean controllable = solver.run();
    SafetyTraceProxy counterexample = null;
    if (!controllable) {
      counterexample = solver.getCounterExample();
      final String tracename = name + mTraceMarshaller.getDefaultExtension();
      final File dir = getOutputDirectory();
      final File tracefilename = new File(dir, tracename);
      mTraceMarshaller.marshal(counterexample, tracefilename);
    }
    if (solvable) {
      assertFalse("Solvable maze model is controllable!", controllable);
      final List<Point> moves = extractMoves(counterexample);
      final MazeSolutionChecker checker = new MazeSolutionChecker();
      final boolean solved = checker.checkSolution(uri, moves);
      assertTrue("Maze not solved after executing counterexample!", solved);
    } else {
      assertTrue("Unsolvable maze model is not controllable!", controllable);
    }
  }

  private List<Point> extractMoves(final SafetyTraceProxy counterexample)
  {
    final List<EventProxy> events = counterexample.getEvents();
    final int len = events.size();
    assertTrue("Empty counterexample!", len > 0);
    final List<Point> result = new ArrayList<Point>(len + 1);
    Point lasttarget = null;
    for (final EventProxy event : events) {
      final String name = event.getName();
      final String[] parts = name.split("_", 6);
      final int x1 = Integer.parseInt(parts[1]);
      final int y1 = Integer.parseInt(parts[2]);
      final int x2 = Integer.parseInt(parts[3]);
      final int y2 = Integer.parseInt(parts[4]);
      final Point source = new Point(x1, y1);
      final Point target = new Point(x2, y2);
      if (lasttarget == null) {
        result.add(source);
      } else {
        assertEquals(source, lasttarget);
      }
      result.add(target);
      lasttarget = target;
    }
    return result;
  }


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  protected void setUp()
    throws JAXBException, SAXException
  {
    mInputDirectory = new File(getInputRoot(), "maze");
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mTraceMarshaller = new JAXBTraceMarshaller(mProductDESFactory);
    mMazeCompiler = new MazeCompiler
      (mInputDirectory, mOutputDirectory, moduleFactory, mModuleMarshaller);
    mMazeCompiler.setUseLanguageInclusion(false);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mMazeCompiler);
    ensureDirectoryExists(mOutputDirectory);
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
  private JAXBTraceMarshaller mTraceMarshaller;
  private MazeCompiler mMazeCompiler;
  private DocumentManager mDocumentManager;

}
