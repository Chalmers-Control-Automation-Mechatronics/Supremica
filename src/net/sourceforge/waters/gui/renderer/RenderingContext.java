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

package net.sourceforge.waters.gui.renderer;

import java.awt.Font;

import net.sourceforge.waters.gui.util.PropositionIcon;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.subject.base.ModelChangeEvent;


public interface RenderingContext
{

  //#########################################################################
  //# Methods
  /**
   * Gets a font for the display of the given identifier.
   * This method is used to provide different fonts for event labels that are
   * known to be controllable or uncontrollable.
   * This method is expected to return the same font each time it is called
   * for the same input; to change the font for an identifier, the identifier
   * must be removed from shape producer's cache.
   */
  public Font getFont(IdentifierProxy ident);

  /**
   * Gets rendering information for the display of the given item.
   * The rendering information provides the colours and highlighting status
   * for an item to be displayed. This method is <I>not</I> required to
   * return the same result when called multiply with the same input.
   */
  public RenderingInformation getRenderingInformation(Proxy proxy);

  /**
   * Gets colour information for the display of the given simple node in
   * the given graph.
   * This method is used to obtain proposition colours for node with marking
   * propositions.
   * This method is expected to return the same colour information each time
   * it is called for the same input; to change the colours for a node, the
   * node must be removed from shape producer's cache.
   */
  public PropositionIcon.ColorInfo getMarkingColorInfo(GraphProxy graph,
                                                       SimpleNodeProxy node);

  /**
   * Returns whether given event may cause the proposition status of
   * the given graph to change. The proposition status of a graph
   * indicates whether the graph uses any propositions, i.e., whether
   * states without propositions are rendered with a filled or a
   * transparent background.
   */
  public boolean causesPropositionStatusChange(ModelChangeEvent event,
                                               GraphProxy graph);

}
