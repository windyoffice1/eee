package net.loyin.test;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;

public class test02 {

	ProcessEngine processEngine =getProcessEngine(); 
	public void testTask() throws Exception { 
	      
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
	} 
	  
	public ProcessEngine getProcessEngine(){
        ProcessEngineConfiguration processEngineConfiguration = ProcessEngineConfiguration  
                .createStandaloneProcessEngineConfiguration();  
        // 连接数据库的配置  
        processEngineConfiguration.setJdbcDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");  
        processEngineConfiguration  
                .setJdbcUrl("jdbc:sqlserver://192.168.31.8:1433;databaseName=ecp"); 
        processEngineConfiguration.setJdbcUsername("mioto");  
        processEngineConfiguration.setJdbcPassword("mioto88888");  
        // Activiti的23个表不存在则自动创建  
        processEngineConfiguration  
                .setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);  
        // 工作流的核心对象，ProcessEnginee对象  
        ProcessEngine processEngine = processEngineConfiguration  
                .buildProcessEngine(); 
        return processEngine;
	}
	
/*	public void startTask(){
		
		 Map<String, Object> variables = new HashMap<String, Object>();  
		 variables.put("applyID", "00123");
		 variables.put("departmentManager", "00123");
		 variables.put("deputy", "00123");
		 variables.put("CEO", "00123");
		 variables.put("total_money",300000);
	    // 2 启动流程  
	    //启动流程实例的同时，设置流程变量  
	    ProcessInstance pi = processEngine.getRuntimeService()//  
	                    .startProcessInstanceByKey("purchaseApplyProcess",variables);  
	    System.out.println("pid:" + pi.getId());  
	}*/
	
	public static void main(String[] args) throws Exception {
		test02 t = new test02();
		t.testTask();

  
	}
	
}
