//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   ModelAnalyser
//###########################################################################
//# $Id: ModelAnalyser.java,v 1.3 2006-08-15 01:43:06 robi Exp $
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * <P>The main model analyser interface.</P>
 *
 * @author Robi Malik
 */

public interface ModelAnalyser
{

  //#########################################################################
  //# Invocation
  public boolean run();


  //#########################################################################
  //# Simple Acess Methods
  public ProductDESProxyFactory  getFactory();

  public ProductDESProxy getInput();

  public AnalysisResult getAnalysisResult();

}
