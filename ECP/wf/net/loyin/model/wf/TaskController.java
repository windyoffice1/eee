/*package net.loyin.model.wf;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snaker.engine.access.Page;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.model.TaskModel.TaskType;

import com.jfinal.plugin.activerecord.Model;
import com.snakerflow.framework.utils.SnakerEngineFacets;


*//**
 * Snaker流程引擎常用Controller
 * @author yuqs
 * @since 0.1
 *//*
public class TaskController {
	private static final Logger log = LoggerFactory.getLogger(TaskController.class);

	private SnakerEngineFacets facets=new SnakerEngineFacets();
	
	public String homeTaskList(Model model) {
		List<String> list = ShiroUtils.getGroups();
		list.add(ShiroUtils.getUsername());
		log.info(list.toString());
		String[] assignees = new String[list.size()];
		list.toArray(assignees);
		
		Page<WorkItem> majorPage = new Page<WorkItem>(5);
		Page<WorkItem> aidantPage = new Page<WorkItem>(3);
		Page<HistoryOrder> ccorderPage = new Page<HistoryOrder>(3);
		List<WorkItem> majorWorks = facets.getEngine()
				.query()
				.getWorkItems(majorPage, new QueryFilter()
				.setOperators(assignees)
				.setTaskType(TaskType.Major.ordinal()));
		List<WorkItem> aidantWorks = facets.getEngine()
				.query()
				.getWorkItems(aidantPage, new QueryFilter()
				.setOperators(assignees)
				.setTaskType(TaskType.Aidant.ordinal()));
		List<HistoryOrder> ccWorks = facets.getEngine()
				.query()
				.getCCWorks(ccorderPage, new QueryFilter()
				.setOperators(assignees)
				.setState(1));
		
		model.addAttribute("majorWorks", majorWorks);
		model.addAttribute("majorTotal", majorPage.getTotalCount());
		model.addAttribute("aidantWorks", aidantWorks);
		model.addAttribute("aidantTotal", aidantPage.getTotalCount());
		model.addAttribute("ccorderWorks", ccWorks);
		model.addAttribute("ccorderTotal", ccorderPage.getTotalCount());
		return "snaker/activeTask";
	}
	
	
}
*/