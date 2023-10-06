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

package net.sourceforge.waters.plain.module;

import java.util.Collection;

import net.sourceforge.waters.model.base.DuplicateNameException;
import net.sourceforge.waters.model.base.IndexedArraySet;
import net.sourceforge.waters.model.base.ItemNotFoundException;
import net.sourceforge.waters.model.base.NameNotFoundException;
import net.sourceforge.waters.model.module.NodeProxy;


class NodeSetElement extends IndexedArraySet<NodeProxy> {

  //#########################################################################
  //# Constructor
  NodeSetElement(final Collection<? extends NodeProxy> nodes)
    throws DuplicateNameException, ItemNotFoundException, NameNotFoundException
  {
    super(nodes.size());
    for (final NodeProxy node : nodes) {
      final Collection<NodeProxy> children = node.getImmediateChildNodes();
      checkAllUnique(children);
      insertUnique(node);
    }
  }

  //#########################################################################
  //# Overrides from base class IndexedArrayList
  protected ItemNotFoundException createItemNotFound(final String name)
  {
    return new ItemNotFoundException
      ("Graph does not contain the node named '" + name + "'!");
  }

  protected NameNotFoundException createNameNotFound(final String name)
  {
    return new NameNotFoundException
      ("Graph does not contain a node named '" + name + "'!");
  }

  protected DuplicateNameException createDuplicateName(final String name)
  {
    return new DuplicateNameException
      ("Graph already contains a node named '" + name + "'!");
  }


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
