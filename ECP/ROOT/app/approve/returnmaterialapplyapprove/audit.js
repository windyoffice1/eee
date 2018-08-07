var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,type=parent.type,
s=["","","通过","拒绝"];
var model = avalon.define({
	$id:'ctrl',parameList:typeList,
	data:{id:"",returnmaterial_no:"",returnmaterial_name:"",create_time:"",returnmaterial_user_id:"",returnmaterial_user_name:"",approve_status:"",company_id:"",create_user_id:"",create_user_name:"",
		update_user_id:"",update_user_name:"",comment:"",child_warehouse_id:"",child_warehouse_name:"",
		process_id:"",returnmaterial_date:SYSTEM.date,sign_date:SYSTEM.date,update_time:"",total_money:0,total_amount:0,
		productlistlength:1,order_amt:0,
		productlist:[{id:"",returnmaterial_apply_id:"",material_data_id: "",target_price:0,amount:0,total_money:0,comment:"",unit:"",material_data_name: "",existing_amount:"",target_price:0,
					material_data_no:"",model_number:"",is_qualified:"",}],
		taskinfolist:[{taskid:"",name:"",user_id:"",user_name:"",position:"",comment:"",result:"",time:"",activityName:""}],
		approve_user_status:0,approve_comment:"",
	},
	total_amount:0,
	total_money:0,
	init:function(){
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/approve/returnmaterialapplyapprove/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					for(var i=0;i<json.data.productlist.length;i++){
						if(json.data.productlist[i].is_qualified=='1'){
							json.data.productlist[i].is_qualified='合格';
						}else{
							json.data.productlist[i].is_qualified='不合格';
						}
					}
					avalon.mix(model.data,json.data);
					jisuan();
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
		api.button({
			id:"confirm1",
			name :"通过",
			focus :true,
			callback:function() {
				model.data.approve_user_status=1;
				postData();
				return false;
			}
		}, {
			id:"confirm",
			name :"驳回",
			focus:false,
			callback:function() {
				model.data.approve_user_status=0;
				postData();
				return false;
			}
		}, {
			id:"cancel",
			name :"取消"
		});
	},
	
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


function postData(){
	Public.ajaxPost(rootPath+"/approve/returnmaterialapplyapprove/approve.json",model.data.$model
			, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : t.msg 
			});
			model.data.id=t.data.id;
			parent.model.reloadData(null);
			api.close(); 
		} else{
			parent.parent.Public.tips({
				type : 1,
				content : "失败！" + t.msg
			});
		}
	});
}

model.init();