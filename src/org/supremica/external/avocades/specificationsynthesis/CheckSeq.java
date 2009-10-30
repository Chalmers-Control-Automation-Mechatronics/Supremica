package org.supremica.external.avocades.specificationsynthesis;


import java.util.*;
import org.jdom.*;

public class CheckSeq {

	Vector<String> predecessor_vector;
	ConverterSTEPtoPA steptopa;

	public CheckSeq() {

		predecessor_vector = new Vector<String>();


	}

	public void createPrecedenceGroups(Element root, String poo_id, ConverterSTEPtoPA steppa) {

		steptopa = steppa;

		// Check which POOs that refers to the specific poo.
		List<?> POOs = root.getChildren("Process_operation_occurrence");
		for(Iterator<?> listIt = POOs.iterator(); listIt.hasNext(); )
		{
			Element poo_element = (Element) listIt.next();
			String id_poo_element = poo_element.getAttributeValue("id");

			// Create a List of the each poo´s poor
			List<?> POORs = poo_element.getChildren("Process_operation_occurrence_relationship");
			for(Iterator<?> listPOOR = POORs.iterator(); listPOOR.hasNext(); )
			{
				Element poor_element = (Element) listPOOR.next();
				Element relType = poor_element.getChild("Relation_type");
				String  type = relType.getText();

				// Checks that it is a sequence relation that is found.
				if (type.equals("sequence")) {

					Element element_related = poor_element.getChild("Related");
					String  related = element_related.getText();

					// Check if it is the specific poo that is related to.
					if (poo_id.equals(related)) {

						if (! checkIfInSeqGroup(poo_id, related, root))
						{
							predecessor_vector.add(id_poo_element);
						}

					}
				}
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector<String> getSeqPOOs() {
		return predecessor_vector;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public boolean checkIfInSeqGroup(String id, String related, Element root)
	{

		boolean InSeqGroup = false;
		Vector<String> group = new Vector<String>();

		steptopa.getPOOelement(id);
		steptopa.getPOOelement(related);

		// Check if the two poo is in the same sequencegroup. If they are they do not result in a restriction expression.
		// 1. Find all PODs that is a seguence group pod.
		List<?> PODs = root.getChildren("Process_operation_definition");
		for(Iterator<?> listIt = PODs.iterator(); listIt.hasNext(); )
		{
			Element pod_element = (Element) listIt.next();
			String pod_name = pod_element.getAttributeValue("id");

			Element process_type = pod_element.getChild("Process_type");
			String process_type_string = process_type.getText();

			Element pod_id = pod_element.getChild("Id");
			String pod_id_string = pod_id.getText();

			if (pod_id_string.equals("sequence") && process_type_string.equals("Grouping")) {

				// 2. Find all POOs that refers to the grouping PODs
				List<?> POOs = root.getChildren("Process_operation_occurrence");
				for(Iterator<?> listPOO = POOs.iterator(); listPOO.hasNext(); )
				{
					Element poo_element = (Element) listPOO.next();

					Element operation_definition = poo_element.getChild("Operation_definition");
					String operation_definition_string = operation_definition.getText();

					if (pod_name.equals(operation_definition_string)) {

						// 3. Using the POO all POOs that are refered to via decomposition is added to the vector group.
						List<?> decomposed_poos = poo_element.getChildren("Process_operation_occurrence_relationship");
						for(Iterator<?> listDecomposedPOO = decomposed_poos.iterator(); listDecomposedPOO.hasNext(); )
						{
							Element relation_member_element = (Element) listDecomposedPOO.next();
							Element relType = relation_member_element.getChild("Relation_type");
							String  type = relType.getText();

							if (type.equals("decomposition")) {
								Element group_member = relation_member_element.getChild("Related");
								String group_member_text = group_member.getText();
								steptopa.getPOOelement(group_member_text);
								group.add(group_member_text);
							}
						}

						// 4. If the two POO that was to be checked is in fact in vector group then no restriction is added.
						if (group.contains(id) && group.contains(related)) {
							InSeqGroup = true;
						}
					}
				}
			}
		}
		return InSeqGroup;
	}
}