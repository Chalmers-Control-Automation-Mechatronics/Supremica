//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: COMP452/552-12A Assignment 3
//# PACKAGE: net.sourceforge.waters.analysis.comp552
//# CLASS:   BDDEncoding
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.analysis.comp552;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.AutomatonTools;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxy;
import net.sourceforge.waters.model.des.StateProxy;
import net.sourceforge.waters.model.des.TransitionProxy;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 * <P>A utility class to encode the events and automata of a model
 * ({@link ProductDESProxy}) with BDDs.</P>
 *
 * <P>This class encodes the transition relation using a conjunctive
 * per-automaton approach. Variables are allocated to encode the events
 * and the current and next states of each automaton. To compute the
 * synchronous product transition relation, first the transition relation
 * is built for each automaton, tagged with event codes. The transition
 * relations of all automata are combined using logical AND, and the event
 * bits are removed from the result using existential quantification.</P>
 *
 * <P>The variable ordering used is such that the event variables appear first,
 * followed by the current and next state bits of all automata. The bits for
 * current and next state of each automaton are clustered together, with the
 * automata being ordered in the way the appear in the input model.
 * Interleaving is used for the current and next state bits of all automata.
 * Events and states are encoded in binary, with the most significant bit
 * appearing first in the ordering.</P>
 *
 * <H4>You are encouraged to modify this class or replace it by something
 * better.</H4>
 *
 * <P>This class mainly serves as a demonstration how to encode
 * automata using BDDs. It is kept simple and leaves a lot of room for
 * performance improvement. Things that can be improved:</P>
 * <OL>
 * <LI>Find a better variable ordering.</LI>
 * <LI>It is better to avoid computing monolithic transition relation as
 *     returned by {@link BDDEncoding#getTransitionRelationBDD()}, and use
 *     partitioned transition relations instead.</LI>
 * <LI>Disjunctive partitioning is better than conjunctive partitioning.</LI>
 * <LI>etc.</LI>
 * </OL>
 *
 * @author Robi Malik
 */

public class BDDEncoding
{

  //#########################################################################
  //# Constructor
  /**
   * Creates a new BDD encoding. The constructor assigns codes to all events
   * and automata states and allocates the necessary BDD variables in the
   * given BDD factory.
   * @param  bddFactory   The BDD factory used to construct BDDs.
   * @param  model        The model for which BDDs are to be constructed.
   */
  public BDDEncoding(final BDDFactory bddFactory, final ProductDESProxy model)
  {
    mBDDFactory = bddFactory;
    // Encode events ...
    final Collection<EventProxy> events = model.getEvents();
    int numEvents = events.size();
    mEventList = new ArrayList<EventProxy>(numEvents);
    mEventMap = new HashMap<EventProxy,Integer>(numEvents);
    int index = 0;
    for (final EventProxy event : events) {
      if (event.getKind() != EventKind.PROPOSITION) {
        mEventList.add(event);
        mEventMap.put(event, index++);
      }
    }
    numEvents = index;
    mNumEventBits = AutomatonTools.log2(numEvents);
    // Encode automata ...
    final Collection<AutomatonProxy> automata = model.getAutomata();
    final int numAutomata = automata.size();
    mAutomata = new ArrayList<AutomatonEncoding>(numAutomata);
    int varIndex = mNumEventBits;
    for (final AutomatonProxy aut : automata) {
      if (aut.getKind() != ComponentKind.PROPERTY) {
        final AutomatonEncoding enc = new AutomatonEncoding(aut, varIndex);
        mAutomata.add(enc);
        final int numBits = enc.getNumberOfBits();
        mNumAutomataBits += numBits;
        varIndex += 2 * numBits;
      }
    }
    // Allocate BDD variables ...
    if (mBDDFactory.varNum() < varIndex) {
      mBDDFactory.setVarNum(varIndex);
    }
  }


  //#########################################################################
  //# BDD Construction
  /**
   * Computes a BDD representing the initial state set of this encoding's
   * model.
   * @return A BDD over the current state variables of all automata in the
   *         model, which is true precisely when the model is in an initial
   *         state.
   */
  public BDD getInitialStateBDD()
  {
    BDD init = mBDDFactory.one();
    final int end = mAutomata.size();
    final ListIterator<AutomatonEncoding> iter = mAutomata.listIterator(end);
    while (iter.hasPrevious()) {
      final AutomatonEncoding enc = iter.previous();
      init = enc.addInitialStateBDD(init);
    }
    return init;
  }

  /**
   * Computes a BDD representing a marked state set of this encoding's
   * model.
   * @param  marking      The marking to be considered. This should be an
   *                      event of type {@link EventKind#PROPOSITION
   *                      PROPOSITION}, which is used by the model.
   * @return A BDD over the current state variables of the automata in the
   *         model, which is true precisely when the model is a state marked
   *         by the given proposition,
   */
  public BDD getMarkedStateBDD(final EventProxy marking)
  {
    BDD terminal = mBDDFactory.one();
    final int end = mAutomata.size();
    final ListIterator<AutomatonEncoding> iter = mAutomata.listIterator(end);
    while (iter.hasPrevious()) {
      final AutomatonEncoding enc = iter.previous();
      terminal = enc.addMarkedStateBDD(terminal, marking);
    }
    return terminal;
  }

  /**
   * Computes a BDD representing the monolithic transition relation of this
   * encoding's model without event bits.
   * @return A BDD over the current and next state variables of all automata
   *         in the model, which is true precisely when there is a transition
   *         between the current and next state in the synchronous composition
   *         of all automata in the encoded model.
   */
  public BDD getTransitionRelationBDD()
  {
    final BDD trans = getTransitionRelationBDDWithEvents();
    final BDDVarSet eventVars = getEventVarSet();
    final BDD result = trans.exist(eventVars);
    trans.free();
    eventVars.free();
    return result;
  }

  /**
   * Computes a BDD representing the monolithic transition relation of this
   * encoding's model with event bits.
   * @return A BDD over the event, current state, and next state variables of
   *         all automata in the model, which is true precisely when there is
   *         a transition with the event between the current and next state
   *         in the synchronous composition of all automata in the encoded
   *         model.
   */
  public BDD getTransitionRelationBDDWithEvents()
  {
    // Compose the transition relations of all automata, bottom-up ...
    final BDD trans = mBDDFactory.one();
    final int end = mAutomata.size();
    final ListIterator<AutomatonEncoding> iter = mAutomata.listIterator(end);
    while (iter.hasPrevious()) {
      final AutomatonEncoding enc = iter.previous();
      final BDD autTrans = enc.getTransitionRelationBDD();
      trans.andWith(autTrans);
    }
    return trans;
  }

  /**
   * Returns the number of non-proposition events in this encoding.
   */
  public int getNumberOfProperEvents()
  {
    return mEventList.size();
  }

  /**
   * Retrieves the event with the given code.
   * @param  code         The integer code assigned to the event.
   *                      Non-proposition events are assigned codes starting
   *                      at&nbsp;0 in the order they appear in the input
   *                      model.
   * @return The event with the given code.
   * @throws IndexOutOfBoundsException if the encoding has no event with
   *                      the given code.
   */
  public EventProxy getEvent(final int code)
  {
    return mEventList.get(code);
  }

  /**
   * Computes a BDD encoding an event.
   * @param  event        The event to be encoded.
   * @return A BDD over the event variables, which is true precisely when
   *         the event variables encode the given event.
   */
  public BDD getEventBDD(final EventProxy event)
  {
    final Integer code = mEventMap.get(event);
    if (code == null) {
      // It is more robust to return the 'false' BDD for propositions and
      // other non-events, though the user should have avoided this call.
      return mBDDFactory.zero();
    } else {
      return encodeBDD(code, 0, mNumEventBits, 1);
    }
  }

  /**
   * <P>Finds an event that can satisfy a given BDD. This method traverses
   * the given BDD to find a bit combination of the event variables that can
   * make the conditions of this BDD true. If found, this bit combination
   * is mapped to an event object.</P>
   * <P>This method can be used to find a counterexample. Given a BDD encoding
   * constraints on states and events, an event satisfying the constraints
   * can be found. Note that choosing an event imposes further constraints,
   * so the next step should be intersect the constraints with the BDD
   * for the returned event.</P>
   * @param  bdd          The BDD to be examined, which should use the event
   *                      variables. It may use other variables as well.
   * @return One possible event that may be true under the constraints of
   *         the given BDD, or <CODE>null</CODE>.
   * @see #getEventBDD(EventProxy) getEventBDD()
   */
  public EventProxy findEvent(final BDD bdd)
  {
    // No event if the BDD is constant false.
    if (bdd.isZero()) {
      return null;
    }
    BDD current = bdd;
    int code = 0;
    while (!current.isOne()) {
      final int varindex = current.var();
      final BDD low = current.low();
      final BDD high = current.high();
      // Follow paths, but never touch 0-terminal.
      if (low.isZero()) {
        current = high;
      } else if (high.isZero()) {
        current = low;
      } else if (low.level() < high.level()) {
        current = high;
      } else {
        current = low;
      }
      // If following 1-branch of an event variable,
      // set corresponding bit in event code.
      if (current == high && varindex < mNumEventBits) {
        final int bitindex = mNumEventBits - varindex - 1;
        code |= (1 << bitindex);
      }
    }
    // Look up event with identified code.
    if (code < mEventList.size()) {
      return mEventList.get(code);
    } else {
      return null;
    }
  }

  /**
   * Computes a variable set containing all current state variables of this
   * encoding.
   * Variable sets can be used with the {@link BDD#exist(BDDVarSet) exist()}
   * or {@link BDD#relprod(BDD, BDDVarSet) relprod()} or similar methods to
   * evaluate Boolean quantification of BDDs.
   */
  public BDDVarSet getCurrentStateVarSet()
  {
    // Most BDD packages represent variable sets as BDDs,
    // so we also build them bottom-up.
    final int firstIndex = mNumEventBits;
    final int lastIndex = firstIndex + 2 * (mNumAutomataBits - 1);
    final BDDVarSet varset = mBDDFactory.emptySet();
    for (int varIndex = lastIndex; varIndex >= firstIndex; varIndex -= 2) {
      varset.unionWith(varIndex);
    }
    return varset;
  }

  /**
   * Computes a variable set containing all next state variables of this
   * encoding.
   * Variable sets can be used with the {@link BDD#exist(BDDVarSet) exist()}
   * or {@link BDD#relprod(BDD, BDDVarSet) relprod()} or similar methods to
   * evaluate Boolean quantification of BDDs.
   */
  public BDDVarSet getNextStateVarSet()
  {
    final int firstIndex = mNumEventBits + 1;
    final int lastIndex = firstIndex + 2 * (mNumAutomataBits - 1);
    final BDDVarSet varset = mBDDFactory.emptySet();
    for (int varIndex = lastIndex; varIndex >= firstIndex; varIndex -= 2) {
      varset.unionWith(varIndex);
    }
    return varset;
  }

  /**
   * Computes a variable set containing all event variables of this encoding.
   * Variable sets can be used with the {@link BDD#exist(BDDVarSet) exist()}
   * or {@link BDD#relprod(BDD, BDDVarSet) relprod()} or similar methods to
   * evaluate Boolean quantification of BDDs.
   */
  public BDDVarSet getEventVarSet()
  {
    final BDDVarSet varset = mBDDFactory.emptySet();
    for (int varIndex = mNumEventBits - 1; varIndex >= 0; varIndex--) {
      varset.unionWith(varIndex);
    }
    return varset;
  }

  /**
   * Computes a pairing that maps all current state variables of this
   * encoding to their corresponding next state variables.
   * BDD pairings can be used with the {@link BDD#replace(BDDPairing) replace()}
   * or {@link BDD#replaceWith(BDDPairing) replaceWith()} methods to
   * substitute variables in a BDD.
   */
  public BDDPairing getCurrentStateToNextStatePairing()
  {
    final int firstIndex = mNumEventBits;
    final int lastIndex = firstIndex + 2 * (mNumAutomataBits - 1);
    final BDDPairing pairing = mBDDFactory.makePair();
    for (int varIndex = lastIndex; varIndex >= firstIndex; varIndex -= 2) {
      final int nextVarIndex = varIndex + 1;
      pairing.set(varIndex, nextVarIndex);
    }
    return pairing;
  }

  /**
   * Computes a pairing that maps all next state variables of this
   * encoding to their corresponding current state variables.
   * BDD pairings can be used with the {@link BDD#replace(BDDPairing) replace()}
   * or {@link BDD#replaceWith(BDDPairing) replaceWith()} methods to
   * substitute variables in a BDD.
   */
  public BDDPairing getNextStateToCurrentStatePairing()
  {
    final int firstIndex = mNumEventBits;
    final int lastIndex = firstIndex + 2 * (mNumAutomataBits - 1);
    final BDDPairing pairing = mBDDFactory.makePair();
    for (int varIndex = lastIndex; varIndex >= firstIndex; varIndex -= 2) {
      final int nextVarIndex = varIndex + 1;
      pairing.set(nextVarIndex, varIndex);
    }
    return pairing;
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Computes a BDD encoding a given number.
   * @param  code          The number to be encoded.
   * @param  firstVarIndex The index of the first variable to be used for
   *                       encoding.
   * @param  numBits       The number of bits used to encode the given number.
   * @param  interleave    The distance between two consecutive bits used to
   *                       encode the given number.
   * @return A BDD over the variables firstVarIndex, firstVarIndex+interleave,
   *         ..., firstVarIndex+(numBits-1)*interleave which is true precisely
   *         when the variables represent a binary code of the given number,
   *         with the most significant bit appearing first, i.e., firstVarIndex
   *         encodes the most significant bit.
   */
  private BDD encodeBDD(final int code,
                        final int firstVarIndex,
                        final int numBits,
                        final int interleave)
  {
    return encodeBDD(code, firstVarIndex, numBits, interleave,
                     mBDDFactory.one());
  }

  /**
   * <P>Computes a BDD encoding a given number and adds it to another BDD.</P>
   * <P>This method constructs a BDD over the variables firstVarIndex,
   * firstVarIndex+interleave, ..., firstVarIndex+(numBits-1)*interleave,
   * which is true precisely when they represent a binary code of the given
   * number, with the most significant bit appearing first, i.e., firstVarIndex
   * encodes the most significant bit.</P>
   * <P>The resulting BDD is composed using logical AND with the given BDD.
   * BDD nodes are constructed bottom-up, and are added to the given BDD
   * one-by-one. This method works most efficiently when the given BDD only
   * contains variables that appear later in the variable ordering than the
   * variables added by this method.</P>
   * @param  code          The number to be encoded.
   * @param  firstVarIndex The index of the first variable to be used for
   *                       encoding.
   * @param  numBits       The number of bits used to encode the given number.
   * @param  interleave    The distance between two consecutive bits used to
   *                       encode the given number.
   * @param  bdd           A BDD to which the encoding is added.
   * @return A BDD representing the conjunction of the given BDD and the
   *         encoding of the given number.
   */
  private BDD encodeBDD(final int code,
                        final int firstVarIndex,
                        final int numBits,
                        final int interleave,
                        final BDD bdd)
  {
    // Construct BDD bottom-up, starting with the highest variable index
    // and the least significant bit of the encoding ...
    int varIndex = firstVarIndex + numBits * interleave;
    int mask = 1;
    while (varIndex > firstVarIndex) {
      varIndex -= interleave;
      if ((code & mask) != 0) {
        final BDD var = mBDDFactory.ithVar(varIndex);
        bdd.andWith(var);
      } else {
        final BDD var = mBDDFactory.nithVar(varIndex);
        bdd.andWith(var);
      }
      mask <<= 1;
    }
    return bdd;
  }


  //#########################################################################
  //# Inner Class AutomatonEncoding
  /**
   * An auxiliary class to represent the encoding of the states of a single
   * automaton as BDDs.
   */
  private class AutomatonEncoding {

    //#######################################################################
    //# Constructor
    /**
     * Creates a new automaton encoding. This constructor assigns state codes
     * to all states of the given automaton and allocates BDD variables for
     * the current and next state variables needed for this automaton.
     * @param  aut       The automaton to be encoded.
     * @param  varIndex  The index of the first variable allocated for
     *                   encoding the states of this automaton.
     */
    private AutomatonEncoding(final AutomatonProxy aut, final int varIndex)
    {
      mAutomaton = aut;
      final Collection<StateProxy> states = aut.getStates();
      final int numStates = states.size();
      mStateMap = new HashMap<StateProxy,Integer>(numStates);
      int index = 0;
      for (final StateProxy state : states) {
        mStateMap.put(state, index++);
      }
      mFirstVariableIndex = varIndex;
      mNumBits = AutomatonTools.log2(numStates);
    }

    //#######################################################################
    //# Simple Access
    /**
     * Gets the index of the first variable allocated for encoding the
     * states of this automaton.
     */
    @SuppressWarnings("unused")
    private int getFirstVariableIndex()
    {
      return mFirstVariableIndex;
    }

    /**
     * Gets the number of bits needed to encode the states of this automaton.
     * Since the automaton uses two sets of variables for the current and next
     * state, the actual number of bits allocated is double this number. The
     * range of allocated bits is from {@link #getFirstVariableIndex()} to
     * {@link #getFirstVariableIndex()}&nbsp;+&nbsp;2*({@link
     * #getNumberOfBits()}-1). Variables are allocated in an interleaving
     * fashion, with each current state followed by a next state bit. The
     * most significant bit appears first in the ordering.
     */
    private int getNumberOfBits()
    {
      return mNumBits;
    }

    //#########################################################################
    //# BDD Construction
    /**
     * <P>Computes a BDD encoding the initial states of this automaton
     * and adds it to another BDD.</P>
     * <P>This method constructs a BDD over the current state variables of
     * this automaton, which is true precisely when the current state is an
     * initial state of this automaton. The resulting BDD is composed using
     * logical AND with the given BDD.</P>
     * <P>Since BDDs are best constructed bottom-up, this method works most
     * efficiently when the given BDD only contains variables that appear later
     * in the variable ordering than the current state variables of this
     * automaton.</P>
     * @param  otherBDD      A BDD to which the initial states of this automaton
     *                       are added.
     * @return A BDD representing the conjunction of the given BDD and the
     *         initial state BDD of this automaton.
     */
    private BDD addInitialStateBDD(final BDD otherBDD)
    {
      final BDD autBDD = mBDDFactory.zero();
      for (final Map.Entry<StateProxy,Integer> entry : mStateMap.entrySet()) {
        final StateProxy state = entry.getKey();
        if (state.isInitial()) {
          final int code = entry.getValue();
          final BDD stateBDD =
            encodeBDD(code, mFirstVariableIndex, mNumBits, 2);
          autBDD.orWith(stateBDD);
        }
      }
      return otherBDD.andWith(autBDD);
    }

    /**
     * <P>Computes a BDD encoding the marked states of this automaton
     * and adds it to another BDD.</P>
     * <P>This method constructs a BDD over the current state variables of
     * this automaton, which is true precisely when the current state is a
     * marked in this automaton. The resulting BDD is composed using logical
     * AND with the given BDD.</P>
     * <P>Since BDDs are best constructed bottom-up, this method works most
     * efficiently when the given BDD only contains variables that appear
     * later in the variable ordering than the current state variables of this
     * automaton.</P>
     * @param  otherBDD      A BDD to which the marked states of this
     *                       automaton are added.
     * @param  marking       The marking to be considered. This should be an
     *                       event of type {@link EventKind#PROPOSITION
     *                       PROPOSITION}, which is used by the model.
     * @return A BDD representing the conjunction of the given BDD and the
     *         marked state BDD of this automaton.
     */
    private BDD addMarkedStateBDD(final BDD otherBDD, final EventProxy marking)
    {
      // If the marking is not in the automaton alphabet, assume all states
      // are marked, and so do not change the BDD.
      if (mAutomaton.getEvents().contains(marking)) {
        final BDD autBDD = mBDDFactory.zero();
        for (final Map.Entry<StateProxy,Integer> entry : mStateMap.entrySet()) {
          final StateProxy state = entry.getKey();
          if (state.getPropositions().contains(marking)) {
            final int code = entry.getValue();
            final BDD stateBDD =
              encodeBDD(code, mFirstVariableIndex, mNumBits, 2);
            autBDD.orWith(stateBDD);
          }
        }
        otherBDD.andWith(autBDD);
      }
      return otherBDD;
    }

    /**
     * Computes a BDD representing the transition relation of this automaton.
     * @return A BDD over the event variables of the model and the current and
     *         next state variables of this automaton, which is true precisely
     *         when there is a transition between the current and next state
     *         in this automaton using the encoded event.
     */
    private BDD getTransitionRelationBDD()
    {
      // Events not in the alphabet give rise to implicit selfloops ...
      final BDD eventsBDD = getEventAlphabetBDD();
      final BDD notEventsBDD = eventsBDD.not();
      eventsBDD.free();
      final BDD unchangedBDD = getUnchangedBDD();
      final BDD transRelBDD = notEventsBDD.andWith(unchangedBDD);
      // Now add to this the explicit transitions ...
      for (final TransitionProxy trans : mAutomaton.getTransitions()) {
        final BDD transBDD = getTransitionBDD(trans);
        transRelBDD.orWith(transBDD);
      }
      return transRelBDD;
    }

    //#######################################################################
    //# Auxiliary Methods
    /**
     * Computes a BDD representing the event alphabet of this automaton.
     * @return A BDD over the event variables of the encoding that is true
     *         precisely when the event is contained in the event alphabet
     *         of this automaton.
     */
    private BDD getEventAlphabetBDD()
    {
      final BDD bdd = mBDDFactory.zero();
      for (final EventProxy event : mAutomaton.getEvents()) {
        if (event.getKind() != EventKind.PROPOSITION) {
          final BDD eventBDD = getEventBDD(event);
          bdd.orWith(eventBDD);
        }
      }
      return bdd;
    }

    /**
     * Computes a BDD representing that the states of this automaton are
     * unchanged.
     * @return A BDD over the current and next state variables of this
     *         automaton that is true precisely when the current state is
     *         equal to the next state.
     */
    private BDD getUnchangedBDD()
    {
      // Construct BDD bottom-up, starting with the highest variable index ...
      final BDD bdd = mBDDFactory.one();
      for (int bitno = mNumBits - 1; bitno >= 0; bitno--) {
        final int varIndex = mFirstVariableIndex + 2 * bitno;
        final BDD var = mBDDFactory.ithVar(varIndex);
        final BDD nextVar = mBDDFactory.ithVar(varIndex + 1);
        final BDD biimp = var.biimpWith(nextVar);
        bdd.andWith(biimp);
      }
      return bdd;
    }

    /**
     * Computes a BDD representing a given transition in this automaton.
     * @param  trans     The transition to be encoded.
     * @return A BDD over the event variables of the encoding and the current
     *         and next state variables of this automaton, which evaluates
     *         to true precisely when the current state is the source state
     *         of the transition, the next state is the target state of the
     *         transition, and the event variables encode the event of the
     *         transition.
     */
    private BDD getTransitionBDD(final TransitionProxy trans)
    {
      final StateProxy source = trans.getSource();
      final int sourceCode = mStateMap.get(source);
      final StateProxy target = trans.getTarget();
      final int targetCode = mStateMap.get(target);
      final BDD bdd = mBDDFactory.one();
      // First encode source and target state.
      // Construct BDD bottom-up, starting with the highest variable index
      // and the least significant bit of the encoding ...
      int varIndex = mFirstVariableIndex + 2 * mNumBits - 1;
      int mask = 1;
      while (varIndex >= mFirstVariableIndex) {
        if ((targetCode & mask) != 0) {
          final BDD var = mBDDFactory.ithVar(varIndex);
          bdd.andWith(var);
        } else {
          final BDD var = mBDDFactory.nithVar(varIndex);
          bdd.andWith(var);
        }
        varIndex--;
        if ((sourceCode & mask) != 0) {
          final BDD var = mBDDFactory.ithVar(varIndex);
          bdd.andWith(var);
        } else {
          final BDD var = mBDDFactory.nithVar(varIndex);
          bdd.andWith(var);
        }
        varIndex--;
        mask <<= 1;
      }
      // Next add the event code ...
      final EventProxy event = trans.getEvent();
      final int eventCode = mEventMap.get(event);
      return encodeBDD(eventCode, 0, mNumEventBits, 1, bdd);
    }

    //#######################################################################
    //# Data Members
    /**
     * The automaton encoded by this encoding.
     */
    private final AutomatonProxy mAutomaton;
    /**
     * A map that assigns each state of the automaton to an integer code.
     */
    private final Map<StateProxy,Integer> mStateMap;
    /**
     * The index of the first variable allocated for encoding the
     * states of this automaton.
     */
    private final int mFirstVariableIndex;
    /**
     * The number of bits needed to encode the states of this automaton.
     * @see #getNumberOfBits()
     */
    private final int mNumBits;

  }


  //#########################################################################
  //# Data Members
  /**
   * The BDD factory used to construct all BDDs.
   */
  private final BDDFactory mBDDFactory;
  /**
   * A list containing all non-proposition events of the model in the
   * indexed by their event codes.
   */
  private final List<EventProxy> mEventList;
  /**
   * A map that assigns each non-proposition event of the model to an
   * integer code. This map is the reverse of the {@link #mEventList}.
   */
  private final Map<EventProxy,Integer> mEventMap;
  /**
   * The number of bits used to encode the events. Since events appear
   * first in the variable ordering, this number also is the index of the
   * first automaton variable. Events are encoded using the variable indexes
   * 0..{@link #mNumEventBits}-1.
   */
  private final int mNumEventBits;
  /**
   * The list of encoded automata, in the order in which they appear in the
   * variable ordering. The automaton listed first starts with the variable
   * index given by {@link #mNumEventBits}.
   */
  private final List<AutomatonEncoding> mAutomata;
  /**
   * The number of bits used to encode all automata states. Since each
   * automaton uses two sets of variables, the actual number of bits allocated
   * is double this number. The total number of bits allocated in the BDD
   * factory is {@link #mNumEventBits}+2*mNumAutomataBits.
   */
  private int mNumAutomataBits;

}
