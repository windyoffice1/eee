package net.loyin.ctrl.basicdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

import net.loyin.ctrl.AdminBaseController;
import net.loyin.jfinal.anatation.PowerBind;
import net.loyin.jfinal.anatation.RouteBind;
import net.loyin.model.basicdata.MaterialBroad;
import net.loyin.model.basicdata.MaterialData;
import net.loyin.model.basicdata.Parame;
import net.loyin.model.basicdata.Warehouselocation;
import net.loyin.model.inventorymanager.Inventory;
import net.loyin.test.ReadExcel;

import org.apache.commons.lang3.StringUtils;

import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

@RouteBind(path="testCtrl",sys="基础资料",model="物料编码")
public class TestCtrl extends AdminBaseController<MaterialBroad>{

	public TestCtrl(){
		this.modelClass=MaterialBroad.class;
	}
	
	public List<MaterialBroad> dataGrid() {
		Map<String,Object> filter=new HashMap<String,Object>();
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		String material_name=this.getPara("material_name");
		filter.put("material_name",material_name);
		String material_no=this.getPara("material_no");
		filter.put("material_no",material_no);
		filter.put("type",this.getPara("type"));
		this.sortField(filter);
		Page<MaterialBroad> page = MaterialBroad.dao.pageGrid(getPageNo(), 100000,filter);
		return page.getList();
	}
	
	public List<Record> Parame() {
		List<Record> list=Parame.dao.qryList(this.getCompanyId(),this.getParaToInt("type"));
		List<Record> list_temp=new ArrayList<Record>();
		if(list!=null&&list.isEmpty()==false){
			dotree(null,list,list_temp,0,true);
		}
		Page<Record> page=new Page<Record>(list_temp,1,0,0,(list==null||list.isEmpty())?0:list.size());
		return page.getList();
	}
	
	
	public List<Warehouselocation>  WarehouselocationList() {
		Map<String,Object> filter=new HashMap<String,Object>();
		filter.put("admin",this.getPara("admin"));
		filter.put("warehouse_name",this.getPara("warehouse_name"));
		this.sortField(filter);
		Page<Warehouselocation> page = Warehouselocation.dao.pageGrid(getPageNo(), 1000000,filter);
		return page.getList();
	}

