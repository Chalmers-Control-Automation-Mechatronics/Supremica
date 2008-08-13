package net.sourceforge.waters.analysis.composing;

import java.util.List;

import net.sourceforge.waters.model.analysis.ModelVerifierFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;




public class ComposeModelVerifierFactory implements ModelVerifierFactory
{

  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelVerifierFactory
  public ComposeControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposeControllabilityChecker(factory);
  }

  public ComposeLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new ComposeLanguageInclusionChecker(factory);
  }


  //#########################################################################
  //# Factory Instantiation
  public static ComposeModelVerifierFactory getInstance()
  {
    if (theInstance == null) {
      theInstance = new ComposeModelVerifierFactory();
    }
    return theInstance;
  }

  public static ComposeModelVerifierFactory
    getInstance(final List<String> cmdline)
  {
    return getInstance();
  }


  //#########################################################################
  //# Class Variables
  private static ComposeModelVerifierFactory theInstance = null;

}
