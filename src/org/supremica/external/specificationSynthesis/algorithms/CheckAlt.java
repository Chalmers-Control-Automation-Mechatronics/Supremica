package org.supremica.external.specificationSynthesis.algorithms;

import java.util.*;
import org.jdom.*;

public class CheckAlt{

	Vector restriction_vector;

	public CheckAlt() {
		restriction_vector = new Vector();
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void addRestrictions(Element poo_element, Element root) {

		Element operation_definition_element = poo_element.getChild("Operation_definition");
		String operation_definition = operation_definition_element.getText();

		if (operation_definition.equals("pod_sub")) {

			List POORs = poo_element.getChildren("Process_operation_occurrence_relationship");
			for(Iterator listIt = POORs.iterator(); listIt.hasNext(); )
			{
				Element poor_element = (Element) listIt.next();
				Element relation_type_element = poor_element.getChild("Relation_type");
				String  relation_type = relation_type_element.getText();

				// Checks if relation th another poo is decomposition
				if (relation_type.equals("decomposition")) {

					Element related_element = poor_element.getChild("Related");
					String  related = related_element.getText();

					// related poo via decomposition.Before adding this control that it iself is not decomposed.
					Element next_poo_element = getPOOelement(related, root);

					Element related_operation_definition_element = next_poo_element.getChild("Operation_definition");
					String related_operation_definition = related_operation_definition_element.getText();

					Element related_pod = getPODelement(related_operation_definition, root);
					String definition = related_pod.getAttributeValue("id");

					// Checks if the found next_poo_element is decomposed. If it is not it is added.
					if (definition.equals("pod_sub")) {
						addRestrictions(next_poo_element, root);
					} else {
						restriction_vector.add(related);
					}
				}
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Element getPODelement(String e, Element root) {

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
		return pod_element;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Element getPOOelement(String e, Element root) {

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
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector getAltPOOs() {
		return restriction_vector;
	}
}
