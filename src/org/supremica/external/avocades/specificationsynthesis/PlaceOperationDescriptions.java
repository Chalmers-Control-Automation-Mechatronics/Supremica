package org.supremica.external.avocades.specificationsynthesis;


import java.util.*;
import org.jdom.*;

public class PlaceOperationDescriptions {

Vector<String> inlagda_operationer = new Vector<String>();
Vector<?> senast_condition = new Vector<Object>();

ConverterSTEPtoPA steptopa;

	public PlaceOperationDescriptions() {


	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void placeOperationDescription(Element STEProot, Element PAroot, ConverterSTEPtoPA steppa) {

		String type = null;
		steptopa = steppa;

		// Create a List of the involved POOs
		List<?> POOs = STEProot.getChildren("Process_operation_occurrence");
		for(Iterator<?> listPOO = POOs.iterator(); listPOO.hasNext(); )
		{
			Element poo_element = (Element) listPOO.next();
			// Check is this POO belongs to a decompgroup and not an grouping.
			if (checkDecompGrouping(poo_element, "Operation_decomp")) {

				// Place POO id in the xml as a new entity.
				String poo_id = poo_element.getAttributeValue("id");

				// Checks if this POO is already placed
				if (! inlagda_operationer.contains(poo_id)) {

					Element poo = new Element("Operation");
					poo.setAttribute("id", poo_id);
					PAroot.addContent(poo);

					// Places this in vector "inlagda_operationer".
					inlagda_operationer.add(poo_id);

					// Create an operation by adding events and restrictions in the form of states.
					CreateOperation(poo_element, PAroot, poo, STEProot, type);
				}
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean checkDecompGrouping(Element group_member_element, String grouping_type) {

		boolean grouping = false;

		Element group_member_element_pod = group_member_element.getChild("Operation_definition");
		String pod = group_member_element_pod.getText();

		Element pod_element = steptopa.getPODelement(pod);
		Element pod_element_process_type = pod_element.getChild("Process_type");
		String type = pod_element_process_type.getText();

		if (type.equals(grouping_type)) {
			grouping = true;
		}

		return grouping;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public String getPodID(Element group_member_element) {

		Element group_member_element_pod = group_member_element.getChild("Operation_definition");
		String pod = group_member_element_pod.getText();

		Element pod_element = steptopa.getPODelement(pod);
		Element pod_element_process_type = pod_element.getChild("Process_type");
		pod_element_process_type.getText();

		Element pod_element_id = pod_element.getChild("Id");
		String id = pod_element_id.getText();

		return id;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


    public void CreateOperation(Element decomp_poo, Element PAroot, Element senastefliken, Element STEProot, String type) {

		Vector<String> total_resource_states = new Vector<String>();

		// 1. Get the relation and add it to the xml.
		//Element relation = new Element(steptopa.getPodID(decomp_poo));
		//senastefliken.addContent(relation);

		// 2. Get all POOs within this decomposed POO POO17 = POO17_1 seq POO17_2 seq POO17_3 ...
		Vector<Element> group = getDecompPOOs(decomp_poo);

		int groupNr;
		for (groupNr=0; groupNr < group.size(); groupNr=groupNr+1) {

			Element group_member_element = group.elementAt(groupNr);
			String group_member_id = group_member_element.getAttributeValue("id");

			Element poo_process = new Element("Process");
			poo_process.setAttribute("id", group_member_id);
			//relation.addContent(poo_process);
			senastefliken.addContent(poo_process);

			inlagda_operationer.add(group_member_id);

			// Add eventnames in xml
			Element definition = group_member_element.getChild("Operation_definition");
			String pod_id = definition.getText();

			Element pod_element = steptopa.getPODelement(pod_id);
			Element pod_name = pod_element.getChild("Name");
			String pod = pod_name.getText();

			Element event = new Element("Event");
			event.setAttribute("id", pod);
			poo_process.addContent(event);

			// 1. If it is a decomposed process add all groupmenbers restrictions on this poo.
			if (checkDecompGrouping(group_member_element, "Operation_decomp") | checkDecompGrouping(group_member_element, "Grouping")) {

				// Place all groupmembers restrictions on the decomposed POO.
				Element restriction = new Element("Restriction");
				event.addContent(restriction);

				Element and = new Element("And");
				restriction.addContent(and);

				place_restriction(and, group_member_element, STEProot, total_resource_states);

				// Adds the groupmembers without the restrictions.
				String typen = getPodID(group_member_element);
				CreateOperation(group_member_element, PAroot, poo_process, STEProot, typen);

			} else {

				// 2. if it is not a decomposed process then the restrictions are added for each poo.
				Vector<String> conditions = getConditions(group_member_id, STEProot);

				boolean relation_type;
				if (type != null) {
					relation_type = type.equals("simultaneity");
				} else {
					relation_type = false;
				}

				// If there are any conditions and that if there is a relation this has to be sim.
				if (! conditions.isEmpty() && ! relation_type) {

					Element restriction = new Element("Restriction");
					event.addContent(restriction);

					Element and = new Element("And");
					restriction.addContent(and);

					int condition_nr;
					for (condition_nr=0; condition_nr < conditions.size(); condition_nr=condition_nr+1) {

						// Each condition is a string expressions with possible several state conditions seperated by " ".
						String condition = conditions.elementAt(condition_nr);

						int nr = condition.indexOf(" ");
						int start_nr = 0;

						while (nr != -1) { // If there are more than one condition

							String single_condition = condition.substring(start_nr, nr);

							// Find condition
							String resource = get_resource(single_condition, STEProot);
							total_resource_states.add(resource);

							// Find resource
							String resource_state = get_state(single_condition, STEProot);
							total_resource_states.add(resource_state);

							Element condition_element = new Element("State");
							condition_element.setAttribute("name", resource);
							condition_element.setAttribute("id", resource_state);
							and.addContent(condition_element);

							start_nr = nr+1;
							nr = condition.indexOf(" ", nr+1);
						}

						if (start_nr != 0) { // adds the last of several conditions

							String single_condition = condition.substring(start_nr, condition.length());

							String resource = get_resource(single_condition, STEProot);
							total_resource_states.add(resource);

							String resource_state = get_state(single_condition, STEProot);
							total_resource_states.add(resource_state);

							Element condition_element = new Element("State");
							condition_element.setAttribute("name", resource);
							condition_element.setAttribute("id", resource_state);
							and.addContent(condition_element);

						} else { // Only one condition in string

							Vector<String> resource_state_vector = new Vector<String>();

							String resource = get_resource(condition, STEProot);
							resource_state_vector.add(resource);

							String resource_state = get_state(condition, STEProot);
							resource_state_vector.add(resource_state);

							total_resource_states = getNewStates(resource_state_vector, total_resource_states);

							int i;
							for (i=0; i < total_resource_states.size(); i=i+2) {

								String namn = total_resource_states.elementAt(i);
								String state = total_resource_states.elementAt(i+1);

								Element condition_element = new Element("State");
								condition_element.setAttribute("name", namn);
								condition_element.setAttribute("id", state);
								and.addContent(condition_element);
							}
						}
					}
				}
			}
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector<String> getNewStates(Vector<String> resource_state_vector, Vector<String> total) {

		// Changes state for a specific resource in the condition vector.
		String namn = resource_state_vector.elementAt(0);
		String state = resource_state_vector.elementAt(1);

		int nr;
		for (nr=0; nr < total.size(); nr=nr+2) {
			String namn_total = total.elementAt(nr);
			if (namn_total.equals(namn)) {
				total.set(nr+1, state);
			}
		}
	return total;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public String get_resource(String condition, Element STEProot) {

		String id_resource = null;
		String assigned_to = null;

		List<?> stateAssignmentList = STEProot.getChildren("State_assignment");
		for(Iterator<?> listSA = stateAssignmentList.iterator(); listSA.hasNext(); )
		{
			Element sa_element = (Element) listSA.next();
			String sa_id = sa_element.getAttributeValue("id");

			if (condition.equals(sa_id)) {
				Element assigned_to_element = sa_element.getChild("Assigned_to");
				assigned_to = assigned_to_element.getText();
			}
		}

		List<?> itemList = STEProot.getChildren("Item");
		for(Iterator<?> listItem = itemList.iterator(); listItem.hasNext(); )
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

		List<?> stateAssignmentList = STEProot.getChildren("State_assignment");
		for(Iterator<?> listSA = stateAssignmentList.iterator(); listSA.hasNext(); )
		{
			Element sa_element = (Element) listSA.next();
			String sa_id = sa_element.getAttributeValue("id");

			if (condition.equals(sa_id)) {
				Element Described_state_element = sa_element.getChild("Described_state");
				describedState = Described_state_element.getText();
			}
		}

		List<?> stateList = STEProot.getChildren("State");
		for(Iterator<?> listState = stateList.iterator(); listState.hasNext(); )
		{
			Element state_element = (Element) listState.next();
			String state_id = state_element.getAttributeValue("id");

			if (describedState.equals(state_id)) {

				Element name_element = state_element.getChild("Name");
				String name = name_element.getText();

				state_namn = name;
			}
		}
	return state_namn;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public void place_restriction(Element and, Element group_member_element, Element STEProot, Vector<String> total_resource_states) {

		Vector<Element> group = getDecompPOOs(group_member_element);

		int number_of_groupmembers = group.size();
		int number_in_group;
		for (number_in_group=0; number_in_group < number_of_groupmembers; number_in_group=number_in_group+1) {

			Element group_element = group.elementAt(number_in_group);
			String group_member_id = group_element.getAttributeValue("id");

			Vector<String> conditions = getConditions(group_member_id, STEProot);

			int condition_nr;
			for (condition_nr=0; condition_nr < conditions.size(); condition_nr=condition_nr+1) {

				String condition = conditions.elementAt(condition_nr);

				int nr = condition.indexOf(" ");
				int start_nr = 0;

				while (nr != -1) { // All states except the last

					String single_condition = condition.substring(start_nr, nr);

					// Find the resource_state
					String resource = get_resource(single_condition, STEProot);
					total_resource_states.add(resource);

					// Find the resource
					String resource_state = get_state(single_condition, STEProot);
					total_resource_states.add(resource_state);

					start_nr = nr+1;
					nr = condition.indexOf(" ", nr+1);
				}

				if (start_nr != 0) { // The last state

					String single_condition = condition.substring(start_nr, condition.length());

					// Find the resource_state
					String resource = get_resource(single_condition, STEProot);
					total_resource_states.add(resource);

					// Find the resource
					String resource_state = get_state(single_condition, STEProot);
					total_resource_states.add(resource_state);

				} else { // There is only one state in the string

					Vector<String> resource_state_vector = new Vector<String>();

					// Find the resource_state
					String resource = get_resource(condition, STEProot);
					resource_state_vector.add(resource);

					// Find the resource
					String resource_state = get_state(condition, STEProot);
					resource_state_vector.add(resource_state);

					total_resource_states = getNewStates(resource_state_vector, total_resource_states);
				}
			}
		}

		int i;
		for (i=0; i < total_resource_states.size(); i=i+2) {

			String namn = total_resource_states.elementAt(i);
			String state = total_resource_states.elementAt(i+1);

			Element condition_element = new Element("State");
			condition_element.setAttribute("name", namn);
			condition_element.setAttribute("id", state);
			and.addContent(condition_element);
		}
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector<Element> getDecompPOOs(Element poo) {

		Vector<Element> group = new Vector<Element>();

		// Get all POOR for a specific POO
		List<?> poorList = poo.getChildren("Process_operation_occurrence_relationship");
		for(Iterator<?> listPOOR = poorList.iterator(); listPOOR.hasNext(); )
		{
			Element relation_member_element = (Element) listPOOR.next();
			Element relType = relation_member_element.getChild("Relation_type");
			String  type = relType.getText();

			if (type.equals("decomposition")) {
				Element group_member = relation_member_element.getChild("Related");
				String group_member_text = group_member.getText();
				Element group_member_element = steptopa.getPOOelement(group_member_text);
				group.add(group_member_element);
			}
		}
	return group;
	}

////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public Vector<String> getConditions(String poo, Element STEProot) {

		Vector<String> group = new Vector<String>();

		List<?> caList = STEProot.getChildren("Condition_assignment");
		for(Iterator<?> listCA = caList.iterator(); listCA.hasNext(); )
		{
			Element ca_element = (Element) listCA.next();

			Element assignedto = ca_element.getChild("Assigned_to");
			String assignedto_text = assignedto.getText();

			Element name_element = ca_element.getChild("Name");
			String name = name_element.getText();

			if (assignedto_text.equals(poo) && name.equals("required resource state")) {
				Element described_condition = ca_element.getChild("Described_condition");
				String conditions = described_condition.getText();

				group.add(conditions);
			}
		}
	return group;
	}
}