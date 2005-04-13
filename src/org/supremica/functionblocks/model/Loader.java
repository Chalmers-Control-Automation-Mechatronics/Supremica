/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.functionblocks.model;

import java.io.File;
import java.util.Iterator;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.JAXBException;
import org.supremica.functionblocks.xsd.libraryelement.*;
import org.supremica.properties.SupremicaProperties;


/**
 * The Loader class uses JAXB to load a FB
 * application into the FBRuntime and then run it.
 *
 *
 * Created: Mon Apr  4 10:29:32 2005
 *
 * @author Goran
 * @version 1.0
 */
public class Loader
{
    private Device device;
    private Resource resource;
    private JAXBContext context;
    private Unmarshaller unmarshaller;

    public Loader(Device dev)
    {
		device = dev;
		try
		{
			context = JAXBContext.newInstance("org.supremica.functionblocks.xsd.libraryelement");
			unmarshaller = context.createUnmarshaller();
			unmarshaller.setValidating(false);
		}
		catch (Exception e)
		{
			java.lang.System.err.println(e);
			java.lang.System.exit(1);
		}
    }

    public void load(String fileName)
    {
		try
		{
			Object unmarshalledXmlObject = unmarshaller.unmarshal(new File(fileName));
			if (unmarshalledXmlObject instanceof org.supremica.functionblocks.xsd.libraryelement.FBType)
			{
				loadFBType((org.supremica.functionblocks.xsd.libraryelement.FBType) unmarshalledXmlObject);
			}
			else if (unmarshalledXmlObject instanceof org.supremica.functionblocks.xsd.libraryelement.System)
			{
				loadSystem((org.supremica.functionblocks.xsd.libraryelement.System) unmarshalledXmlObject);
			}
		}
		catch (Exception e)
		{
			java.lang.System.err.println(e);
			java.lang.System.exit(1);
		}
    }

    private void loadSystem(org.supremica.functionblocks.xsd.libraryelement.System xmlSystemData)
    {
		if (xmlSystemData.isSetDevice())
		{
			org.supremica.functionblocks.xsd.libraryelement.Device theDevice = (org.supremica.functionblocks.xsd.libraryelement.Device) xmlSystemData.getDevice().get(0);
			if(theDevice.isSetResource())
			{
				for (Iterator resIter = theDevice.getResource().iterator();resIter.hasNext();)
				{
					org.supremica.functionblocks.xsd.libraryelement.Resource curResource = (org.supremica.functionblocks.xsd.libraryelement.Resource) resIter.next();
					device.addResource(curResource.getName());
					resource = device.getResource(curResource.getName());
					if (curResource.isSetFBNetwork())
					{
						constructNewFBNetwork(curResource.getFBNetwork());
					}
				}
			}
		}
    }

    private void constructNewFBNetwork(org.supremica.functionblocks.xsd.libraryelement.FBNetworkType xmlFBNetworkData)
    {
		resource.addApplicationFragment("FBNetwork");
		ApplicationFragment appFrag =  resource.getApplicationFragment("FBNetwork");
		if (xmlFBNetworkData.isSetFB())
		{
			for (Iterator fbIter = xmlFBNetworkData.getFB().iterator(); fbIter.hasNext();)
			{
				org.supremica.functionblocks.xsd.libraryelement.FB curFB = (org.supremica.functionblocks.xsd.libraryelement.FB) fbIter.next();
				// get and load the FB type
				if(resource.getFBType(curFB.getType() + ".fbt") == null)
				{
					load(SupremicaProperties.getFBRuntimeLibraryPath() + "/" + curFB.getType() + ".fbt");
				}
				appFrag.addFBInstance(curFB.getName(),curFB.getType());
			}
		}
		if (xmlFBNetworkData.isSetEventConnections())
		{
			//java.lang.System.out.println("Event Connections:");
			for (Iterator eventConnIter = xmlFBNetworkData.getEventConnections().getConnection().iterator(); eventConnIter.hasNext();)
			{
				org.supremica.functionblocks.xsd.libraryelement.Connection curConn = (org.supremica.functionblocks.xsd.libraryelement.Connection) eventConnIter.next();
				String source = curConn.getSource();
				String dest = curConn.getDestination();
				String sinst = source.substring(0,source.indexOf("."));
				String sout = source.substring(source.indexOf(".")+1,source.length());
				String dinst = dest.substring(0,dest.indexOf("."));
				String din = dest.substring(dest.indexOf(".")+1,dest.length());
				//java.lang.System.out.println("from:" + sinst + "!" + sout);
				//java.lang.System.out.println("to:" + dinst + "!" + din);
				appFrag.addEventConnection(sinst, sout, dinst, din);
			}
		}
		if (xmlFBNetworkData.isSetDataConnections())
		{
			//java.lang.System.out.println("Data Connections:");
			for (Iterator dataConnIter = xmlFBNetworkData.getDataConnections().getConnection().iterator(); dataConnIter.hasNext();)
			{
				org.supremica.functionblocks.xsd.libraryelement.Connection curConn = (org.supremica.functionblocks.xsd.libraryelement.Connection) dataConnIter.next();
				String source = curConn.getSource();
				String dest = curConn.getDestination();
				String sinst = source.substring(0,source.indexOf("."));
				String sout = source.substring(source.indexOf(".")+1,source.length());
				String dinst = dest.substring(0,dest.indexOf("."));
				String din = dest.substring(dest.indexOf(".")+1,dest.length());
				//java.lang.System.out.println("from: " + sinst + "!" + sout);
				//java.lang.System.out.println("to: " + dinst + "!" + din);
				appFrag.addDataConnection(sinst, sout, dinst, din);
			}
		}
    }

