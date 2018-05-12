//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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

package net.sourceforge.waters.analysis.hisc;

import net.sourceforge.waters.model.compiler.ModuleCompiler;

/**
 * <P>An enumeration to store different compiler settings to compile a
 * module hierarchy while supporting HISC.</P>
 *
 * <P>Hierarchical Interface-Based Supervisory Control (HISC) structures a
 * system into a hierarchy of components interacting via dedicated interface
 * automata. This structure is emulated in Waters by the module hierarchy.
 * With the HISC compile mode, the {@link ModuleCompiler} can be instructed
 * to compile only a part of a module hierarchy, replacing other modules
 * by their interfaces.</P>
 *
 * <P><I>Reference:</I><BR>
 * Ryan J. Leduc, Bertil A. Brandin, Mark Lawford, and W. M. Wonham.
 * Hierarchical Interface-Based Supervisory Control - Part&nbsp;I:
 * Serial Case. IEEE Transitions on Automatic Control,
 * <STRONG>50</STRONG>&nbsp;(9), September 2005, 1322-1335.</P>
 *
 * @author Robi Malik
 */

public enum HISCCompileMode
{

  /**
   * Constant indicating that HISC is disabled (the default setting).
   * This results in all instances being compiled recursively, and all
   * automata collected in a single large model.
   */
  NOT_HISC,

  /**
   * Constant used when compiling a high-level module.
   * When compiling a high-level module, all automata are compiled.
   * Instances are accessed, and their modules are compiled as low-level
   * modules. This result in a model containing the plants, specifications,
   * and supervisors of the high level and the interfaces of the low levels
   * converted to plants.
   */
  HISC_HIGH,

  /**
   * Constant used when compiling a low-level module.
   * When compiling a low-level module, only interface automata are
   * compiled and converted to plants. Other automata, variables, and
   * instances are ignored. This setting is only used internally when
   * compiling instances found in a high-level module.
   */
  HISC_LOW;

}
