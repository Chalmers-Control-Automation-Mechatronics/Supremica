package org.supremica.gui;

import java.awt.*;
import javax.swing.*;

public class SplashWindow
        extends java.awt.Window
{
        public SplashWindow()
        {
                super(new Frame());

                try
                {
                        init();
                }
                catch (Exception e)
                {
                        e.printStackTrace();
                }

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
                setLocation((screenSize.width  - splashWindowSize.width)  / 2,
                        (screenSize.height - splashWindowSize.height) / 2);
        }

        private void init()
                throws Exception
        {
                this.setSize(new Dimension(300, 170));
                BorderLayout layout = new BorderLayout();
                this.setLayout(layout);
                ImageIcon imageIcon = new ImageIcon(Supremica.class.getResource("/splash_v1.gif"));
                layout.add(new JLabel(imageIcon), BorderLayout.CENTER);
        }
}