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

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ModuleSequenceProxy;
import net.sourceforge.waters.plain.base.DocumentElement;


/**
 * An immutable implementation of the {@link ModuleSequenceProxy} interface.
 *
 * @author Robi Malik
 */

public final class ModuleSequenceElement
  extends DocumentElement
  implements ModuleSequenceProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new module sequence.
   * @param name The name of the new module sequence.
   * @param comment The comment of the new module sequence, or <CODE>null</CODE>.
   * @param location The location of the new module sequence.
   * @param modules The constant of the new module sequence, or <CODE>null</CODE> if empty.
   */
  public ModuleSequenceElement(final String name,
                               final String comment,
                               final URI location,
                               final Collection<? extends ModuleProxy> modules)
  {
    super(name, comment, location);
    if (modules == null) {
      mModules = Collections.emptyList();
    } else {
      final List<ModuleProxy> modulesModifiable =
        new ArrayList<ModuleProxy>(modules);
      mModules =
        Collections.unmodifiableList(modulesModifiable);
    }
  }

  /**
   * Creates a new module sequence using default values.
   * This constructor creates a module sequence with
   * the comment set to <CODE>null</CODE> and
   * an empty constant.
   * @param name The name of the new module sequence.
   * @param location The location of the new module sequence.
   */
  public ModuleSequenceElement(final String name,
                               final URI location)
  {
    this(name,
         null,
         location,
         null);
  }


  //#########################################################################
  //# Cloning
  @Override
  public ModuleSequenceElement clone()
  {
    return (ModuleSequenceElement) super.clone();
  }


  //#########################################################################
  //# Comparing
  public Class<ModuleSequenceProxy> getProxyInterface()
  {
    return ModuleSequenceProxy.class;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitModuleSequenceProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleSequenceProxy
  public List<ModuleProxy> getModules()
  {
    return mModules;
  }


  //#########################################################################
  //# Data Members
  private final List<ModuleProxy> mModules;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -2712334902205586198L;

}
