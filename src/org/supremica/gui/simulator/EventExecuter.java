package org.supremica.gui.simulator;

import javax.swing.event.ListDataEvent;
import org.supremica.automata.LabeledEvent;
import org.supremica.properties.Config;
import org.supremica.log.*;

/**
 * @author ka
 *
 * Automatically execute controllable and uncontrollable events in the simultation.
 */
class EventExecuter
	extends Thread

//      implements ListDataListener
{
	protected static Logger logger = LoggerFactory.createLogger(EventExecuter.class);
	protected long sleepTime = 100;
	protected boolean doRun = true;
	protected boolean executeControllableEvents = false;
	protected boolean executeUncontrollableEvents = false;
	protected SimulatorEventListModel eventModel;
	protected SimulatorExecuter theExecuter;

	public EventExecuter(SimulatorExecuter theExecuter, SimulatorEventListModel eventModel)
	{

//              this.sleepTime = sleepTime;
		this.eventModel = eventModel;
		this.theExecuter = theExecuter;

//              eventModel.addListDataListener(this);
		sleepTime = Config.SIMULATION_CYCLE_TIME.get();

		System.out.println("sleepTime = " + sleepTime);    // DEBUG
	}

	public void run()
	{
		while (doRun)
		{
			try
			{
				Thread.sleep(sleepTime);
			}
			catch (InterruptedException e) {}

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

	protected void tryExecuteUncontrollableEvents()
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

	protected void tryExecuteControllableEvents()
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

	protected void tryExecuteAnyEvents()
	{
		eventModel.enterLock();

		int nbrOfEvents = eventModel.getSize();

		for (int i = 0; i < nbrOfEvents; i++)
		{
			LabeledEvent currEvent = eventModel.getEventAt(i);

			logger.info("Automatically executed event: " + currEvent.getLabel());

			if (!theExecuter.executeEvent(currEvent))
			{
				logger.warn("Failed to execute event: " + currEvent.getLabel());
			}

			eventModel.exitLock();

			return;
		}

		eventModel.exitLock();
	}

	protected synchronized void tryExecuteEvent()
	{
		updateSignals();

/*
				int nbrOfEvents = eventModel.getSize();
				logger.debug("2.a ----------------------------");
				eventModel.enterLock();
				StringBuffer dum = new StringBuffer();
				dum.append("Enabled events are: " );
				for (int i = 0; i < nbrOfEvents; i++) dum.append(" " + eventModel.getEventAt(i).getLabel());
				logger.debug(dum.toString() );
				eventModel.exitLock();
*/
		if (executeUncontrollableEvents)
		{
			tryExecuteUncontrollableEvents();
		}

		updateSignals();

		if (executeControllableEvents)
		{
			tryExecuteControllableEvents();
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
