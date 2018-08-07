package net.loyin.ctrl.approve;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricIdentityLink;
import org.activiti.engine.history.HistoricVariableInstance;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.lang.StringUtils;
import com.jfinal.plugin.activerecord.Page;
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.approve.ApproveUtils;
import net.loyin.model.approve.TaskInfo;
import net.loyin.model.returnmaterial.ReturnMaterialApply;
import net.loyin.model.returnmaterial.ReturnMaterialApplyData;
import net.loyin.model.sso.Person;
@RouteBind(path="returnmaterialapplyapprove",sys="审批管理",model="退料审批")
public class RturnMaterialApproveCtrl  extends AdminBaseController<ReturnMaterialApply>{

	public RturnMaterialApproveCtrl(){
		this.modelClass=ReturnMaterialApply.class;
	}

	private ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	
	public void dataGrid() {
		List<ReturnMaterialApply> returnMaterialApplyList=new ArrayList<ReturnMaterialApply>();
	    //获取任务服务组件  
	    TaskQuery query = processEngine.getTaskService().createTaskQuery();
	    //指定个人任务查询，指定办理人
	    List<Task> tasks = query.taskCandidateUser(this.getCurrentUserId()).list();
	    for(Task task : tasks){
	    	String processInstanceID=task.getProcessInstanceId();
	    	//获取利流程定义
	    	ProcessDefinition pd=processEngine.getRepositoryService() // 获取service类  
	                  .createProcessDefinitionQuery() // 创建流程定义查询  
	                  .processDefinitionId(task.getProcessDefinitionId()) // 通过id查询  
	                  .singleResult(); // 查询返回当个结果  
	    	//判断是否是退料流程
	    	if("returnMaterialApplyProcess".equals(pd.getKey())){
		    	//根据流程实例id获取申请表内容
	    		ReturnMaterialApply returnMaterialApply= ReturnMaterialApply.dao.getReturnMaterialApplyByProcessID(processInstanceID);
		    	if(returnMaterialApply!=null){
		    		returnMaterialApplyList.add(returnMaterialApply);
		    	}
	    	}
	    }
	    Page<ReturnMaterialApply> page =new Page<ReturnMaterialApply>(returnMaterialApplyList, 1, 999999, 1, returnMaterialApplyList.size());
	    this.rendJson(true,null, "", page);
	}
	
