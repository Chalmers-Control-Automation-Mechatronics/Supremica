//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2017 Robi Malik
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

package net.sourceforge.waters.model.compiler.instance;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorHashMap;
import net.sourceforge.waters.model.base.ProxyAccessorMap;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.DefaultModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;

class CompiledNameSpace
{

  //#########################################################################
  //# Constructor
  CompiledNameSpace(final ModuleEqualityVisitor equality)
  {
    mIdentifier = null;
    mParent = null;
    mEquality = equality;
    mNameSpaceMap = new ProxyAccessorHashMap<>(equality);
    mComponentMap = new ProxyAccessorHashMap<>(equality);
    mEventMap = new HashMap<>();
  }

  CompiledNameSpace(final IdentifierProxy ident,
                    final CompiledNameSpace parent)
  {
    mIdentifier = ident;
    mParent = parent;
    mEquality = parent.getEquality();
    mNameSpaceMap = new ProxyAccessorHashMap<>(mEquality);
    mComponentMap = new ProxyAccessorHashMap<>(mEquality);
    mEventMap = new HashMap<>();
  }


  //#########################################################################
  //# Simple Access
  ModuleEqualityVisitor getEquality()
  {
    return mEquality;
  }


  //#########################################################################
  //# Name Space Access
  CompiledEvent getEvent(final IdentifierProxy ident)
  {
    try {
      return EVENT_LOOKUP_VISITOR.getEvent(this, ident, false);
    } catch (final EvalException exception) {
      throw exception.getRuntimeException();
    }
  }

  CompiledEvent findEvent(final IdentifierProxy ident)
    throws EvalException
  {
    return EVENT_LOOKUP_VISITOR.getEvent(this, ident, true);
  }

  IdentifiedProxy getComponent(final IdentifierProxy ident)
  {
    try {
      return COMPONENT_LOOKUP_VISITOR.getComponent(this, ident, false);
    } catch (final EvalException exception) {
      throw exception.getRuntimeException();
    }
  }

  IdentifiedProxy findComponent(final IdentifierProxy ident)
    throws EvalException
  {
    return COMPONENT_LOOKUP_VISITOR.getComponent(this, ident, true);
  }

  CompiledNameSpace getOrAddNameSpace(final IdentifierProxy ident)
  {
    return NAMESPACE_LOOKUP_VISITOR.getOrAddNameSpace(this, ident);
  }

