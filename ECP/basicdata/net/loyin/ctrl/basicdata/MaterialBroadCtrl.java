package net.loyin.ctrl.basicdata;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.plugin.activerecord.Page;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.MaterialBroad;

@RouteBind(path="materialbroad",sys="基础资料",model="物料编码")
public class MaterialBroadCtrl   extends AdminBaseController<MaterialBroad>{

	public MaterialBroadCtrl(){
		this.modelClass=MaterialBroad.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		String material_name=this.getPara("material_name");
		filter.put("material_name",material_name);
		String material_no=this.getPara("material_no");
		filter.put("material_no",material_no);
		filter.put("type",this.getPara("type"));
		this.sortField(filter);
		Page<MaterialBroad> page = MaterialBroad.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	
	
	@PowerBind(code="A8_6_V",funcName="查看")
	public void qryOp() {
		getId();
		Map<String, String> userMap = this.getUserMap();
		MaterialBroad materialBroad = MaterialBroad.dao.findByIdAndCompanyid(userMap.get("company_id"),id);
		Map<String,Object> data=new HashMap<String,Object>();
		if (materialBroad != null){
			data.put("materialBroad", materialBroad);
			this.rendJson(true,null, "",data);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	

	@SuppressWarnings("unused")
	@PowerBind(code="A8_6_E",funcName="编辑")
	public void save() {
		try {
			getId();
			MaterialBroad po = (MaterialBroad) getModel();
			Map<String,String> userMap=this.getUserMap();
			po.set("id",id);
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			SimpleDateFormat spdf= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (StringUtils.isEmpty(id)) {
				po.set("id", UUID.randomUUID().toString());
				po.set("modifier", userMap.get("uid"));
				po.set("company_id",userMap.get("company_id"));
				po.set("alter_time",spdf.format(new Date()));
				po.save();
				id=po.getStr("id");
			} else {
				po.set("modifier", userMap.get("uid"));
				po.set("alter_time",spdf.format(new Date()));
				po.update();
			}
			Map<String,String> d=new HashMap<String,String>();
			d.put("id",id);
			this.rendJson(true,null, "操作成功！",d);
		} catch (Exception e) {
			log.error("保存产品异常", e);
			this.rendJson(false,null, "保存数据异常！");
		}
	}
	
	@PowerBind(code="A8_6_E",funcName="编辑")
	public void del() {
		try {
			id = getId();
			MaterialBroad.dao.del(id);
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	
}
