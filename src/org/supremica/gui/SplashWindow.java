package org.supremica.gui;

import java.awt.*;

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
                this.setLayout(new BorderLayout());
                this.add(new JLabel("Supremica"), BorderLayout.CENTER);
        }
}