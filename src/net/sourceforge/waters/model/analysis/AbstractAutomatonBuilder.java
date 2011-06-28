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

import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * An abstract base class to facilitate the implementation of automaton
 * builders. In addition to the model and factory members inherited from
 * {@link AbstractModelAnalyser}, this class provides access to a automaton
 * result member, and uses this to implement access to the computed
 * automaton.
 *
 * @author Robi Malik
 */

public abstract class AbstractAutomatonBuilder
  extends AbstractModelAnalyser
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

  public AbstractAutomatonBuilder(final AutomatonProxy aut,
                                  final ProductDESProxyFactory factory)
  {
    super(aut, factory);
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

  public AutomatonProxy getComputedAutomaton()
  {
    final AutomatonResult result = getAnalysisResult();
    if (result != null) {
      return result.getAutomaton();
    } else {
      throw new IllegalStateException("Call run() first!");
    }
  }

  public AutomatonResult getAnalysisResult()
  {
    return (AutomatonResult) super.getAnalysisResult();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  protected AutomatonResult createAnalysisResult()
  {
    return new AutomatonResult();
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
    final AutomatonResult result = getAnalysisResult();
    result.setAutomaton(aut);
    return result.isSatisfied();
  }

  /**
   * Computes a name for the output automaton.
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


  //#########################################################################
  //# Data Members
  private String mOuptutName;
  private ComponentKind mOutputKind;

}

