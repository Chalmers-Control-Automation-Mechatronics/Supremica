
/************************ Monitorable.java *******************/

// Interface for being monitored by the Monitor class that
// manages the progress monitoring
package org.supremica.gui;

import org.supremica.gui.ExecutionDialogMode;

public interface Monitorable
{
	public int getProgress();
	public String getActivity();
	public void stopTask();
	public boolean wasStopped();
}
