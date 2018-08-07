package net.loyin.model.basicdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import net.loyin.jfinal.anatation.TableBind;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
/**
 * 
 * @author:lizhangyou
 * @Description:物料基础数据 </p>* 
 * @date 2017-9-12
 */
@TableBind(name="basic_material_data")
public class MaterialData  extends Model<MaterialData> {

	private static final long serialVersionUID = -4221825254783835788L;
	public static final String tableName="basic_material_data";
	public static MaterialData dao=new MaterialData();
	/**
	 * 分页条件查询
	 * @param pageNo
	 * @param pageSize
	 * @param filter 参数 为:k,v==>字段,值
	 * @param qryType 查询类型
	 * @return
	 */
	public Page<MaterialData> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer();
		sql.append("FROM basic_material_data bd  ");
		sql.append("LEFT JOIN basic_material_broad bb ON bd.belong_to_broad_id=bb.id  ");
		sql.append("LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory GROUP BY material_data_id) ivt ON bd.id=ivt.material_data_id ");
		sql.append("LEFT JOIN sso_person p ON bd.keeper=p.id  WHERE 1=1  ");
		String conpany_id=(String) filter.get("company_id");
		if(StringUtils.isNotEmpty(conpany_id)){
			sql.append(" and bd.company_id=? ");
			parame.add(conpany_id);
		}
		String material_name=(String)filter.get("material_name");
		if(StringUtils.isNotEmpty(material_name)){
			sql.append(" and (bd.material_name like ? )");
			parame.add("%"+material_name+"%");
		}
		String material_no=(String)filter.get("material_no");
		if(StringUtils.isNotEmpty(material_no)){
			sql.append(" and bd.material_no like ? ");
			parame.add("%"+material_no+"%");
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer sqlColun=new StringBuffer("SELECT bd.*,(convert(VARCHAR,cast(round(bd.target_price*(1-bd.float_rate),2)as numeric(38,2)))+'-'+");
					sqlColun.append("convert(VARCHAR,cast(round(bd.target_price*(1+bd.float_rate),2)as numeric(38,2)))) AS float_price, ");
					sqlColun.append("p.realname AS keeper_name,bb.material_name AS belong_to_broad_name,bb.material_no AS belong_to_broad_no,ivt.existing_amount AS existing_amount ");
		return dao.paginate(pageNo,pageSize,sqlColun.toString(),sql.toString(), parame.toArray());
		
		/**
SELECT bd.*,(convert(VARCHAR,cast(round(bd.target_price*(1-bd.float_rate),2)as numeric(38,2)))+'-'+
convert(VARCHAR,cast(round(bd.target_price*(1+bd.float_rate),2)as numeric(38,2)))) AS float_price,
p.realname AS keeper_name,bb.material_name AS belong_to_broad_name,bb.material_no AS belong_to_broad_no,
ivt.existing_amount AS existing_amount
FROM basic_material_data bd 
LEFT JOIN basic_material_broad bb ON bd.belong_to_broad_id=bb.id 
LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory GROUP BY material_data_id) ivt ON bd.id=ivt.material_data_id
LEFT JOIN sso_person p ON bd.keeper=p.id  WHERE 1=1 
		 */
	}
	
