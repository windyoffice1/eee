var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",getmaterial_no:"",getmaterial_name:"",type:"",create_time:"",getmaterial_user_id:"",approve_status:"",company_id:"",create_user_id:"",create_user_name:"",update_user_id:"",
			process_id:"",getmaterial_date:SYSTEM.date,sign_date:SYSTEM.date,warehouse_id:"",update_time:"",total_money:0,total_amount:0,
			productlistlength:1,order_amt:0,child_warehouse_id:"",comment:"",
			productlist:[{id:"",getmaterial_apply_id:"",material_data_id: "",purchase_price:0,amount:0,total_money:0,comment:"",unit:"",material_data_name: "",existing_amount:"",existing_amount:0,
						material_data_no:"",model_number:""}]},
	total_amount:0,
	total_money:0,
	superDepartment:"",
	userList:[],
    qryHead:function(v){
    	Public.ajaxPost(rootPath+"/sso/user/dataGrid.json",{keyword:v,status:1,_sortField:"realname",rows:9999999,_sort:"asc",rows:9999},function(json){
    		model.userList=json.data.list;
    	});
    },
    warehouseList:[],
    qryWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/warehouselocation/dataGrid.json",{keyword:v,rows:9999999,_sortField:"child_warehouse_name",_sort:"asc"},function(json){
    		model.warehouseList=json.data.list;
    	});
    },
    childwarehouseList:[],
    qryChildWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json",{child_warehouse_name:v,rows:9999999,type:0,_sortField:"child_warehouse_name",_sort:"asc"},function(json){
    		model.childwarehouseList=json.data.list;
    		model.data.type="平时领料";
    		if(json.data.list){
    			model.data.child_warehouse_id=json.data.list[0].id;
    		}
    		THISPAGE.initBtn();
    	});
    },
    //添加商品
    productlist:[],productComboDiv:-1,
    qryProduct:function(v,idx){//自动完成查商品
    	var height0=document.getElementById("prodcutTable").scrollTop;
    	var comboDiv =document.querySelectorAll("div[class='comboDiv']");
    	comboDiv[idx].style.marginTop="-"+height0+"px";
    	model.productComboDiv=idx;
    	Public.ajaxPost(rootPath+"/basicdata/materialdata/dataGrid.json",{material_no:v,_sortField:"material_no",_sort:"asc"},function(json){
    		model.productlist=json.data.list;
    	});
    },
    chooseProduct:function(e,idx){
    	model.data.productlist[idx].material_data_id=e.id;
    	model.data.productlist[idx].material_data_no=e.material_no;
    	model.data.productlist[idx].material_data_name=e.material_name;
    	model.data.productlist[idx].model_number=e.model_number;
    	model.data.productlist[idx].purchase_price=e.average_price;//将平均价记录到订单中
    	model.data.productlist[idx].unit=e.unit;
    	if(e.existing_amount!=null&&e.existing_amount!=''){
    		model.data.productlist[idx].existing_amount=e.existing_amount;
    	}else{
    		model.data.productlist[idx].existing_amount=0.00;
    	}
    	model.productComboDiv=-1;
    },
    //增加商品行
    addRow:function(){
    	model.data.productlist.push({material_data_id:'',material_data_name:'',material_data_no:'',model_number:'',comment:"",unit:"",total_money:"",purchase_price:"",amount:"",existing_amount:""});
    },
    //删除商品行
    delRow:function(index){
    	if(model.data.productlist.length==1){
    		parent.parent.Public.tips({type : 1,content : "至少输入一个商品信息"});
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
		model.qryWarehouseList();
		model.qryChildWarehouseList();
		this.initDom();
		setTimeout(jisuan,300);
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/getmaterial/getmaterialapply/qryOp.json",{id:id}, function(json){
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
		model.data.getmaterial_no= "add" ==  api.data.oper ?"自动生成":model.data.getmaterial_no;
	    //获取当前登录人的上级部门
    	Public.ajaxPost(rootPath+"/sso/position/getSuperDepartment.json",{},function(json){
    		model.superDepartment=json.data.name;
    		if(json.data.name=='分公司'){
    			model.data.child_warehouse_id=json.data.id;
    		}
    	});
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
	var e = "add" == oper ? "新增申请单" : "修改申请单";
	model.data.productlistlength=model.data.productlist.length;
	model.data.getmaterial_user_id=SYSTEM.user.id;
	if(model.data.productlistlength==0){
		parent.parent.Public.tips({type : 1,content : "商品必须选择"});
		return;
	}
	model.data.getmaterial_date=$("#getmaterial_date").val();
	model.data.sign_date=$("#sign_date").val();
	Public.ajaxPost(rootPath+"/getmaterial/getmaterialapply/save.json",model.data.$model
			, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : e + "成功！"
			});
			model.data.id=t.data.id;
			model.data.getmaterial_no=t.data.getmaterial_no;
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