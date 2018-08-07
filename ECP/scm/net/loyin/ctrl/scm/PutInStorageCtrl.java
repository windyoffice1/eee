package net.loyin.ctrl.scm;

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
import net.loyin.model.scm.PurchaseApply;
import net.loyin.model.scm.PurchaseApplyData;
import net.loyin.model.scm.PutInStorage;
import net.loyin.model.scm.PutInStorageData;
import net.loyin.model.sso.Person;

/**
 * 
 * @author:lizhangyou
 * @Description: </p>* 
 * @date 2017-10-10
 */
@RouteBind(path="putinstorage",sys="采购管理",model="入库通知单")
public class PutInStorageCtrl  extends AdminBaseController<PutInStorage> {

	public PutInStorageCtrl(){
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
		//排除直接入库（数量为负）的
		filter.put("is_since_purchase_apply_id","是");
		
		this.sortField(filter);
		
		Page<PutInStorage> page = PutInStorage.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	
	
	/**
	 * 查询采购申请单的入库情况
	 */
	@PowerBind(code={"A4_2_V"},funcName="查询")
	public void qryOp() {
		getId();
		PurchaseApply purchaseApply = PurchaseApply.dao.findById(id, this.getCompanyId());
		if (purchaseApply != null){
			List<PurchaseApplyData> productlist=PurchaseApplyData.dao.list(id);
/*			//判断是否已经入库
			if(productlist!=null&&productlist.size()>0){
				for(int i=0 ;i<productlist.size();i++ ){
					if(productlist.get(i).getInt("un_putinstorage_amount")==null){
						productlist.get(i).set("un_putinstorage_amount", productlist.get(i).getInt("amount"));
					}
					if(productlist.get(i).getInt("putinstorage_amount")==null){
						productlist.get(i).set("putinstorage_amount", 0);
					}
				}
			}*/
			purchaseApply.put("productlist",productlist);
			purchaseApply.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", purchaseApply);
		}
		else{
			this.rendJson(false,null, "记录不存在！");
		}
	}

	/**
	 * 将申请单中的商品入库
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	@Before(Tx.class)
	@PowerBind(code={"A4_2_E"},funcName="编辑")
	public void putInstorage() throws Exception{
		boolean succeed=true;
		List<Map<String,Object>> productlist2=new ArrayList<Map<String,Object>>();
		try {
			final PurchaseApply purchaseApply = (PurchaseApply) getModel2(PurchaseApply.class);
			if (purchaseApply == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			//根据申请单查询此申请单已经有过入库的记录
			//final PutInStorage put=PutInStorage.dao.isPuted(purchaseApply.getStr("id"));
			PutInStorage putInStorage = new PutInStorage();
			putInStorage.set("id",UUID.randomUUID().toString());
			putInStorage.set("since_purchase_apply_id",purchaseApply.getStr("id"));
			putInStorage.set("since_purchase_apply_name",purchaseApply.getStr("purchase_name"));
			putInStorage.set("create_time",dateTimeFormat.format(new Date()));
			putInStorage.set("create_user_id",this.getCurrentUserId());
			putInStorage.set("comment",this.getAttr("remark"));
			putInStorage.set("company_id",this.getCompanyId());
			putInStorage.set("putinstorage_name", perMap.get("realname")+"的入库单"+dateTimeFormat1.format(new Date()));
			putInStorage.set("putinstorage_date",this.getAttr("putinstorage_date"));
			putInStorage.set("customer_name",purchaseApply.getStr("customer_name"));
			//入库单状态未进行入库审核:【未入库，已入库，正在入库】
			putInStorage.set("putinstorage_status","0");
			//是否已删除
			putInStorage.set("is_deleted","0");
			
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
				attr.put("material_data_name",this.getAttr(pname+"["+i+"][material_data_name]"));
				//计划单价
				attr.put("target_price",this.getAttr(pname+"["+i+"][target_price]"));
				//计划入库总金额
				attr.put("plan_putinstorage_total_money",this.getAttr(pname+"["+i+"][plan_putinstorage_total_money]"));
				
				attr.put("purchase_price",this.getAttr(pname+"["+i+"][purchase_price]"));
				attr.put("unit",this.getAttr(pname+"["+i+"][unit]"));
				//准备入库数量
				attr.put("readey_putinstorage_amount",this.getAttr(pname+"["+i+"][readey_putinstorage_amount]"));
				//准备入库的物料总金额
				attr.put("readey_putinstorage_total_money",this.getAttr(pname+"["+i+"][readey_putinstorage_total_money]"));
				attr.put("create_time", dateTimeFormat.format(new Date()));
				//申请单里的物料单id
				attr.put("purchaseApplyDataID",this.getAttr(pname+"["+i+"][id]"));
				productlist.add(attr);
			}
			productlist2=productlist;
/*			PutInStorage putInStorage1 = putInStorage;
			List<Map<String,Object>> productlist=productlist;
			succeed = Db.tx(new IAtom() {
	            //在执行run的过程中，如果有异常将回滚，或者return false 将回滚
	            @Override
	            public boolean run() throws SQLException {
	    			try {
	    				putInStorage1.save();
		    			//将物料计入入库申请单
		    			PutInStorageData.dao.insert(productlist1);
		    			//修改申请单的已出库数量
						PurchaseApplyData.dao.updatePutInStorage(productlist1);
		    			//判断采购申请单的物料是否已经全部完成入库,如全部完成入库，则改变申请单状态为5（已入库）["未提交","待审核","批准","驳回","已取回","已入库"]
		    			PurchaseApplyData.dao.updatePurchaseStatus(purchaseApply.getStr("id"));
					} catch (Exception e) {
						e.printStackTrace();
						return false;
					}
					return true;
	            }
	        });*/
			putInStorage.save();
			//将物料计入入库申请单
			PutInStorageData.dao.insert(productlist);
			//修改申请单的已出库数量
			PurchaseApplyData.dao.updatePutInStorage(productlist);
			//判断采购申请单的物料是否已经全部完成入库,如全部完成入库，则改变申请单状态为5（已入库）["未提交","待审核","批准","驳回","已取回","已入库"]
			PurchaseApplyData.dao.updatePurchaseStatus(purchaseApply.getStr("id"));
			//入库时改变
			//MaterialData.dao.changeExistingAmount(productlist);
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			this.rendJson(true,null, "入库成功！",data);
		} catch (Exception e) {
			//恢复数据
			PutInStorageData.dao.deletePutInStorageData(productlist2);
			this.rendJson(true,null, "入库数量不能大于未入库数量");
			e.printStackTrace();
		}
	}
	
	
	/**
	 * 查询入库单详情
	 */
	@PowerBind(code={"A4_2_V"},funcName="查询")
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
	
	@PowerBind(code={"A4_2_E"},funcName="删除")
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
	
}
