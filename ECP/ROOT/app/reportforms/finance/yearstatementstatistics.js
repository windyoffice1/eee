var url=rootPath+"/reportforms/finance",gridQryUrl=url+"/yearStatementStatistics.json",custParame=SYSTEM.custParame,typeList=custParame.typeList;
var model = avalon.define({$id:'view',user:SYSTEM.user,parameList:typeList,
    query :{year:"",begin_month:"",end_month:"",material_data_no:"",type:"",child_warehouse_id:"",},
    isChildWarehouseNoData:"",
    begin_total_money:0.00,putinstorage_total_money:0.00,
    outputstorage_total_money:0.00,end_total_money:0.00,
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
    	if(nowMonth==0){
        	model.query.begin_month=12;
        	model.query.end_month=12;
    	}else{
    		model.query.begin_month=nowMonth;
    		model.query.end_month=nowMonth;
    	}
    },
    childwarehouseList:[],
    qryChildWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json",{child_warehouse_name:v,type:0,_sortField:"child_warehouse_name",_sort:"asc"},function(json){
    		model.childwarehouseList=json.data.list;
    	});
    },
    list:[],nprint:true,
    productList:[],
	init:function(){
		model.query.type=0;
		$("#type0").prop("checked","checked");
		model.setYear();
		model.setMonth();
		model.qryChildWarehouseList();
		model.addEvent();
		model.loadData();
	},
	resetQry:function(){
		var date=new Date();
    	var nowYear=date.getFullYear();
    	var nowMonth=date.getMonth();
		model.query={year:nowYear,begin_month:nowMonth,end_month:nowMonth,material_data_no:'',type:0};
		$("#type0").prop("checked","checked");
		$("#type1").prop("checked",false);
		model.loadData();
	},
	//导出
	exportExcel:function(){
		if(model.query.type=='0'){
			$(".exportTable0").table2excel({
				exclude: ".noExl",
				name: "Excel Document Name",
				filename: "年结统计表.xls",
				fileext: "",
				exclude_img: true,
				exclude_links: true,
				exclude_inputs: true
			});
		}else if(model.query.type=='1'){
			$(".exportTable1").table2excel({
				exclude: ".noExl",
				name: "Excel Document Name",
				filename: "年结统计表.xls",
				fileext: "",
				exclude_img: true,
				exclude_links: true,
				exclude_inputs: true
			});
		}
	},
	loadData:function(){
		if($('#begin_month').val()!='{{el}}'){
			model.query.begin_month = $('#begin_month').val();
		}
		if($('#end_month').val()!='{{el}}'){
			model.query.end_month = $('#end_month').val();
		}
		Public.ajaxPost(gridQryUrl,model.query.$model, function(json){
			if(json.status==200){
				if(model.query.type=='0'){
					$('#warehousequery').remove();
					model.isChildWarehouseNoData=0;
					model.begin_total_money=0.00;
					model.putinstorage_total_money=0.00;
					model.outputstorage_total_money=0.00;
					model.end_total_money=0.00;
					if(json.data.length>0){
						for(var i=0;i<json.data.length;i++){
							var a=json.data[i];
							model.begin_total_money=model.begin_total_money+a.begin_money;
							model.putinstorage_total_money=model.putinstorage_total_money+a.putinstorage_money;
							model.outputstorage_total_money=model.outputstorage_total_money+a.outputstorage_money;
							model.end_total_money=model.end_total_money+a.end_money;
						}
					}
					model.list=json.data;
				}else if(model.query.type=='1'){
					$('#warehousequery').remove();
					model.list=[];
					var data=json.data;
					if(data.length==0){
						model.isChildWarehouseNoData=1;
					}else{
						model.isChildWarehouseNoData=0;
						var html="<div class=bills id=warehousequery>";
						html+="<p style='font-size:24px;text-align:center;'><span>年结统计表</span></p>";
						html+="	<table class='table table-bordered table-striped able-hover exportTable1'><thead><tr>";
						html+="<th>部门名称</th>";
						//设置表头
						for(var i=0;i<typeList[21].length;i++){
							html+="<th>"+typeList[21][i].name+"（元）</th>";
						}
						html+="<th>合计（元）</th></tr></thead>";
						html+="<tbody><tr>";
						for(var i=0;i<data.length;i++){
							var total_money=0.00;
							html+="<tr><th>"+data[i].child_material_name+"</th>";
							for(var j=0;j<typeList[21].length;j++){
								var isExist=0;
								for (var p1 in data[i].typeAndMoney) {
									if(typeList[21][j].name==p1){
										html+="<td>"+data[i].typeAndMoney[p1]+"</td>";
										total_money+=data[i].typeAndMoney[p1];
										isExist=1;
									}
								}
								if(isExist==0){
									html+="<td>"+0.00+"</td>";
								}
							}
							html+="<td>"+total_money+"</td></tr>";
	 					}
						html+="</tr></tbody>";
						html+="</table></div>";
						$("#view").append(html);
					}
				}
			}
		});
	},
	addEvent:function() {
		$('input[type=checkbox]').click(function(){
			 $(this).attr('checked','checked').siblings().removeAttr('checked');
			 model.query.type=$(this).val();
			 model.loadData();
		});
	},
	printRpt:function(){
		model.nprint=false;
		window.print();
		model.nprint=true;
	}
});
model.init();