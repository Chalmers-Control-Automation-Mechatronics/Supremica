package org.supremica.gui.simulator;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import org.supremica.automata.LabeledEvent;

/**
 * @author ka
 *
 * Automatically execute controllable and uncontrollable events in the simultation.
 */

class EventExecuter
	extends Thread
	implements ListDataListener
{
	private long sleepTime = 1000;
	private boolean doRun = true;	
	private boolean executeControllableEvents = false;
	private boolean executeUncontrollableEvents = false;
	private SimulatorExecuter theExecuter;
	private SimulatorEventListModel eventModel;
	
	public EventExecuter(long sleepTime, SimulatorExecuter executer, SimulatorEventListModel eventModel)
	{
		this.sleepTime = sleepTime;	
		this.theExecuter = executer;
		this.eventModel = eventModel;
	}	
	
	public void run()
	{
		while (doRun)
		{
			try 
			{
				Thread.sleep(sleepTime);
			} 
			catch (InterruptedException e) 
			{
			
			}		
			tryExecuteEvent();
		}	
	}
	
	public void executeControllableEvents(boolean executeControllableEvents)
	{
		this.executeControllableEvents = executeControllableEvents;
		tryExecuteEvent();	
	}
	
	public void executeUncontrollableEvents(boolean executeUncontrollableEvents)
	{
		this.executeUncontrollableEvents = executeUncontrollableEvents;	
		tryExecuteEvent();
	}
	
	
	
	public void requestStop()
	{
		doRun = false;		
	}
	
	public synchronized void tryExecuteEvent()
	{
		if (executeUncontrollableEvents)
		{
			int nbrOfEvents = eventModel.getSize();
			for (int i = 0; i < nbrOfEvents; i++)
			{
				LabeledEvent currEvent = eventModel.getEventAt(i);
				if (!currEvent.isControllable())
				{
					theExecuter.executeEvent(currEvent);					
					return;
				}	
			}
			
		}	
		if (executeControllableEvents)
		{
			int nbrOfEvents = eventModel.getSize();
			for (int i = 0; i < nbrOfEvents; i++)
			{
				LabeledEvent currEvent = eventModel.getEventAt(i);
				if (currEvent.isControllable())
				{
					theExecuter.executeEvent(currEvent);					
					return;
				}	
			}			
		}	
	}

	public void contentsChanged(ListDataEvent e)
	{
		tryExecuteEvent();		
	}
	
	public void intervalAdded(ListDataEvent e)
	{
		contentsChanged(e);	
	}

	public void intervalRemoved(ListDataEvent e)
	{
		contentsChanged(e);		
	}  	
}


