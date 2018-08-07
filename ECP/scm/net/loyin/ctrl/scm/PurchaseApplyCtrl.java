package net.loyin.ctrl.scm;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang.StringUtils;
import com.jfinal.plugin.activerecord.Page;
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.approve.DepartmentManagerListeners;
import net.loyin.model.scm.PurchaseApply;
import net.loyin.model.scm.PurchaseApplyData;
import net.loyin.model.sso.Person;
import net.loyin.util.GenerateNumberingUtil;
/**
 * 
 * @author:lizhangyou
 * @Description: 采购申请</p>* 
 * @date 2017-9-18
 */
@RouteBind(path="purchaseapply",sys="采购",model="采购申请")
public class PurchaseApplyCtrl   extends AdminBaseController<PurchaseApply>{
	
	public PurchaseApplyCtrl(){
		this.modelClass=PurchaseApply.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//查询出当前人创建的采购申请
		filter.put("creater_id", userMap.get("uid"));
		//是否已删除状态
		String is_deleted=this.getPara("is_deleted");
		if(StringUtils.isBlank(is_deleted)){
			filter.put("is_deleted","0");
		}else{
			filter.put("is_deleted",is_deleted);
		}
		//采购编号
		String purchase_no=this.getPara("purchase_no").trim();
		filter.put("purchase_no",purchase_no);
		//审批状态
		filter.put("approve_status",this.getPara("approve_status"));
		this.sortField(filter);
		Page<PurchaseApply> page = PurchaseApply.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	@PowerBind(code={"A4_1_V"},funcName="查询")
	public void qryOp() {
		getId();
		PurchaseApply purchaseApply = PurchaseApply.dao.findById(id, this.getCompanyId());
		if (purchaseApply != null){
			List<PurchaseApplyData> productlist=PurchaseApplyData.dao.list(id);
			purchaseApply.put("productlist",productlist);
			purchaseApply.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", purchaseApply);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	
	/**
	 * 新增或修改采购申请
	 */	
	@PowerBind(code={"A4_1_E"},funcName="编辑")
	public void save() {
		try {
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			PurchaseApply po = getModel();
			System.err.println(po.toJson());
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			getId();
			String company_id=this.getCompanyId();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String uuid = UUID.randomUUID().toString();
			String pname="productlist";
			this.pullUser(po, this.getCurrentUserId());
			if (StringUtils.isEmpty(id)) {
				po.set("id", uuid);
				po.set("purchase_no", GenerateNumberingUtil.makeOrderNum("CGSQ"));//生成申请单号
				po.set("approve_status", "0");//审批状态设为未提交
				po.set("purchase_name", perMap.get("realname")+"的采购申请"+dateTimeFormat1.format(new Date()));//自动生成采购申请单名称
				po.set("creater_username",perMap.get("realname"));
				po.set("create_time", sdf.format(new Date()));
				po.set("creater_id", this.getUserMap().get("uid"));
				po.set("company_id",company_id);
				po.set("is_deleted",0);
				po.save();
			} else {
				po.set("update_id", this.getCurrentUserId());
				po.set("update_time",dateTimeFormat.format(new Date()) );
				po.update();
			}
			Integer productlistlength=this.getParaToInt("productlistlength");
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				attr.put("purchase_apply_id",po.get("id"));
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				attr.put("material_data_name",this.getAttr(pname+"["+i+"][material_data_name]"));
				attr.put("target_price",this.getAttr(pname+"["+i+"][target_price]"));
				attr.put("float_price",this.getAttr(pname+"["+i+"][float_price]"));
				attr.put("unit",this.getAttr(pname+"["+i+"][unit]"));
				attr.put("amount",this.getAttr(pname+"["+i+"][amount]"));
				attr.put("existing_amount",this.getAttr(pname+"["+i+"][existing_amount]"));
				attr.put("description",this.getAttr(pname+"["+i+"][description]"));
				attr.put("total_money",this.getAttr(pname+"["+i+"][total_money]"));
				productlist.add(attr);
			}
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			data.put("purchase_no",po.getStr("purchase_no"));
			PurchaseApplyData.dao.insert(productlist);
			this.rendJson(true,null, "操作成功！",data);
		} catch (Exception e) {
			log.error("保存订单异常", e);
			this.rendJson(false,null, "保存数据异常！");
		}
	}
	
	@PowerBind(code={"A4_1_E"},funcName="编辑")
	public void del(){
		try {
			getId();
			PurchaseApply.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	
	/**提交审核/取消提交*/
	@PowerBind(code={"A4_1_E"},funcName="编辑")
	public void subAudit() {
/*		String total_money=this.getAttrForStr("total_money");*/
		int status=this.getParaToInt("status");
		String company_id=this.getCompanyId();
		getId();
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		//在流程中设置当前登录对象
		DepartmentManagerListeners.uid=this.getCurrentUserId();
		//提交审核
		if(status==1){
			Map<String, Object> variables = new HashMap<String, Object>();  
			variables.put("applyID", this.getCurrentUserId());
//			variables.put("departmentManager", this.getCurrentUserId());
//			variables.put("deputy", this.getCurrentUserId());
//			variables.put("CEO", this.getCurrentUserId());
/*			variables.put("total_money",total_money);	//设置排他网关值   */	
	    	ProcessInstance pi = processEngine.getRuntimeService()//  
	                .startProcessInstanceByKey("purchaseApplyProcess",variables); //动态设置参与者 与参与条件
//		    List<Task> tasks = processEngine.getTaskService().createTaskQuery().taskAssignee(this.getCurrentUserId()).list(); 
		    // 查询任务  (根据流程实例查询任务)
	        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();  
	        processEngine.getTaskService().complete(task.getId());
			PurchaseApply.dao.subAudit(id,this.getCompanyId(),this.getCurrentUserId(),dateTimeFormat.format(new Date()),status, pi.getProcessInstanceId());
		//取消审核	
		}else if(status==0){
			//根据申请单id获取申请单实例
			PurchaseApply purchaseApply = PurchaseApply.dao.findById(id, company_id);
		    String processInstanceId = purchaseApply.getStr("process_id");  
		    List<HistoricActivityInstance> list = processEngine.getHistoryService()  
		            .createHistoricActivityInstanceQuery()  
		            .processInstanceId(processInstanceId)  
		            .list();
		    for(int i=0 ; i<list.size();i++){
			    if(StringUtils.isNotEmpty(list.get(i).getActivityName())&& list.get(i).getActivityName().equals("采购申请")){
			    	//不是申请人不能取消审核
				    if(list != null && list.size()>0 && list.get(i).getAssignee().equalsIgnoreCase(this.getCurrentUserId())){
//				    	HistoricProcessInstance hpi = processEngine.getHistoryService()  
//				    	            .createHistoricProcessInstanceQuery()  
//				    	            .processInstanceId(processInstanceId)  
//				    	            .singleResult(); 
				        processEngine.getRuntimeService().deleteProcessInstance(processInstanceId, "主动取消审核");
				        PurchaseApply.dao.cancelApprove(id);
				    } 
			    }else{
			    	//只有提交申请的人员才可以取消审核
			    	rendJson(false,null,"取消审核失败,只有提交申请的人员才可以取消审核！",id);
			    }
		    }
		}
		rendJson(true,null,(status==1?"提交审核":"取消审核申请")+"成功！",id);
	}
}
