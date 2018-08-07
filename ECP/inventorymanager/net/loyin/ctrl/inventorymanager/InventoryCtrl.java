package net.loyin.ctrl.inventorymanager;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.plugin.activerecord.Page;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.inventorymanager.Inventory;

@RouteBind(path="inventory",sys="存货管理",model="存货列表")
public class InventoryCtrl extends AdminBaseController<Inventory>{

	public InventoryCtrl(){
		this.modelClass=Inventory.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//物料类型（如修理类）
		filter.put("material_type_id", this.getPara("material_type_id"));
		//物料大类
		filter.put("material_broad_id",this.getPara("material_broad_id"));
		//物料型号
		filter.put("model_number", this.getPara("model_number").trim());
		//物料名称
		filter.put("material_data_name", this.getPara("material_data_name").trim());
		//物料编号
		filter.put("material_data_no", this.getPara("material_data_no").trim());
		//查询c仓位
		filter.put("warehouse_id", this.getPara("warehouse_id"));
		//排序
		this.sortField(filter);
		
		Page<Inventory> page = Inventory.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	
}
