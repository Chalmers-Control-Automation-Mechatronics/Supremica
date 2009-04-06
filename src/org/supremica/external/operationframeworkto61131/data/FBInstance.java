package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
import java.util.List;


// not been used yet
public class FBInstance {
	
	String name;
	String type;
	List<org.supremica.external.operationframeworkto61131.data.FBConnection> fbConnectionList;
	
	
	public List<org.supremica.external.operationframeworkto61131.data.FBConnection> getFbConnectionList() {
		return fbConnectionList;
	}
	public void setFbConnectionList(
			List<org.supremica.external.operationframeworkto61131.data.FBConnection> fbConnectionList) {
		this.fbConnectionList = fbConnectionList;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	
	
	
}
