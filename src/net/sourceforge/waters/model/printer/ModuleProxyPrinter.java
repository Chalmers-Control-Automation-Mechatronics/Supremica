//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.printer
//# CLASS:   ModuleProxyPrinter
//###########################################################################
//# $Id: ModuleProxyPrinter.java,v 1.17 2008-06-29 04:01:44 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.printer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachComponentProxy;
import net.sourceforge.waters.model.module.ForeachEventAliasProxy;
import net.sourceforge.waters.model.module.ForeachEventProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.IdentifiedProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.InstanceProxy;
import net.sourceforge.waters.model.module.IntConstantProxy;
import net.sourceforge.waters.model.module.LabelBlockProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyVisitor;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;

import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.AnchorPosition;
import net.sourceforge.waters.xsd.module.ScopeKind;


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
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
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
    }
    return null;
  }

  public Object visitBoxGeometryProxy(final BoxGeometryProxy geo)
    throws VisitorException
  {
    final Rectangle2D rect = geo.getRectangle();
    print(rect);
    return null;
  }

  public Object visitColorGeometryProxy(final ColorGeometryProxy geo)
    throws VisitorException
  {
    final Set<Color> colors = geo.getColorSet();
    if (colors.size() == 1) {
      print(colors.iterator().next());
    } else {
      boolean first = true;
      print('{');
      for (final Color color : colors) {
        if (first) {
          first = false;
        } else {
          print(", ");
        }
        print(color);
      }
    }
    return visitGeometryProxy(geo);
  }

  public Object visitComponentProxy(final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  public Object visitConstantAliasProxy(final ConstantAliasProxy proxy)
    throws VisitorException
  {
    visitAliasProxy(proxy);
    final ScopeKind scope = proxy.getScope();
    print(scope);
    return null;
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
    final GuardActionBlockProxy guardActionBlock = proxy.getGuardActionBlock();
    if (guardActionBlock != null) {
      visitGuardActionBlockProxy(guardActionBlock);
    }
    return null;
  }

  public Object visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
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
    }
    return null;
  }

  public Object visitEventAliasProxy(final EventAliasProxy proxy)
    throws VisitorException
  {
    return visitAliasProxy(proxy);
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
    final ScopeKind scope = proxy.getScope();
    print(scope);
    return null;
  }

  public Object visitEventListExpressionProxy
      (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    printEmptyCollection(proxy.getEventList());
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
    if (blocked != null && !blocked.getEventList().isEmpty()) {
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

  public Object visitGuardActionBlockProxy
    (final GuardActionBlockProxy proxy)
    throws VisitorException
  {
    println('[');
    printCollection(proxy.getGuards());
    println(']');
    println('{');
    printCollection(proxy.getActions());
    println('}');
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
    return visitSimpleExpressionProxy(proxy);
  }

  public Object visitIndexedIdentifierProxy
      (final IndexedIdentifierProxy proxy)
    throws VisitorException
  {
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
      final int savedPriority = mPriority; 
      final boolean savedAssocBraces = mAssocBraces;
      try {
        print(proxy.getName());
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
    return null;
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
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
      print(proxy.getValue());
    }
    return null;
  }

  public Object visitLabelBlockProxy
      (final LabelBlockProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  public Object visitLabelGeometryProxy
      (final LabelGeometryProxy geo)
    throws VisitorException
  {
    final AnchorPosition anchor = geo.getAnchor();
    final Point2D point = geo.getOffset();
    print('[');
    print(anchor.toString());
    print(']');
    print(point);
    return null;
  }

  public Object visitModuleProxy
      (final ModuleProxy proxy)
    throws VisitorException
  {
    print("MODULE ");
    print(proxy.getName());
    println(" {");
    printCollection("CONSTANTS", proxy.getConstantAliasList());
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

  public Object visitPlainEventListProxy
      (final PlainEventListProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  public Object visitPointGeometryProxy
      (final PointGeometryProxy geo)
    throws VisitorException
  {
    final Point2D point = geo.getPoint();
    print(point);
    return null;
  }

  public Object visitQualifiedIdentifierProxy
      (final QualifiedIdentifierProxy proxy)
    throws VisitorException
  {
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
      final IdentifierProxy base = proxy.getBaseIdentifier();
      base.acceptVisitor(this);
      print('.');
      final IdentifierProxy comp = proxy.getComponentIdentifier();
      comp.acceptVisitor(this);
    }
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
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
      print(proxy.getName());
    }
    return null;
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

  public Object visitSplineGeometryProxy
      (final SplineGeometryProxy geo)
    throws VisitorException
  {
    final List<Point2D> points = geo.getPoints();
    boolean first = true;
    for (final Point2D point : points) {
      if (first) {
        first = false;
      } else {
        print(", ");
      }
      print(point);
    }
    return null;
  }

  public Object visitUnaryExpressionProxy(final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final String text = expr.getPlainText();
    if (text != null) {
      print(text);
    } else {
      final int savedPriority = mPriority; 
      final boolean savedAssocBraces = mAssocBraces;
      try {
        final UnaryOperator operator = expr.getOperator();
        final boolean prefix = operator.isPrefix();
        final String opname = operator.getName();
        mPriority = operator.getPriority();
        final boolean needBraces = (mPriority < savedPriority);
        if (needBraces) {
          print('(');
        }
        mAssocBraces = false;
        if (prefix) {
          print(opname);
        }
        final SimpleExpressionProxy subTerm = expr.getSubTerm();
        subTerm.acceptVisitor(this);
        if (!prefix) {
          print(opname);
        }
        if (needBraces) {
          print(')');
        }
      } finally {
        mPriority = savedPriority;
        mAssocBraces = savedAssocBraces;
      }
    }
    return null;
  }

  public Object visitVariableComponentProxy
      (final VariableComponentProxy proxy)
    throws VisitorException
  {
    print("variable ");
    final IdentifierProxy identifier = proxy.getIdentifier();
    identifier.acceptVisitor(this);
    print(" : ");
    final SimpleExpressionProxy type = proxy.getType();
    type.acceptVisitor(this);
    println(" {");
    indentIn();
    print("initial WHEN ");
    final SimpleExpressionProxy initial = proxy.getInitialStatePredicate();
    initial.acceptVisitor(this);
    println();
    for (final VariableMarkingProxy marking : proxy.getVariableMarkings()) {
      visitVariableMarkingProxy(marking);
      println();
    }
    indentOut();
    print('}');
    return null;
  }

  public Object visitVariableMarkingProxy
      (final VariableMarkingProxy proxy)
    throws VisitorException
  {
    print("marking ");
    final IdentifierProxy proposition = proxy.getProposition();
    proposition.acceptVisitor(this);
    print(" WHEN ");
    final SimpleExpressionProxy predicate = proxy.getPredicate();
    predicate.acceptVisitor(this);
    return null;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void print(final ScopeKind scope)
    throws VisitorException
  {
    switch (scope) {
    case OPTIONAL_PARAMETER:
      print(" (optional parameter)");
      break;
    case REQUIRED_PARAMETER:
      print(" (required parameter)");
      break;
    default:
      break;
    }
  }

  private void print(final Point2D point)
    throws VisitorException
  {
    print('(');
    print(point.getX());
    print(',');
    print(point.getY());
    print(')');
  }

  private void print(final Rectangle2D rect)
    throws VisitorException
  {
    print('(');
    print(rect.getX());
    print(',');
    print(rect.getY());
    print(',');
    print(rect.getWidth());
    print(',');
    print(rect.getHeight());
    print(')');
  }

  private void print(final Color color)
    throws VisitorException
  {
    print("RGB(");
    print(color.getRed());
    print(',');
    print(color.getGreen());
    print(',');
    print(color.getBlue());
    print(')');
  }

  
  //#########################################################################
  //# Data Members
  private int mPriority = OperatorTable.PRIORITY_OUTER;
  private boolean mAssocBraces = false;

}
