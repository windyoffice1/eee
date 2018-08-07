var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",
	data:{id:"",purchase_no:'',is_deleted:0,purchase_name:"",create_time:"",purchase_staff_id:"",approve_status:"",comment:"",company_id:"",billsn:"自动生成",creater_id:"",
			customer_id:"",total_amount:0,total_money:0,creater_username:"",
			contacts_name:"",contacts_id:"",customer_id:"",customer_name:"",productlistlength:1,rebate:0,rebate_amt:0,order_amt:0,
			productlist:[{amount:0,description: "",id: "",material_data_id: "",material_data_no:"",material_no:"",model_number:"",unit:"",material_data_name: "",purchase_price:0,total_money:0}]},
	total_amount:0,
	total_money:0,
	userList:[],
    qryHead:function(v){
    	Public.ajaxPost(rootPath+"/sso/user/dataGrid.json",{keyword:v,status:1,_sortField:"realname",rows:9999,_sort:"asc",rows:9999},function(json){
    		model.userList=json.data.list;
    	});
    },
	customerList:[],custComboV:false,
	chooseCust:function(e){
    	model.data.customer_id=e.id;
    	model.data.customer_name=e.name;
    	model.custComboV=false;
    },
    qryCustomer:function(v){//自动完成查询客户
    	model.custComboV=true;
    	Public.ajaxPost(rootPath+"/crm/customer/dataGrid.json",{keyword:v,_sortField:"realname",_sort:"asc",type:0},function(json){
    		model.customerList=json.data.list;
    	});
    },
    //添加商品
    productlist:[],productComboDiv:-1,
    qryProduct:function(v,idx){//自动完成查商品
    	model.productComboDiv=idx;
    	Public.ajaxPost(rootPath+"/basicdata/materialdata/dataGrid.json",{keyword:v,_sortField:"name",_sort:"asc"},function(json){
    		model.productlist=json.data.list;
    	});
    },
    chooseProduct:function(e,idx){
    	model.data.productlist[idx].material_data_id=e.id;
    	model.data.productlist[idx].material_data_no=e.material_no;
    	model.data.productlist[idx].material_no=e.material_no;
    	model.data.productlist[idx].material_data_name=e.material_name;
    	model.data.productlist[idx].model_number=e.model_number;

    	model.data.productlist[idx].purchase_price=e.target_price;//将采购价记录到订单中
    	model.data.productlist[idx].unit=e.unit;
    	model.productComboDiv=-1;
    },
    addRow:function(){
    	model.data.productlist.push({material_data_id:'',material_data_name:'',material_data_no:'',material_no:"",model_number:'',description:"",unit:"",total_money:"",purchase_price:"",amount:""});
    },//增加商品行;
    delRow:function(e){//删除商品行
    	if(model.data.productlist.length==1){
    		parent.parent.Public.tips({type : 1,content : "至少输入一个商品信息"});
    		return;
    	}
    	e.preventDefault();
    	console.info(e);
        //var item = this.$vmodel.$remove();
    	model.data.productlist.remove(item);
    	jisuan();
    }
});
avalon.scan();

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
		this.initDom();
		this.initBtn();
		setTimeout(jisuan,300);
	},
	initDom : function() {
		$(".ui-datepicker-input").datepicker();
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/scm/purchaseapply/qryOp.json",{id:id}, function(json){
				console.info(json);
				console.info(json);
				console.info(json);
				console.info(json);
				console.info(json);
				console.info(json);
				if(json.status==200){
					avalon.mix(model.data,json.data);
/*					model.data=json.data;
					model.data.productlist=json.data.productlist;
					model.data.productlistlength=json.data.productlistlength;
					console.info(model.data);
					console.info(json.data.productlistlength);
					console.info(json.data.productlist);*/
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
	var e = "add" == oper ? "新增订单" : "修改订单";
	model.data.productlistlength=model.data.productlist.length;
	model.audit_status=0;
	if(model.data.productlistlength==0){
		parent.parent.Public.tips({type : 1,content : "商品必须选择"});
		return;
	}
	Public.ajaxPost(rootPath+"/scm/purchaseapply/save.json",model.data.$model
			, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : e + "成功！"
			});
			model.data.id=t.data.id;
			model.data.billsn=t.data.sn;
			parent.model.reloadData(null);
		} else
			parent.parent.Public.tips({
				type : 1,
				content : e + "失败！" + t.msg
			});
	});
}
THISPAGE.init();