//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractProductDESBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;


/**
 * An abstract base class to facilitate the implementation of product DES
 * builders. In addition to the model and factory members inherited from
 * {@link AbstractModelAnalyzer}, this class provides access to a product DES
 * result member, and uses this to implement access to the computed
 * result.
 *
 * @author Robi Malik
 */

public abstract class AbstractProductDESBuilder
  extends AbstractModelBuilder<ProductDESProxy>
  implements ProductDESBuilder
{

  //#########################################################################
  //# Constructors
  public AbstractProductDESBuilder(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public AbstractProductDESBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public AbstractProductDESBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  public AbstractProductDESBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory)
  {
    super(aut, factory);
  }

  public AbstractProductDESBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator)
  {
    super(aut, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  public ProductDESProxy getComputedProductDES()
  {
    return getComputedProxy();
  }

  @Override
  public ProductDESResult getAnalysisResult()
  {
    return (ProductDESResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected ProductDESResult createAnalysisResult()
  {
    return new DefaultProductDESResult();
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores an product DES result containing a single automaton,
   * indicating successful completion. Setting the result also marks the
   * analysis run as completed and sets the Boolean result.
   * @param  aut    The computed automaton, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  protected boolean setAutomatonResult(final AutomatonProxy aut)
  {
    if (aut == null) {
      return setBooleanResult(false);
    } else {
      final ProductDESProxyFactory factory = getFactory();
      final ProductDESProxy des =
        AutomatonTools.createProductDESProxy(aut, factory);
      return setProductDESResult(des);
    }
  }

  /**
   * Stores an product DES result indicating successful completion.
   * Setting the product DES also marks the analysis run as completed and
   * sets the Boolean result.
   * @param  aut    The computed product DES, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  protected boolean setProductDESResult(final ProductDESProxy des)
  {
    return setProxyResult(des);
  }

}

