
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
package org.supremica.recipe;

// Remove this class
class PlantConnections
{
}

/*
 *  public class RecipeResourceAutomataBuilder
 *  {
 *  private PlantConnections plantConnections;
 *  private Automata theRecipeAutomata;
 *  private Automata theResourceAutomata;
 *  private InternalOperationRecipes theRecipes;
 *  private HashMap resourceIdleStateMap;
 *
 *  public RecipeResourceAutomataBuilder(PlantConnections plantConnections)
 *  {
 *  this.plantConnections = plantConnections;
 *  }
 *
 *  public void buildStateAutomata(InternalOperationRecipes theRecipes)
 *  {
 *  theRecipeAutomata = new Automata();
 *  theResourceAutomata = new Automata();
 *
 *  resourceIdleStateMap = new HashMap();
 *
 *  this.theRecipes = theRecipes;
 *
 *  Iterator recipeIt = theRecipes.iterator();
 *  while (recipeIt.hasNext())
 *  {
 *  InternalOperationRecipe currRecipe = (InternalOperationRecipe)recipeIt.next();
 *  doInternalOperationRecipe(currRecipe);
 *  }
 *  }
 *
 *  public Automata getRecipeAutomata()
 *  {
 *  return theRecipeAutomata;
 *  }
 *
 *  public Automata getResourceAutomata()
 *  {
 *  return theResourceAutomata;
 *  }
 *
 *  public Automata getRecipeResourceAutomata()
 *  {
 *  Automata recipeResourceAutomata = new Automata();
 *
 *  Iterator automataIt = theRecipeAutomata.iterator();
 *  while (automataIt.hasNext())
 *  {
 *  Automaton currAutomaton = (Automaton)automataIt.next();
 *  recipeResourceAutomata.addAutomaton(currAutomaton);
 *  }
 *
 *  automataIt = theResourceAutomata.iterator();
 *  while (automataIt.hasNext())
 *  {
 *  Automaton currAutomaton = (Automaton)automataIt.next();
 *  recipeResourceAutomata.addAutomaton(currAutomaton);
 *  }
 *
 *  return recipeResourceAutomata;
 *  }
 *
 *  private Automaton getResourceAutomaton(String id)
 *  {
 *  if (theResourceAutomata.containsAutomaton(id))
 *  {
 *  return theResourceAutomata.getAutomaton(id);
 *  }
 *  else
 *  {
 *  Automaton newResourceAutomaton = new Automaton(id);
 *  theResourceAutomata.addAutomaton(newResourceAutomaton);
 *
 *  State idleState = new State();
 *  idleState.setAccepting(true);
 *  newResourceAutomaton.addState(idleState);
 *  resourceIdleStateMap.put(id, idleState);
 *
 *  return newResourceAutomaton;
 *  }
 *  }
 *
 *  private State getResourceIdleState(String id)
 *  {
 *  if (resourceIdleStateMap.containsKey(id))
 *  {
 *  return (State)resourceIdleStateMap.get(id);
 *  }
 *  else
 *  {
 *  getResourceAutomaton(id);
 *  return getResourceIdleState(id);
 *  }
 *  }
 *
 *
 *  private void doInternalOperationRecipe(InternalOperationRecipe theRecipe)
 *  throws Exception
 *  {
 *  if (!theRecipe.hasValidStructure())
 *  {
 *  throw new Exception("the InternalOperationRecipe does not have a valid structure");
 *  }
 *
 *  InternalOperationRecipeState currState;
 *
 *  if (theRecipe.getStatus() == InternalOperationRecipeStatus.NotStarted)
 *  { // Create an initial state
 *  State initialState = new State();
 *  initialState.setInitial(true);
 *  }
 *
 *  }
 *  }
 *
 */
