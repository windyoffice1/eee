var queryConditions = {},url=rootPath+"/sso/snCreater",gridQryUrl=url+"/list.json",
 THISPAGE = {
	init : function() {
		this.initDom();
		this.loadGrid();
		this.addEvent()
	},
	initDom : function() {
	},
	view:function(id){
		$.dialog({
			id : "moreCon",
			width : 800,
			height :300,
			min : true,
			max : true,
			title : "查看序号规则",
			button : [{name : "关闭"	} ],
			resize : true,lock:true,
			content : "url:"+url+"/view.html",
			data : {id:id}
		});
	},
	loadGrid : function() {
		function t(t, e, i) {
			var a = '<div class="operating" data-id="'+ i.id+ '"><span class="fa fa-eye mrb" title="查看"></span><span class="fa fa-edit mrb" title="修改"></span><span class="fa fa-trash-o" title="删除"></span></div>';
			return a
		}
		function e(t) {
			var e = t.join('<p class="line" />');
			return e
		}
		var i = Public.setGrid();
		$("#grid").jqGrid({
			url : gridQryUrl,
			postData : queryConditions,
			datatype : "json",
			mtype:'POST',
			autowidth : true,
			height : i.h,
			altRows : true,
			gridview : true,rownumbers:true,
			multiselect : true,
			multiboxonly : true,
			colModel : [ {
				name : "operating",
				label : "操作",
				fixed : true,width:100,
				formatter:t,
				align : "center",
				title : false
			}, {
				name : "code",
				label : "编码",
				align : "center",
				width:100,
				title : false
			}, {
				name : "name",
				label : "名称",
				align : "center",
				width:100,
				title : false
			}, {
				name : "qz",
				label : "前缀",
				align : "center",
				width:100,
				title : false
			}, {
				name : "qyrq",
				label : "启用日期",
				align : "center",formatter:function(v){var vv=["否","是"];return vv[v];},
				width:100,
				title : false
			}, {
				name : "dqxh",
				label : "当前序号",
				align : "center",
				width:100,
				title : false
			}, {
				name : "sfmrgx",
				label : "更新周期",formatter:function(v){var vv=["不更新","每天","每月","每年"];return vv[v];},
				align : "center",
				width:100,
				title : false
			}, {
				name : "ws",
				label : "序号位数",
				align : "center",
				width:100,
				title : false
			}, {
				name : "udate",
				label : "更新日期",
				align : "center",
				width:100,
				title : false
			} ],
			cmTemplate : {
				sortable : false,
				title : false
			},
			page : 1,
			sortname : "name",
			sortorder : "asc",
			pager : "#page",
			rowNum : 50,
			rowList : [ 50,100, 200 ],
			viewrecords : true,
			shrinkToFit : false,
			forceFit : false,
			jsonReader : {
				root:"data.list",
				records:"data.totalRow",
				page:"data.pageNumber", 
				total:"data.totalPage", 
				repeatitems:false, 
				id : "id"
			},
			loadError : function() {
				parent.Public.tips({
					type : 1,
					content :"加载数据异常！"
				});
			},
			ondblClickRow : function(t) {
				THISPAGE.view(t);
			}
		});
	},
	reloadData : function(t) {
		$("#grid").jqGrid("setGridParam", {
			url : gridQryUrl,
			datatype : "json",mtype:'POST',
			postData : t
		}).trigger("reloadGrid");
	},
	addEvent : function() {
		Public.dateCheck();
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
				handle.del(e);
			}
		});
		$("#add").click(function(t) {
			t.preventDefault();
			if(Business.verifyRight("TF_ADD")){
				handle.operate('add');
			}
		});
		$("#btn-batchDel").click(function(e) {
			e.preventDefault();
			if (Business.verifyRight("BU_DELETE")) {
				var t = $("#grid").jqGrid("getGridParam", "selarrrow");
				t.length ? handle.del(t.join()) : parent.Public.tips({
					type : 2,
					content : "请选择需要删除的项"
				});
			}
		});
		$(window).resize(function() {
			Public.resizeGrid();
		});
	}
};
var handle = {
		operate : function(e, t) {
			if ("add" == e)
				var i = "新增序号规则", r = {oper : e,callback : this.callback};
			else
				var i = "修改序号规则", r = {oper : e,rowId : t,callback : this.callback};
			$.dialog({
				title : i,
				content : "url:"+url+"/edit.html",
				data : r,
				width : 800,
				height :300,
				max :true,resize:true,
				min :false,
				cache :false,
				lock :true
			});
		},
		del : function(id) {
			$.dialog.confirm("删除的序号规则将不能恢复，请确认是否删除？", function() {
				Public.ajaxPost(url+"/del.json", {
					id : id
				}, function(t) {
					if (t && 200 == t.status) {
						var i = t.data || [];
						parent.Public.tips({
							type : 2,
							content : t.msg
						});
						THISPAGE.reloadData();
					} else
						parent.Public.tips({
							type : 1,
							content : "删除序号规则失败！" + t.msg
						});
				});
			});
		},
		callback : function(e, t, i) {
			var r = $("#grid").data("gridData");
			if (!r) {
				r = {};
				$("#grid").data("gridData", r);
			}
			r[e.id] = e;
			if ("edit" == t) {
				$("#grid").jqGrid("setRowData", e.id, e);
				i && i.api.close();
			} else {
				$("#grid").jqGrid("addRowData", e.id, e, "last");
				i && i.resetForm(e);
			}
		}
	};
THISPAGE.init();