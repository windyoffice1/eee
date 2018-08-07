var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
custParame=SYSTEM.custParame,typeList=custParame.typeList,url=rootPath+"/basicdata/childwarehouse";
var model = avalon.define({$id:'view',
	data:{id:"",phone:"",adress:"",email:"",manage_user_id:"",child_warehouse_name:"",type:0},
    parameList:typeList,
	userList:[],
    qryHead:function(v){
    	Public.ajaxPost(rootPath+"/sso/user/dataGrid.json",{keyword:v,status:1,_sortField:"realname",rows:9999,_sort:"asc",rows:9999},function(json){
    		model.userList=json.data.list;
    	});
    },
});
var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
		model.qryHead();
	},
	initDom : function() {
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(url+"/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					avalon.mix(model.data,json.data.childWarehouse);
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
	var e = "add" == oper ? "新增子仓库" : "修改子仓库";
	if(model.data.type==1){
		e='修改退料仓库';
	}
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