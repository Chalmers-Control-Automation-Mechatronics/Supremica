//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
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

package net.sourceforge.waters.model.expr;


/**
 * <P>The common interface for unary and binary operators.</P>
 *
 * <P>Operators are used by parsers and scanners in situations where
 * expressions need to be created from operator names, before the class
 * of the expression is known. The can be looked up in the operator table
 * ({@link OperatorTable}) by their name and contain information such
 * as the binding priority of operators and instructions how to construct
 * the actual expression.</P>
 *
 * @author Robi Malik
 */

public interface Operator {

  //#########################################################################
  //# Simple Access Methods
  public String getName();

  public int getPriority();

  //#########################################################################
  //# Class Constants
  public static final int TYPE_BOOLEAN = 1;
  public static final int TYPE_INT = 2;
  public static final int TYPE_ATOM = 4;
  public static final int TYPE_RANGE = 8;
  public static final int TYPE_NAME = 16;

  public static final int TYPE_INDEX = 
    TYPE_BOOLEAN | TYPE_INT | TYPE_ATOM;
  public static final int TYPE_ARITHMETIC = 
    TYPE_BOOLEAN | TYPE_INT | TYPE_ATOM | TYPE_NAME;
  public static final int TYPE_ANY =
    TYPE_BOOLEAN | TYPE_INT | TYPE_ATOM | TYPE_RANGE | TYPE_NAME;

}
