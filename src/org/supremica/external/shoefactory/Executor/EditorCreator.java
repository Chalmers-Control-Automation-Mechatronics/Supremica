package org.supremica.external.shoefactory.Executor;

import grafchart.sfc.*;
import org.supremica.log.*;

public class EditorCreator
{
	private static Logger logger = LoggerFactory.createLogger(EditorCreator.class);
	
	static EditorAPI e = null;
	static boolean running = false;

	public EditorCreator(String[] args)
	{	
		if(e == null)
			e = new EditorAPI(args);
		else
			logger.info("You cannot have another instance of Jgrafchart running!");
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
