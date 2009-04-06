package org.supremica.external.operationframeworkto61131.data;

/**
 * @author LC
 *
 */

public class FBCallingVars {

	
	private Var requestVar;

	private Var feedbackVar;
	
	private String equipmentEntity;

	private String targetState;
	
	private Class ownerType;
	

	
	
	public FBCallingVars() {

	}
	
/*	public static FBCallingVars getNewVarPair(String order, String feedback, Class ownerType) {

		FBCallingVars callingVar = new FBCallingVars();

		Var orderVar = new Var(order, Boolean.TRUE);
		Var feedbackVar = new Var(feedback, Boolean.TRUE);

		callingVar.addOrderVar(orderVar);
		callingVar.addFeedbackVar(feedbackVar);
		return callingVar;

	}*/

	public Var getFeedbackVar() {
		return feedbackVar;
	}

	public void setFeedbackVar(Var feedbackVar) {
		this.feedbackVar = feedbackVar;
	}

	public Var getRequestVar() {
		return requestVar;
	}

	public void setRequestVar(Var requestVar) {
		this.requestVar = requestVar;
	}

	public Class getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(Class ownerType) {
		this.ownerType = ownerType;
	}

	/**
	 * @return the equipmentEntity
	 */
	public String getEquipmentEntity() {
		return equipmentEntity;
	}

	/**
	 * @param equipmentEntity the equipmentEntity to set
	 */
	public void setEquipmentEntity(String equipmentEntity) {
		this.equipmentEntity = equipmentEntity;
	}


	public String getTargetState() {
		return targetState;
	}


	public void setTargetState(String targetState) {
		this.targetState = targetState;
	}


	

}