/******************** MonitorableThread.java *****************/
// Simply combines the Monitorable nterface with the abstract
// Thread class. Presenter needs both
package org.supremica.gui;

import java.lang.Thread;
import org.supremica.gui.Monitorable;

public abstract class MonitorableThread 
	extends Thread 
	implements Monitorable
{
	// all inherited
}
