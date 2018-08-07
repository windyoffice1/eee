var api = frameElement.api,oper = api.data.oper,id=api.data.id;
var model = avalon.define({$id:'ctrl',
	data:{id:"",deploymentId:"",imageName:"",acs:"",
			},
	init:function(){
		if(id!=undefined&&id!=''&&id!='undefined'){
			Public.ajaxPost(rootPath+"/approve/purchaseapplyapprove/purchaseApplyflow.json",{id:id}, function(json){
				if(json.status==200){
					//avalon.mix(model.data,json.data);
					console.info(json.data);
					var html_con ='<img style="position: absolute;top: 0px;left: 0px;" src="viewImage?deploymentId='+json.data.deploymentId+'&imageName='+json.data.imageName+'">';
					html_con+='<div style="position: absolute;border:1px solid red;top:<s:property value='+json.data.acs.y+
						'/>px;left: <s:property value='+json.data.acs.x+
						'/>px;width: <s:property value='+json.data.acs.width+
						'/>px;height:<s:property value='+json.data.acs.height+'/>px;   "></div>';
					$(".manage-wrapper").append(html_con);
				}else{
					parent.Public.tips({type: 1, content : json.msg});
				}
			});
		}
	}
});
model.init();


