//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.constraint
//# CLASS:   ReplaceVisitor
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.constraint;

import java.util.ArrayList;
import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.UnaryOperator;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


class ReplaceVisitor extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  static ReplaceVisitor getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final ReplaceVisitor INSTANCE = new ReplaceVisitor();
  }

  private ReplaceVisitor()
  {
  }

    
  //#########################################################################
  //# Invocation
  SimpleExpressionProxy replace(final SimpleExpressionProxy template,
                                final SimplificationRule rule,
                                final ModuleProxyFactory factory)
  {
    try {
      mFactory = factory;
      mCurrentRule = rule;
      return replace(template);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mFactory = null;
      mCurrentRule = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private SimpleExpressionProxy replace(final SimpleExpressionProxy template)
    throws VisitorException
  {
    return (SimpleExpressionProxy) template.acceptVisitor(this);
  }
    

  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public BinaryExpressionProxy visitBinaryExpressionProxy
    (final BinaryExpressionProxy binary)
    throws VisitorException
  {
    final SimpleExpressionProxy oldleft = binary.getLeft();
    final SimpleExpressionProxy newleft = replace(oldleft);
    final SimpleExpressionProxy oldright = binary.getRight();
    final SimpleExpressionProxy newright = replace(oldright);
    if (newleft == oldleft && newright == oldright) {
      return binary;
    } else {
      final BinaryOperator op = binary.getOperator();
      return mFactory.createBinaryExpressionProxy(op, newleft, newright);
    }
  }

  public IndexedIdentifierProxy visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> indexes =
      new ArrayList<SimpleExpressionProxy>(ident.getIndexes());
    final int numindexes = indexes.size();
    boolean change = false;
    for (int i = 0; i < numindexes; i++) {
      final SimpleExpressionProxy oldindex = indexes.get(i);
      final SimpleExpressionProxy newindex = replace(oldindex);
      if (newindex != oldindex) {
        indexes.set(i, newindex);
        change = true;
      }
    }
    if (change) {
      final String name = ident.getName();
      return mFactory.createIndexedIdentifierProxy(name, indexes);
    } else {
      return ident;
    }
  }

  public QualifiedIdentifierProxy visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy qual)
    throws VisitorException
  {
    final IdentifierProxy oldbase = qual.getBaseIdentifier();
    final IdentifierProxy newbase = (IdentifierProxy) replace(oldbase);
    final IdentifierProxy oldcomp = qual.getComponentIdentifier();
    final IdentifierProxy newcomp = (IdentifierProxy) replace(oldcomp);
    if (newbase == oldbase && newcomp == oldcomp) {
      return qual;
    } else {
      return mFactory.createQualifiedIdentifierProxy(newbase, newcomp);
    }
  }

  public SimpleExpressionProxy visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
  {
    return expr;
  }

  public SimpleExpressionProxy visitSimpleIdentifierProxy
    (final SimpleIdentifierProxy ident)
  {
    final PlaceHolder placeholder = mCurrentRule.getPlaceHolder(ident);
    if (placeholder != null) {
      return placeholder.getBoundExpression();
    } else {
      return visitSimpleExpressionProxy(ident);
    }
  }

  public UnaryExpressionProxy visitUnaryExpressionProxy
    (final UnaryExpressionProxy unary)
    throws VisitorException
  {
    final SimpleExpressionProxy oldsub = unary.getSubTerm();
    final SimpleExpressionProxy newsub = replace(oldsub);
    if (newsub == oldsub) {
      return unary;
    } else {
      final UnaryOperator op = unary.getOperator();
      return mFactory.createUnaryExpressionProxy(op, newsub);
    }
  }


  //#########################################################################
  //# Data Members
  private ModuleProxyFactory mFactory;
  private SimplificationRule mCurrentRule;

}
