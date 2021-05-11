//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */

package org.supremica.automata.waters;

import net.sourceforge.waters.analysis.options.BooleanOption;
import net.sourceforge.waters.analysis.options.OptionPage;
import net.sourceforge.waters.analysis.options.StringOption;
import net.sourceforge.waters.model.analysis.des.AbstractModelAnalyzerFactory;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


/**
 * A model analyser factory that produces model analysers that invoke
 * Supremica's monolithic algorithms.
 *
 * @author Robi Malik
 */

public class SupremicaModelAnalyzerFactory
  extends AbstractModelAnalyzerFactory
{

  //#########################################################################
  //# Singleton Pattern
  public static SupremicaModelAnalyzerFactory getInstance()
  {
    return SingletonHolder.INSTANCE;
  }

  private static class SingletonHolder {
    private static final SupremicaModelAnalyzerFactory INSTANCE =
      new SupremicaModelAnalyzerFactory();
  }


  //#########################################################################
  //# Constructors
  private SupremicaModelAnalyzerFactory()
  {
  }


  //#########################################################################
  //# Interface net.sourceforge.waters.model.analysis.ModelAnalyzerFactory
  @Override
  public SupremicaMonolithicConflictChecker createConflictChecker
    (final ProductDESProxyFactory factory)
  {
    return new SupremicaMonolithicConflictChecker(factory);
  }

  @Override
  public SupremicaMonolithicControllabilityChecker createControllabilityChecker
    (final ProductDESProxyFactory factory)
  {
    return new SupremicaMonolithicControllabilityChecker(factory);
  }

  @Override
  public SupremicaMonolithicLanguageInclusionChecker createLanguageInclusionChecker
    (final ProductDESProxyFactory factory)
  {
    return new SupremicaMonolithicLanguageInclusionChecker(factory);
  }

  @Override
  public SupremicaMonolithicSynthesizer createSupervisorSynthesizer
    (final ProductDESProxyFactory factory)
  {
    return new SupremicaMonolithicSynthesizer(factory);
  }

  @Override
  public SupremicaSynchronousProductBuilder createSynchronousProductBuilder
    (final ProductDESProxyFactory factory)
  {
    return new SupremicaSynchronousProductBuilder(factory);
  }


  @Override
  public void registerOptions(final OptionPage db)
  {
    super.registerOptions(db);

    db.register(new BooleanOption
             (OPTION_SupremicaMonolithicVerifier_DetailedOutputEnabled,
              "Print counterexample",
              "Show trace to bad state as info in log.",
              "-out",
              false));

    db.register(new BooleanOption
             (OPTION_SupremicaSynchronousProductBuilder_EnsuringUncontrollablesInPlant,
              "Add uncontrollables to plant",
              "Treat uncontrollable events that appear in specifications " +
              "but not in plants as always enabled.",
              "-aup",
              true));
    db.register(new BooleanOption
             (OPTION_SupremicaSynchronousProductBuilder_ExpandingForbiddenStates,
              "Expand forbidden states",
              "If enabled, transitions from forbidden states are examined. " +
              "Otherwise forbidden states are considered terminal.",
              "-ef",
              true));
    db.register(new BooleanOption
             (OPTION_SupremicaSynchronousProductBuilder_MarkingUncontrollableStatesAsForbidden,
              "Forbid uncontrollable states",
              "Mark uncontrollable states as forbidden in the synchronous composition.",
              "-fu",
              true));
    db.register(new BooleanOption
             (OPTION_SupremicaSynchronousProductBuilder_RememberingDisabledEvents,
              "Remember disabled events",
              "Add transitions to a 'dump' state for all events enabled in the plant " +
              "but disabled by a specification.",
              "-du",
              false));
    db.register(new BooleanOption
             (OPTION_SupremicaSynchronousProductBuilder_ShortStateNames,
              "Short state names",
              "Use short state instead of detailed state tuple information.",
              "-short",
              false));
    db.register(new StringOption
             (OPTION_SupremicaSynchronousProductBuilder_StateNameSeparator,
              "State name separator",
              "Separator for state tuple components when using long state names.",
              "-sep",
               "."));
    db.register(new BooleanOption
             (OPTION_SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents,
              "Synchronise on unobservable events",
              "If enabled, treat unoberservable as shared events in synchronisation, " +
              "otherwise allow them to be executed independently by each component.",
              "-uos",
              true));

    db.register(new BooleanOption
             (OPTION_SupremicaSupervisorSynthesizer_Purging,
              "Purge result",
              "Remove unreachable states from the synthesised supervisor.",
              "-pirge",
              true));
    db.register(new BooleanOption
             (OPTION_SupremicaSupervisorSynthesizer_SupervisorReduction,
              "Reduce supervisors",
              "Minimize computed supervisors using min-state algorithm.",
              "-red",
              false));
  }


  //#########################################################################
  //# Class Constants
  public static final String
    OPTION_SupremicaMonolithicVerifier_DetailedOutputEnabled =
    "SupremicaMonolithicVerifier.DetailedOutputEnabled";

  public static final String
    OPTION_SupremicaSynchronousProductBuilder_EnsuringUncontrollablesInPlant =
    "SupremicaSynchronousProductBuilder.EnsuringUncontrollablesInPlant";
  public static final String
    OPTION_SupremicaSynchronousProductBuilder_ExpandingForbiddenStates =
    "SupremicaSynchronousProductBuilder.ExpandingForbiddenStates";
  public static final String
    OPTION_SupremicaSynchronousProductBuilder_MarkingUncontrollableStatesAsForbidden =
    "SupremicaSynchronousProductBuilder.MarkingUncontrollableStatesAsForbidden";
  public static final String
    OPTION_SupremicaSynchronousProductBuilder_RememberingDisabledEvents =
    "SupremicaSynchronousProductBuilder.RememberingDisabledEvents";
  public static final String
    OPTION_SupremicaSynchronousProductBuilder_ShortStateNames =
    "SupremicaSynchronousProductBuilder.ShortStateNames";
  public static final String
    OPTION_SupremicaSynchronousProductBuilder_StateNameSeparator =
    "SupremicaSynchronousProductBuilder.StateNameSeparator";
  public static final String
    OPTION_SupremicaSynchronousProductBuilder_SynchronisingOnUnobservableEvents =
    "SupremicaSynchronousProductBuilder.SynchronisingOnUnobservableEvents";

  public static final String
    OPTION_SupremicaSupervisorSynthesizer_Purging =
    "SupremicaSupervisorSynthesizer.Purging";
  public static final String
    OPTION_SupremicaSupervisorSynthesizer_SupervisorReduction =
    "SupremicaSupervisorSynthesizer.SupervisorReduction";

}
