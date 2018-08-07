var url=rootPath+ "/inventorymanager/inventorywarning", gridQryUrl = url+"/dataGrid.json",
custParame=SYSTEM.custParame,parameList=custParame.typeList;

var model = avalon.define({$id:'view',
	query:{
		material_name:"",material_no:""
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
			content:"url:" + rootPath + "/?go=basicdata/materialdata/view",
			data:{
				id:id
			}
		});
	},
	loadGrid:function() {
		function t(val, opt, row) {
			var html_con = '<div class="operating" data-id="'
					+ row.id
					+ '"><span class="fa fa-eye mrb" title="查看"></span>';
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
				name:"belong_to_broad_name",
				label:"所属大类",
				align:"center",
				width:100,
				sortable:true,
				title:true
			}, {
				name:"material_no",
				label:"物料编号",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"material_name",
				label:"物料名称",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"model_number",
				label:"规格型号",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"existing_amount",
				label:"现有数量",
				align:"center",
				width:100,
				sortable:true,
				title:true
			},{
				name:"inventory_warning_amount",
				label:"库存预警数量",
				align:"center",
				width:120,
				sortable:true,
				title:true
			},{
				name:"unit",
				label:"单位名称",
				align:"center",formatter:function(v,e,r){return parent.SYSTEM.custParame[v].name;},
				width:100,
				sortable:true,
				title:true
			},{
				name:"target_price",
				label:"计划价格",
				width:120,
				formatter:fmtAmt,
				align : "right",
				title : false
			},{
				name:"float_rate",
				label:"浮动比例",
				width:120,
				align : "right",
				title : false
			},{
				name:"float_price",
				label:"浮动价格",
				width:200,
				align : "right",
				title : false
			},{
				name:"average_price",
				label:"平均价格",
				width:120,
				formatter:fmtAmt,
				align : "right",
				title : false
			},{
				name:"keeper_name",
				label:"保管员",
				align:"center",
				width:120,
				sortable:true,
				title:true
			}],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"material_no",
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
	}
};
THISPAGE.init();