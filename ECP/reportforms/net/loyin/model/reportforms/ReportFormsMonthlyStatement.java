package net.loyin.model.reportforms;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang.StringUtils;

import net.loyin.jfinal.anatation.TableBind;
import net.loyin.model.basicdata.MaterialData;
import net.loyin.model.ordermanager.GetMaterialOutputStorageData;
import net.loyin.model.ordermanager.PutInStorageRealData;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
@TableBind(name="report_forms_monthly_statement")
public class ReportFormsMonthlyStatement extends Model<ReportFormsMonthlyStatement> {
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="report_forms_monthly_statement";
	public static ReportFormsMonthlyStatement dao = new ReportFormsMonthlyStatement();
		
	/**
	 * 判断查询的月份是否有月结
	 */
	public List<ReportFormsMonthlyStatement> isMonthlyStatement(String year ,String month,String material_data_no ){
		StringBuffer sql= new StringBuffer("SELECT * FROM report_forms_monthly_statement WHERE year=? AND month=? ");
		List<String> params= new ArrayList<String>();
		month= month.length()==1 ? "0"+month:month;
		params.add(year);
		params.add(month);
		if(StringUtils.isNotBlank(material_data_no)){
			sql.append(" AND material_data_no=? ");
			params.add(material_data_no);
		}
		sql.append(" ORDER BY material_data_no ");
		return dao.find(sql.toString(), params.toArray());
	}
	
