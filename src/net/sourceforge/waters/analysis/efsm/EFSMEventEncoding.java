//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters EFSM Analysis
//# PACKAGE: net.sourceforge.waters.analysis.efsm
//# CLASS:   EFSMEventEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.efsm;

import net.sourceforge.waters.analysis.efa.AbstractEFAEventEncoding;
import net.sourceforge.waters.analysis.tr.EventEncoding;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.compiler.constraint.ConstraintList;


/**
 * @author Robi Malik, Sahar Mohajerani
 */

public class EFSMEventEncoding extends AbstractEFAEventEncoding<ConstraintList>
{

  //#########################################################################
  //# Constructors
  EFSMEventEncoding()
  {
  }

  EFSMEventEncoding(final int size)
  {
    super(size);
    // empty constraint list represents true
    final ConstraintList empty = new ConstraintList();
    createEventId(empty);
  }

  EFSMEventEncoding(final EFSMEventEncoding encoding)
  {
    super(encoding);
  }


  //#########################################################################
  //# Simple Access
  void setSelfloops(final ListBufferTransitionRelation rel,
                    final EFSMVariableFinder finder)
  {
    for (int e = EventEncoding.NONTAU; e < size(); e++) {
      final ConstraintList update = getUpdate(e);
      if (!finder.findPrime(update)) {
        final byte status = rel.getProperEventStatus(e);
        rel.setProperEventStatus
          (e, status | EventEncoding.STATUS_OUTSIDE_ONLY_SELFLOOP);
      }
    }
  }

}

