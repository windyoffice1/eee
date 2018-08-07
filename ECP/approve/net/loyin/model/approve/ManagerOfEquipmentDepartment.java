package net.loyin.model.approve;

import java.util.ArrayList;
import java.util.List;

import net.loyin.model.sso.User;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
/**
 * @author:lizhangyou
 * @Description:设备部门经理和副经理 </p>* 
 * @date 2018-6-23
 */
@SuppressWarnings("serial")
public class ManagerOfEquipmentDepartment implements TaskListener{
	@Override
	public void notify(DelegateTask paramDelegateTask) {
		List<String> useridList = new ArrayList<String>();
		List<User> list=User.dao.findManagerOfEquipmentDepartment();
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
