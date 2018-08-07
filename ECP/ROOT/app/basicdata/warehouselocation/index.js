var url=rootPath+ "/basicdata/warehouselocation", gridQryUrl = url+"/dataGrid.json";
var model = avalon.define({$id:'view',
	query:{
		admin:"",warehouse_name:""
	},
});
var THISPAGE = {
	init:function() {
		this.loadGrid();
		this.addEvent()
	},
	view:function(id) {
		$.dialog({
			id:"moreCon",
			width:850,
			height:500,
			min:true,
			max:true,
			title:"查看仓位",
			button:[ {
				name:"关闭"
			} ],
			resize:true,
			lock:true,
			content:"url:" + rootPath + "/?go=basicdata/warehouselocation/view",
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
			}, {
				name:"warehouse_name",
				label:"仓位名称",
				align:"center",
				width:100,
				sortable:true,
				title:true
			}, {
				name:"admin_name",
				label:"管理员",
				align:"center",
				width:100,
				sortable:true,
				title:true
			}, {
				name:"phone",
				label:"联系电话",
				align:"center",
				width:100,
				sortable:true,
				title:true
			}, {
				name:"location",
				label:"位置",
				align:"center",
				width:100,
				sortable:true,
				title:true
			}],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"admin_name",
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
				console.info(e);
				console.info(model);
				if(model.query.qryType!=100)
					handle.del(e);
				else
					handle.trash(e);
			}
		});
		$("#search").click(function() {
			THISPAGE.reloadData();
		});
		$("#refresh").click(function() {
			THISPAGE.reloadData();
		});
		$("#add").click(function(t) {
			t.preventDefault();
			if (Business.verifyRight("TF_ADD")) {
				handle.operate('add');
			}
		});
		$(window).resize(function() {
			Public.resizeGrid();
		});
	}
};
var handle = {
	operate:function(e, t) {
		if ("add" == e)
			var i = "新增仓位", r = {
				oper:e,
				callback:this.callback
			};
		else
			var i = "修改仓位", r = {
				oper:e,
				id:t,
				callback:this.callback
			};
		$.dialog({
			title:i,
			content:"url:" + rootPath + "/?go=basicdata/warehouselocation/edit",
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
		$.dialog.confirm("删除的仓位将不能恢复，请确认是否删除？", function() {
			
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
						content:"删除仓位失败！" + t.msg
					});
			});
		});
	},
	trash:function(e) {
		$.dialog.confirm("删除的供应商将不可使用，但可以在回收站还原，是否继续？", function() {
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
						content:"删除供应商失败！" + t.msg
					});
			});
		});
	}
};
THISPAGE.init();