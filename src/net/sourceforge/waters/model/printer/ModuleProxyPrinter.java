//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.model.printer;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.EventKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AliasProxy;
import net.sourceforge.waters.model.module.AnchorPosition;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.BoxGeometryProxy;
import net.sourceforge.waters.model.module.ColorGeometryProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.ConstantAliasProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EnumSetExpressionProxy;
import net.sourceforge.waters.model.module.EventAliasProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.EventListExpressionProxy;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ForeachProxy;
import net.sourceforge.waters.model.module.FunctionCallExpressionProxy;
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
import net.sourceforge.waters.model.module.ModuleSequenceProxy;
import net.sourceforge.waters.model.module.NestedBlockProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.ParameterBindingProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.ScopeKind;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.SplineGeometryProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;


public class ModuleProxyPrinter
  extends ProxyPrinter
  implements ModuleProxyVisitor
{

  //#########################################################################
  //# Static Class Methods
  public static String getPrintString(final Proxy proxy)
  {
    try {
      final StringWriter writer = new StringWriter();
      final ModuleProxyPrinter printer = new ModuleProxyPrinter(writer);
      printer.pprint(proxy);
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    }
  }

  public static <P extends Proxy> String getPrintString(final P[] array)
  {
    try {
      final StringWriter writer = new StringWriter();
      final ModuleProxyPrinter printer = new ModuleProxyPrinter(writer);
      printer.print('[');
      if (array != null) {
        for (final Proxy proxy : array) {
          printer.pprint(proxy);
        }
      }
      printer.print(']');
      return writer.toString();
    } catch (final IOException exception) {
      throw new WatersRuntimeException(exception);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    }
  }


  //#########################################################################
  //# Constructors
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
  @Override
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

  @Override
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

  @Override
  public Object visitBoxGeometryProxy(final BoxGeometryProxy geo)
    throws VisitorException
  {
    final Rectangle2D rect = geo.getRectangle();
    print(rect);
    return null;
  }

  @Override
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
    return null;
  }

  @Override
  public Object visitComponentProxy(final ComponentProxy proxy)
    throws VisitorException
  {
    return visitIdentifiedProxy(proxy);
  }

  @Override
  public Object visitConditionalProxy(final ConditionalProxy proxy)
    throws VisitorException
  {
    print("IF ");
    final SimpleExpressionProxy guard = proxy.getGuard();
    guard.acceptVisitor(this);
    return visitNestedBlockProxy(proxy);
  }

  @Override
  public Object visitConstantAliasProxy(final ConstantAliasProxy proxy)
    throws VisitorException
  {
    visitAliasProxy(proxy);
    final ScopeKind scope = proxy.getScope();
    print(scope);
    return null;
  }

  @Override
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

  @Override
  public Object visitEnumSetExpressionProxy
      (final EnumSetExpressionProxy proxy)
    throws VisitorException
  {
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
      print('[');
      final List<SimpleIdentifierProxy> items = proxy.getItems();
      final Iterator<SimpleIdentifierProxy> iter = items.iterator();
      while (iter.hasNext()) {
        final SimpleIdentifierProxy item = iter.next();
        visitSimpleIdentifierProxy(item);
        if (iter.hasNext()) {
          print(", ");
        }
      }
      print(']');
    }
    return null;
  }

  @Override
  public Object visitEventAliasProxy(final EventAliasProxy proxy)
    throws VisitorException
  {
    return visitAliasProxy(proxy);
  }

  @Override
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

  @Override
  public Object visitEventListExpressionProxy
      (final EventListExpressionProxy proxy)
    throws VisitorException
  {
    printEmptyCollection(proxy.getEventIdentifierList());
    return null;
  }

  @Override
  public Object visitExpressionProxy
      (final ExpressionProxy proxy)
    throws VisitorException
  {
    return visitProxy(proxy);
  }

  @Override
  public Object visitForeachProxy(final ForeachProxy proxy)
    throws VisitorException
  {
    print("FOR ");
    print(proxy.getName());
    print(" IN ");
    final SimpleExpressionProxy range = proxy.getRange();
    range.acceptVisitor(this);
    return visitNestedBlockProxy(proxy);
  }

  @Override
  public Object visitFunctionCallExpressionProxy
      (final FunctionCallExpressionProxy proxy)
    throws VisitorException
  {
    final String text = proxy.getPlainText();
    if (text != null) {
      print(text);
    } else {
      final int savedPriority = mPriority;
      print(proxy.getFunctionName());
      print('(');
      try {
        mPriority = OperatorTable.PRIORITY_OUTER;
        final List<SimpleExpressionProxy> args = proxy.getArguments();
        final Iterator<SimpleExpressionProxy> iter = args.iterator();
        while (iter.hasNext()) {
          final SimpleExpressionProxy arg = iter.next();
          arg.acceptVisitor(this);
          if (iter.hasNext()) {
            print(", ");
          }
        }
      } finally {
        mPriority = savedPriority;
      }
      print(')');
    }
    return null;
  }

  @Override
  public Object visitGraphProxy
      (final GraphProxy proxy)
    throws VisitorException
  {
    final LabelBlockProxy blocked = proxy.getBlockedEvents();
    if (blocked != null && !blocked.getEventIdentifierList().isEmpty()) {
      print("BLOCKED ");
      visitLabelBlockProxy(blocked);
      println();
    }
    printCollection("NODES", proxy.getNodes());
    printCollection("EDGES", proxy.getEdges());
    return null;
  }

  @Override
  public Object visitGroupNodeProxy
      (final GroupNodeProxy proxy)
    throws VisitorException
  {
    print("groupnode ");
    print(proxy.getName());
    print(" = ");
    printEmptyCollection(proxy.getImmediateChildNodes());
    visitNodeProxy(proxy);
    return null;
  }

  @Override
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

  @Override
  public Object visitIdentifiedProxy
      (final IdentifiedProxy proxy)
    throws VisitorException
  {
    return visitNamedProxy(proxy);
  }

  @Override
  public Object visitIdentifierProxy
      (final IdentifierProxy proxy)
    throws VisitorException
  {
    return visitSimpleExpressionProxy(proxy);
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
  public Object visitLabelBlockProxy
      (final LabelBlockProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  @Override
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

  @Override
  public Object visitModuleProxy
      (final ModuleProxy proxy)
    throws VisitorException
  {
    print("MODULE ");
    print(proxy.getName());
    println(" {");
    printComment(proxy);
    printCollection("CONSTANTS", proxy.getConstantAliasList());
    printCollection("EVENTS", proxy.getEventDeclList());
    printCollection("ALIASES", proxy.getEventAliasList());
    printCollection("COMPONENTS", proxy.getComponentList());
    println('}');
    return null;
  }

  @Override
  public Object visitModuleSequenceProxy(final ModuleSequenceProxy proxy)
    throws VisitorException
  {
    for (final ModuleProxy module : proxy.getModules()) {
      visitModuleProxy(module);
    }
    return null;
  }

  @Override
  public Object visitNestedBlockProxy(final NestedBlockProxy proxy)
    throws VisitorException
  {
    print(' ');
    printEmptyCollection(proxy.getBody());
    return null;
  }

  @Override
  public Object visitNodeProxy
      (final NodeProxy proxy)
    throws VisitorException
  {
    final EventListExpressionProxy propositions = proxy.getPropositions();
    final List<Proxy> list = propositions.getEventIdentifierList();
    if (!list.isEmpty()) {
      print (' ');
      printCollection(list);
    }
    return null;
  }

  @Override
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

  @Override
  public Object visitPlainEventListProxy
      (final PlainEventListProxy proxy)
    throws VisitorException
  {
    return visitEventListExpressionProxy(proxy);
  }

  @Override
  public Object visitPointGeometryProxy
      (final PointGeometryProxy geo)
    throws VisitorException
  {
    final Point2D point = geo.getPoint();
    print(point);
    return null;
  }

  @Override
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

  @Override
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

  @Override
  public Object visitSimpleExpressionProxy
      (final SimpleExpressionProxy proxy)
    throws VisitorException
  {
    return visitExpressionProxy(proxy);
  }

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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

  @Override
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
