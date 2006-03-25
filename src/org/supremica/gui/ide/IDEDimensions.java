
package org.supremica.gui.ide;

import java.awt.Dimension;

public class IDEDimensions
{
	private final static int MAINWINDOWWIDTH = 800;
	private final static int MAINWINDOWHEIGHT = 600;
	public static Dimension mainWindowPreferredSize = new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT);
	public static Dimension mainWindowMinimumSize = new Dimension(100, 100);

	public static Dimension loggerPreferredSize = new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT*1/3);
	public static Dimension loggerMinimumSize = new Dimension(100, 100);

	public static Dimension mainPanelPreferredSize = new Dimension(MAINWINDOWWIDTH, MAINWINDOWHEIGHT*2/3);
	public static Dimension mainPanelMinimumSize = new Dimension(100, 100);

	public static Dimension leftEditorPreferredSize = new Dimension(MAINWINDOWWIDTH*1/3, MAINWINDOWHEIGHT*2/3);
	public static Dimension leftEditorMinimumSize = new Dimension(100, 50);

	public static Dimension rightEditorPreferredSize = new Dimension(MAINWINDOWWIDTH*2/3, MAINWINDOWHEIGHT*2/3);
	public static Dimension rightEditorMinimumSize = new Dimension(100, 50);

	public static Dimension leftAnalyzerPreferredSize = leftEditorPreferredSize;
	public static Dimension leftAnalyzerMinimumSize = leftEditorMinimumSize;

	public static Dimension rightAnalyzerPreferredSize = rightEditorPreferredSize;
	public static Dimension rightAnalyzerMinimumSize = rightEditorMinimumSize;

	public static Dimension rightEmptyPreferredSize = leftEditorPreferredSize;
	public static Dimension rightEmptyMinimumSize = leftEditorMinimumSize;

	private IDEDimensions()
	{
	}
}
