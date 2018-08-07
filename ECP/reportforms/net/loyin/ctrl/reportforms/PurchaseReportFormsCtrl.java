package net.loyin.ctrl.reportforms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.reportforms.PurchaseReportForms;
/**
 * 
 * @author:lizhangyou
 * @Description: 报表</p>* 
 * @date 2017-10-10
 */
@RouteBind(path="purchase",sys="表报",model="采购报表")
public class PurchaseReportFormsCtrl extends AdminBaseController<PurchaseReportForms> {

	public PurchaseReportFormsCtrl(){
		this.modelClass=PurchaseReportForms.class;
	}
	/**
	 * 采购明细表
	 */
	public void purchaseReportFormsDetial(){
		Map<String,Object> paramsMap=new HashMap<String,Object>();
		//全部[0:否,1:是]
		paramsMap.put("is_all", this.getPara("is_all"));
		//供应商
		paramsMap.put("customer_name", this.getPara("customer_name"));
		//物料类型
		paramsMap.put("material_type_id", this.getPara("material_type_id"));
		//物料大类
		paramsMap.put("material_broad_id", this.getPara("material_broad_id"));
		//物料编号
		paramsMap.put("material_data_no", this.getPara("material_data_no"));
		//物料名称
		paramsMap.put("material_data_name", this.getPara("material_data_name"));
		//开始时间
		paramsMap.put("start_date", this.getPara("start_date"));
		//结束时间
		paramsMap.put("end_date", this.getPara("end_date"));
		
		List<PurchaseReportForms> list=PurchaseReportForms.dao.getPurchaseDetials(paramsMap);
		this.rendJson(true,null, "", list);
	}
	/**
	 * 采购汇总表（按物料）
	 */
	public void purchaseReportFormsByMaterial(){
		Map<String,Object> paramsMap=new HashMap<String,Object>();
		//全部[0:否,1:是]
		paramsMap.put("is_all", this.getPara("is_all"));
		//供应商
		paramsMap.put("customer_name", this.getPara("customer_name"));
		//物料类型
		paramsMap.put("material_type_id", this.getPara("material_type_id"));
		//物料大类
		paramsMap.put("material_broad_id", this.getPara("material_broad_id"));
		//物料编号
		paramsMap.put("material_data_no", this.getPara("material_data_no"));
		//物料名称
		paramsMap.put("material_data_name", this.getPara("material_data_name"));
		//开始时间
		paramsMap.put("start_date", this.getPara("start_date"));
		//结束时间
		paramsMap.put("end_date", this.getPara("end_date"));
		
		List<PurchaseReportForms> list=PurchaseReportForms.dao.purchaseReportFormsByMaterial(paramsMap);
		this.rendJson(true,null, "", list);
	}
	
	/**
	 * 采购汇总（按供应商）
	 */
	public void purchaseReportFormsByCustomer(){
		Map<String,Object> paramsMap=new HashMap<String,Object>();
		//全部[0:否,1:是]
		paramsMap.put("is_all", this.getPara("is_all"));
		//供应商
		paramsMap.put("customer_name", this.getPara("customer_name"));
		//物料类型
		paramsMap.put("material_type_id", this.getPara("material_type_id"));
		//物料大类
		paramsMap.put("material_broad_id", this.getPara("material_broad_id"));
		//物料编号
		paramsMap.put("material_data_no", this.getPara("material_data_no"));
		//物料名称
		paramsMap.put("material_data_name", this.getPara("material_data_name"));
		//开始时间
		paramsMap.put("start_date", this.getPara("start_date"));
		//结束时间
		paramsMap.put("end_date", this.getPara("end_date"));
		List<PurchaseReportForms> list=PurchaseReportForms.dao.purchaseReportFormsByCustomer(paramsMap);
		this.rendJson(true,null, "", list);
	}
}
