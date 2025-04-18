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
import java.util.Map;
import java.util.TreeMap;

import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.ProxyVisitor;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.ScopeKind;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An immutable implementation of the {@link EventDeclProxy} interface.
 *
 * @author Robi Malik
 */

public final class EventDeclElement
  extends IdentifiedElement
  implements EventDeclProxy
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new event declaration.
   * @param identifier The identifier defining the name of the new event declaration.
   * @param kind The kind of the new event declaration.
   * @param observable The observability status of the new event declaration.
   * @param scope The scope of the new event declaration.
   * @param ranges The list of index ranges of the new event declaration, or <CODE>null</CODE> if empty.
   * @param colorGeometry The colour information of the new event declaration, or <CODE>null</CODE>.
   * @param attributes The attribute map of the new event declaration, or <CODE>null</CODE> if empty.
   */
  public EventDeclElement(final IdentifierProxy identifier,
                          final EventKind kind,
                          final boolean observable,
                          final ScopeKind scope,
                          final Collection<? extends SimpleExpressionProxy> ranges,
                          final ColorGeometryProxy colorGeometry,
                          final Map<String,String> attributes)
  {
    super(identifier);
    mKind = kind;
    mIsObservable = observable;
    mScope = scope;
    if (ranges == null) {
      mRanges = Collections.emptyList();
    } else {
      final List<SimpleExpressionProxy> rangesModifiable =
        new ArrayList<SimpleExpressionProxy>(ranges);
      mRanges =
        Collections.unmodifiableList(rangesModifiable);
    }
    mColorGeometry = colorGeometry;
    if (attributes == null) {
      mAttributes = Collections.emptyMap();
    } else {
      final Map<String,String> attributesModifiable =
        new TreeMap<String,String>(attributes);
      mAttributes =
        Collections.unmodifiableMap(attributesModifiable);
    }
  }

  /**
   * Creates a new event declaration using default values.
   * This constructor creates an event declaration with
   * the observability status set to <CODE>true</CODE>,
   * the scope set to <CODE>ScopeKind.LOCAL</CODE>,
   * an empty list of index ranges,
   * the colour information set to <CODE>null</CODE>, and
   * an empty attribute map.
   * @param identifier The identifier defining the name of the new event declaration.
   * @param kind The kind of the new event declaration.
   */
  public EventDeclElement(final IdentifierProxy identifier,
                          final EventKind kind)
  {
    this(identifier,
         kind,
         true,
         ScopeKind.LOCAL,
         null,
         null,
         null);
  }


  //#########################################################################
  //# Cloning
  @Override
  public EventDeclElement clone()
  {
    return (EventDeclElement) super.clone();
  }


  //#########################################################################
  //# Comparing
  public Class<EventDeclProxy> getProxyInterface()
  {
    return EventDeclProxy.class;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.base.Proxy
  public Object acceptVisitor(final ProxyVisitor visitor)
    throws VisitorException
  {
    final ModuleProxyVisitor downcast = (ModuleProxyVisitor) visitor;
    return downcast.visitEventDeclProxy(this);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.EventDeclProxy
  public EventKind getKind()
  {
    return mKind;
  }

  public boolean isObservable()
  {
    return mIsObservable;
  }

  public ScopeKind getScope()
  {
    return mScope;
  }

  public List<SimpleExpressionProxy> getRanges()
  {
    return mRanges;
  }

  public ColorGeometryProxy getColorGeometry()
  {
    return mColorGeometry;
  }

  public Map<String,String> getAttributes()
  {
    return mAttributes;
  }


  //#########################################################################
  //# Data Members
  private final EventKind mKind;
  private final boolean mIsObservable;
  private final ScopeKind mScope;
  private final List<SimpleExpressionProxy> mRanges;
  private final ColorGeometryProxy mColorGeometry;
  private final Map<String,String> mAttributes;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1576812251080253312L;

}
