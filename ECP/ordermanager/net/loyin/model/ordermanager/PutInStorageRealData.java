package net.loyin.model.ordermanager;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import net.loyin.jfinal.anatation.TableBind;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
/**
 * 
 * @author:lizhangyou
 * @Description:入库通知单核对表</p>* 
 * @date 2017-9-18
 */
@TableBind(name="scm_putinstorage_real_data")
public class PutInStorageRealData  extends Model<PutInStorageRealData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="scm_putinstorage_real_data";
	public static PutInStorageRealData dao = new PutInStorageRealData();
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		Object[][] paras=new Object[list.size()][10];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("putinstorage_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("real_putinstarage_amount")))
				paras[i][3] = (String) a.get("real_putinstarage_amount");
			if (StringUtils.isNotEmpty((String) a.get("purchase_price")))
				paras[i][4] = Float.parseFloat((String) a.get("purchase_price"));
			if (StringUtils.isNotEmpty((String) a.get("real_putinstorage_total_money")))
				paras[i][5] = Float.parseFloat((String) a.get("real_putinstorage_total_money"));
			paras[i][6] = a.get("putinstorage_comment");
			paras[i][7] = a.get("putinstorage_warehouse_id");
			paras[i][8] = a.get("invoice_number");
			paras[i][9] = a.get("putinstorage_date");
			i++;
		}
		Db.batch("INSERT INTO scm_putinstorage_real_data(id,putinstorage_id,material_data_id,amount," +
				"purchase_price,total_money,putinstorage_comment,putinstorage_warehouse_id,invoice_number,putinstorage_date "+
				")VALUES (?,?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}
	
	
	public List<PutInStorageRealData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT spd.*,bmd.model_number, bmd.material_name as material_data_name, ");
				sql.append("bmd.material_no AS material_data_no,bmd.unit, ");
				sql.append("bwl.warehouse_name AS putinstorage_warehouse_name ");
				sql.append("FROM scm_putinstorage_real_data spd ");
				sql.append("LEFT JOIN basic_material_data bmd ON spd.material_data_id=bmd.id ");
				sql.append("LEFT JOIN basic_warehouse_location bwl ON spd.putinstorage_warehouse_id=bwl.id ");
				sql.append("WHERE spd.putinstorage_id=? order by material_data_no ");
		return dao.find(sql.toString(), id);
		/**
SELECT spd.*,bmd.model_number, bmd.material_name as material_data_name,
bmd.material_no AS material_data_no,bmd.unit,
bwl.warehouse_name AS putinstorage_warehouse_name
FROM scm_putinstorage_real_data spd 
LEFT JOIN basic_material_data bmd ON spd.material_data_id=bmd.id 
LEFT JOIN basic_warehouse_location bwl ON spd.putinstorage_warehouse_id=bwl.id
WHERE spd.putinstorage_id=?
		 */
	}
	
	
	
	
	
	
	
	
	
}


