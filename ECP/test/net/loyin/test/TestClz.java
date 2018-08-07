package net.loyin.test;


import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.loyin.model.sso.Position;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class TestClz {

/*	public static void main(String[] args) {
		String s=null,s1="";
		System.out.println(
				StringUtils.isNoneEmpty(s)+
				"\t"+StringUtils.isNoneEmpty(s)+
				"\t"+StringUtils.isNotBlank(s)+
				"\t"+StringUtils.isNotEmpty(s)+
				
				"\n"+StringUtils.isNoneEmpty(s1)+
				"\t"+StringUtils.isNoneEmpty(s1)+
				"\t"+StringUtils.isNotBlank(s1)+
				"\t"+StringUtils.isNotEmpty(s1)+
				
				"\n"+StringUtils.isEmpty(s)+
				"\t"+StringUtils.isBlank(s)+
				"\n"+StringUtils.isEmpty(s1)+
				"\t"+StringUtils.isBlank(s1)
				);
		System.out.println(URLDecoder.decode("https://223.72.254.135/svn/CMMI/%E9%A1%B9%E7%9B%AE%E7%A0%94%E5%8F%91/NO_%E7%8E%B0%E5%AE%9E%E4%B8%80%E4%BD%93%E5%8C%96%E6%9C%8D%E5%8A%A1%E4%B8%8E%E7%AE%A1%E7%90%86%E5%B9%B3%E5%8F%B0/20_%E6%BA%90%E4%BB%A3%E7%A0%81/trunk"));
	}
*/
    /** 
     * 通过代码的方式创建Activiti数据表（共23张表） 
     */ 
	@Test
	public void createTable() { 
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration  
                .createStandaloneProcessEngineConfiguration();  
        // 连接数据库的配置  
        processEngineConfiguration.setJdbcDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        processEngineConfiguration  
                .setJdbcUrl("jdbc:sqlserver://192.168.31.8;databaseName=ecp"); 
        processEngineConfiguration.setJdbcUsername("mioto");  
        processEngineConfiguration.setJdbcPassword("mioto88888");  
        /**
         * public static final String DB_SCHEMA_UPDATE_FALSE =
         * "false";不能自动创建表，需要表存在 public static final String
         * DB_SCHEMA_UPDATE_CREATE_DROP = "create-drop";先删除表再创建表 public static
         * final String DB_SCHEMA_UPDATE_TRUE = "true";如果表不存在，自动创建表
         */
        processEngineConfiguration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
        // 工作流的核心对象，ProcessEnginee对象
        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();
        System.out.println("processEngine:" + processEngine);  
    }  
  
	public ProcessEngine getProcessEngine(){
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration  
                .createStandaloneProcessEngineConfiguration();  
        // 连接数据库的配置  
        processEngineConfiguration.setJdbcDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        processEngineConfiguration  
                .setJdbcUrl("jdbc:sqlserver://192.168.31.8;databaseName=ecp"); 
        processEngineConfiguration.setJdbcUsername("mioto");  
        processEngineConfiguration.setJdbcPassword("mioto88888");    
        // 工作流的核心对象，ProcessEnginee对象  
        ProcessEngine processEngine = processEngineConfiguration  
                .buildProcessEngine(); 
        return processEngine;
	}
	
	public void testTask() throws Exception { 
		 ProcessEngine processEngine = getProcessEngine(); 
	    // 1 发布流程  
/*	    InputStream inputStreamBpmn = this.getClass().getResourceAsStream("scm/net/loyin/diagram/采购申请.bpmn");  
	    InputStream inputStreamPng = this.getClass().getResourceAsStream("scm/net/loyin/diagram/采购申请.png");  
	    processEngine.getRepositoryService()//  
	                    .createDeployment()//  
	                    .addInputStream("采购申请.bpmn", inputStreamBpmn)//  
	                    .addInputStream("采购申请.png", inputStreamPng)//  
	                    .deploy();*/  
		 processEngine.getRepositoryService()
		 				.createDeployment()
		 				.addClasspathResource("net/loyin/diagram/returnMaterialApplyProcess.bpmn")
		 				.name("returnMaterialApplyProcess") 
		 				.addClasspathResource("net/loyin/diagram/returnMaterialApplyProcess.png")
		 				.deploy();
/*		 Map<String, Object> variables = new HashMap<String, Object>();  
		 variables.put("applyID", "00123");
		 variables.put("departmentManager", "00123");
		 variables.put("deputy", "00123");
		 variables.put("CEO", "00123");
		 variables.put("total_money",2);
	    // 2 启动流程  
	    //启动流程实例的同时，设置流程变量  
	    ProcessInstance pi = processEngine.getRuntimeService()//  
	                    .startProcessInstanceByKey("purchaseApplyProcess",variables);  
	    System.out.println("pid:" + pi.getId()); */ 
	}  
	
	public void startTask(){
		ProcessEngine processEngine = getProcessEngine(); 
		 Map<String, Object> variables = new HashMap<String, Object>();  
		 variables.put("applyID", "00123");
		 variables.put("departmentManager", "00123");
/*		 variables.put("deputy", "00123");
		 variables.put("CEO", "00123");*/
		 variables.put("total_money",300000);
	    // 2 启动流程  
	    //启动流程实例的同时，设置流程变量  
	    ProcessInstance pi = processEngine.getRuntimeService()//  
	                    .startProcessInstanceByKey("purchaseApplyProcess",variables);  
	    System.out.println("pid:" + pi.getId());  
	}
	
	public static void main(String[] args) throws Exception {
		TestClz t = new TestClz();
		t.testTask();
/*	   ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	   List<Model> list = processEngine.getRepositoryService().createModelQuery().list();
		Map<String, Object> variables = new HashMap<String, Object>();  
		variables.put("applyID", "00123"); 
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
	    ProcessInstance pi = processEngine.getRuntimeService()//  
                .startProcessInstanceByKey("purchaseApplyProcess",variables); //动态设置参与者 
	    @SuppressWarnings("unused")
		List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskAssignee("00123").list();  
		 */
	}
	
	@SuppressWarnings("unused")
	public void getTask(){
		 ProcessEngine engine = getProcessEngine();  
	        //获取任务服务组件  
	     TaskService taskService = engine.getTaskService();  
	     TaskQuery query = taskService.createTaskQuery();
	     //指定个人任务查询，指定办理人
	     List<Task> tasks = query.taskCandidateUser("d24770df-ab69-47ba-a4bc-4a3493afb05a").list();//departmentManager
		  /* for (Task task : tasks){
			  // System.out.println(task.getName());
		　　}*/
	  // 根据taskName和流程节点中的名字判断当前节点之后是否还有任务  
        Task task1 = taskService.createTaskQuery().taskId(tasks.get(0).getId())  
                	.singleResult();  
        String taskName = task1.getName();
        Map<String, Object> s= task1.getTaskLocalVariables();
 
		for(Task task : tasks){
			System.out.println(task.getName());
			System.out.println(task.getId());
		}
	}
	
	/**完成个人任务*/
	public void completeTaskTest(){
		ProcessEngine engine = getProcessEngine();
	    //任务ID
	    String taskId = "1104";//1008,1108
		Map<String, Object> variables = new HashMap<String, Object>();  
		variables.put("is_pass","批准");
		engine.getTaskService().addComment(taskId, null, "很好，通过");
		engine.getTaskService()
	                .complete(taskId,variables);
	    System.out.println("完成任务：任务ID："+taskId);
	}
	
	/**查询历史流程实例*/  
	public void findHisProcessInstance(){
		ProcessEngine processEngine = getProcessEngine();
	    List<HistoricProcessInstance> list = processEngine.getHistoryService()  
	            .createHistoricProcessInstanceQuery()  
	            .processDefinitionId("1843")//流程定义ID  
	            .list();  
	      
	    if(list != null && list.size()>0){  
	        for(HistoricProcessInstance hi:list){  
	            System.out.println(hi.getId()+"   "+hi.getStartTime()+"   "+hi.getEndTime());  
	        }  
	    }  
	}  
	
	/**查询历史活动 
	 * 问题：HistoricActivityInstance对应哪个表 
	 * 问题：HistoricActivityInstance和HistoricTaskInstance有什么区别*/  
	public void findHisActivitiList(){ 
		ProcessEngine processEngine = getProcessEngine();
	    String processInstanceId = "1228";  
	    List<HistoricActivityInstance> list = processEngine.getHistoryService()  
	            .createHistoricActivityInstanceQuery()  
	            .processInstanceId(processInstanceId)  
	            .list();  
	    if(list != null && list.size()>0){  
	        for(HistoricActivityInstance hai : list){  
	            System.out.println(hai.getId()+"  "+hai.getActivityName());
	            @SuppressWarnings("unused")
				List<Comment> taskList = processEngine.getTaskService().getTaskComments(hai.getId());
	        }  
	    }  
	} 
	
	
	public void viewImage() throws Exception {
		  // 创建仓库服务对对象
		ProcessEngine processEngine = getProcessEngine();
		RepositoryService repositoryService = processEngine.getRepositoryService();
		  // 从仓库中找需要展示的文件
		String deploymentId = "1";//部署流程id
		List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);
		String imageName = null;
		for (String name : names) {
			if(name.indexOf(".png")>=0){
				imageName = name;
		   }
		}
		if(imageName!=null){
		//		     System.out.println(imageName);
		File f = new File("D:/"+ imageName);
		   // 通过部署ID和文件名称得到文件的输入流
			 InputStream in =  repositoryService.getResourceAsStream(deploymentId, imageName);
			 FileUtils.copyInputStreamToFile(in, f);
		}
	}
	
	@SuppressWarnings("unused")
	public void t(){
		ProcessEngine processEngine = getProcessEngine();
		
	    List<HistoricActivityInstance> approveList = processEngine.getHistoryService()  
	            .createHistoricActivityInstanceQuery()  
	            .processInstanceId("1461")  
	            .list();
	    
/*		List<HistoricIdentityLink> approvrUserList=processEngine.getHistoryService()
        		.getHistoricIdentityLinksForTask(hai.getTaskId());*/
	}
	
	public void t2(){
        Position position = new Position();
        List<String> useridList = position.getUseridByPositionName("总经理");
        System.err.println(useridList.get(0));
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}