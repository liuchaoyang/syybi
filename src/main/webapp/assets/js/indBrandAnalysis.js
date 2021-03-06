jQuery(function($) {

	// 选中navbar
	$('#syy-nav').find('.active').each(function() {
		var $class = 'active';
		if ($(this).hasClass('hover'))
			$class += ' open';

		$(this).removeClass($class);
	});
	$('#syy-industryAnalysis').addClass('active open');

	//============================================各品牌规模======================================================
	var scale_table = null;
	//行业规模检索
	$('#search-scale-btn').click(function(){
		
		//左侧类目树选中的行业或类目编号
		var selectedNo = $('#selected-no').val();
		if(!selectedNo){
			showMsg('请选择左侧行业');
		}else{
			//报表类型
			var reType = $('input[name="reType"]:checked').val();
			//选择月份
			var startMonth = $('#d4321').val();
			var endMonth = $('#d4322').val();
			//查看范围
			var shopType = $('input[name="shopType"]:checked').val();
			//图表类型
			var chartType = $('input[name="chartType"]:checked').val();
			
			var chartWay = $('input[name="chartWay"]:checked').val();
			if(chartType == 'bar'){
				$.get(global.path+'/a/IndustryAnalysis', {
					'catNo': selectedNo,
					'reType': reType,
					'startMonth': startMonth,
					'endMonth': endMonth,
					'shopType': shopType,
					'm': 'brand_scale',
					'chartWay': chartWay
				},function(data){
					
					$('#chartDiv').show();
					$('#tableDiv').hide();
					
					var width = data.data.length * 30 + 200;
					$('#echarts-scale').css('height', width+'px');
					
					//加载图表或数据表
					renderChart(option1(data.data, chartWay,'各品牌'),'echarts-scale');
					
				},'json');
				
			}else if(chartType == 'pie'){
				$.get(global.path+'/a/IndustryAnalysis', {
					'catNo': selectedNo,
					'reType': reType,
					'startMonth': startMonth,
					'endMonth': endMonth,
					'shopType': shopType,
					'm': 'brand_scale'
				},function(data){
					
					$('#chartDiv').show();
					$('#tableDiv').hide();
					
					$('#echarts-scale').css('height', '500px');
					
					//加载图表或数据表
					renderChart(option2_2(data.data, chartWay, '各品牌'),'echarts-scale');
					
				},'json');
			}else if(chartType == 'data'){
				
				$('#chartDiv').hide();
				$('#tableDiv').show();
				
				var scale_config = {};
				scale_config.tableId = 'scale-table';
				scale_config.url = global.path+'/a/IndustryAnalysis?m=brand_scale';
				scale_config.maxIndex = 6;
				scale_config.type = 'POST';//POST提交才能改变scale_config.data参数

				scale_config.data = function(d) {

					d.catNo = $('#selected-no').val();
					d.reType = $('input[name="reType"]:checked').val();
					d.startMonth = $('#d4321').val();
					d.endMonth = $('#d4322').val();
					d.shopType = $('input[name="shopType"]:checked').val();
					d.maxIndex = scale_config.maxIndex;

				};
				
				scale_config.columns = [
						{
							data : 'cat_name',
						},
						{
							data : 'sales_volume',
							fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
								$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
							}
						},
						{
							data : 'volumeWeight',
							fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
								$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
							}
						},
						{
							data : 'sales_amount',
							fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
								$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
							}
						},
						{
							data : 'amountWeight',
							fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
								$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
							}
						},
						{
							data : 'tran_count',
							fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
								$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
							}
						},
						{
							data : 'countWeight',
							fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
								$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
							}
						}];

				if(scale_table){
					scale_table.fnDraw();
				}else{
					scale_table = loadDataTable(scale_config);
				}
				
			}
		}
		
	});
	
	//切换销量、销售额、成交次数选项
	$('input[name="chartWay"]').click(function(){
		
		//图表类型
		var chartType = $('input[name="chartType"]:checked').val();
		var chartWay = $(this).val();
		if(chartType == 'bar'){
			ajaxfn = function(data){
				
				$('#chartDiv').show();
				$('#tableDiv').hide();
				
				var width = data.data.length * 30 + 200;
				$('#echarts-scale').css('height', width+'px');
				
				//加载图表或数据表
				renderChart(option1(data.data, chartWay, '各品牌'),'echarts-scale');
			};
			
		}else if(chartType == 'pie'){
			ajaxfn = function(data){
				
				$('#chartDiv').show();
				$('#tableDiv').hide();
				
				$('#echarts-scale').css('height', '500px');
				
				//加载图表或数据表
				renderChart(option2_2(data.data, chartWay, '各品牌'),'echarts-scale');
			};
		}
		
		$.get(global.path+'/a/IndustryAnalysis', {
			'catNo': $('#selected-no').val(),
			'reType': $('input[name="reType"]:checked').val(),
			'startMonth': $('#d4321').val(),
			'endMonth': $('#d4322').val(),
			'shopType': $('input[name="shopType"]:checked').val(),
			'm': 'brand_scale',
			'chartWay': $(this).val()
		},ajaxfn,'json');
		
	});
	
	//============================================各品牌趋势======================================================
	//行业趋势动态表格
	function buildTrendTable(){
		
		var startMonth = $('#d432112').val();
    	var endMonth = $('#d432222').val();
    	
    	$.get(global.path+'/a/IndustryAnalysis?m=brand_trend',{
    		'catNo': $('#selected-no').val(),
    		'startMonth': startMonth,
			'endMonth': endMonth,
			'shopType': $('input[name="shopType2"]:checked').val()
    	},function(data){
    		
    		$('#trend-table').empty();
    		
    		if(data && data.length > 0){
    			
    			var ms = getMonths(startMonth, endMonth);
    			
    			var thead = '';
    			
    			thead += '<thead>'
    				+'<tr role="row">'
    				+'<th rowspan="2" style="text-align:center">品牌名称</th>'
    				+'<th style="text-align:center">'+startMonth+'</th>';
    			
    			var colTds = '';
    			if(ms > 1){
    				for(var i = 1; i < ms; i++){
    					
    					var reMonth = addMonth(startMonth, i);
    					
    					thead += '<th colspan="2" style="text-align:center">'+reMonth+'</th>';
    					
    					colTds += '<th style="text-align:center">销量</th>'
    						+'<th style="text-align:center">环比</th>';
    				}
    			}
    			
    			thead += '<th style="text-align:center">总计</th>'
    				+'</tr>'
    				+'<th style="text-align:center">销量</th>'
    				+colTds
    				+'<th style="text-align:center">销量</th>'
    				+'</tr>'
    				+'</thead>';
    			
    			var tbody = '<tbody>';
    			
    			var startColTotal = 0, msRowTotal = [];
    			
    			//初始化数组
    			for(var i = 0; i < ms; i++){
    				msRowTotal[i] = 0;
    			}
    			
    			$.each(data, function(idx, d){
    				
    				tbody += '<tr role="row">'
    					+'<td style="text-align:center">'+d.cat_name+'</td>'
    					+'<td style="text-align:right">'+d['a'+startMonth.replace('-','')]+'</td>';
    				
    				startColTotal += parseInt(d['a'+startMonth.replace('-','')]);
    				msRowTotal[0] += parseInt(d['a'+startMonth.replace('-','')]);
    				
    				var colTds = '', msColTotal = 0;
    				for(var i = 1; i < ms; i++){
    					
    					var preMonth = addMonth(startMonth, i - 1);
    					var curMonth = addMonth(startMonth, i);
    					
    					//计算环比
    					var reObj = huanbi(d['a'+preMonth.replace('-','')], d['a'+curMonth.replace('-','')]);
    					
    					colTds += '<td style="text-align:right">'+d['a'+curMonth.replace('-','')]+'</td>'
    						+'<td style="text-align:right">'+reObj.huanbi+'%'+reObj.img+'</td>';
    					
    					msColTotal += parseInt(d['a'+curMonth.replace('-','')]);
    					
    					msRowTotal[i] += parseInt(d['a'+curMonth.replace('-','')]);
    				}
    				
    				tbody += colTds
    					+'<td style="text-align:right">'+(parseInt(d['a'+startMonth.replace('-','')]) + msColTotal)+'</td>'
    					+'</tr>';
    				
    			});
    			
    			tbody += '</tbody>';
    			
    			var tfoot = '';
    			tfoot += '<tfoot>'
    				+'<tr role="row">'
    				+'<th style="text-align:center">总计</th>'
    				+'<th style="text-align:right">'+startColTotal+'</th>';
    				
				var colTds = '', tfootTotal = msRowTotal[0];
				for(var i = 1; i < ms; i++){
					
					//计算环比
					var reObj = huanbi(msRowTotal[i-1], msRowTotal[i]);
					
					colTds += '<th style="text-align:right">'+msRowTotal[i]+'</th>'
						+'<th style="text-align:right">'+reObj.huanbi+'%'+reObj.img+'</th>';
					
					tfootTotal += msRowTotal[i];
				}
    				
				tfoot += colTds
				+'<th style="text-align:right">'+tfootTotal+'</th>'
				+'</tr>'
				+'</tfoot>';
    				
    			$('#trend-table').html(thead + tbody + tfoot);	
    			
    			//append datatable 样式
    			$('#trend-table').dataTable({
		    		paging: false,
		    		retrieve: true,
		    		//destroy: true,
		    		autoWidth: false,
		    		processing: true,
		    		language : dataTableConfig.language,
		    		dom : dataTableConfig.dom
		    	});
    		}
    		
    	},'json');
		
	}
	
	//生成折线图
	function buildTrendChart(){
		var startMonth = $('#d432112').val();
    	var endMonth = $('#d432222').val();
    	
    	$.get(global.path+'/a/IndustryAnalysis?m=brand_trend',{
    		'catNo': $('#selected-no').val(),
    		'startMonth': startMonth,
			'endMonth': endMonth,
			'shopType': $('input[name="shopType2"]:checked').val()
    	},function(data){
    		
    		var chartWay2 = $('input[name="chartWay2"]:checked').val();
    		
    		var ms = getMonths(startMonth, endMonth);
    		
    		renderChart(option5(data, chartWay2, startMonth, ms, '各品牌'),'echarts-trend');
    		
    	},'json');
	}
	
	//检索行业趋势
	$('#search-trend-btn').click(function(){
		
		var chartType2 = $('input[name="chartType2"]:checked').val();
		
		//左侧类目树选中的行业或类目编号
		var selectedNo = $('#selected-no').val();
		if(!selectedNo){
			showMsg('请选择左侧行业');
		}else{

			if(chartType2 == 'data'){
				
				$('#tableDiv2').show();
		    	$('#chartDiv2').hide();
		    	
		    	buildTrendTable();
				
			}else if(chartType2 == 'line'){
				
				$('#tableDiv2').hide();
		    	$('#chartDiv2').show();
		    	
		    	buildTrendChart();
				
			}
		}
		
	});
	
	$('input[name="chartWay2"]').click(function(){
		
		buildTrendChart();
		
	});
	//=================================================热销宝贝=============================================
	var goods_config = {};
	goods_config.tableId = 'goods-table';
	goods_config.url = global.path + '/a/IndustryAnalysis?m=brand_goods';
	goods_config.maxIndex = 9;

	goods_config.type = 'POST';

	goods_config.data = function(d) {

		d.catNo = $('#selected-no').val();
		d.startMonth = $('#d432113').val();
		d.endMonth = $('#d432223').val();
		d.shopType = $('input[name="shopType3"]:checked').val();
		
		d.maxIndex = goods_config.maxIndex;

	};
	goods_config.order = [[ 4, 'desc' ]];
	
	goods_config.initComplete = function (settings, json) {
		
		if(json.extList && json.extList.length > 0){
			
			var html = '';
			$.each(json.extList, function(idx, d){
				
				html += '<option value="'+d.adid+'">'+d.dir_name+'</option>';
				
			});
			
			$('#toDir').html(html);
		}
		
    };

	goods_config.columns = [
			{
				data : 'rowNum',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'center').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'prd_name',
				render : function(data, type, full, meta) {
					var html = '<img width="60" height="60" src="' + full.prd_img + '" alt="商品图片">' + ' <a target="_blank" href="' + full.prd_url
							+ '">' + data + '</a>';
					return html;
				}
			},
			{
				data : 'avg_price',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'avg_price_tran',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				},
				render : function(data, type, full, meta) {

					if (parseFloat(data) < parseFloat(full.avg_price)) {
						return data + '<img src="' + global.path + '/assets/img/down_arrow_new.gif">';
					} else if (data > full.avg_price) {
						return data + '<img src="' + global.path + '/assets/img/up_arrow_new.gif">';
					} else {
						return data;
					}
				}
			},
			{
				data : 'sales_volume',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'sales_amount',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'tran_count',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'shop_name',
				render : function(val, display, val_obj, prop) {
					var html = '<a target="_blank" href="' + val_obj.shop_url + '">' + val + '</a>';

					if (val_obj.shop_type == 'TMALL') {
						html = '<img src="' + global.path + '/assets/imagesLocal/bc_shop_icon.png">'
								+ ' <a target="_blank" href="' + val_obj.shop_url + '">' + val + '</a>';
					}

					return html;
				}
			},
			{
				data : 'region'
			},
			{
				data : 'item_id',
				searchable: false,
				orderable: false,
				render : function(val, display, val_obj,prop) {
					if($.trim(val_obj.asid) != '' ){
						return '已关注';
					}else{
						return '<label class="pos-rel">' + '<input type="checkbox" name="itemIds" value="' + val + "@" + val_obj.shop_id
						+ '" class="ace" />' + '<span class="lbl"></span>' + '</label>';
					}
					return '';
				},
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'center').css('vertical-align', 'inherit');
				}
			} ];

	// 初始加载
	var goods_table = null;//loadDataTable(goods_config);

	var active_class = 'active';
	$('#goods-table > thead > tr > th input[type=checkbox]').eq(0).on('click', function() {
		var th_checked = this.checked;// checkbox inside "TH" table
		// header

		$(this).closest('table').find('tbody > tr').each(function() {
			var row = this;
			if (th_checked)
				$(row).addClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', true);
			else
				$(row).removeClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', false);
		});
	});

	// select/deselect a row when the checkbox is checked/unchecked
	$('#goods-table').on('click', 'td input[type=checkbox]', function() {
		var $row = $(this).closest('tr');
		if (this.checked)
			$row.addClass(active_class);
		else
			$row.removeClass(active_class);
	});
	
	// 检索
	$('#search-goods-btn').click(function() {

		if($('#selected-no').val()){
    		if (goods_table) {
				goods_table.fnDraw();
			} else {
				goods_table = loadDataTable(goods_config);
			}
    	}

	});
	
	// 批量关注
	$('#batch-attn-btn').click(function() {
		
		var itemIds = [];
		
		$('input[name="itemIds"]:checked').each(function(){
			
			itemIds.push($(this).val());
			
		});

		if (itemIds.length == 0) {
			showMsg("至少选择一项");
			return;
		}
		
		$.post(global.path + '/a/GoodsAnalysis', {
			'itemIds' : itemIds.join(','),
			'adid' : $('#toDir').val(),
			'm' : "batch_attned"
		}, function(result) {

			if (result.status === '1') {
				showMsg("宝贝关注成功", function(){
					
					if (goods_table) {
						goods_table.fnDraw();
					}else{
						goods_table = loadDataTable(goods_config);
					}
					
				});
			} else {
				showMsg("宝贝关注失败");
			}
		}, 'json');

	});
	
	
	//=================================================热销店铺=============================================
	var shop_config = {};
	shop_config.tableId = 'shop-table';
	shop_config.url = global.path + '/a/IndustryAnalysis?m=brand_shop';
	shop_config.maxIndex = 6;

	shop_config.type = 'POST';

	shop_config.data = function(d) {

		d.catNo = $('#selected-no').val();
		d.startMonth = $('#d432114').val();
		d.endMonth = $('#d432224').val();
		d.shopType = $('input[name="shopType4"]:checked').val();
		
		d.maxIndex = shop_config.maxIndex;

	};

	shop_config.order = [[ 2, 'desc' ]];
	
	shop_config.columns = [
			{
				data : 'rowNum',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'center').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'shop_name',
				render : function(val, display, val_obj, prop) {
					var html = '<a target="_blank" href="' + val_obj.shop_url + '">' + val + '</a>';

					if (val_obj.shop_type == 'TMALL') {
						html = '<img src="' + global.path + '/assets/imagesLocal/bc_shop_icon.png">'
								+ ' <a target="_blank" href="' + val_obj.shop_url + '">' + val + '</a>';
					}

					return html;
				}
			},
			{
				data : 'sales_volume',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'sales_amount',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'tran_count',
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'right').css('vertical-align', 'inherit');
				}
			},
			{
				data : 'region'
			},
			{
				data : 'shop_id',
				searchable: false,
				orderable: false,
				render : function(val, display, val_obj,prop) {
					
					if($.trim(val_obj.asid) != '' ){
						return '已关注';
					}else{
						return '<label class="pos-rel">' + '<input type="checkbox" name="shopIds" value="' + val + "@" + val_obj.shop_name
						+ '" class="ace" />' + '<span class="lbl"></span>' + '</label>';
					}
					return '';
				},
				fnCreatedCell : function(nTd, sData, oData, iRow, iCol) {
					$(nTd).css('text-align', 'center').css('vertical-align', 'inherit');
				}
			} ];

	// 初始加载
	var shop_table = null;//loadDataTable(shop_config);

	var active_class = 'active';
	$('#shop-table > thead > tr > th input[type=checkbox]').eq(0).on('click', function() {
		var th_checked = this.checked;// checkbox inside "TH" table
		// header

		$(this).closest('table').find('tbody > tr').each(function() {
			var row = this;
			if (th_checked)
				$(row).addClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', true);
			else
				$(row).removeClass(active_class).find('input[type=checkbox]').eq(0).prop('checked', false);
		});
	});

	// select/deselect a row when the checkbox is checked/unchecked
	$('#shop-table').on('click', 'td input[type=checkbox]', function() {
		var $row = $(this).closest('tr');
		if (this.checked)
			$row.addClass(active_class);
		else
			$row.removeClass(active_class);
	});
	
	// 检索
	$('#search-shop-btn').click(function() {

		if($('#selected-no').val()){
    		if (shop_table) {
				shop_table.fnDraw();
			} else {
				shop_table = loadDataTable(shop_config);
			}
    	}

	});
	
	// 批量关注
	$('#batch-attn-btn2').click(function() {
		
		var shopIds = [];
		
		$('input[name="shopIds"]:checked').each(function(){
			
			shopIds.push($(this).val());
			
		});

		if (shopIds.length == 0) {
			showMsg("至少选择一项");
			return;
		}
		
		$.post(global.path + '/a/ShopAnalysis', {
			'shopIds' : shopIds.join(','),
			'm' : "batch_attned"
		}, function(result) {

			if (result.status === '1') {
				showMsg("店铺关注成功", function(){
					
					if (shop_table) {
						shop_table.fnDraw();
					} else {
						shop_table = loadDataTable(shop_config);
					}
					
				});
			} else {
				showMsg("店铺关注失败");
			}
		}, 'json');

	});
	
	
	//===================================切换tab===============================================
	//切换tab
	var curTabIdx = 1;
	
	$('#tab1, #tab2, #tab3, #tab4').on('shown.bs.tab', function (e) {
		
	    curTabIdx = e.target.id.replace('tab','');
	    
	    $('.breadcrumb .active:last').remove();
	    
	    if(curTabIdx == 1){
	    	
	    	$('.breadcrumb').append('<li class="active">各品牌规模 (子类报表)</li>');
	    	
	    	if($('#selected-no').val()){
	    		$('#tableDiv2').hide();
		    	$('#chartDiv2').show();
		    	
		    	buildTrendChart();
	    	}
	    	
	    }else if(curTabIdx == 2){
	    	
	    	$('.breadcrumb').append('<li class="active">各品牌趋势 (子类报表)</li>');
	    	
	    	if($('#selected-no').val()){
	    		
	    		$('#tableDiv2').show();
		    	$('#chartDiv2').hide();
		    	
		    	buildTrendTable();
	    		
	    	}
	    	
	    }else if(curTabIdx == 3){
	    	
	    	$('.breadcrumb').append('<li class="active">热销宝贝 (子类报表之热销宝贝)</li>');
	    	
	    	if($('#selected-no').val()){
	    		if (goods_table) {
					goods_table.fnDraw();
				} else {
					goods_table = loadDataTable(goods_config);
				}
	    	}
	    }else if(curTabIdx == 4){
	    	
	    	$('.breadcrumb').append('<li class="active">热销店铺 (子类报表之热销店铺)</li>');
	    	
	    	if($('#selected-no').val()){
	    		if (shop_table) {
					shop_table.fnDraw();
				} else {
					shop_table = loadDataTable(shop_config);
				}
	    	}
	    }
	});
	
});
