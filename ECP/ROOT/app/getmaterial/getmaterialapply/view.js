var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,type=parent.type;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:type,audit_hidden:parent.audit_hidden,
	data:{id:"",getmaterial_no:"",getmaterial_name:"",create_time:"",getmaterial_user_id:"",getmaterial_user_name:"",approve_status:"",company_id:"",create_user_id:"",create_user_name:"",
		update_user_id:"",update_user_name:"", child_warehouse_name:"",type:"",
		process_id:"",getmaterial_date:SYSTEM.date,sign_date:SYSTEM.date,warehouse_id:"",warehouse_name:"",update_time:"",total_money:0,total_amount:0,
		productlistlength:1,order_amt:0,comment:"",
		productlist:[{id:"",getmaterial_apply_id:"",material_data_id: "",purchase_price:0,amount:0,total_money:0,comment:"",unit:"",material_data_name: "",
					material_data_no:"",model_number:"",existing_amount:0}]},
		total_amount:0,
		total_money:0,
	userList:[],
	init:function(){
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/getmaterial/getmaterialapply/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					avalon.mix(model.data,json.data);
					console.error(model.data);
					jisuan();
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
	}
});
avalon.filters.audit_status=function(v){
	return s[v];
};
function jisuan(v){//计算合计 注意：对于数组不能使用$watch 因为只能监听length。
	model.total_amount=0;
	model.total_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_amount=new Number(model.total_amount)+new Number(el.amount);
		model.total_money=new Number(model.total_money)+new Number(el.total_money);
	}
	model.data.order_amt=model.total_money;
	model.data.total_money=model.total_money;
	model.data.total_amount=model.total_amount;
};
model.init();