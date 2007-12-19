package org.supremica.external.specificationSynthesis.algorithms;


import java.util.*;
import org.jdom.*;

public class ConverterSTEPtoPA {

	Document PAdoc;

	Element root;
	Element and;
	Element OR;

	public ConverterSTEPtoPA() {
		System.out.println("steptoPA");

	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void convertSTEPtoPA(Document doc) {

		// Get STEP root element.
		root = doc.getRootElement();
		String stepID = root.getAttributeValue("id");
		String versionID = root.getAttributeValue("version_id");

		// Create and insert root (PA) in new Document
		Element PA = new Element("PA");
		PA.setAttribute("name", stepID);
		PA.setAttribute("id", versionID);

		// Create new Document
		PAdoc = new Document(PA);

		// Create a List of the involved POOs
		List pooList = root.getChildren("Process_operation_occurrence");
		for(Iterator listPOO = pooList.iterator(); listPOO.hasNext(); )
		{
			Element poo_element = (Element) listPOO.next();
			if (! checkDecomp(poo_element)) {
				CreateGroups(poo_element, PA);
			}
		}

		// Creates and places operation descriptions into xml
		PlaceOperationDescriptions place_operation_descriptions = new PlaceOperationDescriptions();
		place_operation_descriptions.placeOperationDescription(root, PA, this);

		// Creates and places interlockingdescriptions into xml
		PlaceInterlockings place_interlockings = new PlaceInterlockings();
		place_interlockings.placeInterlocking(root, PA, this);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Document getDoc() {
		System.out.println("PAdoc");
		return PAdoc;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// getPOOelement ////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Element getPOOelement(String e) {

		boolean notfound = true;
		String poo_id = e;
		Element poo_element = new Element("ejhittad");

		java.util.List POOs = root.getChildren("Process_operation_occurrence");
		Object[] POOs_array = POOs.toArray();
		int nr_POOs = POOs_array.length;

		int i = 0;
		while (notfound) {

			poo_element = (Element)POOs_array[i];
			String id = poo_element.getAttributeValue("id");

			if (id.equals(poo_id)) {
				notfound = false;
			}
			i=i+1;
		}
		return (poo_element);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// getPODelement ////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Element getPODelement(String e) {

		boolean notfound = true;
		String pod_id = e;
		Element pod_element = new Element("ejhittad");

		java.util.List PODs = root.getChildren("Process_operation_definition");
		Object[] PODs_array = PODs.toArray();
		int nr_PODs = PODs_array.length;

		int i = 0;
		while (notfound) {

			pod_element = (Element)PODs_array[i];
			String id = pod_element.getAttributeValue("id");

			if (id.equals(pod_id)) {
				notfound = false;
			}
			i=i+1;
		}
		return (pod_element);
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// CreateGroups /////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public void CreateGroups(Element poo, Element PA) {

		Vector group = new Vector();

		// Check if it a decomp_group. If it is create and place an element in the xml with the relationtype as element id.
		if (checkGrouping(poo)) {
			Element relation = new Element(getPodID(poo));
			PA.addContent(relation);

			String group_poo_id = poo.getAttributeValue("id");

			// Checks and places restrictions (precedence POOs) on the found group.
			CheckSeq sequence = new CheckSeq();
			sequence.createPrecedenceGroups(root, group_poo_id, this);
			Vector restrictions_on_group = sequence.getSeqPOOs();

			// If there are restrictions these are are placed.
			if (! restrictions_on_group.isEmpty()) {

				Element restriction = new Element("Restriction");
				relation.addContent(restriction);

				// If more than one restriction an "And" element is added.
				if (restrictions_on_group.size()>1) {
					and = new Element("And");
					restriction.addContent(and);
					place_restrictions(root, restrictions_on_group, and);
				} else {
					place_restrictions(root, restrictions_on_group, restriction);
				}
			}

			// Hämta alla process relationer element för en viss POO (Dessa kan vara decomp, alt, par, seq, arb)
			List pooList = poo.getChildren("Process_operation_occurrence_relationship");
			for(Iterator listPOO = pooList.iterator(); listPOO.hasNext(); )
			{
				Element relation_member_element = (Element) listPOO.next();
				Element relType = relation_member_element.getChild("Relation_type");
				String  type = relType.getText();

				if (type.equals("decomposition")) {
					Element group_member = relation_member_element.getChild("Related");
					String group_member_text = group_member.getText();
					Element group_member_element = getPOOelement(group_member_text);
					group.add(group_member_element);
				}
			}

			// Kolla hur många de är. Gå igenom all dessa och kolla hur dessa ser ut (Om dessa är decomp eller inte)
			int number_in_group;
			for (number_in_group=0; number_in_group < group.size(); number_in_group=number_in_group+1) {

				Element group_member_element = (Element) group.elementAt(number_in_group);
				String group_member_id = group_member_element.getAttributeValue("id");

				// If it is an decomposed (Grouping) POO that is found do CreateGroups again else add the single poo.
				if (checkGrouping(group_member_element)) {
					CreateGroups(group_member_element, relation);
				} else {
					Element process = new Element("Process");
					process.setAttribute("Id", group_member_id);
					relation.addContent(process);

					// Check for predecessors..
					CheckSeq seq = new CheckSeq();
					seq.createPrecedenceGroups(root, group_member_id, this);
					Vector restrictions = seq.getSeqPOOs();

					boolean inlagd_and_element = false;

					// Check if there are any restrictions.
					if (! restrictions.isEmpty()) {

						Element restriction = new Element("Restriction");
						process.addContent(restriction);

						// Place an "And" element if there are more than one restriction.
						if (restrictions.size()>1) {
							and = new Element("And");
							restriction.addContent(and);
							place_restrictions(root, restrictions, and);
						} else {
							place_restrictions(root, restrictions, restriction);
						}
					}
				}
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// checkGrouping ////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkGrouping(Element group_member_element) {

		boolean grouping = false;

		Element group_member_element_pod = group_member_element.getChild("Operation_definition");
		String pod = group_member_element_pod.getText();

		Element pod_element = getPODelement(pod);
		Element pod_element_process_type = pod_element.getChild("Process_type");
		String type = pod_element_process_type.getText();

		if (type.equals("Grouping")) {
			grouping = true;
		}
		return grouping;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// getPodID /////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getPodID(Element group_member_element) {

		Element group_member_element_pod = group_member_element.getChild("Operation_definition");
		String pod = group_member_element_pod.getText();

		Element pod_element = getPODelement(pod);
		Element pod_element_process_type = pod_element.getChild("Process_type");
		String type = pod_element_process_type.getText();

		Element pod_element_id = pod_element.getChild("Id");
		String id = pod_element_id.getText();

		return id;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// checkDecomp //////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkDecomp(Element poo) {

		boolean decomp = false;

		List pooList = root.getChildren("Process_operation_occurrence");
		for(Iterator listPOO = pooList.iterator(); listPOO.hasNext(); )
		{
			Element poo_element = (Element) listPOO.next();;
			List poorList = poo_element.getChildren("Process_operation_occurrence_relationship");
			for(Iterator listPOOR = poorList.iterator(); listPOOR.hasNext(); )
			{
				Element poor_element = (Element) listPOOR.next();
				Element relType = poor_element.getChild("Relation_type");
				String  type = relType.getText();

				if (type.equals("decomposition")) {

					Element related = poor_element.getChild("Related");
					String related_id = related.getText();
					Element related_element = getPOOelement(related_id);

					if (related_id.equals(poo.getAttributeValue("id"))) {
						decomp = true;
					}
				}
			}
		}
	return decomp;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////// place_restrictions ///////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void place_restrictions(Element root, Vector restrictions, Element platsen) {

		// Check if there are alternative restrictions.
		int k;
		for (k=0; k<restrictions.size(); k=k+1) {
			String poo = (String) restrictions.elementAt(k);
			Element element_POO = getPOOelement(poo);

			// Check if the found poo is alternative with other poos
			CheckAlt checkalt = new CheckAlt();
			checkalt.addRestrictions(element_POO, root);
			Vector alt_poos = checkalt.getAltPOOs();

			// If there were alternative restrictions
			if (! alt_poos.isEmpty()) {

				OR = new Element("Or");
				platsen.addContent(OR);

				int o;
				for (o=0; o<alt_poos.size(); o=o+1) {

					String poo_alt_id = (String) alt_poos.elementAt(o);
					Element restriction_POO = new Element("Process");
					restriction_POO.setAttribute("id", poo_alt_id);
					restriction_POO.setAttribute("Neg", "false");
					restriction_POO.setAttribute("Start", "false");
					restriction_POO.setAttribute("Stop", "true");
					OR.addContent(restriction_POO);
				}
			}
			if (alt_poos.isEmpty()) {

				Element restriction_POO = new Element("Process");
				restriction_POO.setAttribute("id", poo);
				restriction_POO.setAttribute("Neg", "false");
				restriction_POO.setAttribute("Start", "false");
				restriction_POO.setAttribute("Stop", "true");

				platsen.addContent(restriction_POO);

			}
		}
	}
}