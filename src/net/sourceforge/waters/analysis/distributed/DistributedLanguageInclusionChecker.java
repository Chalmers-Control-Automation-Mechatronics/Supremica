package net.sourceforge.waters.analysis.distributed;

import net.sourceforge.waters.model.analysis.LanguageInclusionChecker;
import net.sourceforge.waters.model.analysis.LanguageInclusionKindTranslator;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * Distributed language inclusion checker. Based on the distributed
 * safety verifier.
 * @author Sam Douglas
 */
public class DistributedLanguageInclusionChecker
  extends DistributedSafetyVerifier
  implements LanguageInclusionChecker
{
  public DistributedLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public DistributedLanguageInclusionChecker
    (final ProductDESProxy model,
     final ProductDESProxyFactory factory)
  {
    super(model, LanguageInclusionKindTranslator.getInstance(), factory);
  }
}