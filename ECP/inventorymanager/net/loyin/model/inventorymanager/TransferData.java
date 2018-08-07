package net.loyin.model.inventorymanager;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import net.loyin.jfinal.anatation.TableBind;

@TableBind(name="inventory_transfer_data")
public class TransferData extends Model<TransferData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="inventory_transfer_data";
	public static TransferData dao = new TransferData();

	public List<TransferData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT ind.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no, ");
				sql.append("bmd.model_number,bmd.unit FROM inventory_transfer_data ind ");
				sql.append("LEFT JOIN basic_material_data bmd ON ind.material_data_id=bmd.id WHERE ind.inventory_transfer_id=? ");
		return dao.find(sql.toString(), id);
	/**
	SELECT ind.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,bmd.model_number
	FROM inventory_transfer_data ind
	LEFT JOIN basic_material_data bmd ON ind.material_data_id=bmd.id
	WHERE ind.inventory_transfer_id=?
	 */
	}
	
	//物料移位
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		//先删除申请表中的物料数据
		if(list!=null&&list.size()>0){
			String sql="DELETE FROM inventory_transfer_data WHERE inventory_transfer_id=? ";
			Db.update(sql, list.get(0).get("inventory_transfer_id"));
		}
		Object[][]paras=new Object[list.size()][7];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("inventory_transfer_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][3] = (String) a.get("amount");
			if (StringUtils.isNotEmpty((String) a.get("target_price")))
				paras[i][4] = Float.parseFloat((String) a.get("target_price"));
			if (StringUtils.isNotEmpty((String) a.get("total_money")))
				paras[i][5] = Float.parseFloat((String) a.get("total_money"));
			paras[i][6] = a.get("comment");
			i++;
		}
		Db.batch("INSERT INTO inventory_transfer_data(id,inventory_transfer_id,material_data_id,amount,target_price,total_money,comment )VALUES (?,?,?,?,?,?,?);",
				paras,list.size());
	}
	
}
