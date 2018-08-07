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
import net.loyin.model.inventorymanager.Check;
import net.loyin.model.inventorymanager.CheckData;
import net.loyin.model.sso.Person;

@RouteBind(path="check",sys="存货管理",model="报废管理")
public class CheckCtrl extends AdminBaseController<Check>{
	public CheckCtrl(){
		this.modelClass=Check.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		//盘点仓库
		String warehouse_id=this.getPara("warehouse_id");
		filter.put("warehouse_id",warehouse_id);
		//盘点状态，【0：盘亏，1：盘盈】
		String status=this.getPara("status");
		filter.put("status",status);
		//排序
		this.sortField(filter);
		
		Page<Check> page = Check.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	@PowerBind(code={"A2_3_V"},funcName="查询")
	public void qryOp() {
		getId();
		Check check = Check.dao.findById(id, this.getCompanyId());
		if (check != null){
			List<CheckData> productlist=CheckData.dao.list(id);
			check.put("productlist",productlist);
			check.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", check);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}

	/**
	 * 新增或修改报废信息
	 */	
	@SuppressWarnings("unused")
	@PowerBind(code={"A2_3_E"},funcName="编辑")
	public void save() {
		try {
			getId();
			Person person = new Person();
			Map<String, Object> perMap = person.qryById(getCurrentUserId());
			Check po = this.getModel();
			String warehouse_id=this.getAttrForStr("warehouse_id");
			Integer deviation_total_money=Integer.parseInt(this.getAttrForStr("deviation_total_money"));
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			//查询仓位
			Warehouselocation warehouse=Warehouselocation.dao.findById(warehouse_id);
			String company_id=this.getCompanyId();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			SimpleDateFormat sdformet = new SimpleDateFormat("yyyyMMdd");
			String uuid = UUID.randomUUID().toString();
			String pname="productlist";
			this.pullUser(po, this.getCurrentUserId());
			if (StringUtils.isEmpty(id)) {
				po.set("id", uuid);
				//组装盘点名称
				po.set("check_name", warehouse.get("warehouse_name")+"的盘点记录"+sdformet.format(new Date()));
				po.set("company_id", this.getCompanyId());
				po.set("operate_user_id",this.getCurrentUserId());
				String status="0";
				if(deviation_total_money>0){
					status="1";//盘亏
				}else if(deviation_total_money==0){
					status="2";//持平
				}else if(deviation_total_money<0){
					status="0";//盘盈
				}
				po.set("status", status);
				po.save();
			}
			Integer productlistlength=this.getParaToInt("productlistlength");
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("id",UUID.randomUUID().toString());
				attr.put("inventory_check_id",po.get("id"));
				attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
				attr.put("target_price",this.getAttr(pname+"["+i+"][target_price]"));
				//原库存数量
				attr.put("original_amount",this.getAttr(pname+"["+i+"][original_amount]"));
				//现库存数量
				attr.put("now_amount",this.getAttr(pname+"["+i+"][now_amount]"));
				//偏差数量
				attr.put("deviation_amount",this.getAttr(pname+"["+i+"][deviation_amount]"));
				//偏差金额
				attr.put("deviation_total_money",this.getAttr(pname+"["+i+"][deviation_total_money]"));
				attr.put("comment",this.getAttr(pname+"["+i+"][comment]"));
				productlist.add(attr);
				//物料盘点
				Check.dao.check(po.getStr("warehouse_id"),this.getAttrForStr(pname+"["+i+"][material_data_id]"),this.getAttrForStr(pname+"["+i+"][now_amount]"));
			}
			CheckData.dao.insert(productlist);
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			this.rendJson(true,null, "盘点成功！",data);
		} catch (Exception e) {
			log.error("盘点异常", e);
			this.rendJson(false,null, "盘点异常！");
			e.printStackTrace();
		}
	}
	
	
	
	@PowerBind(code={"A2_3_E"},funcName="删除")
	public void del(){
		try {
			getId();
			Check.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
			e.printStackTrace();
		}
	}
	
}
