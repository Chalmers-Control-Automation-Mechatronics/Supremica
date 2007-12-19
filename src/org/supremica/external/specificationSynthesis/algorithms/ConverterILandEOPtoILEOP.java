package org.supremica.external.specificationSynthesis.algorithms;

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.*;

public class ConverterILandEOPtoILEOP {

	Document ILEOPDoc;
	Element ILsEOPs = new Element("ILsEOPs");

	public ConverterILandEOPtoILEOP() {

	}



	public void convertILandEOPtoILEOP(Document ILDoc, Document EOPDoc) {


		// ILEOPDoc corresponds to the output xml document.
		ILEOPDoc = new Document(ILsEOPs);
		Element EOProot = EOPDoc.getRootElement();
		Element ILroot = ILDoc.getRootElement();




		// To make the file look like Petter´s old testfiles...
		placeSimultaneity();

		// Get all operations processes
		List opList = EOProot.getChildren("Operation");

		// Place all operations.

		for(Iterator listOP = opList.iterator(); listOP.hasNext(); )
		{
			Element single_OP_element = (Element) listOP.next();
			String single_OP_id = single_OP_element.getAttributeValue("opID");

			List EOP = single_OP_element.getChildren("EOP");
			placeEOPs(single_OP_id, EOP);
		}

		List evILList = ILroot.getChildren("Event_interlocking");

		for(Iterator evILIter = evILList.iterator(); evILIter.hasNext(); )
		{
			Element evILelement = (Element) evILIter.next();
			Element evIL = (Element) evILelement.clone();
			ILsEOPs.addContent(evIL);

		}

		List rILList = ILroot.getChildren("Robot_interlocking");

		for(Iterator rILIter = rILList.iterator(); rILIter.hasNext(); )
		{
			Element rILelement = (Element) rILIter.next();
			Element rIL = (Element) rILelement.clone();
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
		Element sim = new Element("simultaneity");
		Element proc1 = new Element("Process");
		proc1.setAttribute("Id", "OA");
		sim.addContent(proc1);
		Element proc2 = new Element("Process");
		proc2.setAttribute("Id", "OB");
		sim.addContent(proc2);
		ILsEOPs.addContent(sim);
	}


	/**********************************************************
	*
	* placeEOPs
	*
	**********************************************************/

	public void placeEOPs(String opId, List eopList) {


		if(eopList.size() == 1)
		{
			Element eop = (Element) eopList.get(0);
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

	public void placeSingleEOP(String opId, Element eop) {


		Element op = new Element("Operation");
		op.setAttribute("id", opId);


		List initList = eop.getChildren("InitialState");
		Element init = (Element) initList.get(0);
		String description = init.getAttributeValue("Description");


		Element proc = new Element("Process");
		proc.setAttribute("id", "init"+opId);
		op.addContent(proc);
		Element ev = new Element("Event");
		if(description != null)
		{
			ev.setAttribute("id", description);
		}
		proc.addContent(ev);
		Element restr = new Element("Restriction");
		ev.addContent(restr);
		Element and = new Element("And");
		restr.addContent(and);



		List actValues = init.getChildren("ActuatorValue");
		for(Iterator actIter = actValues.iterator(); actIter.hasNext(); )
		{
			Element actValue = (Element) actIter.next();

			List actList = actValue.getChildren("Actuator");
			Element act = (Element) actList.get(0);
			String name = act.getText();

			List valList = actValue.getChildren("Value");
			Element val = (Element) valList.get(0);
			String value = val.getText();

			Element state = new Element("State");
			and.addContent(state);
			state.setAttribute("name", name);
			state.setAttribute("id", value);

		}


		List sensValues = init.getChildren("SensorValue");
		for(Iterator sensIter = sensValues.iterator(); sensIter.hasNext(); )
		{
			Element sensValue = (Element) sensIter.next();
			List sensList = sensValue.getChildren("Sensor");
			Element sens = (Element) sensList.get(0);
			String name = sens.getText();

			List valList = sensValue.getChildren("Value");
			Element val = (Element) valList.get(0);
			String value = val.getText();

			Element state = new Element("State");
			and.addContent(state);
			state.setAttribute("name", name);
			state.setAttribute("id", value);
		}


		List actionList = eop.getChildren("Action");
		for(Iterator actionIter = actionList.iterator(); actionIter.hasNext(); )
		{

			Element action = (Element) actionIter.next();
			String aName = action.getAttributeValue("actionNbr");
			String aDescription = action.getAttributeValue("Description");


			Element aproc = new Element("Process");
			aproc.setAttribute("id", "action"+aName);
			op.addContent(aproc);

			Element aev = new Element("Event");
			if(aDescription != null)
			{
				aev.setAttribute("id", aDescription);
			}
			aproc.addContent(aev);
			Element arestr = new Element("Restriction");
			aev.addContent(arestr);
			Element aand = new Element("And");
			arestr.addContent(aand);


			List aactValues = action.getChildren("ActuatorValue");
			for(Iterator actIter = aactValues.iterator(); actIter.hasNext(); )
			{
				Element actValue = (Element) actIter.next();

				List actList = actValue.getChildren("Actuator");
				Element act = (Element) actList.get(0);
				String name = act.getText();

				List valList = actValue.getChildren("Value");
				Element val = (Element) valList.get(0);
				String value = val.getText();

				Element state = new Element("State");
				aand.addContent(state);
				state.setAttribute("name", name);
				state.setAttribute("id", value);

			}

			List asensValues = action.getChildren("SensorValue");
			for(Iterator sensIter = asensValues.iterator(); sensIter.hasNext(); )
			{
				Element sensValue = (Element) sensIter.next();
				List sensList = sensValue.getChildren("Sensor");
				Element sens = (Element) sensList.get(0);
				String name = sens.getText();

				List valList = sensValue.getChildren("Value");
				Element val = (Element) valList.get(0);
				String value = val.getText();


				Element state = new Element("State");
				aand.addContent(state);
				state.setAttribute("name", name);
				state.setAttribute("id", value);
			}

		}

		ILsEOPs.addContent(op);

	}

}