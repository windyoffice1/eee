package net.loyin.ctrl.ordermanager;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.ChildWarehouse;
import net.loyin.model.getmaterial.GetMaterialApply;
import net.loyin.model.inventorymanager.InventoryChildWarehouse;
import net.loyin.model.inventorymanager.Scrap;
import net.loyin.model.inventorymanager.ScrapData;
import net.loyin.model.ordermanager.ReturnMaterialPutInStorageData;
import net.loyin.model.returnmaterial.ReturnMaterialApply;
import net.loyin.model.returnmaterial.ReturnMaterialApplyData;
import net.loyin.model.sso.Person;

/**
 * 
 * @author:lizhangyou
 * @Description: 根据退料单核对退料</p>* 
 * @date 2017-10-10
 */
@RouteBind(path="returnmaterialmanager",sys="订单管理",model="退料管理")
public class ReturunMaterialManagerCtrl extends AdminBaseController<ReturnMaterialApply> {

	public ReturunMaterialManagerCtrl(){
		this.modelClass=ReturnMaterialApply.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
/*		//是否已删除状态
		String is_deleted=this.getPara("is_deleted");
		if(StringUtils.isBlank(is_deleted)){
			filter.put("is_deleted","0");
		}else{
			filter.put("is_deleted",is_deleted);
		}*/
		//退料单名称||退料单编号
		String returnmaterial_name=this.getPara("returnmaterial_name");
		filter.put("returnmaterial_name",returnmaterial_name);
		//退料情况[1：'待退料',2：'已退料',3：'退料中。。']
		String putinstorage_status=this.getPara("putinstorage_status");
		filter.put("putinstorage_status",putinstorage_status);
		//查询出已批准
		filter.put("approve_status","2");
		//未删除的
		filter.put("is_deleted_putinstorage","0");
		//退料单位
		String child_warehouse_id=this.getPara("child_warehouse_id");
		filter.put("child_warehouse_id",child_warehouse_id);
		this.sortField(filter);
		Page<ReturnMaterialApply> page = ReturnMaterialApply.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	public void qryOp(){
		getId();
		ReturnMaterialApply returnMaterialApply = ReturnMaterialApply.dao.findById(id,this.getCompanyId());
		if(returnMaterialApply!=null){
			//通过退料单查询退料单的物料信息
			List<ReturnMaterialApplyData> list =ReturnMaterialApplyData.dao.getDataList(id,returnMaterialApply.getStr("child_warehouse_id"));
			returnMaterialApply.put("productlist", list);
			returnMaterialApply.put("productlistlength", list.size());
			this.rendJson(true, null, "", returnMaterialApply);
		}else
			this.rendJson(false,null, "记录不存在！");
	}
	
	/**
	 * 将退料通知单中的商品退料,到退料仓库中
	 * @throws Exception 
	 */
	@SuppressWarnings("unused")
	@Before(Tx.class)
	@PowerBind(code={"A1_3_E"},funcName="编辑")
	public void putInstorage() throws Exception{
		try {
			ReturnMaterialApply returnMaterialApply = getModel();
			if (returnMaterialApply == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			Person person = new Person();
			String id=this.getId();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			//完成退料
			String pname="productlist";
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			//如有不合格物料上传，则新增报废信息
			boolean is_have_qualified=false;
			//获取退料单的物料长度
			Integer productlistlength=this.getParaToInt("productlistlength");
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				//退料单id
				attr.put("returnmaterial_apply_id", returnMaterialApply.get("id"));
				//物料id
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				//退料数量
				attr.put("amount",this.getAttr(pname+"["+i+"][putinstorage_amount]"));
				//单价
				attr.put("target_price",this.getAttr(pname+"["+i+"][target_price]"));
				BigDecimal un_putinstorage_amount = new BigDecimal(this.getAttrForStr((pname+"["+i+"][un_putinstorage_amount]")));
				BigDecimal putinstorage_amount = new BigDecimal(this.getAttrForStr((pname+"["+i+"][putinstorage_amount]")));
				//未退料数量
				attr.put("un_putinstorage_amount",un_putinstorage_amount.subtract(putinstorage_amount));
				//退料物料的总金额
				attr.put("total_money",this.getAttr(pname+"["+i+"][putinstorage_total_money]"));
				//退料物料的备注
				attr.put("putinstorage_comment",this.getAttr(pname+"["+i+"][putinstorage_comment]"));
				//退料日期
				attr.put("putinstorage_date",dateFormat.format(new Date()));
				//退料总金额
				attr.put("putinstorage_total_money",this.getAttr(pname+"["+i+"][putinstorage_total_money]"));
				//物料移出子仓库  退料数量加一个负号
				InventoryChildWarehouse.dao.putInChildWarehouse(returnMaterialApply.getStr("child_warehouse_id"),this.getAttrForStr(pname+"["+i+"][material_data_id]"),
						"-"+this.getAttrForStr(pname+"["+i+"][putinstorage_amount]"),this.getCompanyId());
				//物料合格，直接加入至退料仓库        退料仓库的id为2
				if("合格".equals(this.getAttr(pname+"["+i+"][is_qualified]"))){
					InventoryChildWarehouse.dao.putInRetrurnWarehouse("2", this.getAttrForStr(pname+"["+i+"][material_data_id]"),
							this.getAttrForStr(pname+"["+i+"][putinstorage_amount]"),this.getCompanyId());
					//退料物料是否合格[1:合格，0:不合格]
					attr.put("is_qualified","1");
				}else{
					//退料物料是否合格[1:合格，0:不合格]
					attr.put("is_qualified","0");
					//物料不合格，直接加入报废记录
					is_have_qualified=true;
				}

				productlist.add(attr);
			}
			if(is_have_qualified){
				//如有不合格物料上传，则新增报废信息
				Scrap scrap = new Scrap();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
				SimpleDateFormat sdformet = new SimpleDateFormat("yyyyMMdd");
				ChildWarehouse childWarehouse=ChildWarehouse.dao.findById(returnMaterialApply.get("child_warehouse_id"));
				
				scrap.set("id", UUID.randomUUID().toString());
				//组装报废名称
				scrap.set("scrap_name", childWarehouse.get("child_warehouse_name")+"的报废记录"+sdformet.format(new Date()));
				scrap.set("operate_user_id",this.getCurrentUserId());
				scrap.set("operate_date", sdf.format(new Date()));
				scrap.set("warehouse_id", returnMaterialApply.get("child_warehouse_id"));
				//船站报废
				scrap.set("type", "1");
				scrap.set("company_id",this.getCompanyId());
				List<Map<String,Object>> scrapList=new ArrayList<Map<String,Object>>();
				Float total_money=(float)0.00;
				for(Map<String,Object> scrapData:productlist){
					if("0".equals(scrapData.get("is_qualified"))){
						String putinstorage_total_money=(String) scrapData.get("putinstorage_total_money");
						total_money=total_money+Float.parseFloat(putinstorage_total_money);
						scrapData.put("comment",scrapData.get("putinstorage_comment"));
						scrapData.put("inventory_scrap_id", scrap.get("id"));
						scrapList.add(scrapData);
					}
				}
				scrap.set("scrap_total_money", total_money);
				scrap.save();
				ScrapData.dao.insert(scrapList);
			}
			//核对退料通知单的物料，减少未退料数量
			ReturnMaterialApplyData.dao.realOutPutstorage(productlist);
			//根据退料单物料信息，插入真正退料物料表
			ReturnMaterialPutInStorageData.dao.insert(productlist);
			//判断退料通知单的物料是否已经全部完成退料，如完成则改变退料单的退料状态为已退料
			if(ReturnMaterialApplyData.dao.isAllOutPutStorage(returnMaterialApply.getStr("id"))){
				returnMaterialApply.set("putinstorage_status", "1");
			}else{
				//改为退料中
				returnMaterialApply.set("putinstorage_status", "2");
			}
			returnMaterialApply.update();
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			this.rendJson(true,null, "退料成功！",data);
		} catch (Exception e) {
			this.rendJson(true,null, "退料失败！");
			e.printStackTrace();
		}
	}
	
	/**
	 * 查询退料单详情
	 */
	public void viewReturnHistory() {
		this.getId();
		ReturnMaterialApply returnMaterialApply = ReturnMaterialApply.dao.findById(id, this.getCompanyId());
		if (returnMaterialApply != null){
			List<ReturnMaterialPutInStorageData> productlist=ReturnMaterialPutInStorageData.dao.list(id);
			returnMaterialApply.put("productlist",productlist);
			returnMaterialApply.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", returnMaterialApply);
		}
		else{
			this.rendJson(false,null, "记录不存在！");
		}
	}
	
	
	@PowerBind(code={"A1_3_E"},funcName="编辑")
	public void del(){
		try {
			getId();
			ReturnMaterialApply.dao.delHeDui(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
}
