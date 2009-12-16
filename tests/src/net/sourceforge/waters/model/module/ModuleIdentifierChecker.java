//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.subject.module
//# CLASS:   ModuleIdentifierChecker
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.module;

import java.util.Collection;

import junit.framework.Assert;

import net.sourceforge.waters.model.base.NamedProxy;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.module.ModuleElementFactory;


public class ModuleIdentifierChecker
  extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  public static ModuleIdentifierChecker getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static final ModuleIdentifierChecker INSTANCE =
      new ModuleIdentifierChecker();
  }


  //#########################################################################
  //# Constructors
  protected ModuleIdentifierChecker()
  {
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    mParser = new ExpressionParser(factory, optable);
  }


  //#########################################################################
  //# Invocation
  public void check(final Proxy proxy)
  {
    try {
      proxy.acceptVisitor(this);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ProxyVisitor
  public Object visitProxy(final Proxy proxy)
  {
    return null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
  {
    final Collection<Proxy> body = foreach.getBody();
    visitCollection(body);
    return null;
  }

  public Object visitGraphProxy(final GraphProxy graph)
    throws VisitorException
  {
    final Collection<NodeProxy> nodes = graph.getNodes();
    visitCollection(nodes);
    return null;
  }

  public Object visitIdentifiedProxy(final IdentifiedProxy proxy)
    throws VisitorException
  {
    mNameOwner = proxy;
    final IdentifierProxy identifier = proxy.getIdentifier();
    return identifier.acceptVisitor(this);
  }

  public Object visitIndexedIdentifierProxy(final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final String name = ident.getName();
    checkName(name);
    return null;
  }

  public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
  {
    final Collection<ConstantAliasProxy> constants =
      module.getConstantAliasList();
    visitCollection(constants);
    final Collection<EventDeclProxy> events = module.getEventDeclList();
    visitCollection(events);
    final Collection<Proxy> aliases = module.getEventAliasList();
    visitCollection(aliases);
    final Collection<Proxy> components = module.getComponentList();
    visitCollection(components);
    return null;
  }

  public Object visitNodeProxy(final NodeProxy node)
    throws VisitorException
  {
    mNameOwner = node;
    final String name = node.getName();
    checkName(name);
    return null;
  }

  public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy ident)
    throws VisitorException
  {
    final IdentifierProxy base = ident.getBaseIdentifier();
    base.acceptVisitor(this);
    final IdentifierProxy comp = ident.getComponentIdentifier();
    comp.acceptVisitor(this);
    return null;
  }

  public Object visitSimpleComponentProxy(final SimpleComponentProxy comp)
    throws VisitorException
  {
    visitComponentProxy(comp);
    mCurrentComponent = comp;
    final GraphProxy graph = comp.getGraph();
    visitGraphProxy(graph);
    mCurrentComponent = null;
    return null;
  }

  public Object visitSimpleIdentifierProxy(final SimpleIdentifierProxy ident)
    throws VisitorException
  {
    final String name = ident.getName();
    checkName(name);
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void checkName(final String name)
  {
    try {
      mParser.parseSimpleIdentifier(name);
    } catch (final ParseException exception) {
      final StringBuffer buffer = new StringBuffer();
      buffer.append("The name '");
      buffer.append(name);
      buffer.append("' of a ");
      buffer.append(getShortClassName(mNameOwner));
      if (mCurrentComponent != null) {
        buffer.append(" in the component '");
        buffer.append(mCurrentComponent.getName());
        buffer.append("'");
      }
      buffer.append(" is not a proper Waters identifier!");
      Assert.fail(buffer.toString());
    }
  }

  private String getShortClassName(final Proxy proxy)
  {
    final Class<? extends Proxy> clazz = proxy.getProxyInterface();
    final String fullclazzname = clazz.getName();
    final int dotpos = fullclazzname.lastIndexOf('.');
    return fullclazzname.substring(dotpos + 1);
  }


  //#########################################################################
  //# Data Members
  private final ExpressionParser mParser;

  private NamedProxy mNameOwner;
  private SimpleComponentProxy mCurrentComponent;

}
