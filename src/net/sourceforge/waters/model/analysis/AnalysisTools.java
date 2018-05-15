//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.analysis.monolithic.MonolithicModelAnalyzerFactory;
import net.sourceforge.waters.cpp.analysis.NativeModelVerifierFactory;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.analysis.des.EventNotFoundException;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;


/**
 * A collection of static methods to perform analysis operations.
 *
 * @author Robi Malik
 */

public final class AnalysisTools
{

  //########################################################################
  //# Invocation
  /**
   * Checks whether the given automaton is nonblocking.
   */
  public static boolean isNonBlocking(final AutomatonProxy aut)
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    final ProductDESProxy des =
      AutomatonTools.createProductDESProxy(aut, factory);
    try {
      final ConflictChecker checker = getDefaultConflictChecker(des);
      return checker.run();
    } catch (final EventNotFoundException exception) {
      return true;
    } catch (final AnalysisException exception) {
      throw exception.getRuntimeException();
    }
  }


  //########################################################################
  //# Auxiliary Methods
  private static ConflictChecker getDefaultConflictChecker
    (final ProductDESProxy des)
    throws AnalysisConfigurationException
  {
    final ProductDESProxyFactory desFactory =
      ProductDESElementFactory.getInstance();
    ConflictChecker checker;
    try {
      final ModelAnalyzerFactory vFactory =
        NativeModelVerifierFactory.getInstance();
      checker = vFactory.createConflictChecker(desFactory);
    } catch (final UnsatisfiedLinkError | AnalysisConfigurationException error) {
      final ModelAnalyzerFactory vFactory =
        MonolithicModelAnalyzerFactory.getInstance();
      checker = vFactory.createConflictChecker(desFactory);
    }
    checker.setModel(des);
    return checker;
  }
}
