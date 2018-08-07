var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
url=rootPath+"/basicdata/materialbroad";
var model = avalon.define({$id:'view',
	data:{material_no:"",material_name:"",keeper_name:"",type:"",modifier_name:"",alter_time:"",id:""},
    tabActive:0,
    showTab:function(i,b){
    	model.tabActive=i;
    }
});
var THISPAGE = {
	init : function() {
		this.initDom();
	},
	initDom : function() {
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(url+"/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					model.data=json.data.materialBroad;
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
		THISPAGE.initEvent();
	},
	initEvent:function(){
		
	}
};
THISPAGE.init();