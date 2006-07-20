//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MazeSolutionChecker
//###########################################################################
//# $Id: MazeSolutionChecker.java,v 1.2 2006-07-20 02:28:38 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import java.awt.Point;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import junit.framework.Assert;


public class MazeSolutionChecker
{

  //#########################################################################
  //# Constructors
  public MazeSolutionChecker()
  {
    mReader = new MazeReader();
  }


  //#########################################################################
  //# Checking Maze Solutions
  public boolean checkSolution(final File filename,
                               final List<Point> solution)
    throws IOException
  {
    final URI uri = filename.toURI();
    return checkSolution(uri, solution);
  }

  public boolean checkSolution(final URI uri,
                               final List<Point> solution)
    throws IOException
  {
    final String pathname = uri.getPath();
    final int start1 = pathname.lastIndexOf(File.separatorChar);
    final int start2 = start1 < 0 ? 0 : start1 + 1;
    final int stop1 = pathname.lastIndexOf('.');
    final int stop2 = stop1 < 0 ? pathname.length() : stop1;
    final String name = pathname.substring(start2, stop2);
    final Maze maze = mReader.load(uri, name);
    return checkSolution(maze, solution);
  }

  private boolean checkSolution(final Maze maze,
                                final List<Point> solution)
  {
    final int len = solution.size();
    Assert.assertTrue("Solution too short!", len >= 2);
    boolean solved = false;
    for (int i = 0; i < len - 1; i++) {
      Assert.assertFalse("Maze already solved at step " + i +
                         ", but given trace contains more moves!",
                         solved);
      final Point source = solution.get(i);
      final Point target = solution.get(i + 1);
      solved = maze.applyMove(source, target);
    }
    return solved;
  }


  //#########################################################################
  //# Data Members
  private final MazeReader mReader;

}
