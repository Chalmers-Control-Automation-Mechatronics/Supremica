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

package net.sourceforge.waters.model.module;

import java.util.Map;
import java.util.Set;

import net.sourceforge.waters.model.base.NamedProxy;


/**
 * <P>The base class for all nodes.</P>
 *
 * <P>This class serves as a common base for simple nodes ({@link
 * SimpleNodeProxy}) and group nodes ({@link GroupNodeProxy}) and
 * extends some functionality common to both classes.</P>
 *
 * <P>All nodes can be associated with <I>propositions</I>. Propositions
 * are a particular type of event used to define properties of nodes.  The
 * common application to define <I>marked</I> or <I>terminal</I> states is
 * achieved by associating each node to be marked with a particular
 * proposition, e.g., <CODE>:omega</CODE>. The general node structure
 * supports a list of proposition in order to facilitate several marking
 * conditions to check a model for mutual nonblocking conditions or to
 * perform CTL model checking.</P>
 *
 * @author Robi Malik
 */

public interface NodeProxy extends NamedProxy {

  //#########################################################################
  //# Getters and Setters
  /**
   * Gets the list of propositions of this node.
   * @return An event list that defines the proposition events for this
   *         node.
   */
  // @default empty
  public PlainEventListProxy getPropositions();

  /**
   * Gets the set of immediate child nodes of this group node.
   * This method returns the set of simple nodes or group nodes
   * that are directly contained in this group node.
   * @return An unmodifiable set of nodes.
   */
  public Set<NodeProxy> getImmediateChildNodes();

  /**
   * Gets the attribute map for this node.
   * The attribute map can be used by tools supporting external model
   * formats to store information that does not appear in standard DES
   * models.
   * @return An immutable map mapping attribute names to values.
   */
  public Map<String,String> getAttributes();

}
