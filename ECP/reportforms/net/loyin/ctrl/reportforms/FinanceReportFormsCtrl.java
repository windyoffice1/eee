package net.loyin.ctrl.reportforms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.ChildWarehouse;
import net.loyin.model.getmaterial.GetMaterialApply;
import net.loyin.model.reportforms.ChildWarehouseMaterialType;
import net.loyin.model.reportforms.ReportFormsMonthlyStatement;
import net.loyin.model.reportforms.PurchaseReportForms;
/**
 * 
 * @author:lizhangyou
 * @Description: 财务报表</p>* 
 * @date 2017-10-10
 */
@RouteBind(path="finance",sys="表报",model="财务报表")
public class FinanceReportFormsCtrl extends AdminBaseController<PurchaseReportForms> {

	public FinanceReportFormsCtrl(){
		this.modelClass=PurchaseReportForms.class;
	}
	/**
	 * 月结表
	 */
	public void doMonthlyStatement(){
		String year=this.getAttrForStr("year");
		String month=this.getAttrForStr("month");
		String material_data_no=this.getAttrForStr("material_data_no");
		//判断查询的月份是否有月结
		List<ReportFormsMonthlyStatement> isMonthlyStatementList=ReportFormsMonthlyStatement.dao.isMonthlyStatement(year, month,"");
		if(isMonthlyStatementList!=null&&isMonthlyStatementList.size()>0){
			List<ReportFormsMonthlyStatement> MonthlyStatementList=ReportFormsMonthlyStatement.dao.isMonthlyStatement(year, month,material_data_no);
			this.rendJson(true,null, "已月结", MonthlyStatementList);
		}else{
			List<ReportFormsMonthlyStatement> list=ReportFormsMonthlyStatement.dao.doMonthlyStatement(year,month);
			this.rendJson(true,null, "未月结", list);
		}
	}
	/**
	 * 插入月结表
	 */
	@PowerBind(code={"A7_4_E"},funcName="月结")
	public void save(){
		String year=this.getPara("year");
		String month =this.getPara("month");
		/*		List<Map<String,Object>> productlist=new ArrayList<Map<String,Object>>();
		String pname="list";
		for(int i=0;i<material_length;i++){
			Map<String,Object> attr=new HashMap<String,Object>();
			attr.put("id",this.getAttr(pname+"["+i+"][id]"));
			attr.put("year",this.getAttr(pname+"["+i+"][year]"));
			attr.put("month",this.getAttr(pname+"["+i+"][month]"));
			attr.put("material_data_id",this.getAttr(pname+"["+i+"][material_data_id]"));
			attr.put("material_data_no",this.getAttr(pname+"["+i+"][material_data_no]"));
			attr.put("material_data_name",this.getAttr(pname+"["+i+"][material_data_name]"));
			attr.put("unit",this.getAttr(pname+"["+i+"][unit]"));
			attr.put("model_number",this.getAttr(pname+"["+i+"][model_number]"));
			attr.put("belong_to_broad_name",this.getAttr(pname+"["+i+"][belong_to_broad_name]"));
			attr.put("putinstorage_amount",this.getAttr(pname+"["+i+"][putinstorage_amount]"));
			attr.put("putinstorage_money",this.getAttr(pname+"["+i+"][putinstorage_money]"));
			attr.put("outputstorage_amount",this.getAttr(pname+"["+i+"][outputstorage_amount]"));
			attr.put("outputstorage_money",this.getAttr(pname+"["+i+"][outputstorage_money]"));
			attr.put("begin_amount",this.getAttr(pname+"["+i+"][begin_amount]"));
			attr.put("begin_money",this.getAttr(pname+"["+i+"][begin_money]"));
			attr.put("end_price",this.getAttr(pname+"["+i+"][end_price]"));
			attr.put("end_amount",this.getAttr(pname+"["+i+"][end_amount]"));
			attr.put("end_money",this.getAttr(pname+"["+i+"][end_money]"));
			attr.put("operate_date",dateFormat.format(new Date()));
			attr.put("belong_to_broad_id",this.getAttr(pname+"["+i+"][belong_to_broad_id]"));
			attr.put("belong_to_broad_no",this.getAttr(pname+"["+i+"][belong_to_broad_no]"));
			productlist.add(attr);
		}*/
		//查询上个月是否有月结
		if(StringUtils.isNotBlank(year)&&StringUtils.isNotBlank(month)){
			int yearInt=Integer.parseInt(year);
			int monthInt=Integer.parseInt(month);
			if(month=="1"){
				yearInt=Integer.parseInt(year)-1;
				monthInt=12;
			}else{
				monthInt=monthInt-1;
			}
			List<ReportFormsMonthlyStatement> isMonthlyStatementList=ReportFormsMonthlyStatement.dao.isMonthlyStatement(yearInt+"", monthInt+"","");
			if(isMonthlyStatementList!=null&&isMonthlyStatementList.size()>0){
				List<ReportFormsMonthlyStatement> productlist=ReportFormsMonthlyStatement.dao.doMonthlyStatement(year,month);
				ReportFormsMonthlyStatement.dao.insert(productlist);
				//重新设计物料的平均价格8
				ReportFormsMonthlyStatement.dao.MaterialAveragePrice(productlist);
				this.rendJson(true,null, "月结成功！", "");
			}else{
				this.rendJson(true,null, "月结失败，请先将上个月的物料月结！", "");
			}
		}else{
			this.rendJson(true,null, "月结失败，月结年份和月份不能为空！", "");
		}
	}
	/**
	 * 月结统计表
	 */
	public void monthlyStatementStatistics(){
		//type【0：按照物料大类，1：按照船站】
		String type=this.getAttrForStr("type");
		String year=this.getAttrForStr("year");
		String begin_month=this.getAttrForStr("begin_month");
		String end_month=this.getAttrForStr("end_month");
		String material_data_no=this.getAttrForStr("material_data_no");
		String child_warehouse_id=this.getAttrForStr("child_warehouse_id");
		if(type.equals("0")){
			//按照物料大类统计
			List<ReportFormsMonthlyStatement> list=ReportFormsMonthlyStatement.dao.monthlyStatementStatisticsByMaterialBroad(year, begin_month, end_month, material_data_no);
			this.rendJson(true,null, "查询成功", list);
		}else{
			//按照船站统计
			List<ChildWarehouse> list=ChildWarehouse.dao.monthlyStatementStatisticsByChildWarehouse(year,begin_month,child_warehouse_id);
			List<ChildWarehouseMaterialType> list1=new ArrayList<ChildWarehouseMaterialType>();
			for(int i=0; i<list.size();i++){
				if(StringUtils.isNotBlank(list.get(i).getStr("child_warehouse_name"))){
					ChildWarehouseMaterialType po=new ChildWarehouseMaterialType();
					Map<String,BigDecimal> map=new HashMap<String, BigDecimal>();
					if(i==0){
						po.setChild_material_name(list.get(i).getStr("child_warehouse_name"));
						map.put(list.get(i).getStr("name"), list.get(i).getBigDecimal("total_money"));
						po.setTypeAndMoney(map);
						list1.add(po);
					}else{
						ChildWarehouse childWarehouse=list.get(i-1);
						ChildWarehouse warehouse=list.get(i);
						if(warehouse.getStr("child_warehouse_name").equals(childWarehouse.getStr("child_warehouse_name"))){
							for(int j=0;j<list1.size();j++){
								ChildWarehouseMaterialType vo = list1.get(j);
								if(vo.getChild_material_name().equals(warehouse.getStr("child_warehouse_name"))){
									map=vo.getTypeAndMoney();
									map.put(warehouse.getStr("name"), warehouse.getBigDecimal("total_money"));
									vo.setTypeAndMoney(map);
									list1.remove(j);
									list1.add(j, vo);
								}
							}
						}else{
							po.setChild_material_name(warehouse.getStr("child_warehouse_name"));
							map.put(warehouse.getStr("name"), warehouse.getBigDecimal("total_money"));
							po.setTypeAndMoney(map);
							list1.add(po);
						}
					}
				}
			}
			this.rendJson(true,null, "查询成功", list1);
		}
	}
	
