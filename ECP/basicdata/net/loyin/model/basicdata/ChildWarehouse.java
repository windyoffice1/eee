package net.loyin.model.basicdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.loyin.jfinal.anatation.TableBind;

import org.apache.commons.lang3.StringUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
/**
 * 
 * @author:lizhangyou
 * @Description:子仓位：即船站 【type=0】和小仓库即退料仓库【type=1】</p>* 
 * @date 2017-11-8
 */
@TableBind(name="basic_child_warehouse")
public class ChildWarehouse extends Model<ChildWarehouse>{

	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="basic_child_warehouse";
	public static ChildWarehouse dao = new ChildWarehouse();
	
	/**
	 * 分页条件查询
	 * @param pageNo
	 * @param pageSize
	 * @param filter 参数 为:k,v==>字段,值
	 * @param qryType 查询类型
	 * @return
	 */
	public Page<ChildWarehouse> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer();
		sql.append(" FROM basic_child_warehouse bcw LEFT JOIN sso_person p ON bcw.manage_user_id=p.id where 1=1 ");
		String conpany_id=(String) filter.get("company_id");
		if(StringUtils.isNotEmpty(conpany_id)){
			sql.append("  AND bcw.company_id=? ");
			parame.add(conpany_id);
		}
		String child_warehouse_name=(String)filter.get("child_warehouse_name");
		if(StringUtils.isNotEmpty(child_warehouse_name)){
			sql.append(" and (bcw.child_warehouse_name LIKE ?) ");
			child_warehouse_name="%"+child_warehouse_name+"%";
			parame.add(child_warehouse_name);
		}
		String manage_user_id=(String)filter.get("manage_user_id");
		if(StringUtils.isNotEmpty(manage_user_id)){
			sql.append(" AND bcw.manage_user_id=?");
			parame.add("%"+manage_user_id+"%");
		}
		String type=(String)filter.get("type");
		if(StringUtils.isNotEmpty(type)){
			sql.append(" AND bcw.type=?");
			parame.add(type);
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer sqlColun=new StringBuffer("SELECT bcw.*,p.realname AS manage_user_name ");
		return dao.paginate(pageNo,pageSize,sqlColun.toString(),sql.toString(), parame.toArray());
		
		/**
SELECT bcw.*,p.realname AS manage_user_name FROM basic_child_warehouse bcw
LEFT JOIN sso_person p ON bcw.manage_user_id=p.id
WHERE bcw.child_warehouse_name LIKE ? AND bcw.company_id=? AND bcw.manage_user_id=? and bcw.type=?
		 */
	}
	
	
	
	public ChildWarehouse findById(String id){
		String sql = "SELECT bcw.*,p.realname AS manage_user_name FROM basic_child_warehouse bcw "+
				"LEFT JOIN sso_person p ON bcw.manage_user_id=p.id "+
				"WHERE bcw.id=?";
		return dao.findFirst(sql, id);
	}

	
	/**直接删除*/
	@Before(Tx.class)
	public void del(String id) {
		if (StringUtils.isNotEmpty(id)) {
			String[] ids=id.split(",");
			StringBuffer ids_=new StringBuffer();
			List<String> parame=new ArrayList<String>();
			for(String id_:ids){
				ids_.append("?,");
				parame.add(id_);
			}
			ids_.append("'-'");
			Db.update("delete  from " + tableName + " where id in ("+ids_.toString()+")",parame.toArray());
			Db.update("delete  from sso_position  where id in ("+ids_.toString()+")",parame.toArray());
		}
	}
	
	/**
	 * 查询退料仓库的id
	 */
	public String getReturnWarehouseid(){
		String sql = "SELECT * FROM basic_child_warehouse where type=1 ";
		ChildWarehouse childWarehouse=dao.findFirst(sql);
		return childWarehouse!=null ? childWarehouse.getStr("id"):null;
	}
	
	
	/**
	 * 通过船站统计月结统计表
SELECT bcw.child_warehouse_name,bp.name,SUM(god.total_money) AS total_money
FROM basic_parame bp 
LEFT JOIN basic_material_broad bmb ON bp.id=bmb.type
LEFT JOIN basic_material_data bmd ON bmb.id=bmd.belong_to_broad_id
LEFT JOIN getmaterial_outputstorage_data god ON god.material_data_id=bmd.id
LEFT JOIN getmaterial_apply ga ON god.getmaterial_apply_id=ga.id
LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id
WHERE bp.type=21 
AND ga.child_warehouse_id='1'
AND LEFT(god.outputstorage_date,7)='2017-11'
GROUP BY bp.name,bcw.child_warehouse_name 

	 */
	@Deprecated
	public List<ChildWarehouse> monthlyStatementStatisticsByChildWarehouse(String year,String begin_month,String child_warehouse_id){
		List<String> params=new ArrayList<String>();
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT bcw.child_warehouse_name,bp.name,SUM(god.total_money) AS total_money  ");
		sql.append("FROM basic_parame bp LEFT JOIN basic_material_broad bmb ON bp.id=bmb.type LEFT JOIN basic_material_data bmd ON bmb.id=bmd.belong_to_broad_id ");
		sql.append("LEFT JOIN getmaterial_outputstorage_data god ON god.material_data_id=bmd.id LEFT JOIN getmaterial_apply ga ON god.getmaterial_apply_id=ga.id ");
		sql.append("LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id WHERE bp.type=21 ");
		if(StringUtils.isNotBlank(child_warehouse_id)){
			sql.append(" AND ga.child_warehouse_id=?  ");
			params.add(child_warehouse_id);
		}
		if(StringUtils.isNotBlank(year)&&StringUtils.isNotBlank(begin_month)){
			if(begin_month.length()==1){
				begin_month="0"+begin_month;
			}
			sql.append(" AND LEFT(god.outputstorage_date,7)=?  ");
			params.add(year+"-"+begin_month);
		}
		sql.append(" GROUP BY bcw.child_warehouse_name,bp.name ORDER BY child_warehouse_name ");
		return dao.find(sql.toString(), params.toArray());
	}
	
