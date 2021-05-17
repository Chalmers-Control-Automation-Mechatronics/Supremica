/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */


package org.supremica.automata.algorithms;

import net.sourceforge.waters.model.options.BooleanOption;


public enum EquivalenceRelation
{

  //#########################################################################
  //# Enumeration Members
  /** Language equivalence. */
  LANGUAGEEQUIVALENCE("Language equivalence"),
  /** Observer projection (OP-search) */
  OP("Observer projection"),//, ConfigOptions.GUI_ANALYZER_INCLUDE_OP),
  /** Conflict equivalence. */
  CONFLICTEQUIVALENCE("Conflict equivalence"),
  /** Supervision equivalence. */
  SUPERVISIONEQUIVALENCE("Supervision equivalence"),
  /** Synthesis abstraction. */
  SYNTHESISABSTRACTION("Synthesis abstraction"),
  /** Observation equivalence (aka Weak bisimulation equivalence). */
  OBSERVATIONEQUIVALENCE("Observation equivalence");
  /* Bisimulation equivalence (aka Strong bisimulation equivalence).
  BISIMULATIONEQUIVALENCE("Bisimulation equivalence");
  */


  //#########################################################################
  //# Constructors
  private EquivalenceRelation(final String description)
  {
    this(description, null);
  }

  private EquivalenceRelation(final String description,
                              final BooleanOption option)
  {
    mOption = option;
    mDescription = description;
  }


  //#########################################################################
  //# Data Members
  @Override
  public String toString()
  {
    return mDescription;
  }


  //#########################################################################
  //# Simple Access
  boolean isEnabled()
  {
    if (mOption == null) {
      return true;
    } else {
      return mOption.getValue();
    }
  }


  //#########################################################################
  //# Static Access
  public static EquivalenceRelation fromDescription(final String description)
  {
    for (final EquivalenceRelation value: values())
    {
      if (value.mDescription.equals(description))
      {
        return value;
      }
    }
    return null;
  }

  public static EquivalenceRelation[] enabledValues()
  {
    int enabledCount = 0;
    for (final EquivalenceRelation value: values()) {
      if (value.isEnabled()) {
        enabledCount++;
      }
    }
    final EquivalenceRelation[] relations =
      new EquivalenceRelation[enabledCount];
    int i = 0;
    for (final EquivalenceRelation value: values()) {
      if (value.isEnabled()) {
        relations[i++] = value;
      }
    }
    return relations;
  }


  //#########################################################################
  //# Data Members
  private final BooleanOption mOption;
  /** Textual description. */
  private final String mDescription;

}
