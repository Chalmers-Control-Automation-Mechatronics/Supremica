//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ControllabilityChecker
//###########################################################################
//# $Id: ControllabilityChecker.java,v 1.2 2006-11-02 22:40:29 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.SafetyTraceProxy;


/**
 * A model verifier that checks its input for controllability.
 * 
 * @author Robi Malik
 */

public interface ControllabilityChecker extends ModelVerifier
{

  //#########################################################################
  //# More Sepcific Access to the Results
  public SafetyTraceProxy getCounterExample();

}
