package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
import java.util.List;
import java.util.LinkedList;

public class FBCallingVarsList {

	private List<FBCallingVars> fbCallingVarsList = new LinkedList<FBCallingVars>();

	public FBCallingVarsList() {
		// Show strange behaviors if initilize the list here. A live list ?
		// FBCallingVarsList=new LinkedList<FBCallingVars>();

	}

	public void append(FBCallingVars newFBCallingVars) {

		if (newFBCallingVars != null) {
			fbCallingVarsList.add(newFBCallingVars);
		}
	}

	public void append(List<FBCallingVars> newList) {

		if (newList != null & !newList.isEmpty()) {
			for (FBCallingVars newFBCallingVars : newList) {
				fbCallingVarsList.add(newFBCallingVars);
			}
		}
	}

	public void append(FBCallingVarsList newListObject) {

		append(newListObject.getFBCallingVarsList());
	}

	public List<FBCallingVars> getFBCallingVarsList() {

		return fbCallingVarsList;
	}

	public VarList getRequestVarList() {

		VarList varList = VarList.getInstance();

		for (FBCallingVars fbCallingVars : this.fbCallingVarsList) {

			Var requestVar = fbCallingVars.getRequestVar();

			if (requestVar != null) {

				varList.append(requestVar);
			}

		}

		return varList;
	}

	
	public VarList getFeedbackVarList() {

		VarList varList = VarList.getInstance();

		for (FBCallingVars fbCallingVars : this.fbCallingVarsList) {

			Var feedbackVar = fbCallingVars.getFeedbackVar();

			if (feedbackVar != null) {

				varList.append(feedbackVar);
			}

		}

		return varList;
	}
}
