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
 * @Description:退料通知单核对表</p>* 
 * @date 2017-9-18
 */
@TableBind(name="returnmaterial_putinstorage_data")
public class ReturnMaterialPutInStorageData  extends Model<ReturnMaterialPutInStorageData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="returnmaterial_putinstorage_data";
	public static ReturnMaterialPutInStorageData dao = new ReturnMaterialPutInStorageData();
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		Object[][] paras=new Object[list.size()][9];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("returnmaterial_apply_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][3] = (String) a.get("amount");
			if (StringUtils.isNotEmpty((String) a.get("target_price")))
				paras[i][4] = Float.parseFloat((String) a.get("target_price"));
			if (StringUtils.isNotEmpty((String) a.get("total_money")))
				paras[i][5] = Float.parseFloat((String) a.get("total_money"));
			paras[i][6] = a.get("putinstorage_comment");
			paras[i][7] = a.get("is_qualified");
			paras[i][8] = a.get("putinstorage_date");
			i++;
		}
		Db.batch("INSERT INTO returnmaterial_putinstorage_data(id,returnmaterial_apply_id,material_data_id,amount," +
				"target_price,total_money,putinstorage_comment,is_qualified,putinstorage_date "+
				")VALUES (?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}

	public List<ReturnMaterialPutInStorageData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT rpd.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,");
				sql.append("bmd.model_number,bmd.unit FROM  ");
				sql.append("(SELECT * FROM  returnmaterial_putinstorage_data  WHERE returnmaterial_apply_id=?) rpd ");
				sql.append("LEFT JOIN basic_material_data bmd ON rpd.material_data_id=bmd.id ORDER BY material_data_no ");
		return dao.find(sql.toString(), id);
		/**
SELECT rpd.*,bmd.material_name AS material_data_name,bmd.material_no AS material_data_no,
bmd.model_number,bmd.unit FROM
(SELECT * FROM  returnmaterial_putinstorage_data 
WHERE returnmaterial_apply_id='e634f03d-0204-4451-ab95-ec0084fcc13e') rpd
LEFT JOIN basic_material_data bmd ON rpd.material_data_id=bmd.id
		 */
	}
	
	
	
	
	
	
	
	
	
}


