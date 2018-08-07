package net.loyin.model.scm;

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
import net.loyin.model.sso.User;

/**
 * 
 * @author:lizhangyou
 * @Description:采购申请单</p>* 
 * @date 2017-9-18
 */
@TableBind(name="scm_purchase_apply")
public class PurchaseApply  extends Model<PurchaseApply> {
	private static final long  serialVersionUID = -4221825254783835788L;
	public static final String tableName="scm_purchase_apply";
	public static PurchaseApply dao = new PurchaseApply();
	
	
	public Page<PurchaseApply> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" from ");
		sql.append(" (SELECT * FROM scm_purchase_apply  WHERE 1=1  ");
		String company_id = (String) filter.get("company_id");
		if(StringUtils.isNoneBlank(company_id)){
			sql.append(" and company_id= ? ");
			parame.add(company_id);
		}
		String creater_id = (String) filter.get("creater_id");
		if(StringUtils.isNoneBlank(creater_id)){
			sql.append(" and creater_id= ? ");
			parame.add(creater_id);
		}
		String approve_status = (String) filter.get("approve_status");
		if(StringUtils.isNoneBlank(approve_status)){
			sql.append(" and approve_status = ? ");
			parame.add(approve_status);
		}
		String purchase_no = (String) filter.get("purchase_no");
		if(StringUtils.isNoneBlank(purchase_no)){
			sql.append(" and (purchase_no LIKE ? OR purchase_name LIKE ? ) ");
			parame.add("%"+purchase_no+"%");
			parame.add("%"+purchase_no+"%");
		}
		String is_deleted = (String) filter.get("is_deleted");
		if(StringUtils.isNoneBlank(is_deleted)){
			sql.append(" and is_deleted = ? ");
			parame.add(is_deleted);
		}
		sql.append("  ) pa LEFT JOIN sso_person p ON pa.purchase_staff_id=p.id  ");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNoneBlank(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"SELECT pa.*,p.realname AS purchase_staff_name  ",sql.toString(), parame.toArray());
	}
	
	/**
	 * SELECT spa.*,per.realname AS purchase_staff_name ,per2.realname AS creater_username
		FROM scm_purchase_apply spa
		LEFT JOIN sso_person per ON per.id=spa.purchase_staff_id
		LEFT JOIN sso_person per2 ON per2.id=spa.creater_id
		WHERE spa.id='276a20d4-60a7-42fb-8179-17cab5a7d565' AND spa.company_id='0001' 
	 * @param id
	 * @param company_id
	 * @return
	 */
	public PurchaseApply findById(String id ,String company_id){
		StringBuffer sql=new StringBuffer("SELECT spa.*,per.realname AS purchase_staff_name ,per2.realname AS creater_username,per3.realname AS update_username ");
		sql.append("FROM scm_purchase_apply spa ");
		sql.append("LEFT JOIN sso_person per ON per.id=spa.purchase_staff_id ");
		sql.append("LEFT JOIN sso_person per2 ON per2.id=spa.creater_id ");
		sql.append("LEFT JOIN sso_person per3 ON per3.id=spa.update_id ");
		sql.append("WHERE spa.id=? AND spa.company_id=?  ");
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
				PurchaseApply purchaseApply=PurchaseApply.dao.findById(id1);
				//如果申请单审批状态为5[已批准]，修改申请单是否删除状态为已删除[is_deleted=1]
				String approve_status = purchaseApply.getStr("approve_status");
				if(StringUtils.isNotBlank(approve_status)&&"5".equals(approve_status)){
					String sql=" UPDATE scm_purchase_apply SET is_deleted=1 WHERE id in ("+ids_.toString()+")";
					Db.update(sql, parame.toArray());
				}else{
					//驳回是删除流程
					if(StringUtils.isNotBlank(purchaseApply.getStr("process_id"))){
						ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
						processEngine.getRuntimeService().deleteProcessInstance(purchaseApply.getStr("process_id"), "采购申请表被删除！");
					}
					Db.update("delete  from scm_purchase_apply where id in ("+ids_.toString()+")",parame.toArray());
					Db.update("delete  from scm_purchase_apply_data where purchase_apply_id in ("+ids_.toString()+")",parame.toArray());
				}
			}
		}
	}
	
	/**提交审核/取消审核*/
	public void subAudit(String id, String company_id, String uid,String now,int status,String process_id) {
		if (StringUtils.isNotEmpty(id)) {
			//获取当前用户的上级用户
			//User u=User.dao.findSupUser(uid);
			//String auditor_id=u!=null?u.getStr("id"):"";
			if(status==1){
				Db.update("update " + tableName + " set approve_status=1,process_id=? where id=? and company_id=? and (update_id=? or creater_id=?) ",process_id,id,company_id,uid,uid);
			}
			if(status==0){
				Db.update("update " + tableName + " set audit_status=0 where id=? and company_id=? and (update_id=? or creater_id=?) ",id,company_id,uid,uid);
			}
		}
	}
	
	//根据流程实例id获取申请表
	public PurchaseApply getPurchaseApplyByProcessID(String processInstanceID){
		String sql="SELECT * FROM scm_purchase_apply WHERE process_id=?";
		return dao.findFirst(sql, processInstanceID);
	}
	
	/**
	 * 取消审核
	 */
	public void cancelApprove(String purchaseApproveID){
		//approvr_status==4表示‘已取回’
		String sql="UPDATE scm_purchase_apply SET process_id='',approve_status=4 WHERE id=?";
		Db.update(sql, purchaseApproveID);
	}
	
	/**
	 * 流程审核通过,结束流程
	 */
	public void finishApprove(String purchaseApplyID,String approve_status){
		//approvr_status==2表示‘批准’ approvr_status==‘3’驳回
		String sql="UPDATE scm_purchase_apply SET approve_status=? WHERE id=?";
		Db.update(sql, approve_status,purchaseApplyID);
	}
}
