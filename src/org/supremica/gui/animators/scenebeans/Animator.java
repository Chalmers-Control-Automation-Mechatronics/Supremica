//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2017 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.gui.animators.scenebeans;

import java.awt.Checkbox;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.List;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Iterator;
import java.util.TreeSet;

import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sourceforge.waters.gui.util.IconAndFontLoader;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import uk.ac.ic.doc.scenebeans.animation.Animation;
import uk.ac.ic.doc.scenebeans.animation.AnimationCanvas;
import uk.ac.ic.doc.scenebeans.animation.CommandException;
import uk.ac.ic.doc.scenebeans.animation.parse.XMLAnimationParser;
import uk.ac.ic.doc.scenebeans.event.AnimationEvent;
import uk.ac.ic.doc.scenebeans.event.AnimationListener;
import uk.ac.ic.doc.scenebeans.input.MouseDispatcher;


public class Animator
    extends JFrame
    implements ActionListener
{
    private static final long serialVersionUID = 1L;
    private static Logger logger = LogManager.getLogger(Animator.class);

    AnimationCanvas _canvas;
    private final MouseDispatcher _dispatcher;
    private final List _commands, _events, _announced;
    private final Checkbox _paused, _centered, _stretched, _aspect;
    private final JPanel contentPane;

    public Animator(final String detail)
    throws Exception
    {
        super("Supremica Animator - " + detail);

        contentPane = (JPanel) getContentPane();
        final java.util.List<Image> images = IconAndFontLoader.ICONLIST_APPLICATION;
        setIconImages(images);
        contentPane.setLayout(new GridBagLayout());

        final GridBagConstraints pos = new GridBagConstraints();

        pos.fill = GridBagConstraints.BOTH;
        pos.gridx = GridBagConstraints.RELATIVE;
        pos.gridx = 0;
        pos.gridy = 0;

        contentPane.add(new Label("Commands", Label.CENTER), pos);

        pos.gridy++;

        pos.weighty = 1.0;
        _commands = new List(8, false);

        _commands.addActionListener(this);
        contentPane.add(_commands, pos);

        pos.gridy++;

        pos.weighty = 0.0;

        contentPane.add(new Label("Events", Label.CENTER), pos);

        pos.gridy++;

        pos.weighty = 1.0;
        _events = new List(8, false);

        contentPane.add(_events, pos);

        pos.gridy++;

        pos.weighty = 0.0;
        _paused = new Checkbox("Pause");

        _paused.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(final ItemEvent ev)
            {
                _canvas.setPaused(_paused.getState());
            }
        });
        contentPane.add(_paused, pos);

        pos.gridy++;

        pos.weighty = 0.0;
        _centered = new Checkbox("Center");

        _centered.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(final ItemEvent ev)
            {
                _canvas.setAnimationCentered(_centered.getState());
            }
        });
        contentPane.add(_centered, pos);

        pos.gridy++;

        pos.weighty = 0.0;
        _stretched = new Checkbox("Stretch");

        _stretched.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(final ItemEvent ev)
            {
                _canvas.setAnimationStretched(_stretched.getState());
            }
        });
        contentPane.add(_stretched, pos);

        pos.gridy++;

        pos.weighty = 0.0;
        _aspect = new Checkbox("Aspect");

        _aspect.addItemListener(new ItemListener()
        {
            @Override
            public void itemStateChanged(final ItemEvent ev)
            {
                _canvas.setAnimationAspectFixed(_aspect.getState());
            }
        });
        contentPane.add(_aspect, pos);

        pos.gridx++;

        pos.gridy = 0;
        pos.weightx = 1.0;
        pos.gridheight = GridBagConstraints.REMAINDER;
        ;
        _canvas = new AnimationCanvas();

        _canvas.setBackground(java.awt.Color.white);

        final RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        _canvas.setRenderingHints(hints);
        contentPane.add(_canvas, pos);

        pos.gridx++;

        pos.weightx = 0.0;
        pos.gridy = 0;
        pos.weighty = 0.0;
        pos.gridheight = 1;

        contentPane.add(new Label("Announced", Label.CENTER), pos);

        pos.gridy++;

        pos.weighty = 1.0;
        pos.gridheight = GridBagConstraints.REMAINDER;
        _announced = new List(8, false);

        contentPane.add(_announced, pos);

        _dispatcher = new MouseDispatcher(_canvas.getSceneGraph(), _canvas);

        _dispatcher.attachTo(_canvas);
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(final WindowEvent ev)
            {
                dispose();
            }

            @Override
            public void windowClosed(final WindowEvent ev)
            {}
        });
    }

    @SuppressWarnings("unchecked")
    public void setAnimation(final Animation anim)
    {
        _canvas.setAnimation(anim);

        Iterator<?> i;

        i = new TreeSet<Object>(anim.getCommandNames()).iterator();

        while (i.hasNext())
        {
            _commands.add((String) i.next());
        }

        i = new TreeSet<Object>(anim.getEventNames()).iterator();

        while (i.hasNext())
        {
            _events.add((String) i.next());
        }

        anim.addAnimationListener(new AnimationListener()
        {
            @Override
            public void animationEvent(final AnimationEvent ev)
            {
                _announced.add(ev.getName());
                _announced.makeVisible(_announced.getItemCount() - 1);

                //Toolkit.getDefaultToolkit().beep();
            }
        });
        invalidate();
        pack();
    }

    public Animation getAnimation()
    {
        return _canvas.getAnimation();
    }

    @Override
    public void actionPerformed(final ActionEvent ev)
    {
        final String command = _commands.getSelectedItem();

        if (command != null)
        {
            try
            {
                getAnimation().invokeCommand(command);
            }
            catch (final CommandException ex)
            {
                logger.error(ex.getMessage());
                logger.debug(ex.getStackTrace());
            }
        }
    }

    static void usageError()
    {
        logger.error("usage: Animator <xml-file> [<name> <value>]*");

        return;
    }

    public static void main(final String[] args)
    {
        try
        {
            if (args.length % 2 != 1)
            {
                usageError();
            }

            final Animator view = new Animator(args[0]);

            view.addWindowListener(new WindowAdapter()
            {
                @Override
                public void windowClosing(final WindowEvent ev)
                {
                    view.dispose();
                }

                @Override
                public void windowClosed(final WindowEvent ev)
                {
                    System.exit(1);
                }
            });

            final XMLAnimationParser parser = new XMLAnimationParser(new File(args[0]), view._canvas);

            for (int i = 1; i < args.length; i += 2)
            {
                parser.addMacro(args[i], args[i + 1]);
            }

            view.setAnimation(parser.parseAnimation());
            view.setVisible(true);
        }
        catch (final Exception ex)
        {
            logger.error("Error in Animator. ", ex);
            logger.debug(ex.getStackTrace());
            System.exit(1);
        }
    }
}
