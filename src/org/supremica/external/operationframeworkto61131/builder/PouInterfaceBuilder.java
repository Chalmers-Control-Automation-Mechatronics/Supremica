package org.supremica.external.operationframeworkto61131.builder;

import org.plcopen.xml.tc6.*;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;



/**
 * PouInterfaceBuilder.java builds the interface section for Pou. The data type
 * of generated variable is also decided in this class. Methods in this class
 * will be invoked by Builder classes in package builder.
 * 
 * Created: Mar 31, 2009 6:06:20 PM
 * 
 * @author LC
 * @version 1.0
 */
public class PouInterfaceBuilder {
	private static org.plcopen.xml.tc6.ObjectFactory objectFactory = new org.plcopen.xml.tc6.ObjectFactory();

	private static LogUtil log = LogUtil.getInstance();

	public static Project.Types.Pous.Pou.Interface generatePouInterfaceFromVarList(
			org.supremica.external.operationframeworkto61131.data.VarList interfaceVarList,
			Project.Types.Pous.Pou.Interface pouInterface) {

		if (pouInterface == null) {
			pouInterface = objectFactory.createProjectTypesPousPouInterface();

		}
		// FIXME add interface
		// if (true) {
		// return pouInterface;
		// }

		Project.Types.Pous.Pou.Interface.ExternalVars externalVars = objectFactory
				.createProjectTypesPousPouInterfaceExternalVars();

		Project.Types.Pous.Pou.Interface.ExternalVars constantExternalVars = objectFactory
				.createProjectTypesPousPouInterfaceExternalVars();

		Project.Types.Pous.Pou.Interface.ExternalVars retainExternalVars = objectFactory
				.createProjectTypesPousPouInterfaceExternalVars();

		Project.Types.Pous.Pou.Interface.ExternalVars constantAndRetainExternalVars = objectFactory
				.createProjectTypesPousPouInterfaceExternalVars();

		for (org.supremica.external.operationframeworkto61131.data.Var var : interfaceVarList.getVars()) {

			VarListPlain.Variable variable = getPlainVariableFromVar(var);
			// Retain and Constant has to be seperate varlists and added to
			// Global seperately
			if (var.isConstant() && var.isRetain()) {

				// Constant and Retain
				constantAndRetainExternalVars.getVariable().add(variable);
			} else if (var.isConstant()) {

				// Only Constant
				constantExternalVars.getVariable().add(variable);
			} else if (var.isRetain()) {

				// Only Retain
				retainExternalVars.getVariable().add(variable);
			} else {

				// neither Constain nor retain
				externalVars.getVariable().add(variable);
			}

		}

		pouInterface.getLocalVarsOrTempVarsOrInputVars().add(externalVars);

		if (!constantExternalVars.getVariable().isEmpty()) {
			constantExternalVars.setConstant(true);
			pouInterface.getLocalVarsOrTempVarsOrInputVars().add(
					constantExternalVars);

		}

		if (!retainExternalVars.getVariable().isEmpty()) {
			retainExternalVars.setRetain(true);
			pouInterface.getLocalVarsOrTempVarsOrInputVars().add(
					retainExternalVars);

		}

		if (!constantAndRetainExternalVars.getVariable().isEmpty()) {

			constantAndRetainExternalVars.setConstant(true);
			constantAndRetainExternalVars.setConstant(true);
			pouInterface.getLocalVarsOrTempVarsOrInputVars().add(
					constantAndRetainExternalVars);

		}

		return pouInterface;

	}

	public static org.supremica.external.operationframeworkto61131.data.VarList getVarListFromPouInterface(
			Project.Types.Pous.Pou.Interface pouInterface) {

		org.supremica.external.operationframeworkto61131.data.VarList varList = org.supremica.external.operationframeworkto61131.data.VarList.getInstance();

		for (VarList pouVarList : pouInterface
				.getLocalVarsOrTempVarsOrInputVars()) {

			for (VarListPlain.Variable variable : pouVarList.getVariable()) {

				Class type = Boolean.class;

				if (variable.getType().getINT() != null) {

					type = Integer.class;

				} else if (variable.getType().getString() != null) {

					type = String.class;
				}

				String name = variable.getName();
				String value = variable.getInitialValue().getSimpleValue()
						.getValue();

				org.supremica.external.operationframeworkto61131.data.Var var = new org.supremica.external.operationframeworkto61131.data.Var(name, value,
						type);
				String address = variable.getAddress();
				if (address != null) {

					var.setAddress(address);

				}

				varList.append(var);

			}

		}

		return varList;

	}

	public static VarListPlain.Variable getPlainVariableFromVar(
			org.supremica.external.operationframeworkto61131.data.Var var) {

		VarListPlain.Variable variable = objectFactory
				.createVarListPlainVariable();

		if (var.getAddress() != null) {

			variable.setAddress(var.getAddress());

		}

		// TODO add documentation here?

		if (var.getDocumentation() != null && !var.getDocumentation().isEmpty()) {

			// variable.setDocumentation(value)

		}

		// Set name
		variable.setName(var.getName());

		// Set initial value
		Value value = objectFactory.createValue();
		Value.SimpleValue simpleValue = objectFactory.createValueSimpleValue();
		simpleValue.setValue(var.getValue());
		value.setSimpleValue(simpleValue);
		variable.setInitialValue(value);

		// FIXME judge the type for int or time
		// Set type
		DataType type = new DataType();
		if (var.getType().equals(Boolean.class)) {
			// Strange?
			type.setBOOL(new DataType.String());
		} else if (var.getType().equals(Integer.class)) {

			type.setINT(new DataType.String());
		}

		variable.setType(type);
		return variable;

	}

	public static org.plcopen.xml.tc6.Project.Types.Pous.Pou.Interface.LocalVars getFBLocalVar(
			String typeName, String instanceName) {

		org.plcopen.xml.tc6.Project.Types.Pous.Pou.Interface.LocalVars localVars = CommonLayoutObject.objectFactory
				.createProjectTypesPousPouInterfaceLocalVars();

		org.plcopen.xml.tc6.VarListPlain.Variable variable = CommonLayoutObject.objectFactory
				.createVarListPlainVariable();
		variable.setName(instanceName);
		org.plcopen.xml.tc6.DataType dataType = new org.plcopen.xml.tc6.DataType();
		org.plcopen.xml.tc6.DataType.Derived derived = CommonLayoutObject.objectFactory
				.createDataTypeDerived();
		derived.setName(typeName);
		dataType.setDerived(derived);
		variable.setType(dataType);
		localVars.getVariable().add(variable);

		return localVars;
	}

}
