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

package net.sourceforge.waters.gui.transfer;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.waters.model.module.EdgeProxy;


/**
 * A data flavour representing a collection of edges ({@link EdgeProxy}) of a
 * graph. Edges have a separate data flavour to support the insertion of edges
 * with source and target nodes already in the target graph. In contrast,
 * {@link GraphDataFlavor} supports copy and past of subgraphs with nodes and
 * edges found in the transferable.
 *
 * @author Robi Malik
 */

class EdgeDataFlavor extends ModuleDataFlavor
{

  //#########################################################################
  //# Constructor
  EdgeDataFlavor()
  {
    super(EdgeProxy.class);
  }


  //#########################################################################
  //# Importing and Exporting Data
  @Override
  List<WatersDataFlavor> reduceDataFlavorList
    (final List<WatersDataFlavor> flavors)
  {
    final int size = flavors.size();
    final List<WatersDataFlavor> reduced =
      new ArrayList<WatersDataFlavor>(size);
    for (final WatersDataFlavor flavor : flavors) {
      if (flavor != WatersDataFlavor.GRAPH) {
        reduced.add(flavor);
      }
    }
    return reduced;
  }

}
