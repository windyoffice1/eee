package net.loyin.model.approve;

import java.util.List;
import net.loyin.model.sso.Position;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
//总经理
@SuppressWarnings("serial")
public class CEOListeners implements TaskListener{

	@Override
	public void notify(DelegateTask paramDelegateTask) {
	    //添加审批的人员，以下任何一人通过即可进入下一环节
        Position position = new Position();
        List<String> useridList = position.getUseridByPositionName("总经理");
        paramDelegateTask.addCandidateUsers(useridList);
	}
}
