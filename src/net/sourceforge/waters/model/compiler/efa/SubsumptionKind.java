//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   SubsumptionKind
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.model.compiler.efa;


enum SubsumptionKind {

  //#########################################################################
  //# Enumeration Definition
  DISJOINT,
  EQUALS,
  SUBSUMED_BY,
  SUBSUMES,
  INTERSECTS;


  //#########################################################################
  //# Arithmetic
  SubsumptionKind combine(final SubsumptionKind kind)
  {
    switch (this) {
    case DISJOINT:
      return DISJOINT;
    case EQUALS:
      return kind;
    case SUBSUMED_BY:
      switch (kind) {
      case DISJOINT:
	return DISJOINT;
      case SUBSUMES:
      case INTERSECTS:
	return INTERSECTS;
      default:
	return SUBSUMED_BY;
      }
    case SUBSUMES:
      switch (kind) {
      case DISJOINT:
	return DISJOINT;
      case SUBSUMED_BY:
      case INTERSECTS:
	return INTERSECTS;
      default:
	return SUBSUMES;
      }
    case INTERSECTS:
      if (kind == DISJOINT) {
	return DISJOINT;
      } else {
	return INTERSECTS;
      }
    default:
      throw new IllegalArgumentException
	("Unknown subsumption kind '" + this + "'!");
    }
  }

}