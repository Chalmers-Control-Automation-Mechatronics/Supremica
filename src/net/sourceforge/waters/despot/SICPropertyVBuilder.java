package net.sourceforge.waters.despot;

import java.util.Collection;
import java.util.List;

import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;


public class SICPropertyVBuilder
{
  public SICPropertyVBuilder(final ProductDESProxyFactory factory)
  {
    mFactory = factory;
    mModel = null;
  }

  public SICPropertyVBuilder(final ProductDESProxyFactory factory,
      final ProductDESProxy model)
  {
    mModel = model;
    mFactory = factory;
  }

  /**
   * Sets the model which is having SIC Property V checked.
   *
   * @param model
   */
  public void setInputModel(final ProductDESProxy model)
  {
    mModel = model;
  }

  /**
   * Gets all the answer events that belong to the model.
   *
   * @return
   */
  public Collection<EventProxy> getAnswerEvents()
  {
    return null;
  }

  /**
   * Builds a model for a given answer event.
   *
   * @param answerNm
   *          The name of the answer event.
   * @return
   */
  public ProductDESProxy createModelForAnswer(final EventProxy answer)
  {
    return null;
  }

  @SuppressWarnings("unused")
  private AutomatonProxy createModifiedLowLevelAutomaton
    (final AutomatonProxy aut, final EventProxy answer)
  {
    return null;
  }

  @SuppressWarnings("unused")
  private AutomatonProxy createTestForAnswer(final EventProxy answer)
  {
    return null;
  }

  @SuppressWarnings("unused")
  private AutomatonProxy createModifiedInterfaceAutomaton
    (final AutomatonProxy aut)
  {
    return null;
  }

  // #########################################################################
  // # Data Members
  /**
   * The model which is being changed.
   */
  @SuppressWarnings("unused")
  private ProductDESProxy mModel;

  @SuppressWarnings("unused")
  private final ProductDESProxyFactory mFactory;

  /**
   * A list of the low level automaton that are created with the new marking
   * rules.
   */
  @SuppressWarnings("unused")
  private List<AutomatonProxy> mLowLevelAutomata;

}
