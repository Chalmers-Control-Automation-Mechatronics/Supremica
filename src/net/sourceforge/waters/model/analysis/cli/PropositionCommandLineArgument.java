//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.analysis.cli;

import java.util.Collection;
import java.util.ListIterator;

import net.sourceforge.waters.analysis.options.Configurable;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.des.AbstractConflictChecker;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;


/**
 * @author Robi Malik
 */
public class PropositionCommandLineArgument
  extends CommandLineArgument<EventProxy>
{

  //#######################################################################
  //# Constructor
  public PropositionCommandLineArgument(final Option<EventProxy> option)
  {
    super(option);
  }


  //#######################################################################
  //# Simple Access
  @Override
  protected String getArgumentTemplate()
  {
    return "<name>";
  }


  //#######################################################################
  //# Parsing
  @Override
  public void parse(final CommandLineOptionContext context,
                    final Collection<Configurable> configurables,
                    final ListIterator<String> iter)
    throws AnalysisException
  {
    iter.remove();
    if (iter.hasNext()) {
      mName = iter.next();
      iter.remove();
      setUsed(true);
      updateContext(context);
    } else {
      failMissingValue();
    }
  }

  @Override
  public void updateContext(final CommandLineOptionContext context)
    throws AnalysisException
  {
    if (isUsed()) {
      final ProductDESProxy des = context.getProductDES();
      if (des != null) {
        final EventProxy marking =
          AbstractConflictChecker.findMarkingProposition(des, mName);
        final Option<EventProxy> option = getOption();
        option.setValue(marking);
      }
    }
  }


  //#######################################################################
  //# Data Members
  private String mName;

}
