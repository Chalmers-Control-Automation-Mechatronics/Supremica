//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.samples.maze
//# CLASS:   MonolithicMazeTest
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.samples.maze;

import net.sourceforge.waters.analysis.monolithic.
  MonolithicLanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class MonolithicMazeTest extends AbstractMazeTest
{

  //#########################################################################
  //# Overrides for net.sourceforge.waters.samples.maze.AbstractMazeTest
  protected LanguageInclusionChecker getLanguageInclusionChecker
    (final ProductDESProxy des, final ProductDESProxyFactory factory)
  {
    return new MonolithicLanguageInclusionChecker(des, factory);
  }

}
