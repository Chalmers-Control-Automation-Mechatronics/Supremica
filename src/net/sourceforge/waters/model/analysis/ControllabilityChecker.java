//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.java,v 1.1 2006-08-15 01:43:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * @author Robi Malik
 */

public interface ControllabilityChecker extends ModelVerifier
{

  //#########################################################################
  //# More Sepcific Access to the Results
  public SafetyTraceProxy getCounterExample();

}
