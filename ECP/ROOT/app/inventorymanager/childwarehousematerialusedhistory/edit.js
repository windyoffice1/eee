var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
custParame=SYSTEM.custParame,typeList=custParame.typeList,url=rootPath+"/inventorymanager/childwarehousematerialusedhistory";
var model = avalon.define({$id:'view',
	data:{material_data_id:"",amount:0,price:0,total_money:0,child_warehouse_id:"",material_data_no:"",comment:"",},
    parameList:typeList,
    //添加商品
    productlist:[],
    qryProduct:function(v){//自动完成查商品
    	$('.comboDiv').css('display','block');
    	Public.ajaxPost(rootPath+"/inventorymanager/inventorychildwarehouse/dataGrid.json",{child_warehouse_id:model.data.child_warehouse_id,
    		material_data_no:v,_sortField:"material_data_no",_sort:"asc"},function(json){
    		model.productlist=json.data.list;
    	});
    },
    chooseProduct:function(e){
    	$('.comboDiv').css('display','none');
    	model.data.material_data_id=e.material_data_id;
    	model.data.material_data_no=e.material_data_no ;
    	model.data.price=e.average_price;
    },
    superDepartment:"",
	//获取当前登录人的上级部门
	qryCurrentUserOrg:function(){
		console.info(SYSTEM.user);
		Public.ajaxPost(rootPath+"/sso/position/getSuperDepartment.json",{},function(json){
			model.superDepartment=json.data.name;
			if(model.superDepartment=='分公司'){
				model.data.child_warehouse_id=SYSTEM.user.department_id;
			}
		});
	},
    childwarehouseList:[],
    qryChildWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json",{child_warehouse_name:v,type:0,_sortField:"child_warehouse_name",_sort:"asc"},function(json){
    		model.childwarehouseList=json.data.list;
    		if(json.data.list){
    			if(json.data.list.length>0){
    				model.data.child_warehouse_id=json.data.list[0].id;
    			}
    		}
    	});
    },
});
var THISPAGE = {
	init : function() {
		model.qryChildWarehouseList();
		model.qryCurrentUserOrg();
		this.initDom();
		this.initBtn();
	},
	initDom : function() {
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(url+"/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					console.log(json);
					avalon.mix(model.data,json.data.materialData);
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
		THISPAGE.initEvent();
	},
	initBtn:function(){
		var e =  [ "<i class='fa fa-save mrb'></i>保存", "关闭" ] ;
		api.button({
			id : "confirm",
			name : e[0],
			focus : !0,
			callback : function() {
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
			messages : {
				required : "请填写{0}"
			},
			display : function(e) {
				return $(e).closest(".row-item").find("label").text();
			},
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
	Public.ajaxPost(url+"/save.json",model.data.$model, function(json) {
		var e="新增";
		if (200 == json.status) {
			parent.parent.Public.tips({
				content : e + "成功！",
			});
			parent.model.reloadData(null);
			api.close();
		} else
			parent.parent.Public.tips({
				type : 1,
				content :json.msg
			});
	});
}
THISPAGE.init();