
/********************** ScheduleDialog.java *****************/
package org.supremica.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;
import org.supremica.log.*;
import org.supremica.automata.*;
import org.supremica.automata.algorithms.scheduling.*;

class Estimates
	extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JRadioButton zeroEstimate = new JRadioButton("zero estimate", true);    // default selected
	private JRadioButton leastRemainingTime = new JRadioButton("least remaining time");
	private JRadioButton oneProductRelaxation = new JRadioButton("one product relaxation");
	private JRadioButton oneMachineRelaxation = new JRadioButton("one machine relaxation");
	private JRadioButton twoMachineRelaxation = new JRadioButton("two machine relaxation");

	Estimates()
	{
		setLayout(new GridLayout(2, 2));
		add(zeroEstimate);
		add(oneMachineRelaxation);
		add(leastRemainingTime);
		add(oneProductRelaxation);
		add(twoMachineRelaxation);

		ButtonGroup bg = new ButtonGroup();

		bg.add(zeroEstimate);
		bg.add(oneMachineRelaxation);
		bg.add(leastRemainingTime);
		bg.add(oneProductRelaxation);
		bg.add(twoMachineRelaxation);
		zeroEstimate.setEnabled(true);
		oneMachineRelaxation.setEnabled(false);
		leastRemainingTime.setEnabled(false);
		oneProductRelaxation.setEnabled(true);
		twoMachineRelaxation.setEnabled(false);
		setBorder(BorderFactory.createTitledBorder("Estimates"));
	}

	public void setEnabled(boolean b)
	{
		zeroEstimate.setEnabled(b);    // the others are always disabled (as yet)
		oneProductRelaxation.setEnabled(b);
	}

	Estimator getEstimator(Automata automata)
	{
		Estimator estimator = new DefaultEstimator(automata);

		if (oneProductRelaxation.isSelected())
		{
			estimator = new SimpleEstimator(automata);
		}

		return estimator;
	}
}

class AlgorithmsPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JRadioButton algo1 = new JRadioButton("Algo 1");
	private JRadioButton algo3 = new JRadioButton("Algo 3", true);    // default selected

	AlgorithmsPanel()
	{
		setLayout(new GridLayout(1, 2));
		setBorder(BorderFactory.createTitledBorder("Algorithms"));
		add(algo1);
		add(algo3);

		ButtonGroup bg = new ButtonGroup();

		bg.add(algo1);
		bg.add(algo3);
	}

	int getAlgorithm()
	{
		if (algo1.isSelected())
		{
			return 1;
		}

		if (algo3.isSelected())
		{
			return 3;
		}

		return 0;
	}
}

class WeightsPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;
	private JTextField gWeight = new JTextField("1");
	private JTextField hWeight = new JTextField("1");

	class EnableButton
		extends JCheckBox
	{
		private static final long serialVersionUID = 1L;

		EnableButton()
		{
			super("Use weights");

			addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					enableWeights(isSelected());
				}
			});
		}
	}

	private EnableButton enableButton = new EnableButton();

	WeightsPanel()
	{
		setLayout(new GridLayout(1, 2));
		setBorder(BorderFactory.createTitledBorder("Weights"));
		add(enableButton);
		add(gWeight);
		add(hWeight);
		enableWeights(false);
	}

	private void enableWeights(boolean b)
	{
		gWeight.setEnabled(b);
		hWeight.setEnabled(b);
	}

	public double getWeightG()
	{
		return Double.valueOf(gWeight.getText()).doubleValue();
	}

	public double getWeightH()
	{
		return Double.valueOf(hWeight.getText()).doubleValue();
	}

	public Calculator getCalculator(Estimator estimator)
	{
		if (enableButton.isSelected())
		{
			return new WeightedCalculator(estimator, getWeightG(), getWeightH());
		}
		else
		{
			return new DefaultCalculator(estimator);
		}
	}
}

class ProgressPanel
	extends JPanel
{
	private static final long serialVersionUID = 1L;

	ProgressPanel()
	{
		setBorder(BorderFactory.createTitledBorder("Progress"));
	}
}

