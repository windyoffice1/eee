package net.loyin.model.inventorymanager;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;

import net.loyin.jfinal.anatation.TableBind;

@TableBind(name="inventory_check_data")
public class CheckData extends Model<CheckData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_check_data";
	public static CheckData dao = new CheckData();
	
	
/**
SELECT icd.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,
bmd.model_number,bmd.unit from
(SELECT * FROM inventory_check_data WHERE inventory_check_id=? ) icd
LEFT JOIN basic_material_data bmd ON icd.material_data_id=bmd.id
 */
	public List<CheckData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT icd.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,");
				sql.append("bmd.model_number,bmd.unit from (SELECT * FROM inventory_check_data WHERE inventory_check_id=? ) icd ");
				sql.append("LEFT JOIN basic_material_data bmd ON icd.material_data_id=bmd.id ");
		return dao.find(sql.toString(), id);
	}
	
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		Object[][]paras=new Object[list.size()][9];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("inventory_check_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("target_price")))
				paras[i][3] = Integer.parseInt((String) a.get("target_price"));
			if (StringUtils.isNotEmpty((String) a.get("original_amount")))
				paras[i][4] = Float.parseFloat((String) a.get("original_amount"));
			if (StringUtils.isNotEmpty((String) a.get("now_amount")))
				paras[i][5] = Float.parseFloat((String) a.get("now_amount"));
			if (StringUtils.isNotEmpty((String) a.get("deviation_amount")))
				paras[i][6] = Float.parseFloat((String) a.get("deviation_amount"));
			if (StringUtils.isNotEmpty((String) a.get("deviation_total_money")))
				paras[i][7] = Float.parseFloat((String) a.get("deviation_total_money"));
			if (StringUtils.isNotEmpty((String) a.get("comment")))
				paras[i][8] = a.get("comment");
			i++;
		}
		Db.batch("INSERT INTO inventory_check_data(id,inventory_check_id,material_data_id,target_price,original_amount,now_amount,deviation_amount," +
				"deviation_total_money,comment)VALUES (?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}
}

















