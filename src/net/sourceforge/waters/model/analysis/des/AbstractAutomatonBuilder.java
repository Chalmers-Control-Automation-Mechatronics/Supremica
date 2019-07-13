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

import net.sourceforge.waters.analysis.options.EnumParameter;
import net.sourceforge.waters.analysis.options.Parameter;
import net.sourceforge.waters.analysis.options.ParameterIDs;
import net.sourceforge.waters.analysis.options.StringParameter;
import net.sourceforge.waters.model.analysis.KindTranslator;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


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
  @Override
  public void setOutputKind(final ComponentKind kind)
  {
    mOutputKind = kind;
  }

  @Override
  public ComponentKind getOutputKind()
  {
    return mOutputKind;
  }

  @Override
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
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
  @Override
  public List<Parameter> getParameters()
  {
    final List<Parameter> list = super.getParameters();
    for (final Parameter param : list) {
      if (param.getID() == ParameterIDs.ModelAnalyzer_DetailedOutputEnabled) {
        param.setName("Build automaton model");
        param.setDescription("Disable this to suppress the creation of an " +
                             "output automaton, and only run for statistics.");
        break;
      }
    }
    list.add(new StringParameter
      (ParameterIDs.ModelBuilder_OutputName,
       "Output name",
       "Name of the generated automaton.",
       getOutputName())
      {
        @Override
        public void commitValue()
        {
          setOutputName(getValue());
        }
      });
    list.add(new EnumParameter<ComponentKind>
      (ParameterIDs.AutomatonBuilder_OutputKind,
       "Output kind",
       "Type of the generated automaton.",
       ComponentKind.values())
      {
        @Override
        public void commitValue()
        {
          setOutputKind(getValue());
        }
      });
    return list;
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.analysis.AbstractModelAnalyser
  @Override
  public AutomatonResult createAnalysisResult()
  {
    return new DefaultAutomatonResult(this);
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
  private ComponentKind mOutputKind = ComponentKind.PLANT;

}
