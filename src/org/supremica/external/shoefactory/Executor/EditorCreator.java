package org.supremica.external.shoefactory.Executor;

import org.supremica.external.shoefactory.plantBuilder.*;
import org.supremica.external.shoefactory.Animator.*;
import org.supremica.gui.*;
import grafchart.sfc.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.util.*;
import org.supremica.automata.*;
import org.supremica.log.*;

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
