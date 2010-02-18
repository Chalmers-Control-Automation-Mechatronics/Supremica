//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.analysis.gnonblocking
//# CLASS:   minS
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.gnonblocking;

import java.util.Collection;


/**
 * @author rmf18
 */
public class minS implements Heuristic
{

  final Collection<Candidate> mCandidates;

  public minS(final Collection<Candidate> candidates)
  {
    mCandidates = candidates;
  }

  public Collection<Candidate> evaulate()
  {
    // TODO Auto-generated method stub
    return null;
  }

}
