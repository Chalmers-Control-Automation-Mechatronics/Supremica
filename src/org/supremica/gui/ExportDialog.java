package org.supremica.gui;

import java.awt.Frame;
import javax.swing.JOptionPane;
import javax.swing.JDialog;
import javax.swing.JCheckBox;

// This class should really act as a factory for exporter objects, but that
// would mean rewriting the entire export/saveAs functionality. Should I bother?
public class ExportDialog
{
	private static final String xmlString = "xml";
	private static final String spString = "sp";
	private static final String dotString = "dot";
	private static final String dsxString = "dsx";
	private static final String htmlString = "html";
	private static final String fsmString = "fsm";
	private static final String pcgString = "pcg";
	private static final String sspcString = "sspc";
    private static final String stsString = "sts";
	private static final Object[] possibleValues = { xmlString, spString,
	dotString, dsxString,
	fsmString,
	htmlString,
	pcgString,
	sspcString,
    stsString};
	private JOptionPane pane = null;
	private JDialog dialog = null;
	private JCheckBox checkbox = null;
	private Object selectedValue = null;

	public ExportDialog(Frame comp)
	{
		this.pane = new JOptionPane("Export as::", JOptionPane.INFORMATION_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null,    // icon
			null,    // options
			null);    // initialValue

		pane.setWantsInput(true);
		pane.setSelectionValues(possibleValues);
		pane.setInitialSelectionValue(possibleValues[0]);
		pane.setComponentOrientation(((comp == null)
		? JOptionPane.getRootFrame()
		: comp).getComponentOrientation());
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
			if (checkbox.isSelected())
			{
				return ExportFormat.HTML_DEBUG;
			}

			return ExportFormat.HTML;    // Should return a HtmlExporter object
		}
		else if (selectedValue == pcgString)
		{
			return (checkbox.isSelected())
			? ExportFormat.PCG_DEBUG
				: ExportFormat.PCG;
		}
		else if (selectedValue == sspcString)
		{
			return ExportFormat.SSPC;    // no debugview here (multiple files)
		}

		else
		{
			return ExportFormat.UNKNOWN;
		}
	}
}