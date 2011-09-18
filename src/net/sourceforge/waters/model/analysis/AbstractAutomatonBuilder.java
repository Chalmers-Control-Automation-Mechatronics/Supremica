//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAutomatonBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis;

import java.util.Collection;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * An abstract base class to facilitate the implementation of automaton
 * builders. In addition to the model and factory members inherited from
 * {@link AbstractModelAnalyser}, this class provides access to a automaton
 * result member, and uses this to implement access to the computed
 * result.
 *
 * @author Robi Malik
 */

public abstract class AbstractAutomatonBuilder<P extends Proxy>
  extends AbstractModelAnalyser
  implements AutomatonBuilder<P>
{

  //#########################################################################
  //# Constructors
  public AbstractAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    super(factory, IdenticalKindTranslator.getInstance());
  }

  public AbstractAutomatonBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory)
  {
    super(model, factory, IdenticalKindTranslator.getInstance());
  }

  public AbstractAutomatonBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator)
  {
    super(model, factory, translator);
  }

  public AbstractAutomatonBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory)
  {
    super(aut, factory, IdenticalKindTranslator.getInstance());
  }

  public AbstractAutomatonBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator)
  {
    super(aut, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  public void setOutputName(final String name)
  {
    mOuptutName = name;
  }

  public String getOutputName()
  {
    return mOuptutName;
  }

  public void setOutputKind(final ComponentKind kind)
  {
    mOutputKind = kind;
  }

  public ComponentKind getOutputKind()
  {
    return mOutputKind;
  }

  public void setConstructsResult(final boolean construct)
  {
    mConstructsResult = construct;
  }

  public boolean getConstructsResult()
  {
    return mConstructsResult;
  }

  public P getComputedProxy()
  {
    final AutomatonResult<P> result = getAnalysisResult();
    if (result != null) {
      return result.getComputedProxy();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  @SuppressWarnings("unchecked")
  public AutomatonResult<P> getAnalysisResult()
  {
    return (AutomatonResult<P>) super.getAnalysisResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  protected AutomatonResult<P> createAnalysisResult()
  {
    return new AutomatonResult<P>();
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores an automaton result indicating successful computation.
   * Setting the automaton also marks the analysis run as completed and
   * sets the Boolean result.
   * @param  proxy    The computed automaton, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  protected boolean setAutomatonResult(final P proxy)
  {
    final AutomatonResult<P> result = getAnalysisResult();
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
      final StringBuffer buffer = new StringBuffer("{");
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

  /**
   * Computes a component kind for the output automaton (or automata).
   * @return The component kind set by the user, if present, or a default kind
   *         determined from the automata in the input model.
   * @see #setOutputKind(ComponentKind) setOutputKind()
   */
  protected ComponentKind computeOutputKind()
  {
    if (mOutputKind != null) {
      return mOutputKind;
    } else {
      ComponentKind result = null;
      final ProductDESProxy model = getModel();
      final Collection<AutomatonProxy> automata = model.getAutomata();
      for (final AutomatonProxy aut : automata) {
        final ComponentKind kind = aut.getKind();
        if (kind == result) {
          continue;
        } else if (result == null) {
          result = kind;
        } else {
          return ComponentKind.PLANT;
        }
      }
      return result;
    }
  }


  //#########################################################################
  //# Data Members
  private String mOuptutName;
  private ComponentKind mOutputKind;
  private boolean mConstructsResult = true;

}

