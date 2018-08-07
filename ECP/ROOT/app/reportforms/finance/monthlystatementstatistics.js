var url=rootPath+"/reportforms/finance",gridQryUrl=url+"/monthlyStatementStatistics.json",custParame=SYSTEM.custParame,typeList=custParame.typeList;
var model = avalon.define({$id:'view',user:SYSTEM.user,parameList:typeList,
    query :{year:"",begin_month:"",end_month:"",material_data_no:"",type:"",},
    isChildWarehouseNoData:"",
    begin_total_money:0.00,putinstorage_total_money:0.00,
    outputstorage_total_money:0.00,end_total_money:0.00,
    year:[],
    setYear:function(){
    	model.year.splice(0,model.year.length);
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
    	Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json",{child_warehouse_name:v,rows:999999,type:0,_sortField:"belong_to_broad_no",_sort:"asc"},function(json){
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
		model.setYear();
		model.setMonth();
		model.query.material_data_no='';
		model.query.type=0;
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
				filename: "月结统计表（按物料大类）.xls",
				fileext: "",
				exclude_img: true,
				exclude_links: true,
				exclude_inputs: true
			});
		}else if(model.query.type=='1'){
			$(".exportTable1").table2excel({
				exclude: ".noExl",
				name: "Excel Document Name",
				filename: "月结统计表（按船站）.xls",
				fileext: "",
				exclude_img: true,
				exclude_links: true,
				exclude_inputs: true
			});
		}
	},
	loadData:function(){
		if($('#month').val()!='{{el}}'){
			model.query.begin_month = $('#month').val();
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
					model.begin_total_money=model.begin_total_money.toFixed(2);
					model.putinstorage_total_money=model.putinstorage_total_money.toFixed(2);
					model.outputstorage_total_money=model.outputstorage_total_money.toFixed(2);
					model.end_total_money=model.end_total_money.toFixed(2);
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
						html+="<p style='font-size:24px;text-align:center;'><span>月结统计表</span></p>";
						html+="	<table class='table table-bordered table-striped able-hover exportTable1'><thead><tr>";
						html+="<th>部门名称</th>";
						//设置表头
						for(var i=0;i<typeList[21].length;i++){
							html+="<th>"+typeList[21][i].name+"（元）</th>";
						}
						html+="<th>合计（元）</th></tr></thead>";
						html+="<tbody><tr>";
						var all_total_money=0.00;
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
							all_total_money+=total_money;
	 					}
						html+="</tr></tbody>";
						html+="<tfoot><tr><td colspan='6' align='right'><b>合计(元)：</b></td>"
						html+="<td>"+all_total_money+"</td></tr></tfoot>";
						html+="</table></div>";
						$("#view").append(html);
					}

				}
			}
		});
/*		var results;
		var a = new Map();
		var obj ,value;
		for(var i in results){
			obj = results[i];
			if(a.get(obj.id)){
				//a.get(obj.id).push(obj)
				value = a.get(obj.id);
				value.push(obj);
				
				a.put(obj.id,value);
			}else{
				a.put(obj.id,obj);
			}
		}
		*/
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