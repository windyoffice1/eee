var url=rootPath+"/approve/returnmaterialapplyapprove",gridQryUrl=url+"/dataGrid.json",custParame=SYSTEM.custParame,typeList=custParame.typeList,
approve_status=["未提交","待审核","通过","拒绝"];
var model = avalon.define({$id:'view',
	query:{returnmaterial_no:""},
	parameList:typeList,
	init:function() {
		this.loadGrid();
		this.addEvent();
	},
	resetQry:function(){
		model.query={returnmaterial_no:""};
		model.reloadData();
	},
	loadGrid:function() {
			function t(val, opt, row) {
				var html_con = '<div class="operating" data-id="'+ row.id+'" data-total_money="'+ row.total_money+'"><span class="fa fa-eye mrb" title="查看"></span>';
					html_con+='<a href="#" class="fa mrb audit" title="审核">审核</a>';
					html_con+='<a href="#" class="fa mrb flow" title="流程图">流程图</a>';
				html_con+='</div>';
				return html_con;
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
			gridview:true,rownumbers:true,
			multiselect:true,
			multiboxonly:true,
			colModel:[ {
				name:"operating",
				label:"操作",
				fixed:true,width:150,
				formatter:t,
				align:"center",
				title:false
			}, {
				name:"child_warehouse_name",
				label:"退料单位",
				align:"center",
				width:150,sortable:true,
				title:false
			}, {
				name:"returnmaterial_no",
				label:"申请表编号",
				align:"center",
				width:350,sortable:true,
				title:false
			}, {
				name:"returnmaterial_name",
				label:"申请表名称",
				align:"center",
				width:350,sortable:true,
				title:false
			}, {
				name:"total_money",
				label:"总金额",sortable:true,
				align:"center",
				width:100,
				title:false
			}, {
				name:"create_user_name",
				label:"创建人",
				align:"center",sortable:true,
				width:100,
				title:false
			}, {
				name:"approve_status",
				label:"审核状态",
				formatter:function(v){return approve_status[v];},
				align:"center",
				width:100,sortable:true,
				title:false
			}, {
				name:"create_time",
				label:"创建时间",sortable:true,
				align:"center",
				width:180,
				title:false
			} ],
			cmTemplate:{
				sortable:false,
				title:false
			},
			page:1,
			sortname:"create_time",
			sortorder:"desc",
			pager:"#page",
			rowNum:500,
			rowList:[ 500,1000, 2000 ],
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
		$(".grid-wrap").on("click", ".audit", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			model.audit(e);
		});
		//流程图
		$(".grid-wrap").on("click", ".flow", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			model.flow(e);
		});
	},
	view:function(id){
		$.dialog({id:"dialog1",width:1100,height :500,min:true,max:true,
			title:"查看",button:[{name:"关闭"	} ],resize:true,lock:true,
			content:"url:"+url+"/view.html",data:{id:id}});
	},
	audit:function(id){
		$.dialog({id:"dialog1",width:1100,height :500,min:true,max:true,
			title:"审核",resize:true,lock:true,resize:true,lock:true,
			content:"url:"+url+"/audit.html",data:{id:id}});
	},
	flow:function(id){
		$.dialog({id:"dialog1",width:900,height :500,min:true,max:true,
			title:"流程图",resize:true,lock:true,resize:true,lock:true,
			content:"url:"+url+"/flow.html",data:{id:id}});
	},
});
model.init();