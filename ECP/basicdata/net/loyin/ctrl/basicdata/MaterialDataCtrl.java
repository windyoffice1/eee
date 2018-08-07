package net.loyin.ctrl.basicdata;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.plugin.activerecord.Page;
import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.MaterialData;

@RouteBind(path="materialdata",sys="基础资料",model="物料基础数据")
public class MaterialDataCtrl  extends AdminBaseController<MaterialData>{

	public MaterialDataCtrl(){
		this.modelClass=MaterialData.class;
	}
	
	public void dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		String material_name=this.getPara("material_name");
		filter.put("material_name",material_name);
		filter.put("material_no",this.getPara("material_no"));
		this.sortField(filter);
		Page<MaterialData> page = MaterialData.dao.pageGrid(getPageNo(), getPageSize(),filter);
		this.rendJson(true,null, "", page);
	}
	@PowerBind(code="A8_2_V",funcName="查看")
	public void qryOp() {
		getId();
		MaterialData materialData = MaterialData.dao.findByIdAndCompanyid(this.getUserMap().get("company_id"),id);
		Map<String,Object> data=new HashMap<String,Object>();
		if (materialData != null){
			data.put("materialData", materialData);
			this.rendJson(true,null, "",data);
		}
		else
			this.rendJson(false,null, "记录不存在！");
	}
	
	@SuppressWarnings("unused")
	@PowerBind(code="A8_2_E",funcName="编辑")
	public void save() {
		try {
			getId();
			MaterialData po = (MaterialData) getModel();
			po.set("id",id);
			if (po == null) {
				this.rendJson(false,null, "提交数据错误！");
				return;
			}
			if (StringUtils.isEmpty(id)) {
				po.set("id", UUID.randomUUID().toString());
				Map<String,String> userMap=this.getUserMap();
				po.set("company_id",userMap.get("company_id"));
				po.set("average_price",po.get("target_price"));
				po.save();
				id=po.getStr("id");
			} else {
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

	@PowerBind(code="A8_2_E",funcName="编辑")
	public void del() {
		try {
			id = getId();
			MaterialData.dao.del(id);
			rendJson(true,null,"删除成功！",id);
		} catch (Exception e) {
			log.error("删除异常", e);
			rendJson(false,null,"删除失败！请检查是否被使用！");
		}
	}
	
	
}
