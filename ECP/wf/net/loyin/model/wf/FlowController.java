package net.loyin.model.wf;

import org.snaker.engine.entity.Process;
import com.snakerflow.framework.utils.SnakerEngineFacets;
import org.snaker.engine.model.TaskModel;
import java.util.*;
/**
 * @author yuqs
 * @since 2.0
 */
public class FlowController {
    public static final String PARA_PROCESSID = "processId";
    public static final String PARA_ORDERID = "orderId";
    public static final String PARA_TASKID = "taskId";
    public static final String PARA_TASKNAME = "taskName";
    public static final String PARA_METHOD = "method";
    public static final String PARA_NEXTOPERATOR = "nextOperator";
    public static final String PARA_NODENAME = "nodeName";
    public static final String PARA_CCOPERATOR = "ccOperator";
    private SnakerEngineFacets facets=new SnakerEngineFacets();

    /**
     * 节点信息以json格式返回
     * all页面以节点信息构造tab及加载iframe
     */
    public Object node(String processId) {
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

/*    *//**
     * 审批环节的提交处理
     * 其中审批表可根据具体审批的业务进行定制，此处仅仅是举例
     *//*
    public String doApproval(Approval model) {
        model.setOperateTime(new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
        model.setOperator(ShiroUtils.getUsername());
        manager.save(model);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("result", model.getResult());
        facets.execute(model.getTaskId(), ShiroUtils.getUsername(), params);
        return "redirect:/snaker/task/active";
    }*/
}
