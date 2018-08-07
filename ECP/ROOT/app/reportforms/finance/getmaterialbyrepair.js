var url=rootPath+"/reportforms/finance",gridQryUrl=url+"/getMaterialForRepair.json",custParame=SYSTEM.custParame,typeList=custParame.typeList;
var model = avalon.define({$id:'view',user:SYSTEM.user,parameList:typeList,
    query :{begin_year:"",end_year:"",child_warehouse_id:""},
    total_money:"",
    year:[],
    setYear:function(){
    	var date=new Date();
    	var nowYear=date.getFullYear();
    	for(var beginYear=2017;beginYear<=nowYear;beginYear++){
    		model.year.push(beginYear);
    	}
    	model.query.begin_year=nowYear;
    	model.query.end_year=nowYear;
    },
    childwarehouseList:[],
    qryChildWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json",{child_warehouse_name:v,rows:99999,type:0,_sortField:"child_warehouse_name",_sort:"asc"},function(json){
    		model.childwarehouseList=json.data.list;
    	});
    },
    list:[],nprint:true,
    productList:[],
	init:function(){
		model.setYear();
		model.loadData();
		model.qryChildWarehouseList();
	},
	resetQry:function(){
		var date=new Date();
    	var nowYear=date.getFullYear();
		model.query={begin_year:nowYear,end_year:nowYear,child_warehouse_id:""};
		model.loadData();
	},
	//导出
	excelExport:function(){
		$(".table").table2excel({
			exclude: ".noExl",
			name: "Excel Document Name",
			filename: "年修领料表.xls",
			fileext: "",
			exclude_img: true,
			exclude_links: true,
			exclude_inputs: true
		});
	},
	loadData:function(){
		Public.ajaxPost(gridQryUrl,model.query.$model, function(json){
			if(json.status==200){
				model.total_money=0.00;
				if(json.data.length>0){
					for(var i=0;i<json.data.length;i++){
						var a=json.data[i];
						model.total_money=model.total_money+a.total_money;
					}
				}
				model.list=json.data;
			}
		});
	},
	printRpt:function(){
		model.nprint=false;
		window.print();
		model.nprint=true;
	}
});
model.init();