	/**
	 * 新增月结记录
	 */
	public List<ReportFormsMonthlyStatement> doMonthlyStatement(String year,String month){
		month=month.length()==1 ? "0"+month:month;
		String putInStorageSql="SELECT material_data_id,convert(numeric(18, 2),SUM(amount)) AS putinstorage_amount,convert(numeric(18, 2),SUM(amount*purchase_price)) AS putinstorage_money "
								+" FROM scm_putinstorage_real_data WHERE LEFT(putinstorage_date,7)=?  GROUP BY material_data_id";
		//查询当月入库情况
		List<PutInStorageRealData> putInStorageList=PutInStorageRealData.dao.find(putInStorageSql, year+"-"+month);
		
		String outPutStorageSql="SELECT material_data_id,convert(numeric(18, 2),SUM(amount)) AS outputstorage_amount,convert(numeric(18, 2),SUM(amount*purchase_price)) AS outputstorage_money "
								+"FROM getmaterial_outputstorage_data WHERE LEFT(outputstorage_date,7)=? GROUP BY material_data_id";
		//查询当月出库情况
		List<GetMaterialOutputStorageData> outPutStorageList=GetMaterialOutputStorageData.dao.find(outPutStorageSql, year+"-"+month);
		
		String lastMonthlyStatementSql="SELECT * FROM report_forms_monthly_statement WHERE year=? AND month=? ";
		//查询上次月结情况
		List<ReportFormsMonthlyStatement> lastMonthlyStatement=null;
		//当月份等于1时,上月为：年份减1，月份为十二月
		if(Integer.parseInt(month)==1){
			//获取上个月的月结记录
			lastMonthlyStatement=dao.find(lastMonthlyStatementSql, Integer.parseInt(year)-1,12);
		}else{
			Integer ss = Integer.parseInt(month)-1;
			lastMonthlyStatement=dao.find(lastMonthlyStatementSql, year,ss<10 ? "0"+ss:ss);
		}
		//查询出所有物料信息
		List<MaterialData> MaterialDataList=MaterialData.dao.find("SELECT bmd.*,bmb.material_name AS belong_to_broad_name,bmb.id AS belong_to_broad_id,"
					+"bmb.material_no AS belong_to_broad_no FROM basic_material_data bmd LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id ORDER BY bmd.material_no ");
		//组装月结对象
		List<ReportFormsMonthlyStatement> list=new ArrayList<ReportFormsMonthlyStatement>();
		for(int i=0;i<MaterialDataList.size();i++){
			ReportFormsMonthlyStatement reportFormsMonthlyStatement=new ReportFormsMonthlyStatement();
			reportFormsMonthlyStatement.set("id",UUID.randomUUID().toString());
			reportFormsMonthlyStatement.set("year",year);
			reportFormsMonthlyStatement.set("month",month);
			reportFormsMonthlyStatement.set("material_data_id", MaterialDataList.get(i).getStr("id"));
			reportFormsMonthlyStatement.set("material_data_no", MaterialDataList.get(i).getStr("material_no"));
			reportFormsMonthlyStatement.set("material_data_name", MaterialDataList.get(i).getStr("material_name"));
			reportFormsMonthlyStatement.set("unit", MaterialDataList.get(i).getStr("unit"));
			reportFormsMonthlyStatement.set("model_number", MaterialDataList.get(i).getStr("model_number"));
			reportFormsMonthlyStatement.set("belong_to_broad_name", MaterialDataList.get(i).getStr("belong_to_broad_name"));
			reportFormsMonthlyStatement.set("belong_to_broad_id", MaterialDataList.get(i).getStr("belong_to_broad_id"));
			reportFormsMonthlyStatement.set("belong_to_broad_no", MaterialDataList.get(i).getStr("belong_to_broad_no"));
			//上月期末数据设置为本月期初数据
			if(lastMonthlyStatement!=null&&lastMonthlyStatement.size()>0){
				for(int j=0;j<lastMonthlyStatement.size();j++){
					//如果物料id等于出库物料
					if(MaterialDataList.get(i).getStr("id").equals(lastMonthlyStatement.get(j).getStr("material_data_id"))){
						reportFormsMonthlyStatement.set("begin_amount", lastMonthlyStatement.get(j).getBigDecimal("end_amount"));
						reportFormsMonthlyStatement.set("begin_money", lastMonthlyStatement.get(j).getBigDecimal("end_money"));
					}
				}
			}else{
				reportFormsMonthlyStatement.set("begin_amount",new BigDecimal("0.00"));
				reportFormsMonthlyStatement.set("begin_money", new BigDecimal("0.00"));
			}
			if(reportFormsMonthlyStatement.getBigDecimal("begin_amount")==null){
				reportFormsMonthlyStatement.set("begin_amount",new BigDecimal("0.00"));
				reportFormsMonthlyStatement.set("begin_money", new BigDecimal("0.00"));
			}
			//本月入库数据
			for(int j=0;j<putInStorageList.size();j++){
				if(MaterialDataList.get(i).getStr("id").equals(putInStorageList.get(j).getStr("material_data_id"))){
					reportFormsMonthlyStatement.set("putinstorage_amount", putInStorageList.get(j).getBigDecimal("putinstorage_amount"));
					reportFormsMonthlyStatement.set("putinstorage_money", putInStorageList.get(j).getBigDecimal("putinstorage_money"));
				}
			}
			if(reportFormsMonthlyStatement.getBigDecimal("putinstorage_amount")==null){
				reportFormsMonthlyStatement.set("putinstorage_amount", new BigDecimal("0.00"));
				reportFormsMonthlyStatement.set("putinstorage_money",new BigDecimal("0.00"));
			}
			BigDecimal end_amount_js=new BigDecimal(0.00);
			//期末金额计算
			if(reportFormsMonthlyStatement.getBigDecimal("begin_amount")!=null&&reportFormsMonthlyStatement.getBigDecimal("putinstorage_amount")!=null){
				end_amount_js=reportFormsMonthlyStatement.getBigDecimal("begin_amount")
						.add(reportFormsMonthlyStatement.getBigDecimal("putinstorage_amount"));
			}else if(reportFormsMonthlyStatement.getBigDecimal("begin_amount")!=null&&reportFormsMonthlyStatement.getBigDecimal("putinstorage_amount")==null){
				end_amount_js=reportFormsMonthlyStatement.getBigDecimal("begin_amount");
			}else if(reportFormsMonthlyStatement.getBigDecimal("begin_amount")==null&&reportFormsMonthlyStatement.getBigDecimal("putinstorage_amount")!=null){
				end_amount_js=reportFormsMonthlyStatement.getBigDecimal("putinstorage_amount");
			}
			BigDecimal end_money_js=new BigDecimal(0.00);
			if(reportFormsMonthlyStatement.getBigDecimal("begin_money")!=null&&reportFormsMonthlyStatement.getBigDecimal("putinstorage_money")!=null){
				end_money_js=reportFormsMonthlyStatement.getBigDecimal("begin_money")
						.add(reportFormsMonthlyStatement.getBigDecimal("putinstorage_money"));
				
			}else if(reportFormsMonthlyStatement.getBigDecimal("begin_money")!=null&&reportFormsMonthlyStatement.getBigDecimal("putinstorage_money")==null){
				end_money_js=reportFormsMonthlyStatement.getBigDecimal("begin_money");
			}else if(reportFormsMonthlyStatement.getBigDecimal("begin_money")==null&&reportFormsMonthlyStatement.getBigDecimal("putinstorage_money")!=null){
				end_money_js=reportFormsMonthlyStatement.getBigDecimal("putinstorage_money");
			}

			BigDecimal end_price=new BigDecimal(0.00);
			if(end_amount_js!=null&&end_amount_js.compareTo(BigDecimal.ZERO)!=0){
				reportFormsMonthlyStatement.set("end_price", end_money_js.divide(end_amount_js,2));
				end_price = reportFormsMonthlyStatement.getBigDecimal("end_price");
				BigDecimal zoz= new BigDecimal("0.00");
				if(end_price.compareTo(zoz)==0){
					reportFormsMonthlyStatement.set("end_price", MaterialDataList.get(i).get("average_price"));
				}
			}else{
				reportFormsMonthlyStatement.set("end_price", MaterialDataList.get(i).get("average_price"));
			}
			//本月出库数据
			for(int j=0;j<outPutStorageList.size();j++){
				if(MaterialDataList.get(i).getStr("id").equals(outPutStorageList.get(j).getStr("material_data_id"))){
					reportFormsMonthlyStatement.set("outputstorage_amount", outPutStorageList.get(j).getBigDecimal("outputstorage_amount"));
					if(outPutStorageList.get(j).getBigDecimal("outputstorage_amount")!=null&&reportFormsMonthlyStatement.getBigDecimal("end_price")!=null){
						reportFormsMonthlyStatement.set("outputstorage_money",reportFormsMonthlyStatement.getBigDecimal("end_price").multiply(outPutStorageList.get(j).getBigDecimal("outputstorage_amount")));
					}else{
						reportFormsMonthlyStatement.set("outputstorage_money",new BigDecimal(0.00));
					}
				}
			}
			if(reportFormsMonthlyStatement.getBigDecimal("outputstorage_amount")==null){
				reportFormsMonthlyStatement.set("outputstorage_amount",new BigDecimal("0.00"));
				reportFormsMonthlyStatement.set("outputstorage_money", new BigDecimal("0.00"));
			}
			reportFormsMonthlyStatement.set("end_amount", end_amount_js.subtract(reportFormsMonthlyStatement.getBigDecimal("outputstorage_amount")));
			reportFormsMonthlyStatement.set("end_money", end_money_js.subtract(reportFormsMonthlyStatement.getBigDecimal("outputstorage_money")));
			list.add(reportFormsMonthlyStatement);
		}
		return list;
	}
	
