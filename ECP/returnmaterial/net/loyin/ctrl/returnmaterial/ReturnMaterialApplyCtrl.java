package net.loyin.ctrl.returnmaterial;

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
import net.loyin.model.approve.ChildWarehouseManagerListeners;
import net.loyin.model.approve.DepartmentManagerListeners;
import net.loyin.model.returnmaterial.ReturnMaterialApply;
import net.loyin.model.returnmaterial.ReturnMaterialApplyData;
import net.loyin.model.sso.Person;
import net.loyin.util.GenerateNumberingUtil;

@RouteBind(path="returnmaterialapply",sys="退料管理",model="退料申请")
public class ReturnMaterialApplyCtrl extends AdminBaseController<ReturnMaterialApply>{
	public ReturnMaterialApplyCtrl(){
		this.modelClass=ReturnMaterialApply.class;
	}
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//查询出当前人创建的退料申请
		filter.put("create_user_id", userMap.get("uid"));
		//是否已删除状态
		String is_deleted=this.getPara("is_deleted");
		if(StringUtils.isBlank(is_deleted)){
			filter.put("is_deleted","0");
		}else{
			filter.put("is_deleted",is_deleted);
		}
		//退料单编号
		String returnmaterial_no=this.getPara("returnmaterial_no").trim();
		filter.put("returnmaterial_no",returnmaterial_no);
		//退料单名称
		String returnmaterial_name=this.getPara("returnmaterial_name").trim();
		filter.put("returnmaterial_name",returnmaterial_name);
		//审批状态
		filter.put("approve_status",this.getPara("approve_status"));
		this.sortField(filter);
		
		Page<ReturnMaterialApply> page = ReturnMaterialApply.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	@PowerBind(code={"A5_1_V"},funcName="查询")
	public void qryOp(){
		getId();
		ReturnMaterialApply returnMaterialApply = ReturnMaterialApply.dao.findById(id,this.getCompanyId());
		if(returnMaterialApply!=null){
			//通过领料单查询领料单的物料信息
			List<ReturnMaterialApplyData> list =ReturnMaterialApplyData.dao.getDataList(id,returnMaterialApply.getStr("child_warehouse_id"));
			returnMaterialApply.put("productlist", list);
			returnMaterialApply.put("productlistlength", list.size());
			this.rendJson(true, null, "", returnMaterialApply);
		}else
			this.rendJson(false,null, "记录不存在！");
	}
	
	/**
	 * 新增或修改领料申请
	 */	
	@PowerBind(code={"A5_1_E"},funcName="编辑")
	public void save() {
		try {
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			ReturnMaterialApply po = (ReturnMaterialApply) getModel();
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
				po.set("returnmaterial_no", GenerateNumberingUtil.makeOrderNum("TLSQ"));//生成申请单号
				po.set("approve_status", "0");//审批状态设为未提交
				po.set("returnmaterial_name", perMap.get("realname")+"的退料申请"+dateTimeFormat1.format(new Date()));//自动生成退料申请单名称
				po.set("create_user_name",perMap.get("realname"));
				po.set("create_user_id", this.getUserMap().get("uid"));
				po.set("create_time", sdf.format(new Date()));
				po.set("company_id",company_id);
				po.set("is_deleted",0);
				po.set("is_deleted_putinstorage","0");
				po.save();
			} else {
				po.set("update_user_id", this.getCurrentUserId());
				po.set("update_time",dateTimeFormat.format(new Date()) );
				po.update();
			}
			Integer productlistlength=this.getParaToInt("productlistlength");
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				attr.put("returnmaterial_apply_id",po.get("id"));
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				attr.put("target_price",this.getAttr(pname+"["+i+"][target_price]"));
				attr.put("amount",this.getAttr(pname+"["+i+"][amount]"));
				attr.put("comment",this.getAttr(pname+"["+i+"][comment]"));
				attr.put("total_money",this.getAttr(pname+"["+i+"][total_money]"));
				//是否合格
				attr.put("is_qualified",this.getAttr(pname+"["+i+"][is_qualified]"));
				productlist.add(attr);
			}
			ReturnMaterialApplyData.dao.insert(productlist);
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			data.put("returnmaterial_no",po.getStr("returnmaterial_no"));
			this.rendJson(true,null, "操作成功！",data);
		} catch (Exception e) {
			log.error("保存退料异常", e);
			this.rendJson(false,null, "保存数据异常！");
		}
	}
	
	@PowerBind(code={"A5_1_E"},funcName="编辑")
	public void del(){
		try {
			getId();
			ReturnMaterialApply.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	
	/**提交审核/取消提交*/
	@SuppressWarnings("static-access")
	@PowerBind(code={"A5_1_E"},funcName="编辑")
	public void subAudit() {
		getId();
		//总金额
		String total_money=this.getPara("total_money");
		//船/站负责人
		String child_warehouse_id=this.getAttrForStr("child_warehouse_id");
		int status=this.getParaToInt("status");
		String company_id=this.getCompanyId();
		ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
		ChildWarehouseManagerListeners childWarehouseManagerListeners = new ChildWarehouseManagerListeners();
		//在流程中设置当前船/站id对象
		childWarehouseManagerListeners.child_warehouse_id=child_warehouse_id;
		//在流程中设置当前登录对象
		DepartmentManagerListeners.uid=this.getCurrentUserId();
		//提交审核
		if(status==1){
			Map<String, Object> variables = new HashMap<String, Object>();  
			variables.put("applyID", this.getCurrentUserId());
			variables.put("total_money",total_money);	//设置排他网关值
		    ProcessInstance pi = processEngine.getRuntimeService()//  
	                .startProcessInstanceByKey("returnMaterialApplyProcess",variables); //动态设置参与者 与参与条件
		    // 查询任务  (根据流程实例查询任务)
	        Task task = processEngine.getTaskService().createTaskQuery().processInstanceId(pi.getProcessInstanceId()).singleResult();  
	        processEngine.getTaskService().complete(task.getId());
	        ReturnMaterialApply.dao.subAudit(id,this.getCompanyId(),this.getCurrentUserId(),dateTimeFormat.format(new Date()),status, pi.getProcessInstanceId());
		//取消审核	
		}else if(status==0){
			//根据申请单id获取申请单实例
			ReturnMaterialApply returnMaterialApply = ReturnMaterialApply.dao.findById(id, company_id);
		    String processInstanceId = returnMaterialApply.getStr("process_id");  
		    List<HistoricActivityInstance> list = processEngine.getHistoryService()  
		            .createHistoricActivityInstanceQuery()  
		            .processInstanceId(processInstanceId)  
		            .list();
		    for(int i=0 ; i<list.size();i++){
			    if(StringUtils.isNotEmpty(list.get(i).getActivityName())&& list.get(i).getActivityName().equals("退料申请")){
			    	//不是申请人不能取消审核
				    if(list != null && list.size()>0 && list.get(i).getAssignee().equalsIgnoreCase(this.getCurrentUserId())){
				        processEngine.getRuntimeService().deleteProcessInstance(processInstanceId, "主动取消审核");
				        returnMaterialApply.dao.cancelApprove(id);
				    }
			    }else{
			    	//只有提交申请的人员才可以取消审核
			    	//rendJson(true,null,"取消审核失败,只有提交申请的人员才可以取消审核！",id);
			    }
		    }
		}
		rendJson(true,null,(status==1?"提交审核":"取消审核申请")+"成功！",id);
	}
}
