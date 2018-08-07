var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",returnmaterial_no:"",returnmaterial_name:"",create_time:"",returnmaterial_user_id:"",returnmaterial_user_name:"",approve_status:"",company_id:"",create_user_id:"",create_user_name:"",
		update_user_id:"",update_user_name:"",comment:"",child_warehouse_id:"",
		process_id:"",returnmaterial_date:SYSTEM.date,sign_date:SYSTEM.date,update_time:"",total_money:0,total_amount:0,
		productlistlength:1,order_amt:0,
		productlist:[{id:"",returnmaterial_apply_id:"",material_data_id: "",target_price:0,amount:0,total_money:0,comment:"",unit:"",material_data_name: "",existing_amount:"",
					material_data_no:"",model_number:"",is_qualified:"",}]},
	total_amount:0,
	total_money:0,
	superDepartment:"",
	userList:[],
    qryHead:function(v){
    	Public.ajaxPost(rootPath+"/sso/user/dataGrid.json",{keyword:v,status:1,_sortField:"realname",rows:99999,_sort:"asc",rows:9999},function(json){
    		model.userList=json.data.list;
    	});
    },
    //添加商品
    productlist:[],productComboDiv:-1,
    qryProduct:function(v,idx){//自动完成查商品
    	var height0=document.getElementById("prodcutTable").scrollTop;
    	var comboDiv =document.querySelectorAll("div[class='comboDiv']");
    	comboDiv[idx].style.marginTop="-"+height0+"px";
    	
    	model.productComboDiv=idx;
    	Public.ajaxPost(rootPath+"/inventorymanager/inventorychildwarehouse/dataGrid.json",{child_warehouse_id:model.data.child_warehouse_id,rows:999999,
    		material_data_no:v,_sortField:"material_data_no",_sort:"asc"},function(json){
    		model.productlist=json.data.list;
    	});
    },
    childwarehouseList:[],
    qryChildWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json",{child_warehouse_name:v,rows:999999,type:0,_sortField:"child_warehouse_name",_sort:"asc"},function(json){
    		var list=json.data.list;
    		model.childwarehouseList=list;
    		if(list!=null&&list!=''&&list!= undefined&&list!='undefined'&&list.length>0){
    			model.data.child_warehouse_id=list[0].id;
    		}
    	});
    },
    chooseProduct:function(e,idx){
    	model.data.productlist[idx].material_data_id=e.material_data_id;
    	model.data.productlist[idx].material_data_no=e.material_data_no;
    	model.data.productlist[idx].material_data_name=e.material_data_name;
    	model.data.productlist[idx].model_number=e.model_number;
    	
    	model.data.productlist[idx].target_price=e.average_price;//将平均价记录到订单中
    	model.data.productlist[idx].unit=e.unit;
    	model.data.productlist[idx].existing_amount=e.existing_amount;
    	model.productComboDiv=-1;
    },
    //增加商品行;
    addRow:function(){
    	model.data.productlist.push({material_data_id:'',material_data_name:'',material_data_no:'',model_number:'',comment:"",unit:"",total_money:"",target_price:"",amount:"",existing_amount:"",is_qualified:""});
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
model.data.productlist.$watch("length",function(name,a,b){
	jisuan();
});
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
var THISPAGE = {
	init : function() {
		model.qryHead();
		model.qryChildWarehouseList();
		this.initDom();
		this.initBtn();
		setTimeout(jisuan,300);
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/returnmaterial/returnmaterialapply/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					avalon.mix(model.data,json.data);
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}else{
		    //获取当前登录人的上级部门
	    	Public.ajaxPost(rootPath+"/sso/position/getSuperDepartment.json",{},function(json){
	    		model.superDepartment=json.data.name;
	    		if(json.data.name=='分公司'){
	    			model.data.child_warehouse_id=json.data.id;
	    		}
	    	});
		}
		THISPAGE.initEvent();
	},
	initBtn:function(){
		var e = "add" ==  api.data.oper ? [ "<i class='fa fa-save mrb'></i>保存", "关闭" ] : [ "<i class='fa fa-save mrb'></i>确定", "取消" ];
		model.data.returnmaterial_no= "add" ==  api.data.oper ?"自动生成":model.data.returnmaterial_no;
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
	var e = "add" == oper ? "新增退料单" : "修改退料单";
	model.data.productlistlength=model.data.productlist.length;
	model.data.returnmaterial_user_id=SYSTEM.user.id;
	if(model.data.productlistlength==0){
		parent.parent.Public.tips({type : 1,content : "商品必须选择"});
		return;
	}
	var child_warehouse_id = model.data.child_warehouse_id;
	if(child_warehouse_id==''||child_warehouse_id==null||child_warehouse_id==undefined){
		parent.parent.Public.tips({type : 1,content : "退料单位必须选择"});
		return;
	}
	model.data.returnmaterial_date=$("#returnmaterial_date").val();
	model.data.sign_date=$("#sign_date").val();
	Public.ajaxPost(rootPath+"/returnmaterial/returnmaterialapply/save.json",model.data.$model
			, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : e + "成功！"
			});
			model.data.id=t.data.id;
			model.data.returnmaterial_no=t.data.returnmaterial_no;
			parent.model.reloadData(null);
			api.close();
		} else
			parent.parent.Public.tips({
				type : 1,
				content : e + "失败！" + t.msg
			});
	});
}
THISPAGE.init();