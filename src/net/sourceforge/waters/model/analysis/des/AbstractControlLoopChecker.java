//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

import net.sourceforge.waters.analysis.options.EventSetOption;
import net.sourceforge.waters.analysis.options.LeafOptionPage;
import net.sourceforge.waters.analysis.options.Option;
import net.sourceforge.waters.model.analysis.kindtranslator.ControlLoopKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.ControllabilityKindTranslator;
import net.sourceforge.waters.model.analysis.kindtranslator.KindTranslator;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.LoopCounterExampleProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * An abstract base class that can be used for all control-loop checker
 * implementations. In addition to the model and factory members inherited
 * from {@link AbstractModelVerifier}, this class provides some support to
 * return a loop error trace of the appropriate kind.
 *
 * @author Robi Malik
 */

public abstract class AbstractControlLoopChecker
  extends AbstractModelVerifier
  implements ControlLoopChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new control-loop checker without a model and with default
   * kind translator.
   * @param  factory    Factory used for trace construction.
   */
  public AbstractControlLoopChecker(final ProductDESProxyFactory factory)
  {
    this(factory, ControllabilityKindTranslator.getInstance());
  }

  /**
   * Creates a new control-loop checker to check for loops with respect to
   * an alternative set of events.
   * @param  factory    Factory used for trace construction.
   * @param  translator Kind translator to determine loop events. The checker
   *                    will look for loops consisting of events designated
   *                    as controllable by the kind translator.
   */
  public AbstractControlLoopChecker(final ProductDESProxyFactory factory,
                                    final KindTranslator translator)
  {
    this(null, factory, translator);
  }

  /**
   * Creates a new control-loop checker to check whether the given model
   * is control-loop free.
   * @param  model      The model to be checked by this control-loop checker.
   * @param  factory    Factory used for trace construction.
   */
  public AbstractControlLoopChecker(final ProductDESProxy model,
                                 final ProductDESProxyFactory factory)
  {
    this(model, factory, ControllabilityKindTranslator.getInstance());
  }

  /**
   * Creates a new control-loop checker to check whether the given model
   * is loop free with respect to an alternative set of events.
   * @param  model      The model to be checked by this control-loop checker.
   * @param  factory    Factory used for trace construction.
   * @param  translator Kind translator to determine loop events. The checker
   *                    will look for loops consisting of events designated
   *                    as controllable by the kind translator.
   */
  public AbstractControlLoopChecker(final ProductDESProxy model,
                                    final ProductDESProxyFactory factory,
                                    final KindTranslator translator)
  {
    super(model, factory, translator);
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzer
   @Override
  public List<Option<?>> getOptions(final LeafOptionPage db)
  {
    final List<Option<?>> options = super.getOptions(db);
    db.prepend(options, AbstractModelAnalyzerFactory.
                        OPTION_ControlLoopChecker_LoopEvents);
    return options;
  }

  @Override
  public void setOption(final Option<?> option)
  {
    if (option.hasID(AbstractModelAnalyzerFactory.
                     OPTION_ControlLoopChecker_LoopEvents)) {
      final EventSetOption eventSetOption = (EventSetOption) option;
      final KindTranslator translator =
        new ControlLoopKindTranslator(eventSetOption.getValue());
      setKindTranslator(translator);
    } else {
      super.setOption(option);
    }
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ControlLoopChecker
  @Override
  public LoopCounterExampleProxy getCounterExample()
  {
    return (LoopCounterExampleProxy) super.getCounterExample();
  }

  @Override
  public Collection<EventProxy> getNonLoopEvents()
  {
    throw new UnsupportedOperationException
      (ProxyTools.getShortClassName(this) + " does not calculate non-loop events!");
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  protected String getTraceName()
  {
    final ProductDESProxy model = getModel();
    return getTraceName(model);
  }

  /**
   * Gets a name that can be used for a counterexample for the given model.
   */
  public static String getTraceName(final ProductDESProxy model)
  {
    final String modelName = model.getName();
    return modelName + "-loop";
  }

}
