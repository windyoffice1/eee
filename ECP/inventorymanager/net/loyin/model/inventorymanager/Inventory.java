package net.loyin.model.inventorymanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import net.loyin.jfinal.anatation.TableBind;

@TableBind(name="inventory")
public class Inventory extends Model<Inventory>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory";
	public static Inventory dao = new Inventory();
	
	/**
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param filter
SELECT ivt.*,bmd.material_no AS material_data_no,bmd.material_name AS material_data_name,
bmd.average_price AS material_average_price,bmd.average_price*ivt.existing_amount AS total_money,
bmd.model_number,bmd.target_price AS purchase_price,bwl.warehouse_name,p.realname AS admin_name,
bmb.material_name AS material_broad_name,,bp.name AS material_type_name
FROM
(SELECT*FROM inventory WHERE 1=1
AND warehouse_id='1'
AND company_id='00123'
) ivt LEFT JOIN basic_material_data bmd ON ivt.material_data_id=bmd.id
LEFT JOIN basic_warehouse_location bwl ON ivt.warehouse_id=bwl.id
LEFT JOIN sso_person p ON bwl.admin_id=p.id
LEFT JOIN  basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id
LEFT JOIN  basic_parame  bp ON bmb.type=bp.id  WHERE 1=1 
AND bmd.model_number LIKE ? 
AND bmd.material_name LIKE ?
AND bmb.id=?
AND bp.id=?
	 */
	public Page<Inventory> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append("(SELECT*FROM inventory WHERE 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" and company_id= ? ");
			parame.add(company_id);
		}
		//仓位
		String warehouse_id = (String) filter.get("warehouse_id");
		if(StringUtils.isNotEmpty(warehouse_id)){
			sql.append(" and warehouse_id = ? ");
			parame.add(warehouse_id);
		}
		sql.append(") ivt LEFT JOIN basic_material_data bmd ON ivt.material_data_id=bmd.id ");
		sql.append("LEFT JOIN basic_warehouse_location bwl ON ivt.warehouse_id=bwl.id ");
		sql.append("LEFT JOIN sso_person p ON bmd.keeper=p.id ");
		sql.append("LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id ");
		sql.append("LEFT JOIN basic_parame bp ON bmb.type=bp.id  WHERE 1=1  ");
		//物料类型（如修理类）
		String material_type_id = (String) filter.get("material_type_id");
		if(StringUtils.isNotEmpty(material_type_id)){
			sql.append(" AND bp.id=? ");
			parame.add(material_type_id);
		}
		//物料大类
		String material_broad_id = (String) filter.get("material_broad_id");
		if(StringUtils.isNotEmpty(material_broad_id)){
			sql.append(" AND bmb.id=?  ");
			parame.add(material_broad_id);
		}
		//型号
		String model_number = (String) filter.get("model_number");
		if(StringUtils.isNotEmpty(model_number)){
			sql.append(" AND bmd.model_number LIKE ?   ");
			parame.add("%"+model_number+"%");
		}
		//物料名称
		String material_data_name = (String) filter.get("material_data_name");
		if(StringUtils.isNotEmpty(material_data_name)){
			sql.append(" AND bmd.material_name LIKE ?  ");
			parame.add("%"+material_data_name+"%");
		}
		//物料编号
		String material_data_no= (String) filter.get("material_data_no");
		if(StringUtils.isNotEmpty(material_data_no)){
			sql.append(" AND bmd.material_no LIKE ?  ");
			parame.add("%"+material_data_no+"%");
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer qryColun=new StringBuffer("SELECT ivt.*,bmd.material_no AS material_data_no,bmd.material_name AS material_data_name,")
				.append("bmd.average_price AS material_average_price,bmd.average_price*ivt.existing_amount AS total_money,")
				.append("bmd.model_number,bmd.target_price AS purchase_price,bwl.warehouse_name,p.realname AS admin_name,")
				.append("bmb.material_name AS material_broad_name,bp.name AS material_type_name ");
		return dao.paginate(pageNo,pageSize,qryColun.toString(),sql.toString(), parame.toArray());
		
	}
	
	/**
SELECT  ivt.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,
bmd.model_number,bmd.unit,bmd.target_price FROM inventory ivt
LEFT JOIN basic_material_data bmd ON ivt.material_data_id=bmd.id
WHERE ivt.warehouse_id=? AND bmd.material_no LIKE ?
	 */
	/**
	 * 查询特定仓库的物料信息
	 */
	public List<Inventory> getWarehouseMaterial(String warehouseid,String material_no){
		List<Inventory> list = new ArrayList<Inventory>();
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT  ivt.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,");
		sql.append("bmd.model_number,bmd.unit,bmd.target_price FROM inventory ivt ");
		sql.append("LEFT JOIN basic_material_data bmd ON ivt.material_data_id=bmd.id ");
		sql.append("WHERE ivt.warehouse_id=? ");
		if(StringUtils.isNoneEmpty(material_no)){
			sql.append("AND bmd.material_no LIKE ? ");
			list=dao.find(sql.toString(),warehouseid,"%"+material_no+"%");
		}else{
			list=dao.find(sql.toString(),warehouseid);
		}
		return list;
	}
	
	
	/**
	 * 物料移位：移入仓位
	 */
	public void putInWarehouse(String warehouse_id,String material_data_id,String amount,String company_id){
		//判断此仓位是否有次物料
		String psql="SELECT * FROM inventory WHERE warehouse_id=? AND material_data_id=?";
		List<Inventory> iList = dao.find(psql, warehouse_id,material_data_id);
		//有此物料
		if(iList!=null&&iList.size()>0){
			String ysql="UPDATE inventory SET existing_amount=existing_amount+? WHERE warehouse_id=? AND material_data_id=?";
			Db.update(ysql, amount,warehouse_id,material_data_id);
		}else{
			//没有此物料
			String insql="INSERT INTO inventory(id,warehouse_id,material_data_id,existing_amount,company_id) VALUES(?,?,?,?,?);";
			Db.update(insql, UUID.randomUUID().toString(),warehouse_id,material_data_id,amount,company_id);
		}
	}
	
	/**
	 * 物料移位：移出仓位
	 */
	public void outWarehouse(String warehouse_id,String material_data_id,String amount,String company_id){
		//判断此仓位是否有次物料
		String psql="SELECT * FROM inventory WHERE warehouse_id=? AND material_data_id=?";
		List<Inventory> iList = dao.find(psql, warehouse_id,material_data_id);
		//有此物料
		if(iList!=null&&iList.size()>0){
			String ysql="UPDATE inventory SET existing_amount=existing_amount-? WHERE warehouse_id=? AND material_data_id=?";
			Db.update(ysql, amount,warehouse_id,material_data_id);
		}
	}
}
