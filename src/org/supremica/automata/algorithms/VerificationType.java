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


/**
 * Different types of verification.
 */
public enum VerificationType
{

  //#########################################################################
  //# Enumeration Members
  CONTROLLABILITY("Controllability"),
  INVERSECONTROLLABILITY("Inverse controllability"),
  NONBLOCKING("Nonblocking"),
  CONTROLLABILITYNONBLOCKING("Controllability and nonblocking"),
  //MUTUALLYNONBLOCKING("Mutually nonblocking"),
  LANGUAGEINCLUSION("Language inclusion"),
  /** Observer property (OP-verifier) */
  OP("Observer property"),
  DIAGNOSABILITY("Diagnosability");


  //#########################################################################
  //# Constructors
  private VerificationType(final String description)
  {
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

  public void setConfigOption(final BooleanOption option) {
    mOption = option;
  }


  //#########################################################################
  //# Static Access
  public static VerificationType fromDescription(final String description)
  {
    for (final VerificationType value: values())
    {
      if (value.mDescription.equals(description))
      {
        return value;
      }
    }
    return null;
  }

  public static VerificationType[] enabledValues()
  {
    int enabledCount = 0;
    for (final VerificationType value: values()) {
      if (value.isEnabled()) {
        enabledCount++;
      }
    }
    final VerificationType[] result = new VerificationType[enabledCount];
    int i = 0;
    for (final VerificationType value: values()) {
      if (value.isEnabled()) {
        result[i++] = value;
      }
    }
    return result;
  }


  //#########################################################################
  //# Data Members
  private BooleanOption mOption;
  /** Textual description. */
  private final String mDescription;

}
