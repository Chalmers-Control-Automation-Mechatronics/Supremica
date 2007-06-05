package org.supremica.external.processAlgebraPetriNet.algorithms;


import java.util.*;
import org.jdom.*;

public class PlaceInterlockings {

	public PlaceInterlockings() {

	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeInterlocking(Element STEProot, Element PAroot, ConverterSTEPtoPA steppa) {

		List POOs = STEProot.getChildren("Process_operation_occurrence");
		for(Iterator listIt = POOs.iterator(); listIt.hasNext(); )
		{
			Element poo_element = (Element) listIt.next();
			String poo_id = poo_element.getAttributeValue("id");

			Element definition = poo_element.getChild("Operation_definition");
			String pod_id = definition.getText();

			Element pod_element = steppa.getPODelement(pod_id);
			Element pod_name = pod_element.getChild("Name");
			String pod = pod_name.getText();

			// Get a list of operation restrictions on this operation.
			List operation_interlocking = getOperationVector(STEProot, poo_element);
			// Get a list of resourcestate restrictions on this operation.
			List resource_Vector = getResourceVector(STEProot, poo_element);

			Element or = new Element("Or");

			// Check if an operation have an interlocking.
			if (operation_interlocking != null | resource_Vector != null) {
				Element process_interlocking = new Element("Process_interlocking");
				process_interlocking.setAttribute("id", poo_id);
				PAroot.addContent(process_interlocking);

				Element event = new Element("Event");
				event.setAttribute("id", pod);
				process_interlocking.addContent(event);

				Element restriction = new Element("Restriction");
				event.addContent(restriction);

				or = new Element("Or");
				restriction.addContent(or);
			}

			// Place interlockinggroups under element OR. Both operation restrictions and resourcestate restrictions.
			if (operation_interlocking != null) {
				insertInterlocking(or, operation_interlocking, STEProot);
			}
			if (resource_Vector != null) {
				insertInterlocking(or, resource_Vector, STEProot);
			}
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List getOperationVector(Element STEProot, Element poo_element) {

		String poo_id = poo_element.getAttributeValue("id");

		List merged_operation_conditions = null;
		List operation_conditions = null;
		boolean first = true;

		// Get a list of all poor elements of poo.
		List POORs = poo_element.getChildren("Process_operation_occurrence_relationship");
		for(Iterator listIt = POORs.iterator(); listIt.hasNext(); )
		{
			Element poor_element = (Element) listIt.next();
			String poor_elemt_id = poor_element.getAttributeValue("id");

			Element relation_type = poor_element.getChild("Relation_type");
			String relation_type_text = relation_type.getText();

			// Check if the relationtype of the poor is exlusiveness. This means that these operations can´t execute at the same time.
			if (relation_type_text.equals("exclusiveness")) {

				Element related_element = poor_element.getChild("Related");
				String related = related_element.getText();

				// Checks if it is an interlocking.
				Element condition_assigment = checkInterlocking(STEProot, poor_elemt_id);
				if (condition_assigment != null) {

					Element described_condition_element = condition_assigment.getChild("Described_condition");
					// The described conditions may include several requirements seperated by " ".
					String described_condition = described_condition_element.getText();

					Vector operation_interlockings = new Vector();
					// Convert the string to a vector with single conditions and adds this to a vector.
					operation_interlockings = convertToVector(described_condition);
					// Add the operation that was releted to.
					operation_interlockings.addElement(related);

					if (first) {
						operation_conditions = new ArrayList();
						first = false;
					}
					operation_conditions.add(operation_interlockings);
				}
			}
		}
		if (operation_conditions != null) {
			// Several operationconditions can involve the same product and needs to be merged.
			merged_operation_conditions = mergeVectors(operation_conditions);
		}
		return merged_operation_conditions;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public List getResourceVector(Element STEProot, Element poo_element) {

		String poo_id = poo_element.getAttributeValue("id");

		List resource_condition = null;
		boolean first = true;

		// Get all condition assigments.
		List CAs = STEProot.getChildren("Condition_assignment");
		for(Iterator listIt = CAs.iterator(); listIt.hasNext(); )
		{
			Element condition_assiment = (Element) listIt.next();

			Element assigned_to_element = condition_assiment.getChild("Assigned_to");
			String assigned_to = assigned_to_element.getText();

			Element name_element = condition_assiment.getChild("Name");
			String name = name_element.getText();

			// Check that the condition assigment refers to the specific operation and that it is an interlock condition.
			if (assigned_to.equals(poo_id) && name.equals("interlock")) {

				// Get the condition.
				Element described_condition_element = condition_assiment.getChild("Described_condition");
				String described_condition = described_condition_element.getText();

				if (first) {
					resource_condition = new ArrayList();
					first = false;
				}

				// Converts the condition to a vector and adds it to vector.
				resource_condition.add(convertToVector(described_condition));

			}
		}
		return resource_condition;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector convertToVector(String described_condition_text) {

		Vector operation_interlockings = new Vector();

		int nr = described_condition_text.indexOf(" ");
		int start_nr = 0;

		while (nr != -1) { // All conditions are added here except the last one.

			String state = described_condition_text.substring(start_nr, nr);
			operation_interlockings.addElement(state);

			start_nr = nr+1;
			nr = described_condition_text.indexOf(" ", nr+1);
		}
		if (start_nr != 0) { // The last is added here

			String state = described_condition_text.substring(start_nr, described_condition_text.length());
			operation_interlockings.addElement(state);

		} else { // A single condition is added here;
			operation_interlockings.addElement(described_condition_text);
		}

		return operation_interlockings;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public Element checkInterlocking(Element STEProot, String poor_element_id) {

		Element condition_assigment_element = null;

		List condition_assigments = STEProot.getChildren("Condition_assignment");
		for(Iterator listIt = condition_assigments.iterator(); listIt.hasNext(); )
		{
			Element condition_assigment = (Element) listIt.next();

			Element assigned_to_element = condition_assigment.getChild("Assigned_to");
			String assigned_to = assigned_to_element.getText();

			if (assigned_to.equals(poor_element_id)) {
				condition_assigment_element = condition_assigment;
			}
		}
	return condition_assigment_element;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


	public List mergeVectors(List operation_interlocking) {

		Vector merged_vector = new Vector();

		int vector_nr;
		for (vector_nr=0; vector_nr < operation_interlocking.size(); vector_nr=vector_nr+1) {

			Vector condition = (Vector) operation_interlocking.get(vector_nr);
			String product = (String) condition.get(0); // Product type.
			String product_part_of_name = product.substring(0, 4);

			// Checks if the condition include a product type assigned to it. The condition vectors that have the same product type are to be merged into a signle condition vector.
			if (product_part_of_name.equals("poio")) {

				int nr;
				for (nr=vector_nr+1; nr < operation_interlocking.size(); nr=nr+1) {

					Vector condition_next = (Vector) operation_interlocking.get(nr);
					String product_next = (String) condition_next.get(0);

					// Checks if this condition have the same product.
					if (product.equals(product_next)) {

						// Places new condition into one of the vectors and removes the other one.
						merged_vector = merge(condition, condition_next);
						operation_interlocking.set(vector_nr, merged_vector);
						operation_interlocking.remove(nr);
					}
				}
			}
		}
		return operation_interlocking;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector merge(Vector vectorOne, Vector vectorTwo) {

		int nr;
		for (nr=0; nr < vectorTwo.size(); nr=nr+1) {
			String condition = (String) vectorTwo.get(nr);
			if (! vectorOne.contains(condition)) {
				vectorOne.addElement(condition);
			}
		}
		return vectorOne;
	}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void insertInterlocking(Element or, List condition_list, Element STEProot) {

		int nr;
		for (nr=0; nr < condition_list.size(); nr=nr+1) {

			Vector condition_vector = (Vector) condition_list.get(nr);

			if (condition_vector.size() > 1) {

				Element and = new Element("And");
				or.addContent(and);

				int condition_nr;
				for (condition_nr=0; condition_nr < condition_vector.size(); condition_nr=condition_nr+1) {

					String condition = (String) condition_vector.get(condition_nr);

					// Find condition
					String resource = get_resource(condition, STEProot);

					// Find resource
					String resource_state = get_state(condition, STEProot);

					Element state_element = new Element("State");

					if (resource != null) {
						state_element.setAttribute("name", resource);
					} else {
						state_element.setAttribute("name", condition);
					}
					if (resource_state != null) {
						state_element.setAttribute("id", resource_state);
					} else {
						state_element.setAttribute("id", condition);
					}


					and.addContent(state_element);
				}
			} else {

				String condition = (String) condition_vector.get(0);

					// Find condition
					String resource = get_resource(condition, STEProot);

					// Find resource
					String resource_state = get_state(condition, STEProot);

				Element state_element = new Element("State");
					if (resource != null) {
						state_element.setAttribute("name", resource);
					} else {
						state_element.setAttribute("name", condition);
					}
					if (resource_state != null) {
						state_element.setAttribute("id", resource_state);
					} else {
						state_element.setAttribute("id", condition);
					}
				or.addContent(state_element);

			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String get_resource(String condition, Element STEProot) {

		String id_resource = null;
		String assigned_to = null;

		List stateAssignmentList = STEProot.getChildren("State_assignment");
		for(Iterator listSA = stateAssignmentList.iterator(); listSA.hasNext(); )
		{
			Element sa_element = (Element) listSA.next();
			String sa_id = sa_element.getAttributeValue("id");

			if (condition.equals(sa_id)) {
				Element assigned_to_element = sa_element.getChild("Assigned_to");
				assigned_to = assigned_to_element.getText();
			}
		}

		List itemList = STEProot.getChildren("Item");
		for(Iterator listItem = itemList.iterator(); listItem.hasNext(); )
		{
			Element item_element = (Element) listItem.next();
			Element id = item_element.getChild("Id");
			String resurs_id = id.getText();

			Element iv_element = item_element.getChild("Item_version");
			Element ddid_element = iv_element.getChild("Design_discipline_item_definition");
			String ddid_id = ddid_element.getAttributeValue("id");

			if (ddid_id.equals(assigned_to)) {

				id_resource = resurs_id;
			}
		}
	return id_resource;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String get_state(String condition, Element STEProot) {

		String state_namn = null;
		String describedState = null;

		List stateAssignmentList = STEProot.getChildren("State_assignment");
		for(Iterator listSA = stateAssignmentList.iterator(); listSA.hasNext(); )
		{
			Element sa_element = (Element) listSA.next();
			String sa_id = sa_element.getAttributeValue("id");

			if (condition.equals(sa_id)) {
				Element Described_state_element = sa_element.getChild("Described_state");
				describedState = Described_state_element.getText();
			}
		}

		List stateList = STEProot.getChildren("State");
		for(Iterator listState = stateList.iterator(); listState.hasNext(); )
		{
			Element state_element = (Element) listState.next();

			if (state_element != null && describedState != null) {

				String state_id = state_element.getAttributeValue("id");

				if (describedState.equals(state_id)) {

					Element name_element = state_element.getChild("Name");
					String name = name_element.getText();

					state_namn = name;
				}
			}
		}
	return state_namn;
	}


}

/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
