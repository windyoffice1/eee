var url=rootPath+"/reportforms/finance",gridQryUrl=url+"/putinAndOutputMaterialDetial.json",custParame=SYSTEM.custParame,typeList=custParame.typeList;
var model = avalon.define({$id:'view',user:SYSTEM.user,parameList:typeList,
    query :{year:"",month:"",material_broad_id:"",type:"",material_data_no:"",child_warehouse_id:"",material_broad_name:"",},
    begin_total_amount:0,
    begin_total_money:0.00,
    end_total_amount:0,
    end_total_money:0.00,
    total_money:0.00,
    year:[],
    setYear:function(){
    	var date=new Date();
    	var nowYear=date.getFullYear();
    	for(var beginYear=2017;beginYear<=nowYear;beginYear++){
    		model.year.push(beginYear);
    	}
    	model.query.year=nowYear;
    },
    month:[],
    setMonth:function(){
    	var date=new Date();
    	var nowYear=date.getFullYear();
    	var nowMonth=date.getMonth();
    	//清除
    	model.month.splice(0,model.month.length);
    	if(model.query.year==nowYear){
    		for(var beginMonth=1;beginMonth<=nowMonth+1;beginMonth++){
    			model.month.push(beginMonth);
    		}
    	}else{
    		for(var beginMonth=1;beginMonth<=12;beginMonth++){
    			model.month.push(beginMonth);
    		}
    	}
    	model.query.month=nowMonth+1;
    },
    childwarehouseList:[],
    qryChildWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json",{child_warehouse_name:v,type:0,rows:99999,_sortField:"child_warehouse_name",_sort:"asc"},function(json){
    		model.childwarehouseList=json.data.list;
    	});
    },
    materialBroadList:[],materialBroadComboV:false,
    chooseMaterialBroad:function(e){
    	model.query.material_broad_id=e.id;
    	model.query.material_broad_name=e.material_name;
    	model.materialBroadComboV=false;
    	model.loadData();
    },
    chooseALLMaterialBroad:function(e){
    	model.query.material_broad_id=null;
    	model.query.material_broad_name=null;
    	model.materialBroadComboV=false;
    	model.loadData();
    },
    qryMatrialBroad:function(v){//自动完成查询物料大类
    	model.materialBroadComboV=true;
    	model.query.matrial_broad_id='';
    	Public.ajaxPost(rootPath+"/basicdata/materialbroad/dataGrid.json",{keyword:v,_sortField:"material_no",rows:9999,_sort:"asc"},function(json){
    		model.materialBroadList=json.data.list;
    	});
    },
    
    list:[],nprint:true,total_money:0.00,
    productList:[],
	init:function(){
		model.setYear();
		model.setMonth();
		model.loadData();
		model.addEvent();
		model.qryChildWarehouseList();
	},
	//导出
	excelExport:function(){
		$(".table").table2excel({
			exclude: ".noExl",
			name: "Excel Document Name",
			filename: "收发明细表.xls",
			fileext: "",
			exclude_img: true,
			exclude_links: true,
			exclude_inputs: true
		});
	},
	resetQry:function(){
		var date=new Date();
    	var nowYear=date.getFullYear();
    	var nowMonth=date.getMonth();
		model.query={year:nowYear,month:nowMonth+1,material_broad_id:"",material_broad_name:"",type:"",material_data_no:"",child_warehouse_id:"",};
		model.loadData();
	},
	loadData:function(){
		Public.ajaxPost(gridQryUrl,model.query.$model, function(json){
			if(json.status==200){
				model.total_money=0;
				model.begin_total_amount=0;
				model.begin_total_money=0.00;
				model.end_total_amount=0;
				model.end_total_money=0.00;
				if(json.data.length>0){
					for(var i=0;i<json.data.length;i++){
						var a=json.data[i];
						if(a.total_money){
							model.total_money=model.total_money+a.total_money;
						}
						model.begin_total_amount=model.begin_total_amount+a.begin_amount;
						model.begin_total_money=model.begin_total_money+a.begin_money;
						model.end_total_amount=model.end_total_amount+a.end_amount;
						model.end_total_money=model.end_total_money+a.end_money;
					}
				}
				model.list=json.data;
			}
		});
	},
	addEvent:function() {
		$("#mothlyStatement").click(function(){
			$.dialog.confirm("确认月结之后将不能改变，是否确认月结？", function() {
				material_length=model.list.length;
				material_list=model.list.$model;
				console.info(material_list);
				Public.ajaxPost(url+"/save.json", {"list":material_list,"material_length":material_length}, function(t) {
					if (t && 200 == t.status) {
						parent.Public.tips({type:2,content:t.msg});
						model.reloadData();
					} else{
						parent.Public.tips({type:1,content:"月结失败！" + t.msg});
					}
				});
			});
		});
	},
	printRpt:function(){
		model.nprint=false;
		window.print();
		model.nprint=true;
	}
});
model.init();