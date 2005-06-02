package net.sourceforge.waters.gui;

import java.awt.Color;

/**
 * Class defining the colors that are used in the editor.
 * @see EditorObject#getColor
 */
class EditorColor
{
	/** The alpha value of the shadow-colors. */
	private static final int SHADOWALPHA = 32;

	public static Color DEFAULTMARKINGCOLOR = Color.GRAY; 

	public static Color DRAGSELECTCOLOR = new Color(0,0,255,32); 

	public static Color ERRORCOLOR = Color.RED; 
	public static Color ERRORCOLOR_NODE = ERRORCOLOR.darker();

	public static Color SELECTCOLOR = Color.BLUE; 

	public static Color HIGHLIGHTCOLOR = Color.CYAN; 

	public static Color DEFAULTCOLOR = Color.BLACK; 
	public static Color DEFAULTCOLOR_LABEL = (Color.GREEN).darker().darker(); 
	public static Color DEFAULTCOLOR_NODEGROUP = Color.lightGray; 

	public static Color INVISIBLE = new Color(0,0,0,0);

	/**
	 * Returns a shadowed variant of the supplied color. The alpha-value is changed to 
	 * SHADOWALPHA.
	 *
	 * @see #SHADOWALPHA
	 */
	protected static Color shadow(Color color)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), SHADOWALPHA);
	}
}


