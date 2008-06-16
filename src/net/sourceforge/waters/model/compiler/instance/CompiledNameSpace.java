//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.instance
//# CLASS:   CompiledNameSpace
//###########################################################################
//# $Id: CompiledNameSpace.java,v 1.1 2008-06-16 07:09:51 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.compiler.instance;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.compiler.context.
  DuplicateIdentifierException;
import net.sourceforge.waters.model.compiler.context.
  UndefinedIdentifierException;
import net.sourceforge.waters.model.expr.EvalException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.printer.ProxyPrinter;

class CompiledNameSpace
{

  //#########################################################################
  //# Constructor
  CompiledNameSpace()
  {
    this(null, null);
  }

  CompiledNameSpace(final IdentifierProxy ident,
		    final CompiledNameSpace parent)
  {
    mIdentifier = ident;
    mParent = parent;
    mNameSpaceMap =
      new HashMap<ProxyAccessor<IdentifierProxy>,CompiledNameSpace>();
    mComponentMap =
      new HashMap<ProxyAccessor<IdentifierProxy>,IdentifiedProxy>();
    mEventMap =
      new HashMap<String,CompiledEvent>();
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

  CompiledNameSpace getNameSpace(final IdentifierProxy ident)
  {
    try {
      return NAMESPACE_LOOKUP_VISITOR.getNameSpace(this, ident, false);
    } catch (final EvalException exception) {
      throw exception.getRuntimeException();
    }
  }

  CompiledNameSpace findNameSpace(final IdentifierProxy ident)
    throws EvalException
  {
    return NAMESPACE_LOOKUP_VISITOR.getNameSpace(this, ident, true);
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

  void addNameSpace(final IdentifierProxy ident,
		    final CompiledNameSpace subspace)
    throws EvalException
  {
    NAMESPACE_ADD_VISITOR.addNameSpace(this, ident, subspace);
  }

  IdentifierProxy getPrefixedIdentifier(final IdentifierProxy ident,
                                        final ModuleProxyFactory factory)
  {
    if (mParent == null) {
      return ident;
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
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    final IdentifiedProxy comp = mComponentMap.get(accessor);
    if (throwing && comp == null) {
      final String prefixed = getPrefixedName(ident);
      throw new UndefinedIdentifierException(prefixed, "component", null);
    } else {
      return comp;
    }
  }

  private CompiledNameSpace lookupNameSpace(final IdentifierProxy ident,
					    final boolean throwing)
    throws UndefinedIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    final CompiledNameSpace namespace = mNameSpaceMap.get(accessor);
    if (throwing && namespace == null) {
      final String prefixed = getPrefixedName(ident);
      throw new UndefinedIdentifierException(prefixed, "namespace", null);
    } else {
      return namespace;
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
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    final IdentifiedProxy old = mComponentMap.get(accessor);
    if (old == null) {
      return mComponentMap.put(accessor, comp);
    } else {
      final String prefixed = getPrefixedName(ident);
      throw new DuplicateIdentifierException(prefixed, "component", null);
    }
  }

  private Object putNameSpace(final IdentifierProxy ident,
			      final CompiledNameSpace subspace)
    throws DuplicateIdentifierException
  {
    final ProxyAccessor<IdentifierProxy> accessor =
      new ProxyAccessorByContents<IdentifierProxy>(ident);
    final CompiledNameSpace old = mNameSpaceMap.get(accessor);
    if (old == null) {
      return mNameSpaceMap.put(accessor, subspace);
    } else {
      final String prefixed = getPrefixedName(ident);
      throw new DuplicateIdentifierException(prefixed, "namespace", null);
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
      writer.write('.');
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
      writer.write('.');
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
      writer.write('.');
      ProxyPrinter.printProxy(writer, mIdentifier);
    }
  }


  //#########################################################################
  //# Inner Class EventLookupVisitor
  private static class EventLookupVisitor extends AbstractModuleProxyVisitor
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
    public CompiledEvent visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	final String name = ident.getName();
	final List<SimpleExpressionProxy> indexes = ident.getIndexes();
	CompiledEvent event = mNameSpace.lookupEvent(name, mThrowing);
	for (final SimpleExpressionProxy index : indexes) {
	  event = event.find(index);
	}
	return event;
      } catch (final EvalException exception) {
	throw wrap(exception);
      }
    }

    public CompiledEvent visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	final IdentifierProxy base = ident.getBaseIdentifier();
	mNameSpace =
	  NAMESPACE_LOOKUP_VISITOR.getNameSpace(mNameSpace, base, mThrowing);
	final IdentifierProxy comp = ident.getComponentIdentifier();
	return (CompiledEvent) comp.acceptVisitor(this);
      } catch (final EvalException exception) {
	throw wrap(exception);
      }
    }

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
    extends AbstractModuleProxyVisitor
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

    public IdentifiedProxy visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	final IdentifierProxy base = ident.getBaseIdentifier();
	mNameSpace =
	  NAMESPACE_LOOKUP_VISITOR.getNameSpace(mNameSpace, base, mThrowing);
	final IdentifierProxy comp = ident.getComponentIdentifier();
	return (IdentifiedProxy) comp.acceptVisitor(this);
      } catch (final EvalException exception) {
	throw wrap(exception);
      }
    }

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
    extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private CompiledNameSpace getNameSpace(final CompiledNameSpace namespace,
					   final IdentifierProxy ident,
					   final boolean throwing)
      throws EvalException
    {
      try {
	mNameSpace = namespace;
	mThrowing = throwing;
	return (CompiledNameSpace) ident.acceptVisitor(this);
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
    public CompiledNameSpace visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	return mNameSpace.lookupNameSpace(ident, mThrowing);
      } catch (final UndefinedIdentifierException exception) {
	throw wrap(exception);
      }
    }

    public CompiledNameSpace visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      mNameSpace = (CompiledNameSpace) base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return (CompiledNameSpace) comp.acceptVisitor(this);
    }

    public CompiledNameSpace visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
	return mNameSpace.lookupNameSpace(ident, mThrowing);
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
  //# Inner Class EventAddVisitor
  private static class EventAddVisitor extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private void addEvent(final CompiledNameSpace namespace,
                          final IdentifierProxy ident,
                          final CompiledEvent event)
      throws EvalException
    {
      try {
	mNameSpace = namespace;
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

    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	final IdentifierProxy base = ident.getBaseIdentifier();
	mNameSpace =
	  NAMESPACE_LOOKUP_VISITOR.getNameSpace(mNameSpace, base, true);
	final IdentifierProxy comp = ident.getComponentIdentifier();
	return comp.acceptVisitor(this);
      } catch (final EvalException exception) {
	throw wrap(exception);
      }
    }

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
  private static class ComponentAddVisitor extends AbstractModuleProxyVisitor
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

    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	final IdentifierProxy base = ident.getBaseIdentifier();
	mNameSpace =
	  NAMESPACE_LOOKUP_VISITOR.getNameSpace(mNameSpace, base, true);
	final IdentifierProxy comp = ident.getComponentIdentifier();
	return comp.acceptVisitor(this);
      } catch (final EvalException exception) {
	throw wrap(exception);
      }
    }

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
  //# Inner Class NameSpaceAddVisitor
  private static class NameSpaceAddVisitor extends AbstractModuleProxyVisitor
  {
    //#######################################################################
    //# Invocation
    private void addNameSpace(final CompiledNameSpace namespace,
			      final IdentifierProxy ident,
			      final CompiledNameSpace subspace)
      throws EvalException
    {
      try {
	mNameSpace = namespace;
	mSubSpace = subspace;
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
    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	return mNameSpace.putNameSpace(ident, mSubSpace);
      } catch (final DuplicateIdentifierException exception) {
	throw wrap(exception);
      }
    }

    public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
      throws VisitorException
    {
      try {
	final IdentifierProxy base = ident.getBaseIdentifier();
	mNameSpace =
	  NAMESPACE_LOOKUP_VISITOR.getNameSpace(mNameSpace, base, true);
	final IdentifierProxy comp = ident.getComponentIdentifier();
	return comp.acceptVisitor(this);
      } catch (final EvalException exception) {
	throw wrap(exception);
      }
    }

    public Object visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy ident)
      throws VisitorException
    {
      try {
	return mNameSpace.putNameSpace(ident, mSubSpace);
      } catch (final DuplicateIdentifierException exception) {
	throw wrap(exception);
      }
    }

    //#######################################################################
    //# Data Members
    private CompiledNameSpace mNameSpace;
    private CompiledNameSpace mSubSpace;
  }


  //#########################################################################
  //# Data Members
  private final IdentifierProxy mIdentifier;
  private final CompiledNameSpace mParent;
  private final Map<String,CompiledEvent>
    mEventMap;
  private final Map<ProxyAccessor<IdentifierProxy>,IdentifiedProxy>
    mComponentMap;
  private final Map<ProxyAccessor<IdentifierProxy>,CompiledNameSpace>
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
  private static final NameSpaceAddVisitor NAMESPACE_ADD_VISITOR =
    new NameSpaceAddVisitor();

}
