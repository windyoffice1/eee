package net.loyin.ctrl.inventorymanager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Page;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.inventorymanager.Check;
import net.loyin.model.inventorymanager.ChildWarehouseMaterialUsedHistory;
import net.loyin.model.inventorymanager.InventoryChildWarehouse;
import net.loyin.model.inventorymanager.Scrap;
import net.loyin.model.sso.Position;

@RouteBind(path="childwarehousematerialusedhistory",sys="存货管理",model="船站物料使用情况")
public class ChildWarehouseMaterialUsedHistoryCtrl extends AdminBaseController<ChildWarehouseMaterialUsedHistory>{

	public ChildWarehouseMaterialUsedHistoryCtrl(){
		this.modelClass=ChildWarehouseMaterialUsedHistory.class;
	}
	
	public void dataGrid(){
		Map<String,Object> filter=new HashMap<String,Object>();
		
		filter.put("company_id", this.getCompanyId());
		//物料类型
		filter.put("material_type", this.getAttr("material_type_id"));
		//物料大类
		filter.put("material_broad_id", this.getAttr("material_broad_id"));
		//物料编号
		filter.put("material_data_no", this.getAttr("material_data_no"));
		//船站[分公司]
		filter.put("child_warehouse_id", this.getAttr("child_warehouse_id"));
		
		Position position=Position.dao.getSuperDepartment(this.getUserMap().get("department_id"));
		if(position!=null&&position.getStr("name").equals("分公司")){
			filter.put("child_warehouse_id", this.getUserMap().get("department_id"));
		}
		//排序
		this.sortField(filter);
		
		Page<ChildWarehouseMaterialUsedHistory> page = ChildWarehouseMaterialUsedHistory.dao.pageGrid(getPageNo(), getPageSize(), filter);
		this.rendJson(true,null, "查询成功",page);
	}
	
	@PowerBind(code={"A2_7_E"},funcName="编辑")
	public void save(){
		try {
			getId();
			ChildWarehouseMaterialUsedHistory po =this.getModel();
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			SimpleDateFormat dateFormat =new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(StringUtils.isNotBlank(po.getStr("child_warehouse_id"))){
				if(StringUtils.isNotBlank(po.getStr("material_data_id"))){
					po.set("id", UUID.randomUUID().toString());
					po.set("company_id", this.getCompanyId());
					po.set("create_user_id",this.getCurrentUserId());
					Integer amount1=po.getInt("amount");
					BigDecimal amount=new BigDecimal(amount1);
					BigDecimal price=po.getBigDecimal("price");
					BigDecimal total_money=null;
					if(amount!=null&&price!=null){
						total_money=amount.multiply(price);
					}
					po.set("total_money",total_money);
					po.set("create_time",dateFormat.format(new Date()));
					po.save();
					//减少子仓库的物料数量
					InventoryChildWarehouse.dao.ChildWarehouseUsedMaterial(po.getStr("child_warehouse_id"),po.getStr("material_data_id"), amount.toString());
					this.rendJson(true,null, "新增成功","");
				}else{
					this.rendJson(false,null, "新增失败，物料不能为空！","");
				}
			}else{
				this.rendJson(false,null, "新增失败，子公司不能为空！","");
			}
		} catch (Exception e) {
			e.printStackTrace();
			this.rendJson(false,null, "新增失败","");
		}
	}
	
	@PowerBind(code={"A2_7_E"},funcName="删除")
	public void del(){
		try {
			getId();
			ChildWarehouseMaterialUsedHistory.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
			e.printStackTrace();
		}
	}
	
}
