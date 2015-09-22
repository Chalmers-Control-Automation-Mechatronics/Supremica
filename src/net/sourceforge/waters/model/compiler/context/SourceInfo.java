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

package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.base.Proxy;


/**
 * <P>A link between the input and the output of the {@link
 * net.sourceforge.waters.model.compiler.ModuleCompiler ModuleCompiler}.</P>
 *
 * <P>Each item in the compiler output is associated with a source
 * information record that enables the user to trace from which item in the
 * original module the compiled item was produced, and which bindings were
 * used. More specifically, the items of a {@link
 * net.sourceforge.waters.model.des.ProductDESProxy ProductDESProxy} are
 * associated with <CODE>SourceInfo</CODE> records as follows.</P>
 *
 * <UL>
 * <LI>Each {@link net.sourceforge.waters.model.des.AutomatonProxy
 *     AutomatonProxy} object has a record pointing to the {@link
 *     net.sourceforge.waters.model.module.SimpleComponentProxy
 *     SimpleComponentProxy} or {@link
 *     net.sourceforge.waters.model.module.VariableComponentProxy
 *     VariableComponentProxy} object from which it was produced.</LI>
 * <LI>Each {@link net.sourceforge.waters.model.des.StateProxy
 *     StateProxy} object has a record pointing to the {@link
 *     net.sourceforge.waters.model.module.SimpleNodeProxy SimpleNodeProxy}
 *     object from which it was produced.</LI>
 * <LI>Each {@link net.sourceforge.waters.model.des.TransitionProxy
 *     TransitionProxy} object has a record pointing to a {@link
 *     net.sourceforge.waters.model.module.SimpleExpressionProxy
 *     SimpleExpressionProxy} object in the {@link
 *     net.sourceforge.waters.model.module.LabelBlockProxy LabelBlockProxy}
 *     of the edge from which the transition was produced.</LI>
 * <LI>Each {@link net.sourceforge.waters.model.des.EventProxy
 *     EventProxy} object has a record pointing to the {@link
 *     net.sourceforge.waters.model.module.EventDeclProxy EventDeclProxy}
 *     object from which it was produced.</LI>
 * </UL>
 *
 * @author Robi Malik
 */

public class SourceInfo
{

  //#########################################################################
  //# Constructor
  public SourceInfo(final Proxy proxy, final BindingContext context)
  {
    mSourceObject = proxy;
    mBindingContext = context;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the item in the source module associated with this source
   * information record.
   */
  public Proxy getSourceObject()
  {
    return mSourceObject;
  }

  /**
   * Gets the binding context that indicates any variable substitutions,
   * bindings, or aliases applied to the source object.
   */
  public BindingContext getBindingContext()
  {
    return mBindingContext;
  }


  //#########################################################################
  //# Navigation
  /**
   * Gets a parent of this source information record that indicates a
   * location within a label block in a graph of the compiled module.
   * Due to aliasing, the source information of a compiled transition
   * does not necessarily indicate a position within a graph. In such cases,
   * this method can be used to navigate the contexts and find the closest
   * location within a graph (which represents the item that a simulator
   * would want to highlight).
   * @see AliasBindingContext
   */
  public SourceInfo getGraphSourceInfo()
  {
    if (mBindingContext instanceof AliasBindingContext) {
      final AliasBindingContext alias = (AliasBindingContext) mBindingContext;
      final SourceInfo info = alias.getAliasSource();
      return info.getGraphSourceInfo();
    } else {
      return this;
    }
  }

  /**
   * Gets the closed graph item linked to this source information record.
   * This method is equivalent to calling {@link #getGraphSourceInfo()}
   * followed by {@link #getSourceObject()}.
   */
  public Proxy getGraphSourceObject()
  {
    final SourceInfo info = getGraphSourceInfo();
    if (info == this)
      return this.getSourceObject();
    else
      return info.getGraphSourceObject();
  }


  //#########################################################################
  //# Data Members
  private final Proxy mSourceObject;
  private final BindingContext mBindingContext;

}









