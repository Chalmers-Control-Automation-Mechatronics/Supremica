package org.supremica.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;


public class SplashWindow extends java.awt.Window
{
  private static final long serialVersionUID = 1L;
  private static ImageIcon splashIcon =
    new ImageIcon(Supremica.class.getResource("/icons/greeter/supremica.png"));

  public SplashWindow()
  {
    super(new Frame());

    final int height = splashIcon.getIconHeight();
    final int width = splashIcon.getIconWidth();

    this.setSize(new Dimension(width, height));

    final BorderLayout layout = new BorderLayout();

    this.setLayout(layout);
    this.add(new JLabel(splashIcon), BorderLayout.CENTER);

    // Center splash window on screen
    final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    final Dimension splashWindowSize = getSize();

    if (splashWindowSize.height > screenSize.height) {
      splashWindowSize.height = screenSize.height;
    }

    if (splashWindowSize.width > screenSize.width) {
      splashWindowSize.width = screenSize.width;
    }

    setLocation((screenSize.width - splashWindowSize.width) / 2,
                (screenSize.height - splashWindowSize.height) / 2);
  }
}
