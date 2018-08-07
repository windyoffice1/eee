package net.loyin.model.wf;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import net.loyin.jfinal.anatation.TableBind;
/**
 * 
 * @author:lizhangyou
 * @Description: 任务实体类</p>* 
 * @date 2017-9-25
 */
@TableBind(name="wf_task")
public class TaskTable extends Model<TaskTable> {
	private static final long serialVersionUID = 8739554734071036012L;
	public static final String tableName="wf_task";
	public static TaskTable dao=new TaskTable();
	
	//设置操作人
	public void setOperator(String taskid,String operator){
		String sql="update wf_task set operator=? where id=?";
		Db.update(sql,operator,taskid);
	}
	
}
