package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;

public class GrowFrame
	extends Frame
	implements ActionListener
{
	private IntArray vars;
	private GrowCanvas canvas;
	private long start_time, end_time;
	private Button bQuit, bDump, bReturn;
	private Label status;
	private TextArea ta;

	public GrowFrame(String txt)
	{
		super(txt);

		vars = new IntArray();

		Panel pNorth = new Panel(new FlowLayout(FlowLayout.LEFT));

		add(pNorth, BorderLayout.NORTH);
		pNorth.add(bQuit = new Button("Close"));
		bQuit.addActionListener(this);

		pNorth.add(bDump = new Button("Values"));
		bDump.addActionListener(this);

		pNorth.add(bReturn = new Button("Graph"));
		bReturn.addActionListener(this);
		bReturn.setVisible(false);



		add(status = new Label(), BorderLayout.SOUTH);


		canvas = new GrowCanvas();

		add(canvas, BorderLayout.CENTER);

		add( ta = new TextArea(20,80), BorderLayout.WEST);
		ta.setVisible(false);

		start_time = -1;

		pack();
		setVisible(true);
		startTimer();
	}

	public void startTimer()
	{
		start_time = System.currentTimeMillis();
	}

	public void stopTimer()
	{
		showTime();
	}

	private void showTime()
	{
		end_time = System.currentTimeMillis();

		status.setText("Time " + (end_time - start_time) + " [ms]");
	}

	public void add(int value)
	{
		showTime();
		vars.add(value);
		canvas.repaint();
	}

	private void onDump()
	{

		int size_x = vars.getSize();
		StringBuffer sb = new StringBuffer();

		sb.append("x=[0:" + size_x + "];\n");
		sb.append("\n");
		sb.append("y=[");

		for (int i = 0; i < size_x; i++)
		{
			if(i != 0) sb.append("; ");
			if( (i % 20) == 0) sb.append("\n");
			sb.append(vars.get(i));
		}
		sb.append("];\n");

		ta.setText( sb.toString());

		canvas.setVisible(false);
		ta.setVisible(true);

		bDump.setVisible(false);
		bReturn.setVisible(true);
		pack();
	}

	private void onReturn()
	{
		ta.setVisible(false);
		canvas.setVisible(true);
		bReturn.setVisible(false);
		bDump.setVisible(true);
		bReturn.setVisible(false);
		pack();
	}

	public void actionPerformed(ActionEvent e)
	{
		Object src = e.getSource();

		if (src == bQuit)
		{
			dispose();
		}
		else if (src == bDump)
		{
			onDump();
		}
		else if (src == bReturn)
		{
			onReturn();
		}
	}


	private class GrowCanvas
		extends Canvas
	{
		public GrowCanvas()
		{
			this.resize(400, 300);
		}

		public void paint(Graphics g)
		{
			int min = vars.getMin();

			if (min > 0)
			{
				min = 0;
			}

			int max = vars.getMax();
			Dimension dims = this.size();
			int size_x = vars.getSize();
			int size_y = (max - min);
			int marg_y = (dims.height * 1) / 10;

			if(size_y < 1) size_y = 1; // avoid DIV BY ZERO

			dims.height -= 2 * marg_y;

			int old_x = -1, old_y = -1;    // initilized to junk (or jikes will complain)

			if (size_x == 0)
			{
				return;    // no values yet
			}

			for (int i = 0; i < size_x; i++)
			{
				int x = (i * dims.width) / size_x;
				int y = dims.height + marg_y - (vars.get(i) * dims.height) / size_y;

				if (i != 0)
				{
					g.drawLine(old_x, old_y, x, y);
				}

				old_x = x;
				old_y = y;
			}

			g.drawString("" + size_x + " points, max " + max + ", last:" + vars.get(size_x - 1), 10, 10);
		}
	}
}
