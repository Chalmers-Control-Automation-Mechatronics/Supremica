package org.supremica.external.operationframeworkto61131.util;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.plcopen.xml.tc6.Body;
import org.plcopen.xml.tc6.Connection;
import org.plcopen.xml.tc6.ConnectionPointOut;
import org.plcopen.xml.tc6.ConnectionPointIn;
import org.plcopen.xml.tc6.Position;
import org.plcopen.xml.tc6.Project;
import org.supremica.external.operationframeworkto61131.data.FBCallingQuery;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.external.operationframeworkto61131.data.Var;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;




public class PLCopenUtil {

	private static LogUtil log = LogUtil.getInstance();

	private org.plcopen.xml.tc6.ObjectFactory plcopenObjectFactory;

	public PLCopenUtil() {

		plcopenObjectFactory = new org.plcopen.xml.tc6.ObjectFactory();
	}

	// public FBCallingQuery removeDuplicateVarState(FBCallingQuery queryList) {
	//
	// Iterator<StateQuery> itor = queryList.getQueryList().iterator();
	//
	// while (itor.hasNext()) {
	// StateQuery stateQuery = itor.next();
	//
	// Class equipType = stateQuery.getEquipmentEntityType();
	// // only consider actuator and zone booking
	// if (equipType.equals(convertor.xsd.eop.ActuatorValue.class)
	// || equipType.equals(convertor.xsd.eop.ZoneState.class)) {
	//
	// String orderVarName = stateQuery.getMachine()
	// + StringUtil.removeSpace(stateQuery
	// .getEquipmentEntityName());
	// String orderVarState = stateQuery.getState();
	//
	// if (varMap.containsKey(orderVarName)) {
	// String lastVarState = varMap.get(orderVarName);
	//
	// if (lastVarState.equals(orderVarState)) {
	//
	// itor.remove();
	// log.debug("remove duplicate action:" + orderVarName
	// + "=" + orderVarState);
	//
	// } else {
	//
	// varMap.remove(orderVarName);
	// varMap.put(orderVarName, orderVarState);
	//
	// }
	//
	// } else {
	// varMap.put(orderVarName, orderVarState);
	//
	// }
	// }
	// }
	//
	// return queryList;
	//
	// }

	public void displayQueryList(FBCallingQuery queryList) {

		for (StateQuery q : queryList.getQueryList()) {
			log.debug("Machine:" + q.getMachine());
			log.debug("Name:" + q.getEquipmentEntityName());
			// log.debug("Type:" + q.getEquipmentEntityType().toString());
			log.debug("State:" + q.getState());

		}

	}

	public void displayCallingVarList(FBCallingVarsList callingList) {

		for (FBCallingVars c : callingList.getFBCallingVarsList()) {
			// log.debug("Equipment:" + c.getOwnerType().toString());
			log.debug("OrderVar:" + c.getRequestVar().getName());
			log.debug("FeedbackVar:" + c.getFeedbackVar().getName());
		}

	}

	public static FBCallingVars getTRUECallingVar() {
		String expression = "TRUE";
		Var TRUE_feedbackVar = new Var(expression, Boolean.TRUE);
		FBCallingVars tRUE = new FBCallingVars();
		tRUE.setRequestVar(TRUE_feedbackVar);
		tRUE.setFeedbackVar(TRUE_feedbackVar);
		tRUE.setTargetState(Boolean.TRUE.toString());

		return tRUE;
	}

	public static void removeVariableFromPouInterface(
			Project.Types.Pous.Pou pou, String variableName) {

		List<org.plcopen.xml.tc6.VarList> varLists = pou.getInterface()
				.getLocalVarsOrTempVarsOrInputVars();

		for (org.plcopen.xml.tc6.VarList varList : varLists) {

			for (org.plcopen.xml.tc6.VarListPlain.Variable variable : varList
					.getVariable()) {

				if (variable.getName().equals(variableName)) {

					varList.getVariable().remove(variable);

					return;
				}

			}
		}

	}

	public static Class getDateTypeClass(String dataTypeString) {

		Class dataType = Boolean.class;
		// odd way,need defined a better way in schema to determine the data
		// type
		if (!StringUtil.isEmpty(dataTypeString)) {

			if (dataTypeString.equalsIgnoreCase("String")) {

				dataType = String.class;
			} else if (dataTypeString.equalsIgnoreCase("Integer")) {

				dataType = Integer.class;
			} else if (dataTypeString.equalsIgnoreCase("Boolean")) {

				dataType = Boolean.class;
			} else if (dataTypeString.equalsIgnoreCase("Float")) {

				dataType = Float.class;
			} else {

				log.error("Can not find matching data type:" + dataTypeString);
			}

		} else {

			log.error("Find  null or empty data type.");
		}

		return dataType;

	}

	public static String getLetterFromInt(int i, int base) {

		// A:65 a:97
		int op_letter_int = base + i;
		char[] op_letterChar = Character.toChars(op_letter_int);
		String opLetter = String.valueOf(op_letterChar[0]);

		return opLetter;

	}

}
