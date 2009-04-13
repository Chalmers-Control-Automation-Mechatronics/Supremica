package net.sourceforge.waters.analysis.distributed;

import net.sourceforge.waters.model.analysis.ControllabilityChecker;
import net.sourceforge.waters.model.analysis.ControllabilityKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * 'Distributed' controllability checker... some day
 * @author Sam Douglas
 */
public class DistributedControllabilityChecker
  extends DistributedSafetyVerifier
  implements ControllabilityChecker
{
  public DistributedControllabilityChecker(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public DistributedControllabilityChecker(final ProductDESProxy model,
					   final ProductDESProxyFactory factory)
  {
    super(model, ControllabilityKindTranslator.getInstance(), factory);
  }
}