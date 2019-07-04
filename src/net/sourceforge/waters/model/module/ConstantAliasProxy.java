//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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

/**
 * <P>An alias representing a constant definition.</P>
 *
 * <P>A constant alias is used to assign a simple constant to a name.  The
 * name of a constant alias must be a simple identifier ({@link
 * SimpleIdentifierProxy}).</P>
 *
 * <P>A constant alias may be a <I>parameter</I>, in which case the value
 * in the declaration only is a default that can be overridden by
 * instantiation from another module. A parameter can be <I>optional</i> or
 * <I>required</I>: for required parameters, a value must be provided when
 * instantiating. Nevertheless, even required parameters must have a
 * default value, which is used when compiling the module on its own.</P>
 *
 * @author Robi Malik
 */

public interface ConstantAliasProxy extends AliasProxy {

  /**
   * Gets the scope of this alias declaration.
   * The scope defines whether this alias actually is a parameter.
   * @return One of {@link ScopeKind#LOCAL},
   *         {@link ScopeKind#OPTIONAL_PARAMETER}, or
   *         {@link ScopeKind#REQUIRED_PARAMETER}.
   */
  // @default ScopeKind.LOCAL
  public ScopeKind getScope();

}
