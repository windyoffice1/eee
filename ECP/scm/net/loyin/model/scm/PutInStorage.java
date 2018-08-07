package net.loyin.model.scm;

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
 * @Description:入库通知单</p>* 
 * @date 2017-9-18
 */
@TableBind(name="scm_putinstorage")
public class PutInStorage  extends Model<PutInStorage>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="scm_putinstorage";
	public static PutInStorage dao = new PutInStorage();
	
	//查询是否已经有入库记录
	public PutInStorage isPuted(String purchaseApplyID){
		String sql="SELECT * FROM scm_putinstorage WHERE since_purchase_apply_id=?";
		PutInStorage putInStorage = dao.findFirst(sql, purchaseApplyID);
		return putInStorage;
	}
	
	public Page<PutInStorage> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" ");
		sql.append(" FROM scm_putinstorage sp ");
		sql.append(" LEFT JOIN sso_person p ON sp.create_user_id=p.id WHERE 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" AND sp.company_id=? ");
			parame.add(company_id);
		}
		String create_user_id = (String) filter.get("create_user_id");
		if(StringUtils.isNotEmpty(create_user_id)){
			sql.append(" AND sp.create_user_id=? ");
			parame.add(create_user_id);
		}
		String is_deleted = (String) filter.get("is_deleted");
		if(StringUtils.isNotEmpty(is_deleted)){
			sql.append(" AND sp.is_deleted=? ");
			parame.add(is_deleted);
		}
		String putinstorage_name = (String) filter.get("putinstorage_name");
		if(StringUtils.isNotEmpty(putinstorage_name)){
			sql.append(" and sp.putinstorage_name LIKE ?  ");
			parame.add("%"+putinstorage_name+"%");
		}
		String is_since_purchase_apply_id = (String) filter.get("is_since_purchase_apply_id");
		if(StringUtils.isNotEmpty(is_since_purchase_apply_id)){
			sql.append(" AND sp.since_purchase_apply_id IS NOT NULL  ");
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"SELECT sp.*,p.realname AS create_user_name ",sql.toString(), parame.toArray());
	}
	
	//查询入库单详情
	public PutInStorage findById(String id, String company_id){
		StringBuffer sql=new StringBuffer("SELECT sp.*,p.realname AS create_user_name FROM scm_putinstorage sp");
		sql.append(" LEFT JOIN sso_person p ON sp.create_user_id=p.id ");
		sql.append(" WHERE sp.company_id=? AND sp.id=? ");
		return dao.findFirst(sql.toString(),company_id,id);
	}
	
	
	/**直接删除*/
	@Before(Tx.class)
	public void del(String id, String companyId) {
		if (StringUtils.isNotEmpty(id)) {
			String[] ids=id.split(",");
			StringBuffer ids_=new StringBuffer();
			List<String> parame=new ArrayList<String>();
			for(String id_:ids){
				ids_.append("?,");
				parame.add(id_);
			}
			ids_.append("'-'");
			Db.update("UPDATE scm_putinstorage SET is_deleted='1' WHERE  id in ("+ids_.toString()+")",parame.toArray());
			//Db.update("delete  from scm_putinstorage_data where putinstorage_id in ("+ids_.toString()+")",parame.toArray());
		}
	}
	
}
