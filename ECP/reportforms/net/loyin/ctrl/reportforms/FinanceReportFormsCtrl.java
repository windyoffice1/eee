package net.loyin.ctrl.reportforms;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

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
		if(type.equals("0")){
			//按照物料大类统计
			List<ReportFormsMonthlyStatement> list=ReportFormsMonthlyStatement.dao.monthlyStatementStatisticsByMaterialBroad(year, begin_month, end_month, material_data_no);
			this.rendJson(true,null, "查询成功", list);
		}else{
			//按照船站统计
			List<ChildWarehouse> list=ChildWarehouse.dao.monthlyStatementStatisticsByChildWarehouse(year,begin_month);
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
	
	/***
	 * 收发结存表
	 */
	public void putInAndOutMonthlyStatement() {
		//期初金额
		BigDecimal begin_amount=BigDecimal.ZERO;
		Boolean beginAmountHasChange=false;
		//入库数量合计
		BigDecimal total_put_instorage_amount=BigDecimal.ZERO;
		//入库金额合计
		BigDecimal total_put_instorage_money=BigDecimal.ZERO;
		//出库数量合计
		BigDecimal total_outputstorage_amount=BigDecimal.ZERO;
		//出库金额合计
		BigDecimal total_outputstorage_money=BigDecimal.ZERO;
		String year=this.getAttrForStr("year");
		//year="2018";
		String month=this.getAttrForStr("month");
		//month="07";
		String material_data_no=this.getAttrForStr("material_data_no");
		//material_data_no="430307";
		String material_data_name=this.getAttrForStr("material_data_name");
		List<ReportFormsMonthlyStatement> list=ReportFormsMonthlyStatement.dao.findMonthlyStatementByDateAndMaterialNo(year, month,material_data_no , material_data_name);
		if(CollectionUtils.isEmpty(list)) {
			this.rendJson(true,null, "查询数据为空", "");
		}else {
			List<ReportFormsMonthlyStatement> resultList=new ArrayList<ReportFormsMonthlyStatement>();
			//取第一条数据
			ReportFormsMonthlyStatement reportFormsMonthlyStatement=list.get(0);
			//物料ID
			String material_data_id=reportFormsMonthlyStatement.getStr("material_data_id");
			//根据ID查询表格第一行数据
			List<ReportFormsMonthlyStatement> firstList=ReportFormsMonthlyStatement.dao.findFirstPutInAndOutMonthlyStatement(material_data_id,year,month);
			//根据ID查询入库月结存数据
			resultList.addAll(ReportFormsMonthlyStatement.dao.findPutInMonthlyStatement(material_data_id,year,month));
			//根据ID查询出库月结存数据
			resultList.addAll(ReportFormsMonthlyStatement.dao.findPutOutMonthlyStatement(material_data_id,year,month));
			//将出入库结存数据按出入库时间正排序
			Collections.sort(resultList, new Comparator<ReportFormsMonthlyStatement>() {
				@Override
				public int compare(ReportFormsMonthlyStatement o1, ReportFormsMonthlyStatement o2) {
					try {
						if(new SimpleDateFormat("yyyy-MM-dd").parse(o1.getStr("put_date")).before(new SimpleDateFormat("yyyy-MM-dd").parse(o2.getStr("put_date")))) {
							return -1;
						}else {
							return 1;
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
					return 0;
				}
			});
			//设置第一行结存表的期未数据
			resultList.add(0, firstList.get(0));
			for (int i = 0; i < resultList.size(); i++) {
				ReportFormsMonthlyStatement currentData=resultList.get(i);
				String put_flag=currentData.getStr("put_flag");
				if(i==0) {
					begin_amount=currentData.getBigDecimal("begin_amount");
				}else {
					//入库
					if("In".equals(put_flag)) {
						BigDecimal put_instorage_amount=currentData.getBigDecimal("put_instorage_amount")==null?BigDecimal.ZERO:currentData.getBigDecimal("put_instorage_amount");
						BigDecimal put_instorage_money=currentData.getBigDecimal("put_instorage_money")==null?BigDecimal.ZERO:currentData.getBigDecimal("put_instorage_money");
						begin_amount=begin_amount.add(put_instorage_amount);
						total_put_instorage_amount=total_put_instorage_amount.add(put_instorage_amount);
						total_put_instorage_money=total_put_instorage_money.add(put_instorage_money);
						currentData.set("end_amount", begin_amount);
						beginAmountHasChange=true;
					}
					//出库
					if("Out".equals(put_flag)) {
						BigDecimal outputstorage_amount=currentData.getBigDecimal("outputstorage_amount")==null?BigDecimal.ZERO:currentData.getBigDecimal("outputstorage_amount");
						BigDecimal outputstorage_money=currentData.getBigDecimal("outputstorage_money")==null?BigDecimal.ZERO:currentData.getBigDecimal("outputstorage_money");
						begin_amount=begin_amount.subtract(outputstorage_amount);
						total_outputstorage_amount=total_outputstorage_amount.add(outputstorage_amount);
						total_outputstorage_money=total_outputstorage_money.add(outputstorage_money);
						currentData.set("end_amount", begin_amount);
						beginAmountHasChange=true;
					}
				}
			}
			//设置结存表最后一行数据
			List<ReportFormsMonthlyStatement> lastList=ReportFormsMonthlyStatement.dao.findLastPutInAndOutMonthlyStatement(material_data_id,year,month);
			ReportFormsMonthlyStatement lastReportFormsMonthlyStatement=lastList.get(0);
			//设置最后合计行信息
			lastReportFormsMonthlyStatement.put("end_amount",beginAmountHasChange?resultList.get(resultList.size()-1).get("end_amount"):begin_amount);
			lastReportFormsMonthlyStatement.put("put_instorage_amount",total_put_instorage_amount);
			lastReportFormsMonthlyStatement.put("put_instorage_money",total_put_instorage_money);
			lastReportFormsMonthlyStatement.put("outputstorage_amount",total_outputstorage_amount);
			lastReportFormsMonthlyStatement.put("outputstorage_money",total_outputstorage_money);
			resultList.add(resultList.size(),lastReportFormsMonthlyStatement);
			this.rendJson(true,null, "查询成功", resultList);
		}
		
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
