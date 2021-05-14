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

package org.supremica.properties;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.waters.gui.renderer.EdgeArrowPosition;

/**
 * Helper class to map legacy options from old file formats to up-to-date
 * options. Supports replacement of option names as well us upgrade
 * from Boolean to enumeration.
 *
 * @author Robi Malik, Benjamin Wheeler
 */
public class LegacyOption
{

  //#########################################################################
  //# Static Access
  public static LegacyOption get(final String legacyKey)
  {
    return mLegacyMap.get(legacyKey);
  }


  //#########################################################################
  //# Constructors
  private LegacyOption(final String legacyName,
                       final String replacementName,
                       final boolean ignoringCase,
                       final boolean extending,
                       final String... valueReplacements)
  {
    mLegacyName = legacyName;
    mReplacementName = replacementName;
    mIgnoringCase = ignoringCase;
    mExtending = extending;
    if (valueReplacements.length > 0) {
      assert valueReplacements.length % 2 == 0;
      mReplacementValueMap = new HashMap<>(valueReplacements.length / 2);
      for (int i = 0; i < valueReplacements.length; i += 2) {
        final String value =
          ignoringCase ? valueReplacements[i].toLowerCase() : valueReplacements[i];
        final String replacement = valueReplacements[i+1];
        mReplacementValueMap.put(value, replacement);
      }
    } else {
      mReplacementValueMap = null;
    }
  }


  //#########################################################################
  //# Simple Access
  String getLegacyName()
  {
    return mLegacyName;
  }

  String getReplacementName()
  {
    return mReplacementName;
  }

  String getReplacementValue(final String legacyValue)
  {
    if (mReplacementValueMap == null) {
      return legacyValue;
    }
    final String key = mIgnoringCase ? legacyValue.toLowerCase() : legacyValue;
    final String replacement = mReplacementValueMap.get(key);
    if (replacement == null && mExtending) {
      return legacyValue;
    }
    return replacement;
  }


  //#########################################################################
  //# Static Initialisation
  static {
    mLegacyMap = new HashMap<>(16);
    // gui->general
    createSimpleReplacement("gui.ideFrameWidth", "general.ideFrameWidth");
    createSimpleReplacement("gui.ideFrameHeight", "general.ideFrameHeight");
    createSimpleReplacement("gui.ideFrameX", "general.ideFrameX");
    createSimpleReplacement("gui.ideFrameY", "general.ideFrameY");
    createSimpleReplacement("gui.ideFrameMaximized", "general.ideFrameMaximized");
    // gui->gui.compiler
    createSimpleReplacement("gui.includeInstantiation", "gui.compiler.includeInstantiation");
    createSimpleReplacement("gui.backgroundCompiler", "gui.compiler.backgroundCompiler");
    createSimpleReplacement("gui.optimizingCompiler", "gui.compiler.optimizingCompiler");
    createSimpleReplacement("gui.normalizingCompiler", "gui.compiler.normalizingCompiler");
    createSimpleReplacement("gui.automatonVariablesCompiler", "gui.compiler.automatonVariablesCompiler");
    // algorithms.hmi->gui.compiler
    createSimpleReplacement("algorithms.hmi.expandEFA", "gui.compiler.efsmCompiler");
    // gui->misc
    createSimpleReplacement("gui.includeRASSupport", "misc.includeRASSupport");
    // gui.analyzer.includeDiagnosability ->
    // waters.analysis.diagnosability.Algorithm
    createBooleanReplacement("gui.analyzer.includeDiagnosability",
                             "waters.analysis.diagnosability.Algorithm",
                             "(disabled)", "Monolithic");
    // gui.editor.edgeArrowAtEnd
    createBooleanExtension("gui.editor.edgeArrowAtEnd",
                           EdgeArrowPosition.End.toString(),
                           EdgeArrowPosition.Middle.toString());
  }


  private static void createSimpleReplacement(final String legacyName,
                                              final String replacementName)
  {
    final LegacyOption option =
      new LegacyOption(legacyName, replacementName, false, false);
    mLegacyMap.put(legacyName, option);
  }

  private static void createBooleanReplacement(final String legacyName,
                                               final String replacementName,
                                               final String falseReplacement,
                                               final String trueReplacement)
  {
    final LegacyOption option =
      new LegacyOption(legacyName, replacementName, true, false,
                       "false", falseReplacement, "true", trueReplacement);
    mLegacyMap.put(legacyName, option);
  }

  private static void createBooleanExtension(final String name,
                                             final String falseReplacement,
                                             final String trueReplacement)
  {
    final LegacyOption option =
      new LegacyOption(name, name, true, true,
                       "false", falseReplacement, "true", trueReplacement);
    mLegacyMap.put(name, option);
  }


  //#########################################################################
  //# Data Members
  private final String mLegacyName;
  private final String mReplacementName;
  private Map<String,String> mReplacementValueMap;
  private final boolean mIgnoringCase;
  private final boolean mExtending;

  private static final Map<String,LegacyOption> mLegacyMap;

}
