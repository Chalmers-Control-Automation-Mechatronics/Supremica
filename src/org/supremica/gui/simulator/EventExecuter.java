package org.supremica.gui.simulator;

import javax.swing.event.ListDataListener;
import javax.swing.event.ListDataEvent;
import org.supremica.automata.LabeledEvent;
import org.supremica.log.*;

/**
 * @author ka
 *
 * Automatically execute controllable and uncontrollable events in the simultation.
 */

class EventExecuter
	extends Thread
//	implements ListDataListener
{
	private static Logger logger = LoggerFactory.createLogger(EventExecuter.class);

	private long sleepTime = 100;
	private boolean doRun = true;
	private boolean executeControllableEvents = false;
	private boolean executeUncontrollableEvents = false;
	private SimulatorEventListModel eventModel;
	private SimulatorExecuter theExecuter;

	public EventExecuter(SimulatorExecuter theExecuter, SimulatorEventListModel eventModel)
	{
//		this.sleepTime = sleepTime;
		this.eventModel = eventModel;
		this.theExecuter = theExecuter;
//		eventModel.addListDataListener(this);
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
//
	public void executeControllableEvents(boolean executeControllableEvents)
	{
		this.executeControllableEvents = executeControllableEvents;
		//tryExecuteEvent();
	}

	public void executeUncontrollableEvents(boolean executeUncontrollableEvents)
	{
		this.executeUncontrollableEvents = executeUncontrollableEvents;
		//tryExecuteEvent();
	}



	public void requestStop()
	{
		doRun = false;
	}

	private synchronized void tryExecuteEvent()
	{
		updateSignals();

		if (executeUncontrollableEvents)
		{
			eventModel.enterLock();

			int nbrOfEvents = eventModel.getSize();
			for (int i = 0; i < nbrOfEvents; i++)
			{
				LabeledEvent currEvent = eventModel.getEventAt(i);
				if (!currEvent.isControllable())
				{
					logger.info("Automatically executed event: " + currEvent.getLabel());
					if (!theExecuter.executeEvent(currEvent))
					{
						logger.warn("Failed to execute event: " + currEvent.getLabel());
					}
					eventModel.exitLock();
					return;
				}
			}

			eventModel.exitLock();

		}

		updateSignals();

		if (executeControllableEvents)
		{
			eventModel.enterLock();

			int nbrOfEvents = eventModel.getSize();
			for (int i = 0; i < nbrOfEvents; i++)
			{
				LabeledEvent currEvent = eventModel.getEventAt(i);
				if (currEvent.isControllable())
				{
					logger.info("Automatically executed event: " + currEvent.getLabel());
					if (!theExecuter.executeEvent(currEvent))
					{
						logger.warn("Failed to execute event: " + currEvent.getLabel());
					}
					eventModel.exitLock();
					return;
				}
			}

			eventModel.exitLock();
		}
	}



	public void contentsChanged(ListDataEvent e)
	{
		//tryExecuteEvent();
	}

	public void intervalAdded(ListDataEvent e)
	{
		contentsChanged(e);
	}

	public void intervalRemoved(ListDataEvent e)
	{
		contentsChanged(e);
	}

	protected void updateSignals()
	{
		eventModel.update();
		//theExecuter.updateSignals();
	}
}


