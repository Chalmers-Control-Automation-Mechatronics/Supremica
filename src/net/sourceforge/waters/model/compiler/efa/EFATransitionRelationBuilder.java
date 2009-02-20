//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   EFATransitionRelationBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.model.base.ProxyAccessor;
import net.sourceforge.waters.model.base.ProxyAccessorByContents;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;
import net.sourceforge.waters.model.compiler.context.SimpleExpressionCompiler;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


/**
 * An auxiliary component of the EFA compiler to build the variable
 * transition relations for EFA events.
 *
 * @author Robi Malik
 */

class EFATransitionRelationBuilder
{

  //#########################################################################
  //# Constructor
  EFATransitionRelationBuilder
    (final CompilerOperatorTable optable,
     final EFAModuleContext context,
     final SimpleExpressionCompiler compiler)
  {
    mOperatorTable = optable;
    mRootContext = context;
    mSimpleExpressionCompiler = compiler;
    mCollector = new EFAVariableCollector(optable, context);

    mUniqueTransitionRelations =
      new HashMap<EFAVariableTransitionRelation,
                  EFAVariableTransitionRelation>();
    mUniqueTransitionRelationParts =
      new HashMap<EFAVariableTransitionRelationPart,
                  EFAVariableTransitionRelationPart>();
    mCachedTransitionRelationParts =
      new HashMap<ProxyAccessor<SimpleExpressionProxy>,
                  EFAVariableTransitionRelationPart>();
  }


  //#########################################################################
  //# Invocation
  EFAVariableTransitionRelation buildTransitionRelation
    (final ConstraintList constraints)
  {
    for (final SimpleExpressionProxy literal : constraints.getConstraints()) {
      //final EFAVariable var = mCollector.collectOneVariable(literal);
    }
    return null;
  }


  //#########################################################################
  //# Data Members
  private final CompilerOperatorTable mOperatorTable;
  private final SimpleExpressionCompiler mSimpleExpressionCompiler;
  private final EFAModuleContext mRootContext;
  private final EFAVariableCollector mCollector;

  private final
    Map<EFAVariableTransitionRelation,EFAVariableTransitionRelation>
    mUniqueTransitionRelations;
  private final
    Map<EFAVariableTransitionRelationPart,EFAVariableTransitionRelationPart>
    mUniqueTransitionRelationParts;
  private final
    Map<ProxyAccessor<SimpleExpressionProxy>,EFAVariableTransitionRelationPart>
    mCachedTransitionRelationParts;

}
