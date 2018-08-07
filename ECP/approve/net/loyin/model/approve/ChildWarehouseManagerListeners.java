package net.loyin.model.approve;

import java.util.ArrayList;
import java.util.List;
import net.loyin.model.sso.User;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
//船/站负责人
@SuppressWarnings("serial")
public class ChildWarehouseManagerListeners implements TaskListener{
	public static String child_warehouse_id="";
	@Override
	public void notify(DelegateTask paramDelegateTask) {
		List<String> useridList = new ArrayList<String>();
		List<User> list=User.dao.findChildWarehouseManager(child_warehouse_id);
		//船/站负责人为空时
		if(list!=null&&list.size()>0){
			for(int i=0;i<list.size();i++){
				useridList.add(list.get(i).getStr("id"));
			}
		}else{
			useridList.add("00123");
		}
		paramDelegateTask.addCandidateUsers(useridList);
	}
}