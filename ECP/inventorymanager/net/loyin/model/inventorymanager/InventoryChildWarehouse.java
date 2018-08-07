package net.loyin.model.inventorymanager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import net.loyin.jfinal.anatation.TableBind;
import net.loyin.model.basicdata.ChildWarehouse;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
@TableBind(name="inventory_child_warehouse")
public class InventoryChildWarehouse extends Model<InventoryChildWarehouse> {
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_child_warehouse";
	public static InventoryChildWarehouse dao = new InventoryChildWarehouse();
	
	
	/**
	 * 物料移位：移入子仓库
	 */
	public void putInChildWarehouse(String child_warehouse_id,String material_data_id,String amount,String company_id){
		//判断此仓位是否有次物料
		String psql="SELECT * FROM inventory_child_warehouse WHERE child_warehouse_id=? AND material_data_id=?";
		List<InventoryChildWarehouse> iList = dao.find(psql, child_warehouse_id,material_data_id);
		//有此物料
		if(iList!=null&&iList.size()>0){
			String ysql="UPDATE inventory_child_warehouse SET existing_amount=existing_amount+? WHERE child_warehouse_id=? AND material_data_id=?";
			Db.update(ysql, amount,child_warehouse_id,material_data_id);
		}else{
			//没有此物料
			String insql="INSERT INTO inventory_child_warehouse(id,child_warehouse_id,material_data_id,existing_amount,company_id) VALUES(?,?,?,?,?);";
			Db.update(insql, UUID.randomUUID().toString(),child_warehouse_id,material_data_id,amount,company_id);
		}
	}
	
	/**
	 * 子公司使用物料
	 */
	public void ChildWarehouseUsedMaterial(String child_warehouse_id,String material_data_id,String amount){
		String ysql="UPDATE inventory_child_warehouse SET existing_amount=existing_amount-? WHERE child_warehouse_id=? AND material_data_id=?";
		Db.update(ysql, amount,child_warehouse_id,material_data_id);
	}
	
