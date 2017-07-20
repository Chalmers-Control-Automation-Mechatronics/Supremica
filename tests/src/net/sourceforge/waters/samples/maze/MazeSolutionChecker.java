//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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
