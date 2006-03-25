
package org.supremica.gui.ide;

import java.awt.Dimension;

public class IDEDimensions
{
	private final static int MAINWINDOWWIDTH = 800;
	private final static int MAINWINDOWHEIGHT = 600;
	public static Dimension mainWindowPreferredSize = new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT);
	public static Dimension mainWindowMinimumSize = new Dimension(100, 100);

	public static Dimension loggerPreferredSize = new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT*1/4);
	public static Dimension loggerMinimumSize = new Dimension(100, 10);

	public static Dimension mainPanelPreferredSize = new Dimension(200, MAINWINDOWHEIGHT*3/4);
	public static Dimension mainPanelMinimumSize = new Dimension(100, 50);

	public static Dimension leftEditorPreferredSize = new Dimension(300, 200);
	public static Dimension leftEditorMinimumSize = new Dimension(50, 50);

	public static Dimension rightEditorPreferredSize = new Dimension(500, 200);
	public static Dimension rightEditorMinimumSize = new Dimension(50, 50);

	public static Dimension leftAnalyzerPreferredSize = new Dimension(400, 200);
	public static Dimension leftAnalyzerMinimumSize = new Dimension(50, 50);

	public static Dimension rightAnalyzerPreferredSize = new Dimension(600, 200);
	public static Dimension rightAnalyzerMinimumSize = new Dimension(50, 50);

	public static Dimension rightEmptyPreferredSize = new Dimension(400, 200);
	public static Dimension rightEmptyMinimumSize = new Dimension(50, 50);

	private IDEDimensions()
	{
	}
}
