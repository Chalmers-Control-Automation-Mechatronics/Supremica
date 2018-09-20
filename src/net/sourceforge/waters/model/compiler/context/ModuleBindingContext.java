//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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


import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashSet;
import net.sourceforge.waters.model.base.ProxyAccessorSet;
import net.sourceforge.waters.model.expr.ExpressionScanner;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;


/**
 * <P>A binding context constructed when compiling a module.</P>
 *
 * <P>A module binding context contains a symbol map with the bindings of any
 * parameters and constant aliases used throughout the compilation of the
 * module.</P>
 *
 * <P>Special support is provided to create and retrieve bindings for
 * enumeration atoms. These are simple identifiers ({@link
 * SimpleIdentifierProxy}) encountered in the explicit or implicit
 * declarations of enumeration types. The are treated as ordinary symbols
 * bound to themselves in the binding map.</P>
 *
 * <P>If the module was compiled as an instance {@link
 * net.sourceforge.waters.model.module.InstanceProxy InstanceProxy} within
 * another module, information about the location of that instance is also
 * available.</P>
 *
 * @see BindingContext
 * @author Robi Malik
 */

public class ModuleBindingContext implements BindingContext
{

  //#########################################################################
  //# Constructors
  public ModuleBindingContext(final ModuleProxy module)
  {
    this(module, null, null);
  }

  public ModuleBindingContext(final ModuleProxy module,
                              final IdentifierProxy prefix,
                              final SourceInfo info)
  {
    final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
    mModule = module;
    mPrefix = prefix;
    mInstanceSource = info;
    mMap = new ProxyAccessorHashSet<>(eq);
    mEnumAtoms = new ArrayList<>();
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.compiler.BindingContext
  @Override
  public SimpleExpressionProxy getBoundExpression
    (final SimpleExpressionProxy ident)
  {
    final ProxyAccessor<SimpleExpressionProxy> key =
      mMap.createAccessor(ident);
    return mMap.get(key);
  }

  @Override
  public boolean isEnumAtom(final IdentifierProxy ident)
  {
    if (ident instanceof SimpleIdentifierProxy) {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      final SimpleExpressionProxy bound = getBoundExpression(ident);
      return eq.equals(ident, bound);
    } else {
      return false;
    }
  }

  @Override
  public ModuleBindingContext getModuleBindingContext()
  {
    return this;
  }


  //#########################################################################
  //# Simple Access
  /**
   * Gets the module that was compiled.
   */
  public ModuleProxy getModule()
  {
    return mModule;
  }

  /**
   * Gets the prefix attached to all identifiers in the compiled module.
   * @return An {@link IdentifierProxy} encoding the prefix, or
   *         <CODE>null</CODE>, if the module was not obtained by
   *         instantiation.
   */
  public IdentifierProxy getPrefix()
  {
    return mPrefix;
  }

  /**
   * Gets the source information record from which the module has been
   * instantiated.
   * @return A {@link SourceInfo} record pointing to an {@link
   *         net.sourceforge.waters.model.module.InstanceProxy
   *         InstanceProxy}, or <CODE>null</CODE>, if the module was not
   *         obtained by instantiation.
   */
  public SourceInfo getInstanceSource()
  {
    return mInstanceSource;
  }


  //#########################################################################
  //# Compilation
  public void insertBinding(final SimpleExpressionProxy ident,
                            final SimpleExpressionProxy value)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<SimpleExpressionProxy> key =
      mMap.createAccessor(ident);
    if (mMap.containsKey(key)) {
      final String name = ident.toString();
      throw new DuplicateIdentifierException(name);
    } else {
      mMap.put(key, value);
    }
  }

  public void insertEnumAtom(final SimpleIdentifierProxy ident)
    throws DuplicateIdentifierException, TypeMismatchException
  {
    ExpressionScanner.checkWatersIdentifier(ident);
    final ProxyAccessor<SimpleExpressionProxy> key = mMap.createAccessor(ident);
    final SimpleExpressionProxy expr = mMap.get(key);
    if (expr == null) {
      mMap.put(key, ident);
      mEnumAtoms.add(ident);
    } else {
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      if (!eq.equals(ident, expr)) {
        final String name = ident.toString();
        throw new DuplicateIdentifierException(name);
      }
    }
  }

  public List<SimpleIdentifierProxy> getEnumAtoms()
  {
    return mEnumAtoms;
  }


  //#########################################################################
  //# Data Members
  private final ModuleProxy mModule;
  private final IdentifierProxy mPrefix;
  private final SourceInfo mInstanceSource;
  private final ProxyAccessorSet<SimpleExpressionProxy> mMap;
  private final List<SimpleIdentifierProxy> mEnumAtoms;

}
