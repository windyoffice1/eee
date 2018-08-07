package net.loyin.ctrl.basicdata;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import com.jfinal.plugin.activerecord.Page;
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.Notice;

@RouteBind(path="notice",sys="基础资料",model="通知管理")
public class NoticeCtrl extends AdminBaseController<Notice> {
	public NoticeCtrl(){
		this.modelClass=Notice.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		
		filter.put("create_user_id",userMap.get(""));
		filter.put("title",this.getPara("title"));
		this.sortField(filter);
		Page<Notice> page = Notice.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}

	@PowerBind(code="A8_3_V",funcName="查看")
	public void qryOp() {
		getId();
		Notice notice = Notice.dao.findByIdAndCompanyid(this.getCompanyId(),id);
		Map<String,Object> data=new HashMap<String,Object>();
		//修改通知为已读
		Notice.dao.updataNoticeStatus(this.getCurrentUserId(),id);
		if (notice != null){
			data.put("notice", notice);
			this.rendJson(true,null, "",data);
		}else
			this.rendJson(false,null, "记录不存在！");
	}
	
	@PowerBind(code="A8_3_E",funcName="编辑")
	public void save(){
		try {
			getId();
			Notice notice=getModel();
			if (notice == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			if (StringUtils.isEmpty(id)) {
				notice.set("id", UUID.randomUUID().toString());
				Map<String,String> userMap=this.getUserMap();
				notice.set("company_id",userMap.get("company_id"));
				notice.set("create_user_id",this.getCurrentUserId());
				notice.set("create_time",dateTimeFormat.format(new Date()));
				notice.save();
				id=notice.getStr("id");
			} else {
				notice.update();
			}
			Notice.dao.pushNoticeToAll(notice.getStr("id"));
			Map<String,String> d=new HashMap<String,String>();
			d.put("id",id);
			this.rendJson(true,null, "新增通知成功！",d);
		} catch (Exception e) {
			log.error("新增通知异常", e);
			this.rendJson(false,null, "新增通知异常！");
			e.printStackTrace();
		}
	}
	
	@PowerBind(code="A8_3_E",funcName="编辑")
	public void del() {
		try {
			getId();
			Notice.dao.del(id,this.getCompanyId(),this.getCurrentUserId());
			rendJson(true,null, "删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！");
		}
	}
	
	
	public void getUnrealNotice(){
		List<Notice> list=Notice.dao.getUnrealNotice(this.getCurrentUserId(), "0");
		rendJson(true,null, "查询成功！",list);
	}
	
	
	public void getUserNotice(){
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("user_id",this.getCurrentUserId());
		this.sortField(filter);
		Page<Notice> page = Notice.dao.getUserNotice(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
}
