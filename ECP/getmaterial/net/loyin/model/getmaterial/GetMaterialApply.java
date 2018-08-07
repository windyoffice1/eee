package net.loyin.model.getmaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.apache.commons.lang3.StringUtils;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.tx.Tx;
import net.loyin.jfinal.anatation.TableBind;
import net.loyin.model.scm.PurchaseApply;

@TableBind(name="getmaterial_apply")
public class GetMaterialApply extends Model<GetMaterialApply>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="getmaterial_apply";
	public static GetMaterialApply dao = new GetMaterialApply();
	/**
		SELECT ga.*,p1.realname AS create_user_name ,p2.realname AS update_user_name,bcw.child_warehouse_name FROM
		(SELECT * FROM getmaterial_apply WHERE 1=1 
		AND company_id=?
		AND create_user_id=?
		AND getmaterial_no=?
		AND getmaterial_name=?
		AND approve_status=?
		and child_warehouse_id=?
		) ga LEFT JOIN sso_person p1 ON ga.create_user_id=p1.id
		LEFT JOIN sso_person p2 ON ga.update_user_id=p2.id
		LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id
	 */
	public Page<GetMaterialApply> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append("(SELECT * FROM getmaterial_apply WHERE 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" and company_id= ? ");
			parame.add(company_id);
		}
		String creater_id = (String) filter.get("creater_id");
		if(StringUtils.isNotEmpty(creater_id)){
			sql.append(" and create_user_id=? ");
			parame.add(creater_id);
		}
		String is_deleted = (String) filter.get("is_deleted");
		if(StringUtils.isNoneBlank(is_deleted)){
			sql.append(" and is_deleted = ? ");
			parame.add(is_deleted);
		}
		String getmaterial_name = (String) filter.get("getmaterial_name");
		if(StringUtils.isNotEmpty(getmaterial_name)){
			sql.append(" AND (getmaterial_no like ? OR getmaterial_name like ?)");
			parame.add("%"+getmaterial_name+"%");
			parame.add("%"+getmaterial_name+"%");
		}
		String child_warehouse_id = (String) filter.get("child_warehouse_id");
		if(StringUtils.isNotEmpty(child_warehouse_id)){
			sql.append(" and child_warehouse_id=? ");
			parame.add(child_warehouse_id);
		}
		//出库单删除状态
		String is_deleted_outputstorage = (String) filter.get("is_deleted_outputstorage");
		if(StringUtils.isNotEmpty(is_deleted_outputstorage)){
			sql.append(" and is_deleted_outputstorage=? ");
			parame.add(is_deleted_outputstorage);
		}
		//审批状态
		String approve_status = (String) filter.get("approve_status");
		if(StringUtils.isNotEmpty(approve_status)){
			sql.append(" and approve_status=? ");
			parame.add(approve_status);
		}
		String outputstorage_status = (String) filter.get("outputstorage_status");
		if(StringUtils.isNotEmpty(outputstorage_status)){
			sql.append(" and outputstorage_status=? ");
			parame.add(outputstorage_status);
		}
