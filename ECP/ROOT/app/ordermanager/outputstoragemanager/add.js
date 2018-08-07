var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",purchase_no:"",is_deleted:0,purchase_name:"",create_time:"",purchase_staff_id:"",approve_status:"",comment:"",company_id:"",billsn:"自动生成",creater_id:"",
			customer_id:"",total_amount:0,total_money:0,creater_username:"",putinstorage_date:SYSTEM.date,remark:"",dispatch_date:"",purchase_staff_name:"",real_putinstorage_date:SYSTEM.date,
			contacts_name:"",contacts_id:"",customer_id:"",customer_name:"",productlistlength:1,order_amt:0,putinstorage_comment:"",
			productlist:[{amount:0,description: "",id: "",material_data_id: "",material_data_no:"",material_no:"",model_number:"",unit:"",material_data_name: "",
						  material_name:"",total_money:0,un_putinstorage_amount:0,putinstorage_amount:0,readey_putinstorage_amount:0,float_price:"",purchase_price:0,target_price:0,
						  readey_putinstorage_total_money:0,plan_putinstorage_total_money:0,real_putinstarage_amount:0,real_unputinstorage_amount:0,real_putinstorage_total_money:0,
						  putinstorage_comment:"",putinstorage_warehouse_id:"",invoice_number:"",existing_amount:0}]},
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
    		if(model.warehouseList!=undefined&&model.warehouseList!=''&&model.warehouseList!='undefined'&&model.warehouseList.length>0){
        		for(var i=0;i<model.data.productlist.length;i++){
    				var el=model.data.productlist[i];
    				el.putinstorage_warehouse_id=model.warehouseList[0].id;
    			}
    		}
    	});
    },
    //添加商品
    productlist:[],productComboDiv:-1,
    qryProduct:function(v,index){//自动完成查商品
    	model.productComboDiv=index;
    	Public.ajaxPost(rootPath+"/inventorymanager/transfer/getWarehouseMaterial.json",{material_no:v,id:model.data.productlist[index].putinstorage_warehouse_id},function(json){
    		model.productlist=json.data;
    		console.log(model.productlist);
    	});
    },
    chooseProduct:function(e,idx){
    	model.data.productlist[idx].material_data_id=e.material_data_id;
    	model.data.productlist[idx].material_data_no=e.material_data_no;
    	model.data.productlist[idx].material_data_name=e.material_data_name;
    	model.data.productlist[idx].model_number=e.model_number;
    	model.data.productlist[idx].existing_amount=e.existing_amount;
    	
    	model.data.productlist[idx].purchase_price=e.target_price;//将采购价记录到订单中
    	model.data.productlist[idx].unit=e.unit;
    	model.productComboDiv=-1;
    },
    addRow:function(){
    	model.data.productlist.push({material_data_id:'',material_data_name:'',material_name:'',material_data_no:'',material_no:"",real_putinstarage_amount:0,real_putinstorage_total_money:0,
    		model_number:'',unit:"",purchase_price:"",existing_amount:0});
    },//增加商品行;
    delRow:function(index){//删除商品行
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
		if(-model.data.productlist[index].real_putinstarage_amount>model.data.productlist[index].existing_amount){
			alert("入库数量不能大于库存数量");
			model.data.productlist[index].real_putinstarage_amount=0;
			model.data.productlist[index].real_putinstorage_total_money=0;
		}else if(model.data.productlist[index].real_putinstarage_amount>0){
			alert("请输入负数！");
			model.data.productlist[index].real_putinstarage_amount=0;
			model.data.productlist[index].real_putinstorage_total_money=0;
		}else{
			model.data.productlist[index].real_putinstorage_total_money=model.data.productlist[index].real_putinstarage_amount*model.data.productlist[index].purchase_price;//实际入库金额
		}
	}
	model.total_plan_amount=0;
	model.total_real_putinstorage_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_plan_amount=new Number(model.total_plan_amount)+new Number(el.real_putinstarage_amount);
		model.total_real_putinstorage_money=new Number(model.total_real_putinstorage_money)+new Number(el.real_putinstorage_total_money);
	}
	model.data.order_amt=model.total_real_putinstorage_money;
};

function chooseWarehouse(el,index){
	model.data.productlist[index].putinstorage_warehouse_id=el.id;
}

var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
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
		model.qryWarehouseList();
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
	Public.ajaxPost(rootPath+"/ordermanager/putinstoragemanager/add.json",model.data.$model,function(t) {
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