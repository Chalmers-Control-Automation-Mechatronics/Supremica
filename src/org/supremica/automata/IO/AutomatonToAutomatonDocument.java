
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.IO;

public class AutomatonToAutomatonDocument
{

	/*
	 *  private Automaton aut;
	 *  public AutomatonToAutomatonDocument(Automaton aut)
	 *  {
	 *  this.aut = aut;
	 *  }
	 *  public AutomatonDocument getAutomatonDocument()
	 *  throws Exception
	 *  {
	 *  Vector initialStates = new Vector();
	 *  final String initPrefix = "__init_";
	 *  pw.println("digraph state_automaton {");
	 *  pw.println("\tcenter = true;");
	 *  if (leftToRight)
	 *  {
	 *  pw.println("\trankdir = LR;");
	 *  }
	 *  if (!aut.hasInitialState())
	 *  {
	 *  pw.println("\t noState [shape = plaintext, label = \"No initial state\" ]");
	 *  pw.println("}");
	 *  pw.flush();
	 *  pw.close();
	 *  return;
	 *  }
	 *  Iterator states = aut.stateIterator();
	 *  while (states.hasNext())
	 *  {
	 *  State state = (State)states.next();
	 *  if (state.isInitial())
	 *  {
	 *  initialStates.addElement(state);
	 *  pw.println("\tnode [shape = plaintext] " + initPrefix + state.getId() + ";");
	 *  }
	 *  if (state.isAccepting() && !state.isForbidden())
	 *  {
	 *  if (withCircles)
	 *  {
	 *  pw.println("\tnode [shape = doublecircle] " + state.getId() + ";");
	 *  }
	 *  else
	 *  {
	 *  pw.println("\tnode [shape = ellipse] " + state.getId() + ";");
	 *  }
	 *  }
	 *  if (state.isForbidden())
	 *  {
	 *  pw.println("\tnode [shape = box] " + state.getId() + ";");
	 *  }
	 *  }
	 *  if (withCircles)
	 *  {
	 *  pw.println("\tnode [shape = circle];");
	 *  }
	 *  else
	 *  {
	 *  pw.println("\tnode [shape = plaintext];");
	 *  }
	 *  for (int i = 0; i < initialStates.size(); i++)
	 *  {
	 *  String stateId = ((State)initialStates.elementAt(i)).getId();
	 *  pw.println("\t" + initPrefix + stateId + " [label = \"\"]; ");
	 *  pw.println("\t" + initPrefix + stateId + " [height = \"0\"]; ");
	 *  pw.println("\t" + initPrefix + stateId + " [width = \"0\"]; ");
	 *  pw.println("\t" + initPrefix + stateId + " -> " + stateId + ";");
	 *  }
	 *  DestStateMap destStateMap = null;
	 *  if (useMultiLabels)
	 *  {
	 *  destStateMap = new DestStateMap(aut.nbrOfStates());
	 *  }
	 *  Alphabet theAlphabet = aut.getAlphabet();
	 *  states = aut.stateIterator();
	 *  while (states.hasNext())
	 *  {
	 *  State sourceState = (State)states.next();
	 *  pw.print("\t" + sourceState.getId() + " [label = \"");
	 *  if (withLabel)
	 *  {
	 *  pw.print(sourceState.getName());
	 *  }
	 *  pw.println("\"" + getColor(sourceState) + "]; ");
	 *  if (useMultiLabels)
	 *  {
	 *  destStateMap.clear();
	 *  Iterator outgoingArcs = sourceState.outgoingArcsIterator();
	 *  while (outgoingArcs.hasNext())
	 *  {
	 *  Arc arc = (Arc)outgoingArcs.next();
	 *  State destState = (State)arc.getToState();
	 *  destStateMap.addState(destState, arc);
	 *  }
	 *  Iterator destStateIt = destStateMap.getDestStateIterator();
	 *  while (destStateIt.hasNext())
	 *  {
	 *  State destState = (State)destStateIt.next();
	 *  pw.print("\t" + sourceState.getId() + " -> " + destState.getId());
	 *  pw.print(" [ label = \"");
	 *  Iterator arcIt = destStateMap.getArcIterator(destState);
	 *  while (arcIt.hasNext())
	 *  {
	 *  Arc currArc = (Arc)arcIt.next();
	 *  Event thisEvent = theAlphabet.getEventWithId(currArc.getEventId());
	 *  if (!thisEvent.isControllable())
	 *  pw.print("!");
	 *  if (!thisEvent.isPrioritized())
	 *  pw.print("?");
	 *  pw.print(thisEvent.getLabel());
	 *  if (arcIt.hasNext())
	 *  {
	 *  pw.print("\\n");
	 *  }
	 *  }
	 *  pw.println("\" ];");
	 *  }
	 *  }
	 *  else
	 *  {
	 *  Iterator outgoingArcs = sourceState.outgoingArcsIterator();
	 *  while (outgoingArcs.hasNext())
	 *  {
	 *  Arc arc = (Arc)outgoingArcs.next();
	 *  State destState = (State)arc.getToState();
	 *  pw.print("\t" + sourceState.getId() + " -> " + destState.getId());
	 *  Event thisEvent = theAlphabet.getEventWithId(arc.getEventId());
	 *  pw.print(" [ label = \"");
	 *  if (!thisEvent.isControllable())
	 *  pw.print("!");
	 *  if (!thisEvent.isPrioritized())
	 *  pw.print("?");
	 *  pw.println(thisEvent.getLabel() + "\" ];");
	 *  }
	 *  }
	 *  }
	 *  / An attemp to always start at the initial state.
	 *  / The problem is that a rectangle is drawn around the initial state.
	 *  Iterator stateIt = initialStates.iterator();
	 *  while(stateIt.hasNext())
	 *  {
	 *  State currState = (State)stateIt.next();
	 *  pw.println("\t{ rank = min ;");
	 *  pw.println("\t\t" + initPrefix + currState.getId() + ";");
	 *  pw.println("\t\t" + currState.getId() + ";");
	 *  pw.println("\t}");
	 *  }
	 *  pw.println("}");
	 *  pw.flush();
	 *  pw.close();
	 *  }
	 */
}
