package org.supremica.external.avocades.specificationsynthesis;

import java.util.Iterator;
import java.util.List;

import org.jdom.Document;
import org.jdom.Element;


public class ConverterILandEOPtoILEOP {

	Document ILEOPDoc;
	Element ILsEOPs = new Element("ILsEOPs");

	public ConverterILandEOPtoILEOP() {

	}

	public void convertILandEOPtoILEOP(final Document ILDoc, final Document EOPDoc) {

		// ILEOPDoc corresponds to the output xml document.
		ILEOPDoc = new Document(ILsEOPs);
		final Element EOProot = EOPDoc.getRootElement();
		final Element ILroot = ILDoc.getRootElement();


		// To make the file look like Petter´s old testfiles...
		placeSimultaneity();

		// Get all operations processes
		final List<?> opList = EOProot.getChildren("Operation");

		// Place all operations.

		for(final Iterator<?> listOP = opList.iterator(); listOP.hasNext(); )
		{
			final Element single_OP_element = (Element) listOP.next();
			final String single_OP_id = single_OP_element.getAttributeValue("opID");

			final List<?> EOP = single_OP_element.getChildren("EOP");
			placeEOPs(single_OP_id, EOP);
		}

		final List<?> evILList = ILroot.getChildren("Event_interlocking");

		for(final Iterator<?> evILIter = evILList.iterator(); evILIter.hasNext(); )
		{
			final Element evILelement = (Element) evILIter.next();
			final Element evIL = (Element) evILelement.clone();
			ILsEOPs.addContent(evIL);
		}

		final List<?> rILList = ILroot.getChildren("Robot_interlocking");

		for(final Iterator<?> rILIter = rILList.iterator(); rILIter.hasNext(); )
		{
			final Element rILelement = (Element) rILIter.next();
			final Element rIL = (Element) rILelement.clone();
			ILsEOPs.addContent(rIL);
		}

	}



	/**********************************************************
	*
	* getDoc
	*
	**********************************************************/
	public Document getDoc() {
		return ILEOPDoc;
	}


	/**********************************************************
	*
	* placeSimultaneity
	*
	**********************************************************/

	public void placeSimultaneity()
	{
		final Element sim = new Element("simultaneity");
		final Element proc1 = new Element("Process");
		proc1.setAttribute("Id", "OA");
		sim.addContent(proc1);
		final Element proc2 = new Element("Process");
		proc2.setAttribute("Id", "OB");
		sim.addContent(proc2);
		ILsEOPs.addContent(sim);
	}


	/**********************************************************
	*
	* placeEOPs
	*
	**********************************************************/

	public void placeEOPs(final String opId, final List<?> eopList) {


		if(eopList.size() == 1)
		{
			final Element eop = (Element) eopList.get(0);
			placeSingleEOP(opId, eop);
		}
		else if(eopList.size() > 1)
		{
			System.out.println("PlaceAlternativeEOPs not implemented yet.");
			//placeAlternativeEOPs();
		}
		else
			System.err.println("Missing EOP");

	}


	/**********************************************************
	*
	* placeSingleEOP
	*
	**********************************************************/

	public void placeSingleEOP(final String opId, final Element eop) {


		final Element op = new Element("Operation");
		op.setAttribute("id", opId);


		final List<?> initList = eop.getChildren("InitialState");
		final Element init = (Element) initList.get(0);
		final String description = init.getAttributeValue("Description");


		final Element proc = new Element("Process");
		proc.setAttribute("id", "init"+opId);
		op.addContent(proc);
		final Element ev = new Element("Event");
		if(description != null)
		{
			ev.setAttribute("id", description);
		}
		proc.addContent(ev);
		final Element restr = new Element("Restriction");
		ev.addContent(restr);
		final Element and = new Element("And");
		restr.addContent(and);



		final List<?> actValues = init.getChildren("ActuatorValue");
		for(final Iterator<?> actIter = actValues.iterator(); actIter.hasNext(); )
		{
			final Element actValue = (Element) actIter.next();

			final List<?> actList = actValue.getChildren("Actuator");
			final Element act = (Element) actList.get(0);
			final String name = act.getText();

			final List<?> valList = actValue.getChildren("Value");
			final Element val = (Element) valList.get(0);
			final String value = val.getText();

			final Element state = new Element("State");
			and.addContent(state);
			state.setAttribute("name", name);
			state.setAttribute("id", value);

		}


		final List<?> sensValues = init.getChildren("SensorValue");
		for(final Iterator<?> sensIter = sensValues.iterator(); sensIter.hasNext(); )
		{
			final Element sensValue = (Element) sensIter.next();
			final List<?> sensList = sensValue.getChildren("Sensor");
			final Element sens = (Element) sensList.get(0);
			final String name = sens.getText();

			final List<?> valList = sensValue.getChildren("Value");
			final Element val = (Element) valList.get(0);
			final String value = val.getText();

			final Element state = new Element("State");
			and.addContent(state);
			state.setAttribute("name", name);
			state.setAttribute("id", value);
		}


		final List<?> actionList = eop.getChildren("Action");
		for(final Iterator<?> actionIter = actionList.iterator(); actionIter.hasNext(); )
		{

			final Element action = (Element) actionIter.next();
			final String aName = action.getAttributeValue("actionNbr");
			final String aDescription = action.getAttributeValue("Description");


			final Element aproc = new Element("Process");
			aproc.setAttribute("id", "action"+aName);
			op.addContent(aproc);

			final Element aev = new Element("Event");
			if(aDescription != null)
			{
				aev.setAttribute("id", aDescription);
			}
			aproc.addContent(aev);
			final Element arestr = new Element("Restriction");
			aev.addContent(arestr);
			final Element aand = new Element("And");
			arestr.addContent(aand);


			final List<?> aactValues = action.getChildren("ActuatorValue");
			for(final Iterator<?> actIter = aactValues.iterator(); actIter.hasNext(); )
			{
				final Element actValue = (Element) actIter.next();

				final List<?> actList = actValue.getChildren("Actuator");
				final Element act = (Element) actList.get(0);
				final String name = act.getText();

				final List<?> valList = actValue.getChildren("Value");
				final Element val = (Element) valList.get(0);
				final String value = val.getText();

				final Element state = new Element("State");
				aand.addContent(state);
				state.setAttribute("name", name);
				state.setAttribute("id", value);

			}

			final List<?> asensValues = action.getChildren("SensorValue");
			for(final Iterator<?> sensIter = asensValues.iterator(); sensIter.hasNext(); )
			{
				final Element sensValue = (Element) sensIter.next();
				final List<?> sensList = sensValue.getChildren("Sensor");
				final Element sens = (Element) sensList.get(0);
				final String name = sens.getText();

				final List<?> valList = sensValue.getChildren("Value");
				final Element val = (Element) valList.get(0);
				final String value = val.getText();


				final Element state = new Element("State");
				aand.addContent(state);
				state.setAttribute("name", name);
				state.setAttribute("id", value);
			}

		}

		ILsEOPs.addContent(op);

	}

}