	public MaterialData findByIdAndCompanyid(String company_id,String id ){
		StringBuffer sql = new StringBuffer("SELECT bd.*,(convert(VARCHAR,cast(round(bd.target_price*(1-bd.float_rate),2)as numeric(38,2)))+'-'+") ;
		sql.append("convert(VARCHAR,cast(round(bd.target_price*(1+bd.float_rate),2)as numeric(38,2)))) AS float_price, ");
		sql.append("p.realname AS keeper_name,bb.material_name AS belong_to_broad_name,bb.material_no AS belong_to_broad_no,ivt.existing_amount AS existing_amount ");
		sql.append("FROM basic_material_data bd LEFT JOIN basic_material_broad bb ON bd.belong_to_broad_id=bb.id  ");
		sql.append("LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory GROUP BY material_data_id) ivt ON bd.id=ivt.material_data_id  ");
		sql.append("LEFT JOIN sso_person p ON bd.keeper=p.id WHERE bd.id=? AND bd.company_id=? ");
		return dao.findFirst(sql.toString(),id,company_id);
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
	
	/**
	 * 入库，修改物料的现存数量
	public void changeExistingAmount(List<Map<String,Object>> productlist){
		String sql="UPDATE basic_material_data SET existing_number=(existing_number+?) WHERE id=?";
		//修改物料表中的现存数量
		for(Map<String,Object> product:productlist){
			 Db.update(sql,Integer.parseInt((String) product.get("real_putinstarage_amount")),
					 product.get("material_data_id"));
		}
	}
	*/
	
	
	/**
UPDATE basic_material_data SET average_price=((CASE WHEN average_price IS NULL THEN 0 ELSE average_price END)
*((CASE WHEN(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id='7718b94c-b5d0-4246-b5ee-03b248b28ab7' GROUP BY material_data_id) IS NULL THEN 0 ELSE 
(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id='7718b94c-b5d0-4246-b5ee-03b248b28ab7' GROUP BY material_data_id) END) )+600*100)/(
(CASE WHEN(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id='7718b94c-b5d0-4246-b5ee-03b248b28ab7' GROUP BY material_data_id) IS NULL THEN 0 ELSE 
(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id='7718b94c-b5d0-4246-b5ee-03b248b28ab7' GROUP BY material_data_id) END)
+100) 
WHERE id='7718b94c-b5d0-4246-b5ee-03b248b28ab7'
	 */
	//重新计算物料的平均价格
	public void averagePrice(List<Map<String,Object>> productlist){
		StringBuffer sql=new StringBuffer("UPDATE basic_material_data SET average_price=((CASE WHEN average_price IS NULL THEN 0 ELSE average_price END) ")
						.append("*((CASE WHEN(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id=? GROUP BY material_data_id) IS NULL THEN 0 ELSE ")
						.append("(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id=? GROUP BY material_data_id) END) )+?)/( ")
						.append("(CASE WHEN(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id=? GROUP BY material_data_id) IS NULL THEN 0 ELSE  ")
						.append("(SELECT sum(existing_amount) AS existing_amount FROM inventory  WHERE material_data_id=? GROUP BY material_data_id) END) +?) WHERE id=? ");
		for(Map<String,Object> product:productlist){
			Integer total_money=Integer.parseInt((String) product.get("purchase_price"))*Integer.parseInt((String) product.get("real_putinstarage_amount"));
			System.err.println("purchase_price)"+product.get("purchase_price"));
			System.err.println("real_putinstarage_amount)"+product.get("real_putinstarage_amount"));
			System.err.println("total_money)"+total_money);
			 Db.update(sql.toString(),product.get("material_data_id"),product.get("material_data_id"),total_money,product.get("material_data_id"),product.get("material_data_id"),
					 product.get("real_putinstarage_amount"),product.get("material_data_id"));
		}
	}

	
	/**
	 * 出库或报废
	 */
	public void outPutStorage(String material_data_id,String amount){
		String sql="UPDATE basic_material_data SET existing_number=(existing_number-?) WHERE id=?";
		 Db.update(sql,amount,material_data_id);
	}
	
	/**
	 * 分页查询库存预警
	 */
	public Page<MaterialData> pageWaringGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer();
		sql.append("FROM basic_material_data bd  ");
		sql.append("LEFT JOIN basic_material_broad bb ON bd.belong_to_broad_id=bb.id  ");
		sql.append("LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory GROUP BY material_data_id) ivt ON bd.id=ivt.material_data_id ");
		sql.append("LEFT JOIN sso_person p ON bd.keeper=p.id  WHERE 1=1  ");
		String conpany_id=(String) filter.get("company_id");
		if(StringUtils.isNotEmpty(conpany_id)){
			sql.append(" and bd.company_id=? ");
			parame.add(conpany_id);
		}
		String material_name=(String)filter.get("material_name");
		if(StringUtils.isNotEmpty(material_name)){
			sql.append(" and (bd.material_name like ? )");
			material_name="%"+material_name+"%";
			parame.add(material_name);
		}
		String material_no=(String)filter.get("material_no");
		if(StringUtils.isNotEmpty(material_no)){
			sql.append(" and bd.material_no like ? ");
			parame.add("%"+material_no+"%");
		}
		sql.append(" AND bd.inventory_warning_amount>=ivt.existing_amount ");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer sqlColun=new StringBuffer("SELECT bd.*,(convert(VARCHAR,cast(round(bd.target_price*(1-bd.float_rate),2)as numeric(38,2)))+'-'+");
					sqlColun.append("convert(VARCHAR,cast(round(bd.target_price*(1+bd.float_rate),2)as numeric(38,2)))) AS float_price, ");
					sqlColun.append("p.realname AS keeper_name,bb.material_name AS belong_to_broad_name,bb.material_no AS belong_to_broad_no,ivt.existing_amount AS existing_amount ");
		return dao.paginate(pageNo,pageSize,sqlColun.toString(),sql.toString(), parame.toArray());
		
		/**
SELECT bd.*,(convert(VARCHAR,cast(round(bd.target_price*(1-bd.float_rate),2)as numeric(38,2)))+'-'+
convert(VARCHAR,cast(round(bd.target_price*(1+bd.float_rate),2)as numeric(38,2)))) AS float_price,
p.realname AS keeper_name,bb.material_name AS belong_to_broad_name,bb.material_no AS belong_to_broad_no,ivt.existing_amount AS existing_amount
FROM basic_material_data bd 
LEFT JOIN basic_material_broad bb ON bd.belong_to_broad_id=bb.id 
LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory GROUP BY material_data_id) ivt ON bd.id=ivt.material_data_id
LEFT JOIN sso_person p ON bd.keeper=p.id  WHERE 1=1 
AND bd.inventory_warning_amount>=ivt.existing_amount
		 */
	}
	
	
	
}