  CompiledNameSpace getOrAddChildNameSpace(final IdentifierProxy suffix)
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      mNameSpaceMap.createAccessor(suffix);
    CompiledNameSpace nameSpace = mNameSpaceMap.get(accessor);
    if (nameSpace == null) {
      nameSpace = new CompiledNameSpace(suffix, this);
      mNameSpaceMap.put(accessor, nameSpace);
    }
    return nameSpace;
  }

  void addEvent(final IdentifierProxy ident, final CompiledEvent event)
    throws EvalException
  {
    EVENT_ADD_VISITOR.addEvent(this, ident, event);
  }

  void addComponent(final IdentifierProxy ident, final IdentifiedProxy comp)
    throws EvalException
  {
    COMPONENT_ADD_VISITOR.addComponent(this, ident, comp);
  }

  IdentifierProxy getPrefixedIdentifier(final IdentifierProxy ident,
                                        final ModuleProxyFactory factory)
  {
    if (mParent == null) {
      final ModuleProxyCloner cloner = factory.getCloner();
      return (IdentifierProxy) cloner.getClone(ident);
    } else {
      final IdentifierProxy base =
        mParent.getPrefixedIdentifier(mIdentifier, factory);
      return factory.createQualifiedIdentifierProxy(base, ident);
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private CompiledEvent lookupEvent(final String name, final boolean throwing)
    throws UndefinedIdentifierException
  {
    final CompiledEvent event = mEventMap.get(name);
    if (throwing && event == null) {
      final String prefixed = getPrefixedName(name);
      throw new UndefinedIdentifierException(prefixed, "event", null);
    } else {
      return event;
    }
  }

  private IdentifiedProxy lookupComponent(final IdentifierProxy ident,
                                          final boolean throwing)
    throws UndefinedIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      mComponentMap.createAccessor(ident);
    final IdentifiedProxy comp = mComponentMap.get(accessor);
    if (throwing && comp == null) {
      final String prefixed = getPrefixedName(ident);
      throw new UndefinedIdentifierException(prefixed, "component", null);
    } else {
      return comp;
    }
  }

  private Object putEvent(final String name, final CompiledEvent event)
    throws DuplicateIdentifierException
  {
    final CompiledEvent old = mEventMap.get(name);
    if (old == null) {
      return mEventMap.put(name, event);
    } else {
      final String prefixed = getPrefixedName(name);
      throw new DuplicateIdentifierException(prefixed, "event", null);
    }
  }

  private Object putComponent(final IdentifierProxy ident,
                              final IdentifiedProxy comp)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      mComponentMap.createAccessor(ident);
    final IdentifiedProxy old = mComponentMap.get(accessor);
    if (old == null) {
      return mComponentMap.put(accessor, comp);
    } else {
      final String prefixed = getPrefixedName(ident);
      throw new DuplicateIdentifierException(prefixed, "component", null);
    }
  }

  private CompiledArrayAlias createArrayAlias(final String name)
    throws DuplicateIdentifierException
  {
    final CompiledEvent event = mEventMap.get(name);
    if (event == null) {
      final CompiledArrayAlias alias = new CompiledArrayAlias(name);
      mEventMap.put(name, alias);
      return alias;
    } else if (event instanceof CompiledArrayAlias) {
      return (CompiledArrayAlias) event;
    } else {
      final String prefixed = getPrefixedName(name);
      throw new DuplicateIdentifierException(prefixed, "event", null);
    }
  }

  private String getPrefixedName(final String name)
  {
    try {
      final StringWriter writer = new StringWriter();
      appendName(writer);
      writer.write(name);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  private String getPrefixedName(final IdentifierProxy ident)
  {
    try {
      final StringWriter writer = new StringWriter();
      appendName(writer);
      ProxyPrinter.printProxy(writer, ident);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  private void appendName(final StringWriter writer)
    throws IOException
  {
    if (mParent != null) {
      mParent.appendName(writer);
      ProxyPrinter.printProxy(writer, mIdentifier);
      writer.write('.');
    }
  }


  //#########################################################################
  //# Inner Class EventLookupVisitor
  private static class EventLookupVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private CompiledEvent getEvent(final CompiledNameSpace namespace,
                                   final IdentifierProxy ident,
                                   final boolean throwing)
      throws EvalException
    {
      try {
        mNameSpace = namespace;
        mThrowing = throwing;
        return (CompiledEvent) ident.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (throwing && cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public CompiledEvent visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final String name = ident.getName();
        //System.err.println("name=" + name);
        final List<SimpleExpressionProxy> indexes = ident.getIndexes();
        CompiledEvent event = mNameSpace.lookupEvent(name, mThrowing);
        //System.err.println("event=" + event);
        if (event != null) {
          for (final SimpleExpressionProxy index : indexes) {
            //System.err.println("index=" + index);
            event = event.find(index);
            //System.err.println("event=" + event);
          }
        }
        return event;
      } catch (final EvalException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public CompiledEvent visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      mNameSpace =
        NAMESPACE_LOOKUP_VISITOR.getOrAddNameSpace(mNameSpace, base);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return (CompiledEvent) comp.acceptVisitor(this);
    }

    @Override
    public CompiledEvent visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final String name = ident.getName();
        return mNameSpace.lookupEvent(name, mThrowing);
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private CompiledNameSpace mNameSpace;
    private boolean mThrowing;
  }


  //#########################################################################
  //# Inner Class ComponentLookupVisitor
  private static class ComponentLookupVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private IdentifiedProxy getComponent(final CompiledNameSpace namespace,
                                         final IdentifierProxy ident,
                                         final boolean throwing)
      throws EvalException
    {
      try {
        mNameSpace = namespace;
        mThrowing = throwing;
        return (IdentifiedProxy) ident.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (throwing && cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public IdentifiedProxy visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        return mNameSpace.lookupComponent(ident, mThrowing);
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public IdentifiedProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      final CompiledNameSpace namespace =
        NAMESPACE_LOOKUP_VISITOR.getOrAddNameSpace(mNameSpace, base);
      if (namespace == null) {
        return null;
      }
      mNameSpace = namespace;
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return (IdentifiedProxy) comp.acceptVisitor(this);
    }

    @Override
    public IdentifiedProxy visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
        return mNameSpace.lookupComponent(ident, mThrowing);
      } catch (final UndefinedIdentifierException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private CompiledNameSpace mNameSpace;
    private boolean mThrowing;
  }


  //#########################################################################
  //# Inner Class NameSpaceLookupVisitor
  private static class NameSpaceLookupVisitor
    extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private CompiledNameSpace getOrAddNameSpace(final CompiledNameSpace nameSpace,
                                                final IdentifierProxy ident)
    {
      try {
        mNameSpace = nameSpace;
        return (CompiledNameSpace) ident.acceptVisitor(this);
      } catch (final VisitorException exception) {
        throw exception.getRuntimeException();
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public CompiledNameSpace visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
    {
      return mNameSpace.getOrAddChildNameSpace(ident);
    }

    @Override
    public CompiledNameSpace visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      mNameSpace = (CompiledNameSpace) base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return (CompiledNameSpace) comp.acceptVisitor(this);
    }

    @Override
    public CompiledNameSpace visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
    {
      return mNameSpace.getOrAddChildNameSpace(ident);
    }

    //#######################################################################
    //# Data Members
    private CompiledNameSpace mNameSpace;
  }


  //#########################################################################
  //# Inner Class EventAddVisitor
  private static class EventAddVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private void addEvent(final CompiledNameSpace nameSpace,
                          final IdentifierProxy ident,
                          final CompiledEvent event)
      throws EvalException
    {
      try {
        mNameSpace = nameSpace;
        mEvent = event;
        ident.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final String name = ident.getName();
        final List<SimpleExpressionProxy> indexes = ident.getIndexes();
        final CompiledArrayAlias alias = mNameSpace.createArrayAlias(name);
        alias.set(indexes, mEvent);
        return null;
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      mNameSpace =
        NAMESPACE_LOOKUP_VISITOR.getOrAddNameSpace(mNameSpace, base);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return comp.acceptVisitor(this);
    }

    @Override
    public Object visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
        final String name = ident.getName();
        return mNameSpace.putEvent(name, mEvent);
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private CompiledNameSpace mNameSpace;
    private CompiledEvent mEvent;
  }


  //#########################################################################
  //# Inner Class ComponentAddVisitor
  private static class ComponentAddVisitor extends DefaultModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private void addComponent(final CompiledNameSpace namespace,
                              final IdentifierProxy ident,
                              final IdentifiedProxy comp)
      throws EvalException
    {
      try {
        mNameSpace = namespace;
        mComponent = comp;
        ident.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof EvalException) {
          throw (EvalException) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    @Override
    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
        return mNameSpace.putComponent(ident, mComponent);
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    @Override
    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      mNameSpace =
        NAMESPACE_LOOKUP_VISITOR.getOrAddNameSpace(mNameSpace, base);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return comp.acceptVisitor(this);
    }

    @Override
    public Object visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
        return mNameSpace.putComponent(ident, mComponent);
      } catch (final DuplicateIdentifierException exception) {
        throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private CompiledNameSpace mNameSpace;
    private IdentifiedProxy mComponent;
  }


  //#########################################################################
  //# Data Members
  private final IdentifierProxy mIdentifier;
  private final CompiledNameSpace mParent;
  private final ModuleEqualityVisitor mEquality;
  private final Map<String,CompiledEvent>
    mEventMap;
  private final ProxyAccessorMap<IdentifierProxy,IdentifiedProxy>
    mComponentMap;
  private final ProxyAccessorMap<IdentifierProxy,CompiledNameSpace>
    mNameSpaceMap;


  //#########################################################################
  //# Static Class Constants
  private static final EventLookupVisitor EVENT_LOOKUP_VISITOR =
    new EventLookupVisitor();
  private static final ComponentLookupVisitor COMPONENT_LOOKUP_VISITOR =
    new ComponentLookupVisitor();
  private static final NameSpaceLookupVisitor NAMESPACE_LOOKUP_VISITOR =
    new NameSpaceLookupVisitor();

  private static final EventAddVisitor EVENT_ADD_VISITOR =
    new EventAddVisitor();
  private static final ComponentAddVisitor COMPONENT_ADD_VISITOR =
    new ComponentAddVisitor();

}
