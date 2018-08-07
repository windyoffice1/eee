package net.loyin.ctrl.basicdata;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.Warehouselocation;


import com.jfinal.plugin.activerecord.Page;

@RouteBind(path="warehouselocation",sys="基础资料",model="仓位管理")
public class WarehouselocationCtrl extends AdminBaseController<Warehouselocation>{

	public WarehouselocationCtrl(){
		this.modelClass=Warehouselocation.class;
	}
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("admin",this.getPara("admin"));
		filter.put("warehouse_name",this.getPara("warehouse_name"));
		this.sortField(filter);
		Page<Warehouselocation> page = Warehouselocation.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "success", page);
	}
	
	@PowerBind(code="A8_4_V",funcName="查看")
	public void qryOp() {
		getId();
		Warehouselocation warehouselocation = Warehouselocation.dao.findById(id);
		Map<String,Object> data=new HashMap<String,Object>();
		if (warehouselocation != null){
			data.put("warehouselocation", warehouselocation);
			this.rendJson(true,null, "",data);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	
	
	@SuppressWarnings("unused")
	@PowerBind(code="A8_4_E",funcName="编辑")
	public void save() {
		try {
			getId();
			Warehouselocation po = (Warehouselocation) getModel();
			po.set("id",id);
			String uid=this.getCurrentUserId();
			this.pullUser(po, uid);
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			if (StringUtils.isEmpty(id)) {
				po.set("id", UUID.randomUUID().toString());
				po.save();
				id=po.getStr("id");
			} else {
				po.update();
			}
			Map<String,String> d=new HashMap<String,String>();
			d.put("id",id);
			this.rendJson(true,null, "操作成功！",d);
		} catch (Exception e) {
			log.error("保存产品异常", e);
			this.rendJson(false,null, "保存数据异常！");
		}
	}
	
	@PowerBind(code="A8_4_E",funcName="编辑")
	public void del() {
		try {
			id = getId();
			Warehouselocation.dao.del(id);
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	
	
	
}
