var url=rootPath+ "/scm/putinstorage", gridQryUrl = url+"/dataGrid.json",custParame=SYSTEM.custParame,parameList=custParame.typeList;
var model = avalon.define({$id:'view',
	query:{
		putinstorage_name:"",create_user_id:""
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
			width:1000,
			height:500,
			min:true,
			max:true,
			title:"查看入库通知单",
			button:[ {
				name:"关闭"
			} ],
			resize:true,
			lock:true,
			content:"url:" + rootPath + "/?go=scm/putinstorage/view",
			data:{
				id:id
			}
		});
	},
	loadGrid:function() {
		function t(val, opt, row) {
			var html_con = '<div class="operating" data-id="'+ row.id+ '"><span class="fa fa-eye mrb" title="查看"></span>';
			/*'<span class="fa fa-trash-o mrb" title="删除"></span>'+'</div>';*/
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
				name:"putinstorage_name",
				label:"入库单名称",
				align:"center",
				width:300,
				sortable:true,
				title:true
			}, {
				name:"since_purchase_apply_name",
				label:"申请单名称",
				align:"center",
				width:300,
				sortable:true,
				title:true
			},{
				name:"create_user_name",
				label:"创建人",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"create_time",
				label:"创建时间",
				align:"center",
				width:200,
				sortable:true,
				title:true
			}],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"create_time",
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
		$("#search").click(function() {
			THISPAGE.reloadData();
		});
		$("#refresh").click(function() {
			THISPAGE.reloadData();
		});
		$(".grid-wrap").on("click", ".fa-trash-o", function(t) {
			t.preventDefault();
			if (Business.verifyRight("BU_DELETE")) {
				var e = $(this).parent().data("id");
				handle.del(e);
			}
		});
		$(window).resize(function() {
			Public.resizeGrid();
		});
	}
};
var handle = {
	del:function(e) {
		$.dialog.confirm("删除的入库通知单将不能恢复，请确认是否删除？", function() {
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
						content:"删除入库通知单失败！" + t.msg
					});
			});
		});
	},
};
THISPAGE.init();