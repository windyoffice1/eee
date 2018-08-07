var url=rootPath+"/inventorymanager/transfer",gridQryUrl=url+"/dataGrid.json";
var model = avalon.define({$id:'view',
	query:{from_warehouse_id:"",target_warehouse_id:""},
	warehouseList:[],
	qryWarehouse:function(){
		Public.ajaxPost(rootPath+"/basicdata/warehouselocation/dataGrid.json", {rows:999999}, function(t) {
			if (t && 200 == t.status) {
				model.warehouseList=t.data.list;
			} else{
				parent.Public.tips({type:1,content: t.msg});
			}
		});
	},
	init:function() {
		this.loadGrid();
		this.addEvent();
		model.qryWarehouse();
	},
	resetQry:function(){
		model.query={from_warehouse_id:"",target_warehouse_id:""};
		model.reloadData();
	},
	loadGrid:function() {
			function t(val, opt, row) {
				var html_con = '<div class="operating" data-id="'+ row.id+'"><span class="fa fa-eye mrb" title="查看"></span>';
					//html_con+='<span class="fa fa-edit mrb" title="修改"></span>'+
					html_con+='<span class="fa fa-trash-o mrb" title="删除"></span>';
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
			gridview:true,rownumbers:true,
			multiselect:true,
			multiboxonly:true,
			colModel:[ {
				name:"operating",
				label:"操作",
				fixed:true,width:180,
				formatter:t,
				align:"center",
				title:false
			}, {
				name:"from_warehouse_name",
				label:"原属仓位",
				align:"center",
				width:100,sortable:true,
				title:false
			}, {
				name:"target_warehouse_name",
				label:"目标仓位",
				align:"center",
				width:100,sortable:true,
				title:false
			}, {
				name:"transfer_total_money",
				label:"总金额",
				align:"center",
				width:100,
				formatter:fmtAmt,
				sortable:true,
				title:false
			}, {
				name:"operate_date",
				label:"移位日期",
				align:"center",
				width:100,
				sortable:true,
				title:false
			},{
				name:"operate_user_name",
				label:"操作人",sortable:true,
				align:"center",
				width:100,
				title:false
			} ],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"operate_date",
			sortorder:"desc",
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
				model.view(t);
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
		Public.dateCheck();
		var t = this;
		$(".grid-wrap").on("click", ".fa-eye", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			model.view(e);
		});
		$(".grid-wrap").on("click", ".fa-edit", function(e) {
			e.preventDefault();
			if (Business.verifyRight("TD_UPDATE")) {
				var t = $(this).parent().data("id");
				model.operate("edit", t);
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
});
model.init();