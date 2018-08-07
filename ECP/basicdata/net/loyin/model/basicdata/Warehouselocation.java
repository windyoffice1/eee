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
 * @Description:仓位 </p>* 
 * @date 2017-9-11
 */
@TableBind(name="basic_warehouse_location")
public class Warehouselocation  extends Model<Warehouselocation> {

	private static final long serialVersionUID = 1246482917895101250L;
	public static final String tableName="basic_warehouse_location";
	public static Warehouselocation dao=new Warehouselocation();
	/**
	 * 分页查询
	 * @param pageNo
	 * @param pageSize
	 * @param filter
	 * @return
	 */
	/**		SELECT bwl.*,p.realname AS admin_name FROM
	(SELECT * FROM basic_warehouse_location WHERE 1=1 
	AND warehouse_name='仓位1'
	) bwl LEFT JOIN sso_person p ON bwl.admin_id=p.id
	*/
	public Page<Warehouselocation> pageGrid(Integer pageNo,Integer pageSize,Map<String,Object>filter){
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append("(SELECT * FROM basic_warehouse_location WHERE 1=1 ");
		String warehouse_name=(String)filter.get("warehouse_name");
		if(StringUtils.isNotEmpty(warehouse_name)){
			sql.append(" AND warehouse_name= LIKE ? ");
			warehouse_name="%"+warehouse_name+"%";
			parame.add(warehouse_name);
		}
		sql.append(") bwl LEFT JOIN sso_person p ON bwl.admin_id=p.id ");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"SELECT bwl.*,p.realname AS admin_name ", sql.toString(), parame.toArray());
	}

	
	public Warehouselocation findById(String id){
		String sql = "SELECT bwl.*,p.realname AS admin_name FROM(SELECT * FROM basic_warehouse_location WHERE id=? ) bwl LEFT JOIN sso_person p ON bwl.admin_id=p.id";
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
		}
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}