package net.loyin.ctrl.sso;

import java.util.HashMap;
import java.util.Map;

import org.hyperic.sigar.pager.Pager;

import com.jfinal.plugin.activerecord.Page;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.sso.ActionLog;
/**
 * 
 * @author 龙影
 * 2014年10月30日
 */
@RouteBind(sys="设置",path="operlog",model="操作日志")
public class ActionLogCtrl extends AdminBaseController<ActionLog> {

	public ActionLogCtrl(){
		this.modelClass = ActionLog.class;
	}
	
	public void dataGrid(){
		Map<String,String>userMap=this.getUserMap();
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("company_id",userMap.get("company_id"));
		filter.put("uname",this.getPara("uname"));
		filter.put("start_date",this.getPara("beginDate"));
		filter.put("end_date",this.getPara("endDate"));
		filter.put("uid",this.getPara("uid"));
		filter.put("model_name",this.getPara("model_name"));
		this.sortField(filter);
		Page<ActionLog> page= ActionLog.dao.page(this.getPageNo(),this.getPageSize(),filter,this.getParaToInt("qryType",0));
		this.rendJson(true, null, "",page);
	}
}
