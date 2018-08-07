var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",purchase_no:"",is_deleted:0,purchase_name:"",create_time:"",purchase_staff_id:"",approve_status:"",comment:"",company_id:"",creater_id:"",
			customer_id:"",total_amount:0,total_money:0,creater_username:"",putinstorage_date:SYSTEM.date,remark:"",dispatch_date:"",purchase_staff_name:"",real_putinstorage_date:SYSTEM.date,
			contacts_name:"",contacts_id:"",customer_id:"",customer_name:"",productlistlength:1,order_amt:0,putinstorage_comment:"",
			productlist:[{amount:0,description: "",id: "",material_data_id: "",material_data_no:"",material_no:"",model_number:"",unit:"",material_data_name: "",
						  material_name:"",total_money:0,un_putinstorage_amount:0,putinstorage_amount:0,readey_putinstorage_amount:0,float_price:"",purchase_price:0,target_price:0,
						  readey_putinstorage_total_money:0,plan_putinstorage_total_money:0,real_putinstarage_amount:0,real_unputinstorage_amount:0,real_putinstorage_total_money:0,
						  putinstorage_comment:"",putinstorage_warehouse_id:"",invoice_number:"",}]},
	total_plan_amount:0,
	total_plan_money:0,
	total_real_putinstarage_amount:0,
	total_real_unputinstorage_amount:0,
	total_real_putinstorage_money:0,
	//仓位查询
    warehouseList:[],
    qryWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/warehouselocation/dataGrid.json",{keyword:v,rows:9999999,_sortField:"warehouse_name",_sort:"asc"},function(json){
    		model.warehouseList=json.data.list;
    		//将仓位1设为默认仓位
    		if(model.warehouseList){
        		for(var i=0;i<model.data.productlist.length;i++){
    				var el=model.data.productlist[i];
    				el.putinstorage_warehouse_id=model.warehouseList[0].id;
    			}
    		}
    	});
    },
    chooseWarehouse:function(el,index){
    	model.data.productlist[index].putinstorage_warehouse_id=el.id;
    },

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
		if(model.data.productlist[index].real_putinstarage_amount>model.data.productlist[index].real_unputinstorage_amount){
			alert("实际入库数量不能大于未入库数量");
			model.data.productlist[index].real_putinstarage_amount=0;
			model.data.productlist[index].real_putinstorage_total_money=0;
		}else{
			model.data.productlist[index].real_putinstorage_total_money=model.data.productlist[index].real_putinstarage_amount*model.data.productlist[index].purchase_price;//实际入库金额
			//减少未入库数量
			//model.data.productlist[index].real_unputinstorage_amount=new Number(model.data.productlist[index].real_unputinstorage_amount)-new Number(value);
		}
	}
	model.total_plan_amount=0;
	model.total_plan_money=0;
	model.total_real_putinstarage_amount=0;
	model.total_real_unputinstorage_amount=0;
	model.total_real_putinstorage_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_plan_amount=new Number(model.total_plan_amount)+new Number(el.amount);
		model.total_plan_money=new Number(model.total_plan_money)+new Number(el.total_money);
		model.total_real_putinstarage_amount=new Number(model.total_real_putinstarage_amount)+new Number(el.real_putinstarage_amount);
		model.total_real_unputinstorage_amount=new Number(model.total_real_unputinstorage_amount)+new Number(el.real_unputinstorage_amount);
		model.total_real_putinstorage_money=new Number(model.total_real_putinstorage_money)+new Number(el.real_putinstorage_total_money);
	}
	model.data.order_amt=model.total_real_putinstorage_money;
};

var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/ordermanager/putinstoragemanager/qryDetails.json",{id:id}, function(json){
				model.qryWarehouseList();
				if(json.status==200){
					for(var i=0;i<json.data.productlist.length;i++){
						var el=json.data.productlist[i];
						el.real_putinstarage_amount=el.real_unputinstorage_amount;
						el.readey_putinstorage_total_money=0;
						el.plan_putinstorage_total_money=0;
						el.putinstorage_warehouse_id="";
						el.invoice_number="";
						el.putinstorage_comment="";
						el.real_putinstorage_total_money=el.real_putinstarage_amount*el.purchase_price;//实际入库金额
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
		parent.parent.Public.tips({type : 1,content : "物料必须选择"});
		return;
	}
	console.info(model.data.$model);
	Public.ajaxPost(rootPath+"/ordermanager/putinstoragemanager/putInstorage.json",model.data.$model
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