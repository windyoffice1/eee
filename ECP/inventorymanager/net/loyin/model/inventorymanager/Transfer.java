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
 * @Description: 移位数据列表</p>* 
 * @date 2017-10-19
 */
@TableBind(name="inventory_transfer")
public class Transfer extends Model<Transfer>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_transfer";
	public static Transfer dao = new Transfer();
	/**
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param filter
	 * @return
SELECT it.*,bwl1.warehouse_name AS from_warehouse_name,
bwl2.warehouse_name AS target_warehouse_name,
p.realname AS operate_user_name FROM
(SELECT * FROM inventory_transfer WHERE 1=1
) it LEFT JOIN basic_warehouse_location bwl1 ON it.from_warehouse_id=bwl1.id
LEFT JOIN basic_warehouse_location bwl2 ON it.target_warehouse_id=bwl2.id
LEFT JOIN sso_person p ON it.operate_user_id=p.id
	 */
	public Page<Transfer> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append("(SELECT * FROM inventory_transfer WHERE 1=1");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" and company_id= ? ");
			parame.add(company_id);
		}
		String target_warehouse_id = (String) filter.get("target_warehouse_id");
		if(StringUtils.isNotEmpty(target_warehouse_id)){
			sql.append(" and target_warehouse_id= ? ");
			parame.add(target_warehouse_id);
		}
		String from_warehouse_id = (String) filter.get("from_warehouse_id");
		if(StringUtils.isNotEmpty(from_warehouse_id)){
			sql.append(" and from_warehouse_id= ? ");
			parame.add(from_warehouse_id);
		}
		sql.append(" ) it LEFT JOIN basic_warehouse_location bwl1 ON it.from_warehouse_id=bwl1.id ");
		sql.append(" LEFT JOIN basic_warehouse_location bwl2 ON it.target_warehouse_id=bwl2.id ");
		sql.append(" LEFT JOIN sso_person p ON it.operate_user_id=p.id ");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer qryColun=new StringBuffer("SELECT it.*,bwl1.warehouse_name AS from_warehouse_name,")
				.append("bwl2.warehouse_name AS target_warehouse_name,")
				.append("p.realname AS operate_user_name ");
		return dao.paginate(pageNo,pageSize,qryColun.toString(),sql.toString(), parame.toArray());
	}

	/**
SELECT it.*,bwl1.warehouse_name AS from_warehouse_name,bwl2.warehouse_name AS target_warehouse_name,
p.realname AS operate_user_name FROM inventory_transfer it
LEFT JOIN basic_warehouse_location bwl1 ON it.from_warehouse_id=bwl1.id
LEFT JOIN basic_warehouse_location bwl2 ON it.target_warehouse_id=bwl2.id
LEFT JOIN sso_person p ON it.operate_user_id=p.id
WHERE it.company_id=? AND it.id=?
	 */
	public Transfer findById(String id ,String company_id){
		
		StringBuffer sql=new StringBuffer("SELECT it.*,bwl1.warehouse_name AS from_warehouse_name,bwl2.warehouse_name AS target_warehouse_name,");
		sql.append("p.realname AS operate_user_name,p2.realname AS update_user_name FROM inventory_transfer it ");
		sql.append("LEFT JOIN basic_warehouse_location bwl1 ON it.from_warehouse_id=bwl1.id ");
		sql.append("LEFT JOIN basic_warehouse_location bwl2 ON it.target_warehouse_id=bwl2.id ");
		sql.append("LEFT JOIN sso_person p ON it.operate_user_id=p.id ");
		sql.append("LEFT JOIN sso_person p2 ON it.update_user_id=p2.id ");
		sql.append("WHERE it.company_id=? AND it.id=?  ");
		
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
			Db.update("delete  from inventory_transfer where id in ("+ids_.toString()+")",parame.toArray());
			Db.update("delete  from inventory_transfer_data where inventory_transfer_id in ("+ids_.toString()+")",parame.toArray());
		}
	}
	
	
	
}
