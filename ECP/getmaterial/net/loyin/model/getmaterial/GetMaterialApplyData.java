package net.loyin.model.getmaterial;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import net.loyin.jfinal.anatation.TableBind;

@TableBind(name="getmaterial_apply_data")
public class GetMaterialApplyData extends Model<GetMaterialApplyData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="getmaterial_apply_data";
	public static GetMaterialApplyData dao = new GetMaterialApplyData();
	
	/**
	 */
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		//先删除申请表中的物料数据
		if(list!=null&&list.size()>0){
			String sql="DELETE FROM getmaterial_apply_data WHERE getmaterial_apply_id=? ";
			Db.update(sql, list.get(0).get("getmaterial_apply_id"));
		}
		Object[][]paras=new Object[list.size()][11];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("getmaterial_apply_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("purchase_price")))
				paras[i][3] = new BigDecimal((String) a.get("purchase_price"));
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][4] = (String) a.get("amount");
			if (StringUtils.isNotEmpty((String) a.get("total_money")))
				paras[i][5] = Float.parseFloat((String) a.get("total_money"));
			paras[i][6] = a.get("comment");
			paras[i][7] = a.get("unit");
			//未出库数量
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][8] = Float.parseFloat((String) a.get("amount"));
			//已出库数量
			paras[i][9] = 0;
			//已出库数量
			if(a.get("existing_amount")!=null){
				paras[i][10] = a.get("existing_amount");
			}else{
				paras[i][10] =new BigDecimal(0);
			}
			i++;
		}
		Db.batch("INSERT INTO getmaterial_apply_data(id,getmaterial_apply_id,material_data_id,purchase_price,amount,total_money,comment,unit," +
				"un_outputstorage_amount,outputstorage_amount,existing_amount)VALUES (?,?,?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}

	/**
	 * 通过领料单查询领料单的物料信息
	 */
	public List<GetMaterialApplyData> getDataList(String getMaterialApplyID,String childWareHouseId){
		StringBuffer sql=new StringBuffer();
			sql.append("SELECT gad.*, bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,")
				.append("bmd.model_number,bmd.existing_number,bmd.target_price,inv.existing_amount	AS inv_existing_amount FROM  ")
				.append("(SELECT * FROM getmaterial_apply_data WHERE getmaterial_apply_id=? ) gad   ")
				.append(" LEFT JOIN inventory inv on inv.material_data_id=gad.material_data_id ")
				.append("LEFT JOIN basic_material_data bmd ON gad.material_data_id=bmd.id ") 
				.append("LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory_child_warehouse WHERE child_warehouse_id=? ")
				.append("GROUP BY material_data_id) ivt ON bmd.id=ivt.material_data_id order by bmd.material_no ");
		return dao.find(sql.toString(), getMaterialApplyID,childWareHouseId);
		/**
SELECT gad.*, bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,
bmd.model_number,bmd.existing_number,ivt.existing_amount,bmd.target_price FROM 
(SELECT * FROM getmaterial_apply_data WHERE getmaterial_apply_id=? ) gad 
LEFT JOIN basic_material_data bmd ON gad.material_data_id=bmd.id
LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory_child_warehouse WHERE child_warehouse_id=? 
GROUP BY material_data_id) ivt ON bmd.id=ivt.material_data_id  
		 * 
SELECT gad.*, bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,
bmd.model_number,bmd.existing_number,ivt.existing_amount,bmd.target_price FROM 
(SELECT * FROM getmaterial_apply_data WHERE getmaterial_apply_id=? ) gad 
LEFT JOIN basic_material_data bmd ON gad.material_data_id=bmd.id
LEFT JOIN (SELECT material_data_id ,SUM(existing_amount) AS existing_amount FROM inventory GROUP BY material_data_id) ivt ON bmd.id=ivt.material_data_id  
		 */
	}
	
	//根据出库通知单核对，修改未入库数量
	@Before(Tx.class)
	public void realOutPutstorage(List<Map<String,Object>> list){
		StringBuffer sql=new StringBuffer("UPDATE getmaterial_apply_data SET un_outputstorage_amount=? ")
		.append(" where getmaterial_apply_id=? AND material_data_id=?");
		Object[] paras=new Object[3];
		for (Map<String, Object> a : list) {
			paras[0] = a.get("un_outputstorage_amount");
			paras[1] = a.get("getmaterial_apply_id");
			paras[2] = a.get("material_data_id");
			Db.update(sql.toString(), paras);
		}
	}
	
	//判断入库单中的物料是否已经全部完成入库
	public boolean isAllOutPutStorage(String getmaterial_apply_id){
		String sql="SELECT* FROM getmaterial_apply_data where getmaterial_apply_id=? ";
		List<GetMaterialApplyData> list=dao.find(sql, getmaterial_apply_id);
		boolean isAllOutPutStorage=true;
		if(list!=null&&list.size()>0){
			for(int i=0; i<list.size();i++){
				if(list.get(i).getBigDecimal("un_outputstorage_amount").compareTo(BigDecimal.ZERO)>0){
					isAllOutPutStorage=false;
				}
			}
		}
		return isAllOutPutStorage;
	}
	
	//获取领料单中的物料的最大单价
	public BigDecimal getMaxPrice(String id){
		String sql="SELECT max(purchase_price) as maxPrice FROM getmaterial_apply_data WHERE getmaterial_apply_id=? ";
		return Db.findFirst(sql,id).getBigDecimal("maxPrice");
	}
}
