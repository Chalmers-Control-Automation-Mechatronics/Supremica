package org.supremica.util.BDD;

import java.awt.*;
import java.awt.event.*;

public class GrowFrame
	extends Frame
	implements ActionListener
{
	private final static long SHOW_THRESHOLD = 1000; /** how long to wait before next graph is drawn */

	private IntArray vars;
	private GrowCanvas canvas;
	private long start_time, end_time, last_time;
	private Button bQuit, bDump, bReturn;
	private Label status;
	private TextArea ta;
	private boolean showGraph;
	private boolean stopped;


	public GrowFrame(String txt)
	{
		super(txt);

		this.showGraph = true;
		this.vars = new IntArray();

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

		add( ta = new TextArea(20,60), BorderLayout.WEST);
		ta.setVisible(false);

		start_time = -1;
		last_time = -1;

		pack();
		setVisible(true);
		startTimer();
	}

	/**
	 * MAY be called to indicate that we just started counting
	 */
	public void startTimer()
	{
		start_time = System.currentTimeMillis();
		stopped = false;
	}

	/**
	 * MUST be called to indicate that we are done!
	 */
	public void stopTimer()
	{
		end_time = System.currentTimeMillis();
		stopped = true;
		flush();
	}

	public void add(int value)
	{
		end_time = System.currentTimeMillis();
		vars.add(value);


		// dont update toooooo often
		if(end_time < last_time + SHOW_THRESHOLD)
			return;

		last_time = end_time;



		update_screen();
	}

	public void flush() {
		update_screen();
	}
	private void update_screen() {
		status.setText("Time " + (end_time - start_time) + " [ms]");
		canvas.repaint();
	}


	private void onDump()
	{

		int size_x = vars.getSize();
		StringBuffer sb = new StringBuffer();

		sb.append("x=[1:" + size_x + "];\n");
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
			showGraph = false;
			onDump();
		}
		else if (src == bReturn)
		{
			showGraph = true;
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
			if(!showGraph) return;

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




			if(stopped) {
				// we have stoped, and afford to draw a better cuve
				g.setColor(Color.black);

				for (int i = 0; i < size_x; i++)
				{
					int x = (i * dims.width) / size_x;
					int y = dims.height + marg_y - (vars.get(i) * dims.height) / size_y;
					if(i != 0)
						g.drawLine(old_x, old_y, x, y);

					old_x = x;
					old_y = y;
				}
				g.setColor(Color.red); // for the upcoming drawString
			} else {
				// this one is "a-bit" faster (could be much faster using _skips_)
				// good if size becomes huge, ~ 10 000 is not very unusuall)

				// get first one:
				old_x = (0 * dims.width) / size_x;
				old_y = dims.height + marg_y - (vars.get(0) * dims.height) / size_y;

				for (int i = 1; i < size_x; i++)
				{
					int x = (i * dims.width) / size_x;

					if(x == old_x) {
						// dont draw
					} else {
						int y = dims.height + marg_y - (vars.get(i) * dims.height) / size_y;
						g.drawLine(old_x, old_y, x, y);
						old_x = x;
						old_y = y;
					}
				}
			}

			g.drawString("" + size_x + " points, max " + max + ", last:" + vars.get(size_x - 1), 10, 10);
		}
	}
}
