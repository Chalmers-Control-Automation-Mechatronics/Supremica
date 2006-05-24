//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.printer
//# CLASS:   ModuleProxyPrinter
//###########################################################################
//# $Id: ModuleProxyPrinter.java,v 1.5 2006-05-24 12:01:56 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.printer;

import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.EventParameterProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.IntParameterProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.ParameterProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.RangeParameterProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SimpleParameterProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;

import net.sourceforge.waters.model.module.VariableProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.BooleanConstantProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;


public class ModuleProxyPrinter
  extends ProxyPrinter
  implements ModuleProxyVisitor
{

  //#########################################################################
  //# Constructors
  public ModuleProxyPrinter()
  {
    super();
  }

  public ModuleProxyPrinter(final Writer writer)
  {
    super(writer);
  }

  public ModuleProxyPrinter(final Writer writer, final int indentwidth)
  {
    super(writer, indentwidth);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  
//EFA-------------------
  public Object visitGuardActionBlockProxy
  (final GuardActionBlockProxy proxy)
 throws VisitorException
  {
	  //Temporary changes
	     return null;
  }
  
  public Object visitVariableProxy
  (final VariableProxy proxy)
 throws VisitorException
  {
	    print(proxy.getName());
	    return null;
  }
  
  public Object visitBooleanConstantProxy 
  (final BooleanConstantProxy proxy)
  throws VisitorException
	  {
	    print(Boolean.toString(proxy.isValue()));
	    return null;
   }
  
  //------------------
  
  public Object visitAliasProxy
      (final AliasProxy proxy)
    throws VisitorException
  {
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    print(" = ");
    final ExpressionProxy expression = proxy.getExpression();
    expression.acceptVisitor(this);
    return null;
  }

  public Object visitBinaryExpressionProxy
      (final BinaryExpressionProxy proxy)
    throws VisitorException
  {
    final int savedPriority = mPriority; 
    final boolean savedAssocBraces = mAssocBraces;
    try {
      final BinaryOperator operator = proxy.getOperator();
      mPriority = operator.getPriority();
      final int associativity = operator.getAssociativity();
      final boolean needBraces =
        (mPriority < savedPriority) ||
        (mPriority == savedPriority) && savedAssocBraces;
      if (needBraces) {
        print('(');
      }
      mAssocBraces = (associativity == BinaryOperator.ASSOC_RIGHT);
      final SimpleExpressionProxy left = proxy.getLeft();
      left.acceptVisitor(this);
      print(operator.getName());
      mAssocBraces = (associativity == BinaryOperator.ASSOC_LEFT);
      final SimpleExpressionProxy right = proxy.getRight();
      right.acceptVisitor(this);
      if (needBraces) {
        print(')');
      }
    } finally {
      mPriority = savedPriority;
      mAssocBraces = savedAssocBraces;
    }
    return null;
  }

  public Object visitBoxGeometryProxy
      (final BoxGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitColorGeometryProxy
      (final ColorGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitComponentProxy
      (final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public Object visitEdgeProxy
      (final EdgeProxy proxy)
    throws VisitorException
  {
    final NodeProxy source = proxy.getSource();
    print(source.getName());
    print(" -> ");
    final NodeProxy target = proxy.getTarget();
    print(target.getName());
    print(' ');
    final LabelBlockProxy labelBlock = proxy.getLabelBlock();
    visitLabelBlockProxy(labelBlock);
    return null;
  }

  public Object visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    print('{');
    final List<SimpleIdentifierProxy> items = proxy.getItems();
    final Iterator<SimpleIdentifierProxy> iter = items.iterator();
    while (iter.hasNext()) {
      final SimpleIdentifierProxy item = iter.next();
      visitSimpleIdentifierProxy(item);
      if (iter.hasNext()) {
        print(", ");
      }
    }
    print('}');
    return null;
  }

  public Object visitEventDeclProxy
      (final EventDeclProxy proxy)
    throws VisitorException
  {
    if (!proxy.isObservable()) {
      print("unobservable ");
    }
    final EventKind kind = proxy.getKind();
    final String kindName = kind.toString().toLowerCase();
    print(kindName);
    print(' ');
    print(proxy.getName());
    final List<SimpleExpressionProxy> ranges = proxy.getRanges();
    for (final SimpleExpressionProxy expr : ranges) {
      print('[');
      expr.acceptVisitor(this);
      print(']');
    }
    return null;
  }

  public Object visitEventListExpressionProxy
      (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    printEmptyCollection(proxy.getEventList());
    return null;
  }

  public Object visitEventParameterProxy
      (final EventParameterProxy proxy)
    throws VisitorException
  {
    visitParameterProxy(proxy);
    visitEventDeclProxy(proxy.getEventDecl());
    return null;
  }

  public Object visitExpressionProxy
      (final ExpressionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitForeachComponentProxy
      (final ForeachComponentProxy proxy)
    throws VisitorException
  {
    return visitForeachProxy(proxy);
  }

  public Object visitForeachEventAliasProxy
      (final ForeachEventAliasProxy proxy)
    throws VisitorException
  {
    return visitForeachProxy(proxy);
  }

  public Object visitForeachEventProxy
      (final ForeachEventProxy proxy)
    throws VisitorException
  {
    return visitForeachProxy(proxy);
  }

  public Object visitForeachProxy
      (final ForeachProxy proxy)
    throws VisitorException
  {
    print("FOR ");
    print(proxy.getName());
    print(" IN ");
    final SimpleExpressionProxy range = proxy.getRange();
    range.acceptVisitor(this);
    final SimpleExpressionProxy guard = proxy.getGuard();
    if (guard != null) {
      print(" WHERE ");
      guard.acceptVisitor(this);
    }
    print(' ');
    printEmptyCollection(proxy.getBody());
    return null;
  }

  public Object visitGraphProxy
      (final GraphProxy proxy)
    throws VisitorException
  {
    final LabelBlockProxy blocked = proxy.getBlockedEvents();
    if (!blocked.getEventList().isEmpty()) {
      print("BLOCKED ");
      visitLabelBlockProxy(blocked);
      println();
    }
    printCollection("NODES", proxy.getNodes());
    printCollection("EDGES", proxy.getEdges());
    return null;
  }

  public Object visitGroupNodeProxy
      (final GroupNodeProxy proxy)
    throws VisitorException
  {
    print("groupnode ");
    print(proxy.getName());
    print(" = ");
    printSortedEmptyCollection(proxy.getImmediateChildNodes());
    visitNodeProxy(proxy);
    return null;
  }

  public Object visitIdentifiedProxy
      (final IdentifiedProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  public Object visitIdentifierProxy
      (final IdentifierProxy proxy)
    throws VisitorException
  {
    print(proxy.getName());
    return null;
  }

  public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    final int savedPriority = mPriority; 
    final boolean savedAssocBraces = mAssocBraces;
    try {
      visitIdentifierProxy(proxy);
      mPriority = OperatorTable.PRIORITY_OUTER;
      mAssocBraces = false;
      final List<SimpleExpressionProxy> indexes = proxy.getIndexes();
      for (final SimpleExpressionProxy expr : indexes) {
        print('[');
        expr.acceptVisitor(this);
        print(']');
      }
      return null;
    } finally {
      mPriority = savedPriority; 
      mAssocBraces = savedAssocBraces;
    }
  }

  public Object visitInstanceProxy
      (final InstanceProxy proxy)
    throws VisitorException
  {
    print("instance ");
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    print(" = ");
    print(proxy.getModuleName());
    print('(');
    final List<ParameterBindingProxy> bindingList = proxy.getBindingList();
    final Iterator<ParameterBindingProxy> iter = bindingList.iterator();
    if (iter.hasNext()) {
      println();
      indentIn();
      while (iter.hasNext()) {
        final ParameterBindingProxy binding = iter.next();
        visitParameterBindingProxy(binding);
        if (iter.hasNext()) {
          print(',');
        }
        println();
      }
      indentOut();
    }
    print(')');
    return null;
  }

  public Object visitIntConstantProxy
      (final IntConstantProxy proxy)
    throws VisitorException
  {
    print(proxy.getValue());
    return null;
  }

  public Object visitIntParameterProxy
      (final IntParameterProxy proxy)
    throws VisitorException
  {
    printSimpleParameterProxy(proxy, "int");
    return null;
  }

  public Object visitLabelBlockProxy
      (final LabelBlockProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  public Object visitLabelGeometryProxy
      (final LabelGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitModuleProxy
      (final ModuleProxy proxy)
    throws VisitorException
  {
    print("MODULE ");
    print(proxy.getName());
    println(" {");
    printCollection("PARAMETERS", proxy.getParameterList());
    printCollection("ALIASES", proxy.getConstantAliasList());
    printCollection("EVENTS", proxy.getEventDeclList());
    printCollection("ALIASES", proxy.getEventAliasList());
    printCollection("COMPONENTS", proxy.getComponentList());
    println('}');
    return null;
  }

  public Object visitNodeProxy
      (final NodeProxy proxy)
    throws VisitorException
  {
    final EventListExpressionProxy propositions = proxy.getPropositions();
    final List<Proxy> list = propositions.getEventList();
    if (!list.isEmpty()) {
      print (' ');
      printCollection(list);
    }
    return null;
  }

  public Object visitParameterBindingProxy
      (final ParameterBindingProxy proxy)
    throws VisitorException
  {
    print(proxy.getName());
    print(" = ");
    final ExpressionProxy expression = proxy.getExpression();
    expression.acceptVisitor(this);
    return null;
  }

  public Object visitParameterProxy
      (final ParameterProxy proxy)
    throws VisitorException
  {
    if (proxy.isRequired()) {
      print("required ");
    } else {
      print("optional ");
    }
    return null;
  }

  public Object visitPlainEventListProxy
      (final PlainEventListProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  public Object visitPointGeometryProxy
      (final PointGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitRangeParameterProxy
      (final RangeParameterProxy proxy)
    throws VisitorException
  {
    printSimpleParameterProxy(proxy, "range");
    return null;
  }

  public Object visitSimpleComponentProxy
      (final SimpleComponentProxy proxy)
    throws VisitorException
  {
    final ComponentKind kind = proxy.getKind();
    final String kindName = kind.toString().toLowerCase();
    print(kindName);
    print(' ');
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    println(" {");
    indentIn();
    visitGraphProxy(proxy.getGraph());
    indentOut();
    print('}');
    return null;
  }

  public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  public Object visitSimpleIdentifierProxy
      (final SimpleIdentifierProxy proxy)
    throws VisitorException
  {
    return visitIdentifierProxy(proxy);
  }

  public Object visitSimpleNodeProxy
      (final SimpleNodeProxy proxy)
    throws VisitorException
  {
    final boolean initial = proxy.isInitial();
    if (initial) {
      print("initial ");
    }
    print(proxy.getName());
    visitNodeProxy(proxy);
    return null;
  }

  public Object visitSimpleParameterProxy
    (final SimpleParameterProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  public Object visitSplineGeometryProxy
      (final SplineGeometryProxy proxy)
    throws VisitorException
  {
    return visitGeometryProxy(proxy);
  }

  public Object visitUnaryExpressionProxy
      (final UnaryExpressionProxy proxy)
    throws VisitorException
  {
    final int savedPriority = mPriority; 
    final boolean savedAssocBraces = mAssocBraces;
    try {
      final UnaryOperator operator = proxy.getOperator();
      mPriority = operator.getPriority();
      final boolean needBraces = (mPriority < savedPriority);
      if (needBraces) {
        print('(');
      }
      mAssocBraces = false;
      print(operator.getName());
      final SimpleExpressionProxy subTerm = proxy.getSubTerm();
      subTerm.acceptVisitor(this);
      if (needBraces) {
        print(')');
      }
    } finally {
      mPriority = savedPriority;
      mAssocBraces = savedAssocBraces;
    }
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void printSimpleParameterProxy
    (final SimpleParameterProxy proxy, final String typename)
    throws VisitorException
  {
    visitParameterProxy(proxy);
    print(typename);
    print(' ');
    print(proxy.getName());
    print(" = ");
    final SimpleExpressionProxy defaultValue = proxy.getDefaultValue();
    defaultValue.acceptVisitor(this);
  }


  //#########################################################################
  //# Data Members
  private int mPriority = OperatorTable.PRIORITY_OUTER;
  private boolean mAssocBraces = false;

}
