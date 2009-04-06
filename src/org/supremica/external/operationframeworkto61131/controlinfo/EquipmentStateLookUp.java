package org.supremica.external.operationframeworkto61131.controlinfo;
/**
 * @author LC
 *
 */
import org.supremica.external.operationframeworkto61131.data.FBCallingQuery;
import org.supremica.external.operationframeworkto61131.data.FBCallingVars;
import org.supremica.external.operationframeworkto61131.data.FBCallingVarsList;
import org.supremica.external.operationframeworkto61131.data.StateQuery;
import org.supremica.manufacturingtables.xsd.controlInformation.EquipmentEntity;
import org.supremica.manufacturingtables.xsd.controlInformation.Machine;




public interface EquipmentStateLookUp {
	
	
	
	String AREA="Area";
	String FACTORY="Facotry";
	String CELL="Cell";
	String MACHINE="machine";
	
	public FBCallingVarsList getFBCallingVars(FBCallingQuery queryList);
	
	public FBCallingVars getFBCallingVars(StateQuery stateQuery);
	
	public Object getFactory();
		
//	public convertor.xsd.controlInformation.State getState(
//			String targetStateName, EquipmentEntity equipmentEntity);
//	
	public Object getMachine(String machineName);
//	public convertor.data. getFBCallingVars(convertor.xsd.physicalResource.Machine machine, convertor.xsd.physicalResource.EquipmentEntity equipmentEntity, convertor.xsd.physicalResource.Element element);
	
	public Boolean hasOwnSystem(String machineName);
//	
//	public  FBCallingVars getMachineStateVars(String machine, String equipment,
//			String state) ;

}
