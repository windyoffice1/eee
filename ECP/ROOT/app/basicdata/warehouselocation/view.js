var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
url=rootPath+"/basicdata/warehouselocation";
var model = avalon.define({$id:'view',
	data:{admin_id:"",phone:"",location:"",warehouse_name:"",admin_name:""},
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
					model.data=json.data.warehouselocation;
/*					Public.ajaxPost(rootPath+"/crm/contacts/mainContacts.json",{customer_id:id}, function(json){
						if(json.status==200){
							model.contacts=json.data;
						}});*/
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