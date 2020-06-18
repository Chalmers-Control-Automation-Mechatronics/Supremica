//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.compiler.context;


import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * Context information to show how an item was instantiated during
 * compilation. A binding context basically maps names to their bound
 * values. In addition, it provides access to a module context ({@link
 * ModuleBindingContext}) that can provide further information if the item
 * was compiled as part of an instance ({@link
 * net.sourceforge.waters.model.module.InstanceProxy InstanceProxy}).
 *
 * @see SourceInfo
 * @author Robi Malik
 */

public interface BindingContext
{

  /**
   * Gets the value bound to the given name.
   * @param  ident   The name to be looked up.
   *                 It can be identifier, simple or indexed, (to support
   *                 array lookups), or a unary expression (to support
   *                 next-state value of EFA variables.
   * @return A variable-free expression representing the concrete value
   *         bound to the given name in this context, or <CODE>null</CODE>
   *         if there is no binding for the given name.
   */
  public SimpleExpressionProxy getBoundExpression(SimpleExpressionProxy ident);

  /**
   * Determines whether the given identifier represents an enumeration atom
   * in this context.
   */
  public boolean isEnumAtom(IdentifierProxy ident);

  /**
   * Gets the closest ancestor of this that is a module binding context.
   * @return A module binding context, which may be this context itself.
   */
  public ModuleBindingContext getModuleBindingContext();

}
