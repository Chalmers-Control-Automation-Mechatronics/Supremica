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
import java.util.List;

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
  extends ModuleIntegrityChecker
{

  //#########################################################################
  //# Singleton Pattern
  public static ModuleIdentifierChecker getModuleIdentifierCheckerInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder
  {
    private static final ModuleIdentifierChecker INSTANCE =
      new ModuleIdentifierChecker();
  }


  //#########################################################################
  //# Constructor
  private ModuleIdentifierChecker()
  {
    mVisitor = new IdentifierVisitor();
  }


  //#########################################################################
  //# Invocation
  public void check(final ModuleProxy module)
    throws Exception
  {
    super.check(module);
    mVisitor.check(module);
  }


  //#########################################################################
  //# Inner Class IdentifierVisitor
  private static class IdentifierVisitor
    extends AbstractModuleProxyVisitor
  {

    //#######################################################################
    //# Constructors
    private IdentifierVisitor()
    {
      final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
      final OperatorTable optable = CompilerOperatorTable.getInstance();
      mParser = new ExpressionParser(factory, optable);
    }


    //#######################################################################
    //# Invocation
    public void check(final Proxy proxy)
      throws Exception
    {
      try {
        proxy.acceptVisitor(this);
      } catch (final VisitorException exception) {
        final Throwable cause = exception.getCause();
        if (cause instanceof Exception) {
          throw (Exception) cause;
        } else {
          throw exception.getRuntimeException();
        }
      }
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ProxyVisitor
    public Object visitProxy(final Proxy proxy)
    {
      return null;
    }


    //#######################################################################
    //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
    public Object visitEdgeProxy(final EdgeProxy edge)
    throws VisitorException
    {
      final EventListExpressionProxy block = edge.getLabelBlock();
      return visitEventListExpressionProxy(block);
    }

    public Object visitEventListExpressionProxy
      (final EventListExpressionProxy expr)
    throws VisitorException
    {
      final List<Proxy> elist = expr.getEventList();
      visitCollection(elist);
      return null;
    }

    public Object visitForeachProxy(final ForeachProxy foreach)
    throws VisitorException
    {
      final String name = foreach.getName();
      checkName(name, foreach);
      final Collection<Proxy> body = foreach.getBody();
      visitCollection(body);
      return null;
    }

    public Object visitGraphProxy(final GraphProxy graph)
    throws VisitorException
    {
      final EventListExpressionProxy blocked = graph.getBlockedEvents();
      if (blocked != null) {
        visitEventListExpressionProxy(blocked);
      }
      final Collection<NodeProxy> nodes = graph.getNodes();
      visitCollection(nodes);
      final Collection<EdgeProxy> edges = graph.getEdges();
      visitCollection(edges);
      return null;
    }

    public Object visitIdentifiedProxy(final IdentifiedProxy proxy)
    throws VisitorException
    {
      mNameOwner = proxy;
      final IdentifierProxy identifier = proxy.getIdentifier();
      identifier.acceptVisitor(this);
      mNameOwner = null;
      return null;
    }

    public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy ident)
    throws VisitorException
    {
      final String name = ident.getName();
      checkName(name);
      return null;
    }

    public Object visitModuleProxy(final ModuleProxy module)
    throws VisitorException
    {
      mCurrentModule = module;
      final Collection<ConstantAliasProxy> constants =
        module.getConstantAliasList();
      visitCollection(constants);
      final Collection<EventDeclProxy> events = module.getEventDeclList();
      visitCollection(events);
      final Collection<Proxy> aliases = module.getEventAliasList();
      visitCollection(aliases);
      final Collection<Proxy> components = module.getComponentList();
      visitCollection(components);
      mCurrentModule = null;
      return null;
    }

    public Object visitNodeProxy(final NodeProxy node)
    throws VisitorException
    {
      final String name = node.getName();
      checkName(name, node);
      return null;
    }

    public Object visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy ident)
    throws VisitorException
    {
      final IdentifierProxy base = ident.getBaseIdentifier();
      base.acceptVisitor(this);
      final IdentifierProxy comp = ident.getComponentIdentifier();
      return comp.acceptVisitor(this);
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

    public Object visitSimpleNodeProxy(final SimpleNodeProxy node)
    throws VisitorException
    {
      visitNodeProxy(node);
      final EventListExpressionProxy props = node.getPropositions();
      return visitEventListExpressionProxy(props);
    }


    //#######################################################################
    //# Auxiliary Methods
    private void checkName(final String name, final NamedProxy owner)
    {
      mNameOwner = owner;
      checkName(name);
      mNameOwner = null;
    }

    private void checkName(final String name)
    {
      try {
        mParser.parseSimpleIdentifier(name);
      } catch (final ParseException exception) {
        final StringBuffer buffer = new StringBuffer();
        buffer.append("The name '");
        buffer.append(name);
        buffer.append("'");
        if (mNameOwner != null) {
          buffer.append(" of a ");
          buffer.append(getShortClassName(mNameOwner));
        }
        buffer.append(" in ");
        if (mCurrentComponent == null) {
          buffer.append("module '");
          buffer.append(mCurrentModule.getName());
        } else {
          buffer.append("component '");
          buffer.append(mCurrentComponent.getName());
        }
        buffer.append("' is not a proper Waters identifier!");
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


    //#######################################################################
    //# Data Members
    private final ExpressionParser mParser;

    private NamedProxy mNameOwner;
    private ModuleProxy mCurrentModule;
    private SimpleComponentProxy mCurrentComponent;
  }


  //#######################################################################
  //# Data Members
  private final IdentifierVisitor mVisitor;

}
