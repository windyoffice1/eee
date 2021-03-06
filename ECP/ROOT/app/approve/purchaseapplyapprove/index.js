var url=rootPath+"/approve/purchaseapplyapprove",gridQryUrl=url+"/dataGrid.json",custParame=SYSTEM.custParame,typeList=custParame.typeList,
approve_status=["未提交","待审核","通过","拒绝"];
var model = avalon.define({$id:'view',
	query:{purchase_no:""},
	parameList:typeList,
	fastQryText:"快速查询",
	fastQry:[
		       {text:"我创建的",sl:false},
		       {text:"我负责的",sl:false},
		       {text:"下属创建的",sl:false},
		       {text:"下属负责的",sl:false},
		       {text:"回收站",sl:false},
		       {text:"",sl:true},
		       {text:"未支付",sl:false},
		       {text:"已支付",sl:false},
		       {text:"已结算",sl:false}
		       ],
	qry:function(type){
		model.query.qryType=type;
		if(type==4){
			model.query.is_deleted=1;
			model.query.qryType=-1;//查看自己的回收站信息
			model.fastQryText="回收站";
			model.query.pay_status="";
		}else if(type>5){//支付情况
			model.fastQryText=model.fastQry[type].text;
			model.query.pay_status=type-6;
			model.query.qryType=5;
			model.query.is_deleted=0;
		}else{
			model.fastQryText=model.fastQry[type].text;
			model.query.is_deleted=0;
			model.query.pay_status="";
		}
		model.reloadData();
	},
	init:function() {
		/*$(".ui-datepicker-input").datepicker();*/
		this.loadGrid();
		this.addEvent();
	},
	resetQry:function(){
		model.query={purchase_no:""};
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
				name:"purchase_no",
				label:"申请表单号",
				align:"center",
				width:350,sortable:true,
				title:false
			}, {
				name:"purchase_name",
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
				name:"creater_username",
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
/*				console.info($(this).parent().data("id"));
				console.info($(this).parent().data("total_money"));
				console.info($(this).parent());*/
				var total_money = $(this).parent().data("total_money");
				
				model.subAudit(t,total_money);
			}
		});
		//流程图
		$(".grid-wrap").on("click", ".flow", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			model.flow(e);
		});
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
		$(".grid-wrap").on("click", ".fa-reply", function(t) {
			t.preventDefault();
				var e = $(this).parent().data("id");
					model.reply(e);
		});
		$(".grid-wrap").on("click", ".submit", function(t) {
			t.preventDefault();
			var e = $(this).parent().data("id");
			model.submit(e);
		});
		$("#add").click(function(t) {
			t.preventDefault();
			if(Business.verifyRight("TF_ADD")){
				model.operate('add');
			}
		});
		$("#btn-batchDel").click(function(e) {
			e.preventDefault();
			if (Business.verifyRight("BU_DELETE")) {
				var t = $("#grid").jqGrid("getGridParam", "selarrrow");
				t.length ? 
						((model.query.qryType==4)?model.del(t.join()):model.trash(t.join()))
						:parent.Public.tips({
					type:2,
					content:"请选择需要删除的项"
				});
			}
		});
		$("#btn-batchReply").click(function(e) {
			e.preventDefault();
				var t = $("#grid").jqGrid("getGridParam", "selarrrow");
				if(t.length){
						model.reply(t.join());
				}else
					parent.Public.tips({type:2,content:"请选择需要恢复的"+order_name});
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
		$.dialog({id:"dialog1",width:1100,height :500,min:true,max:true,
			title:"查看",button:[{name:"关闭"	} ],resize:true,lock:true,
			content:"url:"+url+"/view.html",data:{id:id}});
	},
	audit:function(id){
		$.dialog({id:"dialog1",width:1100,height :450,min:true,max:true,
			title:"审核",resize:true,lock:true,resize:true,lock:true,
			content:"url:"+url+"/audit.html",data:{id:id}});
	},
	flow:function(id){
		$.dialog({id:"dialog1",width:900,height :500,min:true,max:true,
			title:"流程图",resize:true,lock:true,resize:true,lock:true,
			content:"url:"+url+"/flow.html",data:{id:id}});
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
	trash:function(e) {
			Public.ajaxPost(url + "/trash", {id:e}, function(t) {
				if (t && 200 == t.status) {
					parent.Public.tips({type:2,content:t.msg});
					model.reloadData();
				} else
					parent.Public.tips({type:1,content:"删除"+order_name+"失败！" + t.msg});
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
					parent.Public.tips({type:1,content:"删除"+order_type[type]+"失败！请检查是否被引用！" + t.msg});
				}
			});
		});
	},
	submit:function(id) {
		$.dialog.confirm("提交"+order_type[type]+"后将不能修改，且生成应收应付单，请确认是否提交？", function() {
			Public.ajaxPost(url+"/submit.json", {id:id}, function(t) {
				if (t && 200 == t.status) {
					parent.Public.tips({type:2,content:t.msg});
					model.reloadData();
				} else{
					parent.Public.tips({type:1,content:"提交"+order_type[type]+"失败！" + t.msg});
				}
			});
		});
	},
	subAudit:function(id,total_money) {
			Public.ajaxPost(url+"/subAudit.json", {id:id,total_money:total_money}, function(t) {
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
				parent.Public.tips({type:1,content:"取消"+order_type[type]+"审核失败！" + t.msg});
			}
		});
		});
	}
});
model.init();