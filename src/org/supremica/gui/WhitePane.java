//** MF ********************* Utility.java *******************//
//** license, blah blah blah **//

// Heap of useful classes we should all use
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

//** This one sets its own background to white
class WhitePane extends JScrollPane
{
	private void setWhite()
	{
		Utility.setupPane(this);;
	}
	
	public WhitePane()
	{
		setWhite();
	}
	
	public WhitePane(Component view)
	{
		super(view);
		setWhite();
	}
	
	public WhitePane(Component view, int vsbPolicy, int hsbPolicy)
	{
		super(view, vsbPolicy, hsbPolicy);
		setWhite();
	}
	
	public WhitePane(int vsbPolicy, int hsbPolicy) 
	{
		super(vsbPolicy, hsbPolicy);
		setWhite();
	}
}

