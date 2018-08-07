var url=rootPath+"/inventorymanager/inventory",gridQryUrl=url+"/dataGrid.json",custParame=SYSTEM.custParame,parameList=custParame.typeList;
var model = avalon.define({$id:'view',
	query:{material_type_id:"",material_broad_id:"",model_number:"",material_data_name:"",warehouse_id:"",material_data_no:""},
	warehouseList:[],
	qryWarehouse:function(){
		Public.ajaxPost(rootPath+"/basicdata/warehouselocation/dataGrid.json", {"rows":999999}, function(t) {
			if (t && 200 == t.status) {
				model.warehouseList=t.data.list;
			} else{
				parent.Public.tips({type:1,content: t.msg});
			}
		});
	},
	materialBroadList:[],
	qryMaterialBroad:function(){
		Public.ajaxPost(rootPath+"/basicdata/materialbroad/dataGrid.json", {"rows":999999}, function(t) {
			if (t && 200 == t.status) {
				model.materialBroadList=t.data.list;
			} else{
				parent.Public.tips({type:1,content: t.msg});
			}
		});
	},
	init:function() {
		this.loadGrid();
		this.addEvent();
		model.qryWarehouse();
		model.qryMaterialBroad();
	},
	resetQry:function(){
		model.query={material_type_id:"",material_broad_id:"",model_number:"",material_data_name:"",warehouse_id:"",material_data_no:""};
		model.reloadData();
	},
	loadGrid:function() {
				function t(val, opt, row) {
					var html_con = '<div class="operating" data-id="'+ row.id+'"><span class="fa fa-eye mrb" title="查看"></span>';
						html_con+='<span class="fa fa-edit mrb" title="修改"></span>'+
								'<span class="fa fa-trash-o mrb" title="删除"></span>';
					html_con+='</div>';
					return html_con;
				}
		var i = Public.setGrid();
		function fmtAmt(v) {
			return Public.numToCurrency(v);
		}
		$("#grid").jqGrid({
			url:gridQryUrl,
			postData:model.query.$model,
			datatype:"json",
			mtype:'POST',
			autowidth:true,
			height:i.h,
			altRows:true,
			gridview:true,
			rownumbers:true,
			multiselect:true,
			multiboxonly:true,
			colModel:[  {
				name:"warehouse_name",
				label:"仓位名称",
				align:"center",
				width:100,sortable:true,
				title:false
			}, {
				name:"material_type_name",
				label:"物料类型",
				align:"center",
				width:100,sortable:true,
				title:false
			}, {
				name:"material_broad_name",
				label:"物料大类",
				align:"center",
				width:100,sortable:true,
				title:false
			},  {
				name:"material_data_no",
				label:"物料编码",
				align:"center",
				width:100,sortable:true,
				title:false
			}, {
				name:"material_data_name",
				label:"物料名称",sortable:true,
				align:"center",
				width:100,
				title:false
			}, {
				name:"model_number",
				label:"型号",
				align:"center",sortable:true,
				width:100,
				title:false
			}, {
				name:"existing_amount",
				label:"现存数量",
				align:"center",
				width:100,sortable:true,
				title:false
			},{
				name:"material_average_price",
				label:"平均价格",
				align:"center",
				formatter:fmtAmt,
				width:100,
				sortable:true,
				title:false
			},{
				name:"total_money",
				label:"总金额",
				align:"center",
				width:100,
				formatter:fmtAmt,
				sortable:true,
				title:false
			},{
				name:"admin_name",
				label:"保管员",sortable:true,
				align:"center",
				width:100,
				title:false
			} ],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"material_data_no",
			sortorder:"asc",
			pager:"#page",
			rowNum:50,
			rowList:[ 50,100, 200 ],
			viewrecords:true,
			shrinkToFit:false,
			forceFit:false,
			jsonReader:{
				root:"data.list",
				records:"data.totalRow",
				page:"data.pageNumber", 
				total:"data.totalPage", 
				repeatitems:false, 
				id : "id"
			},
			loadError:function() {
				parent.Public.tips({
					type:1,
					content :"加载数据异常！"
				});
			},
			ondblClickRow:function(t) {
				//model.view(t);
			}
		});
	},
	reloadData:function() {
		$("#grid").jqGrid("setGridParam", {
			url:gridQryUrl,
			datatype:"json",mtype:'POST',
			postData:model.query.$model
		}).trigger("reloadGrid");
	},
	addEvent:function() {
		//Public.dateCheck();
		var t = this;
		$(".grid-wrap").on("click", ".fa-eye", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			model.view(e);
		});
		$(".grid-wrap").on("click", ".audit", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			model.audit(e);
		});
		$(".grid-wrap").on("click", ".fa-edit", function(e) {
			e.preventDefault();
			if (Business.verifyRight("TD_UPDATE")) {
				var t = $(this).parent().data("id");
				model.operate("edit", t);
			}
		});
		$(".grid-wrap").on("click", ".subAudit", function(e) {
			e.preventDefault();
			if (Business.verifyRight("TD_UPDATE")) {
				var t = $(this).parent().data("id");
				var total_money = $(this).parent().data("total_money");
				model.subAudit(t,total_money);
			}
		});
		$(".grid-wrap").on("click", ".disSubAudit", function(e) {
			e.preventDefault();
			if (Business.verifyRight("TD_UPDATE")) {
				var t = $(this).parent().data("id");
				model.disSubAudit(t);
			}
		});
		//删除
		$(".grid-wrap").on("click", ".fa-trash-o", function(t) {
			t.preventDefault();
			if (Business.verifyRight("BU_DELETE")) {
				var e = $(this).parent().data("id");
				model.del(e);
			}
		});
		$(".grid-wrap").on("click", ".del", function(t) {
			t.preventDefault();
			if (Business.verifyRight("BU_DELETE")) {
				var e = $(this).parent().data("id");
					model.del(e);
			}
		});
		$("#add").click(function(t) {
			t.preventDefault();
			if(Business.verifyRight("TF_ADD")){
				model.operate('add');
			}
		});
		$(window).resize(function() {
			Public.resizeGrid();
		});
	},
	operate:function(e, t) {
			if ("add" == e)
				var i = "新增", r = {oper:e};
			else
				var i = "修改", r = {oper:e,id:t};
			$.dialog({title:i,content:"url:"+url+"/edit.html",
				data:r,width:900,height :500,max :true,resize:true,	min :false,	cache :false,lock :true
			});
	},
	view:function(id){
		$.dialog({id:"dialog1",width:900,height :500,min:true,max:true,
			title:"查看",button:[{name:"关闭"	} ],resize:true,lock:true,
			content:"url:"+url+"/view.html",data:{id:id}});
	},
	audit:function(id){
		$.dialog({id:"dialog1",width:900,height :500,min:true,max:true,
			title:"审核"+order_type[type],resize:true,lock:true,
			content:"url:"+url+"/audit.html",data:{id:id}});
	},
	reply:function(e) {
			Public.ajaxPost(url + "/reply.json", {id:e}, function(t) {
				if (t && 200 == t.status) {
					parent.Public.tips({type:2,content:t.msg});
					model.reloadData();
				} else
					parent.Public.tips({type:1,content:"恢复"+order_name+"失败！" + t.msg});
			});
	},
	del:function(id) {
		$.dialog.confirm("删除的采购申请单将不能恢复，请确认是否删除？", function() {
			Public.ajaxPost(url+"/del.json", {
				id:id
			}, function(t) {
				if (t && 200 == t.status) {
					parent.Public.tips({type:2,content:t.msg});
					model.reloadData();
				} else{
					parent.Public.tips({type:1,content: t.msg});
				}
			});
		});
	},
	subAudit:function(id,total_money) {
			Public.ajaxPost(url+"/subAudit.json", {id:id,total_money:total_money,status:1}, function(t) {
				if (t && 200 == t.status) {
					parent.Public.tips({type:2,content:t.msg});
					model.reloadData();
				} else{
					parent.Public.tips({type:1,content:"提交"+order_type[type]+"审核失败！" + t.msg});
				}
		});
	},
	disSubAudit:function(id) {
		$.dialog.confirm("确定取消审核？", function() {
		Public.ajaxPost(url+"/subAudit.json", {id:id,status:0}, function(t) {
			if (t && 200 == t.status) {
				parent.Public.tips({type:2,content:t.msg});
				model.reloadData();
			} else{
				parent.Public.tips({type:1,content:"取消审核失败！" + t.msg});
			}
		});
		});
	}
});
model.init();