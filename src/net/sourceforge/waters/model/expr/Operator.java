//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.waters.model.expr
//# CLASS:   Operator
//###########################################################################
//# $Id: Operator.java,v 1.3 2006-03-02 12:12:50 martin Exp $
//###########################################################################

package net.sourceforge.waters.model.expr;


/**
 * <P>The common interface for unary and binary operators.</P>
 *
 * <P>Operators are used by parsers and scanners in situations where
 * expressions need to be created from operator names, before the class
 * of the expression is known. The can be looked up in the operator table
 * ({@link OperatorTable}) by their name and contain information such
 * as the binding priority of operators and instructions how to construct
 * the actual expression.</P>
 *
 * @author Robi Malik
 */

public interface Operator {

  //#########################################################################
  //# Simple Access Methods
  public String getName();

  public int getPriority();

  //#########################################################################
  //# Class Constants
  public static final int TYPE_INT = 1;
  public static final int TYPE_ATOM = 2;
  public static final int TYPE_RANGE = 4;
  public static final int TYPE_NAME = 8;
  public static final int TYPE_BOOLEAN = 16;
  public static final int TYPE_ANY =
    TYPE_INT | TYPE_ATOM | TYPE_RANGE | TYPE_NAME | TYPE_BOOLEAN;

}
