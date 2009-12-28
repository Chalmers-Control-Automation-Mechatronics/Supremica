//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.context
//# CLASS:   OccursChecker
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.model.compiler.context;

import java.util.List;

import net.sourceforge.waters.model.base.VisitorException;
import net.sourceforge.waters.model.module.AbstractModuleProxyVisitor;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.IndexedIdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.QualifiedIdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.UnaryExpressionProxy;


/**
 * A simple implementation of the occurs-check. The occurs-check is needed
 * to determine whether a given expression contains a particular subterm.
 * It is used by the EFA compiler to check whether expressions contain
 * particular variables, e.g., to avoid cyclic bindings.
 *
 * @author Robi Malik
 */

public class OccursChecker extends AbstractModuleProxyVisitor
{

  //#########################################################################
  //# Singleton Pattern
  public static OccursChecker getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final OccursChecker INSTANCE = new OccursChecker();
  }

  private OccursChecker()
  {
  }


  //#########################################################################
  //# Invocation
  public boolean occurs(final SimpleExpressionProxy varname,
                        final SimpleExpressionProxy expr)
  {
    try {
      mVarName = varname;
      return occurs(expr);
    } catch (final VisitorException exception) {
      throw exception.getRuntimeException();
    } finally {
      mVarName = null;
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  private boolean occurs(final SimpleExpressionProxy expr)
    throws VisitorException
  {
    final ModuleEqualityVisitor eq =
      ModuleEqualityVisitor.getInstance(false);
    if (eq.equals(expr, mVarName)) {
      return true;
    } else {
      return (Boolean) expr.acceptVisitor(this);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.module.ModuleProxyVisitor
  public Boolean visitBinaryExpressionProxy
    (final BinaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy lhs = expr.getLeft();
    final SimpleExpressionProxy rhs = expr.getRight();
    return occurs(lhs) || occurs(rhs);
  }

  public Boolean visitIndexedIdentifierProxy
    (final IndexedIdentifierProxy ident)
    throws VisitorException
  {
    final List<SimpleExpressionProxy> indexes = ident.getIndexes();
    for (final SimpleExpressionProxy index : indexes) {
      if (occurs(index)) {
        return true;
      }
    }
    return false;
  }

  public Boolean visitQualifiedIdentifierProxy
    (final QualifiedIdentifierProxy ident)
    throws VisitorException
  {
    final IdentifierProxy base = ident.getBaseIdentifier();
    final Boolean occbase = (Boolean) base.acceptVisitor(this);
    if (occbase) {
      return occbase;
    }
    final IdentifierProxy comp = ident.getComponentIdentifier();
    return (Boolean) comp.acceptVisitor(this);
  }

  public Boolean visitSimpleExpressionProxy
    (final SimpleExpressionProxy expr)
  {
    return false;
  }

  public Boolean visitUnaryExpressionProxy
    (final UnaryExpressionProxy expr)
    throws VisitorException
  {
    final SimpleExpressionProxy subterm = expr.getSubTerm();
    return occurs(subterm);
  }


  //#########################################################################
  //# Data Members
  private SimpleExpressionProxy mVarName;

}
