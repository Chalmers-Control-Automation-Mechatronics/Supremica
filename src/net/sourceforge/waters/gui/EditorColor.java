package net.sourceforge.waters.gui;

import java.awt.Color;

/**
 * Class defining the colors that are used in the editor.
 * @see EditorObject.getColor()
 */
class EditorColor
{
	private static final int SHADOWALPHA = 16;

	public static Color DRAGSELECTCOLOR = new Color(0,0,255,32); 

	public static Color DEFAULTMARKINGCOLOR = Color.GRAY; 

	public static Color ERRORCOLOR = Color.RED; 
	public static Color ERRORCOLOR_NODE = new Color(255,0,0,128); 
	public static Color ERRORSHADOWCOLOR = new Color(255,0,0,SHADOWALPHA); 

	public static Color SELECTCOLOR = Color.BLUE; 
	public static Color SELECTSHADOWCOLOR = new Color(0,0,255,SHADOWALPHA); 

	public static Color HIGHLIGHTCOLOR = Color.CYAN; 
	public static Color HIGHLIGHTSHADOWCOLOR = new Color(0,255,255,SHADOWALPHA); 

	public static Color DEFAULTCOLOR = Color.BLACK; 
	public static Color DEFAULTSHADOWCOLOR = new Color(0,0,0,SHADOWALPHA); 
	public static Color DEFAULTCOLOR_NODEGROUP = Color.lightGray; 
	public static Color DEFAULTSHADOWCOLOR_NODEGROUP = new Color(0,0,0,SHADOWALPHA/2); 

	public static Color INVISIBLE = new Color(0,0,0,0);
}


