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

package net.sourceforge.waters.analysis.tr;


/**
 * An iterator over a collection of <CODE>long</CODE> values.
 *
 * @author Robi Malik
 */

public interface WatersLongIterator
{

  /**
   * Resets the iteration to start iterating over the underlying collection
   * again.
   */
  public void reset();

  /**
   * Advances iteration.
   * This method moves the iterator one step forward.
   * It must be called before trying to access the list using the
   * {@link #getCurrentData()}.
   * @return <CODE>true</CODE> if the iteration contains another element
   *         that is now accessible, or <CODE>false</CODE> if the end of
   *         the list has been reached.
   */
  public boolean advance();

  /**
   * Retrieves the data element at the current position of this iterator.
   */
  public long getCurrentData();

  /**
   * Deletes the data element at the current position of this iterator
   * from the underlying collection. Optional operation.
   */
  public void remove();

}