	/**
	 * 确认月结，插入数据库
	 * @param list
	 */
	@Before(Tx.class)
	public void insert(List<ReportFormsMonthlyStatement> list){
		Object[][]paras=new Object[list.size()][21];
		int i=0;
		for (ReportFormsMonthlyStatement a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("year");
			String month = (String) a.get("month");
			if(StringUtils.isNotBlank(month)){
				paras[i][2] = month.length()==1 ? "0"+month:month;
			}
			paras[i][3] = a.get("material_data_id");
			paras[i][4] = a.get("material_data_no");
			paras[i][5] = a.get("material_data_name");
			paras[i][6] = a.get("unit");
			paras[i][7] = a.get("model_number");
			paras[i][8] = a.get("belong_to_broad_name");
			paras[i][9] = a.getBigDecimal("putinstorage_amount");
			paras[i][10] = a.getBigDecimal("putinstorage_money");
			paras[i][11] = a.getBigDecimal("outputstorage_amount");
			paras[i][12] = a.getBigDecimal("outputstorage_money");
			paras[i][13] = a.getBigDecimal("begin_amount");
			paras[i][14] = a.getBigDecimal("begin_money");
			paras[i][15] = a.getBigDecimal("end_price");
			paras[i][16] = a.getBigDecimal("end_amount");
			paras[i][17] = a.getBigDecimal("end_money");
			paras[i][18] = a.get("operate_date");
			paras[i][19] = a.get("belong_to_broad_id");
			paras[i][20] = a.get("belong_to_broad_no");
			BigDecimal end_money = a.getBigDecimal("begin_money").add(a.getBigDecimal("putinstorage_money")).subtract(a.getBigDecimal("outputstorage_money"));
			System.out.println(end_money.compareTo(a.getBigDecimal("end_money")));
			i++;
		}
		StringBuffer sql=new StringBuffer("INSERT INTO report_forms_monthly_statement(id,year,month,material_data_id,material_data_no,material_data_name,")
				.append("unit,model_number,belong_to_broad_name,putinstorage_amount,putinstorage_money,outputstorage_amount,")
				.append("outputstorage_money,begin_amount,begin_money,end_price,end_amount,end_money,operate_date,belong_to_broad_id,belong_to_broad_no) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
		Db.batch(sql.toString(),paras,list.size());
	}
	
	/**
	 * 重新设计物料平均价格(加权平均)
	 */
	public void MaterialAveragePrice(List<ReportFormsMonthlyStatement> list){
		StringBuffer sql=new StringBuffer("UPDATE basic_material_data SET average_price=? WHERE id=?  ");
		for(ReportFormsMonthlyStatement product:list){
			BigDecimal end_price=product.getBigDecimal("end_price");
			if(BigDecimal.ZERO.compareTo(end_price)==0){
			}else{
				String material_data_id= (String) product.get("material_data_id");
				Db.update(sql.toString(),end_price,material_data_id);
			}
		}
	}
	
	
	/**
SELECT belong_to_broad_name,belong_to_broad_no,SUM(begin_money) AS begin_money ,
SUM(putinstorage_money) AS putinstorage_money,
SUM(outputstorage_money) AS outputstorage_money,
SUM(end_money) AS end_money
FROM report_forms_monthly_statement WHERE 1=1 
AND material_data_id=?
AND year=?
AND month IN()
GROUP BY belong_to_broad_name,belong_to_broad_no
	 * 通过物料大类统计年结统计表
	 */
	public List<ReportFormsMonthlyStatement> yearStatementStatisticsByMaterialBroad(String year,String begin_month,String end_month,String material_data_no){
		List<String> params=new ArrayList<String>();
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT belong_to_broad_name,belong_to_broad_no,SUM(begin_money) AS begin_money , ");
		sql.append("SUM(putinstorage_money) AS putinstorage_money,SUM(outputstorage_money) AS outputstorage_money,");
		sql.append("SUM(end_money) AS end_money FROM report_forms_monthly_statement WHERE 1=1 ");
		if(StringUtils.isNotBlank(year)){
			sql.append(" AND year=? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(begin_month)&&StringUtils.isNotBlank(end_month)){
			sql.append(" AND month IN( ");
			Integer begin_month_int=Integer.parseInt(begin_month);
			Integer end_month_int=Integer.parseInt(end_month);
			if(begin_month_int<=end_month_int){
				for(int i=begin_month_int ; i<=end_month_int ; i++){
					if(i>=10){
						sql.append("'"+i+"',");
					}else{
						sql.append("'0"+i+"',");
					}
				}
			}
			sql.append(" '-') ");
		}
		if(StringUtils.isNotBlank(material_data_no)){
			sql.append(" AND material_data_no LIKE ? ");
			params.add("%"+material_data_no+"%");
		}
		sql.append(" GROUP BY belong_to_broad_name,belong_to_broad_no ");
		return dao.find(sql.toString(), params.toArray());
	}

	
	/**
SELECT belong_to_broad_name,belong_to_broad_no,SUM(begin_money) AS begin_money ,
SUM(putinstorage_money) AS putinstorage_money,
SUM(outputstorage_money) AS outputstorage_money,
SUM(end_money) AS end_money
FROM report_forms_monthly_statement WHERE 1=1 
AND material_data_id=?
AND year=?
AND month IN()
GROUP BY belong_to_broad_name,belong_to_broad_no
	 * 通过物料大类统计月结统计表
	 */
	public List<ReportFormsMonthlyStatement> monthlyStatementStatisticsByMaterialBroad(String year,String begin_month,String end_month,String material_data_no){
		List<String> params=new ArrayList<String>();
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT belong_to_broad_name,belong_to_broad_id,belong_to_broad_no,convert(numeric(18, 2),SUM(begin_money)) AS begin_money ,  ");
		sql.append("convert(numeric(18, 2),SUM(putinstorage_money)) AS putinstorage_money,convert(numeric(18, 2),SUM(outputstorage_money)) AS outputstorage_money,");
		sql.append("convert(numeric(18, 2),SUM(end_money)) AS end_money FROM report_forms_monthly_statement WHERE 1=1 ");
		if(StringUtils.isNotBlank(year)){
			sql.append(" AND year=? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(begin_month)){
			sql.append(" AND month IN( ");
			if(begin_month.length()==1){
				sql.append("'0"+begin_month+"',");
			}else{
				sql.append("'"+begin_month+"',");
			}
			sql.append(" '-') ");
		}
		if(StringUtils.isNotBlank(material_data_no)){
			sql.append(" AND material_data_no LIKE ? ");
			params.add("%"+material_data_no+"%");
		}
		sql.append(" GROUP BY belong_to_broad_name,belong_to_broad_no,belong_to_broad_id order by belong_to_broad_no ");
		return dao.find(sql.toString(), params.toArray());
	}

	
	
	
	/**
	 * 收发明细表
	 * 
	 * 收
	 * 	SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_broad_name, 
		bmd.material_name AS material_data_name,bmd.model_number,bmd.unit,sp.putinstorage_name as put_name,
		sprd.amount,sprd.purchase_price AS price,sprd.total_money,rfms.begin_amount,rfms.begin_money,
		rfms.end_amount,rfms.end_money 
		FROM scm_putinstorage_real_data sprd
		LEFT JOIN scm_putinstorage sp ON sprd.putinstorage_id=sp.id
		LEFT JOIN basic_material_data bmd ON sprd.material_data_id=bmd.id
		LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id
		LEFT JOIN (SELECT * FROM report_forms_monthly_statement WHERE year='2017' AND month='11') rfms ON rfms.material_data_id=bmd.id
		WHERE LEFT(sprd.putinstorage_date,7)='2017-11'
		AND bmd.material_no LIKE '%1%'
		AND bmb.id='1'
	 * 
	 * 
	 * 发
	 *  SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_broad_name,
		bmd.material_name AS material_data_name,bmd.model_number,bmd.unit,ga.getmaterial_name AS put_name,
		god.amount,god.purchase_price AS price,god.total_money,rfms.begin_amount,rfms.begin_money,
		rfms.end_amount,rfms.end_money 
		FROM getmaterial_outputstorage_data god
		LEFT JOIN getmaterial_apply ga ON god.getmaterial_apply_id=ga.id
		LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id
		LEFT JOIN basic_material_data bmd ON god.material_data_id=bmd.id
		LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id
		LEFT JOIN (SELECT * FROM report_forms_monthly_statement WHERE year='2017' AND month='11') rfms ON rfms.material_data_id=bmd.id
		WHERE LEFT(god.outputstorage_date,7)='2017-11'
		AND bmb.id=''
		AND bmd.material_no LIKE ''
		AND bcw.id=''
	 * @return 
	 * 
	 */
	public List<ReportFormsMonthlyStatement> putinAndOutputMaterialDetial(String year,String month,String material_broad_id,String type,String material_data_no,String child_warehouse_id){
		//查询入库记录
		List<ReportFormsMonthlyStatement> outputList=new ArrayList<ReportFormsMonthlyStatement>();
		List<ReportFormsMonthlyStatement> puinList=new ArrayList<ReportFormsMonthlyStatement>();
		List<ReportFormsMonthlyStatement> list=new ArrayList<ReportFormsMonthlyStatement>();
		month=month.length()==1 ? "0"+month:month;
		if("入库单".equals(type)||StringUtils.isBlank(type)){
			List<String> putinParams=new ArrayList<String>();
			StringBuffer putInStorageSql=new StringBuffer();
			putInStorageSql.append("SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_broad_name, ");
			putInStorageSql.append("bmd.material_name AS material_data_name,bmd.model_number,bmd.unit,sp.putinstorage_name as put_name, ");
			putInStorageSql.append("sprd.amount,sprd.purchase_price AS price,sprd.total_money,rfms.begin_amount,rfms.begin_money, rfms.end_amount,rfms.end_money ");
			putInStorageSql.append("FROM scm_putinstorage_real_data sprd LEFT JOIN scm_putinstorage sp ON sprd.putinstorage_id=sp.id ");
			putInStorageSql.append("LEFT JOIN basic_material_data bmd ON sprd.material_data_id=bmd.id ");
			putInStorageSql.append("LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id ");
			putInStorageSql.append("LEFT JOIN (SELECT * FROM report_forms_monthly_statement WHERE year=? AND month=? ) rfms ON rfms.material_data_id=bmd.id ");
			putinParams.add(year);
			putinParams.add(month);
			putInStorageSql.append("WHERE LEFT(sprd.putinstorage_date,7)= ? ");
			putinParams.add(year+"-"+month);
			if(StringUtils.isNotBlank(material_broad_id)){
				putInStorageSql.append(" AND bmb.id=? ");
				putinParams.add(material_broad_id);
			}
			if(StringUtils.isNotBlank(material_data_no)){
				putInStorageSql.append(" AND bmd.material_no LIKE ?  ");
				putinParams.add("%"+material_data_no+"%");
			}
			//查询入库记录
			puinList=dao.find(putInStorageSql.toString(), putinParams.toArray());
		}
		if("出库单".equals(type)||StringUtils.isBlank(type)){

			List<String> outputParams=new ArrayList<String>();
			StringBuffer outputStorageSql=new StringBuffer();
			outputStorageSql.append("SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_broad_name, ");
			outputStorageSql.append("bmd.material_name AS material_data_name,bmd.model_number,bmd.unit,ga.getmaterial_name AS put_name,");
			outputStorageSql.append("god.amount,god.purchase_price AS price,god.total_money,rfms.begin_amount,rfms.begin_money,rfms.end_amount,rfms.end_money ");
			outputStorageSql.append("FROM getmaterial_outputstorage_data god LEFT JOIN getmaterial_apply ga ON god.getmaterial_apply_id=ga.id ");
			outputStorageSql.append("LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id  ");
			outputStorageSql.append("LEFT JOIN basic_material_data bmd ON god.material_data_id=bmd.id ");
			outputStorageSql.append("LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id ");
			outputStorageSql.append("LEFT JOIN (SELECT * FROM report_forms_monthly_statement WHERE year=? AND month=? ) rfms ON rfms.material_data_id=bmd.id ");
			outputParams.add(year);
			outputParams.add(month);
			outputStorageSql.append("WHERE LEFT(god.outputstorage_date,7)=? ");
			outputParams.add(year+"-"+month);
			if(StringUtils.isNotBlank(material_broad_id)){
				outputStorageSql.append(" AND bmb.id=? ");
				outputParams.add(material_broad_id);
			}
			if(StringUtils.isNotBlank(material_data_no)){
				outputStorageSql.append(" AND bmd.material_no LIKE ?  ");
				outputParams.add("%"+material_data_no+"%");
			}
			if(StringUtils.isNotBlank(child_warehouse_id)){
				outputStorageSql.append(" AND bcw.id=? ");
				outputParams.add(child_warehouse_id);
			}
			//查询入库记录
			outputList=dao.find(outputStorageSql.toString(), outputParams.toArray());
		}
		list.addAll(outputList);
		list.addAll(puinList);
		return list;
	}
	
	/***
	 * 根据年，月，物料编号查询出物料月结信
	 */
	public List<ReportFormsMonthlyStatement> findMonthlyStatementByDateAndMaterialNo(String year,String month,String material_data_no,String material_data_name){
		List<String> params=new ArrayList<String>();
		StringBuilder sb=new StringBuilder();
		sb.append(" SELECT rfms.* FROM report_forms_monthly_statement rfms LEFT JOIN basic_material_data bmd ON bmd.id= rfms.material_data_id ");
		sb.append(" WHERE 1=1 ");
		if(StringUtils.isNotBlank(year)) {
			sb.append(" AND rfms.year =? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(month)) {
			month=month.length()==1?"0"+month:month;
			sb.append(" AND rfms.month=? ");
			params.add(month);
		}
		if(StringUtils.isNotBlank(material_data_no)) {
			sb.append(" AND bmd.material_no=? ");
			params.add(material_data_no);
		}
		if(StringUtils.isNotBlank(material_data_name)) {
			sb.append(" AND bmd.material_name LIKE ? ");
			params.add("%"+material_data_name+"%");
		}
		return dao.find(sb.toString(),params.toArray());
	}
	
	/**
	 * 根据物料ID查询月结收发明细表第一行数据
	 */
	public List<ReportFormsMonthlyStatement> findFirstPutInAndOutMonthlyStatement(String material_data_id,String year,String month) {
		List<String> params=new ArrayList<String>();
		StringBuilder sb=new StringBuilder(" SELECT bmd.material_no,bmb.material_name AS belong_to_broad_name,bmd.material_name,bmd.model_number, ");
		sb.append(" bmd.unit,'期初结余' AS put_desc, NULL AS put_date,rfms.begin_amount,rfms.begin_average_price,rfms.begin_money, ");
		sb.append(" NULL AS put_instorage_amount,NULL AS put_instorage_price,NULL AS put_instorage_money,NULL AS outputstorage_amount, ");
		sb.append(" NULL AS outputstorage_price,NULL AS outputstorage_money,NULL AS end_amount,NULL AS end_price,NULL AS end_money,NULL AS put_flag ");
		sb.append(" from report_forms_monthly_statement rfms JOIN basic_material_data bmd ON bmd.id= rfms.material_data_id JOIN basic_material_broad bmb ON bmd.belong_to_broad_id= bmb.id ");
		if(StringUtils.isNotBlank(material_data_id)) {
			sb.append(" where rfms.material_data_id=? ");
			params.add(material_data_id);
		}
		if(StringUtils.isNotBlank(year)) {
			sb.append(" AND rfms.year =? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(month)) {
			month=month.length()==1?"0"+month:month;
			sb.append(" AND rfms.month=? ");
			params.add(month);
		}
		return dao.find(sb.toString(), params.toArray());
	}
	
	/***
	 * 根据物料ID查询 入库结存明细表
	 */
	public List<ReportFormsMonthlyStatement> findPutInMonthlyStatement(String material_data_id,String year,String month){
		List<String> params=new ArrayList<String>();
		StringBuilder sb=new StringBuilder(" SELECT bmd.material_no,bmb.material_name AS belong_to_broad_name,bmd.material_name,bmd.model_number, ");
		sb.append(" bmd.unit,sp.putinstorage_name AS put_desc, sprd.putinstorage_date AS put_date,NULL as begin_amount,NULL as begin_average_price,NULL as begin_money, ");
		sb.append(" sprd.amount AS put_instorage_amount,sprd.purchase_price AS put_instorage_price,sprd.total_money AS put_instorage_money,NULL AS outputstorage_amount, ");
		sb.append(" NULL AS outputstorage_price,NULL AS outputstorage_money,NULL AS end_amount,NULL AS end_price,NULL AS end_money, 'In' AS put_flag ");
		sb.append(" from report_forms_monthly_statement rfms JOIN basic_material_data bmd ON bmd.id= rfms.material_data_id ");
		sb.append(" JOIN basic_material_broad bmb ON bmd.belong_to_broad_id= bmb.id JOIN scm_putinstorage_real_data sprd ON sprd.material_data_id=rfms.material_data_id ");
		sb.append(" JOIN scm_putinstorage sp ON sprd.putinstorage_id=sp.id ");
		if(StringUtils.isNotBlank(material_data_id)) {
			sb.append(" where rfms.material_data_id=? ");
			params.add(material_data_id);
		}
		if(StringUtils.isNotBlank(year)) {
			sb.append(" AND rfms.year =? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(month)) {
			month=month.length()==1?"0"+month:month;
			sb.append(" AND rfms.month=? ");
			params.add(month);
		}
		sb.append(" AND LEFT(sprd.putinstorage_date,7)=? ");
		params.add(year+"-"+month);
		return dao.find(sb.toString(), params.toArray());
	}
	
	/***
	 * 根据物料ID查询 出库结存明细表
	 */
	public List<ReportFormsMonthlyStatement> findPutOutMonthlyStatement(String material_data_id,String year,String month){
		List<String> params=new ArrayList<String>();
		StringBuilder sb=new StringBuilder();
		sb.append(" SELECT bmd.material_no,bmb.material_name AS belong_to_broad_name,bmd.material_name,bmd.model_number,bmd.unit,ga.getmaterial_name AS put_desc,");
		sb.append(" god.outputstorage_date AS put_date,NULL AS begin_amount,NULL AS begin_average_price,NULL AS begin_money,NULL AS put_instorage_amount,NULL AS put_instorage_price,");
		sb.append(" NULl AS put_instorage_money,god.amount AS outputstorage_amount,NULL AS outputstorage_price,NULL AS outputstorage_money,NULL AS end_amount,NULL AS end_price,NULL AS end_money,'Out' AS put_flag ");
		sb.append(" FROM report_forms_monthly_statement rfms JOIN basic_material_data bmd ON bmd.id= rfms.material_data_id JOIN basic_material_broad bmb ON bmd.belong_to_broad_id= bmb.id");
		sb.append(" JOIN getmaterial_outputstorage_data god ON god.material_data_id= rfms.material_data_id JOIN getmaterial_apply ga ON ga.id=god.getmaterial_apply_id ");
		if(StringUtils.isNotBlank(material_data_id)) {
			sb.append(" where rfms.material_data_id=? ");
			params.add(material_data_id);
		}
		if(StringUtils.isNotBlank(year)) {
			sb.append(" AND rfms.year =? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(month)) {
			month=month.length()==1?"0"+month:month;
			sb.append(" AND rfms.month=? ");
			params.add(month);
		}
		sb.append(" AND LEFT(god.outputstorage_date,7)=? ");
		params.add(year+"-"+month);
		return dao.find(sb.toString(), params.toArray());	
	}
	
	/***
	 * 根据物料ID查询月结存明细表最后一行
	 */
	public List<ReportFormsMonthlyStatement> findLastPutInAndOutMonthlyStatement(String material_data_id,String year,String month){
		List<String> params=new ArrayList<String>();
		StringBuilder sb=new StringBuilder();
		sb.append(" SELECT DISTINCT '合计' AS material_no,NULL AS belong_to_broad_name,NULL AS material_name,NULL AS model_number,NULL AS unit, NULL AS put_desc,NULL AS put_date,rfms.begin_amount,");
		sb.append(" NULL AS begin_average_price,rfms.begin_money,NULL AS put_instorage_amount,NULL AS put_instorage_price,NULL AS put_instorage_money,NULL AS outputstorage_amount,god.purchase_price AS outputstorage_price,");
		sb.append(" NULL AS outputstorage_money,NULL AS end_amount,rfms.end_price AS end_price,rfms.end_money AS end_money");
		sb.append(" FROM report_forms_monthly_statement rfms LEFT JOIN basic_material_data bmd ON bmd.id= rfms.material_data_id LEFT JOIN getmaterial_outputstorage_data god ON god.material_data_id= rfms.material_data_id ");
		if(StringUtils.isNotBlank(material_data_id)) {
			sb.append(" where rfms.material_data_id=? ");
			params.add(material_data_id);
		}
		if(StringUtils.isNotBlank(year)) {
			sb.append(" AND rfms.year =? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(month)) {
			month=month.length()==1?"0"+month:month;
			sb.append(" AND rfms.month=? ");
			params.add(month);
		}
		return dao.find(sb.toString(), params.toArray());	
	}
}
