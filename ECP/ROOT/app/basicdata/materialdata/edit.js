var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
custParame=SYSTEM.custParame,typeList=custParame.typeList,url=rootPath+"/basicdata/materialdata";
var model = avalon.define({$id:'view',
	data:{belong_to_broad_id:"",unit:"",material_name:"",material_no:"",model_number:"",existing_number:"",target_price:"",float_price:"",keeper:"",id:"",
			float_rate:0.00,inventory_warning_amount:0,existing_amount:0,average_price:0.00},
    parameList:typeList,
    userList:[],
    qryHead:function(v){
    	Public.ajaxPost(rootPath+"/sso/user/dataGrid.json",{keyword:v,status:1,_sortField:"realname",rows:9999,_sort:"asc"},function(json){
    		model.userList=json.data.list;
    	});
    },
    
    materialBroadList:[],
    qryMaterialBroad:function(){
    	Public.ajaxPost(rootPath+"/basicdata/materialbroad/dataGrid.json",{_sortField:"material_name",rows:9999,_sort:"asc"},function(json){
    		model.materialBroadList=json.data.list;
    		if(oper=='add'){
    			if(json.data.list&&json.data.list.length>0){
    				model.data.material_no=json.data.list[0].material_no;
    			}
    		}
    	});
    },
    chooseMaterialBroad:function(id){
    	var materialBroadList=model.materialBroadList.$model;
    	console.info(model.materialBroadList);
    	console.info(materialBroadList);
    	if(materialBroadList){
    		if(id){
        		for(var i=0;materialBroadList.length>i;i++){
        			if(id==materialBroadList[i].id){
        				model.data.material_no=materialBroadList[i].material_no;
        			}
        		}
    		}
    	}
    },
});
model.materialBroadList.$watch("length",function(name,a,b){
});
var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
		model.qryMaterialBroad();
		model.qryHead();
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
		var e = "add" ==  api.data.oper ? [ "<i class='fa fa-save mrb'></i>保存", "关闭" ] : [ "<i class='fa fa-save mrb'></i>确定", "取消" ];
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
	var e = "add" == oper ? "新增物料" : "修改物料";
	/*if(model.data.location==''){
		model.tabActive=1;
		parent.parent.Public.tips({type:1,content:"仓位位置必填！"});
		return;
	}*/
	Public.ajaxPost(url+"/save.json",model.data.$model, function(json) {
		if (200 == json.status) {
			parent.parent.Public.tips({
				content : e + "成功！"
			});
			parent.THISPAGE.reloadData(null);
			api.close();
		} else
			parent.parent.Public.tips({
				type : 1,
				content : e + "失败！" + json.msg
			});
	});
}
THISPAGE.init();