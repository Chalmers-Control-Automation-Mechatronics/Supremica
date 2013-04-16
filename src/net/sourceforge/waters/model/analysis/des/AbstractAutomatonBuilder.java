//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.analysis
//# CLASS:   AbstractAutomatonBuilder
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * An abstract base class to facilitate the implementation of automaton
 * builders. In addition to the model and factory members inherited from
 * {@link AbstractModelAnalyzer}, this class provides access to a automaton
 * result member, and uses this to implement access to the computed
 * result.
 *
 * @author Robi Malik
 */

public abstract class AbstractAutomatonBuilder
  extends AbstractModelBuilder<AutomatonProxy>
  implements AutomatonBuilder
{

  //#########################################################################
  //# Constructors
  public AbstractAutomatonBuilder(final ProductDESProxyFactory factory)
  {
    super(factory);
  }

  public AbstractAutomatonBuilder(final ProductDESProxy model,
                                  final ProductDESProxyFactory factory)
  {
    super(model, factory);
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
    super(aut, factory);
  }

  public AbstractAutomatonBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory,
                                  final KindTranslator translator)
  {
    super(aut, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.AutomatonBuilder
  public void setOutputKind(final ComponentKind kind)
  {
    mOutputKind = kind;
  }

  public ComponentKind getOutputKind()
  {
    return mOutputKind;
  }

  public AutomatonProxy getComputedAutomaton()
  {
    return getComputedProxy();
  }

  @Override
  public AutomatonResult getAnalysisResult()
  {
    return (AutomatonResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  protected AutomatonResult createAnalysisResult()
  {
    return new DefaultAutomatonResult();
  }


  //#########################################################################
  //# Setting the Result
  /**
   * Stores an automaton result indicating successful computation.
   * Setting the automaton also marks the analysis run as completed and
   * sets the Boolean result.
   * @param  aut    The computed automaton, or <CODE>null</CODE> to
   *                indicate an unsuccessful computation. The Boolean analysis
   *                result is set to <CODE>false</CODE> if and only if this
   *                parameter is <CODE>null</CODE>.
   */
  protected boolean setAutomatonResult(final AutomatonProxy aut)
  {
    return setProxyResult(aut);
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
  private ComponentKind mOutputKind;

}

