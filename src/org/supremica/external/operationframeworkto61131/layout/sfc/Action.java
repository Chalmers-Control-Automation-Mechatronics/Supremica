package org.supremica.external.operationframeworkto61131.layout.sfc;
/**
 * @author LC
 *
 */

public class Action {

	// <action>
	// <reference name="C1R1_Op20_exe" />
	// </action>

	private String referece = "";

	Action() {

	}

	public String getReferece() {
		return referece;
	}

	public void setReferece(String referece) {

		if (referece != null) {
			this.referece = referece;
		}
	}

}
