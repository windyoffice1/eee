package net.loyin.model.returnmaterial;

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

@TableBind(name="returnmaterial_apply")
public class ReturnMaterialApply extends Model<ReturnMaterialApply>{
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="returnmaterial_apply";
	public static ReturnMaterialApply dao = new ReturnMaterialApply();
	
	/**
SELECT ra.*,bcw.child_warehouse_name,p1.realname AS create_user_name ,p2.realname AS update_user_name FROM
(SELECT * FROM returnmaterial_apply WHERE 1=1 
AND company_id=?
AND create_user_id=?
AND returnmaterial_no=?
AND returnmaterial_name=?
AND approve_status=?
) ra LEFT JOIN sso_person p1 ON ra.create_user_id=p1.id
LEFT JOIN sso_person p2 ON ra.update_user_id=p2.id
LEFT JOIN basic_child_warehouse bcw ON ra.child_warehouse_id=bcw.id
	 */
	public Page<ReturnMaterialApply> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append("(SELECT * FROM returnmaterial_apply WHERE 1=1 ");
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
		String is_deleted = (String) filter.get("is_deleted");
		if(StringUtils.isNoneBlank(is_deleted)){
			sql.append(" and is_deleted = ? ");
			parame.add(is_deleted);
		}
		String returnmaterial_no = (String) filter.get("returnmaterial_no");
		if(StringUtils.isNotEmpty(returnmaterial_no)){
			sql.append("AND returnmaterial_no like ? ");
			parame.add("%"+returnmaterial_no+"%");
		}
		String returnmaterial_name = (String) filter.get("returnmaterial_name");
		if(StringUtils.isNotEmpty(returnmaterial_name)){
			sql.append(" and returnmaterial_name like ? ");
			parame.add("%"+returnmaterial_name+"%");
		}
		//退料情况[1：'待退料',2：'已退料',3：'退料中。。']
		String putinstorage_status = (String) filter.get("putinstorage_status");
		if(StringUtils.isNotEmpty(putinstorage_status)){
			sql.append(" and putinstorage_status like ? ");
			parame.add(putinstorage_status);
		}
		//退料单位
		String child_warehouse_id = (String) filter.get("child_warehouse_id");
		if(StringUtils.isNotEmpty(child_warehouse_id)){
			sql.append(" and child_warehouse_id like ? ");
			parame.add(child_warehouse_id);
		}
		//退料核对是否删除
		String is_deleted_putinstorage = (String) filter.get("is_deleted_putinstorage");
		if(StringUtils.isNotEmpty(is_deleted_putinstorage)){
			sql.append("AND is_deleted_putinstorage=?");
			parame.add(is_deleted_putinstorage);
		}
		//查询出已批准了的
		//sql.append(" and approve_status='2' ");
		sql.append(") ra LEFT JOIN sso_person p1 ON ra.create_user_id=p1.id ");
		sql.append(" LEFT JOIN sso_person p2 ON ra.update_user_id=p2.id ");
		sql.append(" LEFT JOIN basic_child_warehouse bcw ON ra.child_warehouse_id=bcw.id ");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"SELECT ra.*,bcw.child_warehouse_name,p1.realname AS create_user_name ,p2.realname AS update_user_name ",sql.toString(), parame.toArray());
	}
	
	/**
SELECT ra.*,p1.realname AS create_user_name ,p2.realname AS update_user_name,
p3.realname AS returnmaterial_user_name,bcw.child_warehouse_name FROM
(SELECT * FROM returnmaterial_apply WHERE 1=1 AND id=? 
) ra LEFT JOIN sso_person p1 ON ra.create_user_id=p1.id
LEFT JOIN sso_person p2 ON ra.update_user_id=p2.id
LEFT JOIN sso_person p3 ON ra.returnmaterial_user_id=p3.id
LEFT JOIN basic_child_warehouse bcw ON ra.child_warehouse_id=bcw.id
	 */
	public ReturnMaterialApply findById(String id ,String company_id){
		StringBuffer sql=new StringBuffer("SELECT ra.*,p1.realname AS create_user_name ,p2.realname AS update_user_name,")
			.append("p3.realname AS returnmaterial_user_name,bcw.child_warehouse_name FROM ")
			.append("(SELECT * FROM returnmaterial_apply WHERE 1=1 AND id=? AND company_id=? ")
			.append(") ra LEFT JOIN sso_person p1 ON ra.create_user_id=p1.id  ")
			.append("LEFT JOIN sso_person p2 ON ra.update_user_id=p2.id ")
			.append("LEFT JOIN sso_person p3 ON ra.returnmaterial_user_id=p3.id ")
			.append("LEFT JOIN basic_child_warehouse bcw ON ra.child_warehouse_id=bcw.id ");
		return dao.findFirst(sql.toString(),id,company_id);
	}
	
	/**
	 * 修改申请单是否删除状态为已删除
	 * @param id
	 * @param companyId
	 */
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
				ReturnMaterialApply returnMaterialApply=ReturnMaterialApply.dao.findById(id1 ,companyId);
				//如果申请单审批状态为2[已批准]，修改申请单是否删除状态为已删除[is_deleted=1]
				String approve_status = returnMaterialApply.getStr("approve_status");
				if(StringUtils.isNotBlank(approve_status)&&"2".equals(approve_status)){
					String sql=" UPDATE returnmaterial_apply SET is_deleted=1 WHERE id in ("+ids_.toString()+")";
					Db.update(sql, parame.toArray());
				}else{
					//驳回是删除流程
					if(StringUtils.isNotBlank(returnMaterialApply.getStr("process_id"))){
						ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
						processEngine.getRuntimeService().deleteProcessInstance(returnMaterialApply.getStr("process_id"), "领料申请表被删除！");
					}
					Db.update("delete  from returnmaterial_apply where id in ("+ids_.toString()+")",parame.toArray());
					Db.update("delete  from returnmaterial_apply_data where returnmaterial_apply_id in ("+ids_.toString()+")",parame.toArray());
				}
			}
		}
	}
	
	/**
	 * 根据流程实例id获取申请表
SELECT ra.*,bcw.child_warehouse_name FROM returnmaterial_apply ra 
LEFT JOIN basic_child_warehouse bcw ON ra.child_warehouse_id=bcw.id
WHERE process_id=
	 */
	public ReturnMaterialApply getReturnMaterialApplyByProcessID(String processInstanceID){
		StringBuffer sql=new StringBuffer("SELECT ra.*,bcw.child_warehouse_name,p.realname AS create_user_name FROM returnmaterial_apply ra " );
				sql.append("LEFT JOIN basic_child_warehouse bcw ON ra.child_warehouse_id=bcw.id LEFT JOIN sso_person p ON ra.create_user_id=p.id  WHERE process_id=? ");
		return dao.findFirst(sql.toString(), processInstanceID);
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
	public void cancelApprove(String purchaseApproveID){
		//approvr_status==4表示‘已取回’
		String sql="UPDATE returnmaterial_apply SET process_id='',approve_status=4 WHERE id=? ";
		Db.update(sql, purchaseApproveID);
	}
	
	/**
	 * 流程审核通过,结束流程
	 */
	public void finishApprove(String purchaseApplyID,String approve_status){
		//approvr_status==2表示‘批准’ approvr_status==‘3’驳回
		String sql="";
		if("2".equalsIgnoreCase(approve_status)){
			 sql="UPDATE returnmaterial_apply SET approve_status=?,putinstorage_status='0' WHERE id=?";
		}else{
			 sql="UPDATE returnmaterial_apply SET approve_status=? WHERE id=?";
		}
		Db.update(sql, approve_status,purchaseApplyID);
	}
	
	/**直接出库核对单删除*/
	@Before(Tx.class)
	public void delHeDui(String id, String company_id) {
		if (StringUtils.isNotEmpty(id)) {
			String[] ids=id.split(",");
			StringBuffer ids_=new StringBuffer();
			List<String> parame=new ArrayList<String>();
			for(String id_:ids){
				ids_.append("?,");
				parame.add(id_);
			}
			ids_.append("'-'");
			Db.update("update  returnmaterial_apply set is_deleted_putinstorage='1' where id in ("+ids_.toString()+")",parame.toArray());
		}
	}
}
