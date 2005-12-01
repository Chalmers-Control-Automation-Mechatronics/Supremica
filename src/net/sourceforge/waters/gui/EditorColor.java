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

	/** The default color of marked (accepting) nodes. */
	public static Color DEFAULTMARKINGCOLOR = Color.GRAY; 

	/** The color of the drag-select area. */
	public static Color DRAGSELECTCOLOR = new Color(0,0,255,32); 

	/** The default color of objects. */
	public static Color DEFAULTCOLOR = Color.BLACK; 
	public static Color DEFAULTCOLOR_LABEL = (Color.GREEN).darker().darker(); 
	public static Color DEFAULTCOLOR_NODEGROUP = Color.lightGray; 

	/** The color of erring objects. For example colliding nodes and nodegroups. */
	public static Color ERRORCOLOR = Color.RED; 
	public static Color ERRORCOLOR_NODE = ERRORCOLOR.darker();

	/** The color of selected objects. */
	public static Color SELECTCOLOR = Color.BLUE; 

	public static Color HIGHLIGHTCOLOR = Color.CYAN; 

    /** the color of acceptable drag objects */
    public static Color CANDROP = (Color.GREEN).darker().darker();
    public static Color CANTDROP = Color.RED;

	/** Invisible color. */
	public static Color INVISIBLE = new Color(0,0,0,0);

	/**
	 * Returns a transparent variant of the supplied color. The alpha-value is changed to 
	 * SHADOWALPHA.
	 *
	 * @see #SHADOWALPHA
	 */
	protected static Color shadow(Color color)
	{
		return new Color(color.getRed(), color.getGreen(), color.getBlue(), SHADOWALPHA);
	}
}


