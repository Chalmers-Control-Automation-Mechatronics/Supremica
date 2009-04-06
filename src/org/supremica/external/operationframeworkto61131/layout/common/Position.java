package org.supremica.external.operationframeworkto61131.layout.common;
/**
 * @author LC
 *
 */
import java.math.BigDecimal;

public class Position {

	private int x;

	private int y;

	public Position() {

		x = 0;
		y = 0;
	}

	public Position(int x, int y) {

		this.x = x;
		this.y = y;
	}

	public Position(Position p) {

		this.x = p.getX();
		this.y = p.getY();
	}

	public Position(org.plcopen.xml.tc6.Position position) {

		if (position.getX() != null) {

			this.x = position.getX().intValue();
		}

		if (position.getY() != null) {

			this.y = position.getY().intValue();
		}

	}

	public org.plcopen.xml.tc6.Position getPLCOpenObject() {

		org.plcopen.xml.tc6.Position poistion = CommonLayoutObject.objectFactory
				.createPosition();

		poistion.setY(BigDecimal.valueOf(y));
		poistion.setX(BigDecimal.valueOf(x));

		return poistion;

	}

	public Position add(Position operand) {
		addX(operand.getX());
		addY(operand.getY());

		return new Position(x, y);
	}

	public Position add(int x, int y) {
		addX(x);
		addY(y);

		return new Position(this.x, this.y);
	}

	public Position subtract(Position operand) {
		subtractX(operand.getX());
		subtractY(operand.getY());

		return new Position(x, y);
	}

	public Position subtract(int x, int y) {
		subtractX(x);
		subtractY(y);

		return new Position(this.x, this.y);
	}

	public void addX(int op) {

		x = x + op;

	}

	public void addY(int op) {

		y = y + op;

	}

	public void subtractX(int op) {

		x = x - op;

	}

	public void subtractY(int op) {

		y = y - op;

	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public String toString() {

		return new String("(" + x + ", " + y + ")");
	}

	// return 1/d x
	public int getXOf(int d) {

		return BigDecimal.valueOf(x).divideToIntegralValue(
				BigDecimal.valueOf(d)).intValue();

	}

	// return 1/d y
	public int getYof(int d) {

		return BigDecimal.valueOf(y).divideToIntegralValue(
				BigDecimal.valueOf(d)).intValue();

	}
}
