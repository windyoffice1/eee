var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,type=parent.type,
s=["","","通过","拒绝"];
var model = avalon.define({
	$id:'ctrl',
	data:{id:"",check_name:"",warehouse_id:"",check_date:"",operate_user_id:"",operate_user_name:"",company_id:"",status:"",warehouse_name:"",deviation_total_money:0,
		productlistlength:1,order_amt:0,
		productlist:[{id:"",inventory_check_id:"",material_data_id: "",amount:0,target_price:0,total_money:0,comment:"",unit:"",material_data_name: "",unit:"",
			material_data_no:"",model_number:"",original_amount:0,now_amount:0,deviation_amount:0,deviation_total_money:0.00,comment:""}]},
	original_total_amount:0,
	now_total_amount:0,
	total_amount:0,
	deviation_total_money:0,
	init:function(){
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/inventorymanager/check/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					if(json.data!=null&&json.data!=''&&json.data!=undefined&&json.data!='undefined'){
						if(json.data.status=='0'){
							json.data.status='盘亏';
						}else if(json.data.status=='1'){
							json.data.status='盘盈';
						}else if(json.data.status=='2'){
							json.data.status='持平';
						}
					}
					avalon.mix(model.data,json.data);
					jisuan();
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
	}
});
function jisuan(){//计算合计 注意：对于数组不能使用$watch 因为只能监听length。
	model.original_total_amount=0;
	model.now_total_amount=0;
	model.total_amount=0;
	model.deviation_total_money=0.00;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.original_total_amount=new Number(model.original_total_amount)+new Number(el.original_amount);
		model.now_total_amount=new Number(model.now_total_amount)+new Number(el.now_amount);
		model.total_amount=new Number(model.total_amount)+new Number(el.deviation_amount);
		model.deviation_total_money=new Number(model.deviation_total_money)+new Number(el.deviation_total_money);
	}
	model.data.order_amt=model.deviation_total_money;
	model.data.deviation_total_money=model.deviation_total_money;
	model.data.total_amount=model.total_amount;
};
model.init();