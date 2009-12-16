package net.sourceforge.waters.despot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

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
    final Set<EventProxy> allEvents = mModel.getEvents();
    final List<EventProxy> answerEvents = new ArrayList<EventProxy>(0);
    for (final EventProxy event : allEvents) {
      if (event.getAttributes().equals(HISCAttributes.ATTRIBUTES_ANSWER)) {
        answerEvents.add(event);
      }
    }
    return answerEvents;
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
  private AutomatonProxy createTestForAnswer(final EventProxy answer)
  {
    return null;
  }

  @SuppressWarnings("unused")
  /**
   * This returns a new AutomatonProxy which is an altered version of the interface automaton passed to the method.
   */
  private AutomatonProxy createModifiedInterfaceAutomaton(
      final AutomatonProxy aut, final EventProxy answer)
  {
    return null;
  }

  @SuppressWarnings("unused")
  /**
   * This returns a new AutomatonProxy which is an altered version of the low level automaton passed to the method.
   * All states are marked with the :alpha precondition and :accepting marking.
   * Instead of explicitly making these markings for every state, the existing markings are removed from the event alphabet of the given automaton.
   */
  private AutomatonProxy createModifiedLowLevelAutomaton(
      final AutomatonProxy aut)
  {
    return null;
  }

  // #########################################################################
  // # Data Members
  /**
   * The model which is being changed.
   */
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
