package org.supremica.gui;

import java.awt.Cursor;

public interface DotViewerInterface
{
	public void internalBuild();
	public void setCursor(Cursor theCursor);
	public void draw();
	public void stopProcess();
}