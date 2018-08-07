package net.loyin.ctrl.basicdata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.plugin.activerecord.Page;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.ChildWarehouse;
import net.loyin.model.sso.Position;

@RouteBind(path="childwarehouse",sys="基础资料",model="分公司管理")
public class ChildWarehouseCtrl   extends AdminBaseController<ChildWarehouse>{
	public ChildWarehouseCtrl(){
		this.modelClass=ChildWarehouse.class;
	}
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		
		String child_warehouse_name=this.getPara("child_warehouse_name");
		filter.put("child_warehouse_name",child_warehouse_name);
		filter.put("manage_user_id",this.getPara("manage_user_id"));
		filter.put("type",this.getPara("type"));
		this.sortField(filter);
		Page<ChildWarehouse> page = ChildWarehouse.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	@PowerBind(code="A8_5_V",funcName="查看")
	public void qryOp() {
		getId();
		ChildWarehouse childWarehouse = ChildWarehouse.dao.findById(id);
		Map<String,Object> data=new HashMap<String,Object>();
		if (childWarehouse != null){
			data.put("childWarehouse", childWarehouse);
			this.rendJson(true,null, "",data);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	
	
	@PowerBind(code="A8_5_E",funcName="编辑")
	public void save() {
		try {
			getId();
			ChildWarehouse po = (ChildWarehouse) getModel();
			String uid=this.getCurrentUserId();
			this.pullUser(po, uid);
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			if (StringUtils.isEmpty(id)) {
				//添加船站
				po.set("id",UUID.randomUUID().toString());
				po.set("company_id", this.getCompanyId());
				po.save();
				id=po.getStr("id");
				//查询组织名为“分公司”的id
				Position position=new Position();
				Position org = position.getPositionByOrgName("分公司");
				//添加子公司
				if(org!=null){
					position.set("id",id);
					position.set("company_id", this.getCompanyId());
					position.set("department_id", null);
					position.set("permission", null);
					position.set("departids", null);
					position.set("m", 0);
					position.set("type", 0);
					position.set("quota", 0);
					position.set("sort_num", 2);
					position.set("description", null);
					position.set("name", po.get("child_warehouse_name"));
					position.set("is_head", null);
					position.set("parentids", null);
					position.set("parent_id", org.get("id"));
					position.save();
				}
			} else {
				//更新船站
				po.update();
				//更新子公司
				Position position=new Position();
				position.set("id", po.get("id"));
				position.set("name", po.get("child_warehouse_name"));
				position.update();
			}
			Map<String,String> d=new HashMap<String,String>();
			d.put("id",id);
			this.rendJson(true,null, "操作成功！",d);
		} catch (Exception e) {
			log.error("保存子仓库异常", e);
			this.rendJson(false,null, "保存数据异常！");
		}
	}
	
	@PowerBind(code="A8_5_E",funcName="编辑")
	public void del() {
		try {
			id = getId();
			ChildWarehouse.dao.del(id);
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	
}