var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
url=rootPath+"/basicdata/childwarehouse";
var model = avalon.define({$id:'view',
	data:{id:"",phone:"",manage_user_id:"",manage_user_name:"",adress:"",email:"",child_warehouse_name:"",company_id:""},
    tabActive:0,
    showTab:function(i,b){
    	model.tabActive=i;
    },
});
var THISPAGE = {
	init : function() {
		this.initDom();
	},
	initDom : function() {
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(url+"/qryOp.json",{id:id}, function(json){
				if(json.status==200){
					model.data=json.data.childWarehouse;
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