package net.loyin.ctrl.ordermanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.ChildWarehouse;
import net.loyin.model.basicdata.MaterialData;
import net.loyin.model.getmaterial.GetMaterialApply;
import net.loyin.model.getmaterial.GetMaterialApplyData;
import net.loyin.model.inventorymanager.Inventory;
import net.loyin.model.inventorymanager.InventoryChildWarehouse;
import net.loyin.model.ordermanager.GetMaterialOutputStorageData;
import net.loyin.model.sso.Person;
/**
 * 
 * @author:lizhangyou
 * @Description: 根据出库单核对出库</p>* 
 * @date 2017-10-10
 */
@RouteBind(path="outputstoragemanager",sys="订单管理",model="出库管理")
public class OutputStorageManagerCtrl extends AdminBaseController<GetMaterialApply> {

	public OutputStorageManagerCtrl(){
		this.modelClass=GetMaterialApply.class;
	}
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//领料单名称||领料单编号
		String getmaterial_name=this.getPara("getmaterial_name").trim();
		filter.put("getmaterial_name",getmaterial_name);
		//出库情况[1：'待出库',2：'已出库',3：'出库中。。']
		String outputstorage_status=this.getPara("outputstorage_status");
		filter.put("outputstorage_status",outputstorage_status);
		//审批状态为2即已通过
		filter.put("approve_status","2");
		//未删除的
		filter.put("is_deleted_outputstorage","0");
		//领料单位
		String child_warehouse_id=this.getPara("child_warehouse_id");
		filter.put("child_warehouse_id",child_warehouse_id);
		this.sortField(filter);
		Page<GetMaterialApply> page = GetMaterialApply.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	@PowerBind(code={"A1_1_V"},funcName="查看")
	public void qryOp(){
		getId();
		GetMaterialApply getMaterialApply = GetMaterialApply.dao.findById(id,this.getCompanyId());
		if(getMaterialApply!=null){
			//通过领料单查询领料单的物料信息
			List<GetMaterialApplyData> list =GetMaterialApplyData.dao.getDataList(id,getMaterialApply.getStr("child_warehouse_id"));
			getMaterialApply.put("productlist", list);
			getMaterialApply.put("productlistlength", list.size());
			this.rendJson(true, null, "", getMaterialApply);
		}else
			this.rendJson(false,null, "记录不存在！");
	}
	
	/**
	 * 将出库通知单中的商品出库
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	@Before(Tx.class)
	@PowerBind(code={"A1_1_E"},funcName="编辑")
	public void outPutstorage() throws Exception{
		try {
			GetMaterialApply getMaterialApply =  getModel();
			if (getMaterialApply == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			String id=this.getId();
			//完成出库
			String pname="productlist";
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			//获取出库单的物料长度
			Integer productlistlength=this.getParaToInt("productlistlength");
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				//出库单id
				attr.put("getmaterial_apply_id", getMaterialApply.get("id"));
				//物料id
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				//出库数量
				attr.put("amount",this.getAttr(pname+"["+i+"][real_outputstorage_amount]"));
				//采购价
				attr.put("purchase_price",this.getAttr(pname+"["+i+"][purchase_price]"));
				
				BigDecimal un_outputstorage_amount=new BigDecimal(this.getAttrForStr((pname+"["+i+"][un_outputstorage_amount]")));
				BigDecimal real_outputstorage_amount = new BigDecimal(this.getAttrForStr((pname+"["+i+"][real_outputstorage_amount]")));
				//未出库数量
				attr.put("un_outputstorage_amount",un_outputstorage_amount.subtract(real_outputstorage_amount));
				//出库物料的总金额
				attr.put("total_money",this.getAttr(pname+"["+i+"][outputstorage_total_money]"));
				//出库物料的备注
				attr.put("outputstorage_comment",this.getAttr(pname+"["+i+"][outputstorage_comment]"));
				//仓位选择
				attr.put("outputstorage_warehouse_id",this.getAttr(pname+"["+i+"][outputstorage_warehouse_id]"));
				//出库日期
				attr.put("outputstorage_date",dateFormat.format(new Date()));
				//物料进入子仓库
				InventoryChildWarehouse.dao.putInChildWarehouse(getMaterialApply.getStr("child_warehouse_id"),this.getAttrForStr(pname+"["+i+"][material_data_id]"),
						this.getAttrForStr(pname+"["+i+"][real_outputstorage_amount]"),this.getCompanyId());
				productlist.add(attr);
			}
			//查询退料仓库中是否有此物料，如有则首先出库此仓库的数量
			List<Map<String,Object>> productlist2=InventoryChildWarehouse.dao.outPutStorageFirst(productlist);
			//查询出退料仓库id
			String  returnMaterial_id=ChildWarehouse.dao.getReturnWarehouseid();
			for(int i=0 ;i<productlist2.size();i++){
				String idf=(String) productlist2.get(i).get("id");
				//排除退料仓库已经出库的
				if(!returnMaterial_id.equals((String)productlist2.get(i).get("outputstorage_warehouse_id"))){
					//物料出仓(仓位)
					Inventory.dao.outWarehouse(productlist2.get(i).get("outputstorage_warehouse_id").toString(),
							productlist2.get(i).get("material_data_id").toString(),(String)productlist2.get(i).get("amount").toString(), this.getCompanyId());
					//出库时改变现存物料数量(总仓库)
					MaterialData.dao.outPutStorage(productlist2.get(i).get("material_data_id").toString(),productlist2.get(i).get("amount").toString());
				}
			}
			//核对出库通知单的物料，减少未出库数量
			GetMaterialApplyData.dao.realOutPutstorage(productlist);
			//根据出库单物料信息，插入真正出库物料表
			GetMaterialOutputStorageData.dao.insert(productlist2);
			//判断出库通知单的物料是否已经全部完成出库，如完成则改变出库单的出库状态为已出库
			if(GetMaterialApplyData.dao.isAllOutPutStorage(getMaterialApply.getStr("id"))){
				getMaterialApply.set("outputstorage_status", "1");
			}else{
				//改为出库中
				getMaterialApply.set("outputstorage_status", "2");
			}
			getMaterialApply.update();
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			this.rendJson(true,null, "出库成功！",data);
		} catch (Exception e) {
			this.rendJson(false,null, "出库失败！");
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询出库单详情
	 */
	public void viewOutPutStorageHistory() {
		this.getId();
		GetMaterialApply getMaterialApply = GetMaterialApply.dao.findById(id, this.getCompanyId());
		if (getMaterialApply != null){
			List<GetMaterialOutputStorageData> productlist=GetMaterialOutputStorageData.dao.list(id);
			getMaterialApply.put("productlist",productlist);
			getMaterialApply.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", getMaterialApply);
		}
		else{
			this.rendJson(false,null, "记录不存在！");
		}
	}
	
	@PowerBind(code={"A1_1_E"},funcName="编辑")
	public void del(){
		try {
			getId();
			GetMaterialApply.dao.delHedui(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
}
