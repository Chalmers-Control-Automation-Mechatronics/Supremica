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

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.OverflowException;
import net.sourceforge.waters.model.des.EventProxy;


/**
 * <P>The synchronous product algorithm. A synchronous product builder
 * takes a finite-state machine model ({@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy}) as input
 * and computes a single automaton representing the synchronous product
 * of all automata contained in the input model.</P>
 *
 * @author Robi Malik
 */

public interface SynchronousProductBuilder
  extends AutomatonBuilder
{

  //#########################################################################
  //# More Specific Access to the Results
  @Override
  public SynchronousProductResult getAnalysisResult();


  //#########################################################################
  //# Parameterisation
  /**
   * Defines the set of propositions to be retained in the synchronous
   * product. If specified, only the events from the given proposition set
   * will be copied to the output automaton, all others will be ignored.
   * @param  props       The set of propositions to be retained,
   *                     or <CODE>null</CODE> to keep all propositions.
   * @throws OverflowException to indicate that the number of propositions
   *                     exceeds the maximum supported by the underlying
   *                     data structures.
   */
  public void setPropositions(final Collection<EventProxy> props)
    throws OverflowException;

  /**
   * Gets the set of propositions retained in the synchronous product.
   * @see #setPropositions(Collection) setPropositions()
   */
  public Collection<EventProxy> getPropositions();

  /**
   * Specifies an event mask for hiding. Events can be masked or hidden
   * by specifying a set of events to be masked and a replacement event.
   * When creating transitions of the output automaton, all events in the
   * mask will be replaced by the specified event. This method can be called
   * multiply; in this case, the result is undefined if the specified event
   * sets are not disjoint.
   * @param  hidden      A set of events to be replaced.
   * @param  replacement An event to be used instead of any of the hidden
   *                     events.
   * @throws OverflowException to indicate that the number of propositions
   *                     exceeds the maximum supported by the underlying
   *                     data structures.
   */
  public void addMask(final Collection<EventProxy> hidden,
                      final EventProxy replacement) throws OverflowException;

  /**
   * Resets all event masks. This method clears any masks set by the
   * {@link #addMask(Collection,EventProxy) addMask()} method,
   * so any further computation is done without hiding.
   */
  public void clearMask();

}








