package net.loyin.model.reportforms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import net.loyin.jfinal.anatation.TableBind;

import com.jfinal.plugin.activerecord.Model;
@TableBind(name="scm_purchase_apply")
public class PurchaseReportForms extends Model<PurchaseReportForms> {
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="scm_purchase_apply";
	public static PurchaseReportForms dao = new PurchaseReportForms();
	
	/**采购明细表
SELECT spa.purchase_no,LEFT (spa.create_time,10) AS create_date,bmd.material_no AS material_data_no,bp.name AS material_type_name,
bmb.material_name AS belong_to_material_name ,bmd.model_number,bmd.material_name AS material_data_name,spad.amount,bmd.unit,
bmd.target_price,(CASE WHEN spa.approve_status='2' THEN '批准' ELSE '已入库' END) AS status,spa.customer_name
FROM scm_purchase_apply spa
LEFT JOIN scm_purchase_apply_data spad ON spa.id=spad.purchase_apply_id
LEFT JOIN basic_material_data bmd ON spad.material_data_id=bmd.id
LEFT JOIN basic_material_broad bmb ON bmb.id=bmd.belong_to_broad_id
LEFT JOIN basic_parame bp ON bmb.type=bp.id 
WHERE 1=1
AND spa.create_time>='2017-11-20'
AND spa.create_time<='2017-11-25' 
AND spa.customer_name=''
AND bp.id=''
AND bmb.id=''
AND bmd.material_no LIKE ''
AND bmd.material_name LIKE '' 
	 */
	public List<PurchaseReportForms> getPurchaseDetials(Map<String,Object> paramsMap){
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer("SELECT spa.purchase_no,LEFT (spa.create_time,10) AS create_date,bmd.material_no AS material_data_no,bp.name AS material_type_name, ");
		sql.append("bmb.material_name AS belong_to_material_name ,bmd.model_number,bmd.material_name AS material_data_name,spad.amount,bmd.unit,");
		sql.append("bmd.target_price,(CASE WHEN spa.approve_status='2' THEN '批准' ELSE '已入库' END) AS status,spa.customer_name  ");
		sql.append("FROM scm_purchase_apply spa ");
		sql.append("LEFT JOIN scm_purchase_apply_data spad ON spa.id=spad.purchase_apply_id  ");
		sql.append("LEFT JOIN basic_material_data bmd ON spad.material_data_id=bmd.id ");
		sql.append("LEFT JOIN basic_material_broad bmb ON bmb.id=bmd.belong_to_broad_id ");
		sql.append("LEFT JOIN basic_parame bp ON bmb.type=bp.id WHERE 1=1 ");
		String is_all=(String) paramsMap.get("is_all");
		if(StringUtils.isNotBlank(is_all)&&is_all.equals("0")){
			//供应商
			if(StringUtils.isNotBlank((String) paramsMap.get("customer_name"))){
				sql.append("AND spa.customer_name like ?  ");
				parame.add("%"+paramsMap.get("customer_name")+"%");
			}
			//物料类型
			if(StringUtils.isNotBlank((String) paramsMap.get("material_type_id"))){
				sql.append("AND bp.id=? ");
				parame.add(paramsMap.get("material_type_id"));
			}
			//物料大类
			if(StringUtils.isNotBlank((String) paramsMap.get("material_broad_id"))){
				sql.append("AND bmb.id=? ");
				parame.add(paramsMap.get("material_broad_id"));
			}
			//物料编号
			if(StringUtils.isNotBlank((String) paramsMap.get("material_data_no"))){
				sql.append("AND bmd.material_no LIKE ? ");
				parame.add("%"+paramsMap.get("material_data_no")+"%");
			}
			//物料名称
			if(StringUtils.isNotBlank((String) paramsMap.get("material_data_name"))){
				sql.append("AND bmd.material_name LIKE ? ");
				parame.add("%"+paramsMap.get("material_data_name")+"%");
			}
			//开始时间
			if(StringUtils.isNotBlank((String) paramsMap.get("start_date"))){
				sql.append("AND spa.create_time>=?  ");
				parame.add(paramsMap.get("start_date"));
			}
			//结束时间
			if(StringUtils.isNotBlank((String) paramsMap.get("end_date"))){
				String end_date=getSpecifiedDayBefore((String) paramsMap.get("end_date"));
				sql.append("AND spa.create_time<= ?  ");
				parame.add(end_date);
			}
		}
		//值查询出已入库的和批准的
		sql.append("AND (spa.approve_status='2' or spa.approve_status='5')  ");
		
		sql.append(" ORDER BY create_time DESC  ");
		
		return dao.find(sql.toString(), parame.toArray());
	}
	
	
	/**采购汇总（按物料）
SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_material_name,bmd.model_number,bmd.material_name AS material_data_name,
SUM(spad.amount) AS amount,bmd.unit,bmd.target_price,(CASE WHEN spa.approve_status='2' THEN '批准' ELSE '已入库' END) AS status
FROM scm_purchase_apply spa
LEFT JOIN scm_purchase_apply_data spad ON spa.id=spad.purchase_apply_id
LEFT JOIN basic_material_data bmd ON spad.material_data_id=bmd.id
LEFT JOIN basic_material_broad bmb ON bmb.id=bmd.belong_to_broad_id
LEFT JOIN basic_parame bp ON bmb.type=bp.id 
WHERE 1=1 
AND spa.create_time>='2017-11-20'
AND spa.create_time<='2017-11-25' 
AND spa.customer_name=''
AND bp.id=''
AND bmb.id=''
AND bmd.material_no LIKE ''
AND bmd.material_name LIKE '' 
GROUP BY  bmd.material_no,bmb.material_name,bmd.model_number,bmd.material_name,bmd.unit,bmd.target_price
	 */
	public List<PurchaseReportForms> purchaseReportFormsByMaterial(Map<String,Object> paramsMap){
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer("SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_material_name,bmd.model_number,bmd.material_name AS material_data_name,");
		sql.append("SUM(spad.amount) AS amount,bmd.unit,bmd.target_price,(CASE WHEN spa.approve_status='2' THEN '批准' ELSE '已入库' END) AS status ");
		sql.append("FROM scm_purchase_apply spa ");
		sql.append("LEFT JOIN scm_purchase_apply_data spad ON spa.id=spad.purchase_apply_id  ");
		sql.append("LEFT JOIN basic_material_data bmd ON spad.material_data_id=bmd.id ");
		sql.append("LEFT JOIN basic_material_broad bmb ON bmb.id=bmd.belong_to_broad_id ");
		sql.append("LEFT JOIN basic_parame bp ON bmb.type=bp.id WHERE 1=1 ");
		String is_all=(String) paramsMap.get("is_all");
		if(StringUtils.isNotBlank(is_all)&&is_all.equals("0")){
			//供应商
			if(StringUtils.isNotBlank((String) paramsMap.get("customer_name"))){
				sql.append("AND spa.customer_name=? ");
				parame.add(paramsMap.get("customer_name"));
			}
			//物料类型
			if(StringUtils.isNotBlank((String) paramsMap.get("material_type_id"))){
				sql.append("AND bp.id=? ");
				parame.add(paramsMap.get("material_type_id"));
			}
			//物料大类
			if(StringUtils.isNotBlank((String) paramsMap.get("material_broad_id"))){
				sql.append("AND bmb.id=? ");
				parame.add(paramsMap.get("material_broad_id"));
			}
			//物料编号
			if(StringUtils.isNotBlank((String) paramsMap.get("material_data_no"))){
				sql.append("AND bmd.material_no LIKE ? ");
				parame.add("%"+paramsMap.get("material_data_no")+"%");
			}
			//物料名称
			if(StringUtils.isNotBlank((String) paramsMap.get("material_data_name"))){
				sql.append("AND bmd.material_name LIKE ? ");
				parame.add("%"+paramsMap.get("material_data_name")+"%");
			}
			//开始时间
			if(StringUtils.isNotBlank((String) paramsMap.get("start_date"))){
				sql.append("AND spa.create_time>=?  ");
				parame.add(paramsMap.get("start_date"));
			}
			//结束时间
			if(StringUtils.isNotBlank((String) paramsMap.get("end_date"))){
				String end_date=getSpecifiedDayBefore((String) paramsMap.get("end_date"));
				sql.append("AND spa.create_time<= ?  ");
				parame.add(end_date);
			}
		}
		//值查询出已入库的和批准的
		sql.append("AND (spa.approve_status='2' or spa.approve_status='5')  ");
		sql.append(" GROUP BY  bmd.material_no,bmb.material_name,bmd.model_number,bmd.material_name,bmd.unit,bmd.target_price,spa.approve_status  ");
		
		return dao.find(sql.toString(), parame.toArray());
	}
	
	
	/**采购汇总（按供应商）
SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_material_name,bmd.model_number,bmd.material_name AS material_data_name,
SUM(spad.amount) AS amount,bmd.unit,bmd.target_price,spa.customer_name 
FROM scm_purchase_apply spa
LEFT JOIN scm_purchase_apply_data spad ON spa.id=spad.purchase_apply_id
LEFT JOIN basic_material_data bmd ON spad.material_data_id=bmd.id
LEFT JOIN basic_material_broad bmb ON bmb.id=bmd.belong_to_broad_id
LEFT JOIN basic_parame bp ON bmb.type=bp.id 
WHERE 1=1 
AND spa.create_time>='2017-11-20'
AND spa.create_time<='2017-11-25' 
AND spa.customer_name=''
AND bp.id=''
AND bmb.id=''
AND bmd.material_no LIKE ''
AND bmd.material_name LIKE ''
GROUP BY  bmd.material_no,bmb.material_name,bmd.model_number,bmd.material_name,bmd.unit,bmd.target_price,spa.customer_name 
	 */
	public List<PurchaseReportForms> purchaseReportFormsByCustomer(Map<String,Object> paramsMap){
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer("SELECT bmd.material_no AS material_data_no,bmb.material_name AS belong_to_material_name,bmd.model_number,bmd.material_name AS material_data_name,");
		sql.append("SUM(spad.amount) AS amount,bmd.unit,bmd.target_price,spa.customer_name,(CASE WHEN spa.approve_status='2' THEN '批准' ELSE '已入库' END) AS status  ");
		sql.append("FROM scm_purchase_apply spa ");
		sql.append("LEFT JOIN scm_purchase_apply_data spad ON spa.id=spad.purchase_apply_id  ");
		sql.append("LEFT JOIN basic_material_data bmd ON spad.material_data_id=bmd.id ");
		sql.append("LEFT JOIN basic_material_broad bmb ON bmb.id=bmd.belong_to_broad_id ");
		sql.append("LEFT JOIN basic_parame bp ON bmb.type=bp.id WHERE 1=1 ");
		String is_all=(String) paramsMap.get("is_all");
		if(StringUtils.isNotBlank(is_all)&&is_all.equals("0")){
			//供应商
			if(StringUtils.isNotBlank((String) paramsMap.get("customer_name"))){
				sql.append("AND spa.customer_name=? ");
				parame.add(paramsMap.get("customer_name"));
			}
			//物料类型
			if(StringUtils.isNotBlank((String) paramsMap.get("material_type_id"))){
				sql.append("AND bp.id=? ");
				parame.add(paramsMap.get("material_type_id"));
			}
			//物料大类
			if(StringUtils.isNotBlank((String) paramsMap.get("material_broad_id"))){
				sql.append("AND bmb.id=? ");
				parame.add(paramsMap.get("material_broad_id"));
			}
			//物料编号
			if(StringUtils.isNotBlank((String) paramsMap.get("material_data_no"))){
				sql.append("AND bmd.material_no LIKE ? ");
				parame.add("%"+paramsMap.get("material_data_no")+"%");
			}
			//物料名称
			if(StringUtils.isNotBlank((String) paramsMap.get("material_data_name"))){
				sql.append("AND bmd.material_name LIKE ? ");
				parame.add("%"+paramsMap.get("material_data_name")+"%");
			}
			//开始时间
			if(StringUtils.isNotBlank((String) paramsMap.get("start_date"))){
				sql.append("AND spa.create_time>=?  ");
				parame.add(paramsMap.get("start_date"));
			}
			//结束时间
			if(StringUtils.isNotBlank((String) paramsMap.get("end_date"))){
				String end_date=getSpecifiedDayBefore((String) paramsMap.get("end_date"));
				sql.append("AND spa.create_time<= ?  ");
				parame.add(end_date);
			}
		}
		//值查询出已入库的和批准的
		sql.append("AND (spa.approve_status='2' or spa.approve_status='5')  ");
		sql.append(" GROUP BY  bmd.material_no,bmb.material_name,bmd.model_number,bmd.material_name,bmd.unit,bmd.target_price,spa.customer_name,spa.approve_status  ");
		
		return dao.find(sql.toString(), parame.toArray());
	}
	
	
	/** 
	* 获得指定日期的后一天 
	* @param specifiedDay 
	* @return 
	* @throws Exception 
	*/ 
	public static String getSpecifiedDayBefore(String specifiedDay){ 
		//SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); 
		Calendar c = Calendar.getInstance(); 
		Date date=null;  
		try { 
			date = new SimpleDateFormat("yy-MM-dd").parse(specifiedDay); 
			} catch (ParseException e) { 
				e.printStackTrace(); 
			} 
		c.setTime(date); 
		int day=c.get(Calendar.DATE); 
		c.set(Calendar.DATE,day+1); 
		
		String dayBefore=new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()); 
		return dayBefore; 
	}
	
}
