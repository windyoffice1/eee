package net.loyin.utils;

import java.util.Arrays;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyTaskListener implements TaskListener{

	@Override
	public void notify(DelegateTask task) {
		String [] candidateUsers={"developer"};  
		task.setVariable("apply", Arrays.asList(candidateUsers));  
		task.setVariable("departMentmanager", Arrays.asList(candidateUsers)); //部门经理
		task.setVariable(" deputy", Arrays.asList(candidateUsers)); //分管副总
		task.setVariable("CEO", Arrays.asList(candidateUsers)); //总经理
	}

}
