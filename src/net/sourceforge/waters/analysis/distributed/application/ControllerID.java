//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

package net.sourceforge.waters.analysis.distributed.application;

import java.io.Serializable;

/**
 * A controller ID is used to uniquely identify a controller instance to
 * nodes, for example, to clean up any allocated worker objects.
 * 
 * ControllerID implements the equals/hashCode contract in a way that works
 * correctly when the object is serialised, that is to say it does not depend
 * on reference equality.
 *
 * The constructor for this class is package-local; instances should only be
 * created by the server.
 *
 * @author Sam Douglas
 */
public final class ControllerID implements Serializable
{
  ControllerID(String id)
  {
    mId = id;
  }

  public boolean equals(Object o)
  {
    if (o == null) return false;
    if (!(o instanceof ControllerID)) return false;
    return mId.equals(((ControllerID)o).mId);
  }
  public int hashCode()
  {
    return mId.hashCode();
  }

  public String toString()
  {
    return mId;
  }

  private final String mId;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
}
