package net.sourceforge.waters.gui;

import java.awt.Color;

import net.sourceforge.waters.gui.renderer.RenderingInformation;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.SimpleNodeProxy;
import net.sourceforge.waters.model.module.GroupNodeProxy;
import net.sourceforge.waters.model.module.LabelGeometryProxy;

/**
 * Class defining the colors that are used in the editor.
 * @see EditorObject#getColor
 */
public class EditorColor
{
    /** The alpha value of the shadow-colors. */
    private static final int SHADOWALPHA = 48;
    
    /** The default color of disabled text. */
    public static final Color DISABLEDCOLOR = Color.GRAY;
    
    /** The default color of marked (accepting) nodes. */
    public static final Color DEFAULTMARKINGCOLOR = Color.GRAY;
    
    /** The color of guard expressions. */
    public static final Color GUARDCOLOR = Color.GREEN.darker().darker();
    
    /** The color of action expressions. */
    public static final Color ACTIONCOLOR = Color.BLUE;
    
    /** The color of the drag-select area. */
    public static final Color DRAGSELECTCOLOR = new Color(0,0,255,32);
    
    /** The default color of objects. */
    public static final Color DEFAULTCOLOR = Color.BLACK;
    public static final Color DEFAULTCOLOR_LABEL = (Color.GREEN).darker().darker();
    public static final Color DEFAULTCOLOR_NODEGROUP = Color.lightGray;
    
    /** The color of erring objects. For example colliding nodes and nodegroups. */
    public static final Color ERRORCOLOR = Color.RED;
    public static final Color ERRORCOLOR_NODE = ERRORCOLOR.darker();
    
    /** The color of selected objects. */
    public static final Color SELECTCOLOR = Color.BLUE;
    
    /** The color of objects when showing wether stuff can be dropped on them. */
    public static final Color CANDROPCOLOR = (Color.GREEN).darker().darker();
    public static final Color CANTDROPCOLOR = Color.RED;
    
    /** Invisible color. */
    public static final Color INVISIBLE = new Color(0,0,0,0);
    
    /**
     * Returns a transparent variant of the supplied color. The
     * alpha-value is changed to SHADOWALPHA.
     *
     * @see #SHADOWALPHA
     */
    public static Color shadow(Color color)
    {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), SHADOWALPHA);
    }
    
    /**
     * Returns the apropriate color for painting this object.
     */
    public static Color getColor(Proxy o, EditorSurface.DRAGOVERSTATUS dragOver, boolean selected,
        boolean error)
    {
        // In order of importance
        if(dragOver != EditorSurface.DRAGOVERSTATUS.NOTDRAG)
        {
            if (dragOver == EditorSurface.DRAGOVERSTATUS.CANDROP)
                return CANDROPCOLOR;
            else if(dragOver == EditorSurface.DRAGOVERSTATUS.CANTDROP)
                return CANTDROPCOLOR;
        }
        else if(error)
        {
            if(o instanceof SimpleNodeProxy)
            {
                // Slightly different color, to distinguish nodes from
                // nodegroups more clearly. Overkill?
                return ERRORCOLOR_NODE;
            }
            return ERRORCOLOR;
        }
        else if(selected)
        {
            return SELECTCOLOR;
        }
        
        // Defaults
        if(o instanceof GroupNodeProxy)
        {
            return DEFAULTCOLOR_NODEGROUP;
        }
        else if(o instanceof LabelGeometryProxy)
        {
            return DEFAULTCOLOR_LABEL;
        }
        return DEFAULTCOLOR;
    }
    
    /**
     * Returns a lighter shade of the color of the object for drawing a "shadow".
     */
    public static Color getShadowColor(Proxy o, EditorSurface.DRAGOVERSTATUS dragOver, 
        boolean selected, boolean error)
    {
        // Overrides, if not selected and not error (then the color is normal...)
        if(!selected && !error && o instanceof GroupNodeProxy)
        {
            // Unfortunately, the light gray color gives a too weak shadow!
            return shadow(getColor(o, dragOver, selected, error).darker().darker().darker());
        }

        // Return the shadowed variant of the ordinary color of this object
        return shadow(getColor(o, dragOver, selected, error));
    }
}
