package org.supremica.gui;

import java.awt.*;
import javax.swing.*;

public class SplashWindow
	extends java.awt.Window
{
	private static final long serialVersionUID = 1L;
	private static ImageIcon splashIcon = new ImageIcon(Supremica.class.getResource("/splash_v1.gif"));

	public SplashWindow()
	{
		super(new Frame());

		int height = splashIcon.getIconHeight();
		int width = splashIcon.getIconWidth();

		this.setSize(new Dimension(width, height));

		BorderLayout layout = new BorderLayout();

		this.setLayout(layout);
		this.add(new JLabel(splashIcon), BorderLayout.CENTER);

		/*
		 * Center splash window on screen
		 */
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension splashWindowSize = getSize();

		if (splashWindowSize.height > screenSize.height)
		{
			splashWindowSize.height = screenSize.height;
		}

		if (splashWindowSize.width > screenSize.width)
		{
			splashWindowSize.width = screenSize.width;
		}

		setLocation((screenSize.width - splashWindowSize.width) / 2, (screenSize.height - splashWindowSize.height) / 2);
	}
}
