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
import net.loyin.model.inventorymanager.Inventory;
import net.loyin.model.ordermanager.PutInStorageRealData;
import net.loyin.model.scm.PurchaseApply;
import net.loyin.model.scm.PurchaseApplyData;
import net.loyin.model.scm.PutInStorage;
import net.loyin.model.scm.PutInStorageData;
import net.loyin.model.sso.Person;
/**
 * 
 * @author:lizhangyou
 * @Description: 根据入库单核对入库</p>* 
 * @date 2017-10-10
 */
@RouteBind(path="putinstoragemanager",sys="订单管理",model="入库管理")
public class PutInStorageManagerCtrl  extends AdminBaseController<PutInStorage> {

	public PutInStorageManagerCtrl(){
		this.modelClass=PutInStorage.class;
	}
	
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//查询出当前人创建的采购申请
		filter.put("create_user_id", this.getPara("create_user_id"));
		//入库单名称 
		String putinstorage_name=this.getPara("putinstorage_name");
		filter.put("putinstorage_name",putinstorage_name);
		//未删除的
		String is_deleted="0";
		filter.put("is_deleted",is_deleted);
		this.sortField(filter);
		
		Page<PutInStorage> page = PutInStorage.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	/**
	 * 查询采购申请单的入库情况
	 */
	public void qryOp() {
		getId();
		PurchaseApply purchaseApply = PurchaseApply.dao.findById(id, this.getCompanyId());
		if (purchaseApply != null){
			List<PurchaseApplyData> productlist=PurchaseApplyData.dao.list(id);
			purchaseApply.put("productlist",productlist);
			purchaseApply.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", purchaseApply);
		}
		else{
			this.rendJson(false,null, "记录不存在！");
		}
	}

