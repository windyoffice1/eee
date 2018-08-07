package net.loyin.ctrl.scm;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.scm.StockAllot;
import net.loyin.model.scm.StockAllotList;
import net.loyin.model.sso.SnCreater;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.plugin.activerecord.Record;

/**
 * 调拨
 * 
 * @author 龙影 2014年9月29日
 */
@RouteBind(path = "stockAllot",sys="仓库",model="调拨")
public class StockAllotCtrl extends AdminBaseController<StockAllot> {
	public StockAllotCtrl() {
		this.modelClass = StockAllot.class;
	}
	public void dataGrid(){
		Map<String,String>userMap=this.getUserMap();
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("company_id",userMap.get("company_id"));
		filter.put("keyword",this.getPara("keyword"));
		filter.put("start_date",this.getPara("start_date"));
		filter.put("end_date",this.getPara("end_date"));
		filter.put("submit_status",this.getParaToInt("submit_status"));
		filter.put("uid",this.getPara("uid"));
		filter.put("is_deleted",this.getParaToInt("is_deleted"));
		filter.put("user_id",userMap.get("uid"));
		filter.put("position_id",userMap.get("position_id"));
		this.sortField(filter);
		this.rendJson(true, null, "",StockAllot.dao.page(this.getPageNo(),this.getPageSize(),filter,this.getParaToInt("qryType")));
	}
	@PowerBind(code="A4_1_E",funcName="删除")
	public void del(){
		try {
			getId();
			if(StockAllot.dao.isSubmit(id)){
				this.rendJson(false, null, "已经存在已提交的调拨单，请重新选择需要删除的数据！");
				return;
			}
			StockAllot.dao.del(id,this.getCompanyId());
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	public void qryOp() {
		getId();
		StockAllot m = StockAllot.dao.findById(id, this.getCompanyId());
		if (m != null){
			List<StockAllotList>productlist=StockAllotList.dao.list(id);
			m.put("productlist",productlist);
			m.put("productlistlength",productlist.size());
			this.rendJson(true,null, "", m);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	@PowerBind(code="A4_1_E",funcName="回收站")
	public void trash() {
		try {
			getId();
			if(StockAllot.dao.isSubmit(id)){
				this.rendJson(false, null, "已经存在已提交的调拨单，请重新选择需要删除的数据！");
				return;
			}
			StockAllot.dao.trash(id,this.getCurrentUserId(),this.getCompanyId(),dateTimeFormat.format(new Date()));
			rendJson(true,null,"移动到回收站成功！",id);
		} catch (Exception e) {
			log.error("移动到回收站异常", e);
			rendJson(false,null,"移动到回收站失败！");
		}
	}
	/**恢复*/
	@PowerBind(code="A4_1_E",funcName="恢复")
	public void reply() {
		try {
			getId();
			StockAllot.dao.reply(id,this.getCompanyId());
			rendJson(true,null,"恢复成功！",id);
		} catch (Exception e) {
			log.error("恢复异常", e);
			rendJson(false,null,"恢复失败！");
		}
	}
	/**提交*/
	@PowerBind(code="A4_1_E",funcName="提交")
	public void submit() {
		try {
			getId();
			if(StockAllot.dao.isSubmit(id)){
				this.rendJson(false, null, "已经提交了，请勿重复提交！");
				return;
			}
			String[] sn=StockAllot.dao.submit(id,this.getCompanyId(),this.getCurrentUserId(),dateFormat.format(new Date()),dateTimeFormat.format(new Date()));
			rendJson(true,null,"提交操作成功！出库单号："+sn[1]+"，入库单号："+sn[1],id);
		} catch (Exception e) {
			log.error("提交异常", e);
			rendJson(false,null,"操作失败！");
		}
	}
	@PowerBind(code="A4_1_E",funcName="编辑")
	public void save() {
		try {
			StockAllot po = (StockAllot) getModel();
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			getId();
			String company_id=this.getCompanyId();
			String pname="productlist";
			Integer productlistlength=this.getParaToInt("productlistlength");
			List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
			for(int i=0;i<productlistlength;i++){
				Map<String,Object> attr=new HashMap<String,Object>();
				attr.put("product_id",this.getAttr(pname+"["+i+"][product_id]"));
				attr.put("amount",this.getAttr(pname+"["+i+"][amount]"));
				productlist.add(attr);
			}
			this.pullUser(po, this.getCurrentUserId());
			String sn="";
			if (StringUtils.isEmpty(id)) {
				sn=SnCreater.dao.create("STOCKALLOT", company_id);
				po.set("billsn",sn);
				po.set("company_id",company_id);
				po.save();
				id=po.getStr("id");
			} else {
				po.update();
			}
			Map<String,String> data=new HashMap<String,String>();
			data.put("id",id);
			data.put("sn",po.getStr("billsn"));
			StockAllotList.dao.insert(productlist, id);
			this.rendJson(true,null, "操作成功！",data);
		} catch (Exception e) {
			log.error("保存订单异常", e);
			this.rendJson(false,null, "保存数据异常！");
		}
	}
	/**调拨明细表*/
	public void rptList(){
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("product_id",this.getPara("product_id"));
		filter.put("head_id",this.getPara("head_id"));
		filter.put("out_depot_id",this.getPara("out_depot_id"));
		filter.put("to_depot_id",this.getPara("to_depot_id"));
		filter.put("start_date",this.getPara("start_date"));
		filter.put("end_date",this.getPara("end_date"));
		filter.put("company_id",this.getCompanyId());
		List<Record> list=StockAllot.dao.rptList(filter);
		this.rendJson(true, null, "",list);
	}
}
