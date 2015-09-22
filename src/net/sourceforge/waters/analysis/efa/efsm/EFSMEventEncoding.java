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

package net.sourceforge.waters.analysis.efa.efsm;

import net.sourceforge.waters.analysis.efa.base.AbstractEFATransitionLabelEncoding;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.EventStatus;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMEventEncoding extends AbstractEFATransitionLabelEncoding<ConstraintList>
{

  //#########################################################################
  //# Constructors
  EFSMEventEncoding()
  {
    this(DEFAULT_SIZE);
  }

  EFSMEventEncoding(final int size)
  {
    super(size);
    createEventId(ConstraintList.TRUE);
  }

  EFSMEventEncoding(final EFSMEventEncoding encoding)
  {
    super(encoding);
  }

  public int getEventId(final ConstraintList update)
  {
    return super.getTransitionLabelId(update);
  }

  public ConstraintList getUpdate(final int event)
  {
    return super.getTransitionLabel(event);
  }

  public int createEventId(final ConstraintList update)
  {
    return super.createTransitionLabelId(update);
  }

  //#########################################################################
  //# Simple Access
  void setSelfloops(final ListBufferTransitionRelation rel,
                    final EFSMVariableFinder finder)
  {
    for (int e = EventEncoding.NONTAU; e < size(); e++) {
      final ConstraintList update = getUpdate(e);
      if (!finder.findPrime(update)) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus
          (e, status | EventStatus.STATUS_SELFLOOP_ONLY);
      }
    }
  }

}









