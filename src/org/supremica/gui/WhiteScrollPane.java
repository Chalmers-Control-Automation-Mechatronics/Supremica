
// ** MF ********************* WhiteScrollPane.java *******************//
// ** license, blah blah blah **//
// Heap of useful classes we should all use
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;

// ** This one sets its own background to white
class WhiteScrollPane
	extends JScrollPane
{
	private void setWhite()
	{
		Utility.setupPane(this);
		;
	}

	public WhiteScrollPane()
	{
		setWhite();
	}

	public WhiteScrollPane(Component view)
	{
		super(view);

		setWhite();
	}

	public WhiteScrollPane(Component view, int vsbPolicy, int hsbPolicy)
	{
		super(view, vsbPolicy, hsbPolicy);

		setWhite();
	}

	public WhiteScrollPane(int vsbPolicy, int hsbPolicy)
	{
		super(vsbPolicy, hsbPolicy);

		setWhite();
	}
}
