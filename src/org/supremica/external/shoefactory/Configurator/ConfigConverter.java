package org.supremica.external.shoefactory.Configurator;

public class ConfigConverter
{
	public ConfigConverter() {}

	public boolean[] getConfig(String c, String s, String gt, String g, String st, String m)
	{
		boolean[] stationVisit = new boolean[24];

		if (c.compareTo("red") == 0)
		{
			stationVisit[14] = true;
		}
		else if (c.compareTo("black") == 0)
		{
			stationVisit[15] = true;
		}
		else if (c.compareTo("white") == 0)
		{

			//nothing
		}
		else if (c.compareTo("brown") == 0)
		{
			stationVisit[15] = true;
		}
		else if (c.compareTo("pink") == 0)
		{
			stationVisit[14] = true;
		}
		else if (c.compareTo("green") == 0)
		{
			stationVisit[15] = true;
		}

		if (s.compareTo("35") == 0)
		{
			stationVisit[0] = true;
			stationVisit[5] = true;
			stationVisit[9] = true;
		}
		else if (s.compareTo("36") == 0)
		{
			stationVisit[1] = true;
			stationVisit[6] = true;
			stationVisit[10] = true;
		}
		else if (s.compareTo("37") == 0)
		{
			stationVisit[2] = true;
			stationVisit[7] = true;
			stationVisit[11] = true;
		}
		else if (s.compareTo("38") == 0)
		{
			stationVisit[3] = true;
			stationVisit[8] = true;
			stationVisit[9] = true;
		}
		else if (s.compareTo("39") == 0)
		{
			stationVisit[4] = true;
			stationVisit[5] = true;
			stationVisit[11] = true;
		}
		else if (s.compareTo("40") == 0)
		{
			stationVisit[0] = true;
			stationVisit[6] = true;
			stationVisit[9] = true;
		}
		else if (s.compareTo("41") == 0)
		{
			stationVisit[1] = true;
			stationVisit[7] = true;
			stationVisit[10] = true;
		}
		else if (s.compareTo("42") == 0)
		{
			stationVisit[2] = true;
			stationVisit[8] = true;
			stationVisit[11] = true;
		}
		else if (s.compareTo("43") == 0)
		{
			stationVisit[3] = true;
			stationVisit[5] = true;
			stationVisit[9] = true;
		}
		else if (s.compareTo("44") == 0)
		{
			stationVisit[4] = true;
			stationVisit[6] = true;
			stationVisit[10] = true;
		}
		else if (s.compareTo("45") == 0)
		{
			stationVisit[0] = true;
			stationVisit[7] = true;
			stationVisit[11] = true;
		}
		else if (s.compareTo("46") == 0)
		{
			stationVisit[1] = true;
			stationVisit[8] = true;
			stationVisit[9] = true;
		}

		if (gt.compareTo("Adult") == 0)
		{
			stationVisit[21] = true;
		}
		else if (gt.compareTo("Children") == 0)
		{
			stationVisit[22] = true;
		}

		if (g.compareTo("Male") == 0)
		{
			stationVisit[12] = true;
		}
		else if (g.compareTo("Female") == 0)
		{
			stationVisit[13] = true;
		}

		if (st.compareTo("typeA") == 0)
		{

			//stationVisit[20]=true;
		}
		else if (st.compareTo("typeB") == 0)
		{

			//nothing
		}

		if (m.compareTo("Hiking") == 0)
		{
			stationVisit[16] = true;
		}
		else if (m.compareTo("Indoors") == 0)
		{
			stationVisit[17] = true;
		}
		else if (m.compareTo("Walking") == 0)
		{
			stationVisit[18] = true;
		}
		else if (m.compareTo("Running") == 0)
		{
			stationVisit[19] = true;
		}

		stationVisit[23] = true;

		return stationVisit;
	}
}
