var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',
	data:{id:"",check_name:"",warehouse_id:"",check_date:SYSTEM.date,operate_user_id:"",operate_user_name:"",company_id:"",status:"",warehouse_name:"",deviation_total_money:0,
		productlistlength:1,order_amt:0,
		productlist:[{id:"",inventory_check_id:"",material_data_id: "",amount:0,target_price:0,total_money:0,comment:"",unit:"",material_data_name: "",unit:"",
			material_data_no:"",model_number:"",original_amount:0,now_amount:0,deviation_amount:0,deviation_total_money:0.00,comment:""}]},
	original_total_amount:0,
	now_total_amount:0,
	total_amount:0,
	deviation_total_money:0,
    warehouseList:[],
    qryWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/warehouselocation/dataGrid.json",{keyword:v,rows:999999,_sortField:"warehouse_name",_sort:"asc"},function(json){
    		model.warehouseList=json.data.list;
    		if(json.data.list!=null&&json.data.list.length>0){
    			model.data.warehouse_id=json.data.list[0].id;
    			model.getWarehouseMaterial(json.data.list[0].id);
    		}
    	});
    },
    //添加商品
    getWarehouseMaterial:function(warehouseId){//自动完成查商品
    	Public.ajaxPost(rootPath+"/inventorymanager/transfer/getWarehouseMaterial.json",{id:warehouseId},function(json){
    		var productlist=json.data;
    		model.data.productlist.splice(0,model.data.productlist.length);
    		if(productlist!=''&&productlist!=null&&productlist!=undefined&&productlist!='undefined'){
    			for(var i=0;i<productlist.length;i++){
    				model.data.productlist.push({id:"",inventory_check_id:"",material_data_id: "",amount:0,target_price:0,total_money:0,comment:"",unit:"",material_data_name: "",unit:"",
    					material_data_no:"",model_number:"",original_amount:0,now_amount:0,deviation_amount:0,deviation_total_money:0.00,});
    				var e=productlist[i];
    				model.data.productlist[i].material_data_id=e.material_data_id;
    				model.data.productlist[i].material_data_no=e.material_data_no;
    		    	model.data.productlist[i].material_data_name=e.material_data_name;
    		    	model.data.productlist[i].model_number=e.model_number;
    		    	model.data.productlist[i].target_price=e.target_price;//将采购价记录到订单中
    		    	model.data.productlist[i].unit=e.unit;
    		    	model.data.productlist[i].original_amount=e.existing_amount;
    		    	model.data.productlist[i].now_amount=0;
    		    	model.data.productlist[i].deviation_amount=0;
    		    	model.data.productlist[i].deviation_total_money=0.00;
    		    	model.data.productlist[i].comment='';
    			}
    		}
    	});
    },
    delRow:function(index){//删除商品行
    	if(model.data.productlist.length==1){
    		parent.parent.Public.tips({type : 1,content : "至少输入一个物料信息"});
    		return;
    	}
    	model.data.productlist.splice(index,1);
    	jisuan();
    }
});

model.data.productlist.$watch("length",function(name,a,b){
	//jisuan();
});

function jisuan(value,index){//计算合计 注意：对于数组不能使用$watch 因为只能监听length。
	if(value!=undefined&&value!=''&&value!='undefined'){
		model.data.productlist[index].deviation_amount=value-model.data.productlist[index].original_amount;
		model.data.productlist[index].deviation_total_money=(value-model.data.productlist[index].original_amount)*model.data.productlist[index].target_price;
	}
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

var THISPAGE = {
	init : function() {
		model.qryWarehouseList();
		this.initDom();
		this.initBtn();
		setTimeout(jisuan,300);
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/inventorymanager/check/qryOp.json",{id:id}, function(json){
				if(json.status==200){
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
		model.data.check_name= "add" ==  api.data.oper ?"自动生成":model.data.check_name;
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
	var e = "add" == oper ? "盘点" : "盘点";
	model.data.productlistlength=model.data.productlist.length;
	if(model.data.productlistlength==0){
		parent.parent.Public.tips({type : 1,content : "物料必须选择"});
		return;
	}
	console.info(model.data.$model);
	Public.ajaxPost(rootPath+"/inventorymanager/check/save.json",model.data.$model, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : e + "成功！"
			});
			model.data.id=t.data.id;
			parent.model.reloadData(null);
			api.close();
		} else
			parent.parent.Public.tips({
				type : 1,
				content : e + "失败！"
			});
	});
}
THISPAGE.init();