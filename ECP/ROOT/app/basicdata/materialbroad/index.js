var url=rootPath+ "/basicdata/materialbroad", gridQryUrl = url+"/dataGrid.json",custParame=SYSTEM.custParame,parameList=custParame.typeList;
var model = avalon.define({$id:'view',
	query:{
		matarial_name:"",type:"",material_no:"",
	},
});
var THISPAGE = {
	init:function() {
		this.loadGrid();
		this.addEvent();
	},
	view:function(id) {
		$.dialog({
			id:"moreCon",
			width:850,
			height:500,
			min:true,
			max:true,
			title:"查看物料",
			button:[ {
				name:"关闭"
			} ],
			resize:true,
			lock:true,
			content:"url:" + rootPath + "/?go=basicdata/materialbroad/view",
			data:{
				id:id
			}
		});
	},
	loadGrid:function() {
		function t(val, opt, row) {
			var html_con = '<div class="operating" data-id="'
					+ row.id
					+ '"><span class="fa fa-eye mrb" title="查看"></span><span class="fa fa-edit mrb" title="修改"></span><span class="fa fa-trash-o mrb" title="删除"></span>'+(row.is_deleted==1?'<span class="fa fa-reply mrb" title="恢复"></span>':'')+'</div>';
			return html_con;
		}
		function fmtAmt(v) {
			return Public.numToCurrency(v);
		}
		var i = Public.setGrid();
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
			colModel:[ {
				name:"operating",
				label:"操作",
				fixed:true,
				width:100,
				formatter:t,
				align:"center",
				title:false
			},{
				name:"type",
				label:"物料类型",formatter:function(v,e,r){return parent.SYSTEM.custParame[v].name;},
				align:"center",
				width:100,
				sortable:true,
				title:true
			},  {
				name:"material_no",
				label:"大类编号",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"material_name",
				label:"大类名称",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"keeper_name",
				label:"保管员",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"modifier_name",
				label:"修改人",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"alter_time",
				label:"修改时间",
				align:"center",
				width:180,
				sortable:true,
				title:true
			}],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"type",
			sortorder:"desc",
			pager:"#page",
			rowNum:50,
			rowList:[ 50, 100, 200 ],
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
					content:"加载数据异常！"
				});
			},
			ondblClickRow:function(t) {
				$("#" + t).find(".fa-eye").trigger("click");
			}
		});
	},
	reloadData:function() {
		$("#grid").jqGrid("setGridParam", {
			url:gridQryUrl,
			datatype:"json",
			mtype:'POST',
			postData:model.query.$model
		}).trigger("reloadGrid");
	},
	addEvent:function() {
		var t = this;
		$(".grid-wrap").on("click", ".fa-eye", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			THISPAGE.view(e);
		});
		$(".grid-wrap").on("click", ".fa-edit", function(e) {
			e.preventDefault();
			if (Business.verifyRight("TD_UPDATE")) {
				var t = $(this).parent().data("id");
				handle.operate("edit", t);
			}
		});
		$(".grid-wrap").on("click", ".fa-trash-o", function(t) {
			t.preventDefault();
			if (Business.verifyRight("BU_DELETE")) {
				var e = $(this).parent().data("id");
				if(model.query.qryType!=100)
					handle.del(e);
				else
					handle.trash(e);
			}
		});
		$(".grid-wrap").on("click", ".fa-reply", function(t) {
			t.preventDefault();
				var e = $(this).parent().data("id");
				handle.reply(e);
		});
		$("#search").click(function() {
			THISPAGE.reloadData();
		});
		$("#refresh").click(function() {
			model.query={material_name:"",type:"",material_no:"",};
			THISPAGE.reloadData();
		});
		$("#add").click(function(t) {
			t.preventDefault();
			if (Business.verifyRight("TF_ADD")) {
				handle.operate('add');
			}
		});
		$("#btn-batchDel").click(function(e) {
			e.preventDefault();
			if (Business.verifyRight("BU_DELETE")) {
				var t = $("#grid").jqGrid("getGridParam", "selarrrow");
				if(t.length){
					handle.del(t.join());
				}else
					parent.Public.tips({type:2,content:"请选择需要删除的物料大类"});
			}
		});
		$("#btn-batchReply").click(function(e) {
			e.preventDefault();
				var t = $("#grid").jqGrid("getGridParam", "selarrrow");
				if(t.length){
						handle.reply(t.join());
				}else
					parent.Public.tips({type:2,content:"请选择需要恢复的物料大类"});
		});
		$(window).resize(function() {
			Public.resizeGrid();
		});
	}
};
var handle = {
	operate:function(e, t) {
		if ("add" == e)
			var i = "新增物料大类", r = {
				oper:e,
				callback:this.callback
			};
		else
			var i = "修改物料大类", r = {
				oper:e,
				id:t,
				callback:this.callback
			};
		$.dialog({
			title:i,
			content:"url:" + rootPath + "/?go=basicdata/materialbroad/edit",
			data:r,
			width:850,
			height:500,
			max:true,
			min:false,
			cache:false,
			resize:true,
			lock:true
		});
	},
	del:function(e) {
		$.dialog.confirm("删除的物料大类将不能恢复，请确认是否删除？", function() {
			
			Public.ajaxPost(url + "/del.json", {
				id:e
			}, function(t) {
				if (t && 200 == t.status) {
					
					parent.Public.tips({
						type:2,
						content:t.msg 
					});
					THISPAGE.reloadData();
				} else
					parent.Public.tips({
						type:1,
						content:"删除物料大类失败！" + t.msg
					});
			});
		});
	},
	reply:function(e) {
		$.dialog.confirm("请确认是否恢复？", function() {
			Public.ajaxPost(url + "/reply.json", {
				id:e
			}, function(t) {
				if (t && 200 == t.status) {
					
					parent.Public.tips({
						type:2,
						content:t.msg
					});
					THISPAGE.reloadData();
				} else
					parent.Public.tips({
						type:1,
						content:"恢复物料大类失败！" + t.msg
					});
			});
		});
	},
	trash:function(e) {
		$.dialog.confirm("删除的物料大类将不可使用，但可以在回收站还原，是否继续？", function() {
			Public.ajaxPost(url + "/trash", {
				id:e
			}, function(t) {
				if (t && 200 == t.status) {
					
					parent.Public.tips({
						type:2,
						content:t.msg
					});
					THISPAGE.reloadData();
				} else
					parent.Public.tips({
						type:1,
						content:"删除物料失败！" + t.msg
					});
			});
		});
	}
};
THISPAGE.init();