/*		//筛选出已完成审批的
		sql.append(" AND len(outputstorage_status) >0 ");*/
		sql.append(" ) ga LEFT JOIN sso_person p1 ON ga.create_user_id=p1.id ");
		sql.append(" LEFT JOIN sso_person p2 ON ga.update_user_id=p2.id");
		sql.append(" LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"SELECT ga.*,p1.realname AS create_user_name ,p2.realname AS update_user_name,bcw.child_warehouse_name ",sql.toString(), parame.toArray());
	}
	
	public Page<GetMaterialApply> OutputStoragePageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append("(SELECT * FROM getmaterial_apply WHERE 1=1 ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNotEmpty(company_id)){
			sql.append(" and company_id= ? ");
			parame.add(company_id);
		}
		String create_user_id = (String) filter.get("create_user_id");
		if(StringUtils.isNotEmpty(create_user_id)){
			sql.append(" and create_user_id= ? ");
			parame.add(create_user_id);
		}
		String approve_status = (String) filter.get("approve_status");
		if(StringUtils.isNotEmpty(approve_status)){
			sql.append(" and approve_status = ? ");
			parame.add(approve_status);
		}
		String getmaterial_no = (String) filter.get("getmaterial_no");
		if(StringUtils.isNotEmpty(getmaterial_no)){
			sql.append(" and getmaterial_no= ? ");
			parame.add(getmaterial_no);
		}
		String getmaterial_name = (String) filter.get("getmaterial_name");
		if(StringUtils.isNotEmpty(getmaterial_name)){
			sql.append(" and getmaterial_name like ? ");
			parame.add("%"+getmaterial_name+"%");
		}
		String child_warehouse_id = (String) filter.get("child_warehouse_id");
		if(StringUtils.isNotEmpty(child_warehouse_id)){
			sql.append(" and child_warehouse_id=? ");
			parame.add(child_warehouse_id);
		}
		sql.append(" ) ga LEFT JOIN sso_person p1 ON ga.create_user_id=p1.id ");
		sql.append(" LEFT JOIN sso_person p2 ON ga.update_user_id=p2.id");
		sql.append(" LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"SELECT ga.*,p1.realname AS create_user_name ,p2.realname AS update_user_name,bcw.child_warehouse_name ",sql.toString(), parame.toArray());
	}
	
	public GetMaterialApply findById(String id ,String company_id){
		StringBuffer sql=new StringBuffer("SELECT ga.*,p1.realname AS getmaterial_user_name, ")
			.append("p2.realname AS update_user_name,p3.realname AS create_user_name, ")
			.append("bwl.warehouse_name,bcw.child_warehouse_name FROM ")
			.append("(SELECT * FROM getmaterial_apply  ")
			.append("WHERE id=? AND company_id=? ) ga ")
			.append("LEFT JOIN sso_person p1 ON ga.getmaterial_user_id=p1.id ")
			.append("LEFT JOIN sso_person p2 ON ga.update_user_id=p2.id ")
			.append("LEFT JOIN sso_person p3 ON ga.create_user_id=p3.id ")
			.append("LEFT JOIN basic_warehouse_location bwl ON ga.warehouse_id=bwl.id ")
			.append("LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id");
		return dao.findFirst(sql.toString(),id,company_id);
	}

	/**直接删除*/
	@Before(Tx.class)
	public void del(String id, String companyId) {
		if (StringUtils.isNotEmpty(id)) {
			String[] ids=id.split(",");
			StringBuffer ids_=new StringBuffer();
			List<String> parame=new ArrayList<String>();
			for(String id_:ids){
				ids_.append("?,");
				parame.add(id_);
			}
			ids_.append("'-'");
			for(String id1:ids){	
				//删除部署流程实例
				GetMaterialApply getMaterialApply=GetMaterialApply.dao.findById(id1 ,companyId);
				//如果申请单审批状态为2[已批准]，修改申请单是否删除状态为已删除[is_deleted=1]
				String approve_status = getMaterialApply.getStr("approve_status");
				if(StringUtils.isNotBlank(approve_status)&&"2".equals(approve_status)){
					String sql=" UPDATE getmaterial_apply SET is_deleted=1 WHERE id in ("+ids_.toString()+")";
					Db.update(sql, parame.toArray());
				}else{
					//驳回是删除流程
					if(StringUtils.isNotBlank(getMaterialApply.getStr("process_id"))){
						ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
						processEngine.getRuntimeService().deleteProcessInstance(getMaterialApply.getStr("process_id"), "领料申请表被删除！");
					}
					Db.update("delete  from getmaterial_apply where id in ("+ids_.toString()+")",parame.toArray());
					Db.update("delete  from getmaterial_apply_data where getmaterial_apply_id in ("+ids_.toString()+")",parame.toArray());
				}
			}
		}
	}
	
	/**直接出库核对单删除*/
	@Before(Tx.class)
	public void delHedui(String id, String company_id) {
		if (StringUtils.isNotEmpty(id)) {
			String[] ids=id.split(",");
			StringBuffer ids_=new StringBuffer();
			List<String> parame=new ArrayList<String>();
			for(String id_:ids){
				ids_.append("?,");
				parame.add(id_);
			}
			ids_.append("'-'");
			Db.update("update  getmaterial_apply set is_deleted_outputstorage='1' where id in ("+ids_.toString()+")",parame.toArray());
/*			Db.update("delete  from getmaterial_apply_data where getmaterial_apply_id in ("+ids_.toString()+")",parame.toArray());
			Db.update("delete  from getmaterial_outputstorage_data where getmaterial_apply_id in ("+ids_.toString()+")",parame.toArray());*/
		}
	}
	
	/**提交审核/取消审核*/
	public void subAudit(String id, String company_id, String uid,String now,int status,String process_id) {
		if (StringUtils.isNotEmpty(id)) {
			/*//获取当前用户的上级用户
			User u=User.dao.findSupUser(uid);*/
			//approve_status[0："未提交",1："待审核",2："已批准",3："驳回",4："已取回",5："已入出库"]
			if(status==1){
				Db.update("update " + tableName + " set approve_status=1,process_id=? where id=? and company_id=? and (update_user_id=? or create_user_id=?) ",process_id,id,company_id,uid,uid);
			}
			if(status==0){
				Db.update("update " + tableName + " set audit_status=0 where id=? process_id=NULL and company_id=? and (update_user_id=? or create_user_id=?) ",id,company_id,uid,uid);
			}
		}
	}
	
	/**
	 * 取消审核
	 */
	public void cancelApprove(String getMaterialApproveID){
		//approvr_status==4表示‘已取回’
		String sql="UPDATE getmaterial_apply SET process_id='',approve_status=4 WHERE id=?";
		Db.update(sql, getMaterialApproveID);
	}

	/**
	 * 根据流程实例id获取申请表
	 */
	public GetMaterialApply getMaterialApplyByProcessID(String processInstanceID){
		String sql="SELECT * FROM getmaterial_apply WHERE process_id=?";
		return dao.findFirst(sql, processInstanceID);
	}
	
	/**
	 * 流程审核通过,结束流程
	 */
	public void finishApprove(String getMaterialApplyID,String approve_status){
		//approvr_status==2表示‘批准’ approvr_status==‘3’驳回
		String sql="";
		if("2".equalsIgnoreCase(approve_status)){
			sql="UPDATE getmaterial_apply SET approve_status=?,outputstorage_status='0' WHERE id=?";
		}else{
			sql="UPDATE getmaterial_apply SET approve_status=? WHERE id=?";
		}
		
		Db.update(sql, approve_status,getMaterialApplyID);
	}
	
	/**
	 * 年修领料报表
SELECT LEFT(ga.create_time,4) AS YEAR,bmd.material_no AS material_data_no,bcw.child_warehouse_name,
bmd.material_name AS material_data_name,bmd.model_number,bmb.material_name AS belong_to_material_name,
SUM(gad.amount) AS amount,SUM(gad.total_money) AS total_money,SUM(gad.total_money)/SUM(gad.amount) AS average_price
FROM getmaterial_apply ga
LEFT JOIN getmaterial_apply_data gad ON ga.id=gad.getmaterial_apply_id
LEFT JOIN basic_material_data bmd ON gad.material_data_id=bmd.id 
LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id
LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id
WHERE ga.approve_status='2' 
AND ga.type='年修领料'
AND LEFT(ga.create_time,4) IN ('2017')
AND ga.child_warehouse_id IN ('')
GROUP BY LEFT(ga.create_time,4),bmd.material_no,bmd.material_name,bmd.model_number,bmb.material_name,bcw.child_warehouse_name
	 */
	public List<GetMaterialApply> getmaterialForRepair(String begin_year,String end_year,String child_warehouse_id){
		List<String> params=new ArrayList<String>();
		StringBuffer sql=new StringBuffer();
		sql.append("SELECT LEFT(ga.create_time,4) AS year,bmd.material_no AS material_data_no,bcw.child_warehouse_name,");
		sql.append("bmd.material_name AS material_data_name,bmd.model_number,bmb.material_name AS belong_to_material_name,");
		sql.append("SUM(gad.amount) AS amount,SUM(gad.total_money) AS total_money,SUM(gad.total_money)/SUM(gad.amount) AS average_price ");
		sql.append("FROM getmaterial_apply ga ");
		sql.append("LEFT JOIN getmaterial_apply_data gad ON ga.id=gad.getmaterial_apply_id ");
		sql.append("LEFT JOIN basic_material_data bmd ON gad.material_data_id=bmd.id  ");
		sql.append("LEFT JOIN basic_material_broad bmb ON bmd.belong_to_broad_id=bmb.id ");
		sql.append("LEFT JOIN basic_child_warehouse bcw ON ga.child_warehouse_id=bcw.id ");
		sql.append("WHERE ga.approve_status='2' AND ga.type='年修领料' ");
		if(StringUtils.isNotBlank(begin_year)&&StringUtils.isNotBlank(end_year)){
			Integer begin_year_int=Integer.parseInt(begin_year);
			Integer end_year_int=Integer.parseInt(end_year);
			if(begin_year_int<=end_year_int){
				sql.append(" AND LEFT(ga.create_time,4) IN ( ");
				for(int i=begin_year_int;i<=end_year_int;i++){
					sql.append("'"+i+"',");
				}
				sql.append(" '-')");
			}
		}
		if(StringUtils.isNotBlank(child_warehouse_id)){
			sql.append("AND ga.child_warehouse_id = ? ");
			params.add(child_warehouse_id);
		}
		sql.append(" GROUP BY LEFT(ga.create_time,4),bmd.material_no,bmd.material_name,bmd.model_number,bmb.material_name,bcw.child_warehouse_name");
		return dao.find(sql.toString(),params.toArray());
	}
}
