var url=rootPath+"/reportforms/purchase",gridQryUrl=url+"/purchaseReportFormsDetial.json",custParame=SYSTEM.custParame,typeList=custParame.typeList;
var model = avalon.define({$id:'view',user:SYSTEM.user,parameList:typeList,
    query :{start_date:'',end_date:'',material_type_id:"",customer_id:'',customer_name:'',material_data_no:'',material_data_name:'',is_all:0,
    	material_broad_id:"",material_broad_name:"",},
    list:[],nprint:true,total_money:0.00,
    productList:[],
    customerList:[],custComboV:false,
	chooseCust:function(e){
    	model.query.customer_id=e.id;
    	model.query.customer_name=e.name;
    	model.custComboV=false;
    	model.loadData();
    },
    qryCustomer:function(v){//自动完成查询客户
    	model.custComboV=true;
    	model.query.customer_id='';
    	Public.ajaxPost(rootPath+"/crm/customer/dataGrid.json",{keyword:v,_sortField:"name",rows:99999,_sort:"asc"},function(json){
    		model.customerList=json.data.list;
    		console.info(json.data.list);
    	});
    },
    materialBroadList:[],materialBroadComboV:false,
    chooseMaterialBroad:function(e){
    	model.query.material_broad_id=e.id;
    	model.query.material_broad_name=e.material_name;
    	model.materialBroadComboV=false;
    	model.loadData();
    },
    qryMatrialBroad:function(v){//自动完成查询物料大类
    	model.materialBroadComboV=true;
    	model.query.matrial_broad_id='';
    	Public.ajaxPost(rootPath+"/basicdata/materialbroad/dataGrid.json",{keyword:v,_sortField:"material_no",rows:99999,_sort:"asc"},function(json){
    		model.materialBroadList=json.data.list;
    	});
    },
	init:function(){
		$(".ui-datepicker-input").datepicker();
		var myDate = new Date();
		var year=myDate.getFullYear();    //获取完整的年份(4位,1970-????)
		var month=myDate.getMonth()+1;       //获取当前月份(0-11,0代表1月)
		var date=year+'-'+month+'-'+'01';
		$('#beginDate').val(date);
		$('#endDate').val(SYSTEM.endDate);
		model.loadData();
	},
	queryAll:function(){
		if(model.query.is_all==0){
			$('#is_all').css('color','red');
			model.query={start_date:'',end_date:'',material_type_id:"",customer_id:'',customer_name:'',material_data_no:'',material_data_name:'',
					matrial_broad_id:'',matrial_broad_name:'',is_all:1,};
		}else{
			var myDate = new Date();
			var year=myDate.getFullYear();    //获取完整的年份(4位,1970-????)
			var month=myDate.getMonth()+1;       //获取当前月份(0-11,0代表1月)
			var date=year+'-'+month+'-'+'01';
			$('#is_all').css('color','black');
			model.query={start_date:date,end_date:SYSTEM.endDate,material_type_id:"",customer_id:'',customer_name:'',material_data_no:'',material_data_name:'',
			    	matrial_broad_id:"",matrial_broad_name:"",is_all:0,};
		}
		model.loadData();
	},
	resetQry:function(){
		var myDate = new Date();
		var year=myDate.getFullYear();    //获取完整的年份(4位,1970-????)
		var month=myDate.getMonth()+1;       //获取当前月份(0-11,0代表1月)
		var date=year+'-'+month+'-'+'01';
		model.query={start_date:date,end_date:SYSTEM.endDate,material_type_id:"",customer_id:'',customer_name:'',material_data_no:'',material_data_name:'',
		    	matrial_broad_id:"",matrial_broad_name:"",is_all:0,};
		model.loadData();
	},
	is_all:function(){
		
	},
	//导出
	exportExcel:function(){
		$(".table").table2excel({
			exclude: ".noExl",
			name: "Excel Document Name",
			filename: "采购明细表.xls",
			fileext: "",
			exclude_img: true,
			exclude_links: true,
			exclude_inputs: true
		});
	},
	loadData:function(){
		model.query.start_date=$('#beginDate').val();
		model.query.end_date=$('#endDate').val();
		Public.ajaxPost(gridQryUrl,model.query.$model, function(json){
			if(json.status==200){
				model.total_money=0;
				if(json.data.length>0){
					for(var i=0;i<json.data.length;i++){
						var a=json.data[i];
						json.data[i].total_money=a.amount*a.target_price;
						model.total_money+=a.amount*a.target_price;
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