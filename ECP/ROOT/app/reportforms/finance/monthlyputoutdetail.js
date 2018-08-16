var url = rootPath + "/reportforms/finance", gridQryUrl = url
		+ "/putInAndOutMonthlyStatement.json", custParame = SYSTEM.custParame, typeList = custParame.typeList;
var model = avalon.define({
	$id : 'view',
	user : SYSTEM.user,
	parameList : typeList,
	errMsg : "",
	query : {
		year : "",
		month : "",
		material_data_no : "",
		material_data_name : ""
	},
	year : [],
	setYear : function() {
		var date = new Date();
		var nowYear = date.getFullYear();
		for (var beginYear = 2017; beginYear <= nowYear; beginYear++) {
			model.year.push(beginYear);
		}
		model.query.year = nowYear;
	},
	month : [],
	setMonth : function() {
		var date = new Date();
		var nowYear = date.getFullYear();
		var nowMonth = date.getMonth();
		// 清除
		model.month.splice(0, model.month.length);
		if (model.query.year == nowYear) {
			for (var beginMonth = 1; beginMonth <= nowMonth + 1; beginMonth++) {
				model.month.push(beginMonth);
			}
		} else {
			for (var beginMonth = 1; beginMonth <= 12; beginMonth++) {
				model.month.push(beginMonth);
			}
		}
		model.query.month = nowMonth + 1;
	},
	list : [],
	nprint : true,
	productList : [],
	init : function() {
		model.setYear();
		model.setMonth();
	},
	// 导出
	excelExport : function() {
		$(".table").table2excel({
			exclude : ".noExl",
			name : "Excel Document Name",
			filename : "收发结存明细表.xls",
			fileext : "",
			exclude_img : true,
			exclude_links : true,
			exclude_inputs : true
		});
	},
	resetQry : function() {
		var date = new Date();
		var nowYear = date.getFullYear();
		var nowMonth = date.getMonth();
		model.query = {
			year : nowYear,
			month : nowMonth + 1,
			material_data_no : "",
			material_data_name : "",
		};
	},
	loadData : function() {
		model.errMsg ="";
		if(model.query.material_data_no=="" && model.query.material_data_name ==""){
			model.errMsg = "物料编号或物料名称必填其一";
			return ;
		}
		Public.ajaxPost(gridQryUrl, model.query.$model, function(json) {
			if (json.status == 200) {
				model.list=[];
				model.list = json.data;
			}
		});
	},
	changeMonth:function(){
		if(model.query.material_data_no!="" || model.query.material_data_name !=""){
			model.loadData();
		}
	},
	printRpt : function() {
		model.nprint = false;
		window.print();
		model.nprint = true;
	}
});
model.init();