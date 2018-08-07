package net.loyin.ctrl.scm;

import java.util.HashMap;
import java.util.Map;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.crm.Customer;

/**
 * 销售目标
 * 
 * @author 龙影 2014年10月21日
 */
@SuppressWarnings("rawtypes")
@RouteBind(path = "saleGoal")
public class SaleGoalCtrl extends AdminBaseController {
	public void rptList() {
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("uid",this.getPara("uid"));
		filter.put("qryType",this.getParaToInt("qryType"));
		filter.put("start_date",this.getPara("start_date"));
		filter.put("end_date",this.getPara("end_date"));
		filter.put("company_id",this.getCompanyId());
		this.rendJson(true,null,"",Customer.dao.performance(filter));
	}
}
