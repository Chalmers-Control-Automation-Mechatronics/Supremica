package org.supremica.comm.documentdb;

import javax.swing.JOptionPane;
import org.supremica.gui.Supremica;
import org.supremica.properties.SupremicaProperties;
import rpcdocdb.client.*;

public class DocumentRPCDatabase
{
	public static void createNew(Supremica supremica)
	{
		try
		{
			ClientConnectionDialog cdd = new ClientConnectionDialog();

			cdd.setHost(SupremicaProperties.getDocDBHost());
			cdd.setPort(SupremicaProperties.getDocDBPort());
			cdd.setUsername(SupremicaProperties.getDocDBUsername());
			cdd.setDocument(SupremicaProperties.getDocDBDocument());

			if (!cdd.doModal())
			{
				return;
			}

			SupremicaProperties.setDocDBHost(cdd.getHost());
			SupremicaProperties.setDocDBPort(cdd.getPort());
			SupremicaProperties.setDocDBUsername(cdd.getUsername());
			SupremicaProperties.setDocDBDocument(cdd.getDocument());
			SupremicaProperties.savePropperties();

			RPCDocClient client = new RPCDocClient(cdd);
			ClientFrame cf = new SupremicaRPCClientFrame(client, cdd.getDocument(), supremica);
		}
		catch (Exception exx)
		{
			JOptionPane.showMessageDialog(null, exx.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
}