	public List<ChildWarehouse> monthlyStatementStatisticsByChildWarehouse(String year,String begin_month){
		List<String> params=new ArrayList<String>();
		StringBuffer sql=new StringBuffer();
		sql.append(" SELECT t1.child_warehouse_name,t1.name,SUM ( t1.amount* t2.end_price ) AS total_money  ");
		sql.append(" FROM (select ga.child_warehouse_id,bcw.child_warehouse_name,got.amount,t.id as typeId,t.name,t.material_data_id from getmaterial_outputstorage_data got ");
		sql.append(" join getmaterial_apply ga on got.getmaterial_apply_id=ga.id join dbo.basic_child_warehouse bcw on bcw.id=ga.child_warehouse_id ");
		sql.append(" join (select bp.id,bp.name,bmb.material_name,bmd.id as material_data_id from basic_parame bp join basic_material_broad bmb on bmb.type=bp.id join basic_material_data bmd on bmd.belong_to_broad_id=bmb.id ");
		sql.append(" where bp.type=21) t on t.material_data_id=got.material_data_id) t1 join ( select material_data_id,end_price from report_forms_monthly_statement ");
		if(StringUtils.isNotBlank(year)) {
			sql.append(" where year=? ");
			params.add(year);
		}
		if(StringUtils.isNotBlank(begin_month)) {
			if(begin_month.length()==1) {
				begin_month="0"+begin_month;
			}
			sql.append(" and month =? ");
			params.add(begin_month);
		}
		sql.append(" ) t2 on t1.material_data_id=t2.material_data_id group by t1.child_warehouse_name,name  order by t1.child_warehouse_name ");
		return dao.find(sql.toString(), params.toArray());
	}
	
	/**
	 * 通过船站统计年结统计表
	 */
	public List<ChildWarehouse> yearStatementStatisticsByChildWarehouse(String year,String begin_month,String end_month,String child_warehouse_id){
		List<String> params=new ArrayList<String>();
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT bcw.child_warehouse_name,bp.name,SUM(god.total_money) AS total_money  ");
		sql.append("FROM basic_parame bp LEFT JOIN basic_material_broad bmb ON bp.id=bmb.type LEFT JOIN basic_material_data bmd ON bmb.id=bmd.belong_to_broad_id ");
		sql.append("LEFT JOIN getmaterial_outputstorage_data god ON god.material_data_id=bmd.id LEFT JOIN getmaterial_apply ga ON god.getmaterial_apply_id=ga.id ");
		sql.append("LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id WHERE bp.type=21 ");
		if(StringUtils.isNotBlank(child_warehouse_id)){
			sql.append(" AND ga.child_warehouse_id=?  ");
			params.add(child_warehouse_id);
		}
		if(StringUtils.isNotBlank(begin_month)&&StringUtils.isNotBlank(end_month)){
			sql.append(" AND LEFT(god.outputstorage_date,7) in(  ");
			Integer begin_month_int=Integer.parseInt(begin_month);
			Integer end_month_int=Integer.parseInt(end_month);
			if(begin_month_int<=end_month_int){
				for(int i=begin_month_int ; i<=end_month_int ; i++){
					if(i<10){
						sql.append("'"+year+"-"+"0"+i+"',");
					}else{
						sql.append("'"+year+"-"+i+"',");
					}
				}
			}
			sql.append(" '-') ");
		}
		sql.append(" GROUP BY bcw.child_warehouse_name,bp.name ORDER BY child_warehouse_name");
		return dao.find(sql.toString(), params.toArray());
	}
	

}
