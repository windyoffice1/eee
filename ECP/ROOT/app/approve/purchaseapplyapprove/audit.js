var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,type=parent.type,
s=["","","通过","拒绝"];
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:type,audit_hidden:parent.audit_hidden,
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",purchase_no:'',is_deleted:0,purchase_name:"",create_time:"",purchase_staff_id:"",approve_status:"",comment:"",company_id:"",billsn:"自动生成",creater_id:"",purchase_staff_name:"",
			customer_id:"",total_amount:0,total_money:0,creater_username:"",update_username:"",update_time:"",update_id:"",process_id:"",dispatch_date:"",sign_date:"",
			approve_user_status:1,approve_comment:"",
			contacts_name:"",contacts_id:"",customer_id:"",customer_name:"",productlistlength:1,rebate:0,rebate_amt:0,order_amt:0,
			productlist:[{amount:0,description: "",id: "",material_data_id: "",material_data_no:"",material_no:"",model_number:"",unit:"",material_data_name: "",
				existing_amount:0,target_price:0,total_money:0,float_price:""}],
			taskinfolist:[{taskid:"",name:"",user_id:"",user_name:"",position:"",comment:"",result:"",time:"",activityName:""}],
	},
	total_amount:0,
	total_money:0,
	userList:[],
	init:function(){
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/approve/purchaseapplyapprove/qryOp.json",{id:id}, function(json){
				if(json.status==200){
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
	Public.ajaxPost(rootPath+"/approve/purchaseapplyapprove/approve.json",model.data.$model
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