    private void loadFBType(org.supremica.functionblocks.xsd.libraryelement.FBType xmlFBTypeData)
    {
		if (xmlFBTypeData.isSetBasicFB())
		{
			constructNewBasicFBType(xmlFBTypeData);
		}
    }


    private void constructNewBasicFBType(org.supremica.functionblocks.xsd.libraryelement.FBType xmlFBTypeData)
    {

		resource.addBasicFBType(xmlFBTypeData.getName());
		BasicFBType newBasicFBType = (BasicFBType) resource.getFBType(xmlFBTypeData.getName());

		// Build the interface
		// event inputs
		for (Iterator iter = xmlFBTypeData.getInterfaceList().getEventInputs().getEvent().iterator(); iter.hasNext();)
		{
			org.supremica.functionblocks.xsd.libraryelement.Event curEvent = (org.supremica.functionblocks.xsd.libraryelement.Event) iter.next();
			newBasicFBType.addVariable(curEvent.getName(), new BooleanVariable("EventInput",false));
			// data associations
			for (Iterator withIter = curEvent.getWith().iterator(); withIter.hasNext();)
			{
				org.supremica.functionblocks.xsd.libraryelement.With curWith = (org.supremica.functionblocks.xsd.libraryelement.With) withIter.next();
				newBasicFBType.addDataAssociation(curEvent.getName(), curWith.getVar());
			}
		}
		// event outputs
		for (Iterator iter = xmlFBTypeData.getInterfaceList().getEventOutputs().getEvent().iterator(); iter.hasNext();)
		{
			org.supremica.functionblocks.xsd.libraryelement.Event curEvent = (org.supremica.functionblocks.xsd.libraryelement.Event) iter.next();
			newBasicFBType.addVariable(curEvent.getName(), new BooleanVariable("EventOutput",false));
			// data associations
			for (Iterator withIter = curEvent.getWith().iterator(); withIter.hasNext();)
			{
				org.supremica.functionblocks.xsd.libraryelement.With curWith = (org.supremica.functionblocks.xsd.libraryelement.With) withIter.next();
				newBasicFBType.addDataAssociation(curEvent.getName(), curWith.getVar());
			}
		}
		// input data variables
		for (Iterator iter = xmlFBTypeData.getInterfaceList().getInputVars().getVarDeclaration().iterator(); iter.hasNext();)
		{
			org.supremica.functionblocks.xsd.libraryelement.VarDeclaration curVar = (org.supremica.functionblocks.xsd.libraryelement.VarDeclaration) iter.next();
			if (curVar.getType().toLowerCase().equals("int"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new IntegerVariable("DataInput",new Integer(curVar.getInitialValue()).intValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new IntegerVariable("DataInput",0));
				}
			}
			else if (curVar.getType().toLowerCase().equals("bool"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new BooleanVariable("DataInput",new Boolean(curVar.getInitialValue()).booleanValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new BooleanVariable("DataInput",false));
				}
			}
			else if (curVar.getType().toLowerCase().equals("real"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new DoubleVariable("DataInput",new Double(curVar.getInitialValue()).doubleValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new DoubleVariable("DataInput",0.0));
				}
			}
			else if (curVar.getType().toLowerCase().equals("string"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new StringVariable("DataInput", curVar.getInitialValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new StringVariable("DataInput",""));
				}
			}
		}
		// output data variables
		for (Iterator iter = xmlFBTypeData.getInterfaceList().getOutputVars().getVarDeclaration().iterator(); iter.hasNext();)
		{
			org.supremica.functionblocks.xsd.libraryelement.VarDeclaration curVar = (org.supremica.functionblocks.xsd.libraryelement.VarDeclaration) iter.next();
			if (curVar.getType().toLowerCase().equals("int"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new IntegerVariable("DataOutput",new Integer(curVar.getInitialValue()).intValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new IntegerVariable("DataOutput",0));
				}
			}
			else if (curVar.getType().toLowerCase().equals("bool"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new BooleanVariable("DataOutput",new Boolean(curVar.getInitialValue()).booleanValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new BooleanVariable("DataOutput",false));
				}
			}
			else if (curVar.getType().toLowerCase().equals("real"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new DoubleVariable("DataOutput",new Double(curVar.getInitialValue()).doubleValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new DoubleVariable("DataOutput",0.0));
				}
			}
			else if (curVar.getType().toLowerCase().equals("string"))
			{
				if (curVar.isSetInitialValue())
				{
					newBasicFBType.addVariable(curVar.getName(), new StringVariable("DataOutput", curVar.getInitialValue()));
				}
				else
				{
					newBasicFBType.addVariable(curVar.getName(), new StringVariable("DataOutput",""));
				}
			}
		}
		// End Build the interface

		// Build internal variables
		if (xmlFBTypeData.getBasicFB().isSetInternalVars())
		{
			for (Iterator iter = xmlFBTypeData.getBasicFB().getInternalVars().getVarDeclaration().iterator(); iter.hasNext();)
			{
				org.supremica.functionblocks.xsd.libraryelement.VarDeclaration curVar = (org.supremica.functionblocks.xsd.libraryelement.VarDeclaration) iter.next();
				if (curVar.getType().toLowerCase().equals("int"))
				{
					if (curVar.isSetInitialValue())
					{
						newBasicFBType.addVariable(curVar.getName(), new IntegerVariable("Local",new Integer(curVar.getInitialValue()).intValue()));
					}
					else
					{
						newBasicFBType.addVariable(curVar.getName(), new IntegerVariable("Local",0));
					}
				}
				else if (curVar.getType().toLowerCase().equals("bool"))
				{
					if (curVar.isSetInitialValue())
					{
						newBasicFBType.addVariable(curVar.getName(), new BooleanVariable("Local",new Boolean(curVar.getInitialValue()).booleanValue()));
					}
					else
					{
						newBasicFBType.addVariable(curVar.getName(), new BooleanVariable("Local",false));
					}
				}
				else if (curVar.getType().toLowerCase().equals("real"))
				{
					if (curVar.isSetInitialValue())
					{
						newBasicFBType.addVariable(curVar.getName(), new DoubleVariable("Local",new Double(curVar.getInitialValue()).doubleValue()));
					}
					else
					{
						newBasicFBType.addVariable(curVar.getName(), new DoubleVariable("Local",0.0));
					}
				}
				else if (curVar.getType().toLowerCase().equals("string"))
				{
					if (curVar.isSetInitialValue())
					{
						newBasicFBType.addVariable(curVar.getName(), new StringVariable("Local", curVar.getInitialValue()));
					}
					else
					{
						newBasicFBType.addVariable(curVar.getName(), new StringVariable("Local",""));
					}
				}
			}
		}
		// End Build internal variables


		// Build ECC
		// Build States
		int curStateNum = 0;
		for (Iterator iter = xmlFBTypeData.getBasicFB().getECC().getECState().iterator(); iter.hasNext();)
		{
			org.supremica.functionblocks.xsd.libraryelement.ECState curState = (org.supremica.functionblocks.xsd.libraryelement.ECState) iter.next();
			if (curStateNum == 0)
			{
				newBasicFBType.getECC().addInitialState(curState.getName());
				curStateNum++;
			}
			else
			{
				newBasicFBType.getECC().addState(curState.getName());
			}
			for(Iterator actionIter = curState.getECAction().iterator(); actionIter.hasNext();)
			{
				org.supremica.functionblocks.xsd.libraryelement.ECAction curAction = (org.supremica.functionblocks.xsd.libraryelement.ECAction) actionIter.next();
				newBasicFBType.getECC().getState(curState.getName()).addAction(curAction.getAlgorithm(), curAction.getOutput());
			}
		}
		// Build transitions
		for (Iterator iter = xmlFBTypeData.getBasicFB().getECC().getECTransition().iterator(); iter.hasNext();)
		{
			org.supremica.functionblocks.xsd.libraryelement.ECTransition curTrans = (org.supremica.functionblocks.xsd.libraryelement.ECTransition) iter.next();
			if (curTrans.getCondition().equals("1"))
			{
				newBasicFBType.getECC().addTransition(curTrans.getSource(),curTrans.getDestination(),"TRUE");
			}
			else
			{
				newBasicFBType.getECC().addTransition(curTrans.getSource(),curTrans.getDestination(), curTrans.getCondition());
			}
		}
		// End Build ECC


		//Build Algorithms
		for (Iterator iter = xmlFBTypeData.getBasicFB().getAlgorithm().iterator(); iter.hasNext();)
		{
			org.supremica.functionblocks.xsd.libraryelement.Algorithm curAlg = (org.supremica.functionblocks.xsd.libraryelement.Algorithm) iter.next();
			if (curAlg.isSetOther())
			{
				if (curAlg.getOther().getLanguage().toLowerCase().equals("java"))
				{
					newBasicFBType.addAlgorithm(new JavaTextAlgorithm(curAlg.getName(),curAlg.getOther().getText()));
				}
			}
		}
		// End Build Algorithms

    }


}
