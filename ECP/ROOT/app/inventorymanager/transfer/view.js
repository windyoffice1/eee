var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,type=parent.type,
s=["","","通过","拒绝"];
var model = avalon.define({
	$id:'ctrl',
	data:{id:"",from_warehouse_id:"",from_warehouse_name:"",target_warehouse_id:"",target_warehouse_name:"",transfer_total_money:"",operate_user_id:"",operate_user_name:"",company_id:"",
		operate_date:"",update_user_name:"",update_date:"",
		productlistlength:1,order_amt:0,
		productlist:[{id:"",inventory_transfer_id:"",material_data_id: "",amount:0,target_price:0,total_money:0,comment:"",unit:"",material_data_name: "",unit:"",
					material_data_no:"",model_number:""}]},
		total_amount:0,
		total_money:0,
	init:function(){
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/inventorymanager/transfer/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					console.info(model.data);
					avalon.mix(model.data,json.data);
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