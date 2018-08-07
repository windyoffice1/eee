package net.loyin.ctrl.inventorymanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.jfinal.plugin.activerecord.Page;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.Warehouselocation;
import net.loyin.model.inventorymanager.Inventory;
import net.loyin.model.inventorymanager.Scrap;
import net.loyin.model.inventorymanager.ScrapData;
import net.loyin.model.sso.Person;

@RouteBind(path="scrap",sys="存货管理",model="报废管理")
public class ScrapCtrl extends AdminBaseController<Scrap>{
	public ScrapCtrl(){
		this.modelClass=Scrap.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//移位仓库
		String warehouse_id=this.getPara("warehouse_id");
		filter.put("warehouse_id",warehouse_id);
		//报废单位类型【0：仓位，1：船站】
		String type=this.getPara("type");
		filter.put("type",type);
		//排序
		this.sortField(filter);
		
		Page<Scrap> page = Scrap.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	
	@PowerBind(code={"A2_5_V"},funcName="查询")
	public void qryOp() {
		getId();
		Scrap scrap = Scrap.dao.findById(id, this.getCompanyId());
		if (scrap != null){
			List<ScrapData> productlist=ScrapData.dao.list(id);
			
			scrap.put("productlist",productlist);
			scrap.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", scrap);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}

	/**
	 * 新增或修改报废信息
	 */	
	@SuppressWarnings("unused")
	@PowerBind(code={"A2_5_E"},funcName="编辑")
	public void save() {
		try {
			getId();
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			Scrap po = (Scrap) getModel();
			
			String warehouse_id=po.getStr("warehouse_id");
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			//查询仓库
			Warehouselocation warehouse=Warehouselocation.dao.findById(warehouse_id);
			
			String company_id=this.getCompanyId();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdformet = new SimpleDateFormat("yyyyMMdd");
			String uuid = UUID.randomUUID().toString();
			
			String pname="productlist";
			this.pullUser(po, this.getCurrentUserId());
			if (StringUtils.isEmpty(id)) {
				po.set("id", uuid);
				//组装报废名称
				po.set("scrap_name", warehouse.get("warehouse_name")+"的报废记录"+sdformet.format(new Date()));
				po.set("operate_user_id",this.getCurrentUserId());
				po.set("operate_date", sdf.format(new Date()));
				//新增的为仓位报废
				po.set("type", "0");
				po.set("company_id",company_id);
				po.save();
			} else {
				po.set("update_user_id", this.getCurrentUserId());
				po.set("update_date",sdf.format(new Date()));
				po.update();
			}
			
			Integer productlistlength=this.getParaToInt("productlistlength");
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				attr.put("inventory_scrap_id",po.get("id"));
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				attr.put("amount",this.getAttr(pname+"["+i+"][amount]"));
				attr.put("target_price",this.getAttr(pname+"["+i+"][target_price]"));
				attr.put("total_money",this.getAttr(pname+"["+i+"][total_money]"));
				attr.put("comment",this.getAttr(pname+"["+i+"][comment]"));
				productlist.add(attr);
				//物料报废-减少仓位的数量
				Inventory.dao.outWarehouse(po.getStr("warehouse_id"),this.getAttrForStr(pname+"["+i+"][material_data_id]"), this.getAttrForStr(pname+"["+i+"][amount]"), company_id);
/*				//物料报废-减少总的物料数量
				MaterialData.dao.outPutStorage(this.getAttrForStr(pname+"["+i+"][material_data_id]"),  this.getAttrForStr(pname+"["+i+"][amount]"));*/
			}
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			ScrapData.dao.insert(productlist);
			this.rendJson(true,null, "报废成功！",data);
		} catch (Exception e) {
			log.error("报废申请异常", e);
			this.rendJson(false,null, "报废申请异常！");
			e.printStackTrace();
		}
	}
	
	
	
	@PowerBind(code={"A2_5_E"},funcName="删除")
	public void del(){
		try {
			getId();
			Scrap.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
			e.printStackTrace();
		}
	}
	
}
