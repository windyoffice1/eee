var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",returnmaterial_no:"",returnmaterial_name:"",create_time:"",returnmaterial_user_id:"",returnmaterial_user_name:"",approve_status:"",company_id:"",create_user_id:"",create_user_name:"",
		update_user_id:"",update_user_name:"",comment:"",child_warehouse_id:"",child_warehouse_name:"",
		process_id:"",returnmaterial_date:SYSTEM.date,sign_date:SYSTEM.date,update_time:"",total_money:0,total_amount:0,
		productlistlength:1,order_amt:0,
		productlist:[{id:"",returnmaterial_apply_id:"",material_data_id: "",target_price:0,amount:0,total_money:0,comment:"",unit:"",material_data_name: "",existing_amount:"",
					material_data_no:"",model_number:"",is_qualified:"",putinstorage_amount:"",un_putinstorage_amount:"",putinstorage_comment:"",putinstorage_total_money:"",}]},
	total_plan_amount:0,
	total_un_putinstorage_amount:0,
	total_putinstorage_amount:0,
	total_putinstorage_total_money:0,
    //删除商品行
    delRow:function(index){
    	if(model.data.productlist.length==1){
    		parent.parent.Public.tips({type : 1,content : "至少输入一个物料信息"});
    		return;
    	}
    	model.data.productlist.splice(index,1);
    	jisuan();
    }
});
//计算合计 注意：对于数组不能使用$watch 因为只能监听length。
model.data.productlist.$watch("length",function(name,a,b){
	//jisuan();
});
function jisuan(value,index){
	if(value!=undefined&&value!=''&&value!='undefined'){
		if(model.data.productlist[index].putinstorage_amount>model.data.productlist[index].un_putinstorage_amount){
			alert("实际退料数量不能大于未退料数量");
			model.data.productlist[index].putinstorage_amount=0;
			model.data.productlist[index].putinstorage_total_money=0;
		}else{
			model.data.productlist[index].putinstorage_total_money=model.data.productlist[index].putinstorage_amount*model.data.productlist[index].target_price;//实际入库金额
		}
	}
	model.total_plan_amount=0;
	model.total_un_putinstorage_amount=0;
	model.total_putinstorage_amount=0;
	model.total_putinstorage_total_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_plan_amount=new Number(model.total_plan_amount)+new Number(el.amount);
		model.total_un_putinstorage_amount=new Number(model.total_un_putinstorage_amount)+new Number(el.un_putinstorage_amount);
		model.total_putinstorage_amount=new Number(model.total_putinstorage_amount)+new Number(el.putinstorage_amount);
		model.total_putinstorage_total_money=new Number(model.total_putinstorage_total_money)+new Number(el.putinstorage_total_money);
	}
	model.data.order_amt=model.total_putinstorage_total_money;
};

var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/ordermanager/returnmaterialmanager/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					for(var i=0;i<json.data.productlist.length;i++){
						var el=json.data.productlist[i];
						el.putinstorage_amount=0;
						el.putinstorage_total_money=0;
						if(el.is_qualified==1){
							el.is_qualified='合格';
						}else{
							el.is_qualified='不合格';
						}
					}
					avalon.mix(model.data,json.data);
					jisuan();
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
		THISPAGE.initEvent();
	},
	initBtn:function(){
		var e = "add" ==  oper ? [ "<i class='fa fa-save mrb'></i>保存", "关闭" ] : [ "<i class='fa fa-save mrb'></i>确定", "取消" ];
		api.button({
			id : "confirm",
			name : e[0],
			focus : !0,
			callback : function() {
				addNew=false;
				$_form.trigger("validate");
				return false;
			}
		}, {
			id : "cancel",
			name : e[1]
		});
	},
	initEvent:function(){
		this.initValidator();
	},
	initValidator:function() {
		$_form.validator({
			valid : function() {
				postData();
			},
			ignore : ":hidden",
			theme : "yellow_bottom",
			timely : 1,
			stopOnError : true
		});
	}
};
function postData(){
	model.data.productlistlength=model.data.productlist.length;
	if(model.data.productlistlength==0){
		parent.parent.Public.tips({type : 1,content : "商品必须选择"});
		return;
	}
	Public.ajaxPost(rootPath+"/ordermanager/returnmaterialmanager/putInstorage.json",model.data.$model
			, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : t.msg
			});
			parent.THISPAGE.reloadData();
			api.close();
		} else
			parent.parent.Public.tips({
				type : 1,
				content : "入库失败！" + t.msg
			});
	});
}
THISPAGE.init();