var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",purchase_no:"",is_deleted:0,purchase_name:"",create_time:"",purchase_staff_id:"",approve_status:"",comment:"",company_id:"",billsn:"自动生成",creater_id:"",
			customer_id:"",total_amount:0,total_money:0,creater_username:"",putinstorage_date:SYSTEM.date,remark:"",dispatch_date:"",purchase_staff_name:"",
			contacts_name:"",contacts_id:"",customer_id:"",customer_name:"",productlistlength:1,order_amt:0,
			productlist:[{amount:0,description: "",id: "",material_data_id: "",material_data_no:"",material_no:"",model_number:"",unit:"",material_data_name: "",
						  material_name:"",total_money:0,un_putinstorage_amount:0,putinstorage_amount:0,readey_putinstorage_amount:0,float_price:"",purchase_price:0,target_price:0,
						  readey_putinstorage_total_money:0,plan_putinstorage_total_money:0}]},
	total_amount:0,
	total_money:0,

    delRow:function(index){//删除商品行
    	if(model.data.productlist.length==1){
    		parent.parent.Public.tips({type : 1,content : "至少输入一个商品信息"});
    		return;
    	}
    	model.data.productlist.splice(index,1);
    	jisuan();
    }
});
//avalon.scan();

model.data.productlist.$watch("length",function(name,a,b){
	jisuan();
});
function jisuan(value,index){//计算合计 注意：对于数组不能使用$watch 因为只能监听length。
	if(value!=undefined&&value!=''&&value!='undefined'){
		if(value>model.data.productlist[index].un_putinstorage_amount){
			alert("入库数量不能大于未出库数量");
			model.data.productlist[index].readey_putinstorage_amount=0;
			model.data.productlist[index].readey_putinstorage_total_money=0;
			model.data.productlist[index].plan_putinstorage_total_money=0;
/*			model.data.order_amt=0;
			model.total_amount=0;
			model.total_money=0;*/
		}else{
			model.data.productlist[index].readey_putinstorage_total_money=value*model.data.productlist[index].purchase_price;//实际金额
			model.data.productlist[index].plan_putinstorage_total_money=value*model.data.productlist[index].target_price;//计划金额
		}
		model.total_amount=0;
		model.total_money=0;
		for(var i=0;i<model.data.productlist.length;i++){
			var el=model.data.productlist[i];
			model.total_amount=new Number(model.total_amount)+new Number(el.readey_putinstorage_amount);
			model.total_money=new Number(model.total_money)+new Number(el.readey_putinstorage_total_money);
		}
		model.data.order_amt=model.total_money;
		model.data.total_money=model.total_money;
		model.data.total_amount=model.total_amount;
	}
	
};
function jisuan_purchase(purchase_price,index){
	model.data.productlist[index].readey_putinstorage_total_money=purchase_price*model.data.productlist[index].readey_putinstorage_amount;//实际金额
	model.total_amount=0;
	model.total_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_amount=new Number(model.total_amount)+new Number(el.readey_putinstorage_amount);
		model.total_money=new Number(model.total_money)+new Number(el.readey_putinstorage_total_money);
	}
	model.data.order_amt=model.total_money;
	model.data.total_money=model.total_money;
	model.data.total_amount=model.total_amount;
};

var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
		//setTimeout(jisuan,300);
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/scm/putinstorage/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					for(var i=0;i<json.data.productlist.length;i++){
						var el=json.data.productlist[i];
						el.purchase_price=el.target_price;
						el.readey_putinstorage_amount=el.un_putinstorage_amount;
						el.readey_putinstorage_total_money=el.target_price*el.un_putinstorage_amount;
						el.plan_putinstorage_total_money=el.target_price*el.un_putinstorage_amount;
						model.total_amount=new Number(model.total_amount)+new Number(el.readey_putinstorage_amount);
						model.total_money=new Number(model.total_money)+new Number(el.readey_putinstorage_total_money);
					}
					avalon.mix(model.data,json.data);
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
		THISPAGE.initEvent();
	},
	initBtn:function(){
		var e = "add" ==  api.data.oper ? [ "<i class='fa fa-save mrb'></i>保存", "关闭" ] : [ "<i class='fa fa-save mrb'></i>确定", "取消" ];
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
		jisuan();
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
	Public.ajaxPost(rootPath+"/scm/putinstorage/putInstorage.json",model.data.$model, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : t.msg
			});
			parent.model.reloadData(null);
			api.close();
		} else
			parent.parent.Public.tips({
				type : 1,
				content : "入库失败！" + t.msg
			});
	});
}
THISPAGE.init();