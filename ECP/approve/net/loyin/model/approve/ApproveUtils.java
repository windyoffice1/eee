package net.loyin.model.approve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.RepositoryServiceImpl;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.persistence.entity.TaskEntity;
import org.activiti.engine.impl.pvm.PvmTransition;
import org.activiti.engine.impl.pvm.process.ActivityImpl;
import org.activiti.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;



public class ApproveUtils{
	
	
	private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	private TaskService taskService = processEngine.getTaskService();
	protected RepositoryService repositoryService = processEngine.getRepositoryService(); 
	
    /** 
     * @param taskId 
     *            当前任务ID 
     * @param variables 
     *            流程变量 
     * @param activityId 
     *            流程转向执行任务节点ID<br> 
     *            此参数为空，默认为提交操作 
     * @throws Exception 
     */  
    public void commitProcess(String taskId, Map<String, Object> variables,  
            String activityId) throws Exception { 
        if (variables == null) {  
            variables = new HashMap<String, Object>();  
        }  
        // 跳转节点为空，默认提交操作  
        if (!StringUtils.isNotEmpty(activityId)) {  
        	processEngine.getTaskService().complete(taskId, variables);  
        } else {// 流程转向操作  
            turnTransition(taskId, activityId, variables);  
        }  
    } 
    
    /** 
     * 流程转向操作 
     * @param taskId 
     *            当前任务ID 
     * @param activityId 
     *            目标节点任务ID 
     * @param variables 
     *            流程变量 
     * @throws Exception 
     */  
    private void turnTransition(String taskId, String activityId,  
            Map<String, Object> variables) throws Exception {  
        // 当前节点  
        ActivityImpl currActivity = findActivitiImpl(taskId, null);  
        // 清空当前流向  
        List<PvmTransition> oriPvmTransitionList = clearTransition(currActivity);  
        // 创建新流向  
        TransitionImpl newTransition = currActivity.createOutgoingTransition();  
        // 目标节点  
        ActivityImpl pointActivity = findActivitiImpl(taskId, activityId);  
        // 设置新流向的目标节点  
        newTransition.setDestination(pointActivity);  
        // 执行转向任务  
        taskService.complete(taskId, variables);  
        // 删除目标节点新流入  
        pointActivity.getIncomingTransitions().remove(newTransition);  
        // 还原以前流向  
        restoreTransition(currActivity, oriPvmTransitionList);  
    }  
    
    /** 
     * 根据任务ID和节点ID获取活动节点 <br> 
     * @param taskId 
     *            任务ID 
     * @param activityId 
     *            活动节点ID <br> 
     *            如果为null或""，则默认查询当前活动节点 <br> 
     *            如果为"end"，则查询结束节点 <br> 
     * @return 
     * @throws Exception 
     */  
    private ActivityImpl findActivitiImpl(String taskId, String activityId)  
            throws Exception {  
        // 取得流程定义  
        ProcessDefinitionEntity processDefinition = findProcessDefinitionEntityByTaskId(taskId);  
        // 获取当前活动节点ID  
        if (!StringUtils.isNotEmpty(activityId)) {  
            activityId = findTaskById(taskId).getTaskDefinitionKey();  
        }  
        // 根据流程定义，获取该流程实例的结束节点  
        if (activityId.toUpperCase().equals("END")) {  
            for (ActivityImpl activityImpl : processDefinition.getActivities()) {  
                List<PvmTransition> pvmTransitionList = activityImpl  
                        .getOutgoingTransitions();  
                if (pvmTransitionList.isEmpty()) {  
                    return activityImpl;  
                }  
            }  
        }  
        // 根据节点ID，获取对应的活动节点  
        ActivityImpl activityImpl = ((ProcessDefinitionImpl) processDefinition)  
                .findActivity(activityId);  
        return activityImpl;  
    } 
    
    /** 
     * 清空指定活动节点流向 
     * @param activityImpl 
     *            活动节点 
     * @return 节点流向集合 
     */  
    private List<PvmTransition> clearTransition(ActivityImpl activityImpl) {  
        // 存储当前节点所有流向临时变量  
        List<PvmTransition> oriPvmTransitionList = new ArrayList<PvmTransition>();  
        // 获取当前节点所有流向，存储到临时变量，然后清空  
        List<PvmTransition> pvmTransitionList = activityImpl  
                .getOutgoingTransitions();  
        for (PvmTransition pvmTransition : pvmTransitionList) {  
            oriPvmTransitionList.add(pvmTransition);  
        }  
        pvmTransitionList.clear();  
        return oriPvmTransitionList;  
    }  
  
