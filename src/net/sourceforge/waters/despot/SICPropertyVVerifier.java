package net.sourceforge.waters.despot;

import java.util.List;

import net.sourceforge.waters.model.analysis.AbstractModelVerifier;
import net.sourceforge.waters.model.analysis.AnalysisException;
import net.sourceforge.waters.model.analysis.ConflictChecker;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class SICPropertyVVerifier extends AbstractModelVerifier
{

  public SICPropertyVVerifier(final ProductDESProxyFactory factory)
  {
    this(null, factory);
  }

  public SICPropertyVVerifier(final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(model, factory);
  }

  public SICPropertyVVerifier(final ConflictChecker checker,
                              final ProductDESProxy model,
                              final ProductDESProxyFactory factory)
  {
    super(model, factory);
    mChecker = checker;
  }

  public void setConflictChecker(final ConflictChecker checker)
  {
    mChecker = checker;
  }

  public boolean run() throws AnalysisException
  {
    final ProductDESProxy model = getModel();
    final SICPropertyVBuilder builder =
        new SICPropertyVBuilder(model, getFactory());
    final List<EventProxy> answers =
        (List<EventProxy>) builder.getAnswerEvents();
    setConflictCheckerMarkings(builder);
    ProductDESProxy convertedModel = null;
    boolean result = true;
    for (final EventProxy answer : answers) {
      convertedModel = builder.createModelForAnswer(answer);
      mChecker.setModel(convertedModel);
      result &= mChecker.run();
      if (!result) {
        break;
      }
    }
    return result;
  }

  private void setConflictCheckerMarkings(final SICPropertyVBuilder builder)
  {
    builder.setDefaultMarkings();
    final EventProxy defaultMark = builder.getMarkingProposition();
    final EventProxy preconditionMark = builder.getGeneralisedPrecondition();
    mChecker.setMarkingProposition(defaultMark);
    mChecker.setGeneralisedPrecondition(preconditionMark);
  }

  // #########################################################################
  // # Data Members
  private ConflictChecker mChecker;

}
