package org.supremica.external.jgrafchart.toSMV;

import org.jdom.*;
import org.jdom.input.*;
import java.io.*;
import java.util.*;
import org.supremica.external.jgrafchart.toSMV.SFCDataStruct.*;
import org.supremica.external.jgrafchart.toSMV.SFCVerification.SFCToSMV;


public class ParseXML
{
	public static void main(String arg[])
	{
		try {
			SAXBuilder builder = new SAXBuilder();
			String xmlFileName = 	arg[0];
			Document SFCDoc = builder.build(new File(xmlFileName));
			SFC sfc = new SFC(SFCDoc);
			String smvFileName = xmlFileName.replaceFirst(".xml",".smv");
			SFCToSMV smvGenerator = new SFCToSMV(sfc,smvFileName);

			smvGenerator.writeCode();


			List allSteps = sfc.getAllSteps();
			List allTransitions = sfc.getAllTransitions();
			List allVariables = sfc.getAllVariables();

			//Tester to be deleted later
			sfc.tester1();

			System.out.println("The total steps are "+allSteps.size());
			System.out.println("The total transitions are "+allTransitions.size());
			System.out.println("The total variables are "+allVariables.size());

			Iterator it = allSteps.iterator();
			while(it.hasNext())
			{
				SFCStep aStep = (SFCStep) it.next();
				System.out.println("Step Id is "+aStep.getId());

				/*
				List actions = aStep.getActionsList();
				if(actions != null)
				{
					Iterator actionIt = actions.iterator();
					while(actionIt.hasNext())
					{
						SFCAction ac = (SFCAction) actionIt.next();
						System.out.println("Associated Action is "+ac.toString());
					}
				}
				*/

				Iterator itTransOG = aStep.outgoingTransIterator();
				while(itTransOG.hasNext())
				{
					SFCTransition outgoingTrans = (SFCTransition) itTransOG.next();
					//System.out.println("The object is "+ (obj.getClass()).getName());
					System.out.println("Outgoing Transitions are "+outgoingTrans.getId()+" for Step id :"+aStep.getId());
				}

				Iterator itTransIC = aStep.incomingTransIterator();
				while(itTransIC.hasNext())
				{
					SFCTransition incomingTrans = (SFCTransition) itTransIC.next();
					System.out.println("Incoming Transitions are "+incomingTrans.getId()+" for Step id :"+aStep.getId());
				}

			}



			}
			catch(JDOMException e)
			{
				e.printStackTrace();
			}
/* This exception cannot be thrown
	catch(IOException ioe)
			{
					System.err.println("File Not found...check name and path again");
					ioe.printStackTrace();
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}
*/

	}

	public static void listChildren(Element current, int depth)
	{

    	printSpaces(depth);
    	System.out.println(current.getName());
    	List children = current.getChildren();
    	Iterator iterator = children.iterator();
    	while (iterator.hasNext())
    	{
      		Element child = (Element) iterator.next();
      		listChildren(child, depth+1);
    	}

  	}

  	private static void printSpaces(int n)
  	{

    	for (int i = 0; i < n; i++)
    	{
      		System.out.print(' ');
	    }

	}

}
/*
public class ParseXML
{
	public static void main(String arg[])
	{
		List allContents = null;
		//List allLinks = null;

		try {
			SAXBuilder builder = new SAXBuilder();
			//ElementFilter linkFilter = new ElementFilter("GCLink");

			Document SFCDoc = builder.build(new File(arg[0]));
			allContents = SFCDoc.getContent();
			//allLinks = SFCDoc.getContent(linkFilter);

			System.out.println(allContents.size());
			//System.out.println("Total Links :" + allLinks.size());

			//Iterator it = allContents.iterator();
			//
			while(it.hasNext())
			{
				Element e = (Element) it.next();
				System.out.println(e.getName());

			}
			//
			Element e = SFCDoc.getRootElement();
			List initialStep = e.getChildren("GCInitialStep");
			Iterator it = initialStep.iterator();
			String id = ((Element)it.next()).getAttributeValue("id");
			System.out.println(id);

			List allLinks = e.getChildren("GCLink");

			List outgoingLinks = findOutgoingLinks(allLinks,id);
			if (outgoingLinks != null)
			{
				System.out.println(outgoingLinks.size());
			}

			System.out.println("The total links are "+allLinks.size());

			//listChildren(e,0);


			//System.out.println(e.getName());

			}
			catch(JDOMException e)
			{
				e.printStackTrace();
			}
			catch(IOException ioe)
			{
					ioe.printStackTrace();
			}
			catch(NullPointerException e)
			{
				e.printStackTrace();
			}

	}

	public static void listChildren(Element current, int depth)
	{

    	printSpaces(depth);
    	System.out.println(current.getName());
    	List children = current.getChildren();
    	Iterator iterator = children.iterator();
    	while (iterator.hasNext())
    	{
      		Element child = (Element) iterator.next();
      		listChildren(child, depth+1);
    	}

  	}

  	private static void printSpaces(int n)
  	{

    	for (int i = 0; i < n; i++)
    	{
      		System.out.print(' ');
	    }

	}

	private static List findOutgoingLinks(List allLinks,String id)
	{
		Vector outgoingLinks = new Vector();
		Iterator it = allLinks.iterator();

		while (it.hasNext())
		{
				Element aLink = (Element) it.next();

				if (id.equals(aLink.getAttributeValue("fromObject")))
				{
					outgoingLinks.add(aLink);

				}
		}
		 return outgoingLinks ;

	}

	private static List findIncomingLinks(List allLinks, String id)
	{
		Vector incomingLinks = new Vector();
		Iterator it = allLinks.iterator();

		while (it.hasNext())
		{
				Element aLink = (Element) it.next();
				if (id.equals(aLink.getAttributeValue("toObject")))
				{
					incomingLinks.add(aLink);

				}
		}
		 return incomingLinks ;

	}

}
*/
