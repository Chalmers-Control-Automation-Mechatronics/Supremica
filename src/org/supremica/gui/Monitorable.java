
/************************ Monitorable.java *******************/

// Interface for being monitored by the Monitor class that
// manages the progress monitoring
package org.supremica.gui;

import org.supremica.gui.ExecutionDialogMode;

public interface Monitorable
{
	int getProgress();

	String getActivity();

	void stopTask();

	boolean wasStopped();
}
