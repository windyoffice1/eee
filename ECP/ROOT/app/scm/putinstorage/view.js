var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,type=parent.type;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:type,
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",putinstorage_name:'',since_purchase_apply_id:0,since_purchase_apply_name:"",create_time:"",create_user_id:"",create_user_name:"",putinstorage_status:"",comment:"",
			company_id:"",customer_name:"",putinstorage_date:"",
			customer_id:"",total_amount:0,total_money:0,order_amt:0,
			productlistlength:1,
			productlist:[{id:0,putinstorage_id: "",id: "",material_data_id: "",amount:0,total_money:0,model_number:"",unit:"",material_name: "",target_price:0,plan_putinstorage_total_money:0,
				material_no:"",purchase_price:0,unit:"",comment:""}]},
	total_amount:0,
	total_money:0,
	userList:[],
	init:function(){
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/scm/putinstorage/qryDetails.json",{id:id}, function(json){
				console.log(json);
				if(json.status==200){
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