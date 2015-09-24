//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBException;

import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.VerificationResult;
import net.sourceforge.waters.model.analysis.des.LanguageInclusionChecker;
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


public abstract class AbstractMazeTest extends AbstractWatersTest
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

  public void testMaze_maze4_10()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze4_10", true);
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

  public void testMaze_maze15()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze15", true);
  }

  public void testMaze_maze15a()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze15a", true);
  }

  public void testMaze_maze15b()
    throws IOException, JAXBException, WatersException
  {
    testMaze("maze15b", true);
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

  public void testMaze_mx08()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx08", true);
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

  public void testMaze_mx11()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx11", true);
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

  public void testMaze_mx17()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx16", true);
  }

  public void testMaze_mx18()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx18", true);
  }

  public void testMaze_mx19()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx19", false);
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

  public void testMaze_mx26()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx26", true);
  }

  public void testMaze_mx26a()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx26a", false);
  }

  public void testMaze_mx27()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx27", true);
  }

  public void testMaze_mx28()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx28", true);
  }

  public void testMaze_mx29()
    throws IOException, JAXBException, WatersException
  {
    testMaze("mx29", true);
  }

  public void testMaze_peter()
    throws IOException, JAXBException, WatersException
  {
    testMaze("peter", true);
  }

  public void testMaze_twokeys()
    throws IOException, JAXBException, WatersException
  {
    testMaze("twokeys", true);
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
    final ModuleProxy module = mMazeCompiler.unmarshalCopying(uri);
    final File outmodulefile = module.getFileLocation();
    final URI outmoduleuri = outmodulefile.toURI();
    final ModuleProxy read =
      (ModuleProxy) mModuleMarshaller.unmarshal(outmoduleuri);
    assertModuleProxyEquals("Module changed after reading back in!",
                            read, module);

    final ModuleCompiler compiler =
      new ModuleCompiler(mDocumentManager, mProductDESFactory, module);
    final ProductDESProxy des = compiler.compile();
    final File outdesfile = des.getFileLocation();
    mProductDESMarshaller.marshal(des, outdesfile);

    final VerificationResult result =
      checkLanguageInclusion(des, mProductDESFactory);
    final boolean controllable = result.isSatisfied();
    SafetyTraceProxy counterexample = null;
    if (!controllable) {
      counterexample = (SafetyTraceProxy) result.getCounterExample();
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

  private VerificationResult checkLanguageInclusion
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
    throws AnalysisException
  {
    final LanguageInclusionChecker checker =
      getLanguageInclusionChecker(des, factory);
    checker.run();
    return checker.getAnalysisResult();
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
  //# Provided by Subclasses
  protected abstract LanguageInclusionChecker getLanguageInclusionChecker
    (ProductDESProxy des, ProductDESProxyFactory factory);


  //#########################################################################
  //# Overrides for junit.framework.TestCase
  @Override
  protected void setUp()
    throws Exception
  {
    super.setUp();
    mInputDirectory = new File(getWatersInputRoot(), "maze");
    mOutputDirectory = getOutputDirectory();
    final ModuleProxyFactory moduleFactory =
      ModuleElementFactory.getInstance();
    mProductDESFactory = ProductDESElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mModuleMarshaller = new JAXBModuleMarshaller(moduleFactory, optable);
    mProductDESMarshaller = new JAXBProductDESMarshaller(mProductDESFactory);
    mTraceMarshaller = new JAXBTraceMarshaller(mProductDESFactory);
    // mMazeCompiler.setUseLanguageInclusion(false);
    mDocumentManager = new DocumentManager();
    mDocumentManager.registerMarshaller(mModuleMarshaller);
    mDocumentManager.registerMarshaller(mProductDESMarshaller);
    mDocumentManager.registerUnmarshaller(mModuleMarshaller);
    mDocumentManager.registerUnmarshaller(mProductDESMarshaller);
    mMazeCompiler =
      new MazeCompiler(mOutputDirectory, moduleFactory, mDocumentManager);
    ensureDirectoryExists(mOutputDirectory);
  }

  @Override
  protected void tearDown()
    throws Exception
  {
    mInputDirectory = null;
    mOutputDirectory = null;
    mProductDESFactory = null;
    mModuleMarshaller = null;
    mProductDESMarshaller = null;
    mMazeCompiler = null;
    mDocumentManager = null;
    super.tearDown();
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
