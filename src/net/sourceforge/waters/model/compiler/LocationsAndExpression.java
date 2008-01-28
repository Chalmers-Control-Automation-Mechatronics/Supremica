
package net.sourceforge.waters.model.compiler;
import java.util.List;
import java.util.Set;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;


class LocationsAndExpression
{

  //#########################################################################
  //# Constructors
	LocationsAndExpression(String event, Set<String> forbiddenLoc, 
		  List <SimpleExpressionProxy> sortedDNFClause)
  {
	  mEvent = event;
	  mForbiddenLoc = forbiddenLoc;
	  mUncontrollableClauses = sortedDNFClause;
  }


  //#########################################################################
  //# Simple Access
  String getEvent(){
	  return mEvent;
  }
	
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
  void setEvent(String name){
	  mEvent = name;
  }
  
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
  private String mEvent;
  private Set<String> mForbiddenLoc;
  private List <SimpleExpressionProxy> mUncontrollableClauses;

}
