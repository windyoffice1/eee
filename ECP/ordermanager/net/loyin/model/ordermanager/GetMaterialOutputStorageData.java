package net.loyin.model.ordermanager;

import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import net.loyin.jfinal.anatation.TableBind;
/**
 * 
 * @author:lizhangyou
 * @Description:出库单核对数据</p>* 
 * @date 2017-9-18
 */
@TableBind(name="getmaterial_outputstorage_data")
public class GetMaterialOutputStorageData  extends Model<GetMaterialOutputStorageData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="getmaterial_outputstorage_data";
	public static GetMaterialOutputStorageData dao = new GetMaterialOutputStorageData();
	
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		Object[][] paras=new Object[list.size()][9];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("getmaterial_apply_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty(a.get("amount").toString()))
				paras[i][3] =a.get("amount").toString();
			if (StringUtils.isNotEmpty(a.get("purchase_price").toString()))
				paras[i][4] = Float.parseFloat(a.get("purchase_price").toString());
			if (StringUtils.isNotEmpty( a.get("total_money").toString()))
				paras[i][5] = Float.parseFloat( a.get("total_money").toString());
			paras[i][6] = a.get("outputstorage_warehouse_id");
			paras[i][7] = a.get("outputstorage_comment");
			paras[i][8] = a.get("outputstorage_date");
			i++;
		}
		Db.batch("INSERT INTO getmaterial_outputstorage_data(id,getmaterial_apply_id,material_data_id,amount," +
				"purchase_price,total_money,outputstorage_warehouse_id,outputstorage_comment,outputstorage_date "+
				")VALUES (?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}
	
	
	public List<GetMaterialOutputStorageData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT god.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,");
				sql.append("(CASE WHEN bwl.warehouse_name IS NULL THEN bcw.child_warehouse_name ELSE bwl.warehouse_name END) AS outputstorage_warehouse_name, ");
				sql.append("bmd.model_number,bmd.unit FROM getmaterial_outputstorage_data god ");
				sql.append("LEFT JOIN basic_material_data bmd ON god.material_data_id=bmd.id ");
				sql.append("LEFT JOIN basic_warehouse_location bwl ON god.outputstorage_warehouse_id=bwl.id ");
				sql.append("LEFT JOIN basic_child_warehouse bcw ON god.outputstorage_warehouse_id=bcw.id  ");
				sql.append("WHERE god.getmaterial_apply_id=? order by bmd.material_no  ");
		return dao.find(sql.toString(), id);
		/**
SELECT god.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,
(CASE WHEN bwl.warehouse_name IS NULL THEN bcw.child_warehouse_name ELSE bwl.warehouse_name END) AS outputstorage_warehouse_name,
bmd.model_number,bmd.unit FROM getmaterial_outputstorage_data god
LEFT JOIN basic_material_data bmd ON god.material_data_id=bmd.id
LEFT JOIN basic_warehouse_location bwl ON god.outputstorage_warehouse_id=bwl.id
LEFT JOIN basic_child_warehouse bcw ON god.outputstorage_warehouse_id=bcw.id 
		 */
	}
	
	
}
