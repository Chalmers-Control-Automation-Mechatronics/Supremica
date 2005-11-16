
/************************ Monitorable.java *******************/

// Interface for being monitored by the Monitor class that
// manages the progress monitoring
package org.supremica.gui;

public interface Monitorable
{
	int getProgress(); // return progress in precentage

	String getActivity(); // return activity string

	void stopTask(); // user stopped the task

	boolean wasStopped(); // return whether the task was stopped or completed
}
