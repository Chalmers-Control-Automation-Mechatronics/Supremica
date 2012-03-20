package net.sourceforge.waters.analysis.certainconf;

import net.sourceforge.waters.analysis.abstraction.AbstractMarkingTRSimplifier;
import net.sourceforge.waters.analysis.tr.ListBufferTransitionRelation;
import net.sourceforge.waters.model.analysis.AnalysisException;


class CertainConflictsTRSimplifier extends AbstractMarkingTRSimplifier {

  public CertainConflictsTRSimplifier()
  {
  }

  public CertainConflictsTRSimplifier(final ListBufferTransitionRelation rel)
  {
    super(rel);
  }

    @Override
    protected void setUp()  throws AnalysisException {
        super.setUp();
    }

    @Override
    protected void tearDown() {
        super.tearDown();
    }

    @Override
    protected boolean runSimplifier() throws AnalysisException
    {
      // TODO Auto-generated method stub
      return false;
    }
}