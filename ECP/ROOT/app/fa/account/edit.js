var api = frameElement.api,oper = api.data.oper,id=api.data.id,
$_form=$("#base_form"),addNew=false,accountType=parent.accountType;
var model = avalon.define({
	$id:'view',
	data:{name:"",bankname:"",khmc:"",yh:1,amt:0,account:"",id:""},
	accountType:accountType
});
var THISPAGE = {
	init : function() {
		this.initDom();
		this.initBtn();
	},
	reset:function(){
	},
	initDom : function() {
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/fa/account/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					model.data=json.data;
					THISPAGE.initEvent();
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}else{
			THISPAGE.initEvent();
		}
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
				return false
			}
		}, {
			id : "cancel",
			name : e[1]
		})
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
				return $(e).closest(".row-item").find("label").text()
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
	var e = "add" == oper ? "新增" : "修改"+"账户";
	Public.ajaxPost(rootPath+"/fa/account/save.json",model.data.$model, function(t) {
		if (200 == t.status) {
			parent.parent.Public.tips({
				content : e + "成功！"
			});
			model.data.id=t.data;
			parent.THISPAGE.reloadData(null);
		} else
			parent.parent.Public.tips({
				type : 1,
				content : e + "失败！" + t.msg
			});
	});
}
THISPAGE.init();