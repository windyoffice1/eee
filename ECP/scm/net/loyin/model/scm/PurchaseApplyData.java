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
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;

@TableBind(name="scm_purchase_apply_data")
public class PurchaseApplyData extends Model<PurchaseApplyData>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="scm_purchase_apply_data";
	public static PurchaseApplyData dao = new PurchaseApplyData();
	
	@Before(Tx.class)
	public void insert(List<Map<String,Object>> list){
		//先删除申请表中的物料数据
		if(list!=null&&list.size()>0){
			String sql="DELETE FROM scm_purchase_apply_data WHERE purchase_apply_id=? ";
			Db.update(sql, list.get(0).get("purchase_apply_id"));
		}
		
		Object[][]paras=new Object[list.size()][12];
		int i=0;
		for (Map<String, Object> a : list) {
			paras[i][0] = a.get("id");
			paras[i][1] = a.get("purchase_apply_id");
			paras[i][2] = a.get("material_data_id");
			if (StringUtils.isNotEmpty((String) a.get("target_price")))
				paras[i][3] = new BigDecimal((String) a.get("target_price"));
			if (StringUtils.isNotEmpty((String) a.get("material_data_name")))
				paras[i][4] = (String) a.get("material_data_name");
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][5] = Float.parseFloat((String) a.get("amount"));
			if (StringUtils.isNotEmpty((String) a.get("total_money")))
				paras[i][6] = Float.parseFloat((String) a.get("total_money"));
			paras[i][7] = a.get("description");
			paras[i][8] = a.get("unit");
			//未入库数量
			if (StringUtils.isNotEmpty((String) a.get("amount")))
				paras[i][9] = Float.parseFloat((String) a.get("amount"));
			//已入库数量
			paras[i][10] = 0;
			if (StringUtils.isNotEmpty((String) a.get("existing_amount")))
				paras[i][11] = Float.parseFloat((String) a.get("existing_amount"));
			i++;
		}
		Db.batch("INSERT INTO scm_purchase_apply_data(id,purchase_apply_id,material_data_id,target_price,material_data_name,amount,total_money,description,unit," +
				"un_putinstorage_amount,putinstorage_amount,existing_amount)VALUES (?,?,?,?,?,?,?,?,?,?,?,?);",
				paras,list.size());
	}
	
	public List<PurchaseApplyData> list(String id){
		StringBuffer sql = new StringBuffer("SELECT spad.*,bmd.model_number,bmd.material_name as material_data_name,bmd.material_no as material_data_no"); 
				sql.append(",(convert(VARCHAR,cast(round(bmd.target_price*(1-bmd.float_rate),2)as numeric(38,2)))+'-'+convert(VARCHAR,cast(round(bmd.target_price*(1+bmd.float_rate),2)as numeric(38,2)))) AS float_price FROM scm_purchase_apply_data  spad ");
				sql.append("LEFT JOIN basic_material_data bmd ON spad.material_data_id=bmd.id ");
				sql.append("WHERE purchase_apply_id=? ORDER BY material_data_no ");
		return dao.find(sql.toString(), id);
	}
	
	//修改申请单的已出库数量
	public void updatePutInStorage(List<Map<String,Object>> list) throws Exception{
		String sql = "UPDATE scm_purchase_apply_data SET putinstorage_amount=? ,un_putinstorage_amount=? WHERE id=?";
		if(list!=null&&list.size()>0){
			for(int i=0 ; i<list.size(); i++){
				String purchaseApplyDataID=(String) list.get(i).get("purchaseApplyDataID");
				PurchaseApplyData purchaseApplyData=PurchaseApplyData.dao.findById(purchaseApplyDataID);
				//准备入库的数量
				BigDecimal readey_putinstorage_amount=new BigDecimal((String) list.get(i).get("readey_putinstorage_amount"));
				//原来未入库数量
				BigDecimal up_putinstorage_amount=purchaseApplyData.getBigDecimal("un_putinstorage_amount");
				if(readey_putinstorage_amount.compareTo(up_putinstorage_amount)<=0){
					//现未入库数量=原来未入库数量-准备入库的数量
					up_putinstorage_amount=up_putinstorage_amount.subtract(readey_putinstorage_amount);
					//原来已入库数量
					BigDecimal putinstorage_amount=purchaseApplyData.getBigDecimal("putinstorage_amount");
					//现已入库数量=原来已入库数量+准备入库数量
					putinstorage_amount=putinstorage_amount.add(readey_putinstorage_amount);
					if(purchaseApplyData!=null){
						Db.update(sql, putinstorage_amount,up_putinstorage_amount,purchaseApplyDataID);
					}
				}else{
					throw new Exception("入库数量不能大于未入库数量");
				}
			}
		}
	}
	
	//判断采购申请单的物料是否已经全部完成入库,如全部完成入库，则改变申请单状态["未提交","待审核","批准","驳回","已取回","已入库"]
	public void updatePurchaseStatus(String purchaseApplyID){
		String sql="SELECT * FROM scm_purchase_apply_data WHERE purchase_apply_id=?";
		int isFinih=1;
		List<PurchaseApplyData> list = dao.find(sql, purchaseApplyID);
		if(list!=null&&list.size()>0){
			for(PurchaseApplyData data : list){
				if(!(data.getBigDecimal("un_putinstorage_amount").compareTo(BigDecimal.ZERO)==0)){
					isFinih=0;
				}
			}
			//全部完成入库。改变申请单状态
			if(isFinih==1){
				String updateSql="UPDATE scm_purchase_apply SET approve_status='5' WHERE id=?";
				Db.update(updateSql, purchaseApplyID);
			}
		}

		
	}
	
	/***
	 * 	查询出所有审核通过且未入库的记录
	 * @param pageNo
	 * @param pageSize
	 * @param filter
	 * @return
	 */
	public List<PurchaseApplyData> pageGrid(String company_id,String creater_id){
		
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer();
		sql.append(" SELECT spa.purchase_no,spa.purchase_name,	spad.*,bmd.model_number,bmd.material_no AS material_data_no  FROM scm_purchase_apply_data spad  ");
		sql.append(" join scm_purchase_apply spa ON spad.purchase_apply_id= spa.id ");
		sql.append(" JOIN basic_material_data bmd ON spad.material_data_id= bmd.id ");
		sql.append("  where spad.approve_status IS NULL   AND spa.approve_status= '2' ");
		if(StringUtils.isNotBlank(company_id)){
			sql.append(" and spa.company_id= ? ");
			parame.add(company_id);
		}
		if(StringUtils.isNotBlank(creater_id)){
			sql.append(" and spa.creater_id= ? ");
			parame.add(creater_id);
		}
		sql.append(" order by  spa.create_time desc");
		return dao.find(sql.toString(), parame.toArray());
		
	}
}
