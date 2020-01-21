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

/**
 *
 * @author Benjamin Wheeler
 */
public class LegacyOptions
{

  public static String get(final String legacyKey) {
    final String newKey = legacyMap.get(legacyKey);
    return newKey != null ? newKey : legacyKey;
  }

  private static void put(final String legacyKey, final String newKey) {
    legacyMap.put(legacyKey, newKey);
  }

  public static void init() {
    if (init) return;

    //gui->general
    put("gui.ideFrameWidth", "general.ideFrameWidth");
    put("gui.ideFrameHeight", "general.ideFrameHeight");
    put("gui.ideFrameX", "general.ideFrameX");
    put("gui.ideFrameY", "general.ideFrameY");
    put("gui.ideFrameMaximized", "general.ideFrameMaximized");
    //gui->gui.compiler
    put("gui.includeInstantiation", "gui.compiler.includeInstantiation");
    put("gui.backgroundCompiler", "gui.compiler.backgroundCompiler");
    put("gui.optimizingCompiler", "gui.compiler.optimizingCompiler");
    put("gui.normalizingCompiler", "gui.compiler.normalizingCompiler");
    put("gui.automatonVariablesCompiler", "gui.compiler.automatonVariablesCompiler");
    put("gui.includeRASSupport", "gui.compiler.includeRASSupport");

  }

  private static final Map<String, String> legacyMap = new HashMap<>();
  private static boolean init = false;

}