	/**
SELECT icw.*,bcw.child_warehouse_name,bmd.material_name AS material_data_name,
bmd.material_no AS material_data_no,bmd.model_number,bmd.average_price,bmd.target_price,
p.realname AS manage_user_name,bmb.material_name AS material_broad_name,bp.name AS
material_type_name,bmd.average_price*icw.existing_amount AS total_money,bmd.unit 
FROM inventory_child_warehouse icw 
LEFT JOIN basic_child_warehouse bcw ON  icw.child_warehouse_id=bcw.id
LEFT JOIN basic_material_data bmd ON icw.material_data_id=bmd.id
LEFT JOIN sso_person p ON p.id=bcw.manage_user_id 
LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id
LEFT JOIN basic_parame bp ON bmb.type=bp.id
WHERE 1=1 
AND icw.company_id='0001'
AND bp.id=?
AND bmb.id=?
AND bmd.model_number LIKE ?
AND bmd.material_name LIKE ?
AND icw.child_warehouse_id=?
	 */
	public Page<InventoryChildWarehouse> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" FROM inventory_child_warehouse icw  ");
		sql.append("LEFT JOIN basic_child_warehouse bcw ON  icw.child_warehouse_id=bcw.id ");
		sql.append("LEFT JOIN basic_material_data bmd ON icw.material_data_id=bmd.id ");
		sql.append("LEFT JOIN sso_person p ON p.id=bcw.manage_user_id  ");
		sql.append("LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id ");
		sql.append("LEFT JOIN basic_parame bp ON bmb.type=bp.id ");
		sql.append("WHERE 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append("AND icw.company_id=? ");
			parame.add(company_id);
		}
		//物料类型（如修理类）
		String material_type_id = (String) filter.get("material_type_id");
		if(StringUtils.isNotEmpty(material_type_id)){
			sql.append(" AND bp.id=? ");
			parame.add(material_type_id);
		}
		//物料大类
		String material_broad_id = (String) filter.get("material_broad_id");
		if(StringUtils.isNotEmpty(material_broad_id)){
			sql.append(" AND bmb.id=? ");
			parame.add(material_broad_id);
		}
		//物料型号
		String model_number = (String) filter.get("model_number");
		if(StringUtils.isNotEmpty(model_number)){
			sql.append(" AND bmd.model_number LIKE ? ");
			parame.add("%"+model_number+"%");
		}
		//物料名称
		String material_data_name = (String) filter.get("material_data_name");
		if(StringUtils.isNotEmpty(material_data_name)){
			sql.append(" AND (bmd.material_name LIKE ? or bmd.material_no LIKE ? ) ");
			parame.add("%"+material_data_name+"%");
			parame.add("%"+material_data_name+"%");
		}
		//物料名称
		String material_data_no = (String) filter.get("material_data_no");
		if(StringUtils.isNotEmpty(material_data_no)){
			sql.append(" AND bmd.material_no LIKE ? ");
			parame.add("%"+material_data_no+"%");
		}
		//船/站选择
		String child_warehouse_id = (String) filter.get("child_warehouse_id");
		if(StringUtils.isNotEmpty(child_warehouse_id)){
			sql.append(" AND icw.child_warehouse_id=? ");
			parame.add(child_warehouse_id);
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer qryColun=new StringBuffer("SELECT icw.*,bcw.child_warehouse_name,bmd.material_name AS material_data_name,")
				.append("bmd.material_no AS material_data_no,bmd.model_number,bmd.average_price,bmd.target_price,")
				.append("p.realname AS manage_user_name,bmb.material_name AS material_broad_name,bp.name AS material_type_name,bmd.average_price*icw.existing_amount AS total_money,bmd.unit ");
		return dao.paginate(pageNo,pageSize,qryColun.toString(),sql.toString(), parame.toArray());
	}
	
	
	/**
	 * 物料退料移入退料仓库
	 */
	public void putInRetrurnWarehouse(String return_warehouse_id,String material_data_id,String amount,String company_id){
		//判断此仓位是否有次物料
		String psql="SELECT * FROM inventory_child_warehouse WHERE child_warehouse_id=? AND material_data_id=?";
		List<InventoryChildWarehouse> iList = dao.find(psql, return_warehouse_id,material_data_id);
		//有此物料
		if(iList!=null&&iList.size()>0){
			String ysql="UPDATE inventory_child_warehouse SET existing_amount=existing_amount+? WHERE child_warehouse_id=? AND material_data_id=?";
			Db.update(ysql, amount,return_warehouse_id,material_data_id);
		}else{
			//没有此物料
			String insql="INSERT INTO inventory_child_warehouse(id,child_warehouse_id,material_data_id,existing_amount,company_id) VALUES(?,?,?,?,?);";
			Db.update(insql, UUID.randomUUID().toString(),return_warehouse_id,material_data_id,amount,company_id);
		}
	}
	
	
	/**
	 * 退料仓库中的物料先出库
	 */
	@SuppressWarnings("static-access")
	public List<Map<String,Object>> outPutStorageFirst(List<Map<String,Object>> productlist){
		List<Map<String,Object>> productlist2=new ArrayList<Map<String,Object>>();
		if(productlist!=null&&productlist.size()>0){
			String sqlup="UPDATE inventory_child_warehouse SET existing_amount=existing_amount-? WHERE child_warehouse_id=? AND material_data_id=?";
			String sql="SELECT * FROM inventory_child_warehouse WHERE child_warehouse_id=? AND material_data_id=? ";
			//查询退料仓库的id
			String returnWarehouseId=ChildWarehouse.dao.getReturnWarehouseid();
			for(int i=0;i<productlist.size();i++){
				int returnWarehouseHashMaterial=0;
				String material_data_id=(String) productlist.get(i).get("material_data_id");
				BigDecimal amount=new BigDecimal((String)productlist.get(i).get("amount"));
				List<InventoryChildWarehouse> list=this.dao.find(sql,returnWarehouseId, material_data_id);
				if(list!=null&&list.size()>0&&list.get(0).getBigDecimal("existing_amount").compareTo(BigDecimal.ZERO)>0){
					if(list.get(0).getBigDecimal("existing_amount").compareTo(amount)>=0){
						productlist.get(i).put("outputstorage_warehouse_id", returnWarehouseId);
						Db.update(sqlup, amount,returnWarehouseId,material_data_id);
					}else{
						//退料仓库中有此物料，但是数量小于要出库的数量
						productlist.get(i).put("amount",amount.subtract(list.get(0).getBigDecimal("existing_amount")));
						productlist2.add(productlist.get(i));
						returnWarehouseHashMaterial=1;
						Db.update(sqlup, list.get(0).getBigDecimal("existing_amount"),returnWarehouseId,material_data_id);
						Map<String,Object> map=new HashMap<String,Object>();
						//将退料仓库出库的记录插入出库表中
						map.put("id", UUID.randomUUID().toString());
						map.put("amount", list.get(0).getBigDecimal("existing_amount"));
						//仓位为退料仓库
						map.put("outputstorage_warehouse_id", returnWarehouseId);
						map.put("outputstorage_comment", "退料仓库优先出库");
						map.put("un_outputstorage_amount", productlist.get(i).get("un_outputstorage_amount"));
						map.put("purchase_price", productlist.get(i).get("purchase_price"));
						map.put("material_data_id", productlist.get(i).get("material_data_id"));
						map.put("getmaterial_apply_id", productlist.get(i).get("getmaterial_apply_id"));
						map.put("outputstorage_date", productlist.get(i).get("outputstorage_date"));
						map.put("total_money", productlist.get(i).get("total_money"));
						productlist2.add(map);
					}
				}
				if(returnWarehouseHashMaterial==0){
					productlist2.add(productlist.get(i));
				}
			}
		}
		return productlist2;
	}
	
	public static void main(String[] args) {
		List<String> list0 = new ArrayList<String>();
		List<String> list1= new ArrayList<String>();
		list0.add("0");
		list0.add("1");
		list0.add("2");
		String str0=list0.get(0);
		str0="00";
		list1.add(list0.get(0));
		System.err.println(list1.get(0));
	}
	
	
	
	
}
