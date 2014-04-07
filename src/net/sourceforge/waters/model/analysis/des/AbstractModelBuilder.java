//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractModelBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;

import net.sourceforge.waters.model.analysis.IdenticalKindTranslator;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.analysis.ProxyResult;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstract base class to facilitate the implementation of model builders
 * ({@link ModelBuilder}. In addition to the model and factory members
 * inherited from {@link AbstractModelAnalyzer}, this class provides access to
 * a result member, and uses this to implement access to the computed result.
 *
 * @author Robi Malik
 */

public abstract class AbstractModelBuilder<P extends Proxy>
  extends AbstractModelAnalyzer
  implements ModelBuilder<P>
{

  //#########################################################################
  //# Constructors
  public AbstractModelBuilder(final ProductDESProxyFactory factory)
  {
    super(factory, IdenticalKindTranslator.getInstance());
  }

  public AbstractModelBuilder(final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(model, factory, IdenticalKindTranslator.getInstance());
  }

  public AbstractModelBuilder(final ProductDESProxy model,
                              final ProductDESProxyFactory factory,
                              final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  public AbstractModelBuilder(final AutomatonProxy aut,
                              final ProductDESProxyFactory factory)
  {
    super(aut, factory, IdenticalKindTranslator.getInstance());
  }

  public AbstractModelBuilder(final AutomatonProxy aut,
                              final ProductDESProxyFactory factory,
                              final KindTranslator translator)
  {
    super(aut, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelBuilder
  @Override
  public void setOutputName(final String name)
  {
    mOuptutName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOuptutName;
  }

  @Override
  public P getComputedProxy()
  {
    final ProxyResult<P> result = getAnalysisResult();
    if (result != null) {
      return result.getComputedProxy();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public ProxyResult<P> getAnalysisResult()
  {
    return (ProxyResult<P>) super.getAnalysisResult();
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores a  result indicating successful computation.
   * Setting the computed object also marks the analysis run as completed and
   * sets the Boolean result.
   * @param  proxy  The computed object, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  protected boolean setProxyResult(final P proxy)
  {
    final ProxyResult<P> result = getAnalysisResult();
    result.setComputedProxy(proxy);
    return result.isSatisfied();
  }

  /**
   * Computes a name for the output object.
   * @return The name set by the user, if present, or a default name computed
   *         from the names of the automata in the input model.
   * @see #setOutputName(String) setOutputName()
   */
  protected String computeOutputName()
  {
    if (mOuptutName != null) {
      return mOuptutName;
    } else {
      final StringBuilder buffer = new StringBuilder("{");
      final ProductDESProxy model = getModel();
      final Collection<AutomatonProxy> automata = model.getAutomata();
      boolean first = true;
      for (final AutomatonProxy aut : automata) {
        if (first) {
          first = false;
        } else {
          buffer.append(',');
        }
        buffer.append(aut.getName());
      }
      buffer.append('}');
      return buffer.toString();
    }
  }


  //#########################################################################
  //# Data Members
  private String mOuptutName;

}

