
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

import net.sourceforge.waters.model.analysis.des.ModelAnalyzerFactoryLoader;

import org.supremica.util.BDD.JDDLoader;

public enum SynthesisAlgorithm
{

  //#########################################################################
  //# Enumeration Constants
  MONOLITHIC("Monolithic (explicit)", false),
  MONOLITHIC_WATERS("Monolithic (Waters)", false,
                    ModelAnalyzerFactoryLoader.Monolithic),
  MONOLITHICBDD("Monolithic (symbolic)", false),

  PARTITIONBDD("Partitioning (symbolic)", false),
  CLOCKPARTITION("Clock partitioning", false),
  MINIMALITY_C("Minimality approach (classic)", false),
  MINIMALITY_M("Minimality approach (monolithic)", false),
  MINIMALITY_P("Minimality approach (partition)", false),

  MODULAR("Modular"),
  COMPOSITIONAL("Compositional"),
  //    SYNTHESISA("Compositional (SynthesisA)"),
  COMPOSITIONAL_WATERS("Compositional (Waters)", true,
                       ModelAnalyzerFactoryLoader.Compositional),
  //IDD("IDD"),
  //MonolithicSingleFixpoint("MONOLITHIC (single fixpoint)", false),    // works, but is very slow [due to lame implementation :s ]
  BDD("BDD") {
    @Override
    public boolean isLoadable() {
      return JDDLoader.canLoadJDD();
    }
  };


  //#########################################################################
  //# Constructors
  private SynthesisAlgorithm(final String description)
  {
    this(description, true);
  }

  private SynthesisAlgorithm(final String description,
                             final boolean preferModular)
  {
    this(description, preferModular, null);
  }

  private SynthesisAlgorithm(final String description,
                             final boolean preferModular,
                             final ModelAnalyzerFactoryLoader loader)
  {
    mDescription = description;
    mPrefersModular = preferModular;
    mLoader = loader;
  }


  //#########################################################################
  //# Simple Access
  @Override
  public String toString()
  {
    return mDescription;
  }

  public boolean prefersModular()
  {
    return mPrefersModular;
  }

  public boolean isLoadable()
  {
    return mLoader == null || mLoader.isLoadable();
  }

  public static SynthesisAlgorithm fromDescription(final String description)
  {
    for (final SynthesisAlgorithm value : values()) {
      if (value.mDescription.equals(description)) {
        return value;
      }
    }
    return null;
  }


  //#########################################################################
  //# Instance Variables
  /**
   * Textual description.
   */
  private final String mDescription;
  /**
   * True if this algorithm prefers working on modular systems.
   */
  private final boolean mPrefersModular;
  /**
   * Factory loader used for Waters algorithms,
   * <CODE>null</CODE> for Supremica.
   */
  private ModelAnalyzerFactoryLoader mLoader;
}
