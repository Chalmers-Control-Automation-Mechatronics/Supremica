//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   NativeMazeTest
//###########################################################################
//# $Id: NativeMazeTest.java,v 1.3 2006-11-08 21:49:12 robi Exp $
//###########################################################################

package net.sourceforge.waters.samples.maze;

import net.sourceforge.waters.cpp.analysis.NativeLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class NativeMazeTest extends AbstractMazeTest
{

  //#########################################################################
  //# Overrides for net.sourceforge.waters.samples.maze.AbstractMazeTest
  protected LanguageInclusionChecker getLanguageInclusionChecker
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
  {
    return new NativeLanguageInclusionChecker(des, factory);
  }

}
