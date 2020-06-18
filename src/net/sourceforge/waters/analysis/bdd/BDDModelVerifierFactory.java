//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.analysis.bdd;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.EnumOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.PositiveIntOption;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.analysis.des.ModelAnalyzer;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model verifier factory to produce BDD-based model verifiers.
 *
 * @author Robi Malik
 */

public class BDDModelVerifierFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Implementation
  public static BDDModelVerifierFactory getInstance()
  {
    return SingletonHolder.theInstance;
  }

  private static class SingletonHolder {
    private static final BDDModelVerifierFactory theInstance =
      new BDDModelVerifierFactory();
  }


  //#########################################################################
  //# Constructors
  private BDDModelVerifierFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzerFactory
  @Override
  public BDDConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDConflictChecker(factory);
  }

  @Override
  public BDDControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDControllabilityChecker(factory);
  }

  @Override
  public BDDDeadlockChecker createDeadlockChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDDeadlockChecker(factory);
  }

  @Override
  public BDDLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new BDDLanguageInclusionChecker(factory);
  }

  @Override
  public BDDStateCounter createStateCounter
    (final ProductDESProxyFactory factory)
  {
    return new BDDStateCounter(factory);
  }


  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);
    db.add(new EnumOption<BDDPackage>
             (OPTION_BDDModelAnalyzer_BDDPackage,
              "BDD package",
              "The BDD implementation used to manipulate BDDs.",
              "-pack",
              BDDPackage.values()));
    db.add(new PositiveIntOption
             (OPTION_BDDModelAnalyzer_InitialSize,
              "Initial BDD table size",
              "The initial number of BDD nodes to be supported by the BDD package.",
               "-size",
               50000));
    db.add(new PositiveIntOption
             (OPTION_BDDModelAnalyzer_NodeLimit,
              "BDD Node limit",
              "Maximum number of BDD nodes allowed before aborting.",
               "-limit"));
    db.add(new PositiveIntOption
             (OPTION_BDDModelAnalyzer_PartitionSizeLimit,
              "Partition size limit",
              "The maximum number of BDD nodes allowed in a transition relation " +
              "BDD before it is split when partitioning.",
              "-plimit",
              10000));
    db.add(new BooleanOption
             (OPTION_BDDModelAnalyzer_ReorderingEnabled,
              "Dynamic variable reordering",
              "Try to improve the BDD variable ordering between iterations.",
              "-dynamic",
              false));
    db.add(new EnumOption<TransitionPartitioningStrategy>
             (OPTION_BDDModelAnalyzer_TransitionPartitioningStrategy,
              "Transition partitioning strategy",
              "The method used to split the transition relation BDD into " +
              "disjunctive components.",
              "-part",
              TransitionPartitioningStrategy.values()));
    db.add(new EnumOption<VariableOrdering>
             (OPTION_BDDModelAnalyzer_VariableOrdering,
              "Initial variable ordering",
              "The strategy to determine the initial ordering of the BDD variables.",
              "-order",
              VariableOrdering.values()));
  }


  //#########################################################################
  //# Supremica Options
  /**
   * Configures a BDD model verifier from Supremica options, if these
   * are available. This method uses reflection to avoid compile-time
   * dependency on Supremica licensed code.
   */
  @Override
  public void configureFromOptions(final ModelAnalyzer analyzer)
  {
    if (analyzer instanceof BDDModelVerifier) {
      final ClassLoader loader = getClass().getClassLoader();
      try {
        final Class<?> clazz = loader.loadClass(CONFIG_BRIDGE);
        final Method method =
          clazz.getMethod("configureModelAnalyzer", BDDModelVerifier.class);
        method.invoke(null, analyzer);
      } catch (final ClassNotFoundException exception) {
        // Can't access Config --- use default settings
      } catch (NoSuchMethodException |
        InvocationTargetException |
        IllegalAccessException exception) {
        throw new WatersRuntimeException(exception);
      }
    }
  }


  //#########################################################################
  //# Class Constants
  private static final String CONFIG_BRIDGE =
    "net.sourceforge.waters.gui.util.ConfigBridge";


  //#########################################################################
  //# Class Constants
  public static final String OPTION_BDDModelAnalyzer_BDDPackage =
    "BDDModelAnalyzer.BDDPackage";
  public static final String OPTION_BDDModelAnalyzer_InitialSize =
    "BDDModelAnalyzer.InitialSize";
  public static final String OPTION_BDDModelAnalyzer_NodeLimit =
    "BDDModelAnalyzer.NodeLimit";
  public static final String OPTION_BDDModelAnalyzer_PartitionSizeLimit =
    "BDDModelAnalyzer.PartitionSizeLimit";
  public static final String OPTION_BDDModelAnalyzer_ReorderingEnabled =
    "BDDModelAnalyzer.ReorderingEnabled";
  public static final String OPTION_BDDModelAnalyzer_TransitionPartitioningStrategy =
    "BDDModelAnalyzer.TransitionPartitioningStrategy";
  public static final String OPTION_BDDModelAnalyzer_VariableOrdering =
    "BDDModelAnalyzer.VariableOrdering";

}
