var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,
$_form=$("#base_form"),addNew=false;
var model = avalon.define({
	$id:'ctrl',
	data:{id:"",scrap_name:"",warehouse_id:"",scrap_total_money:"",operate_user_id:"",operate_date:"",company_id:"",operate_user_name:"",warehouse_name:"",
		productlistlength:1,order_amt:0,
		productlist:[{id:"",inventory_scrap_id:"",material_data_id: "",amount:0,target_price:0,total_money:0,comment:"",unit:"",material_data_name: "",unit:"",existing_amount:0,
			material_data_no:"",model_number:""}]},
	total_amount:0,
	scrap_total_money:0,
    warehouseList:[],
    qryWarehouseList:function(v){
    	Public.ajaxPost(rootPath+"/basicdata/warehouselocation/dataGrid.json",{keyword:v,rows:999999,_sortField:"warehouse_name",_sort:"asc"},function(json){
    		model.warehouseList=json.data.list;
    		if(json.data.list!=null&&json.data.list.length>0){
    			model.data.warehouse_id=json.data.list[0].id;
    		}
    	});
    },
    //添加商品
    productlist:[],productComboDiv:-1,
    qryProduct:function(v,idx){//自动完成查商品
    	var height0=document.getElementById("prodcutTable").scrollTop;
    	var comboDiv =document.querySelectorAll("div[class='comboDiv']");
    	comboDiv[idx].style.marginTop="-"+height0+"px";
    	
    	model.productComboDiv=idx;
    	Public.ajaxPost(rootPath+"/inventorymanager/transfer/getWarehouseMaterial.json",{material_no:v,id:model.data.warehouse_id},function(json){
    		model.productlist=json.data;
    	});
    },
    chooseProduct:function(e,idx){
    	model.data.productlist[idx].material_data_id=e.material_data_id;
    	model.data.productlist[idx].material_data_no=e.material_data_no;
    	model.data.productlist[idx].material_data_name=e.material_data_name;
    	model.data.productlist[idx].model_number=e.model_number;
    	model.data.productlist[idx].existing_amount=e.existing_amount;
    	
    	model.data.productlist[idx].target_price=e.target_price;//将采购价记录到订单中
    	model.data.productlist[idx].unit=e.unit;
    	model.productComboDiv=-1;
    },
    addRow:function(){
    	model.data.productlist.push({material_data_id:'',material_data_name:'',material_data_no:'',model_number:'',comment:"",unit:"",total_money:"",target_price:"",amount:"",existing_amount:0,});
    },//增加商品行;
    delRow:function(index){//删除商品行
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

function jisuan(value,index){//计算合计 注意：对于数组不能使用$watch 因为只能监听length。
	if(value!=undefined&&value!=''&&value!='undefined'){
		if(value>model.data.productlist[index].existing_amount){
			alert("移位数量不能大于库存数量！");
			model.data.productlist[index].amount=0;
			model.data.productlist[index].total_money=0;
		}else{
			model.data.productlist[index].total_money=value*model.data.productlist[index].target_price;
		}
	}
	model.total_amount=0;
	model.total_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_amount=new Number(model.total_amount)+new Number(el.amount);
		model.total_money=new Number(model.total_money)+new Number(el.total_money);
	}
	model.data.order_amt=model.total_money;
	model.data.scrap_total_money=model.total_money;
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
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/inventorymanager/scrap/qryOp.json",{id:id}, function(json){
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
	var e = "add" == oper ? "报废" : "报废";
	model.data.productlistlength=model.data.productlist.length;
	if(model.data.productlistlength==0){
		parent.parent.Public.tips({type : 1,content : "物料必须选择"});
		return;
	}
	Public.ajaxPost(rootPath+"/inventorymanager/scrap/save.json",model.data.$model
			, function(t) {
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