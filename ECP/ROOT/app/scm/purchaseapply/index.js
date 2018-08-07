var url=rootPath+"/scm/purchaseapply",gridQryUrl=url+"/dataGrid.json",
approve_status=["未提交","待审核","批准","驳回","已取回","已入库"];
var model = avalon.define({$id:'view',
	query:{purchase_no:"",approve_status:"",is_deleted:0,},
	fastQryText:"快速查询",
	fastQry:[
	           {text:"清空条件 ",sl:false},
		       {text:"未提交",sl:false},
		       {text:"待审核",sl:false},
		       {text:"已批准",sl:false},
		       {text:"驳回",sl:false},
		       {text:"已取回",sl:false},
		       {text:"已入库",sl:false},
		       {text:"",sl:true},
		       {text:"未删除",sl:false},
		       {text:"已删除",sl:false},
		       ],
	qry:function(type){
		model.query.qryType=type;
		if(type==0){
			model.query.approve_status="";
			model.query.is_deleted="";
		}else if(type==8){
			model.query.is_deleted=0;
			model.query.approve_status="";
		}else if(type==9){//支付情况
			model.query.is_deleted=1;
			model.query.approve_status="";
		}else{
			model.query.approve_status=type-1;
			model.query.is_deleted="";
		}
		model.fastQryText=model.fastQry[type].text;
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
						if(row.approve_status==0||row.approve_status==3 ||row.approve_status==4){ //未提交或拒绝或已取回时可以修改和删除
							html_con+='<span class="fa fa-edit mrb" title="修改"></span>'+
									'<span class="fa fa-trash-o mrb" title="删除"></span>';
						}
						if ((row.approve_status ==0||row.approve_status ==3||row.approve_status==4)&&(row.creater_id==SYSTEM.user.id||row.update_id==SYSTEM.user.id)){//未提交或已取回或驳回可以审核
							html_con+='<a href="#" class="fa mrb subAudit" title="提交审核">提交审核</a>';
						}else if (row.approve_status ==1&&(row.creater_id==SYSTEM.user.id||row.update_id==SYSTEM.user.id)){//取消提交审核
							html_con+='<a href="#" class="fa mrb disSubAudit" title="取消审核">取消审核</a>';
						}else if (row.approve_status ==2){//入库通知单
							html_con+='<a href="#" class="fa mrb putInStorage" title="入库">入库</a>';
						}else if (row.approve_status ==5&&row.is_deleted==0){//已入库的可以删除[修改申请单状态为已删除]
							html_con+='<span class="fa fa-trash-o mrb" title="删除"></span>';
						}/*else if (row.approve_status ==1&&row.auditor_id==SYSTEM.user.id){
							html_con+='<a href="#" class="fa mrb audit" title="审核">审核</a>';
						}*/
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
				width:230,
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
	d_close:function(){
		alert("dsakj");
		$('#dialog1').dialog('close');
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
				var total_money = $(this).parent().data("total_money");
				
				model.subAudit(t,total_money);
			}
		});
		$(".grid-wrap").on("click", ".disSubAudit", function(e) {
			e.preventDefault();
			if (Business.verifyRight("TD_UPDATE")) {
				var t = $(this).parent().data("id");
				model.disSubAudit(t);
			}
		});
		//入库
		$(".grid-wrap").on("click", ".putInStorage", function(e) {
			e.preventDefault();
			if (Business.verifyRight("TD_UPDATE")) {
				var t = $(this).parent().data("id");
				model.putInStorage(t);
			}
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
			$.dialog({
				title:i,
				content:"url:"+url+"/edit.html",
				data:r,
				width:900,
				height:500,
				max:true,
				resize:true,
				min:true,	
				cache :false,
				lock :true
			});
	},
	view:function(id){
		$.dialog({
			id:"dialog1",
			width:900,
			height:500,
			min:true,
			max:true,
			title:"查看",
			button:[{name:"关闭"}],
			resize:true,
			lock:true,
			cache:false,
			content:"url:"+url+"/view.html",
			data:{id:id}
		});
	},
	audit:function(id){
		$.dialog({id:"dialog1",width:900,height :500,min:true,max:true,
			title:"审核"+order_type[type],resize:true,lock:true,
			content:"url:"+url+"/audit.html",data:{id:id}});
	},
	putInStorage:function(id){
		$.dialog({id:"dialog1",width:1100,height :500,min:true,max:true,
			title:"入库",resize:true,lock:true,
			content:"url:"+url+"/putinstorage.html",data:{id:id}});
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
			Public.ajaxPost(url+"/subAudit.json", {id:id,total_money:total_money,status:1}, function(t) {
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
				parent.Public.tips({type:1,content:"取消审核失败！" + t.msg});
			}
		});
		});
	}
});

model.init();