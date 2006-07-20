package net.sourceforge.waters.gui.renderer;

import java.awt.Shape;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;

public interface RendererShape
{
  public void draw(Graphics2D g, RenderingInformation status);
	
	public Shape getShape();
  
	public boolean isClicked(int x, int y);
}
