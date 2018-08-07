var url=rootPath+"/reportforms/finance",gridQryUrl=url+"/doMonthlyStatement.json",custParame=SYSTEM.custParame,typeList=custParame.typeList;
var model = avalon.define({$id:'view',user:SYSTEM.user,parameList:typeList,
    query :{year:"",month:"",material_data_no:""},
    list:[],
    isMonthlyState:"",
    begin_total_amount:0,begin_total_money:0.00,putinstorage_total_amount:0,putinstorage_total_money:0.00,
    outputstorage_total_amount:0,outputstorage_total_money:0.00,end_total_amount:0,end_total_money:0.00,
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
    	model.query.month=nowMonth+1;
    },
    
    list:[],nprint:true,total_money:0.00,
    productList:[],
	init:function(){
		model.setYear();
		model.setMonth();
		model.loadData();
		model.addEvent();
	},
	resetQry:function(){
/*		var date=new Date();
    	var nowYear=date.getFullYear();
    	var nowMonth=date.getMonth();
    	model.query.year=nowYear;
    	model.query.nowMonth=nowMonth;
    	model.query.material_data_no='';
		model.query={year:nowYear,month:nowMonth,material_data_no:''};*/
		model.setYear();
		model.setMonth();
		model.query.material_data_no='';
		model.loadData();
	},
	//导出
	exportExcel:function(){
		$(".table").table2excel({
			exclude: ".noExl",
			name: "Excel Document Name",
			filename: "月结表.xls",
			fileext: "",
			exclude_img: true,
			exclude_links: true,
			exclude_inputs: true
		});
	},
	sava:function(){
		var is = window.confirm("确认月结之后将不能改变，是否确认月结？");
		if(is){
			Public.ajaxPost(url+"/save.json", {year:model.query.year,month:model.query.month}, function(t) {
				if (t && 200 == t.status) {
					parent.Public.tips({type:2,content:t.msg});
					model.loadData();
				} else{
					parent.Public.tips({type:1,content:"月结失败！" + t.msg});
				}
			});
		}
		
	},
	loadData:function(){
		Public.ajaxPost(gridQryUrl,model.query.$model, function(json){
			if(json.status==200){
				model.isMonthlyState=json.msg;
				model.begin_total_amount=0;
				model.begin_total_money=0.00;
				model.putinstorage_total_amount=0;
				model.putinstorage_total_money=0.00;
				model.outputstorage_total_amount=0;
				model.outputstorage_total_money=0.00;
				model.end_total_amount=0;
				model.end_total_money=0.00;
				if(json.data.length>0){
					for(var i=0;i<json.data.length;i++){
						var a=json.data[i];
						model.begin_total_amount=model.begin_total_amount+a.begin_amount;
						if(a.begin_money){
							model.begin_total_money=model.begin_total_money+parseFloat(a.begin_money);
						}
						model.putinstorage_total_amount=model.putinstorage_total_amount+a.putinstorage_amount;
						if(a.putinstorage_money){
							model.putinstorage_total_money=model.putinstorage_total_money+parseFloat(a.putinstorage_money);
						}
						if(a.outputstorage_money){
							model.outputstorage_total_money=model.outputstorage_total_money+parseFloat(a.outputstorage_money);
						}
						model.outputstorage_total_amount=model.outputstorage_total_amount+a.outputstorage_amount;
						model.end_total_amount=model.end_total_amount+a.end_amount;
						if(a.end_money){
							model.end_total_money=model.end_total_money+parseFloat(a.end_money);
						}
					}
					model.begin_total_money = model.begin_total_money.toFixed(2);
					model.putinstorage_total_money = model.putinstorage_total_money.toFixed(2);
					model.outputstorage_total_money = model.outputstorage_total_money.toFixed(2);
					model.end_total_money = model.end_total_money.toFixed(2);
				}
				model.list=json.data;
			}
		});
	},
	addEvent:function() {
					
	},
	printRpt:function(){
		model.nprint=false;
		window.print();
		model.nprint=true;
	},
});
model.init();