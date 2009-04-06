package org.supremica.external.operationframeworkto61131.layout.ladder;
/**
 * @author LC
 *
 */
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.Position;

public class Contact extends CommonLayoutObject {

	private String varaible;

	private Boolean isNegated;
	
//	FIXME move to Constant
	private static int contactHeight = 15;

	private static int contactWidth = 21;

	public Contact() {

	}

	// Generate a Contact of default size with ConnectionPointIn and
	// ConnectionPointOut
	// at the default position
	public Contact(int localId) {

		Position conInRelPosition = new Position(0, 8);

		Position conOutRelPosition = new Position(contactWidth, 8);

		super.setDefault(localId, contactHeight, contactWidth,
				conInRelPosition, conOutRelPosition);

	}

	public org.plcopen.xml.tc6.Body.SFC.Contact getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.Contact contact = CommonLayoutObject.objectFactory
				.createBodySFCContact();
	
		try {

			super.getPLCopenObject(contact);
			super.getCommenConInPLCOpenObject(contact);
			super.getCommenConOutPLCOpenObject(contact);

			if (this.varaible != null) {

				contact.setVariable(varaible);
			}

			if (isNegated != null) {

				contact.setNegated(Boolean.TRUE);
			}
		} catch (Exception e) {

			e.printStackTrace();
		}

		return contact;

	}

	public String getVaraible() {
		return varaible;
	}

	public void setVaraible(String varaible) {
		this.varaible = varaible;
	}

	// this attribute is absent when it's false.
	public void setNegated() {
		this.isNegated = Boolean.TRUE;
	}

}
