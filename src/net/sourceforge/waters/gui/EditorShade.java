//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorShade
//###########################################################################
//# $Id: EditorShade.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.*;

public class EditorShade
{
	private String name;
	private int RGB;

	public EditorShade()
	{
		name = "";
		RGB = (int)Math.pow(2, 24) -1;
	}

	public EditorShade(String n, int R, int G, int B)
	{
		name = n;
		RGB = R * (int)Math.pow(2,16) + G * (int)Math.pow(2,8) + B;
	}


	public EditorShade(String n, int IRGB)
	{
		name = n;
		RGB = IRGB;
	}
	
	public void setRGB(int R, int G, int B)
	{
		RGB = R * (int)Math.pow(2,16) + G * (int)Math.pow(2,8) + B;
	}


	public void setRGB(int NRGB)
	{
		RGB = NRGB;
	}

	public void setName(String n)
	{
		name = n;
	}
	
	public int getRGB()
	{
		return RGB;
	}
	
	public String getName()
	{
		return name;
	}
}
