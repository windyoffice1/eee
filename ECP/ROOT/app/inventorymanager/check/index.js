var url=rootPath+"/inventorymanager/check",gridQryUrl=url+"/dataGrid.json",status_status=["盘亏","盘盈","持平"];
var model = avalon.define({$id:'view',
	query:{warehouse_id:"",status:''},
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
	childWarehouseList:[],
	qryChildWarehouse:function(){
		Public.ajaxPost(rootPath+"/basicdata/childwarehouse/dataGrid.json", {type:0}, function(t) {
			console.info(t);
			if (t && 200 == t.status) {
				model.childWarehouseList=t.data.list;
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
		model.query={warehouse_id:"",status:''};
		model.reloadData();
	},
	loadGrid:function() {
			function t(val, opt, row) {
				console.info(row);
				var html_con = '<div class="operating" data-id="'+ row.id+'"><span class="fa fa-eye mrb" title="查看"></span>';
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
				fixed:true,width:100,
				formatter:t,
				align:"center",
				title:false
			}, {
				name:"check_name",
				label:"盘点名称",
				align:"center",
				width:320,
				sortable:true,
				title:false
			}, {
				name:"warehouse_name",
				label:"仓位名称",
				align:"center",
				width:100,sortable:true,
				title:false
			}, {
				name:"operate_user_name",
				label:"操作人",
				align:"center",
				width:100,
				sortable:true,
				title:false
			}, {
				name:"status",
				label:"状态",
				formatter:function(v){return status_status[v];},
				align:"center",
				width:100,
				sortable:true,
				title:false
			},{
				name:"deviation_total_money",
				label:"盈亏金额",
				align:"center",
				width:100,
				sortable:true,
				title:false
			},{
				name:"check_date",
				label:"盘点时间",
				align:"center",
				width:100,
				sortable:true,
				title:false
			}],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"check_date",
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
		$.dialog.confirm("删除的盘点将不能恢复，请确认是否删除？", function() {
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