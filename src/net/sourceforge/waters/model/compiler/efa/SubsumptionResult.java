//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler.efa
//# CLASS:   SubsumptionResult
//###########################################################################
//# $Id$
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
    switch (mKind) {
    case SUBSUMED_BY:
      return new SubsumptionResult(Kind.SUBSUMES, mTransitionRelation);
    case SUBSUMES:
      return new SubsumptionResult(Kind.SUBSUMED_BY, mTransitionRelation);
    default:
      return this;
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
