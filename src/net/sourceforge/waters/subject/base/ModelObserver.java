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

package net.sourceforge.waters.subject.base;

/**
 * The callback interface for changes in modifiable Waters data.
 * A model observer is registered on a {@link Subject} using {@link
 * Subject#addModelObserver(ModelObserver) addModelObserver()} to be called
 * whenever the data of the subject or one of its children gets modified
 * in some way.
 *
 * @see Subject
 * @see ModelChangeEvent
 *
 * @author Robi Malik
 */

public interface ModelObserver {

  /**
   * Notifies the recipient of a change to the observed {@link Subject}.
   * @param  event    A model change event containing information about
   *                  what specific change was made to the model.
   */
  public void modelChanged(ModelChangeEvent event);

  /**
   * Gets the priority of this model observer. If more than one model
   * observer is registered with the same subject, the priority determines
   * the order in which they are called. Observers with a smaller number
   * as their priority are called first. For observer with equal priority,
   * the order is unspecified.
   */
  public int getModelObserverPriority();


  /**
   * Constant defining very high priority.
   */
  public static final int CLEANUP_PRIORITY_0 = 0;

  /**
   * Constant defining high priority.
   */
  public static final int CLEANUP_PRIORITY_1 = 10;

  /**
   * Constant defining medium priority.
   */
  public static final int DEFAULT_PRIORITY = 20;

  /**
   * Constant defining low priority.
   */
  public static final int RENDERING_PRIORITY = 30;

}
