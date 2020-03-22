//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2019 Knut Akesson, Martin Fabian, Robi Malik
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

package org.supremica.gui;

import java.awt.Frame;

import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

// This class should really act as a factory for exporter objects, but that
// would mean rewriting the entire export/saveAs functionality. Should I bother?
public class ExportDialog
{
	private static final String xmlString = "xml";
	@SuppressWarnings("unused")
    private static final String spString = "sp";
	private static final String dotString = "dot";
	private static final String dsxString = "dsx";
	private static final String htmlString = "html";
	private static final String fsmString = "fsm";
	private static final String pcgString = "pcg";
	private static final String sspcString = "sspc";
    private static final String stsString = "sts";
	private static final String smcString = "smc";
	private static final String smvString = "smv";	// The NuSMV model checker format
	private static final Object[] possibleValues = { xmlString, // spString,
	dotString, dsxString,
	fsmString,
	htmlString,
	pcgString,
	sspcString,
    stsString,
	smcString,
	smvString,
	};
	private JOptionPane pane = null;
	private JDialog dialog = null;
	private JCheckBox checkbox = null;
	private Object selectedValue = null;

	public ExportDialog(final Frame comp)
	{
		this.pane = new JOptionPane("Export as::", JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,    // icon
			null,    // options
			null);    // initialValue

		pane.setWantsInput(true);
		pane.setSelectionValues(possibleValues);
		pane.setInitialSelectionValue(possibleValues[0]);
		pane.setComponentOrientation(((comp == null) ? JOptionPane.getRootFrame() : comp).getComponentOrientation());
		pane.selectInitialValue();

		this.checkbox = new JCheckBox("Export to debugview");

		pane.add(checkbox);

		// int style = styleFromMessageType(JOptionPane.INFORMATION_MESSAGE);
		dialog = pane.createDialog(comp, "Export");
	}

	public void show()
	{

		// this.selectedValue = JOptionPane.showInputDialog(comp, "Export as", "Export", JOptionPane.INFORMATION_MESSAGE, null, possibleValues, possibleValues[0]);
		dialog.setVisible(true);
		dialog.dispose();

		// Is this the right thing to do? It seems to work, but the manuals...
		if (((Integer) pane.getValue()).intValue() == JOptionPane.CANCEL_OPTION)
		{
			selectedValue = null;

			return;
		}

		selectedValue = pane.getInputValue();
	}

	public boolean wasCancelled()
	{
		return selectedValue == null;
	}

	// public Exporter getExporter()
	public ExportFormat getExportMode()
	{
		if (selectedValue == xmlString)
		{
			if (checkbox.isSelected())
			{
				return ExportFormat.XML_DEBUG;
			}

			return ExportFormat.XML;    // Should return an XmlExporter object
		}
		else if (selectedValue == dotString)
		{
			if (checkbox.isSelected())
			{
				return ExportFormat.DOT_DEBUG;
			}

			return ExportFormat.DOT;    // Should return a DotExporter object
		}
		else if (selectedValue == dsxString)
		{
			if (checkbox.isSelected())
			{
				return ExportFormat.DSX_DEBUG;
			}

			return ExportFormat.DSX;    // Should return a DsxExporter object
		}
		else if (selectedValue == fsmString)
		{
			if (checkbox.isSelected())
			{
				return ExportFormat.FSM_DEBUG;
			}

			return ExportFormat.FSM;    // Should return a FsmExporter object
		}
        else if (selectedValue == stsString)
		{
			if (checkbox.isSelected())
			{
				return ExportFormat.STS_DEBUG;
			}

			return ExportFormat.STS;    // Should return an XmlExporter object
		}
//		else if (selectedValue == spString)
//		{
//			if (checkbox.isSelected())
//			{
//				return ExportFormat.SP_DEBUG;
//			}
//
//			return ExportFormat.SP;    // Should return a SpExporter object
//		}
		else if (selectedValue == htmlString)
		{
			return checkbox.isSelected() ? ExportFormat.HTML_DEBUG : ExportFormat.HTML;    // Should relly return a HtmlExporter object
		}
		else if (selectedValue == pcgString)
		{
			return (checkbox.isSelected()) ? ExportFormat.PCG_DEBUG	: ExportFormat.PCG;
		}
		else if (selectedValue == sspcString)
		{
			return ExportFormat.SSPC;    // no debugview here (multiple files)
		}
		else if(selectedValue == smcString)
		{
			return checkbox.isSelected() ? ExportFormat.SMC_DEBUG : ExportFormat.SMC;
		}
		else if(selectedValue == smvString)
		{
			return checkbox.isSelected() ? ExportFormat.SMV_DEBUG : ExportFormat.SMV;
		}

		else
		{
			return ExportFormat.UNKNOWN;
		}
	}
}