    /** 
     * 根据任务ID获取流程定义 
     * @param taskId 
     *            任务ID 
     * @return 
     * @throws Exception 
     */  
    private ProcessDefinitionEntity findProcessDefinitionEntityByTaskId(  
            String taskId) throws Exception {  
        // 取得流程定义  
        ProcessDefinitionEntity processDefinition = (ProcessDefinitionEntity) ((RepositoryServiceImpl) repositoryService)  
                .getDeployedProcessDefinition(findTaskById(taskId)  
                        .getProcessDefinitionId());  
        if (processDefinition == null) {  
            throw new Exception("流程定义未找到!");  
        }  
        return processDefinition;  
    } 
	
    /** 
     * 根据任务ID获得任务实例 
     * @param taskId 
     *            任务ID 
     * @return 
     * @throws Exception 
     */  
    private TaskEntity findTaskById(String taskId) throws Exception {  
        TaskEntity task = (TaskEntity) taskService.createTaskQuery().taskId(  
                taskId).singleResult();  
        if (task == null) {  
            throw new Exception("任务实例未找到!");  
        }  
        return task;  
    } 
    
    /** 
     * 还原指定活动节点流向 
     * @param activityImpl 
     *            活动节点 
     * @param oriPvmTransitionList 
     *            原有节点流向集合 
     */  
    private void restoreTransition(ActivityImpl activityImpl,  
            List<PvmTransition> oriPvmTransitionList) {  
        // 清空现有流向  
        List<PvmTransition> pvmTransitionList = activityImpl  
                .getOutgoingTransitions();  
        pvmTransitionList.clear();  
        // 还原以前流向  
        for (PvmTransition pvmTransition : oriPvmTransitionList) {  
            pvmTransitionList.add(pvmTransition);  
        }  
    } 
    
    public ProcessDefinition getProcessDefinitionByTaskId(String taskId) {
        // 1. 得到task
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 2. 通过task对象的pdid获取流程定义对象
        ProcessDefinition pd = repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        return pd;
    }
    
    public Map<String, Object> getCurrentActivityCoordinates(String taskId) {
    	ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        Map<String, Object> coordinates = new HashMap<String, Object>();
        // 1. 获取到当前活动的ID
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        ProcessInstance pi = processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        String currentActivitiId = pi.getActivityId();
        // 2. 获取到流程定义
        ProcessDefinitionEntity pd = (ProcessDefinitionEntity) repositoryService.getProcessDefinition(task.getProcessDefinitionId());
        // 3. 使用流程定义通过currentActivitiId得到活动对象
        ActivityImpl activity =  pd.findActivity(currentActivitiId);
        // 4. 获取活动的坐标
        coordinates.put("x", activity.getX());
        coordinates.put("y", activity.getY());
        coordinates.put("width", activity.getWidth());
        coordinates.put("height", activity.getHeight());
        //如果有多个流程活动节点（并发流程一般有多个活动节点）该方法应该返回一个list，代码应该使用下面的方法
        // 得到流程执行对象
/*        List<Execution> executions = runtimeService.createExecutionQuery()
                .processInstanceId(pi.getId()).list();
        // 得到正在执行的Activity的Id
        List<String> activityIds = new ArrayList<String>();
        for (Execution exe : executions) {
            List<String> ids = runtimeService.getActiveActivityIds(exe.getId());
            activityIds.addAll(ids);
        }
        List<Map<String, Integer>> list = new ArrayList<Map<String, Integer>>();
        for (String id : activityIds) {
            ActivityImpl activity1 = pd.findActivity(id);
            Map<String, Integer> map = new HashMap<String, Integer>();
            map.put("x", activity1.getX());
            map.put("y", activity1.getY());
            map.put("width", activity1.getWidth());
            map.put("height", activity1.getHeight());
            list.add(map);
        }*/
        return coordinates;
    }
}
