function option3(data, chartWay){
	var legend = "", xAxisData = [], seriesData = [], unit = "";
	if(chartWay == 'volume'){
		legend = "销量";
		unit = "件";
	}else if(chartWay == 'amount'){
		legend = "销售额";
		
		unit = "万元";
	}else if(chartWay == 'count'){
		legend = "成交次数";
		
		unit = "次";
	}
	
	$.each(data, function(idx, d){
		xAxisData.push(d.tran_date);
		if(chartWay == 'volume'){
			seriesData.push(d.sales_volume);
		}else if(chartWay == 'amount'){
			seriesData.push(d.sales_amount);
		}else if(chartWay == 'count'){
			seriesData.push(d.tran_count);
		}
	});
	
	return {
		    title : {
		        text: legend + '走势图',
		        x: 'center',
		        y: 'top'
		    },
		    tooltip : {
		        trigger: 'axis'
		    },
		    legend:{
		    	data:[legend],
		    	x : 'center',
		        y : 'bottom'
		    },
		    toolbox: {
		        show : true,
		        feature : {
		            mark : {show: true},
		            dataView : {show: true, readOnly: false},
		            magicType : {show: true, type: ['line', 'bar']},
		            restore : {show: true},
		            saveAsImage : {show: true}
		        }
		    },
		    calculable : true,
		    xAxis : [
		        {
		            type : 'category',
		            data : xAxisData
		        }
		    ],
		    yAxis : [
	             {
	                 type : 'value',
	                 splitArea : {show : true},
	 	            axisLabel : {
	 	                formatter: '{value} '+unit
	 	            }
	             }
	         ],
		    series : [
		        {
		        	name:legend,
		            type:'bar',
		            data:seriesData,
		            barMaxWidth: 30
		        }
		    ]
		};
}

                    