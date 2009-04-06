package org.supremica.external.operationframeworkto61131.layout.ladder;
/**
 * @author LC
 *
 */
import java.lang.reflect.Method;
import java.util.List;

import org.plcopen.xml.tc6.StorageModifierType;
import org.supremica.external.operationframeworkto61131.layout.common.CommonLayoutObject;
import org.supremica.external.operationframeworkto61131.layout.common.ConnectionPointIn;
import org.supremica.external.operationframeworkto61131.layout.common.Position;



public class Coil extends CommonLayoutObject {

	private String varaible;

	private Boolean isNegated;

	private static int coilHeight = 15;

	private static int coilWidth = 21;

	private StorageModifierType storageModifier;

	public Coil() {

	}

	// Generate a coil of default size with ConnectionPointIn and
	// ConnectionPointOut
	// at the default position
	public Coil(int localId) {

		Position conInRelPosition = new Position(0, 8);

		Position conOutRelPosition = new Position(coilWidth, 8);

		super.setDefault(localId, coilHeight, coilWidth, conInRelPosition,
				conOutRelPosition);

	}

	public org.plcopen.xml.tc6.Body.SFC.Coil getPLCOpenObject() {

		org.plcopen.xml.tc6.Body.SFC.Coil coil = CommonLayoutObject.objectFactory
				.createBodySFCCoil();
	
		try {

			super.getPLCopenObject(coil);
			super.getCommenConInPLCOpenObject(coil);
			super.getCommenConOutPLCOpenObject(coil);

			if (this.varaible != null) {

				coil.setVariable(varaible);
			}

			if (isNegated != null) {

				coil.setNegated(Boolean.TRUE);
			}

			if (this.storageModifier != null) {

				coil.setStorage(this.storageModifier);
			}

		} catch (Exception e) {

			e.printStackTrace();

		}

		return coil;
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

	public void setStorage(StorageModifierType _storageModifier) {

		this.storageModifier = _storageModifier;

	}

	public void setStorage(String _storageModifierStr) {

		this.storageModifier = StorageModifierType
				.fromValue(_storageModifierStr);

	}

	// Decide the poistion of the coil first and then connect contacts to
	// coil(insert Connection to Coil.ConnectionPointIn)
	public void connectToContacts(List<Contact> lastContactList,
			LeftPowerRail leftPowerRail, int distanceUnit) {

		if (lastContactList == null || lastContactList.isEmpty()) {

			// when there is no contact generated from all terms, connect coil
			// to
			// leftPowerRail

			super.connectToOut(leftPowerRail, new Position(distanceUnit
					* this.getVaraible().length(), 0));

			return;
		}

		int coilPositionX = 0;
		int coilPositionY = 0;

		Contact largestX = lastContactList.get(0);
		Contact smallestY = lastContactList.get(0);
		// find the contact with the largest X ,then it's in the longest line.
		for (Contact contact : lastContactList) {

			if (contact.getPosition().getX() >= largestX.getPosition().getX()) {

				largestX = contact;
			}

			if (contact.getPosition().getY() <= smallestY.getPosition().getY()) {

				smallestY = contact;
			}

		}

		int distance = (this.getVaraible().length() + largestX.getVaraible()
				.length())
				* distanceUnit;

		coilPositionX = largestX.getPosition().getX() + distance;
		coilPositionY = smallestY.getPosition().getY();

		// Specified the midway of the route
		Position midWayPoint = new Position(coilPositionX
				- this.getVaraible().length() * distanceUnit, coilPositionY);

		super.setPosition(coilPositionX, coilPositionY);

		// Connect contacts to coil
		for (Contact contact : lastContactList) {

			// LayoutUtil.connectLastToNext(contact, coil, midWayPoint);

			super.connectToOut(contact, 0, 0, midWayPoint, null);

		}

		return;

	}

}
