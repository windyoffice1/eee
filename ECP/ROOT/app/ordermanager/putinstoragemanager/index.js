var url=rootPath+ "/ordermanager/putinstoragemanager", gridQryUrl = url+"/dataGrid.json",custParame=SYSTEM.custParame,parameList=custParame.typeList
var putinstorage_status=['待入库','已入库','入库中'];

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
			title:"查看入库情况",
/*			button:[ {
				name:"关闭"
			} ],*/
			resize:true,
			lock:true,
			content:"url:" + rootPath + "/?go=ordermanager/putinstoragemanager/view",
			data:{
				id:id
			}
		});
	},
	loadGrid:function() {
		function t(val, opt, row) {
			var html_con = '';
				html_con+='<div class="operating" data-id="'+ row.id+'">';
			if(row.putinstorage_status==1||row.putinstorage_status==2){ 
				html_con+='<span class="fa fa-eye mrb" title="查看"></span>';
			}			
			if(row.putinstorage_status==0||row.putinstorage_status==2){ 
				html_con+='<span class="fa fa-edit mrb" title="修改"></span>';
			}
			if(row.putinstorage_status==1){
				html_con+='<span class="fa fa-trash-o mrb" title="删除"></span></div>';
			}
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
				name:"putinstorage_status",
				label:"状态",
				align:"center",
				formatter:function(v){return putinstorage_status[v];},
				width:100,
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
				total:"data.totalPage",          //   很重要 定义了 后台分页参数的名字。
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
				THISPAGE.operate("edit", t);
			}
		});
		$("#search").click(function() {
			THISPAGE.reloadData();
		});
		$("#refresh").click(function() {
			model.query={putinstorage_name:"",create_user_id:""};
			THISPAGE.reloadData();
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
				THISPAGE.add('add');
			}
		});
		$(window).resize(function() {
			Public.resizeGrid();
		});
	},
	operate:function(e, t) {
		var i = "核对入库", r = {oper:e,id:t};
		$.dialog({title:i,content:"url:"+url+"/edit.html",
			data:r,width:1100,height :450,max :true,resize:true,	min :false,	cache :false,lock :true
		});
	},
	add:function(e){
		var i = "退料入库", r = {oper:e};
		$.dialog({title:i,content:"url:"+url+"/add.html",
			data:r,width:900,height :450,max :true,resize:true,	min :false,	cache :false,lock :true
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