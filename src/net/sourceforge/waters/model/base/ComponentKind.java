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

package net.sourceforge.waters.model.base;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;


/**
 * Enumeration of component types.
 * Components (i.e., automata) are classified as plants or specifications
 * according to supervisory control theory. In addition, Waters supports
 * properties (for language inclusion check) and supervisors (for
 * synthesis).
 *
 * @see AutomatonProxy
 * @see SimpleComponentProxy
 *
 * @author Robi Malik
 */

public enum ComponentKind {

  /**
   * Descriptor for plants.
   * According to supervisory control theory, a plant is an automaton that
   * models the possible behaviour of the system to be controlled.
   */
  PLANT,

  /**
   * Descriptor for specifications.
   * According to supervisory control theory, a specification is an automaton
   * that models the desired behaviour of a controlled system.
   * A specification can be considered as a model of control software
   * that restricts the system by disabling controllable events&mdash;but if
   * a specifications an uncontrollable event possible in the plant, it is
   * an uncontrollable specification that cannot be implemented.
   */
  SPEC,

  /**
   * Descriptor for properties.
   * A property is an automaton that represents desired system behaviour,
   * but unlike a specification it is never part of the controlled or
   * uncontrolled system. Properties are used by the language inclusion
   * check to determine whether or not a system behaves as desired.
   */
  PROPERTY,

  /**
   * Descriptor for supervisors.
   * Supervisors are the automatically generated results from synthesis.
   * The act as specification, but the identification as supervisor makes
   * it possible to replace the automatically generated components when
   * synthesis is performed repeatedly.
   */
  SUPERVISOR;

}
