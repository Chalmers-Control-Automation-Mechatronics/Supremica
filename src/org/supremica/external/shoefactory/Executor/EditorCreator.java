package org.supremica.external.shoefactory.Executor;

import grafchart.sfc.*;

public class EditorCreator
{
	static EditorAPI e = null;
	static boolean running = false;

	public EditorCreator(String[] args)
	{	
		if(e == null)
			e = new EditorAPI(args);
		else
			System.out.println("You cannot have another instance of Jgrafchart running!");
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
