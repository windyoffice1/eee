package net.loyin.model.scm;

import java.math.BigDecimal;
import java.util.ArrayList;
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
 * @Description:入库通知单</p>* 
 * @date 2017-9-18
 */
@TableBind(name="scm_putinstorage_data")
public class PutInStorageData  extends Model<PutInStorageData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="scm_putinstorage_data";
	public static PutInStorageData dao = new PutInStorageData();
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		
		Object[][] paras=new Object[list.size()][14];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("putinstorage_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("material_data_name")))
				paras[i][3] = (String) a.get("material_data_name");
			if (StringUtils.isNotEmpty((String) a.get("readey_putinstorage_amount")))
				paras[i][4] =Double.parseDouble( (String) a.get("readey_putinstorage_amount"));
			paras[i][5] = null;
			if (StringUtils.isNotEmpty((String) a.get("unit")))
				paras[i][6] = (String) a.get("unit");
			if (StringUtils.isNotEmpty((String) a.get("readey_putinstorage_total_money")))
				paras[i][7] = Float.parseFloat((String) a.get("readey_putinstorage_total_money"));
			paras[i][8] = a.get("purchase_price");
			paras[i][9] = a.get("create_time");
			//计划单价
			if (StringUtils.isNotEmpty((String) a.get("target_price")))
				paras[i][10] = Float.parseFloat((String) a.get("target_price"));
			//计划总金额
			if (StringUtils.isNotEmpty((String) a.get("plan_putinstorage_total_money")))
				paras[i][11] = Float.parseFloat((String) a.get("plan_putinstorage_total_money"));
			paras[i][12] = 0 ;
			if (StringUtils.isNotEmpty((String) a.get("readey_putinstorage_amount")))
				paras[i][13] =Double.parseDouble( (String) a.get("readey_putinstorage_amount"));
			i++;
		}
		Db.batch("INSERT INTO scm_putinstorage_data(id,putinstorage_id,material_data_id,material_data_name," +
				"amount,comment,unit,total_money,purchase_price,create_time,target_price,plan_putinstorage_total_money,real_putinstarage_amount," +
				"real_unputinstorage_amount)VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}
	
	/**
	 * 事务控制之恢复
	 */
	public void deletePutInStorageData(List<Map<String,Object>> list){
		//String sql = "DELETE FROM scm_putinstorage_data WHERE id=?";
		for(Map<String, Object> a : list){
			Db.deleteById("scm_putinstorage_data", a.get("id"));
		}
	}
	
	public List<PutInStorageData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT spd.*,bmd.model_number, bmd.material_name,bmd.material_no FROM scm_putinstorage_data spd ");
				sql.append("LEFT JOIN basic_material_data bmd ON spd.material_data_id=bmd.id ");
				sql.append("WHERE spd.putinstorage_id=? order by material_no ");
		return dao.find(sql.toString(), id);
		
	}
	
	
	//根据入库通知单核对，修改未入库数量
	@Before(Tx.class)
	public void realPutInstorage(List<Map<String,Object>> list){
		for (int i=0;i<list.size();i++) {
			List<Object> params=new ArrayList<Object>();
			Map<String,Object> a=list.get(i);
			params.add( a.get("real_unputinstorage_amount"));
			params.add(a.get("putinstorage_id"));
			params.add(a.get("material_data_id"));
			StringBuffer sql=new StringBuffer("UPDATE scm_putinstorage_data SET real_unputinstorage_amount=? ")
			.append(" where putinstorage_id=? AND material_data_id=? ");
			Db.update(sql.toString(), params.toArray());
		}

	}
	
	//判断入库单中的物料是否已经全部完成入库
	public boolean isAllPutInStorage(String putinstorage_id){
		String sql="SELECT* FROM scm_putinstorage_data where putinstorage_id=? ";
		List<PutInStorageData> list=dao.find(sql, putinstorage_id);
		boolean isAllPutinstorage=true;
		if(list!=null&&list.size()>0){
			for(int i=0; i<list.size();i++){
				if(list.get(i).getBigDecimal("real_unputinstorage_amount").compareTo(BigDecimal.ZERO)>0){
					isAllPutinstorage=false;
				}
			}
			
		}
		return isAllPutinstorage;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}


