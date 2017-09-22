package org.supremica.external.shoefactory.Executor;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import grafchart.sfc.EditorAPI;


public class EditorCreator
{
	private static Logger logger = LogManager.getLogger(EditorCreator.class);
	static EditorAPI e = null;
	static boolean running = false;

	public EditorCreator(final String[] args)
	{
		if (e == null)
		{
			e = new EditorAPI(args);
		}
		else
		{
			logger.info("You cannot have another instance of Jgrafchart running!");
		}
	}

	public EditorAPI getEditor()
	{
		running = true;

		return e;
	}

	public boolean isStarted()
	{
		return running;
	}
}
