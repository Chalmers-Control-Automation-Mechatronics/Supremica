package org.supremica.external.shoefactory.Executor;

//import org.supremica.external.shoefactory.plantBuilder.*;


public class FactoryExecutor extends Thread
{
	boolean[] stationV;

	public FactoryExecutor(boolean [] sv)
	{
		stationV = sv;
	}

	public void run()
	{
		//JGrafbuilder jg = new JGrafbuilder();
		while(true)
		{
			try	{

			sleep(200);

			}
			catch(Exception ex)
			{}
		}
	}
}