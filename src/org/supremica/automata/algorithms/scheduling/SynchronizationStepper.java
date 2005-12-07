package org.supremica.automata.algorithms.scheduling;

import org.supremica.automata.*;
import org.supremica.automata.algorithms.AutomataSynchronizerExecuter;
import org.supremica.automata.algorithms.AutomataSynchronizerHelper;
import org.supremica.automata.algorithms.SynchronizationOptions;

public class SynchronizationStepper
{
	private AutomataSynchronizerHelper synchHelper = null;

	private AutomataSynchronizerExecuter synchronizer = null;

	public SynchronizationStepper(Automata theAutomata)
		throws Exception
	{
		this(theAutomata, new SynchronizationOptions());
	}

	public SynchronizationStepper(Automata theAutomata, SynchronizationOptions synchOptions)
		throws Exception
	{
		synchHelper = new AutomataSynchronizerHelper(theAutomata, synchOptions);
		synchHelper.initialize();

		synchronizer = new AutomataSynchronizerExecuter(synchHelper);
		synchronizer.initialize();

		// Must be remapped for some reason. If not, the indices will come out all wrong. 
		for (int i=0; i<theAutomata.size(); i++)
		{
			theAutomata.getAutomatonAt(i).remapStateIndices();
		}
	}

	public int[] getInitialStateIndices()
	{
		int[] initialStateIndices = synchHelper.getStateToProcess();

		// Is needed to avoid NullPointerException when calling synchronizer.isEnabled()-method
		synchronizer.getOutgoingEvents(initialStateIndices);

		return initialStateIndices;
	}

	public boolean isEnabled(LabeledEvent event)
	{
		return synchronizer.isEnabled(event);
	}

	public int[] step(int[] fromIndices, LabeledEvent event)
	{
		int[] toIndices = synchronizer.doTransition(fromIndices, event);

		// Is needed to avoid NullPointerException when calling synchronizer.isEnabled()-method
		synchronizer.getOutgoingEvents(toIndices);

		return toIndices;
	}
}