public class ScheduleDialog
	extends JDialog
{
	private static final long serialVersionUID = 1L;

	private static Logger logger = LoggerFactory.createLogger(ScheduleDialog.class);

	class OkButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		OkButton()
		{
			super("Ok");

			addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					doit();
				}
			});
		}
	}

	class CancelButton
		extends JButton
	{
		private static final long serialVersionUID = 1L;

		CancelButton()
		{
			super("Cancel");

			addActionListener(new java.awt.event.ActionListener()
			{
				public void actionPerformed(java.awt.event.ActionEvent e)
				{
					done();
				}
			});
		}
	}

	private Estimates estimates;

	// private AlgorithmsPanel algorithms;
	private WeightsPanel weights;

	// private JCheckBox invertPlantSpec = new JCheckBox("Treat plants as spec and vice versa");
	private JCheckBox getTimeFromPlants = new JCheckBox("Get times from plants");
	private JCheckBox closeOnFinishing = new JCheckBox("Close on finishing", true);
	private OkButton okButton;
	private CancelButton cancelButton;
	private ProgressPanel progress;

	public ScheduleDialog()
	{
		this(ActionMan.getGui().getFrame());
	}

	public ScheduleDialog(JFrame frame)
	{
		super(frame, "Schedule Selected Automata", true);

		getContentPane().setLayout(new GridLayout(6, 1));

		this.okButton = new OkButton();
		this.cancelButton = new CancelButton();
		this.estimates = new Estimates();

		// this.algorithms = new AlgorithmsPanel();
		this.weights = new WeightsPanel();
		this.progress = new ProgressPanel();

		JPanel buttonpanel = new JPanel();

		buttonpanel.add(okButton);
		buttonpanel.add(cancelButton);
		getContentPane().add(estimates);

		// getContentPane().add(invertPlantSpec);
		getContentPane().add(getTimeFromPlants);

		// getContentPane().add(algorithms);
		getContentPane().add(weights);
		getContentPane().add(buttonpanel);
		getContentPane().add(closeOnFinishing);
		getContentPane().add(progress);
		Utility.setDefaultButton(this, okButton);
		Utility.setupDialog(this, 400, 350);
	}

	void doit()
	{

		// Disable all but the cancel button and the progressbar
		estimates.setEnabled(false);
		okButton.setEnabled(false);
		getTimeFromPlants.setEnabled(false);

		Automata automata = ActionMan.getGui().getSelectedAutomata();

		if (getTimeFromPlants.isSelected())
		{

			// temporarily set all plants as specs and all specs as plants - reset below
			invertTypes(automata);
		}

		try
		{
			ModifiedAstar mastar = new ModifiedAstar(automata, weights.getCalculator(estimates.getEstimator(automata)));
			Element elem = mastar.walk();

			if (elem == null)
			{
				throw new RuntimeException("no marked state found");
			}

			// logger.info(mastar.trace(elem));
			logger.info(mastar.getInfo(elem).toString());

			Automaton automaton = mastar.getAutomaton(elem);

			ActionMan.getGui().addAutomaton(automaton);
		}
		catch (Exception excp)
		{
			logger.error("ScheduleDialog::doit " + excp);
			logger.debug(excp.getStackTrace());
		}

		if (getTimeFromPlants.isSelected())
		{

			// reset the automaton types
			invertTypes(automata);
		}

		if (closeOnFinishing.isSelected())
		{
			done();
		}
		else
		{

			// Enable what was previously disabled
			estimates.setEnabled(true);
			okButton.setEnabled(true);
			getTimeFromPlants.setEnabled(true);
			cancelButton.setText("Close");
		}
	}

	void done()
	{
		setVisible(false);
		dispose();
		getParent().repaint();
	}

	private static void invertTypes(Automata automata)
	{
		for (Iterator it = automata.iterator(); it.hasNext(); )
		{
			Automaton automaton = (Automaton) it.next();

			if (automaton.isSpecification())
			{
				automaton.setType(AutomatonType.Plant);
			}
			else if (automaton.isPlant())
			{
				automaton.setType(AutomatonType.Specification);
			}
		}
	}
}
