
package net.sourceforge.waters.model.compiler;
import java.util.List;
import java.util.Set;

import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class LocationsAndExpression
{

  //#########################################################################
  //# Constructors
	LocationsAndExpression(Set<String> forbiddenLoc, 
		  List <SimpleExpressionProxy> sortedDNFClause)
  {
	  mForbiddenLoc = forbiddenLoc;
	  mUncontrollableClauses = sortedDNFClause;
  }


  //#########################################################################
  //# Simple Access
  Set<String> getLocations()
  {
    return mForbiddenLoc;
  }

  List <SimpleExpressionProxy> getExpression()
  {
    return mUncontrollableClauses;
  }
  
  //#########################################################################
  //# Setters
  void setLocations(Set<String> forbiddenLoc)
  {
   mForbiddenLoc = forbiddenLoc;
  }

  void setExpression(List <SimpleExpressionProxy> uncontrollableClauses)
  {
   mUncontrollableClauses = uncontrollableClauses;
  }
  
  //#########################################################################
  //# Data Members
  private Set<String> mForbiddenLoc;
  private List <SimpleExpressionProxy> mUncontrollableClauses;

}
