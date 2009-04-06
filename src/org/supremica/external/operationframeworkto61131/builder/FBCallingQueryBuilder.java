package org.supremica.external.operationframeworkto61131.builder;

import java.math.BigInteger;
import java.util.List;

import org.supremica.external.operationframeworkto61131.data.FBCallingQuery;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.external.operationframeworkto61131.util.StringUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;
import org.supremica.manufacturingtables.xsd.eop.ActuatorValue;
import org.supremica.manufacturingtables.xsd.eop.ExternalComponentValue;
import org.supremica.manufacturingtables.xsd.eop.SensorValue;
import org.supremica.manufacturingtables.xsd.eop.VariableValue;
import org.supremica.manufacturingtables.xsd.eop.ZoneState;



/**
 * FBCallingQueryBuilder.java builds machine state query for the conversion of
 * EOP and COP. The query will be solved by the interface
 * EquipmentStateLookup.java
 * 
 * Created: Mar 31, 2009 5:52:32 PM
 * 
 * @author LC
 * @version 1.0
 */
public class FBCallingQueryBuilder {

	private LogUtil log = LogUtil.getInstance();

	public FBCallingQuery processActuatorValues(
			List<ActuatorValue> actuatorValues, String machine) {

		FBCallingQuery queryList = FBCallingQuery.getInstance();

		if (actuatorValues != null & !actuatorValues.isEmpty()) {

			for (ActuatorValue actuatorValue : actuatorValues) {
				StateQuery query = new StateQuery();
				query.setMachine(machine);
				query.setEquipmentEntityType(actuatorValue.getClass());
				query.setEquipmentEntityName(machine
						+ actuatorValue.getActuator());
				query.setState(actuatorValue.getValue());
				queryList.append(query);

			}
		}
		return queryList;
	}

	public FBCallingQuery processSensorValues(List<SensorValue> sensorValues,
			String machine) {

		FBCallingQuery queryList = FBCallingQuery.getInstance();

		if (sensorValues != null & !sensorValues.isEmpty()) {
			for (SensorValue sensorValue : sensorValues) {

				StateQuery query = new StateQuery();

				query.setMachine(machine);
				query.setEquipmentEntityType(sensorValue.getClass());
				query.setEquipmentEntityName(machine + sensorValue.getSensor());
				query.setState(sensorValue.getValue());
				queryList.append(query);

			}
		}
		return queryList;
	}

	public FBCallingQuery processVariableValues(
			List<VariableValue> variableValues, String machine) {

		FBCallingQuery queryList = FBCallingQuery.getInstance();
		if (variableValues != null & !variableValues.isEmpty()) {
			for (VariableValue variableValue : variableValues) {

				StateQuery query = new StateQuery();

				String variableName = StringUtil
						.replaceSpaceWithUnderscore(variableValue.getVariable());
				log.debug("Query:" + variableName);
				query.setMachine(machine);
				query.setEquipmentEntityType(variableValue.getClass());
				query.setEquipmentEntityName(variableName);
				query.setState(variableValue.getValue());
				queryList.append(query);

			}
		}
		return queryList;
	}

	public FBCallingQuery processZoneStates(List<ZoneState> zoneStates,
			String machine) {

		FBCallingQuery queryList = FBCallingQuery.getInstance();

		if (zoneStates != null & !zoneStates.isEmpty()) {
			for (ZoneState zoneState : zoneStates) {

				StateQuery query = new StateQuery();

				query.setEquipmentEntityType(zoneState.getClass());
				query.setEquipmentEntityName(zoneState.getZone());
				query.setMachine(machine);
				query.setState(zoneState.getState());
				queryList.append(query);

			}
		}
		return queryList;
	}

	public FBCallingQuery processExteranlComponents(
			List<ExternalComponentValue> externalComponentValues) {

		FBCallingQuery queryList = FBCallingQuery.getInstance();
		if (externalComponentValues != null
				& !externalComponentValues.isEmpty()) {
			for (ExternalComponentValue externalComponentValue : externalComponentValues) {

				String nameOfComponent = StringUtil
						.replaceSpaceWithUnderscore(externalComponentValue
								.getExternalComponent().getComponent());
				String machineOfComponent = externalComponentValue
						.getExternalComponent().getMachine();
				String valueOfComponnet = externalComponentValue.getValue();

				StateQuery query = new StateQuery();

				query.setEquipmentEntityName(machineOfComponent
						+ nameOfComponent);
				query.setEquipmentEntityType(externalComponentValue.getClass());
				query.setMachine(machineOfComponent);
				query.setState(valueOfComponnet);

				queryList.append(query);
			}
		}
		return queryList;

	}

	public FBCallingQuery processMachineOperation(BigInteger opID,
			String machine) {

		FBCallingQuery queryList = FBCallingQuery.getInstance();

		if (opID != null) {
			StateQuery query = new StateQuery();
			query.setMachine(machine);
			query.setEquipmentEntityType(null);
			query.setEquipmentEntityName(machine);
			query.setState(opID.toString());
			queryList.append(query);
		}
		return queryList;

	}

}