	@SuppressWarnings("unused")
	@PowerBind(code={"A3_3_V"},funcName="查询")
	public void qryOp() {
		getId();
		ReturnMaterialApply returnMaterialApply = ReturnMaterialApply.dao.findById(id, this.getCompanyId());
		if (returnMaterialApply != null){
			List<ReturnMaterialApplyData> productlist = ReturnMaterialApplyData.dao.getDataList(id,returnMaterialApply.getStr("child_warehouse_id"));
		    String processInstanceId = returnMaterialApply.getStr("process_id");
		    //获取申请单的历史活动记录
		    List<HistoricActivityInstance> approveList = processEngine.getHistoryService()  
		            .createHistoricActivityInstanceQuery()  
		            .processInstanceId(processInstanceId)  
		            .list();
		    List<TaskInfo> taskInfoList=new ArrayList<TaskInfo>();
		    if(approveList != null && approveList.size()>0){ 
		    	int i=0;
		        for(HistoricActivityInstance hai : approveList){
		        	//是审批任务时
		        	if(hai.getActivityType()!=null&&hai.getActivityType().equals("userTask")){
		        		TaskInfo taskInfo = new TaskInfo();
		        		List<HistoricIdentityLink> approvrUserList=processEngine.getHistoryService()
				        		.getHistoricIdentityLinksForTask(hai.getTaskId());
			        	//设置已审核的人员
		        		for(HistoricIdentityLink user : approvrUserList){
		        			Map<String,Object> personInfo = Person.dao.qryById(user.getUserId());
				        	String user_name=(String) personInfo.get("realname");
				        	taskInfo.setUser_name(user_name);
		        		}
		        		//排除正在审核的人和申请人
			        	if(hai.getEndTime()!=null&&!hai.getActivityName().equals("退料申请")){
				        	String user_id=hai.getAssignee();
				        	//获取审批人姓名
			        		taskInfo.setTime(hai.getEndTime());
				        	taskInfo.setTaskid(hai.getId());
				        	taskInfo.setUser_id(user_id);
				        	taskInfo.setActivityName(hai.getActivityName());
 				        	//Map<String,Object> map = processEngine.getRuntimeService().getVariablesLocal(hai.getExecutionId());
 				        	//获取局部流程变量
 				        	List<HistoricVariableInstance> historicVariableInstances = processEngine.getHistoryService().createHistoricVariableInstanceQuery().taskId(hai.getTaskId()).list();
 				        	//查询驳回或批准
 				        	for(HistoricVariableInstance variable : historicVariableInstances){
 				        		if(variable.getVariableName().equals("批准or驳回")){
 				        			if(variable.getValue().equals("批准")){
 	 				        			taskInfo.setResult("通过");
 	 				        		}else if(variable.getValue().equals("驳回")){
 	 				        			taskInfo.setResult("驳回");
 	 				        		}
 				        		}
 				        	}
				        	List<Comment> taskCommentList = processEngine.getTaskService().getTaskComments(hai.getTaskId());
				        	if(taskCommentList!=null&& taskCommentList.size()>0){
				        		String comment=taskCommentList.get(0).getFullMessage();
				        		taskInfo.setComment(comment);
				        	}
				        	taskInfoList.add(taskInfo);
			        	}
		        	}
		            i++;
		        }  
		    }  
		    returnMaterialApply.put("productlist",productlist);
		    returnMaterialApply.put("productlistlength",productlist.size());
		    returnMaterialApply.put("taskInfoList", taskInfoList);
			this.rendJson(true,null, "", returnMaterialApply);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	
	/**
	 * 审核
	 */
	@PowerBind(code={"A3_3_E"},funcName="查询")
	public void approve(){
		try {
			getId();
			ReturnMaterialApply returnMaterialApply= (ReturnMaterialApply)getModel();
			Integer approve_user_status=this.getParaToInt("approve_user_status");
			String comment=this.getPara("approve_comment");//审核批注
			Map<String,Object> paranMap=new HashMap<String,Object>();
			// 查询任务  (根据流程实例查询任务)
	        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(returnMaterialApply.getStr("process_id")).singleResult(); 
	        //添加备注
	        if(StringUtils.isNotEmpty(comment)){
	        	processEngine.getTaskService().addComment(task.getId(), task.getProcessInstanceId(), comment);
	        }
			//通过审核
			if(approve_user_status!=null&&approve_user_status==1){
				paranMap.put("is_pass", "批准");
				processEngine.getTaskService().setVariableLocal(task.getId(), "批准or驳回", "批准");
				processEngine.getTaskService().complete(task.getId(),paranMap);
				//判断流程是否已经结束
				ProcessInstance pi = processEngine.getRuntimeService().createProcessInstanceQuery()//
						.processInstanceId(returnMaterialApply.getStr("process_id"))//使用流程实例ID查询
						.singleResult();
				if(pi==null){
					// "流程结束:批准";修改领料状态为待领料
					ReturnMaterialApply.dao.finishApprove(id, "2");
				}else{
					//return "未结束";
				}
			//驳回
			}else if(approve_user_status!=null&&approve_user_status==0){
				paranMap.put("is_pass", "驳回");
				processEngine.getTaskService().setVariableLocal(task.getId(), "批准or驳回", "驳回");
				processEngine.getTaskService().complete(task.getId(),paranMap);
				// "流程结束:驳回";
				ReturnMaterialApply.dao.finishApprove(id, "3");
			}
			this.rendJson(true,null, approve_user_status!=null&&approve_user_status==1 ? "通过成功！":"驳回成功！", id);
		} catch (Exception e) {
			e.printStackTrace();
			this.rendJson(true,null, "", "服务端错误！");
		}
	}
	
	//查看流程图
	public void getReturnMaterialApplyflow(){
		ApproveUtils ApproveUtils =new ApproveUtils();
		getId();
		ReturnMaterialApply returnMaterialApply = ReturnMaterialApply.dao.findById(id);
		 // 查询任务  (根据流程实例查询任务)
        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(returnMaterialApply.getStr("process_id")).singleResult();  
        ProcessDefinition pd = ApproveUtils.getProcessDefinitionByTaskId(task.getId());
        Map<String,Object> map=new HashMap<String,Object>();
        // 1. 获取流程部署ID
        map.put("deploymentId", pd.getDeploymentId());
        // 2. 获取流程图片的名称
        map.put("imageName",  pd.getDiagramResourceName());
        // 3.获取当前活动的坐标
        Map<String,Object> currentActivityCoordinates =ApproveUtils.getCurrentActivityCoordinates(task.getId());
        map.put("acs",  currentActivityCoordinates);
        this.rendJson(true,null,null,map);
	}
	
    public void viewImage(){
    	String deploymentId=this.getAttr("deploymentId");
    	String imageName=this.getAttr("imageName");
        InputStream  in = processEngine.getRepositoryService().getResourceAsStream(deploymentId, imageName);
        		/*getResourceAsStream.getImageStream(deploymentId,imageName);//此处方法实际项目应该放在service里面*/
        HttpServletResponse resp = this.getResponse();
        try {
            OutputStream out = resp.getOutputStream();
            // 把图片的输入流程写入resp的输出流中
            byte[] b = new byte[1024];
            for (int len = -1; (len= in.read(b))!=-1; ) {
                out.write(b, 0, len);
            }
            // 关闭流
            out.close();
            in.close();
            this.rendJson(true,null, "查询成功！", "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
