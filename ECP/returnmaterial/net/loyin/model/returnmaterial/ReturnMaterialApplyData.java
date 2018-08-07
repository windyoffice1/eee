package net.loyin.model.returnmaterial;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import net.loyin.jfinal.anatation.TableBind;

@TableBind(name="returnmaterial_apply_data")
public class ReturnMaterialApplyData extends Model<ReturnMaterialApplyData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="returnmaterial_apply_data";
	public static ReturnMaterialApplyData dao = new ReturnMaterialApplyData();
	
	

	/**
	 * 通过退料单查询退料单的物料信息
SELECT gad.*, bmd.material_name AS material_data_name,bmd.unit, 
bmd.material_no AS material_data_no,bmd.model_number,icw.existing_amount,
bmd.target_price FROM 
(SELECT * FROM returnmaterial_apply_data WHERE 
returnmaterial_apply_id='e634f03d-0204-4451-ab95-ec0084fcc13e' ) gad 
LEFT JOIN basic_material_data bmd ON gad.material_data_id=bmd.id
LEFT JOIN (SELECT* FROM inventory_child_warehouse where child_warehouse_id= '1' )icw 
ON gad.material_data_id=icw.material_data_id
	 */
	public List<ReturnMaterialApplyData> getDataList(String returnMaterialApplyID,String child_warehouse_id){
		StringBuffer sql=new StringBuffer();
			sql.append("SELECT gad.*, bmd.material_name AS material_data_name,bmd.unit,bmd.target_price, ")
				.append("bmd.material_no AS material_data_no,bmd.model_number,icw.existing_amount FROM  ")
				.append("(SELECT * FROM returnmaterial_apply_data ")
				.append("WHERE returnmaterial_apply_id=? ) gad  ") 
				.append("LEFT JOIN basic_material_data bmd ON gad.material_data_id=bmd.id ")
				.append("LEFT JOIN (SELECT* FROM inventory_child_warehouse where child_warehouse_id= ? ) icw ON gad.material_data_id=icw.material_data_id ORDER BY material_data_no ");
		return dao.find(sql.toString(), returnMaterialApplyID,child_warehouse_id);
	}
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		//先删除申请表中的物料数据
		if(list!=null&&list.size()>0){
			String sql="DELETE FROM returnmaterial_apply_data WHERE returnmaterial_apply_id=? ";
			Db.update(sql, list.get(0).get("returnmaterial_apply_id"));
		}
		Object[][]paras=new Object[list.size()][10];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("returnmaterial_apply_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("target_price")))
				paras[i][3] = new BigDecimal((String) a.get("target_price"));
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][4] = (String) a.get("amount");
			if (StringUtils.isNotEmpty((String) a.get("total_money")))
				paras[i][5] = Float.parseFloat((String) a.get("total_money"));
			paras[i][6] = a.get("comment");
			//未出库数量
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][7] = Float.parseFloat((String) a.get("amount"));
			//已出库数量
			paras[i][8] = 0;
			//是否合格
			paras[i][9] = a.get("is_qualified");
			i++;
		}
		Db.batch("INSERT INTO returnmaterial_apply_data(id,returnmaterial_apply_id,material_data_id,target_price,amount,total_money,comment," +
				"un_putinstorage_amount,putinstorage_amount,is_qualified)VALUES (?,?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}
	
	//根据子仓位的出库通知单核对，修改未入库数量
	@Before(Tx.class)
	public void realOutPutstorage(List<Map<String,Object>> list){
		StringBuffer sql=new StringBuffer("UPDATE returnmaterial_apply_data SET un_putinstorage_amount=? ")
		.append(" where returnmaterial_apply_id=? AND material_data_id=?");
		Object[] paras=new Object[3];
		for(Map<String, Object> a : list){
			paras[0] = a.get("un_putinstorage_amount");
			paras[1] = a.get("returnmaterial_apply_id");
			paras[2] = a.get("material_data_id");
			Db.update(sql.toString(), paras);
		}
	}
	
	//判断退料单中的物料是否已经全部完成出库
	public boolean isAllOutPutStorage(String returnmaterial_apply_id){
		String sql="SELECT* FROM returnmaterial_apply_data where returnmaterial_apply_id=? ";
		List<ReturnMaterialApplyData> list=dao.find(sql, returnmaterial_apply_id);
		boolean isAllOutPutStorage=true;
		if(list!=null&&list.size()>0){
			for(int i=0; i<list.size();i++){
				if(list.get(i).getBigDecimal("un_putinstorage_amount").compareTo(BigDecimal.ZERO)>0){
					isAllOutPutStorage=false;
				}
			}
		}
		return isAllOutPutStorage;
	}
	

	
}
