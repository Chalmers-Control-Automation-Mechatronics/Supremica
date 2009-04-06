package org.supremica.external.operationframeworkto61131.rslogix.ladder;
/**
 * @author LC
 *
 */
import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

import org.plcopen.xml.tc6.Connection;
import org.plcopen.xml.tc6.ConnectionPointIn;
import org.supremica.external.operationframeworkto61131.util.ReflectionUtil;
import org.supremica.external.operationframeworkto61131.util.log.LogUtil;


import sun.security.util.BigInt;

public class CommonObj {

	private static LogUtil log = LogUtil.getInstance();
	
	private String type;

	public static final String COIL = "Coil";
	public static final String CONTACT = "Contact";

	private String qualifier = "";

	private List<BigInteger> predecessor = new LinkedList<BigInteger>();

	private List<BigInteger> connected = new LinkedList<BigInteger>();

	private boolean isEnd = false;

	private boolean isStart = false;

	private boolean isVisitied = false;

	private String variable;

	private BigInteger localId = BigInteger.ZERO;


	public String getQualifier() {
		return qualifier;
	}

	public void setQualifier(String qualifier) {
		this.qualifier = qualifier;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<BigInteger> getPredecessor() {
		return predecessor;
	}

	public void addPredecessor(BigInteger refLocalId) {
		predecessor.add(refLocalId);
	}

	public Boolean isConnected(BigInteger localId) {

		if (connected.contains(localId)) {
			return true;
		} else {
			return false;
		}
	}

	public List<BigInteger> getConnected() {
		return connected;
	}

	public void setConnected(List<BigInteger> connected) {
		this.connected = connected;
	}

	public String getVariable() {
		return variable;
	}

	public void setVariable(String variable) {
		this.variable = variable;
	}

	public BigInteger getLocalId() {
		return localId;
	}

	public void setLocalId(BigInteger localId) {
		this.localId = localId;
	}

	public boolean isEnd() {

		return isEnd;

	}

	public void setEnd() {
		this.isStart = false;
		this.isEnd = true;
	}

	public boolean isStart() {
		return isStart;
	}

	public void setStart() {
		this.isStart = true;
		this.isEnd = false;
	}

	public void setVisited() {

		this.isVisitied = true;
	}

	public Boolean isVisited() {

		return this.isVisitied;
	}

}
