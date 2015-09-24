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

package net.sourceforge.waters.model.expr;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * An intermediate result of the expression parser that produces an
 * identifier ({@link SimpleIdentifierProxy} or {@link
 * IndexedIdentifierProxy}) object.
 *
 * @author Robi Malik
 */

class IdentifierResult extends ParseResult {

  //#########################################################################
  //# Constructors
  IdentifierResult(final String name, final List<ParseResult> indexes)
  {
    mName = name;
    mIndexes = indexes;
  }


  //#########################################################################
  //# Overrides for Abstract Baseclass
  //# net.sourceforge.waters.model.expr.ParseResult
  @Override
  int getTypeMask()
  {
    return Operator.TYPE_ANY;
  }

  @Override
  IdentifierProxy createProxy(final ModuleProxyFactory factory,
                              final String text)
  {
    final int size = mIndexes.size();
    if (size == 0) {
      return factory.createSimpleIdentifierProxy(text, mName);
    } else {
      final List<SimpleExpressionProxy> expressions =
        new ArrayList<SimpleExpressionProxy>(size);
      for (final ParseResult result : mIndexes) {
        final SimpleExpressionProxy expr = result.createProxy(factory);
        expressions.add(expr);
      }
      return factory.createIndexedIdentifierProxy(text, mName, expressions);
    }
  }


  //#########################################################################
  //# Data Members
  private final String mName;
  private final List<ParseResult> mIndexes;

}
