package net.loyin.model.inventorymanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hyperic.sigar.pager.Pager;

import net.loyin.jfinal.anatation.TableBind;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 
 * @author:lizhangyou
 * @Description:船站物料使用情况 </p>* 
 * @date 2017-12-20
 */

@TableBind(name="inventory_child_warehouse_used_history")
public class ChildWarehouseMaterialUsedHistory extends Model<ChildWarehouseMaterialUsedHistory>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_child_warehouse_used_history";
	public static ChildWarehouseMaterialUsedHistory dao = new ChildWarehouseMaterialUsedHistory();
	
	/**
SELECT icwuh.*,p.realname AS create_user_name,bcw.child_warehouse_name,bmd.material_no AS material_data_no,
bmd.material_name AS material_data_name,bmb.modifier,bmd.unit,bmb.material_name AS material_broad_name,
bmb.material_no AS material_broad_no,bp.name FROM inventory_child_warehouse_used_history icwuh
JOIN basic_child_warehouse bcw ON icwuh.child_warehouse_id=bcw.id
JOIN basic_material_data bmd ON icwuh.material_data_id=bmd.id
JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id
JOIN basic_parame bp ON bmb.type=bp.id 
JOIN sso_person p ON icwuh.create_user_id=p.id
WHERE 1=1 AND icwuh.company_id='0001'
AND icwuh.child_warehouse_id='1'
AND (bmd.material_no LIKE '%%' OR bmd.material_name LIKE '%%' )
AND bmb.id=''
AND bp.id=''
	 */
	public Page<ChildWarehouseMaterialUsedHistory> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer("FROM inventory_child_warehouse_used_history icwuh ");
		sql.append("JOIN basic_child_warehouse bcw ON icwuh.child_warehouse_id=bcw.id ");
		sql.append("JOIN basic_material_data bmd ON icwuh.material_data_id=bmd.id ");
		sql.append("JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id  ");
		sql.append("JOIN basic_parame bp ON bmb.type=bp.id ");
		sql.append("JOIN sso_person p ON icwuh.create_user_id=p.id WHERE 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append("  AND icwuh.company_id=? ");
			parame.add(company_id);
		}
		String child_warehouse_id = (String) filter.get("child_warehouse_id");
		if(StringUtils.isNotEmpty(child_warehouse_id)){
			sql.append(" AND icwuh.child_warehouse_id=? ");
			parame.add(child_warehouse_id);
		}
		String material_data_no = (String) filter.get("material_data_no");
		if(StringUtils.isNotEmpty(material_data_no)){
			sql.append(" AND (bmd.material_no LIKE ? OR bmd.material_name LIKE ? ) ");
			parame.add("%"+material_data_no+"%");
			parame.add("%"+material_data_no+"%");
		};
		String material_broad_id = (String) filter.get("material_broad_id");
		if(StringUtils.isNotEmpty(material_broad_id)){
			sql.append(" AND bmb.id=? ");
			parame.add(material_broad_id);
		}
		String material_type = (String) filter.get("material_type");
		if(StringUtils.isNotEmpty(material_type)){
			sql.append(" AND bp.id=? ");
			parame.add(material_type);
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer qryColun=new StringBuffer("SELECT icwuh.*,bcw.child_warehouse_name,bmd.material_no AS material_data_no,p.realname AS create_user_name,")
				.append("bmd.material_name AS material_data_name,bmb.modifier AS model_number,bmd.unit,bmb.material_name AS material_broad_name,")
				.append("bmb.material_no AS material_broad_no,bp.name as material_type_name ");
		return dao.paginate(pageNo,pageSize,qryColun.toString(),sql.toString(), parame.toArray());
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
			Db.update("delete  from inventory_child_warehouse_used_history where id in ("+ids_.toString()+")",parame.toArray());
		}
	}
	

}
