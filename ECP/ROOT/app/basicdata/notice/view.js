var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
url=rootPath+"/basicdata/notice";
var model = avalon.define({$id:'view',
	data:{title:"",content:"",create_user_id:"",create_user_name:"",create_time:"",id:""},
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
					model.data=json.data.notice;
					parent.THISPAGE.reloadData(null);
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