	public List<MaterialData> MaterialDataList() {
		Map<String,Object> filter=new HashMap<String,Object>();
		Map<String,String> userMap=this.getUserMap();
		filter.put("company_id",userMap.get("company_id"));
		String matarial_name=this.getPara("matarial_name");
		filter.put("matarial_name",matarial_name);
		filter.put("material_no",this.getPara("material_no"));
		this.sortField(filter);
		Page<MaterialData> page = MaterialData.dao.pageGrid(getPageNo(), 100000,filter);
		return page.getList();
	}
	
	
    // 去读Excel的方法readExcel，该方法的入口参数为一个File对象
	public void readExcel() {
        try {
        	//大类
        	List<MaterialBroad>  materialBroadList = this.dataGrid();
/*        	for(MaterialBroad broad : materialBroadList){
        		System.out.println(broad.toJson());
        	}*/
        	//单位
        	List<Record> parame = this.Parame();
/*        	//仓位
        	List<Warehouselocation> WarehouselocationList= this.WarehouselocationList();*/
            // 此处为我创建Excel路径：E:/zhanhj/studysrc/jxl下
            File file = new File("D:/readExcel.xls");
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            Sheet sheet = wb.getSheet(0);
            // sheet.getRows()返回该页的总行数
            for (int i = 2; i < sheet.getRows(); i++) {
            	MaterialData materialData = new MaterialData();
            	materialData.set("id", UUID.randomUUID().toString());
                //浮动价格比例是0.3
                materialData.set("float_rate", 0.3);
                materialData.set("company_id", "0001");
                materialData.set("keeper", "eb5c8551-793f-4d0e-92f9-a090493b7237");
                // sheet.getColumns()返回该页的总列数
                for (int j = 0; j < sheet.getColumns(); j++) {
                    String cellinfo = sheet.getCell(j, i).getContents();
                    if(cellinfo.isEmpty()){
                        continue;
                    }
                    //大类编号
                    if(j==0){
                    	for(MaterialBroad broad : materialBroadList){
                    		if(broad.get("material_no").equals(cellinfo)){
                    			materialData.set("belong_to_broad_id", broad.get("id"));
                    		}
                    	}
                    }else if(j==1){
                    	materialData.set("material_no", cellinfo);
                    }else if(j==2){
                    	materialData.set("material_name", cellinfo);
                    }else if(j==3){
                    	materialData.set("model_number", cellinfo);
                    }else if(j==4){
                    	materialData.set("average_price", new BigDecimal(cellinfo.replaceAll(",", "")));
                    	materialData.set("target_price", new BigDecimal(cellinfo.replaceAll(",", "")));
                    }else if(j==14){
                    	for(Record record : parame){
                    		if(record.get("name").equals(cellinfo)){
                    			materialData.set("unit", record.get("id"));
                    		}
                    	}
                    }
                }
                materialData.save();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
    
    // 去读Excel的方法readExcel，该方法的入口参数为一个File对象
	public void readExcel01() {
        try {
            // 此处为我创建Excel路径：E:/zhanhj/studysrc/jxl下
            File file = new File("D:/readExcel.xls");
        	//仓位名称
        	Set<String> cSet=new HashSet<String>();
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            // Excel的页签数量
            int sheet_size = wb.getNumberOfSheets();
            for (int index = 0; index < sheet_size; index++) {
                // 每个页签创建一个Sheet对象
                Sheet sheet = wb.getSheet(index);
                // sheet.getRows()返回该页的总行数
                for (int i = 2; i < sheet.getRows(); i++) {
                    // sheet.getColumns()返回该页的总列数
                    for (int j = 0; j < sheet.getColumns(); j++) {
                        String cellinfo = sheet.getCell(j, i).getContents();
                        if(cellinfo.isEmpty()){
                            continue;
                        }
                        if(j==1){
                        	cSet.add(cellinfo.substring(0,3));
                        }
                    }
                }
            }
            for(String set:cSet){
            	Warehouselocation warehouselocation=new Warehouselocation();
            	warehouselocation.set("id", UUID.randomUUID().toString());
            	warehouselocation.set("warehouse_name", set);
            	warehouselocation.set("id", UUID.randomUUID().toString());
            	warehouselocation.set("admin_id", "e2fcfcd2-f319-4ee6-bc63-c5d66d451fc5");
            	warehouselocation.save();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
    // 去读Excel的方法readExcel，该方法的入口参数为一个File对象
	public void readExcel02() {
        try {
        	//仓位
        	List<Warehouselocation> warehouselocationList = this.WarehouselocationList();
        	List<MaterialData> MaterialDataList =  MaterialDataList();
            // 此处为我创建Excel路径：E:/zhanhj/studysrc/jxl下
            File file = new File("D:/readExcel.xls");
            // 创建输入流，读取Excel
            InputStream is = new FileInputStream(file.getAbsolutePath());
            // jxl提供的Workbook类
            Workbook wb = Workbook.getWorkbook(is);
            // Excel的页签数量
            int sheet_size = wb.getNumberOfSheets();
            for (int index = 0; index < sheet_size; index++) {
                // 每个页签创建一个Sheet对象
                Sheet sheet = wb.getSheet(index);
                // sheet.getRows()返回该页的总行数
                for (int i = 2; i < sheet.getRows(); i++) {
                	Inventory inventory= new Inventory();
                	inventory.set("id", UUID.randomUUID().toString());
                	inventory.set("company_id", "0001");
                    // sheet.getColumns()返回该页的总列数
                    for (int j = 0; j < sheet.getColumns(); j++) {
                        String cellinfo = sheet.getCell(j,i).getContents();
                        if(cellinfo.isEmpty()){
                            continue;
                        }
                        if(j==1){
                        	for(Warehouselocation Warehouselocation:warehouselocationList){
                        		if(Warehouselocation.get("warehouse_name").equals(cellinfo.substring(0,3))){
                        			inventory.set("warehouse_id",Warehouselocation.get("id") );
                        		}
                        	}
                        	for(MaterialData materialData:MaterialDataList){
                        		if(materialData.get("material_no").equals(cellinfo)){
                        			inventory.set("material_data_id",materialData.get("id") );
                        		}
                        	}
                        }else if(j==12){
                        	inventory.set("existing_amount",cellinfo);
                        }
                    }
                    inventory.save();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	
}
