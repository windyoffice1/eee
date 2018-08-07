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
import net.loyin.model.inventorymanager.Inventory;
import net.loyin.model.inventorymanager.Transfer;
import net.loyin.model.inventorymanager.TransferData;
import net.loyin.model.sso.Person;

@RouteBind(path="transfer",sys="存货管理",model="移位管理")
public class TransferCtrl extends AdminBaseController<Transfer>{

	public TransferCtrl(){
		this.modelClass=Transfer.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//目标仓库
		String target_warehouse_id=this.getPara("target_warehouse_id");
		filter.put("target_warehouse_id",target_warehouse_id);
		//原属仓库
		String from_warehouse_id=this.getPara("from_warehouse_id");
		filter.put("from_warehouse_id",from_warehouse_id);
		//排序
		this.sortField(filter);
		
		Page<Transfer> page = Transfer.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	@PowerBind(code={"A2_4_V"},funcName="查询")
	public void qryOp() {
		getId();
		Transfer transfer = Transfer.dao.findById(id, this.getCompanyId());
		if (transfer != null){
			List<TransferData> productlist=TransferData.dao.list(id);
			
			transfer.put("productlist",productlist);
			transfer.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", transfer);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	
	
	/**
	 * 新增或修改位移记录
	 */	
	@SuppressWarnings("unused")
	@PowerBind(code={"A2_4_E"},funcName="编辑")
	public void save() {
		try {
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			Transfer po = (Transfer) getModel();
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			getId();
			String company_id=this.getCompanyId();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			String uuid = UUID.randomUUID().toString();
			
			String pname="productlist";
			this.pullUser(po, this.getCurrentUserId());
			if (StringUtils.isEmpty(id)) {
				po.set("id", uuid);
				//po.set("transfer_total_money", this.getParaToInt("transfer_total_money"));
				po.set("operate_user_id",this.getCurrentUserId());
				po.set("operate_date", sdf.format(new Date()));
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
				attr.put("inventory_transfer_id",po.get("id"));
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				attr.put("amount",this.getAttr(pname+"["+i+"][amount]"));
				attr.put("target_price",this.getAttr(pname+"["+i+"][target_price]"));
				attr.put("total_money",this.getAttr(pname+"["+i+"][total_money]"));
				attr.put("comment",this.getAttr(pname+"["+i+"][comment]"));
				productlist.add(attr);
				//物料出仓
				Inventory.dao.outWarehouse(po.getStr("from_warehouse_id"),this.getAttrForStr(pname+"["+i+"][material_data_id]"), this.getAttrForStr(pname+"["+i+"][amount]"), company_id);
				//物料入仓
				Inventory.dao.putInWarehouse(po.getStr("target_warehouse_id"),this.getAttrForStr(pname+"["+i+"][material_data_id]"), this.getAttrForStr(pname+"["+i+"][amount]"), company_id);
			}
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			TransferData.dao.insert(productlist);
			this.rendJson(true,null, "操作成功！",data);
		} catch (Exception e) {
			log.error("保存订单异常", e);
			this.rendJson(false,null, "保存数据异常！");
			e.printStackTrace();
		}
	}
	
	
	
	
	@PowerBind(code={"A2_4_E"},funcName="删除")
	public void del(){
		try {
			getId();
			Transfer.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询特定仓库的物料信息
	 */
	public void getWarehouseMaterial(){
		getId();
		String material_no=this.getAttrForStr("material_no");
		List<Inventory> data = Inventory.dao.getWarehouseMaterial(id,material_no);
		this.rendJson(true,null, "操作成功！",data);
	}
	
}
