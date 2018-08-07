package net.loyin.model.sso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import net.loyin.jfinal.anatation.TableBind;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
/**
 * 操作日志
 * @author 龙影
 */
@TableBind(name="sso_action_log")
public class ActionLog extends Model<ActionLog> {
	private static final long serialVersionUID = -2062896551324797433L;
	public static final String tableName="sso_action_log";
	public static ActionLog dao=new ActionLog();
	/**其他*/
	public static final int OTH_=0;
	/**查询*/
	public static final int QRY_=1;
	/**添加*/
	public static final int ADD_=2;
	/**删除*/
	public static final int DEL_=3;
	/**编辑*/
	public static final int EDIT_=4;
	/**授权*/
	public static final int AUTH_=5;
	/**审批*/
	public static final int PIHENPI_=6;
	/**提交*/
	public static final int SUBMIT_=7;
	/**导入*/
	public static final int IMPL_=8;
	/**导出*/
	public static final int EXPOUT_=9;
	/**设置权限*/
	public static final int POWER_ = 10;
	public Page<ActionLog> page(int pageNo, int pageSize,StringBuffer where, List<Object> param) {
		return dao.paginate(pageNo,pageSize,"select  * ",	"from " + tableName + " d where 1=1 "+ where.toString(), param.toArray());
	}
	/**
	 * 添加日志
	 * @param modelName	模块名称
	 * @param func					功能
	 * @param uid					用户id
	 * @param exct					执行操作
	 * @param detail				操作内容
	 */
	public void addLog(String modelName,String func,String uid,String ip,int exct,String detail){
		//String sql="INSERT INTO "+ tableName +" (id,user_id,model_name,action_name,content,create_datetime,ip) value(?,?,?,?,?,?,?)";
		ActionLog actionLog =new ActionLog();
		actionLog.set("id", UUID.randomUUID().toString());
		actionLog.set("user_id", uid);
		actionLog.set("model_name", modelName);
		actionLog.set("action_name", exct);
		actionLog.set("detail", detail);
		actionLog.set("ip", ip);
		SimpleDateFormat dateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		actionLog.set("create_datetime", dateFormat.format(new Date()));
		actionLog.save();
	}
	/**
SELECT sal.*,u.uname AS uname FROM sso_action_log sal
JOIN sso_user u ON sal.user_id=p.id 
WHERE 1=1 
AND u.uname LIKE '%%'
AND sal.model_name=''
AND sal.action_name=''
	 * @param pageNo
	 * @param pageSize
	 * @param filter
	 * @param paraToInt
	 * @return
	 */
	public Page<ActionLog> page(int pageNo, int pageSize, Map<String, Object> filter,Integer paraToInt) {
		List<Object> parame=new ArrayList<Object>();
		StringBuffer sql=new StringBuffer(" FROM sso_action_log sal ");
		sql.append(" JOIN sso_user u ON sal.user_id=u.id  WHERE 1=1 ");
		String uname = (String) filter.get("uname");
		if(StringUtils.isNotEmpty(uname)){
			sql.append(" AND u.uname LIKE ? ");
			parame.add("%"+uname+"%");
		}
		String model_name = (String) filter.get("model_name");
		if(StringUtils.isNotEmpty(model_name)){
			sql.append(" AND sal.model_name LIKE ? ");
			parame.add("%"+model_name+"%");
		}
		String start_date = (String) filter.get("start_date");
		if(StringUtils.isNotEmpty(start_date)){
			sql.append(" AND sal.create_datetime > ? ");
			parame.add(start_date);
		}
		String end_date = (String) filter.get("end_date");
		if(StringUtils.isNotEmpty(end_date)){
			sql.append("  AND sal.create_datetime < ?  ");
			parame.add(end_date);
		}
		String action_name = (String) filter.get("action_name");
		if(StringUtils.isNotEmpty(action_name)){
			sql.append(" AND sal.action_name=? ");
			parame.add(action_name);
		}
		String _sortField=(String)filter.get("_sortField");
		if(StringUtils.isNotEmpty(_sortField)){
			sql.append(" order by ");
			sql.append(_sortField);
			sql.append(" ");
			String _sort=(String)filter.get("_sort");
			sql.append(_sort);
		}
		return dao.paginate(pageNo,pageSize,"SELECT sal.*,u.uname AS uname ",sql.toString(), parame.toArray());
	}
}
