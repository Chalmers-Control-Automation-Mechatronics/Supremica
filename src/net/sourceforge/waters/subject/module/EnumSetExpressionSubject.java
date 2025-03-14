//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# THIS FILE HAS BEEN AUTOMATICALLY GENERATED BY A SCRIPT.
//# DO NOT EDIT.
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

package net.sourceforge.waters.subject.module;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.base.ArrayListSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.RecursiveUndoInfo;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.UndoInfo;


/**
 * The subject implementation of the {@link EnumSetExpressionProxy} interface.
 *
 * @author Robi Malik
 */

public final class EnumSetExpressionSubject
  extends SimpleExpressionSubject
  implements EnumSetExpressionProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new enumerated range.
   * @param plainText The original text of the new enumerated range, or <CODE>null</CODE>.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionSubject(final String plainText,
                                  final Collection<? extends SimpleIdentifierProxy> items)
  {
    super(plainText);
    if (items == null) {
      mItems = new ArrayListSubject<SimpleIdentifierSubject>();
    } else {
      mItems = new ArrayListSubject<SimpleIdentifierSubject>
        (items, SimpleIdentifierSubject.class);
    }
    mItems.setParent(this);
  }

  /**
   * Creates a new enumerated range using default values.
   * This constructor creates an enumerated range with
   * the original text set to <CODE>null</CODE>.
   * @param items The list of items of the new enumerated range, or <CODE>null</CODE> if empty.
   */
  public EnumSetExpressionSubject(final Collection<? extends SimpleIdentifierProxy> items)
  {
    this(null,
         items);
  }


  //#########################################################################
  //# Cloning and Assigning
  @Override
  public EnumSetExpressionSubject clone()
  {
    final ModuleProxyCloner cloner =
      ModuleSubjectFactory.getCloningInstance();
    return (EnumSetExpressionSubject) cloner.getClone(this);
  }

  @Override
  protected void collectUndoInfo(final ProxySubject newState,
                                 final RecursiveUndoInfo info,
                                 final Set<? extends Subject> boundary)
  {
    super.collectUndoInfo(newState, info, boundary);
    final EnumSetExpressionSubject downcast =
      (EnumSetExpressionSubject) newState;
    final UndoInfo step2 = mItems.createUndoInfo(downcast.mItems, boundary);
    if (step2 != null) {
      info.add(step2);
    }
  }


  //#########################################################################
  //# Comparing
  public Class<EnumSetExpressionProxy> getProxyInterface()
  {
    return EnumSetExpressionProxy.class;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitEnumSetExpressionProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.EnumSetExpressionProxy
  public List<SimpleIdentifierProxy> getItems()
  {
    final List<?> precast = mItems;
    @SuppressWarnings("unchecked")
    final List<SimpleIdentifierProxy> downcast =
      (List<SimpleIdentifierProxy>) precast;
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Gets the modifiable list of items in this enumeration.
   */
  public ListSubject<SimpleIdentifierSubject> getItemsModifiable()
  {
    return mItems;
  }


  //#########################################################################
  //# Data Members
  private ListSubject<SimpleIdentifierSubject> mItems;

}
