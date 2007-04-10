package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;

public class SynchronizationStepper
{
	private AutomataSynchronizerHelper synchHelper = null;
	private AutomataSynchronizerExecuter synchronizer = null;
	private AutomataIndexMap indexMap = null;
	private Automata theAutomata;

	public SynchronizationStepper(Automata theAutomata)
		throws Exception
	{
		this(theAutomata, new SynchronizationOptions());
	}

	public SynchronizationStepper(Automata theAutomata, SynchronizationOptions synchOptions)
		throws Exception
	{
		this.theAutomata = theAutomata;

		synchHelper = new AutomataSynchronizerHelper(theAutomata, synchOptions);
		synchHelper.initialize();

		synchronizer = new AutomataSynchronizerExecuter(synchHelper);
		synchronizer.initialize();

		indexMap = synchHelper.getIndexMap();

		// Must be remapped for some reason. If not, the indices will come out all wrong. 
// 		for (int i=0; i<theAutomata.size(); i++)
// 		{
// 			theAutomata.getAutomatonAt(i).remapStateIndices();
// 		}
	}

	public int[] getInitialStateIndices()
	{
// 		int[] initialStateIndices = synchHelper.getStateToProcess();
		int[] initialStateIndices = AutomataIndexFormHelper.createState(theAutomata.size());
        for (int i=0; i<theAutomata.size(); i++)
        {
			Automaton currAuto = indexMap.getAutomatonAt(i);
            initialStateIndices[indexMap.getAutomatonIndex(currAuto)] = indexMap.getStateIndex(currAuto, currAuto.getInitialState());
        }

		// Is needed to avoid NullPointerException when calling synchronizer.isEnabled()-method
		synchronizer.getOutgoingEvents(initialStateIndices);

		return initialStateIndices;
	}

	public boolean isEnabled(LabeledEvent event)
	{
		return synchronizer.isEnabled(indexMap.getEventIndex(event));
	}

	public int[] step(int[] fromIndices, LabeledEvent event)
	{
		int[] toIndices = synchronizer.doTransition(fromIndices, indexMap.getEventIndex(event));

		// Is needed to avoid NullPointerException when calling synchronizer.isEnabled()-method
		synchronizer.getOutgoingEvents(toIndices);

		return toIndices;
	}
}