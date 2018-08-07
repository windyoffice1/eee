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
 * @Description:报废管理 </p>* 
 * @date 2017-10-25
 */
@TableBind(name="inventory_scrap")
public class Scrap extends Model<Scrap>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_scrap";
	public static Scrap dao = new Scrap();
	
	/**
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param filter
	 * @return
SELECT its.*,p.realname AS operate_user_name,
(case when bwl.warehouse_name IS NULL 
then bcw.child_warehouse_name else bwl.warehouse_name end) AS warehouse_name
FROM inventory_scrap its 
LEFT JOIN basic_warehouse_location bwl ON its.warehouse_id=bwl.id
LEFT JOIN basic_child_warehouse bcw ON its.warehouse_id=bcw.id
LEFT JOIN sso_person p ON its.operate_user_id=p.id
WHERE its.warehouse_id=? AND its.company_id=? AND its.type=?
	 */
	public Page<Scrap> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer("FROM inventory_scrap its  ");
		sql.append("LEFT JOIN basic_warehouse_location bwl ON its.warehouse_id=bwl.id ");
		sql.append("LEFT JOIN basic_child_warehouse bcw ON its.warehouse_id=bcw.id ");
		sql.append("LEFT JOIN sso_person p ON its.operate_user_id=p.id WHERE 1=1 ");
		String warehouse_id = (String) filter.get("warehouse_id");
		if(StringUtils.isNotEmpty(warehouse_id)){
			sql.append(" and its.warehouse_id=? ");
			parame.add(warehouse_id);
		}
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" and its.company_id=?");
			parame.add(company_id);
		};
		String type = (String) filter.get("type");
		if(StringUtils.isNotEmpty(type)){
			sql.append(" and its.type=?");
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
		StringBuffer qryColun=new StringBuffer("SELECT its.*,(case when bwl.warehouse_name IS NULL then bcw.child_warehouse_name else bwl.warehouse_name end) AS warehouse_name,p.realname AS operate_user_name ");

		return dao.paginate(pageNo,pageSize,qryColun.toString(),sql.toString(), parame.toArray());
	}
	
	/**
	 * 
SELECT ivs.*,bwl.warehouse_name,p.realname AS operate_user_name FROM inventory_scrap ivs
LEFT JOIN basic_warehouse_location bwl ON ivs.warehouse_id=bwl.id
LEFT JOIN sso_person p ON ivs.operate_user_id=p.id
WHERE ivs.id=? AND ivs.company_id=?
	 */
	public Scrap findById(String id ,String company_id){
		
		StringBuffer sql=new StringBuffer("SELECT ivs.*,(case when bwl.warehouse_name IS NULL then bcw.child_warehouse_name else bwl.warehouse_name end) AS warehouse_name,");
		sql.append("p.realname AS operate_user_name FROM inventory_scrap ivs LEFT JOIN basic_warehouse_location bwl ON ivs.warehouse_id=bwl.id ");
		sql.append("LEFT JOIN basic_child_warehouse bcw ON ivs.warehouse_id=bcw.id LEFT JOIN sso_person p ON ivs.operate_user_id=p.id ");
		sql.append(" WHERE ivs.id=? AND ivs.company_id=?");
		
		return dao.findFirst(sql.toString(),id,company_id);
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
			Db.update("delete  from inventory_scrap where id in ("+ids_.toString()+")",parame.toArray());
			Db.update("delete  from inventory_scrap_data where inventory_scrap_id in ("+ids_.toString()+")",parame.toArray());
		}
	}
	
	
	
}
