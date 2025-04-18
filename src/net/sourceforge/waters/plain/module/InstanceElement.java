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

package net.sourceforge.waters.plain.module;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ParameterBindingProxy;


/**
 * An immutable implementation of the {@link InstanceProxy} interface.
 *
 * @author Robi Malik
 */

public final class InstanceElement
  extends ComponentElement
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
  public InstanceElement(final IdentifierProxy identifier,
                         final String moduleName,
                         final Collection<? extends ParameterBindingProxy> bindingList)
  {
    super(identifier);
    mModuleName = moduleName;
    if (bindingList == null) {
      mBindingList = Collections.emptyList();
    } else {
      final List<ParameterBindingProxy> bindingListModifiable =
        new ArrayList<ParameterBindingProxy>(bindingList);
      mBindingList =
        Collections.unmodifiableList(bindingListModifiable);
    }
  }

  /**
   * Creates a new instance using default values.
   * This constructor creates an instance with
   * an empty binding list.
   * @param identifier The identifier defining the name of the new instance.
   * @param moduleName The module name of the new instance.
   */
  public InstanceElement(final IdentifierProxy identifier,
                         final String moduleName)
  {
    this(identifier,
         moduleName,
         null);
  }


  //#########################################################################
  //# Cloning
  @Override
  public InstanceElement clone()
  {
    return (InstanceElement) super.clone();
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
    return mBindingList;
  }


  //#########################################################################
  //# Data Members
  private final String mModuleName;
  private final List<ParameterBindingProxy> mBindingList;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1495666764808319468L;

}
