package net.loyin.model.basicdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import net.loyin.jfinal.anatation.TableBind;
import net.loyin.model.sso.Person;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

/**
 * 
 * @author:lizhangyou
 * @Description:物料大类</p>* 
 * @date 2017-9-12
 */
@TableBind(name="basic_material_broad")
public class MaterialBroad  extends Model<MaterialBroad> {

	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="basic_material_broad";
	public static MaterialBroad dao = new MaterialBroad();
	
	/**
	 * 分页条件查询
	 * @param pageNo
	 * @param pageSize
	 * @param filter 参数 为:k,v==>字段,值
	 * @param qryType 查询类型
	 * @return
	 */
	public Page<MaterialBroad> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append(tableName);
		sql.append(" mb LEFT JOIN ");
		sql.append(Person.tableName);
		sql.append(" p ON p.id=mb.keeper LEFT JOIN  ");
		sql.append(Person.tableName);
		sql.append(" p2 ON p2.id=mb.modifier where 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" and mb.company_id= ? ");
			parame.add(company_id);
		}
		String material_name = (String) filter.get("material_name");
		if(StringUtils.isNotEmpty(material_name)){
			sql.append(" and mb.material_name like ? ");
			parame.add("%"+ material_name + "%");
		}
		String type = (String) filter.get("type");
		if(StringUtils.isNotEmpty(type)){
			sql.append(" and mb.type= ? ");
			parame.add(type);
		}
		String material_no = (String) filter.get("material_no");
		if(StringUtils.isNotEmpty(material_no)){
			sql.append(" and mb.material_no like ? ");
			parame.add("%"+material_no+ "%");
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"select mb.*,p.realname as keeper_name, p2.realname as modifier_name ",sql.toString(), parame.toArray());
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
	
	public MaterialBroad findByIdAndCompanyid(String company_id,String id ){
		StringBuffer sql = new StringBuffer("select mb.*,p.realname as keeper_name, p2.realname as modifier_name  ") ;
			sql.append("from basic_material_broad mb ");
			sql.append("LEFT JOIN sso_person p ON p.id=mb.keeper ");
			sql.append("LEFT JOIN  sso_person p2 ON p2.id=mb.modifier ");
			sql.append("where mb.id=?  and mb.company_id=?  ");
		return dao.findFirst(sql.toString(),id,company_id);
	}
	
}
