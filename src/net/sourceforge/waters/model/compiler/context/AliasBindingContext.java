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

package net.sourceforge.waters.model.compiler.context;

import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * <P>A binding context constructed when compiling an alias.</P>
 *
 * <P>A alias binding context contains a reference to another source
 * information record ({@link SourceInfo}), identifying a location
 * within a compiled alias declaration.</P>
 *
 * <P>When an alias symbol in a label block is compiled, each generated
 * transition has a source information record ({@link SourceInfo})
 * indicating the location of the elementary event label within the
 * compiled alias declaration and with an <CODE>AliasBindingContext</CODE>
 * as context, which contains another source information record ({@link
 * SourceInfo}). That context's source information record indicates the
 * location of the alias symbol that was replaced, which may be an
 * occurrence on actual edge, or in the body of another alias
 * declaration.</P>
 *
 * <P>Thus, the source object of a compiled transition's source information
 * record ({@link SourceInfo}) does not necessarily indicate a location in
 * a graph edge of the original model. To get a location within a graph, the
 * source information's {@link SourceInfo#getGraphSourceInfo()
 * getGraphSourceInfo()} method can be used.</P>
 *
 * @see BindingContext
 * @see SourceInfo
 * @author Robi Malik
 */

public class AliasBindingContext implements BindingContext
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new alias binding context.
   * @param  info   The source information of the alias declaration
   *                that is being replaced.
   * @param  parent The parent binding context with additional bindings
   *                for the aliased symbol before its replacement.
   */
  public AliasBindingContext(final SourceInfo info,
                             final BindingContext parent)
  {
    mAliasSource = info;
    mParent = parent;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    return mParent.getBoundExpression(ident);
  }

  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    return mParent.isEnumAtom(ident);
  }

  public ModuleBindingContext getModuleBindingContext()
  {
    return mParent.getModuleBindingContext();
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the source information record from where the alias has been
   * instantiated.
   * @return A {@link SourceInfo} record pointing to the {@link
   *         net.sourceforge.waters.model.module.SimpleExpressionProxy
   *         SimpleExpressionProxy} of the alias identifier that
   *         was replaced.
   */
  public SourceInfo getAliasSource()
  {
    return mAliasSource;
  }

  /**
   * Gets the parent binding context of this alias binding.
   * The parent context may provide additional bindings
   * for the aliased symbol after its replacement.
   */
  public BindingContext getParent()
  {
    return mParent;
  }


  //#########################################################################
  //# Data Members
  private final SourceInfo mAliasSource;
  private final BindingContext mParent;

}
