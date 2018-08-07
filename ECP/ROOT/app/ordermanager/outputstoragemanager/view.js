var api = frameElement.api,oper = api.data.oper,id=api.data.id,custParame=SYSTEM.custParame,typeList=custParame.typeList,type=parent.type;
var model = avalon.define({
	$id:'ctrl',parameList:typeList,type:"",nprint:false,
	data:{id:"",putinstorage_name:'',since_purchase_apply_id:0,since_purchase_apply_name:"",create_time:"",create_user_id:"",create_user_name:"",putinstorage_status:"",comment:"",
			company_id:"",customer_name:"",putinstorage_date:"",real_putinstorage_date:"",putinstorage_comment:"",child_warehouse_name:"",getmaterial_name:"",type:"",
			customer_id:"",total_amount:0,total_money:0,order_amt:0,
			productlistlength:1,
			productlist:[{id:"",putinstorage_id: "",material_data_id: "",amount:0,purchase_price:0,total_money:0,model_number:"",unit:"",material_data_name: "",
				plan_putinstorage_total_money:0,material_data_no:"",unit:"",outputstorage_comment:"",outputstorage_warehouse_id:"",outputstorage_warehouse_name:"",
				outputstorage_date:""}]},
	total_amount:0,
	total_money:0,
	userList:[],
	init:function(){
		var e = [ "<i class='fa fa-save mrb'></i>打印", "关闭" ];
		api.button({
			id : "confirm",
			name : e[0],
			focus : !0,
			callback : function() {
				document.getElementById("prodcutTable").style.overflow="hidden";
				document.getElementById("prodcutTable").style.maxHeight="1200px";
				model.nprint=true;
				window.print();
				model.nprint=false;
				document.getElementById("prodcutTable").style.overflow="auto";
				document.getElementById("prodcutTable").style.maxHeight="400px";
				return false;
			}
		}, {
			id : "cancel",
			name : e[1]
		});
		
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/ordermanager/outputstoragemanager/viewOutPutStorageHistory.json",{id:id}, function(json){
				if(json.status==200){
					avalon.mix(model.data,json.data);
					jisuan();
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
	}
});
function jisuan(v){//计算合计 注意：对于数组不能使用$watch 因为只能监听length。
	model.total_amount=0;
	model.total_money=0;
	for(var i=0;i<model.data.productlist.length;i++){
		var el=model.data.productlist[i];
		model.total_amount=new Number(model.total_amount)+new Number(el.amount);
		model.total_money=new Number(model.total_money)+new Number(el.total_money);
	}
	model.total_money=model.total_money.toFixed(2);
	model.data.order_amt=new Number(model.total_money);
};
model.init();