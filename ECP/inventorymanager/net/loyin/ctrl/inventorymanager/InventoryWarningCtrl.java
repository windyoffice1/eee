package net.loyin.ctrl.inventorymanager;

import java.util.HashMap;
import java.util.Map;

import com.jfinal.plugin.activerecord.Page;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.MaterialData;

@RouteBind(path="inventorywarning",sys="存货管理",model="库存预警")
public class InventoryWarningCtrl extends AdminBaseController<MaterialData>{

	public InventoryWarningCtrl(){
		this.modelClass=MaterialData.class;
	}

	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		
		String matarial_name=this.getPara("matarial_name");
		filter.put("matarial_name",matarial_name);
		filter.put("material_no",this.getPara("material_no"));
		this.sortField(filter);
		Page<MaterialData> page = MaterialData.dao.pageWaringGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}

}
