var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
custParame=SYSTEM.custParame,typeList=custParame.typeList,url=rootPath+"/basicdata/materialbroad";
var model = avalon.define({$id:'view',
	data:{material_name:"",keeper:"",type:"",id:"",material_no:"",},
    parameList:typeList,
    userList:[],
    qryHead:function(v){
    	Public.ajaxPost(rootPath+"/sso/user/dataGrid.json",{keyword:v,status:1,_sortField:"realname",rows:9999,_sort:"asc"},function(json){
    		model.userList=json.data.list;
    	});
    },
});

model.data.$watch("$all",function(name,a,b){
	if(a==null||a=="null"){
		model.data[name]="";
	}
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
					avalon.mix(model.data,json.data.materialBroad);
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
	var e = "add" == oper ? "新增物料大类" : "修改物料大类";
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