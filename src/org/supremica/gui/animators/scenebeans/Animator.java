package org.supremica.gui.animators.scenebeans;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.*;
import java.util.Iterator;
import java.util.TreeSet;
import org.supremica.gui.*;
import uk.ac.ic.doc.scenebeans.*;
import uk.ac.ic.doc.scenebeans.activity.*;
import uk.ac.ic.doc.scenebeans.behaviour.*;
import uk.ac.ic.doc.scenebeans.event.*;
import uk.ac.ic.doc.scenebeans.animation.*;
import uk.ac.ic.doc.scenebeans.animation.parse.*;
import uk.ac.ic.doc.scenebeans.pick.*;
import uk.ac.ic.doc.scenebeans.input.*;

public class Animator
	extends JFrame
	implements ActionListener
{
	AnimationCanvas _canvas;
	MouseDispatcher _dispatcher;
	List _commands, _events, _announced;
	Checkbox _paused, _centered, _stretched, _aspect;

	public Animator(String detail)
		throws Exception
	{
		setFrameIcon(Supremica.cornerIcon);
		setTitle("Supremica Animator - " + detail);
		setLayout(new GridBagLayout());

		GridBagConstraints pos = new GridBagConstraints();

		pos.fill = GridBagConstraints.BOTH;
		pos.gridx = GridBagConstraints.RELATIVE;
		pos.gridx = 0;
		pos.gridy = 0;

		add(new Label("Commands", Label.CENTER), pos);

		pos.gridy++;

		pos.weighty = 1.0;
		_commands = new List(8, false);

		_commands.addActionListener(this);
		add(_commands, pos);

		pos.gridy++;

		pos.weighty = 0.0;

		add(new Label("Events", Label.CENTER), pos);

		pos.gridy++;

		pos.weighty = 1.0;
		_events = new List(8, false);

		add(_events, pos);

		pos.gridy++;

		pos.weighty = 0.0;
		_paused = new Checkbox("Pause");

		_paused.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ev)
			{
				_canvas.setPaused(_paused.getState());
			}
		});
		add(_paused, pos);

		pos.gridy++;

		pos.weighty = 0.0;
		_centered = new Checkbox("Center");

		_centered.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ev)
			{
				_canvas.setAnimationCentered(_centered.getState());
			}
		});
		add(_centered, pos);

		pos.gridy++;

		pos.weighty = 0.0;
		_stretched = new Checkbox("Stretch");

		_stretched.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ev)
			{
				_canvas.setAnimationStretched(_stretched.getState());
			}
		});
		add(_stretched, pos);

		pos.gridy++;

		pos.weighty = 0.0;
		_aspect = new Checkbox("Aspect");

		_aspect.addItemListener(new ItemListener()
		{
			public void itemStateChanged(ItemEvent ev)
			{
				_canvas.setAnimationAspectFixed(_aspect.getState());
			}
		});
		add(_aspect, pos);

		pos.gridx++;

		pos.gridy = 0;
		pos.weightx = 1.0;
		pos.gridheight = GridBagConstraints.REMAINDER;
		;
		_canvas = new AnimationCanvas();

		_canvas.setBackground(java.awt.Color.white);

		RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		_canvas.setRenderingHints(hints);
		add(_canvas, pos);

		pos.gridx++;

		pos.weightx = 0.0;
		pos.gridy = 0;
		pos.weighty = 0.0;
		pos.gridheight = 1;

		add(new Label("Announced", Label.CENTER), pos);

		pos.gridy++;

		pos.weighty = 1.0;
		pos.gridheight = GridBagConstraints.REMAINDER;
		_announced = new List(8, false);

		add(_announced, pos);

		_dispatcher = new MouseDispatcher(_canvas.getSceneGraph(), _canvas);

		_dispatcher.attachTo(_canvas);
	}

	public void setAnimation(Animation anim)
	{
		_canvas.setAnimation(anim);

		Iterator i;

		i = new TreeSet(anim.getCommandNames()).iterator();

		while (i.hasNext())
		{
			_commands.add((String) i.next());
		}

		i = new TreeSet(anim.getEventNames()).iterator();

		while (i.hasNext())
		{
			_events.add((String) i.next());
		}

		anim.addAnimationListener(new AnimationListener()
		{
			public void animationEvent(AnimationEvent ev)
			{
				_announced.add(ev.getName());
				_announced.makeVisible(_announced.getItemCount() - 1);
				Toolkit.getDefaultToolkit().beep();
			}
		});
		invalidate();
		pack();
	}

	public Animation getAnimation()
	{
		return _canvas.getAnimation();
	}

	public void actionPerformed(ActionEvent ev)
	{
		String command = _commands.getSelectedItem();

		if (command != null)
		{
			try
			{
				getAnimation().invokeCommand(command);
			}
			catch (CommandException ex)
			{
				System.err.println(ex.getMessage());
			}
		}
	}

	static void usageError()
	{
		System.err.println("usage: Animator <xml-file> [<name> <value>]*");
		System.exit(1);
	}

	public static void main(String[] args)
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
				public void windowClosing(WindowEvent ev)
				{
					view.dispose();
				}

				public void windowClosed(WindowEvent ev)
				{
					System.exit(1);
				}
			});

			XMLAnimationParser parser = new XMLAnimationParser(new File(args[0]), view._canvas);

			for (int i = 1; i < args.length; i += 2)
			{
				parser.addMacro(args[i], args[i + 1]);
			}

			view.setAnimation(parser.parseAnimation());
			view.setVisible(true);
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			System.exit(1);
		}
	}
}
