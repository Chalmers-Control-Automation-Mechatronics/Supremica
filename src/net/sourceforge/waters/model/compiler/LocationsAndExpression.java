
package net.sourceforge.waters.model.compiler;
import java.util.Set;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class LocationsAndExpression
{

  //#########################################################################
  //# Constructors
	LocationsAndExpression(final Set<String> forbiddenLoc, 
		  final SimpleExpressionProxy sortedUncClause)
  {
	  mForbiddenLoc = forbiddenLoc;
	  mUncontrollableClauses = sortedUncClause;
  }


  //#########################################################################
  //# Simple Access
  Set<String> getLocations()
  {
    return mForbiddenLoc;
  }

  SimpleExpressionProxy getExpression()
  {
    return mUncontrollableClauses;
  }


  //#########################################################################
  //# Data Members
  private final Set<String> mForbiddenLoc;
  private final SimpleExpressionProxy mUncontrollableClauses;

}
