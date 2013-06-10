// ** MF ********************* Utility.java *******************//
// ** license, blah blah blah **//

package org.supremica.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;

import net.sourceforge.waters.gui.util.IconLoader;

/**
 * The utility class that contains the functions that do the real job.
 *
 * @author Martin Fabian
 */

public class Utility
{

    /**
     * Returns a point for the upper left corner of a centred component of size comp_d
     */
    public static Point getPosForCenter(final Dimension comp_d)
    {
		final KeyboardFocusManager kfm = KeyboardFocusManager.getCurrentKeyboardFocusManager();
		final Window windowFrame = kfm.getFocusedWindow();
		Point centerPoint = null;
		if (windowFrame != null)
		{
			final Point frameTopLeft = windowFrame.getLocation();
			final Dimension frameSize = windowFrame.getSize();
			final int x = (int)(frameTopLeft.getX() + (frameSize.width/2) - (comp_d.width/2));
			final int y = (int)(frameTopLeft.getY() + (frameSize.height/2) - (comp_d.height/2));
			centerPoint = new Point(x, y);
		}
		else
		{
        	final Toolkit tool_kit = Toolkit.getDefaultToolkit();
        	final Dimension screen_d = tool_kit.getScreenSize();
        	centerPoint = new Point((screen_d.width - comp_d.width) / 2, (screen_d.height - comp_d.height) / 2);
		}
		return centerPoint;
    }

    public static void setupFrame(final JFrame frame, final int width, final int height)
    {
        frame.setSize(width, height);    // from Component
        frame.setLocation(getPosForCenter(new Dimension(width, height)));    // from Component
        final Image image = IconLoader.ICON_APPLICATION.getImage();
        frame.setIconImage(image);    // from Frame
    }

    public static void setupFrame(final JFrame frame, final Dimension dimension)
    {
        setupFrame(frame, (int)dimension.getWidth(), (int)dimension.getHeight());
    }

    public static void setupPane(final JScrollPane pane)
    {
        pane.getViewport().setBackground(Color.white);
    }

    public static void setupDialog(final JDialog dialog, final int width, final int height)
    {
        dialog.setSize(width, height);    // from Component
        dialog.setLocation(getPosForCenter(new Dimension(width, height)));    // from Component

        // dialog.setIconImage(Supremica.cornerImage);    // from Frame
    }

    public static JButton setDefaultButton(final JFrame frame, final JButton b)
    {
        frame.getRootPane().setDefaultButton(b);

        return b;
    }

    public static JButton setDefaultButton(final JDialog dialog, final JButton b)
    {
        dialog.getRootPane().setDefaultButton(b);

        return b;
    }

    public static JButton setDisabledButton(final JFrame frame, final JButton b)
    {
        b.setEnabled(false);

        return b;
    }
}
