package net.loyin.model.inventorymanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

import net.loyin.jfinal.anatation.TableBind;
/**
 * 
 * @author:lizhangyou
 * @Description:盘点管理</p>* 
 * @date 2017-10-25
 */
@TableBind(name="inventory_check")
public class Check extends Model<Check>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_check";
	public static Check dao = new Check();
	/**
	 * @param pageNo
	 * @param pageSize
	 * @param filter
	 * @return
		SELECT ic.*,p.realname AS operate_user_name,bwl.warehouse_name 
		FROM inventory_check ic 
		LEFT JOIN sso_person p ON ic.operate_user_id=p.id 
		LEFT JOIN basic_warehouse_location bwl ON ic.warehouse_id=bwl.id WHERE 1=1 
		AND ic.company_id=?
		AND ic.warehouse_id=?
		AND ic.check_name=?
		AND ic.status=?
	 */
	public Page<Check> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer("FROM inventory_check ic ");
		sql.append("LEFT JOIN sso_person p ON ic.operate_user_id=p.id ");
		sql.append("LEFT JOIN basic_warehouse_location bwl ON ic.warehouse_id=bwl.id WHERE 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" AND ic.company_id=? ");
			parame.add(company_id);
		}
		String warehouse_id = (String) filter.get("warehouse_id");
		if(StringUtils.isNotEmpty(warehouse_id)){
			sql.append(" AND ic.warehouse_id=? ");
			parame.add(warehouse_id);
		};
		String check_name = (String) filter.get("check_name");
		if(StringUtils.isNotEmpty(check_name)){
			sql.append(" AND ic.check_name like ? ");
			parame.add("%"+check_name+"%");
		}
		String status = (String) filter.get("status");
		if(StringUtils.isNotEmpty(status)){
			sql.append(" AND ic.status=? ");
			parame.add(status);
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer qryColun=new StringBuffer("SELECT ic.*,p.realname AS operate_user_name,bwl.warehouse_name ");
		return dao.paginate(pageNo,pageSize,qryColun.toString(),sql.toString(), parame.toArray());
	}
	/**
	SELECT ic.*,bwl.warehouse_name,p.realname AS operate_user_name FROM
	(SELECT * FROM inventory_check WHERE company_id=? AND id=? ) ic
	LEFT JOIN basic_warehouse_location bwl ON ic.warehouse_id=bwl.id
	LEFT JOIN sso_person p ON p.id=ic.operate_user_id
	 */
	public Check findById(String id ,String company_id){
		StringBuffer sql=new StringBuffer("SELECT ic.*,bwl.warehouse_name,p.realname AS operate_user_name FROM ");
		sql.append("(SELECT * FROM inventory_check WHERE company_id=? AND id=? ) ic ");
		sql.append("LEFT JOIN basic_warehouse_location bwl ON ic.warehouse_id=bwl.id ");
		sql.append("LEFT JOIN sso_person p ON p.id=ic.operate_user_id ");
		return dao.findFirst(sql.toString(),company_id,id);
	}
	/**直接删除*/
	@Before(Tx.class)
	public void del(String id, String company_id) {
		if (StringUtils.isNotEmpty(id)) {
			String[] ids=id.split(",");
			StringBuffer ids_=new StringBuffer();
			List<String> parame=new ArrayList<String>();
			for(String id_:ids){
				ids_.append("?,");
				parame.add(id_);
			}
			ids_.append("'-'");
			Db.update("delete  from inventory_check where id in ("+ids_.toString()+")",parame.toArray());
			Db.update("delete  from inventory_check_data where inventory_check_id in ("+ids_.toString()+")",parame.toArray());
		}
	}
	//盘点
	public void check(String wahouse_id,String material_data_id,String existing_amount){
		String sql="UPDATE inventory SET existing_amount=? WHERE warehouse_id=? AND material_data_id=?";
		Db.update(sql, existing_amount,wahouse_id,material_data_id);
	}
}
