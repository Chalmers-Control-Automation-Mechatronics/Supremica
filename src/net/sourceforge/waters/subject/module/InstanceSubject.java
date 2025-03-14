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
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.subject.base.IndexedArrayListSubject;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.base.RecursiveUndoInfo;
import net.sourceforge.waters.subject.base.ReplacementUndoInfo;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.UndoInfo;


/**
 * The subject implementation of the {@link InstanceProxy} interface.
 *
 * @author Robi Malik
 */

public final class InstanceSubject
  extends ComponentSubject
  implements InstanceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new instance.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   * @param bindingList The binding list of the new instance, or <CODE>null</CODE> if empty.
   */
  public InstanceSubject(final IdentifierProxy identifier,
                         final String moduleName,
                         final Collection<? extends ParameterBindingProxy> bindingList)
  {
    super(identifier);
    mModuleName = moduleName;
    if (bindingList == null) {
      mBindingList = new IndexedArrayListSubject<ParameterBindingSubject>();
    } else {
      mBindingList = new IndexedArrayListSubject<ParameterBindingSubject>
        (bindingList, ParameterBindingSubject.class);
    }
    mBindingList.setParent(this);
  }

  /**
   * Creates a new instance using default values.
   * This constructor creates an instance with
   * an empty binding list.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   */
  public InstanceSubject(final IdentifierProxy identifier,
                         final String moduleName)
  {
    this(identifier,
         moduleName,
         null);
  }


  //#########################################################################
  //# Cloning and Assigning
  @Override
  public InstanceSubject clone()
  {
    final ModuleProxyCloner cloner =
      ModuleSubjectFactory.getCloningInstance();
    return (InstanceSubject) cloner.getClone(this);
  }

  @Override
  public ModelChangeEvent assignMember(final int index,
                                       final Object oldValue,
                                       final Object newValue)
  {
    if (index <= 1) {
      return super.assignMember(index, oldValue, newValue);
    } else {
      switch (index) {
      case 2:
        mModuleName = (String) newValue;
        return ModelChangeEvent.createStateChanged(this);
      default:
        return null;
      }
    }
  }

  @Override
  protected void collectUndoInfo(final ProxySubject newState,
                                 final RecursiveUndoInfo info,
                                 final Set<? extends Subject> boundary)
  {
    super.collectUndoInfo(newState, info, boundary);
    final InstanceSubject downcast = (InstanceSubject) newState;
    if (!mModuleName.equals(downcast.mModuleName)) {
      final UndoInfo step2 =
        new ReplacementUndoInfo(2, mModuleName, downcast.mModuleName);
      info.add(step2);
    }
    final UndoInfo step3 =
      mBindingList.createUndoInfo(downcast.mBindingList, boundary);
    if (step3 != null) {
      info.add(step3);
    }
  }


  //#########################################################################
  //# Comparing
  public Class<InstanceProxy> getProxyInterface()
  {
    return InstanceProxy.class;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitInstanceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.InstanceProxy
  public String getModuleName()
  {
    return mModuleName;
  }

  public List<ParameterBindingProxy> getBindingList()
  {
    final List<?> precast = mBindingList;
    @SuppressWarnings("unchecked")
    final List<ParameterBindingProxy> downcast =
      (List<ParameterBindingProxy>) precast;
    return Collections.unmodifiableList(downcast);
  }


  //#########################################################################
  //# Setters
  /**
   * Sets the module name of this instance.
   */
  public void setModuleName(final String moduleName)
  {
    if (mModuleName.equals(moduleName)) {
      return;
    }
    mModuleName = moduleName;
    fireStateChanged();
  }

  /**
   * Gets the modifiable binding list of this instance.
   */
  public IndexedListSubject<ParameterBindingSubject> getBindingListModifiable()
  {
    return mBindingList;
  }


  //#########################################################################
  //# Data Members
  private String mModuleName;
  private IndexedListSubject<ParameterBindingSubject> mBindingList;

}