	/**
	 * 年修领料报表
	 */
	public void getMaterialForRepair(){
		String begin_year=this.getAttrForStr("begin_year");
		String end_year=this.getAttrForStr("end_year");
		String child_warehouse_id=this.getAttrForStr("child_warehouse_id");
		List<GetMaterialApply> list=GetMaterialApply.dao.getmaterialForRepair(begin_year, end_year, child_warehouse_id);
		this.rendJson(true,null, "查询成功", list);
	}
	
	/**
	 * 收发明细表
	 */
	public void putinAndOutputMaterialDetial(){
		String year=this.getAttrForStr("year");
		String month=this.getAttrForStr("month");
		String material_broad_id=this.getAttrForStr("material_broad_id");
		//单据类型【入库单，出库单】
		String type=this.getAttrForStr("type");
		String material_data_no=this.getAttrForStr("material_data_no");
		String child_warehouse_id=this.getAttrForStr("child_warehouse_id");
		List<ReportFormsMonthlyStatement> list=ReportFormsMonthlyStatement.dao.putinAndOutputMaterialDetial(year, month, material_broad_id, type, material_data_no, child_warehouse_id);
		this.rendJson(true,null, "查询成功", list);
	}

	/**
	 * 年结统计表
	 */
	public void yearStatementStatistics(){
		//type【0：按照物料大类，1：按照船站】
		String type=this.getAttrForStr("type");
		String year=this.getAttrForStr("year");
		String begin_month=this.getAttrForStr("begin_month");
		String end_month=this.getAttrForStr("end_month");
		String material_data_no=this.getAttrForStr("material_data_no");
		String child_warehouse_id=this.getAttrForStr("child_warehouse_id");
		if(type.equals("0")){
			//按照物料大类统计
			List<ReportFormsMonthlyStatement> list=ReportFormsMonthlyStatement.dao.yearStatementStatisticsByMaterialBroad(year, begin_month, end_month, material_data_no);
			this.rendJson(true,null, "查询成功", list);
		}else{
			//按照船站统计
			List<ChildWarehouse> list=ChildWarehouse.dao.yearStatementStatisticsByChildWarehouse(year,begin_month,end_month,child_warehouse_id);
			List<ChildWarehouseMaterialType> list1=new ArrayList<ChildWarehouseMaterialType>();
			for(int i=0; i<list.size();i++){
				if(StringUtils.isNotBlank(list.get(i).getStr("child_warehouse_name"))){
					ChildWarehouseMaterialType po=new ChildWarehouseMaterialType();
					Map<String,BigDecimal> map=new HashMap<String, BigDecimal>();
					if(i==0){
						po.setChild_material_name(list.get(i).getStr("child_warehouse_name"));
						map.put(list.get(i).getStr("name"), list.get(i).getBigDecimal("total_money"));
						po.setTypeAndMoney(map);
						list1.add(po);
					}else{
						ChildWarehouse childWarehouse=list.get(i-1);
						ChildWarehouse warehouse=list.get(i);
						if(warehouse.getStr("child_warehouse_name").equals(childWarehouse.getStr("child_warehouse_name"))){
							for(int j=0;j<list1.size();j++){
								ChildWarehouseMaterialType vo = list1.get(j);
								if(vo.getChild_material_name().equals(warehouse.getStr("child_warehouse_name"))){
									map=vo.getTypeAndMoney();
									map.put(warehouse.getStr("name"), warehouse.getBigDecimal("total_money"));
									vo.setTypeAndMoney(map);
									list1.remove(j);
									list1.add(j, vo);
								}
							}
						}else{
							po.setChild_material_name(warehouse.getStr("child_warehouse_name"));
							map.put(warehouse.getStr("name"), warehouse.getBigDecimal("total_money"));
							po.setTypeAndMoney(map);
							list1.add(po);
						}
					}
				}
			}
			this.rendJson(true,null, "查询成功", list1);
		}
	}
}
