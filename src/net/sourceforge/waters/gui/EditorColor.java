package net.sourceforge.waters.gui;

import java.awt.Color;

/**
 * Class defining the colors that are used in the editor.
 * @see EditorObject.getColor()
 */
class EditorColor
{
	public static Color HIGHLIGHTCOLOR = Color.CYAN; 

	public static Color SELECTCOLOR = Color.BLUE; 

	//public static Color DRAGSELECTCOLOR = Color.PINK; 
	public static Color DRAGSELECTCOLOR = new Color(0,0,255,36); 

	public static Color DEFAULTMARKINGCOLOR = Color.GRAY; 

	public static Color ERRORCOLOR = Color.RED; 
	//public static Color ERRORCOLOR_NODE = ERRORCOLOR.brighter(); // Doesn't work?
	public static Color ERRORCOLOR_NODE = new Color(255,0,0,128); 

	public static Color DEFAULTCOLOR = Color.BLACK; 
	public static Color DEFAULTCOLOR_NODEGROUP = Color.lightGray; 
}


