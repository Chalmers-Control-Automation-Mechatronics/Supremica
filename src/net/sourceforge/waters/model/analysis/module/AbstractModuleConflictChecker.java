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

package net.sourceforge.waters.model.analysis.module;

import net.sourceforge.waters.model.analysis.kindtranslator.ConflictKindTranslator;
import net.sourceforge.waters.model.des.ConflictCounterExampleProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;


/**
 * An abstract base class that can be used for all module conflict checker
 * implementations. In addition to the model and factory members inherited
 * from {@link AbstractModuleVerifier}, this class provides some support to
 * get and set the default marking, and to return an error trace of the
 * appropriate kind.
 *
 * @author Robi Malik, Sahar Mohajerani
 */

public abstract class AbstractModuleConflictChecker
  extends AbstractModuleVerifier
  implements ModuleConflictChecker
{

  //#########################################################################
  //# Constructors
  /**
   * Creates a new conflict checker without a model or marking
   * proposition.
   */
  public AbstractModuleConflictChecker(final ModuleProxyFactory factory)
  {
    this(null, factory);
  }

  /**
   * Creates a new conflict checker to check whether the given model
   * nonconflicting with respect to the default marking proposition.
   * @param  model      The model to be checked by this conflict checker.
   * @param  factory    Factory used for trace construction.
   */
  public AbstractModuleConflictChecker(final ModuleProxy model,
                                       final ModuleProxyFactory factory)
  {
    this(model, null, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The name of the proposition event that defines which states are
   *          marked. Every state has a list of propositions attached to it;
   *          the conflict checker considers only those states as marked that
   *          are labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param factory
   *          Factory used for trace construction.
   */
  public AbstractModuleConflictChecker(final ModuleProxy model,
                                       final IdentifierProxy marking,
                                       final ModuleProxyFactory factory)
  {
    this(model, marking, null, factory);
  }

  /**
   * Creates a new conflict checker to check a particular model.
   *
   * @param model
   *          The model to be checked by this conflict checker.
   * @param marking
   *          The name of the proposition event that defines which states are
   *          marked. Every state has a list of propositions attached to it;
   *          the conflict checker considers only those states as marked that
   *          are labelled by <CODE>marking</CODE>, i.e., their list of
   *          propositions must contain this event (exactly the same object).
   * @param preMarking
   *          The name of the proposition event that defines which states have
   *          alpha (precondition) markings for a generalised nonblocking
   *          check.
   * @param factory
   *          Factory used for trace construction.
   */
    public AbstractModuleConflictChecker(final ModuleProxy model,
                                         final IdentifierProxy marking,
                                         final IdentifierProxy preMarking,
                                         final ModuleProxyFactory factory)
    {
      super(model, factory, ConflictKindTranslator.getInstanceUncontrollable());
      mMarking = marking;
      mPreconditionMarking = preMarking;
      mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModuleAnalyser
  @Override
  public void setModel(final ModuleProxy model)
  {
    super.setModel(model);
    mUsedMarking = null;
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModuleConflictChecker
  @Override
  public void setConfiguredDefaultMarking(final IdentifierProxy marking)
  {
    mMarking = marking;
    mUsedMarking = null;
  }

  @Override
  public IdentifierProxy getConfiguredDefaultMarking()
  {
    return mMarking;
  }

  @Override
  public void setConfiguredPreconditionMarking(final IdentifierProxy marking){
    mPreconditionMarking = marking;
  }

  @Override
  public IdentifierProxy getConfiguredPreconditionMarking(){
    return mPreconditionMarking;
  }

  @Override
  public ConflictCounterExampleProxy getCounterExample()
  {
    return (ConflictCounterExampleProxy) super.getCounterExample();
  }


  //#########################################################################
  //# Overrides for net.sourceforge.waters.model.ModuleAnalyser
  @Override
  protected void tearDown()
  {
    mUsedMarking = null;
    super.tearDown();
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Gets the marking proposition to be used.
   * This method returns the marking proposition specified by the {@link
   * #setConfiguredDefaultMarking(IdentifierProxy) setMarkingProposition()}
   * method, if non-null, or the default marking proposition of the input model.
   * @throws IllegalArgumentException to indicate that the a
   *         <CODE>null</CODE> marking was specified, but input model does
   *         not contain any proposition with the default marking name.
   */
  protected IdentifierProxy getUsedDefaultMarking()
  {
    if (mUsedMarking == null) {
      final String name = EventDeclProxy.DEFAULT_MARKING_NAME;
      mUsedMarking = getFactory().createSimpleIdentifierProxy(name);
    }
    return mUsedMarking;
  }

  /**
   * Gets a name that can be used for a counterexample for the current model.
   */
  protected String getTraceName()
  {
    final ModuleProxy model = getModel();
    return getTraceName(model);
  }

  /**
   * Gets a name that can be used for a counterexample for the given model.
   */
  public static String getTraceName(final ModuleProxy model)
  {
    final String modelname = model.getName();
    return modelname + "-conflicting";
  }


  //#########################################################################
  //# Data Members
  private IdentifierProxy mMarking;
  private IdentifierProxy mUsedMarking;
  private IdentifierProxy mPreconditionMarking;
}
