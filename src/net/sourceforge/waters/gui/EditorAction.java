package net.sourceforge.waters.gui;

import javax.swing.JTextPane;

public class EditorAction extends JTextPane {
	public EditorAction(String action) 
	{
		super();
		this.setText(action);
		this.setForeground(EditorColor.ACTIONCOLOR);
		this.setEditable(false);
	}
	
	public String toString() 
	{
		return super.toString();
	}
}
