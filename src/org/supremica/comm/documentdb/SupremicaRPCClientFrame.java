package org.supremica.comm.documentdb;

import javax.swing.JOptionPane;
import java.io.*;
import org.supremica.gui.Supremica;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import rpcdocdb.client.*;

public class SupremicaRPCClientFrame
	extends ClientFrame
{
	private static final long serialVersionUID = 1L;
	private Supremica supremica;

	public SupremicaRPCClientFrame(RPCDocClient client, String document, Supremica supremica)
	{
		super(client, document);

		this.supremica = supremica;
	}

	// --------------------------------------------------------
	protected void onSave()
	{
		try
		{
			Project proj = supremica.getActiveProject();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintWriter pw = new PrintWriter(baos);
			AutomataToXml atx = new AutomataToXml(proj);

			atx.serialize(pw);
			pw.flush();
			client.saveDocument(document, baos.toByteArray());
		}
		catch (Exception exx)
		{
			JOptionPane.showMessageDialog(this, exx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	protected void onLoad()
	{
		try
		{
			Project proj = supremica.getActiveProject();
			int size = proj.size();

			if (size > 0)
			{
				int opt = JOptionPane.showConfirmDialog(this, "There are currently " + size + " automata in the porject\n" + "Would you like to remove (delete!) these automata first?\n" + "\n" + "If you choose no now, and a loaded automaton has the same\n" + "name as an existing one, the old automaton will not be overwritten.\n" + "Instead, the new one will be renamed.", "Clear project first?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

				if (opt == JOptionPane.CANCEL_OPTION)
				{
					return;
				}

				if (opt == JOptionPane.YES_OPTION)
				{
					proj.clear();
				}
			}

			byte[] data = client.loadDocument(document);
			ByteArrayInputStream bais = new ByteArrayInputStream(data);
			ProjectBuildFromXml build = new ProjectBuildFromXml();

			proj = build.build(bais);

			supremica.addAutomata(proj);
		}
		catch (Exception exx)
		{
			JOptionPane.showMessageDialog(this, exx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
