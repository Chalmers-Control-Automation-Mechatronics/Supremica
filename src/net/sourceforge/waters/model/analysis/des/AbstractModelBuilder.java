//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
//###########################################################################

package net.sourceforge.waters.model.analysis.des;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.analysis.options.StringParameter;
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
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer

  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    list.add(new StringParameter(ParameterIDs.ModelBuilder_OutputName,
      "Output name", "Sets the name to be given to the output object.", "Supervisor") {
      @Override
      public void commitValue()
      {
        setOutputName(getValue());
      }
    });
    return list;
  }



  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelBuilder
  @Override
  public void setOutputName(final String name)
  {
    mOutputName = name;
  }

  @Override
  public String getOutputName()
  {
    return mOutputName;
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
   *                indicate an unsuccessful computation.
   * @return The Boolean analysis result at the end of the call.
   *         It is set to <CODE>false</CODE> if and only if the
   *         <CODE>proxy</CODE> parameter is <CODE>null</CODE>.
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
    if (mOutputName != null) {
      return mOutputName;
    } else {
      final ProductDESProxy des = getModel();
      return computeOutputName(des);
    }
  }

  /**
   * Computes a default name for an output object by combining all the
   * automata names in the given model.
   */
  public static String computeOutputName(final ProductDESProxy des)
  {
    final StringBuilder buffer = new StringBuilder("{");
    final Collection<AutomatonProxy> automata = des.getAutomata();
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


  //#########################################################################
  //# Data Members
  private String mOutputName;

}
