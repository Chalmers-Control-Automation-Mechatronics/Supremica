//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelVerifier
//###########################################################################
//# $Id: ModelVerifier.java,v 1.1 2006-08-15 01:43:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.TraceProxy;


/**
 * @author Robi Malik
 */

public interface ModelVerifier extends ModelAnalyser
{

  //#########################################################################
  //# More Sepcific Access to the Results
  public boolean isSatisfied();

  public TraceProxy getCounterExample();

  public VerificationResult getAnalysisResult();

}
