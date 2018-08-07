var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",getmaterial_no:"",create_time:0,getmaterial_user_id:"",getmaterial_user_name:"",approve_status:"",company_id:"",comment:"",company_id:"",create_user_name:"",create_user_id:"",
			total_money:"",update_user_id:0,getmaterial_date:0,sign_date:"",total_amount:0,update_time:"",child_warehouse_id:"",child_warehouse_name:"",outputstorage_status:"",getmaterial_name:"",type:"",
			productlistlength:1,order_amt:0,
			productlist:[{getmaterial_apply_id:'',material_data_id: "",id: "",purchase_price: "",material_data_no:"",material_no:"",model_number:"",unit:"",material_data_name: "",
						  material_name:"",total_money:0,amount:0,comment:0,un_outputstorage_amount:0,outputstorage_amount:"",outputstorage_comment:0,target_price:0,real_outputstorage_amount:0,
						  real_outputstorage_money:0,outputstorage_total_money:"",outputstorage_warehouse_id:"",inv_existing_amount:0}]},
	total_plan_amount:0,
	total_real_un_outputstorage_amount:0,
	total_real_outputstarage_amount:0,
	total_real_outputstorage_money:0,
	//仓位查询
    warehouseList:[],
    qryWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/warehouselocation/dataGrid.json",{keyword:v,_sortField:"warehouse_name",rows:9999999,_sort:"asc"},function(json){
    		model.warehouseList=json.data.list;
    		//将仓位1设为默认仓位
    		if(model.warehouseList!=undefined&&model.warehouseList!=''&&model.warehouseList!='undefined'&&model.warehouseList.length>0){
        		for(var i=0;i<model.data.productlist.length;i++){
    				var el=model.data.productlist[i];
    				el.outputstorage_warehouse_id=model.warehouseList[0].id;
    			}
    		}
    	});
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
		if(model.data.productlist[index].real_outputstorage_amount>model.data.productlist[index].un_outputstorage_amount){
			alert("实际出库数量不能大于未出库数量");
			model.data.productlist[index].real_outputstorage_amount=0;
			model.data.productlist[index].outputstorage_total_money=0;
		}else{
			model.data.productlist[index].outputstorage_total_money=model.data.productlist[index].real_outputstorage_amount*model.data.productlist[index].purchase_price;//实际出库金额
		}
	}
	model.total_plan_amount=0;
	model.total_real_un_outputstorage_amount=0;
	model.total_real_outputstarage_amount=0;
	model.total_real_outputstorage_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_plan_amount=new Number(model.total_plan_amount)+new Number(el.amount);
		model.total_real_un_outputstorage_amount=new Number(model.total_real_un_outputstorage_amount)+new Number(el.un_outputstorage_amount);
		model.total_real_outputstarage_amount=new Number(model.total_real_outputstarage_amount)+new Number(el.real_outputstorage_amount);
		model.total_real_outputstorage_money=new Number(model.total_real_outputstorage_money)+new Number(el.outputstorage_total_money);
	}
	model.data.order_amt=model.total_real_outputstorage_money;
};

function chooseWarehouse(el,index){
	model.data.productlist[index].outputstorage_warehouse_id=el.id;
}

var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
	},
	initDom : function() {
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/ordermanager/outputstoragemanager/qryOp.json",{id:id}, function(json){
				model.qryWarehouseList();
				if(json.status==200){
					for(var i=0;i<json.data.productlist.length;i++){
						var el=json.data.productlist[i];
						el.outputstorage_warehouse_id="";
						el.real_outputstorage_amount=el.un_outputstorage_amount;
						el.outputstorage_total_money=el.un_outputstorage_amount*el.purchase_price;
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
//				$_form.trigger("validate");
				postData();
				return false;
			}
		}, {
			id : "cancel",
			name : e[1]
		});
	},
	initEvent:function(){
//		this.initValidator();
	},
//	initValidator:function() {
//		$_form.validator({
//			valid : function() {
//				postData();
//			},
//			ignore : ":hidden",
//			theme : "yellow_bottom",
//			timely : 1,
//			stopOnError : true
//		});
//	}
};
function postData(){
	model.data.productlistlength=model.data.productlist.length;
	if(model.data.productlistlength==0){
		parent.parent.Public.tips({type : 1,content : "物料必须选择"});
		return;
	}
	Public.ajaxPost(rootPath+"/ordermanager/outputstoragemanager/outPutstorage.json",model.data.$model, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : t.msg
			});
			parent.THISPAGE.reloadData();
			api.close();
		} else{
			parent.parent.Public.tips({
				type : 1,
				content : "出库失败！" + t.msg
			});
		}
	});
}
THISPAGE.init();