var api = frameElement.api,oper = api.data.oper,id=api.data.id,$_form=$("#base_form"),
url=rootPath+"/basicdata/materialdata";
var model = avalon.define({$id:'view',
	data:{belong_to_broad_name:"",belong_to_broad_no:"",material_name:"",material_no:"",model_number:"",existing_number:"",unit:"",target_price:"",float_price:"",keeper:"",keeper_name:"",id:"",
		inventory_warning_amount:"",existing_amount:0,average_price:0.00},
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
					model.data=json.data.materialData;
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