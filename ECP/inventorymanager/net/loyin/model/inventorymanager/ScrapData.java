package net.loyin.model.inventorymanager;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;

import net.loyin.jfinal.anatation.TableBind;

@TableBind(name="inventory_scrap_data")
public class ScrapData extends Model<ScrapData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_scrap_data";
	public static ScrapData dao = new ScrapData();
	
	
/**
SELECT isd.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,
bmd.model_number,bmd.unit FROM inventory_scrap_data isd 
LEFT JOIN basic_material_data bmd ON isd.material_data_id=bmd.id
WHERE isd.inventory_scrap_id=? 
 */
	public List<ScrapData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT isd.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,");
				sql.append("bmd.model_number,bmd.unit FROM inventory_scrap_data isd ");
				sql.append("LEFT JOIN basic_material_data bmd ON isd.material_data_id=bmd.id WHERE isd.inventory_scrap_id=? ");
		return dao.find(sql.toString(), id);
	}
	
	
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		//先删除申请表中的物料数据
		if(list!=null&&list.size()>0){
			String sql="DELETE FROM inventory_scrap_data WHERE inventory_scrap_id=? ";
			Db.update(sql, list.get(0).get("inventory_scrap_id"));
		}
		Object[][]paras=new Object[list.size()][7];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("inventory_scrap_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][3] = (String) a.get("amount");
			if (StringUtils.isNotEmpty((String) a.get("target_price")))
				paras[i][4] = Float.parseFloat((String) a.get("target_price"));
			if (StringUtils.isNotEmpty((String) a.get("total_money")))
				paras[i][5] = Float.parseFloat((String) a.get("total_money"));
			if (StringUtils.isNotEmpty((String) a.get("comment")))
				paras[i][6] = a.get("comment");
			i++;
		}
		Db.batch("INSERT INTO inventory_scrap_data(id,inventory_scrap_id,material_data_id,amount,target_price,total_money,comment)VALUES (?,?,?,?,?,?,?);",
				paras,list.size());
	}
}
