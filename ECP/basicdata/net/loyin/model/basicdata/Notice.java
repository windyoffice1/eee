package net.loyin.model.basicdata;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import net.loyin.jfinal.anatation.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
/**
 * 
 * @author:lizhangyou
 * @Description:通知 </p>* 
 * @date 2017-9-12
 */
@TableBind(name="basic_notice")
public class Notice  extends Model<Notice> {

	private static final long serialVersionUID = -4221825254783835788L;
	public static final String tableName="basic_notice";
	public static Notice dao=new Notice();
	/**
	 * 分页条件查询
SELECT bn.*,p.realname AS create_user_name FROM 
(SELECT * FROM basic_notice WHERE company_id='0001' AND create_user_id='00123') bn
LEFT JOIN sso_person p ON bn.create_user_id=p.id
	 */
	public Page<Notice> pageGrid(int pageNo, int pageSize,Map<String, Object> filter) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer();
		sql.append("FROM   ");
		sql.append("(SELECT * FROM basic_notice WHERE 1=1  ");
		String conpany_id=(String) filter.get("company_id");
		if(StringUtils.isNotEmpty(conpany_id)){
			sql.append(" and company_id=? ");
			parame.add(conpany_id);
		}
		String create_user_id=(String)filter.get("create_user_id");
		if(StringUtils.isNotEmpty(create_user_id)){
			sql.append("  AND create_user_id=? ");
			parame.add(create_user_id);
		}
		String title=(String)filter.get("title");
		if(StringUtils.isNotEmpty(title)){
			sql.append("  AND title=? ");
			parame.add(title);
		}
		sql.append(") bn LEFT JOIN sso_person p ON bn.create_user_id=p.id ");
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		StringBuffer sqlColun=new StringBuffer("SELECT bn.*,p.realname AS create_user_name ");
		return dao.paginate(pageNo,pageSize,sqlColun.toString(),sql.toString(), parame.toArray());
		
	}
	
	public Notice findByIdAndCompanyid(String company_id,String id){
		StringBuffer sql=new StringBuffer("SELECT bn.*,p.realname AS create_user_name FROM ")
					.append("(SELECT * FROM basic_notice WHERE company_id=? AND id=? ) bn ")
					.append("LEFT JOIN sso_person p ON bn.create_user_id=p.id");
		return dao.findFirst(sql.toString(), company_id,id);
	}
	
	//发布通知为每个人都通知
	public void pushNoticeToAll(String notice_id){
		List<Record> ids=Db.find("SELECT id FROM sso_user");
		String sql="INSERT INTO basic_notice_user(id,notice_id,user_id,status) VALUES(?,?,?,?)";
		Object[][] paras=new Object[ids.size()][4];
		int i=0;
		for(Record user:ids){
			paras[i][0]=UUID.randomUUID().toString();
			paras[i][1]=notice_id;
			paras[i][2]=user.get("id");
			//通知状态为未读【0：未读，1：已读】
			paras[i][3]="0";
			i++;
		}
		Db.batch(sql, paras, ids.size());
	}
	
	public void del(String id,String company_id,String user_id) {
		if (StringUtils.isNotEmpty(id)) {
			String[] ids=id.split(",");
			StringBuffer ids_=new StringBuffer();
			List<String> parame=new ArrayList<String>();
			StringBuffer idsString=new StringBuffer();
			for(String id_:ids){
				ids_.append("?,");
				parame.add(id_);
				idsString.append("'"+id_+"',");
			}
			ids_.append("'-'");
			idsString.append("'-'");
			parame.add(company_id);
			parame.add(user_id);
			Db.update("delete  from basic_notice_user where notice_id in ("+idsString.toString()+")");
			Db.update("delete  from " + tableName + " where id in ("+ids_.toString()+") and company_id=? and create_user_id=?  ",parame.toArray());
		}
	}
	
	
	public List<Notice> getUnrealNotice(String user_id,String status){
		StringBuffer sql=new StringBuffer();
		sql.append(" SELECT bn.*,bnu.status FROM(SELECT * FROM basic_notice_user WHERE user_id=? AND status=? ) bnu ");
		sql.append(" LEFT JOIN basic_notice bn ON bnu.notice_id=bn.id ORDER BY create_time DESC");
		List<Notice> list = dao.find(sql.toString(), user_id,status);
		return list;
	}
	
	public Page<Notice> getUserNotice(int pageNo, int pageSize,Map<String, Object> filter){
		StringBuffer sql=new StringBuffer();
		sql.append(" FROM (SELECT * FROM basic_notice_user WHERE user_id= ? ) bnu ");
		sql.append(" LEFT JOIN basic_notice bn ON bnu.notice_id=bn.id ");
		sql.append(" LEFT JOIN sso_person p ON bn.create_user_id=p.id ");
		StringBuffer sqlColun=new StringBuffer("SELECT bn.*,bnu.status,p.realname AS create_user_name ");
		List<Object> parame=new ArrayList<Object>();
		parame.add(filter.get("user_id"));
		return dao.paginate(pageNo,pageSize,sqlColun.toString(),sql.toString(), parame.toArray());
	}
	
	public void updataNoticeStatus(String user_id,String notice_id){
		String sql="UPDATE basic_notice_user SET status='1' WHERE user_id=? AND notice_id=?";
		Db.update(sql, user_id,notice_id);
	}
	
}
