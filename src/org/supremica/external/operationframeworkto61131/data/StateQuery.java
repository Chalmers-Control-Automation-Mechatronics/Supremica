package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
public class StateQuery {

	
	private String machine;
	private Class equipmentEntityType;
	private String equipmentEntityName;
	private String state;
	
	
	public Class getEquipmentEntityType() {
		return equipmentEntityType;
	}
	public void setEquipmentEntityType(Class equipmentEntityType) {
		this.equipmentEntityType = equipmentEntityType;
	}
	public String getMachine() {
		return machine;
	}
	public void setMachine(String machine) {
		this.machine = machine;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getEquipmentEntityName() {
		return equipmentEntityName;
	}
	public void setEquipmentEntityName(String equipmentEntityName) {
		this.equipmentEntityName = equipmentEntityName;
	}
	
	
}
