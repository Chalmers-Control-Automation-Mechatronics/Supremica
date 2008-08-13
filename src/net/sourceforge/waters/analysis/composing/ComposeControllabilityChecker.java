package net.sourceforge.waters.analysis.composing;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;



public class ComposeControllabilityChecker
  extends ComposeSafetyVerifier
  implements ControllabilityChecker
{

  //#########################################################################
  //# Constructors
  public ComposeControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public ComposeControllabilityChecker(final ProductDESProxy model,
                                       final ProductDESProxyFactory factory)
  {
    super(model, ControllabilityKindTranslator.getInstance(), factory);
  }

}
