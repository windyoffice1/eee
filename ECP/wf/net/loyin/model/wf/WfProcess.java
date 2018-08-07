package net.loyin.model.wf;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.snaker.engine.entity.Process;
import org.snaker.engine.model.TaskModel;

import net.loyin.jfinal.anatation.TableBind;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;

import com.snakerflow.framework.utils.ConvertUtils;
import com.snakerflow.framework.utils.SnakerEngineFacets;

/**
 * 流程定义 
 * 增加company_id
 * @author 龙影
 */
@TableBind(name = "wf_process")
public class WfProcess extends Model<WfProcess> {
	private static final long serialVersionUID = -5081919728844431708L;
	public static final String tableName = "wf_process";
	public static WfProcess dao=new WfProcess();
	
    public static final String PARA_PROCESSID = "processId";
    public static final String PARA_ORDERID = "orderId";
    public static final String PARA_TASKID = "taskId";
    public static final String PARA_TASKNAME = "taskName";
    public static final String PARA_METHOD = "method";
    public static final String PARA_NEXTOPERATOR = "nextOperator";
    public static final String PARA_NODENAME = "nodeName";
    public static final String PARA_CCOPERATOR = "ccOperator";
   
	/**
	 * 分页查询部署的流程
	 * @param pageNumber
	 * @param pageSize
	 * @param filter
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public Page page(int pageNumber,int pageSize,Map<String,Object> filter){
		StringBuffer sql=new StringBuffer(" from ");
		sql.append(tableName);
		List<Object> parame=new ArrayList<Object>();
		if(filter.keySet().isEmpty()==false){
			sql.append(" where 1=1 ");
			if(StringUtils.isNotEmpty((String)filter.get("company_id"))){
				sql.append(" and company_id=?");
				parame.add(filter.get("company_id"));
			}
		}
		return dao.paginate(pageNumber, pageSize, "select * ",sql.toString(),parame.toArray());
	}
	/**
	 * 更新企业id
	 * @param id
	 * @param company_id
	 */
	public void upCompanyId(String id,String company_id){
		Db.update("update "+tableName+" set company_id=? where id=?",company_id,id);
	}

	
	
    @SuppressWarnings("unchecked")
	public String process(HttpServletRequest request,Map<String,Object> map) {
        Map<String, Object> params = new HashMap<String, Object>();
        Enumeration<String> paraNames = request.getParameterNames();
        while (paraNames.hasMoreElements()) {
            String element = paraNames.nextElement();
            int index = element.indexOf("_");
            String paraValue = request.getParameter(element);
            if(index == -1) {
                params.put(element, paraValue);
            } else {
                char type = element.charAt(0);
                String name = element.substring(index + 1);
                Object value = null;
                switch(type) {
                    case 'S':
                        value = paraValue;
                        break;
                    case 'I':
                        value = ConvertUtils.convertStringToObject(paraValue, Integer.class);
                        break;
                    case 'L':
                        value = ConvertUtils.convertStringToObject(paraValue, Long.class);
                        break;
                    case 'B':
                        value = ConvertUtils.convertStringToObject(paraValue, Boolean.class);
                        break;
                    case 'D':
                        value = ConvertUtils.convertStringToObject(paraValue, Date.class);
                        break;
                    case 'N':
                        value = ConvertUtils.convertStringToObject(paraValue, Double.class);
                        break;
                    default:
                        value = paraValue;
                        break;
                }
                params.put(name, value);
            }
        }
        String processId = (String) map.get(PARA_PROCESSID);
        String orderId = request.getParameter(PARA_ORDERID);
        String taskId = request.getParameter(PARA_TASKID);
        String nextOperator = request.getParameter(PARA_NEXTOPERATOR);
        SnakerEngineFacets facets=new SnakerEngineFacets();
        if (StringUtils.isEmpty(orderId) && StringUtils.isEmpty(taskId)) {
            facets.startAndExecute(processId, (String)map.get("username"), params);
        } else {
            String methodStr = request.getParameter(PARA_METHOD);
            int method;
            try {
                method = Integer.parseInt(methodStr);
            } catch(Exception e) {
                method = 0;
            }
            switch(method) {
                case 0://任务执行
                    facets.execute(taskId,  (String)map.get("username"), params);
                    break;
                case -1://驳回、任意跳转
                    facets.executeAndJump(taskId,  (String)map.get("username"), params, request.getParameter(PARA_NODENAME));
                    break;
                case 1://转办
                    if(StringUtils.isNotEmpty(nextOperator)) {
                        facets.transferMajor(taskId,  (String)map.get("username"), nextOperator.split(","));
                    }
                    break;
                case 2://协办
                    if(StringUtils.isNotEmpty(nextOperator)) {
                        facets.transferAidant(taskId,  (String)map.get("username"), nextOperator.split(","));
                    }
                    break;
                default:
                    facets.execute(taskId,  (String)map.get("username"), params);
                    break;
            }
        }
        String ccOperator = request.getParameter(PARA_CCOPERATOR);
        if(StringUtils.isNotEmpty(ccOperator)) {
            facets.getEngine().order().createCCOrder(orderId,  (String)map.get("username"), ccOperator.split(","));
        }
        return "redirect:/snaker/task/active";
    }
	
    /**
     * 节点信息以json格式返回
     * all页面以节点信息构造tab及加载iframe
     * 获取流程的所有任务
     */
    public Object node(String processId) {
    	SnakerEngineFacets facets=new SnakerEngineFacets();
        Process process = facets.getEngine().process().getProcessById(processId);
        List<TaskModel> models = process.getModel().getModels(TaskModel.class);
        List<TaskModel> viewModels = new ArrayList<TaskModel>();
        for(TaskModel model : models) {
            TaskModel viewModel = new TaskModel();
            viewModel.setName(model.getName());
            viewModel.setDisplayName(model.getDisplayName());
            viewModel.setForm(model.getForm());
            viewModels.add(viewModel);
        }
        return viewModels;
    }
	
}
