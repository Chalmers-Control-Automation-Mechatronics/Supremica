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

package net.sourceforge.waters.model.compiler.efa;


class SubsumptionResult {

  //#########################################################################
  //# Factory Method
  static SubsumptionResult create(final Kind kind)
  {
    switch (kind) {
    case EQUALS:
      return EQUALS;
    case INTERSECTS:
      return INTERSECTS;
    default:
      return new SubsumptionResult(kind);
    }
  }


  //#########################################################################
  //# Constructors
  private SubsumptionResult(final Kind kind)
  {
    mKind = kind;
  }

  private SubsumptionResult(final Kind kind,
                            final EFAVariableTransitionRelation rel)
  {
    this(kind);
    setTransitionRelation(rel);
  }


  //#########################################################################
  //# Simple Access
  Kind getKind()
  {
    return mKind;
  }

  EFAVariableTransitionRelation getTransitionRelation()
  {
    return mTransitionRelation;
  }

  void setTransitionRelation(final EFAVariableTransitionRelation rel)
  {
    assert mKind == Kind.SUBSUMES || mKind == Kind.SUBSUMED_BY;
    mTransitionRelation = rel;
  }

  SubsumptionResult reverse()
  {
    final Kind kind = reverse(mKind);
    if (kind == mKind) {
      return this;
    } else {
      return new SubsumptionResult(kind, mTransitionRelation);
    }
  }


  //#########################################################################
  //# Static Access
  static Kind combine(final Kind kind1, final Kind kind2)
  {
    switch (kind1) {
    case EQUALS:
      return kind2;
    case SUBSUMED_BY:
      switch (kind2) {
      case SUBSUMES:
      case INTERSECTS:
	return Kind.INTERSECTS;
      default:
	return Kind.SUBSUMED_BY;
      }
    case SUBSUMES:
      switch (kind2) {
      case SUBSUMED_BY:
      case INTERSECTS:
	return Kind.INTERSECTS;
      default:
	return Kind.SUBSUMES;
      }
    case INTERSECTS:
      return Kind.INTERSECTS;
    default:
      throw new IllegalArgumentException
	("Unknown subsumption kind '" + kind1 + "'!");
    }
  }

  static Kind reverse(final Kind kind)
  {
    switch (kind) {
    case SUBSUMED_BY:
      return Kind.SUBSUMES;
    case SUBSUMES:
      return Kind.SUBSUMED_BY;
    default:
      return kind;
    }
  }


  //#########################################################################
  //# Inner Class Kind
  static enum Kind {
    EQUALS,
    SUBSUMED_BY,
    SUBSUMES,
    INTERSECTS;
  };


  //#########################################################################
  //# Data Members
  private final Kind mKind;
  private EFAVariableTransitionRelation mTransitionRelation;


  //#########################################################################
  //# Class Constants
  private static final SubsumptionResult EQUALS =
    new SubsumptionResult(Kind.EQUALS);
  private static final SubsumptionResult INTERSECTS =
    new SubsumptionResult(Kind.INTERSECTS);

}
