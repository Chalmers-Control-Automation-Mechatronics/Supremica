package org.supremica.gui.examplegenerator;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.supremica.automata.Project;
import org.supremica.gui.ide.IDE;
import org.supremica.testcases.warehouse.SelectEventsWindow;
import org.supremica.testcases.warehouse.Warehouse;

class WarehousePanel extends JPanel implements TestCase {
	private static final long serialVersionUID = 1L;
	Warehouse warehouse = new Warehouse();
	IntegerField nbr_events_k = new IntegerField("3", 6);
	IntegerField nbr_events_m = new IntegerField("1", 6);
	SelectEventsWindow selectOperatorEventsWindow = null;
	SelectEventsWindow selectUnobservableEventsWindow = null;

	WarehousePanel() {
		JPanel panel = new JPanel(new GridLayout(3, 2));

		add(panel, BorderLayout.WEST);
		panel.add(new JLabel("Number of operator events (k): "));
		panel.add(nbr_events_k);
		panel.add(new JLabel("Number of supervisor events (m): "));
		panel.add(nbr_events_m);

		JButton selectOperatorEventsButton = new JButton(
				"Select operator events");

		selectOperatorEventsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectOperatorEventsWindow == null) {
					selectOperatorEventsWindow = new SelectEventsWindow(
							warehouse.getTruckAlphabet(),
							"Select operator events", "Select operator events",
							true);
				}

				selectOperatorEventsWindow.actionPerformed(e);

				// ActionMan.fileOpen(ActionMan.getGui());
			}
		});
		panel.add(selectOperatorEventsButton);

		// JButton selectControlEventsButton = new JButton("Select control
		// events");
		// panel.add(selectControlEventsButton);
		JButton selectUnobservableEventsButton = new JButton(
				"Select unobservable events");

		selectUnobservableEventsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (selectUnobservableEventsWindow == null) {
					selectUnobservableEventsWindow = new SelectEventsWindow(
							warehouse.getTruckAlphabet(),
							"Select unobservable events",
							"Select unobservable events", false);
				}

				selectUnobservableEventsWindow.actionPerformed(e);

				// ActionMan.fileOpen(ActionMan.getGui());
			}
		});
		panel.add(selectUnobservableEventsButton);
	}

	public void synthesizeSupervisor(IDE ide) {
	}

	public Project generateAutomata() throws Exception {
		warehouse.setK(nbr_events_k.get());
		warehouse.setM(nbr_events_m.get());

		// System.err.println("Warehouse doIt");
		return warehouse.getProject();
	}
}
