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

package net.sourceforge.waters.subject.base;

import java.util.Collection;

/**
 * <P>The common interface for all Waters elements.</P>
 *
 * @author Robi Malik
 */

public interface Subject {

  //#########################################################################
  //# Hierarchy
  /**
   * Gets the parent of this subject.
   * @return The parent of this subject, or <CODE>null</CODE> if this
   *         subject does not have any parent.
   */
  public Subject getParent();

  /**
   * Gets the document this subject belongs to. The document is the
   * root of the parent hierarchy, i.e., the ancestor that does not
   * have a parent anymore.
   * @return The document of this subject, or <CODE>null</CODE> if this
   *         subject or some of its parents has not yet been added to any
   *         document.
   */
  public DocumentSubject getDocument();

  /**
   * Sets the parent of this subject can be set to the given new value.
   * A non-<CODE>null</CODE> parent can only be assigned to a subject
   * that does not have a parent assigned yet.
   * @param  parent  The new parent to be assigned, or <CODE>null</CODE>
   *                 to reset the subject's parent.
   * @throws IllegalStateException to indicate that the <CODE>parent</CODE>
   *                 argument is not <CODE>null</CODE> and this subject
   *                 already has a non-<CODE>null</CODE> parent.
   */
  public void setParent(Subject parent);

  /**
   * Checks whether the parent of this subject can be set to the given
   * new value. A new parent can only be assigned to a subject that
   * does not have a parent assigned yet. This method can be used to
   * check whether a call to {@link #setParent(Subject) setParent()}
   * will throw an exception.
   * @param  parent  The new parent to be tested.
   * @throws IllegalStateException if the <CODE>parent</CODE> argument
   *                 is not <CODE>null</CODE> and this subject already
   *                 has a non-<CODE>null</CODE> parent.
   */
  public void checkSetParent(Subject parent);


  //#########################################################################
  //# Assignment
  public ModelChangeEvent assignMember(int index,
                                       Object oldValue,
                                       Object newValue);


  //#########################################################################
  //# Observers
  public void addModelObserver(ModelObserver observer);

  public void removeModelObserver(ModelObserver observer);

  public Collection<ModelObserver> getModelObservers();

}
