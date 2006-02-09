package org.supremica.automata.algorithms.scheduling;

import java.util.*;
import javax.swing.*;
import java.awt.*;

import org.supremica.log.*;
import org.supremica.automata.*;

public class VisGraphScheduler 
	implements Scheduler
{
	/** The logger */
	private static Logger logger = LoggerFactory.createLogger(VisGraphScheduler.class);

	/** The involved automata, plants, zone specifications */
	private Automata robots, zones;

	/** The times corresponding to booking and unbooking events for every robot and zone */
	private Hashtable<Integer, double[]>[] zoneBoundaryTimes = null;

	/** The finishing times of each robot (when run independently of the others) */
	private double[] goalTimes = null;

	public VisGraphScheduler(Automata theAutomata)
	{
		robots = theAutomata.getPlantAutomata();
		zones = theAutomata.getSpecificationAutomata();
	}

	public void schedule()
		throws Exception
	{
		extractGraphTimes();
		drawVisibilityGraph(500, 500);
	}

	public Automaton buildScheduleAutomaton()
		throws Exception
	{
		logger.info("Buildar Schedule");
		return null;
	}

	public void requestStop()
	{
		
	}

	/**
	 * Iterates through the states of each robot, recording the booking and 
	 * the corresponding unbooking times. Also the total (independent) running
	 * time of each robot is stored.
	 */
	private void extractGraphTimes()
		throws Exception 
	{
		zoneBoundaryTimes = new Hashtable[robots.size()]; 
		goalTimes = new double[robots.size()];

		for (int i=0; i<robots.size(); i++)
		{
			double currTime = 0;

			zoneBoundaryTimes[i] = new Hashtable<Integer, double[]>(2 * zones.size());

			State currState = robots.getAutomatonAt(i).getInitialState();

			while (! currState.isAccepting())
			{
				if (currState.nbrOfOutgoingArcs() > 1)
					throw new Exception("Visibility Graph cannot handle alternative routing. State " + currState.getName() + " has several outgoing arcs.");
				currTime += currState.getCost();

				Arc currArc = (Arc) currState.outgoingArcsIterator().next();
				LabeledEvent currEvent = currArc.getEvent();

				for (int j=0; j<zones.size(); j++)
				{
					Automaton currZone = zones.getAutomatonAt(j);

					if (currZone.getAlphabet().contains(currEvent))
					{
						if (currZone.getInitialState().activeEvents(false).contains(currEvent))
						{
							zoneBoundaryTimes[i].put(new Integer(j), new double[]{currTime, -1});
						}
						else
						{
							double[] currTimePair = zoneBoundaryTimes[i].get(new Integer(j));
							currTimePair[1] = currTime;
							zoneBoundaryTimes[i].put(new Integer(j), currTimePair);
						}
					}
				}

				currState = currArc.getToState();
			}

			goalTimes[i] = currTime;

			Enumeration<Integer> keys = zoneBoundaryTimes[i].keys();
			while (keys.hasMoreElements())
			{
				String str = "";
				Integer key = keys.nextElement();
				double[] times = zoneBoundaryTimes[i].get(key);
				for (int k=0; k<times.length; k++)
					str += times[k] + " ";

				logger.info("hash[" + robots.getAutomatonAt(i).getName() + "], zone " + zones.getAutomatonAt(key.intValue()).getName() + " = " + str);
			}
			logger.info("goal time for  " + robots.getAutomatonAt(i).getName() + " = " + goalTimes[i]);
		    
		}
	}

	private void drawVisibilityGraph(int height, int width)
	{
		new Drawer("New visibility graph");

// 		JFrame graph = new JFrame("Visibility Graph");

// 		graph.setSize(height, width);
// 		graph.setBackground(Color.GREEN);

// 		graph.setVisible(true);

// 		Graphics g = graph.getGraphics();
// 		g.drawLine(50,50,250,250);
	}
}

class Drawer extends JFrame
{
	Drawer(String label)
	{
		super(label);

		setSize(500,500);
		setVisible(true);
	}

	public void paint(Graphics g)
	{
		setBackground(Color.WHITE);
		g.drawLine(50,50,250,250);
		g.drawLine(0,0,20,100);
	}
}