	/**
	 * 将入库通知单中的商品入库
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	@Before(Tx.class)
	@PowerBind(code={"A1_2_E"},funcName="编辑")
	public void putInstorage() throws Exception{
		List<Map<String,Object>> productlist2=new ArrayList<Map<String,Object>>();
		try {
			PutInStorage putInstorage =  getModel();
			if (putInstorage == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			String id=this.getId();
			//根据id查询入库通知单详情
			PutInStorage put=PutInStorage.dao.findById(id);
			//put.set("putinstorage_comment", putInstorage.getStr("putinstorage_comment"));
			put.set("real_putinstorage_user_id", putInstorage.getStr("real_putinstorage_user_id"));
			//完成入库
			String pname="productlist";
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			//获取入库单的物料长度
			Integer productlistlength=this.getParaToInt("productlistlength");
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				//入库单id
				attr.put("putinstorage_id", put.get("id"));
				//物料id
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				//入库数量
				attr.put("real_putinstarage_amount",this.getAttr(pname+"["+i+"][real_putinstarage_amount]"));
				//采购价
				attr.put("purchase_price",this.getAttr(pname+"["+i+"][purchase_price]"));
				BigDecimal real_unputinstorage_amount = new BigDecimal(this.getAttrForStr((pname+"["+i+"][real_unputinstorage_amount]")));
				BigDecimal real_putinstarage_amount = new BigDecimal(this.getAttrForStr((pname+"["+i+"][real_putinstarage_amount]")));
				//未入库数量
				attr.put("real_unputinstorage_amount",real_unputinstorage_amount.subtract(real_putinstarage_amount));
				//入库物料的总金额
				attr.put("real_putinstorage_total_money",this.getAttr(pname+"["+i+"][real_putinstorage_total_money]"));
				//入库物料的备注
				attr.put("putinstorage_comment",this.getAttr(pname+"["+i+"][putinstorage_comment]"));
				//发票号码
				attr.put("invoice_number",this.getAttr(pname+"["+i+"][invoice_number]"));
				//仓位选择
				attr.put("putinstorage_warehouse_id",this.getAttr(pname+"["+i+"][putinstorage_warehouse_id]"));
				//入库日期
				attr.put("putinstorage_date",dateFormat.format(new Date()));
				System.err.println(attr.toString());
				productlist.add(attr);
			}
/*			//重新计算物料的平均价格
			MaterialData.dao.averagePrice(productlist);*/
			//核对入库通知单的物料，减少未入库数量
			PutInStorageData.dao.realPutInstorage(productlist);
			for(Map<String,Object> data:productlist){
				//物料入仓
				System.err.println(data.toString());
				Inventory.dao.putInWarehouse(data.get("putinstorage_warehouse_id").toString(),
						data.get("material_data_id").toString(),data.get("real_putinstarage_amount").toString(), this.getCompanyId());
			}
			//根据入库单物料信息，插入真正入库物料表
			PutInStorageRealData.dao.insert(productlist);
			//判断入库通知单的物料是否已经全部完成入库，如完成则改变入库单的入库状态为已入库
			if(PutInStorageData.dao.isAllPutInStorage(put.getStr("id"))){
				put.set("putinstorage_status", "1");
			}else{
				//改为入库中
				put.set("putinstorage_status", "2");
			}
			put.update();
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			this.rendJson(true,null, "入库成功！",data);
		} catch (Exception e) {
			this.rendJson(true,null, "入库失败！");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 查询入库单详情
	 */
	public void qryDetails() {
		getId();
		PutInStorage putInStorage = PutInStorage.dao.findById(id, this.getCompanyId());
		if (putInStorage != null){
			List<PutInStorageData> productlist=PutInStorageData.dao.list(id);
			putInStorage.put("productlist",productlist);
			putInStorage.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", putInStorage);
		}
		else{
			this.rendJson(false,null, "记录不存在！");
		}
	}
	
	/**
	 * 查询入库单详情
	 */
	public void viewPutInStorageHistory() {
		this.getId();
		PutInStorage putInStorage = PutInStorage.dao.findById(id, this.getCompanyId());
		if (putInStorage != null){
			List<PutInStorageRealData> productlist=PutInStorageRealData.dao.list(id);
			putInStorage.put("productlist",productlist);
			putInStorage.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", putInStorage);
		}
		else{
			this.rendJson(false,null, "记录不存在！");
		}
	}
	
	@PowerBind(code={"A1_2_V"},funcName="删除")
	public void del(){
		try {
			getId();
			PutInStorage.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	
	/**
	 * 直接新增入库单,入库数量为负,即修改入库单输入错误的操作
	 */
	@PowerBind(code={"A1_2_V"},funcName="新增")
	public void add(){
		try {
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			//根据申请单查询此申请单已经有过入库的记录
			//final PutInStorage put=PutInStorage.dao.isPuted(purchaseApply.getStr("id"));
			PutInStorage putInStorage = new PutInStorage();
			putInStorage.set("id",UUID.randomUUID().toString());
			putInStorage.set("create_time",dateTimeFormat.format(new Date()));
			putInStorage.set("real_putinstorage_date",dateTimeFormat.format(new Date()));
			putInStorage.set("create_user_id",this.getCurrentUserId());
			putInStorage.set("real_putinstorage_user_id",this.getCurrentUserId());
			putInStorage.set("company_id",this.getCompanyId());
			putInStorage.set("putinstorage_name", perMap.get("realname")+"的入库单(退料)"+dateTimeFormat1.format(new Date()));
			//入库单状态未进行入库审核:【未入库，已入库，正在入库】
			putInStorage.set("putinstorage_status","1");
			//完成入库
			String pname="productlist";
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			//获取入库单的物料长度
			Integer productlistlength=this.getParaToInt("productlistlength");
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				//入库单id
				attr.put("putinstorage_id", putInStorage.get("id"));
				//物料id
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				//入库数量
				attr.put("real_putinstarage_amount",this.getAttr(pname+"["+i+"][real_putinstarage_amount]"));
				//采购价
				attr.put("purchase_price",this.getAttr(pname+"["+i+"][purchase_price]"));
				//入库物料的总金额
				attr.put("real_putinstorage_total_money",this.getAttr(pname+"["+i+"][real_putinstorage_total_money]"));
				//入库物料的备注
				attr.put("putinstorage_comment",this.getAttr(pname+"["+i+"][putinstorage_comment]"));
				//仓位选择
				attr.put("putinstorage_warehouse_id",this.getAttr(pname+"["+i+"][putinstorage_warehouse_id]"));
				//入库日期
				attr.put("putinstorage_date",dateFormat.format(new Date()));
				//入库日期
				attr.put("is_deleted","0");
				//物料入仓
				Inventory.dao.putInWarehouse(this.getAttrForStr(pname+"["+i+"][putinstorage_warehouse_id]"),
						this.getAttrForStr(pname+"["+i+"][material_data_id]"),this.getAttrForStr(pname+"["+i+"][real_putinstarage_amount]"), this.getCompanyId());
				productlist.add(attr);
			}
			//根据入库单物料信息，插入真正入库物料表
			PutInStorageRealData.dao.insert(productlist);
			putInStorage.save();
/*			//重新计算物料的平均价格
			MaterialData.dao.averagePrice(productlist);*/
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			this.rendJson(true,null, "入库成功！",data);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
