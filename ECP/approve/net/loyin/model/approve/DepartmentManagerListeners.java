package net.loyin.model.approve;

import java.util.ArrayList;
import java.util.List;

import net.loyin.model.sso.User;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
//部门经理
@SuppressWarnings("serial")
public class DepartmentManagerListeners implements TaskListener{
	public static String uid="";
	@Override
	public void notify(DelegateTask paramDelegateTask) {
		List<String> useridList = new ArrayList<String>();
		List<User> list=User.dao.findSupUser(uid);
		//上级经理为空时
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
