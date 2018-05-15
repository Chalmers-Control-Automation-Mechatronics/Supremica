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

package net.sourceforge.waters.analysis.hisc;

import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.analysis.des.ConflictChecker;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstract base class for model verifier to check SIC or LDIC Properties
 * of HISC models that are based on a conflict check.
 *
 * The abstract base class provides the common configuration option to
 * configure the underlying conflict checker needed for different SIC
 * verification tasks.
 *
 * @author Robi Malik
 */

abstract public class AbstractSICConflictChecker
  extends AbstractConflictChecker
{

  //#########################################################################
  //# Constructors
  public AbstractSICConflictChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public AbstractSICConflictChecker(final ConflictChecker checker,
                                    final ProductDESProxyFactory factory)
  {
    this(checker, null, factory);
  }

  public AbstractSICConflictChecker(final ConflictChecker checker,
                                    final ProductDESProxy model,
                                    final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mChecker = checker;
  }


  //#########################################################################
  //# Configuration
  public ConflictChecker getConflictChecker()
  {
    return mChecker;
  }

  public void setConflictChecker(final ConflictChecker checker)
  {
    mChecker = checker;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.Abortable
  @Override
  public void requestAbort()
  {
    super.requestAbort();
    mChecker.requestAbort();
  }

  @Override
  public void resetAbort()
  {
    super.resetAbort();
    mChecker.resetAbort();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyser
  @Override
  public boolean supportsNondeterminism()
  {
    return mChecker.supportsNondeterminism();
  }


  //#########################################################################
  //# Data Members
  private ConflictChecker mChecker;

}
