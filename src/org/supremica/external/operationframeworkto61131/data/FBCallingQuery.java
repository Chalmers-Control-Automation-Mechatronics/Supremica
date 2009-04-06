package org.supremica.external.operationframeworkto61131.data;
/**
 * @author LC
 *
 */
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.supremica.external.operationframeworkto61131.util.log.LogUtil;




public class FBCallingQuery {

	List<StateQuery> queryList;
	private LogUtil log=LogUtil.getInstance();
	
	private FBCallingQuery() {

		queryList = new LinkedList<StateQuery>();
	}

	public static FBCallingQuery getInstance() {

		return new FBCallingQuery();
	}

	public void append(FBCallingQuery newQueryList) {

		if (newQueryList.getQueryList().isEmpty()) {

			return;
		} else if (queryList.isEmpty()) {

			queryList = newQueryList.getQueryList();
		} else {

			Iterator<StateQuery> iter = newQueryList.getQueryList().iterator();

			while (iter.hasNext()) {

				append(iter.next());
			}

		}

	}

	public void append(StateQuery query) {

		if (!queryList.contains(query)) {

			queryList.add(query);
		}
	}

	public List<StateQuery> getQueryList() {
		return queryList;
	}
	
	public void removeStateWithValue(String stateValueToRemove){
		
		Iterator<StateQuery> itor=queryList.iterator();
		
		
		while(itor.hasNext()){
			
			StateQuery query=itor.next();
			
			if(query.getState().equals(stateValueToRemove)){
				
				log.debug("remove query:"+query.getEquipmentEntityName());
				itor.remove();
						
			}
			
		}
		                      
	}

}
