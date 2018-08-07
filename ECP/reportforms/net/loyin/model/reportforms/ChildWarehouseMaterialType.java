package net.loyin.model.reportforms;

import java.math.BigDecimal;
import java.util.Map;

public class ChildWarehouseMaterialType {
	private String child_material_name;//部门名称既船站名称
	private Map<String,BigDecimal> typeAndMoney;
	
	public String getChild_material_name() {
		return child_material_name;
	}
	public void setChild_material_name(String child_material_name) {
		this.child_material_name = child_material_name;
	}
	public Map<String, BigDecimal> getTypeAndMoney() {
		return typeAndMoney;
	}
	public void setTypeAndMoney(Map<String, BigDecimal> typeAndMoney) {
		this.typeAndMoney = typeAndMoney;
	}

	
}
