package com.sanyanyu.syybi.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sanyanyu.syybi.constants.FinalConstants;
import com.sanyanyu.syybi.entity.AdAnalysis;
import com.sanyanyu.syybi.entity.AdvertBase;
import com.sanyanyu.syybi.entity.AdvertCu;
import com.sanyanyu.syybi.entity.AdvertHot;
import com.sanyanyu.syybi.entity.AdvertJu;
import com.sanyanyu.syybi.entity.AdvertTaoke;
import com.sanyanyu.syybi.entity.AdvertZTC;
import com.sanyanyu.syybi.entity.AttnDir;
import com.sanyanyu.syybi.entity.AttnDirDetail;
import com.sanyanyu.syybi.entity.CatApi;
import com.sanyanyu.syybi.entity.CatData;
import com.sanyanyu.syybi.entity.ChngName;
import com.sanyanyu.syybi.entity.ChngPrice;
import com.sanyanyu.syybi.entity.DirEntity;
import com.sanyanyu.syybi.entity.GoodsList;
import com.sanyanyu.syybi.entity.HotGoods;
import com.sanyanyu.syybi.entity.PageEntity;
import com.sanyanyu.syybi.entity.PageParam;
import com.sanyanyu.syybi.entity.PriceTrend;
import com.sanyanyu.syybi.entity.SaleShop;
import com.sanyanyu.syybi.entity.ScalpEntity;
import com.sanyanyu.syybi.entity.ShopRate;
import com.sanyanyu.syybi.entity.ShopSearch;
import com.sanyanyu.syybi.utils.DateUtils;
import com.sanyanyu.syybi.utils.StringUtils;
import com.sanyanyu.syybi.utils.SysUtil;

/**
 * 宝贝分析Service
 * 
 * @Description: TODO
 * @author Ivan 2862099249@qq.com
 * @date 2015年7月3日 下午4:34:54
 * @version V1.0
 */
public class GoodsService extends BaseService {

	
	/**
	 * 刷单分析的关注的店铺列表
	 * @param pageParam
	 * @param uid
	 * @param shopName
	 * @return
	 * @throws Exception
	 */
	public PageEntity<ScalpEntity> getShopList(PageParam pageParam, String uid, String shopName) throws Exception{
		
		List<Object> params = new ArrayList<Object>();
		params.add(DateUtils.getCurMonth());
		params.add(DateUtils.getOffsetMonth(-1, "yyyy-MM"));
		params.add(uid);
		
		StringBuffer sql = new StringBuffer();
		sql.append("SELECT t2.shop_id,t2.shop_img,t2.shop_name,t2.shop_type,t1.tag,t3.rise_index,t2.item_count,t2.region,t3.sales_volume,")
		.append(" t4.sales_volume as sales_volume_pre,t3.sales_amount,t4.sales_amount as sales_amount_pre,t1.att_date from tbweb.tb_attn_shop t1")
		.append(" join tbbase.tb_base_shop t2 on t1.shop_id = t2.shop_id")
		.append(" left join tbdaily.tb_tran_month_shop t3 on t1.shop_id = t3.shop_id and t3.tran_month = ?")
		.append(" left join tbdaily.tb_tran_month_shop t4 on t1.shop_id = t4.shop_id and t4.tran_month = ?")
		.append(" where t1.uid = ? and t1.att_type = 1");
		
		if(StringUtils.isNotBlank(shopName)){
			sql.append(" and t2.shop_name = ?");
			params.add(shopName);
		}
		
		List<ScalpEntity> list = sqlUtil.searchList(ScalpEntity.class, pageParam.buildSql(sql.toString()), params.toArray());
		
		return PageEntity.getPageEntity(pageParam, list);
	} 
	
	/**
	 * 搜索用户已关注的店铺
	 * 
	 * @param uid
	 * @param q
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAttnedGoods( String adid, String q) throws Exception {

		String sql = "select t2.prd_name from tbweb.tb_attn_dir_detail t1"
				+" join tbbase.tb_base_product t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
				+" where t1.adid = ? and t2.prd_name like '" + q + "%'";
		
		return sqlUtil.searchList(sql, adid);

	}
	
	public List<Map<String, Object>> getAttnedShop(String uid) throws Exception {

		String sql = "SELECT shop_id, shop_name FROM tbweb.tb_attn_shop where uid = ? and att_type = 1";

		return sqlUtil.searchList(sql, uid);

	}
	
	public void updateDetail(String adid, String itemIds, String toAdid) throws Exception{
		
		String sql = "update tbweb.tb_attn_dir_detail set adid = ? where adid = ? and concat_ws('_', shop_id, item_id) in ("+StringUtils.strIn(itemIds)+")";
		
		sqlUtil.update(sql, toAdid, adid);
		
	}

	public Map<String, Object> getShopIdByItemId(String itemId) throws Exception{
		
		String sql = "select shop_id from tbbase.tb_base_product where item_id = ?";
		
		return sqlUtil.search(sql, itemId);
	}
	
	
	public boolean getGoodsExist(String adid, String shopId, String itemId) throws Exception{
		
		String sql = "select count(0) as cnt from tbweb.tb_attn_dir_detail where adid = ? and shop_id = ? and item_id = ?";
		
		Map<String, Object> map = sqlUtil.search(sql, adid, shopId, itemId);
		
		int cnt = StringUtils.toInteger(map.get("cnt"));
		
		return cnt > 0;
	}
	
	public void saveGoods(AttnDirDetail detail) throws Exception{
		
		sqlUtil.insert(detail);
		
	}
	
	/**
	 * 判断目录是否存在
	 * @param uid
	 * @param dirName
	 * @return
	 * @throws Exception
	 */
	public boolean getAttnedExist(String uid, String dirName) throws Exception {

		String sql = "SELECT count(0) as cnt FROM tbweb.tb_attn_dir where uid = ? and dir_name = ?";

		Map<String, Object> baseMap = sqlUtil.search(sql, uid, dirName);
		int baseCount = StringUtils.toInteger(baseMap.get("cnt"));
		return baseCount > 0;
	}

	/**
	 * 新增关注目录
	 * @param uid
	 * @param dirName
	 * @return
	 * @throws Exception
	 */
	public String attnedDir(String uid, String dirName) throws Exception {

		boolean exist = getAttnedExist(uid, dirName);
		if (exist) {
			return "exist";
		} else {// 存在才能新增
			
			AttnDir dir = new AttnDir();
			dir.setAdid(SysUtil.getUUID());
			dir.setDir_name(dirName);
			dir.setUid(uid);
			sqlUtil.insert(dir);

			return "success";
		}

	}
	
	/**
	 * 宝贝分析-目录一览
	 * @param uid
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<DirEntity> getDirList(String uid, PageParam pageParam, String dirName) throws Exception{
		
		String curMonth = DateUtils.getCurMonth();
		String preMonth = DateUtils.getOffsetMonth(-1, "yyyy-MM");
		
		String sql = "select * from ("
				 +" select dir_name, adid, count(t3.item_id) as item_count, round(avg(t4.avg_price),2) as avg_price, round(avg(t4.avg_price_tran),2) as avg_price_tran, round(avg(t5.avg_price_tran),2) as avg_price_tran_pre,"
				 +" sum(t4.sales_volume) as sales_volume, sum(t5.sales_volume) as sales_volume_pre, sum(t5.sales_amount) as sales_amount_pre from ("
				 +" select t1.dir_name, t1.adid, t2.shop_id, t2.item_id from tbweb.tb_attn_dir t1 "
				 +" left join tbweb.tb_attn_dir_detail t2 on t1.adid = t2.adid"
				 +" where t1.uid = ?) t3"
				 +" left join tbdaily.tb_tran_month t4 on t3.shop_id = t4.shop_id and t3.item_id = t4.item_id and t4.tran_month = ?"
				 +" left join tbdaily.tb_tran_month t5 on t3.shop_id = t5.shop_id and t3.item_id = t5.item_id and t5.tran_month = ?"
				 +" group by dir_name, adid) t";
		
		if(StringUtils.isNotBlank(dirName)){
			sql += " where dir_name = '"+dirName+"'";
		}
		
		List<DirEntity> list = sqlUtil.searchList(DirEntity.class, pageParam.buildSql(sql), uid, curMonth, preMonth);
		
		return PageEntity.getPageEntity(pageParam, list);
		
	}
	
	/**
	 * 批量关注
	 * @param uid
	 * @param adid
	 * @param itemIds
	 * @throws Exception
	 */
	public void attnedGoods(String uid, String adid, String itemIds) throws Exception {

		String[] itemIdArr = itemIds.split(",");
		
		List<AttnDirDetail> list = new ArrayList<AttnDirDetail>();
		for(String itemId : itemIdArr){
			
			String[] iArr = itemId.split("@");
			
			AttnDirDetail detail = new AttnDirDetail();
			
			detail.setAddid(SysUtil.getUUID());
			detail.setAdid(adid);
			detail.setItem_id(iArr[0]);
			detail.setShop_id(iArr[1]);
			
			list.add(detail);
		}
		sqlUtil.batchInsert(AttnDirDetail.class, list);
	}

	/**
	 * 获取可以关注的店铺
	 * 
	 * @param uid
	 * @param q
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAttnShop(String uid, String q) throws Exception {

		String sql = "SELECT shop_id, shop_name FROM tbbase.tb_base_shop t1 where t1.shop_name <> '' and t1.shop_name like '"
				+ q
				+ "%'"
				+ " and not exists (select 'X' from tbweb.tb_attn_shop t2 where t1.shop_id = t2.shop_id and t2.uid = ? and t2.att_type = 1 ) order by t1.shop_name limit 50";

		return sqlUtil.searchList(sql, uid);

	}

	/**
	 * 判断是否满足删除关注的店铺的条件（添加一个月后才能删除）
	 * 
	 * @param uid
	 * @param shopIds
	 * @return
	 * @throws Exception
	 */
	public boolean enabledDel(String adid, String itemIds) throws Exception {

		String sql = "select count(0) as cnt from tbweb.tb_attn_dir_detail where adid = ? and concat_ws('_', shop_id, item_id) in (" + StringUtils.strIn(itemIds)
				+ ") and str_to_date(att_date, '%Y-%m-%d') > date_sub(curdate(), interval 1 month)";

		Map<String, Object> map = sqlUtil.search(sql, adid);

		int cnt = StringUtils.toInteger(map.get("cnt"));

		return cnt == 0;
	}

	/**
	 * 删除关注目录
	 * @param uid
	 * @param dirIds
	 * @throws Exception
	 */
	public void delAttn(String uid, String dirIds) throws Exception {

		String sql = "delete FROM tbweb.tb_attn_dir where adid in (" + StringUtils.strIn(dirIds) + ") and uid = ?";

		sqlUtil.delete(sql, uid);

	}
	
	public void delAttnGoods(String adid, String itemIds) throws Exception {

		String sql = "delete FROM tbweb.tb_attn_dir_detail where concat_ws('_', shop_id, item_id) in (" + StringUtils.strIn(itemIds) + ") and adid = ?";

		sqlUtil.delete(sql, adid);

	}

	/**
	 * 用户的关注目录
	 * @param uid
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getAttnedDirs(String uid) throws Exception{
		
		String sql = "SELECT adid, dir_name FROM tbweb.tb_attn_dir where uid = ?";
		
		return sqlUtil.searchList(sql, uid);
		
	}
	
	public List<Map<String, Object>> getAttnedDirs(String uid, String adid) throws Exception{
		
		String sql = "SELECT adid, dir_name FROM tbweb.tb_attn_dir where uid = ? and adid <> ?";
		
		return sqlUtil.searchList(sql, uid, adid);
		
	}
	
	public Map<String, Object> getPriceMinMax(String adid) throws Exception{
		
		String curDate = DateUtils.getDate();
		String preDate = DateUtils.getLastMonthDate();
		
		String sql = "select min(t1.avg_price) as avg_price_min, max(t1.avg_price) as avg_price_max from tbdaily.tb_tran_day t1" 
				+" where exists (select 'X' from tbweb.tb_attn_dir_detail t2 where t2.adid = ? and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id)"
				+" and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		return sqlUtil.search(sql, adid, preDate, curDate);
		
	}
	
	/**
	 * 宝贝分析-价格段分析
	 * @param adid
	 * @param startDate
	 * @param endDate
	 * @param shopType
	 * @param startAvgPrice
	 * @param endAvgPrice
	 * @param priceMap
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> getPriceAnalysis(String adid, String startDate, String endDate, String shopType, String startAvgPrice, String endAvgPrice, List<Map<String, String>> priceList) throws  Exception{
		
		StringBuffer cols1 = new StringBuffer();
		StringBuffer cols2 = new StringBuffer();
		StringBuffer cols3 = new StringBuffer();
		
		for(int i = 0; i < priceList.size(); i++){
			
			Map<String, String> map = priceList.get(i);
			
			String start = map.get("start").toString();
			String end = map.get("end").toString();
			
			String priceSeg = "p" + start + "-" + end;
			
			cols1.append("sum(`").append(priceSeg).append("`)").append(" as `").append(priceSeg).append("`");
			
			cols2.append("if(t.price_count = '").append(priceSeg).append("', sum(t.sales_amount), 0) as `").append(priceSeg).append("`");
			
			cols3.append("when (t2.avg_price >=" ).append(start).append(" and t2.avg_price <= ").append(end).append(") then '").append(priceSeg).append("'");
			
			if(i != priceList.size() - 1){
				
				cols1.append(",");
				cols2.append(",");
				cols3.append(" ");
			}
			
		}
		
		StringBuffer sql = new StringBuffer();
		
		List<Object> params = new ArrayList<Object>();
		params.add(adid);
		params.add(startDate);
		params.add(endDate);
		params.add(startAvgPrice);
		params.add(endAvgPrice);
		
		sql.append("select ").append(cols1).append(" from (")
		.append("select ").append(cols2).append(" from (")
		.append("select case ")
		.append(cols3)
		.append(" end as price_count, t2.sales_amount from tbweb.tb_attn_dir_detail t1")
		.append(" left join tbdaily.tb_tran_day t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id ")
		.append(" join tbbase.tb_base_shop t3 on t1.shop_id = t3.shop_id")
		.append(" where t1.adid = ? and t2.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d') and t2.avg_price >= ? and t2.avg_price <= ?");
		
		if(!"ALL".equals(shopType)){
			sql.append(" and t3.shop_type = ?");
			params.add(shopType);
		}
		
		sql.append(" ) t group by t.price_count) tt");
		
		return sqlUtil.search(sql.toString(), params.toArray());
		
	}
	
	/**
	 * 计算价格段
	 * @param startAvgPrice
	 * @param endAvgPrice
	 * @param priceSize
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, String>> getPriceCount(String startAvgPrice, String endAvgPrice, int priceSize) throws Exception{
		
		double avgPrice1 = StringUtils.toDouble(startAvgPrice);
		double avgPrice2 = StringUtils.toDouble(endAvgPrice);
		
		if(avgPrice2 <= avgPrice1 || priceSize < 0 || priceSize > 10){
			return null;
		}
		
		double diff = (avgPrice2 - avgPrice1) * 1.0 / priceSize;
		
		List<Map<String, String>> mapList = new ArrayList<Map<String, String>>();
		
		for(int i = 0; i < priceSize; i++){
			
			Map<String, String> map = new HashMap<String, String>();
			
			map.put("start", StringUtils.formatDecimal(avgPrice1+(i*diff), "#####0.00"));
			
			map.put("end", StringUtils.formatDecimal((avgPrice1 + (i+1)*diff), "#####0.00"));
			
			mapList.add(map);
			
		}
		return mapList;
	}
	
	/**
	 * 获取目录下的宝贝列表
	 * @param adid
	 * @param prdName
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<GoodsList> getDirGoodsList(String adid, String prdName, String categoty, PageParam pageParam) throws Exception{
		
		String curMonth = DateUtils.getCurMonth();
		String preMonth = DateUtils.getOffsetMonth(-1, "yyyy-MM");
		
		String sql = "select t4.prd_name, t4.item_id,t4.prd_img,t4.cat_path,t5.shop_name,t5.shop_id,t5.shop_type,"
		  +" t2.avg_price,t2.avg_price_tran,t2.sales_volume,t3.sales_volume as sales_volume_pre,t2.sales_amount, t3.sales_amount as sales_amount_pre,"
		  +" date_format(t2.createtime,'%Y-%m-%d') as createtime"
		  +" from tbweb.tb_attn_dir_detail t1 "
		  
		  +" left join tbdaily.tb_tran_month t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.tran_month = ?"
		  +" left join tbdaily.tb_tran_month t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id and t3.tran_month = ?"
		  
		  +" join tbbase.tb_base_product t4 on t1.shop_id = t4.shop_id and t1.item_id = t4.item_id"
		  +" join tbbase.tb_base_shop t5 on t1.shop_id = t5.shop_id"
		  +" where t1.adid = ? ";
		
		List<Object> params = new ArrayList<Object>();
		params.add(curMonth);
		params.add(preMonth);
		params.add(adid);
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t4.prd_name = ?";
			params.add(prdName);
		}
		
		if(StringUtils.isNotBlank(categoty)){
			sql += " and t4.cat_path like '"+categoty+"%' ";
		}
		
		List<GoodsList> list = sqlUtil.searchList(GoodsList.class, pageParam.buildSql(sql), params.toArray());
		
		return PageEntity.getPageEntity(pageParam, list);
	}
	
	/**
	 * 获取商品类别
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<CatApi> getCat(String parentNo) throws Exception {

		String sql = "SELECT cat_no as catNo, cat_name as category, CONCAT_WS('-',cat_name_single,cat_name) as catName, isparent as hasChild FROM tbbase.tb_base_cat_api where parent_no = ? order by cat_name_single";

		return sqlUtil.searchList(CatApi.class, sql, parentNo);

	}
	
	/**
	 * 获取商品类别
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<CatApi> getCat(String parentNo, String uid) throws Exception {

		String sql = "select t2.* from tbweb.tb_attn_cat t1 "
		+" join (SELECT cat_no as catNo, cat_name as category, CONCAT_WS('-',cat_name_single,cat_name) as catName, isparent as hasChild FROM tbbase.tb_base_cat_api where parent_no = ? order by cat_name_single) t2 on t1.att_cat = t2.catNo"
		+" where t1.uid = ?";
		
		return sqlUtil.searchList(CatApi.class, sql, parentNo, uid);

	}

	/**
	 * 飚量店铺搜索
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<ShopSearch> getSearchShops() throws Exception {

		String sql = "select t2.shop_name as shopName, t2.region, t3.sales_volume as salesVolume,t3.sales_amount as salesAmount,t3.tran_count as tranCount,"
				+ " t4.sales_volume as salesVolumePre,t4.sales_amount as salesAmountPre,t4.tran_count as tranCountPre, t3.rise_index as riseIndex from tbdaily.tb_advert_total t1"
				+ " left join tbbase.tb_base_shop t2 on t1.shop_id = t2.shop_id"
				+ " left join tbdaily.tb_tran_month_shop t3 on t1.shop_id = t3.shop_id and t3.tran_month = ''"
				+ " left join tbdaily.tb_tran_month_shop t4 on t1.shop_id = t4.shop_id and t4.tran_month = ''"
				+ " where not exists (select 'X' from tbweb.tb_attn_shop t5 where t1.shop_id = t5.shop_id and t5.uid = '1')";

		return null;

	}

	/**
	 * 获取店铺下的宝贝列表，并按月统计
	 * 
	 * @param shopId
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getShopList(String shopId) throws Exception {

		StringBuffer sql = new StringBuffer();
		sql.append(
				"SELECT t1.item_id,t2.prd_name,t1.category,t1.avg_price,t1.avg_price_tran, t4.avg_price_tran,t1.zk_rate,t4.zk_rate, t1.sales_volume, t4.sales_volume, t1.sales_amount, t4.sales_amount,")
				.append("t3.hot,t3.normal,t3.tb_cu,t3.activity,t3.taobaoke,t3.ztc,t3.ju,t3.normal_cu,t3.hot_mobile,t3.tb_cu_mobile,t3.activity_mobile,t3.ztc_mobile,t3.normal_cu_mobile FROM tbdaily.tb_tran_month t1 ")
				.append(" left join tbbase.tb_base_product t2 on t1.item_id = t2.item_id")
				.append(" left join tbdaily.tb_advert_product t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id")
				.append(" left join tbdaily.tb_tran_month t4 on t1.shop_id = t4.shop_id and t1.item_id = t4.item_id and t4.tran_month = '"
						+ DateUtils.getOffsetMonth(-1) + "'")
				.append(" where t1.shop_id = ? and t1.tran_month = '" + DateUtils.getCurMonth() + "'");

		return sqlUtil.searchList(sql.toString(), shopId);

	}

	/**
	 * 获取分页的宝贝列表
	 * 
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<GoodsList> getPageGoodsList(PageParam pageParam, String shopId, String category, String prdName) throws Exception {

		PageEntity<GoodsList> pageEntity = new PageEntity<GoodsList>();

		String reSql = " from tbbase.tb_base_product t1 "
				+ " join tbdaily.tb_shua_month t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.tran_month = '"+ DateUtils.getCurMonth() +"'"
				+ " left join tbdaily.tb_shua_month t4 on t1.shop_id = t4.shop_id and t1.item_id = t4.item_id and t4.tran_month = '"+ DateUtils.getOffsetMonth(-1, "yyyy-MM") +"'"
				+ " left join tbdaily.tb_tran_month t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id and t3.tran_month = '"+ DateUtils.getCurMonth() +"'"
				+ " left join tbdaily.tb_tran_month t5 on t1.shop_id = t5.shop_id and t1.item_id = t5.item_id and t5.tran_month = '"+ DateUtils.getOffsetMonth(-1, "yyyy-MM") +"'"
				+ " where t1.shop_id = ?";

		List<Object> params = new ArrayList<Object>();
		params.add(shopId);
		if (StringUtils.isNotBlank(category)) {
			
			reSql += " and t1.cat_path like '" + category + "%'";
		}
		if (StringUtils.isNotBlank(prdName)) {
			reSql += " and t1.prd_name like '%" + prdName + "%'";
		}

		String totalSql = "select count(0) as recordsTotal" + reSql;
		Map<String, Object> totalMap = sqlUtil.search(totalSql, params.toArray());

		long recordsTotal = StringUtils.toLong(totalMap.get("recordsTotal"));
		pageEntity.setRecordsFiltered(recordsTotal);
		pageEntity.setRecordsTotal(recordsTotal);

		// 排序
		String orderSql = " order by " + pageParam.getoTag() + ".";
		if (StringUtils.isNotBlank(pageParam.getOrderColumn()) && pageParam.getOrderColumn().indexOf("pre") > -1) {// 特殊处理
			orderSql += pageParam.getOrderColumn().replace("_pre", "");
		} else {
			orderSql += pageParam.getOrderColumn();
		}
		orderSql += " " + pageParam.getOrderDir();

		String searchFields = "t1.prd_img,t1.item_id,t1.cat_path,t1.prd_name,t3.avg_price,t3.avg_price_tran,t5.avg_price_tran as avg_price_tran_pre,"
				+ "t3.sales_volume,t5.sales_volume as sales_volume_pre, t3.sales_amount, t5.sales_amount as sales_amount_pre,"
				+ "t2.shua_volume, t4.shua_volume as shua_volume_pre,t2.shua_amount, t4.shua_amount as shua_amount_pre";
		String pageSql = "select " + searchFields + reSql + orderSql + " limit " + pageParam.getStart() + ","
				+ pageParam.getLength();

		List<GoodsList> pageList = sqlUtil.searchList(GoodsList.class, pageSql, params.toArray());

		pageEntity.setData(pageList);

		pageEntity.setDraw(pageParam.getDraw());
		return pageEntity;
	}

	/**
	 * 广告分析列表
	 * 
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<ScalpEntity> getPageScalpList(PageParam pageParam, String shopId, String startDate, String endDate)
			throws Exception {

		String sql = "SELECT t1.tran_date,t2.sales_amount,t2.sales_volume,t2.tran_count,t1.shua_amount,t1.shua_volume,t1.shua_count,t2.rise_index FROM tbdaily.tb_shua_day_shop t1"
				+" left join tbdaily.tb_tran_day_shop t2 on t1.shop_id = t2.shop_id and t1.tran_date = t2.tran_date"
				+" where t1.shop_id = ? and  t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		
		List<ScalpEntity> list = sqlUtil.searchList(ScalpEntity.class, pageParam.buildSql(sql), shopId, startDate, endDate);
	
		PageEntity<ScalpEntity> pageEntity = PageEntity.getPageEntity(pageParam, list);
	
		return pageEntity;
		
	}

	/**
	 * 获取广告分析图表的数据
	 * 
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public List<ScalpEntity> getChartData(String shopId, String itemId, String startDate, String endDate) throws Exception {

		String sql = "SELECT t1.tran_date,t2.sales_amount,t2.sales_volume,t2.tran_count,sum(t1.shua_amount) as shua_amount,sum(t1.shua_volume) as shua_volume,sum(t1.shua_count) as shua_count FROM tbdaily.tb_shua_day t1" 
				+" left join tbdaily.tb_tran_day t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t1.tran_date = t2.tran_date"
				+" where t1.shop_id = ? and t1.item_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')"
				+" group by NULL";

		return sqlUtil.searchList(ScalpEntity.class, sql, shopId, itemId, startDate, endDate);
	}

	/**
	 * 宝贝广告-淘宝客
	 * 
	 * @param shopId
	 * @param itemId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertTaoke> getTaokes(PageParam pageParam, String shopId, String itemId, String startDate,
			String endDate) throws Exception {

		String sql = "SELECT t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.reserve_price,t2.commission_rate_percent,t2.cal_commission,t2.total_fee,t2.total_num"
				+ " FROM tbdaily.tb_tran_day t1"
				+ " left join tbdaily.tb_advert_taoke t2 on t2.shop_id = t1.shop_id and t2.item_id = t1.item_id and t2.catch_date = t1.tran_date"
				+ " where t1.shop_id = ? and t1.item_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		List<AdvertTaoke> list = sqlUtil.searchList(AdvertTaoke.class, pageParam.buildSql(sql), shopId, itemId,
				startDate, endDate);

		PageEntity<AdvertTaoke> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 宝贝广告-热门钻展
	 * 
	 * @param shopId
	 * @param itemId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertHot> getHots(PageParam pageParam, String shopId, String itemId, String startDate,
			String endDate, String adType) throws Exception {

		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count, t4.position,t4.pic_url,t4.ad_pic,t4.screenshots from tbdaily.tb_tran_day t1"
				+ " left join (select t2.put_date,t2.shop_id,t2.item_id,t3.position,t3.pic_url,t2.ad_pic,t2.screenshots from tbdaily.tb_advert_zuanz t2 "
				+ " join tbbase.tb_base_postion t3 on t2.bpid = t3.bpid where t3.ad_type = ?) t4"
				+ " on t1.tran_date = t4.put_date and t1.shop_id = t4.shop_id and t1.item_id = t4.item_id"
				+ " where t1.shop_id = ? and t1.item_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), adType, shopId, itemId,
				startDate, endDate);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 宝贝广告-淘宝促销
	 * 
	 * @param shopId
	 * @param itemId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertHot> getTaobaoCus(PageParam pageParam, String shopId, String itemId, String startDate,
			String endDate) throws Exception {

		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content as position,"
				+ " t2.ad_url as pic_url,t2.ad_url as ad_pic,t2.ad_url as screenshots from tbdaily.tb_tran_day t1 left join tbdaily.tb_advert_cu t2 "
				+ " on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '淘宝促销' "
				+ " where t1.shop_id = ? and t1.item_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), shopId, itemId, startDate,
				endDate);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 改名跟踪
	 * 
	 * @param pageParam
	 * @param shopId
	 * @param itemId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<ChngName> getChngNames(PageParam pageParam, String startDate,
			String endDate, String adid) throws Exception {

		String sql = "select * from tbdaily.tb_chng_name t1 where"

				+" t1.change_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')"

				+" and exists (select 'X' from tbweb.tb_attn_dir_detail t2 where t2.adid = ? and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id)";
		
		List<ChngName> list = sqlUtil.searchList(ChngName.class, pageParam.buildSql(sql), startDate, endDate, adid);

		PageEntity<ChngName> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 调价跟踪
	 * 
	 * @param pageParam
	 * @param shopId
	 * @param itemId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<ChngPrice> getChngPrices(PageParam pageParam, String startDate,
			String endDate, String adid) throws Exception {

		String sql = "select * from tbdaily.tb_chng_price t1 where"

				+" t1.change_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')"

				+" and exists (select 'X' from tbweb.tb_attn_dir_detail t2 where t2.adid = ? and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id)";
		
		List<ChngPrice> list = sqlUtil.searchList(ChngPrice.class, pageParam.buildSql(sql), startDate, endDate, adid);

		PageEntity<ChngPrice> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
		
		
	}
	
	
	/**
	 * 刷单分析详情
	 * @param pageParam
	 * @param shopId
	 * @param itemId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<ScalpEntity> getScalpInfos(PageParam pageParam, String shopId, String itemId, String startDate,
			String endDate) throws Exception {

		 String sql = "SELECT t1.tran_date,t2.sales_amount,t2.sales_volume,t2.tran_count,t1.shua_amount,t1.shua_volume,t1.shua_count,t1.rule,t1.precision FROM tbdaily.tb_shua_day t1" 
				 +" left join tbdaily.tb_tran_day t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t1.tran_date = t2.tran_date"
				 +" where t1.shop_id = ? and t1.item_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<ScalpEntity> list = sqlUtil.searchList(ScalpEntity.class, pageParam.buildSql(sql), shopId, itemId,
				startDate, endDate);

		PageEntity<ScalpEntity> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 店铺运营分析-宝贝列表
	 * 
	 * @param shopId
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<GoodsList> getPageShopGoodList(PageParam pageParam, String category, String shopId, String date, String detailType)
			throws Exception {

		String coreSql = "from ("
				+ " select t1.prd_img,t1.item_id,t1.cat_path,t1.prd_name,t3.avg_price,t3.avg_price_tran,t3.sales_volume,"
				+ " t3.sales_amount, t3.tran_count,sum(t2.shua_amount) as shua_amount,sum(t2.shua_volume) as shua_volume,sum(t2.shua_count) as shua_count from tbbase.tb_base_product t1 "
				+ " "+(detailType.equals("sales") ? "left" : "")+" join tbdaily.tb_shua_day t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
				+ " left join tbdaily.tb_tran_day t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id"
				+ " where t1.shop_id = ? and t2.tran_date = t3.tran_date and t2.tran_date = str_to_date(?, '%Y-%m-%d') ";
		
		if (StringUtils.isNotBlank(category)) {
			
			coreSql += " and t1.cat_path like '" + category + "%'";
		}	
		
		String totalSql = "select count(0) as cnt "+coreSql + " group by NULL) t";
		
		Map<String, Object> map = sqlUtil.search(totalSql, shopId, date);

		long totalRecords = StringUtils.toLong(map.get("cnt"));
		pageParam.setTotalRecords(totalRecords);
		

		String pageSql = "select * "+coreSql+" group by NULL) t";

		List<GoodsList> list = sqlUtil.searchList(GoodsList.class, pageParam.buildSql(pageSql), shopId, date);

		PageEntity<GoodsList> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 店铺广告-直通车
	 * 
	 * @param pageParam
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertZTC> getShopZTCs(PageParam pageParam, String shopId, String startDate, String endDate)
			throws Exception {

		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.keyword from tbdaily.tb_tran_day_shop t1"
				+ " left join tbdaily.tb_advert_shop_ztc t2 on t1.shop_id = t2.shop_id and t1.tran_date = t2.catch_date"
				+ " where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		List<AdvertZTC> list = sqlUtil.searchList(AdvertZTC.class, pageParam.buildSql(sql), shopId, startDate, endDate);

		PageEntity<AdvertZTC> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 店铺广告-钻展
	 * 
	 * @param pageParam
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @param zuanType
	 *            热门钻展、普通钻展
	 * @return
	 */
	public PageEntity<AdvertHot> getShopZuan(PageParam pageParam, String shopId, String startDate, String endDate,
			String zuanType) {

		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count, t4.position,t4.pic_url,t4.ad_pic,t4.screenshots from tbdaily.tb_tran_day t1"
				+ " left join (select t2.put_date,t2.shop_id,t2.item_id,t3.position,t3.pic_url,t2.ad_pic,t2.screenshots from tbdaily.tb_advert_zuanz t2 "
				+ " join tbbase.tb_base_postion t3 on t2.bpid = t3.bpid where t3.ad_type = ? and t2.item_id = '') t4 "
				+ " on t1.tran_date = t4.put_date and t1.shop_id = t4.shop_id "
				+ " where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), zuanType, shopId,
				startDate, endDate);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}

	/**
	 * 店铺广告-淘宝客
	 * 
	 * @param pageParam
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertTaoke> getShopTaokes(PageParam pageParam, String shopId, String startDate, String endDate)
			throws Exception {

		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.settle_amt,t2.settle_num,t2.commisiona_amt from tbdaily.tb_tran_day_shop t1"
				+ " left join tbdaily.tb_advert_taoke_shop t2 on t1.shop_id = t2.shop_id and t1.tran_date = t2.catch_date"
				+ " where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		List<AdvertTaoke> list = sqlUtil.searchList(AdvertTaoke.class, pageParam.buildSql(sql), shopId, startDate,
				endDate);

		PageEntity<AdvertTaoke> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 店铺广告-搭配减价
	 * @param pageParam
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @return
	 * @throws Exception
	 */
	public PageEntity<SaleShop> getSaleShops(PageParam pageParam, String shopId, String date)
			throws Exception {

		String sql = "SELECT t1.meal_id,max(t1.sale_name) as sale_name,max(t1.sale_num) as sale_num,GROUP_CONCAT(t1.prd_name SEPARATOR '@') as prd_name,"
				+ " GROUP_CONCAT(t1.item_id SEPARATOR '@') as item_id,GROUP_CONCAT(t1.price SEPARATOR '@') as price,max(t1.sale_price) as sale_price,"
				+ " max(t1.sale_price_zk) as sale_price_zk FROM tbdaily.tb_advert_sale_shop t1 "
				+ " where t1.shop_id = ? and t1.catch_date = str_to_date(?, '%Y-%m-%d') group by t1.meal_id";

		List<SaleShop> list = sqlUtil.searchList(SaleShop.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<SaleShop> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-淘宝客
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertTaoke> getGoodsTaokes(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String sql = "SELECT t3.prd_name, t3.prd_img, t1.item_id,t2.sales_amount,t2.sales_volume,t2.tran_count,t1.reserve_price,t1.commission_rate_percent,t1.cal_commission,t1.total_fee,t1.total_num"
					+" FROM tbdaily.tb_tran_day t2"
					+" left join tbdaily.tb_advert_taoke t1 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t1.catch_date = t2.tran_date"
					+" left join tbbase.tb_base_product t3 on t2.shop_id = t3.shop_id and t2.item_id = t3.item_id "
					+" where t2.shop_id = ? and t2.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t3.prd_name like '%"+prdName+"%'";
		}
		
		List<AdvertTaoke> list = sqlUtil.searchList(AdvertTaoke.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertTaoke> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-钻展
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @param zuanType
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertHot> getGoodsZuans(PageParam pageParam, String shopId, String prdName, String date, String zuanType) throws Exception{
		
		String sql = "select t5.prd_name, t5.prd_img,t1.item_id, t1.sales_amount,t1.sales_volume,t1.tran_count, t4.position,t4.pic_url,t4.ad_pic,t4.screenshots from tbdaily.tb_tran_day t1"
					+" left join (select t2.put_date,t2.shop_id,t2.item_id,t3.position,t3.pic_url,t2.ad_pic,t2.screenshots from tbdaily.tb_advert_zuanz t2 "
					+" join tbbase.tb_base_postion t3 on t2.bpid = t3.bpid where t3.ad_type = ?) t4"
					+" on t1.tran_date = t4.put_date and t1.shop_id = t4.shop_id and t1.item_id = t4.item_id"
					+" left join tbbase.tb_base_product t5 on t1.shop_id = t5.shop_id and t1.item_id = t5.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t5.prd_name like '%"+prdName+"%'";
		}
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), zuanType, shopId, date);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	public PageEntity<AdvertHot> getGoodsZuans(String shopId, String startDate, String endDate, String zuanType, PageParam pageParam) throws Exception{
		
		String sql = "select t1.tran_date, t1.sales_amount,t1.sales_volume,t1.tran_count, t4.position,t4.pic_url,t4.ad_pic,t4.screenshots from tbdaily.tb_tran_day t1"
					+" left join (select t2.put_date,t2.shop_id,t2.item_id,t3.position,t3.pic_url,t2.ad_pic,t2.screenshots from tbdaily.tb_advert_zuanz t2 "
					+" join tbbase.tb_base_postion t3 on t2.bpid = t3.bpid where t3.ad_type = ?) t4"
					+" on t1.tran_date = t4.put_date and t1.shop_id = t4.shop_id and t1.item_id = t4.item_id"
					+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), zuanType, shopId, startDate, endDate);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-淘宝促销
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertHot> getGoodsTbCus(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String sql = "select t5.prd_name, t5.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content as position,"
					+" t2.ad_url as pic_url,t2.ad_url as ad_pic,t2.ad_url as screenshots from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_cu t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '淘宝促销'"
					+" left join tbbase.tb_base_product t5 on t1.shop_id = t5.shop_id and t1.item_id = t5.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t5.prd_name like '%"+prdName+"%'";
		}
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-手机淘宝促销
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertHot> getGoodsTbCuMs(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String distUrl = "http://page.m.tmall.com/myy/myy_cjy.html";
		
		String sql = "select t5.prd_name, t5.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content as position,"
					+" '"+distUrl+"' as pic_url,'"+distUrl+"' as ad_pic,'"+distUrl+"' as screenshots from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_phone_sale t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '手机淘宝促销'"
					+" left join tbbase.tb_base_product t5 on t1.shop_id = t5.shop_id and t1.item_id = t5.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t5.prd_name like '%"+prdName+"%'";
		}
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	public PageEntity<AdvertHot> getGoodsTbCuMs(String shopId, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String distUrl = "http://page.m.tmall.com/myy/myy_cjy.html";
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content as position,"
					+" '"+distUrl+"' as pic_url,'"+distUrl+"' as ad_pic,'"+distUrl+"' as screenshots from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_phone_sale t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '手机淘宝促销'"
					+" left join tbbase.tb_base_product t5 on t1.shop_id = t5.shop_id and t1.item_id = t5.item_id "
					+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), shopId, startDate, endDate);

		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-直通车
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertZTC> getGoodsZTCs(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String sql = "select t3.prd_name, t3.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,group_concat(t2.keyword SEPARATOR '@') as keyword,group_concat(t2.seq SEPARATOR '@') as seq from tbdaily.tb_tran_day t1"
					+" left join tbdaily.tb_advert_prod_ztc t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
					+" left join tbbase.tb_base_product t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t3.prd_name like '%"+prdName+"%'";
		}
		sql += " group by t1.item_id";
		List<AdvertZTC> list = sqlUtil.searchList(AdvertZTC.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertZTC> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	public PageEntity<AdvertZTC> getGoodsZTCs(String shopId, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,group_concat(t2.keyword SEPARATOR '@') as keyword,group_concat(t2.seq SEPARATOR '@') as seq from tbdaily.tb_tran_day t1"
					+" left join tbdaily.tb_advert_prod_ztc t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
					+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d') group by t1.item_id";

		List<AdvertZTC> list = sqlUtil.searchList(AdvertZTC.class, pageParam.buildSql(sql), shopId, startDate, endDate);

		PageEntity<AdvertZTC> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-聚划算
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertJu> getGoodsJus(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String sql = "select t3.prd_name, t3.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_ju t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
					+" left join tbbase.tb_base_product t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t3.prd_name like '%"+prdName+"%'";
		}
		List<AdvertJu> list = sqlUtil.searchList(AdvertJu.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertJu> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	public PageEntity<AdvertJu> getGoodsJus(String shopId, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_ju t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
					+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<AdvertJu> list = sqlUtil.searchList(AdvertJu.class, pageParam.buildSql(sql), shopId, startDate, endDate);

		PageEntity<AdvertJu> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-商品促销
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertCu> getGoodsCus(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String sql = "select t3.prd_name, t3.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_cu t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '商品促销'"
					+" left join tbbase.tb_base_product t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t3.prd_name like '%"+prdName+"%'";
		}
		List<AdvertCu> list = sqlUtil.searchList(AdvertCu.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertCu> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	public PageEntity<AdvertCu> getGoodsCus(String shopId, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.activity_content from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_cu t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '商品促销'"
					+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";

		List<AdvertCu> list = sqlUtil.searchList(AdvertCu.class, pageParam.buildSql(sql), shopId, startDate, endDate);

		PageEntity<AdvertCu> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-手机直通车
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertZTC> getGoodsZTCMs(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String sql = "select t3.prd_name, t3.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,group_concat(t2.keyword SEPARATOR '@') as keyword,group_concat(t2.seq SEPARATOR '@') as seq from tbdaily.tb_tran_day t1"
					+" left join tbdaily.tb_advert_prod_ztc_m t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
					+" left join tbbase.tb_base_product t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t3.prd_name like '%"+prdName+"%'";
		}
		sql += " group by t1.item_id";
		List<AdvertZTC> list = sqlUtil.searchList(AdvertZTC.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertZTC> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	public PageEntity<AdvertZTC> getGoodsZTCMs(String shopId, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,group_concat(t2.keyword SEPARATOR '@') as keyword,group_concat(t2.seq SEPARATOR '@') as seq from tbdaily.tb_tran_day t1"
					+" left join tbdaily.tb_advert_prod_ztc_m t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id"
					+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d') group by t1.item_id";
		
		List<AdvertZTC> list = sqlUtil.searchList(AdvertZTC.class, pageParam.buildSql(sql), shopId, startDate, endDate);

		PageEntity<AdvertZTC> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 宝贝广告-手机促销
	 * @param pageParam
	 * @param shopId
	 * @param prdName
	 * @param date
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertCu> getGoodsCuMs(PageParam pageParam, String shopId, String prdName, String date) throws Exception{
		
		String sql = "select t3.prd_name, t3.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,concat_ws(':',t2.activity_content,t2.price) as activity_content from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_phone_sale t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '手机促销'"
					+" left join tbbase.tb_base_product t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id "
					+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t3.prd_name like '%"+prdName+"%'";
		}
		
		List<AdvertCu> list = sqlUtil.searchList(AdvertCu.class, pageParam.buildSql(sql), shopId, date);

		PageEntity<AdvertCu> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	public PageEntity<AdvertCu> getGoodsCuMs(String shopId, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,concat_ws(':',t2.activity_content,t2.price) as activity_content from tbdaily.tb_tran_day t1 "
					+" left join tbdaily.tb_advert_phone_sale t2 on t1.tran_date = t2.catch_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.activity_type = '手机促销'"
					+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<AdvertCu> list = sqlUtil.searchList(AdvertCu.class, pageParam.buildSql(sql), shopId, startDate, endDate);

		PageEntity<AdvertCu> pageEntity = PageEntity.getPageEntity(pageParam, list);

		return pageEntity;
	}
	
	/**
	 * 店铺广告-淘宝活动
	 * @param pageParam
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @param isphone
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertHot> getShopTaobaos(PageParam pageParam, String shopId, String startDate, String endDate, String isphone) throws Exception{
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.position,"
				+" t2.ad_pos_url,t2.ad_dest_url,t2.ad_pic,t2.screenshots from tbdaily.tb_tran_day t1 "
				+" left join tbdaily.tb_advert_taobao t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t2.isphone = ? and t2.item_id = ''"
				+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), isphone, shopId, startDate, endDate);
	
		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);
	
		return pageEntity;
		
	}
	
	/**
	 * 宝贝广告-淘宝活动
	 * @param pageParam
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @param isphone
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertHot> getGoodsTaobaos(PageParam pageParam, String shopId, String prdName, String date, String isphone) throws Exception{
		
		String sql = "select t5.prd_name, t5.prd_img, t1.item_id,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.position,"
				+" t2.ad_pos_url,t2.ad_dest_url,t2.ad_pic,t2.screenshots from tbdaily.tb_tran_day t1 "
				+" left join tbdaily.tb_advert_taobao t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.isphone = ?  and t2.item_id <> ''"
				+" left join tbbase.tb_base_product t5 on t1.shop_id = t5.shop_id and t1.item_id = t5.item_id "
				+" where t1.shop_id = ? and t1.tran_date = str_to_date(?, '%Y-%m-%d')";
		
		if(StringUtils.isNotBlank(prdName)){
			sql += " and t5.prd_name like '%"+prdName+"%'";
		}
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), isphone, shopId, date);
		
		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);
	
		return pageEntity;
		
	}
	
	public PageEntity<AdvertHot> getGoodsTaobaos(String shopId, String startDate, String endDate, String isphone, PageParam pageParam) throws Exception{
		
		String sql = "select t1.tran_date,t1.sales_amount,t1.sales_volume,t1.tran_count,t2.position,"
				+" t2.ad_pos_url,t2.ad_dest_url,t2.ad_pic,t2.screenshots from tbdaily.tb_tran_day t1 "
				+" left join tbdaily.tb_advert_taobao t2 on t1.tran_date = t2.put_date and t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.isphone = ?"
				+" where t1.shop_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<AdvertHot> list = sqlUtil.searchList(AdvertHot.class, pageParam.buildSql(sql), isphone, shopId, startDate, endDate);
		
		PageEntity<AdvertHot> pageEntity = PageEntity.getPageEntity(pageParam, list);
	
		return pageEntity;
		
	}
	
	/**
	 * 价格走势
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @param itemId
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<PriceTrend> getPriceTrends(String shopId, String startDate, String endDate, String itemId, PageParam pageParam) throws Exception{
		
		String sql = "SELECT t1.tran_date, t1.avg_price, t1.avg_price_tran, t1.sales_volume FROM tbdaily.tb_tran_day t1 "
				+" where t1.shop_id = ? and t1.item_id = ? and t1.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<PriceTrend> list = sqlUtil.searchList(PriceTrend.class, pageParam.buildSql(sql), shopId,itemId, startDate, endDate);
		
		PageEntity<PriceTrend> pageEntity = PageEntity.getPageEntity(pageParam, list);
	
		return pageEntity;
		
	}
	
	/**
	 * 飚量店铺检索
	 * @param pageParam
	 * @param uid
	 * @param category
	 * @param types
	 * @param ntypes
	 * @param startReAmount
	 * @param endReAmount
	 * @param shopType
	 * @param startAmount
	 * @param endAmount
	 * @param startRiseIndex
	 * @param endRiseIndex
	 * @return
	 * @throws Exception
	 */
	public PageEntity<ShopSearch> getPageShopSearch(PageParam pageParam, String uid, String category, String types, String ntypes, String startReAmount, String endReAmount, String shopType, String startAmount, String endAmount, String startRiseIndex, String endRiseIndex) throws Exception{
		
		String curMonth = DateUtils.getCurMonth();
		
		String preMonth = DateUtils.getOffsetMonth(-1, "yyyy-MM");
		
		String coreSql = "SELECT t1.shop_id,t2.rate_link,t2.shop_name,t2.shop_img,t2.shop_url,t2.shop_type,t2.region,t1.sales_volume,t1.sales_amount,t1.tran_count,t3.sales_volume as pre_sales_volume,t3.sales_amount as pre_sales_amount,"
				+" t3.tran_count as pre_tran_count,sum(t5.sales_amount) as re_sales_amount,t1.rise_index,"
				+" sum(t4.hot) as hot,sum(t4.normal) as normal,sum(t4.tb_cu) as tb_cu,sum(t4.activity) as activity,sum(t4.taobaoke) as taobaoke,sum(t4.ztc) as ztc,sum(t4.ju) as ju,"
				+" sum(t4.normal_cu) as normal_cu,sum(t4.normal_cu_mobile) as normal_cu_mobile,sum(t4.hot_mobile) as hot_mobile,sum(t4.tb_cu_mobile) as tb_cu_mobile,sum(t4.activity_mobile) as activity_mobile,sum(t4.ztc_mobile) as ztc_mobile,t5.cat_path FROM tbdaily.tb_tran_month_shop t1"
				+" left join tbbase.tb_base_shop t2 on t1.shop_id = t2.shop_id"
				+" left join tbdaily.tb_tran_month_shop t3 on t1.shop_id = t3.shop_id and t3.tran_month = '"+preMonth+"'"
				+" left join tbdaily.tb_advert_product t4 on t1.shop_id = t4.shop_id and  date_format(t4.put_date, '%Y-%m') = t1.tran_month"
				+" left join tbdaily.tb_tran_month t5 on t1.shop_id = t5.shop_id and t5.tran_month = '"+preMonth+"'"
				+" where t1.tran_month = '"+curMonth+"'";
		
		StringBuffer totalSql = new StringBuffer();
		totalSql.append("select count(0) as recordsTotal from (")
		.append(coreSql).append(") t where t.cat_path like '"+category+"%' ");
		
		StringBuffer pageSql = new StringBuffer();
		pageSql.append("select t.*,t6.asid from (")
		.append(coreSql).append(") t left join tbweb.tb_attn_shop t6 on t.shop_id = t6.shop_id and t6.uid = '"+uid+"' where t.cat_path like '"+category+"%' ");
		
		List<Object> params = new ArrayList<Object>();
		StringBuffer whereSql = new StringBuffer();
		if(StringUtils.isNotBlank(types)){
			
			typeToSql(types, whereSql, ">");
			
		}
		
		if(StringUtils.isNotBlank(ntypes)){
			
			typeToSql(ntypes, whereSql, "<");
			
		}
		
		if(StringUtils.isNotBlank(startReAmount)){
			
			whereSql.append(" and t.re_sales_amount > ?");
			params.add(startReAmount);
			
		}
		
		if(StringUtils.isNotBlank(endReAmount)){
			whereSql.append(" and t.re_sales_amount < ?");
			params.add(endReAmount);
		}
		
		if(StringUtils.isNotBlank(shopType)){
			whereSql.append(" and t.shop_type = ?");
			params.add(shopType);
		}
		
		if(StringUtils.isNotBlank(startAmount)){
			whereSql.append(" and t.sales_amount > ?");
			params.add(startAmount);
		}
		
		if(StringUtils.isNotBlank(endAmount)){
			whereSql.append(" and t.sales_amount < ?");
			params.add(endAmount);
		}
		
		if(StringUtils.isNotBlank(startRiseIndex)){
			whereSql.append(" and t.rise_index > ?");
			params.add(startRiseIndex);
		}
		
		if(StringUtils.isNotBlank(endRiseIndex)){
			whereSql.append(" and t.rise_index < ?");
			params.add(endRiseIndex);
		}
		
		totalSql.append(whereSql);
		
		PageEntity<ShopSearch> pageEntity = new PageEntity<ShopSearch>();
		
		Map<String, Object> totalMap = sqlUtil.search(totalSql.toString(), params.toArray());
		
		long recordsTotal = StringUtils.toLong(totalMap.get("recordsTotal"));
		pageEntity.setRecordsFiltered(recordsTotal);
		pageEntity.setRecordsTotal(recordsTotal);

		// 排序
		String orderSql = "order by " + pageParam.getoTag() + ".";
		if (StringUtils.isNotBlank(pageParam.getOrderColumn()) && pageParam.getOrderColumn().indexOf("pre") > -1) {// 特殊处理
			orderSql += pageParam.getOrderColumn().replace("_pre", "");
		} else {
			orderSql += pageParam.getOrderColumn();
		}
		orderSql += " " + pageParam.getOrderDir();

		pageSql.append(whereSql).append(" ").append(orderSql).append(" limit " + pageParam.getStart() + ","+ pageParam.getLength());

		List<ShopSearch> pageList = sqlUtil.searchList(ShopSearch.class, pageSql.toString(), params.toArray());

		pageEntity.setData(pageList);

		pageEntity.setDraw(pageParam.getDraw());
		
		return pageEntity;
	}

	private void typeToSql(String types, StringBuffer whereSql, String gl) {
		String[] typeArr = types.split(",");
		for(String type : typeArr){
			
			if("1".equals(type)){
				whereSql.append(" and t.hot "+gl+" 0");
			}else if("2".equals(type)){
				whereSql.append(" and t.normal "+gl+" 0");
			}else if("3".equals(type)){
				whereSql.append(" and t.tb_cu "+gl+" 0");
			}else if("4".equals(type)){
				whereSql.append(" and t.activity "+gl+" 0");
			}else if("5".equals(type)){
				whereSql.append(" and t.taobaoke "+gl+" 0");
			}else if("6".equals(type)){
				whereSql.append(" and t.ztc "+gl+" 0");
			}else if("7".equals(type)){
				whereSql.append(" and t.ju "+gl+" 0");
			}else if("8".equals(type)){
				whereSql.append(" and t.normal_cu "+gl+" 0");
			}else if("11".equals(type)){
				whereSql.append(" and t.hot_mobile "+gl+" 0");
			}else if("13".equals(type)){
				whereSql.append(" and t.tb_cu_mobile "+gl+" 0");
			}else if("12".equals(type)){
				whereSql.append(" and t.activity_mobile "+gl+" 0");
			}else if("14".equals(type)){
				whereSql.append(" and t.ztc_mobile "+gl+" 0");
			}else if("10".equals(type)){
				whereSql.append(" and t.normal_cu_mobile "+gl+" 0");
			}
		}
	}
	
	/**
	 * 店铺分析-店铺比对-数据表
	 * @param shopIds
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdAnalysis> getShopCompares(String shopIds, PageParam pageParam) throws Exception{
		
		String curMonth = DateUtils.getCurMonth();
		String preMonth = DateUtils.getOffsetMonth(-1, "yyyy-MM");
		
		String[] shopIdArr = shopIds.split(",");
		//这里使用union all而不用in,是由于shop_id是唯一索引，两者查询效率都差不多，而union all有排序功能
		
		StringBuffer sb = new StringBuffer();
		String coreSql = "select t1.shop_name, t1.region, t1.item_count,t2.sales_amount,t2.sales_volume,t2.tran_count,"
				 +" t3.sales_amount as sales_amount_pre,t3.sales_volume as sales_volume_pre,t3.tran_count as tran_count_pre,"
				 +" t2.rise_index,t5.*,t7.* from tbbase.tb_base_shop t1 "
				 +" left join tbdaily.tb_tran_month_shop t2 on t1.shop_id = t2.shop_id and t2.tran_month = '"+curMonth+"'"
				 +" left join tbdaily.tb_tran_month_shop t3 on t1.shop_id = t3.shop_id and t3.tran_month = '"+preMonth+"'"
				 +" left join (select t4.shop_id, sum(t4.ztc) as shop_ztc,sum(t4.normal) as shop_normal,sum(t4.hot) as shop_hot,sum(t4.tb_cu) as shop_tb_cu,"
				 +" sum(t4.activity) as shop_activity, sum(t4.taobaoke) as shop_taobaoke,sum(t4.hot_mobile) as shop_hot_mobile,sum(t4.activity_mobile) as shop_activity_mobile,"
				 +" sum(t4.sale) as shop_sale from tbdaily.tb_advert_shop t4 where date_format(t4.put_date, '%Y-%m') = '"+curMonth+"' group by t4.shop_id) t5"
				 +" on t5.shop_id = t1.shop_id"
				 +" left join (SELECT t6.shop_id,sum(t6.hot) as hot,sum(t6.normal) as normal,sum(t6.tb_cu) as tb_cu, sum(t6.activity) as activity,sum(t6.taobaoke) as taobaoke, sum(t6.ztc) as ztc,"
				 +" sum(t6.ju) ju,sum(t6.normal_cu) as normal_cu,sum(t6.hot_mobile) as hot_mobile,sum(t6.tb_cu_mobile) as tb_cu_mobile,sum(t6.activity_mobile) as activity_mobile,"
				 +" sum(t6.ztc_mobile) as ztc_mobile,sum(t6.normal_cu_mobile) as normal_cu_mobile FROM tbdaily.tb_advert_product t6 where date_format(t6.put_date, '%Y-%m') = '"+curMonth+"' group by t6.shop_id) t7"
				 +" 	on t1.shop_id = t7.shop_id";
		
		for(int i = 0; i < shopIdArr.length; i++){
			
			String shopId = shopIdArr[i];
			if(StringUtils.isNotBlank(shopId)){
				sb.append(coreSql).append(" where t1.shop_id = '").append(shopId).append("'");
				
				if(i != shopIdArr.length - 1){
					sb.append(" union all ");
				}
			}
			
		}
		 
		 List<AdAnalysis> list = sqlUtil.searchList(AdAnalysis.class, sb.toString());
		 
		 pageParam.setLength(-1);//不分页
		 
		 return PageEntity.getPageEntity(pageParam, list);
		
	}
	
	/**
	 * 店铺分析-店铺对比-店铺走势图
	 * @param shopIds
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getShopTrends(String shopIds) throws Exception{
		
		String curMonth = DateUtils.getCurMonth();
		String preMonth = DateUtils.getOffsetMonth(-5, "yyyy-MM");
		
		String[] shopIdArr = shopIds.split(",");
		//这里使用union all而不用in,是由于shop_id是唯一索引，两者查询效率都差不多，而union all有排序功能

		List<String> monthList = DateUtils.getMonthListBetweenDates(preMonth, curMonth);
		
		StringBuffer cols = new StringBuffer();
		for(int i = 0; i < monthList.size(); i++){
			cols.append("sum(if(t2.tran_month='").append(monthList.get(i)).append("',t2.sales_amount, 0)) as a").append(monthList.get(i).replace("-", ""));
			
			if(i != monthList.size() - 1){
				cols.append(",");
			}
		}
		
		StringBuffer sb = new StringBuffer();
		
		for(int i = 0; i < shopIdArr.length; i++){
			
			String shopId = shopIdArr[i];
			if(StringUtils.isNotBlank(shopId)){
				
				sb.append("select t1.shop_name, ")
				.append(cols)
				.append(" from tbbase.tb_base_shop t1 ")
				.append(" left join tbdaily.tb_tran_month_shop t2 on t1.shop_id = t2.shop_id ")
				.append(" where t1.shop_id = '"+shopId+"' and str_to_date(t2.tran_month,'%Y-%m') between str_to_date('"+preMonth+"', '%Y-%m') and str_to_date('"+curMonth+"', '%Y-%m')")
				.append(" group by t1.shop_name");
				
				if(i != shopIdArr.length - 1){
					sb.append(" union all ");
				}
			}
			
		}
		
		return sqlUtil.searchList(sb.toString());
		
	}
	
	/**
	 * 店铺分析-类目分析：获取所有类目列表
	 * @param shopIds
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getCatByShopIds(String shopIds) throws Exception{
		
		String preMonth = DateUtils.getOffsetMonth(-1, "yyyy-MM");
		
		String[] shopIdArr = shopIds.split(",");
		
		//这里使用union 来获取是因为cat_no需要按店铺销售额排序
		StringBuffer sql = new StringBuffer();
		sql.append("select ta.cat_no, tb.cat_name from (");
		for(int i = 0; i < shopIdArr.length; i++){
			
			sql.append("select cat_no from (select t1.cat_no from tbbase.tb_base_product t1 ")
			.append(" left join tbdaily.tb_tran_month t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.tran_month = '"+preMonth+"'")
			.append(" where t1.shop_id = '"+shopIdArr[i]+"' group by t1.cat_no order by sum(t2.sales_amount) desc) t ");
			
			if(i != shopIdArr.length -1){
				sql.append(" union ");
			}
			
		}
		sql.append(") ta")
		.append(" join tbbase.tb_base_cat_api tb on ta.cat_no = tb.cat_no");
		
		return sqlUtil.searchList(sql.toString());
		
	}
	
	/**
	 * 店铺分析-类目分析
	 * @param shopIds
	 * @param catList
	 * @return
	 * @throws Exception
	 */
	public List<Map<String, Object>> getCatAnalysis(String shopIds, List<Map<String, Object>> catList) throws Exception{

		StringBuffer cTag = new StringBuffer();
		StringBuffer cols = new StringBuffer();
		
		for(int i = 0; i < catList.size(); i++){
			
			if(catList.get(i) != null && !catList.get(i).isEmpty()){
				String catNo = catList.get(i).get("cat_no").toString();
				
				cTag.append("sum(c").append(catNo).append(") as c").append(catNo);
				
				cols.append("if(t1.cat_no='").append(catNo).append("'").append(", sum(t2.sales_amount), 0) as c").append(catNo);
				
				if(i != catList.size() - 1){
					cTag.append(",");
					cols.append(",");
				}
			}
			
		}
		  
		String preMonth = DateUtils.getOffsetMonth(-1, "yyyy-MM");
		
		String[] shopIdArr = shopIds.split(",");
		
		StringBuffer sql = new StringBuffer();
		
		for(int i = 0; i < shopIdArr.length; i++){
			
			sql.append("select shop_name, ").append(cTag).append(" from (")
			.append("select t4.shop_name, ").append(cols).append(" from tbbase.tb_base_product t1")
			.append(" left join  tbdaily.tb_tran_month t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.tran_month = '"+preMonth+"'")
			.append(" join tbbase.tb_base_shop t4 on t1.shop_id = t4.shop_id")
			.append(" where t1.shop_id = '"+shopIdArr[i]+"' and t1.cat_no is not null  group by t1.cat_no) t3 group by shop_name");
			
			if(i != shopIdArr.length -1){
				sql.append(" union all ");
			}
			
		}
		
		return sqlUtil.searchList(sql.toString());
		
	}
	
	/**
	 * 获取关注店铺下的热销宝贝
	 * @param shopIds
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<HotGoods> getHotGoods(String uid, String category, String shopType, String prdName, String volumeTotal, String amountTotal, PageParam pageParam) throws Exception{
		
		int volumeTotalInt = StringUtils.toInteger(volumeTotal);
		if(volumeTotalInt == 0){
			volumeTotalInt = FinalConstants.DEFAULT_QUERY_VOLUME;
		}
		
		int amountTotalInt = StringUtils.toInteger(amountTotal);
		if(amountTotalInt == 0){
			amountTotalInt = FinalConstants.DEFAULT_QUERY_AMOUNT;
		}
		
		String coreSql = " from ("
					+" select t1.item_id,t1.prd_img,t1.prd_name,t1.shop_id,t2.shop_type,t2.shop_name,t1.cat_path,t3.avg_price,t4.avg_price_tran as avg_price_tran_pre,"
					+" t3.sales_volume,t4.sales_volume as sales_volume_pre,t3.sales_amount, t4.sales_amount as sales_amount_pre,"
					+" ifnull(sum(t3.sales_volume),0)+ifnull(sum(t4.sales_volume),0) as volume_total,ifnull(sum(t3.sales_amount),0)+ifnull(sum(t4.sales_amount),0) as amount_total from tbbase.tb_base_product t1"
					+" join tbbase.tb_base_shop t2 on t1.shop_id = t2.shop_id"
					+" left join tbdaily.tb_tran_month t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id and t3.tran_month = '"+DateUtils.getCurMonth()+"'"
					+" left join tbdaily.tb_tran_month t4 on t1.shop_id = t4.shop_id and t1.item_id = t4.item_id and t4.tran_month = '"+DateUtils.getOffsetMonth(-1, "yyyy-MM")+"'"
					+" where exists (select 'X' from tbweb.tb_attn_shop t5 where t5.uid = '"+uid+"' and t5.shop_id = t1.shop_id) group by t1.item_id) t"
					+" where shop_type = '"+shopType+"' and volume_total > "+volumeTotalInt+" and amount_total > "+amountTotalInt+"";
		
		if(StringUtils.isNotBlank(category)){
			coreSql += " and cat_path like '"+category+"%'";
		}
		
		if(StringUtils.isNotBlank(prdName)){
			coreSql += " and prd_name like '%"+prdName+"%'";
		}
		
		String totalSql = "select count(0) as cnt "+ coreSql;
		
		Map<String, Object> map = sqlUtil.search(totalSql);
		
		long cnt = StringUtils.toLong(map.get("cnt"));
		
		PageEntity<HotGoods> pageEntity = new PageEntity<HotGoods>();
		pageEntity.setRecordsFiltered(cnt);
		pageEntity.setRecordsTotal(cnt);
		pageEntity.setDraw(pageParam.getDraw());
		
		String orderSql = " order by ";
		if(StringUtils.isNotBlank(pageParam.getoTag())){
			orderSql = " order by " + pageParam.getoTag() + ".";
		}
		
		if (StringUtils.isNotBlank(pageParam.getOrderColumn()) && pageParam.getOrderColumn().indexOf("pre") > -1) {// 特殊处理
			orderSql += pageParam.getOrderColumn().replace("_pre", "");
		} else {
			orderSql += pageParam.getOrderColumn();
		}
		orderSql += " " + pageParam.getOrderDir();

		String pageSql = "select * "+ coreSql + orderSql + " limit " + pageParam.getStart() + ","
				+ pageParam.getLength();
		
		List<HotGoods> list = sqlUtil.searchList(HotGoods.class, pageSql);
		
		pageEntity.setData(list);
		
		return pageEntity;
	}
	
	/**
	 * 店铺分析-宝贝列表
	 * @param shopId
	 * @param category
	 * @param prdName
	 * @return
	 * @throws Exception
	 */
	public PageEntity<GoodsList> getShopGoodsList(String shopId, String category, String prdName, PageParam pageParam) throws Exception{
		
		PageEntity<GoodsList> pageEntity = new PageEntity<GoodsList>();

		String reSql = " from tbbase.tb_base_product t1 "
				+ " left join tbdaily.tb_tran_month t2 on t1.shop_id = t2.shop_id and t1.item_id = t2.item_id and t2.tran_month = ?"
				+ " left join tbdaily.tb_tran_month t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id and t3.tran_month = ?"
				+ " where t1.shop_id = ?";

		List<Object> params = new ArrayList<Object>();
		params.add(DateUtils.getCurMonth());
		params.add(DateUtils.getOffsetMonth(-1, "yyyy-MM"));
		params.add(shopId);
		if (StringUtils.isNotBlank(category)) {
			
			reSql += " and t1.cat_path like '" + category + "%'";
		}
		if (StringUtils.isNotBlank(prdName)) {
			reSql += " and t1.prd_name like '%" + prdName + "%'";
		}

		String totalSql = "select count(0) as recordsTotal" + reSql;
		Map<String, Object> totalMap = sqlUtil.search(totalSql, params.toArray());

		long recordsTotal = StringUtils.toLong(totalMap.get("recordsTotal"));
		pageEntity.setRecordsFiltered(recordsTotal);
		pageEntity.setRecordsTotal(recordsTotal);

		// 排序
		String orderSql = " order by " + pageParam.getoTag() + ".";
		if (StringUtils.isNotBlank(pageParam.getOrderColumn()) && pageParam.getOrderColumn().indexOf("pre") > -1) {// 特殊处理
			orderSql += pageParam.getOrderColumn().replace("_pre", "");
		} else {
			orderSql += pageParam.getOrderColumn();
		}
		orderSql += " " + pageParam.getOrderDir();

		String searchFields = "t1.prd_img,t1.prd_name,t1.item_id,t1.cat_path,t2.avg_price,t2.avg_price_tran,t3.avg_price_tran as avg_price_tran_pre,"
		 +" t2.zk_rate, t3.zk_rate as zk_rate_pre,t2.sales_volume, t3.sales_volume as sales_volume_pre,t2.sales_amount, t3.sales_amount as sales_amount_pre, date_format(t2.createtime, '%Y-%m-%d') as createtime";
		String pageSql = "select " + searchFields + reSql + orderSql + " limit " + pageParam.getStart() + ","
				+ pageParam.getLength();

		List<GoodsList> pageList = sqlUtil.searchList(GoodsList.class, pageSql, params.toArray());

		pageEntity.setData(pageList);

		pageEntity.setDraw(pageParam.getDraw());
		return pageEntity;
	}
	
	/**
	 * 宝贝分析-销售趋势
	 * @param showType
	 * @param startDate
	 * @param endDate
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<AdvertBase> getSalesTrend(String adid, String showType, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String sql = "";
		if("day".equals(showType)){
			sql = " select t.tran_date,sum(t.sales_amount) as sales_amount, sum(t.sales_volume) as sales_volume, sum(t.tran_count) as tran_count from tbdaily.tb_tran_day t "
					+" where exists (select 'X' from tbweb.tb_attn_dir_detail t2 where t2.adid = ? and t.shop_id = t2.shop_id and t.item_id = t2.item_id)"
					+" and t.tran_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d') group by t.tran_date";
			
		}else if("month".equals(showType)){
			sql = " select * from (select t.tran_month as tran_date,sum(t.sales_amount) as sales_amount, sum(t.sales_volume) as sales_volume, sum(t.tran_count) as tran_count from tbdaily.tb_tran_month t "
				 +" where exists (select 'X' from tbweb.tb_attn_dir_detail t2 where t2.adid = ? and t.shop_id = t2.shop_id and t.item_id = t2.item_id)"
				 +" and str_to_date(t.tran_month,'%Y-%m') between str_to_date(?, '%Y-%m') and str_to_date(?, '%Y-%m') group by t.tran_month) tt";
		}
		
		String pageSql = "";
		if(StringUtils.isNotBlank(pageParam.getOrderColumn())){
			pageSql = pageParam.buildSql(sql);
		}else{
			if("day".equals(showType)){
				sql += "  order by t.tran_date";
				
			}else if("month".equals(showType)){
				sql += "  order by tt.tran_date";
			}
			pageSql = sql;
		}
		
		List<AdvertBase> list = sqlUtil.searchList(AdvertBase.class, pageSql, adid, startDate, endDate);
		
		return PageEntity.getPageEntity(pageParam, list);
		
	}
	
	/**
	 * 店铺分析-类目分析
	 * @param shopId
	 * @param startDate
	 * @param endDate
	 * @param orderWay
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<CatData> getCatAnalysis(String shopId, String startDate, String endDate, String orderWay, PageParam pageParam) throws Exception{
		
		String sql = "select t4.cat_name, t3.* from ("
				+" select t1.cat_no, ifnull(sum(t2.sales_volume),0) as sales_volume, ifnull(sum(t2.sales_amount),0) as sales_amount,ifnull(sum(t2.tran_count),0) as tran_count from tbbase.tb_base_product t1"
				+" left join tbdaily.tb_tran_month_cat t2 on t1.cat_no = t2.cat_no and str_to_date(t2.tran_month,'%Y-%m') between str_to_date(?, '%Y-%m') and str_to_date(?, '%Y-%m')"
				+" where t1.shop_id = ?"
				+" group by t1.cat_no ) t3 join tbbase.tb_base_cat_api t4 on t3.cat_no = t4.cat_no ";
		
		String pageSql = "";
		if(StringUtils.isNotBlank(pageParam.getOrderColumn())){
			pageSql = pageParam.buildSql(sql);
		}else{
			sql += "  order by t3.sales_"+orderWay+"";
			pageSql = sql;
		}
		
		List<CatData> list = sqlUtil.searchList(CatData.class, pageSql, startDate, endDate, shopId);
		
		return PageEntity.getPageEntity(pageParam, list);
	}
	
	/**
	 * 店铺分析-店铺详情
	 * @param shopId
	 * @return
	 * @throws Exception
	 */
	public ShopSearch getShopDetail(String shopId) throws Exception{
		
		 String sql = "SELECT t1.shop_name,t1.item_count,t1.region,t2.sales_amount,t3.sales_amount as pre_sales_amount,"
				 +" t2.sales_volume,t3.sales_volume as pre_sales_volume,t2.tran_count,t3.tran_count as pre_tran_count,"
				 +" t1.shop_id, t1.seller, t1.shop_img, t4.html_source FROM tbbase.tb_base_shop t1"
				 +" left join tbdaily.tb_tran_month_shop t2 on t1.shop_id = t2.shop_id and t2.tran_month = ?"
				 +" left join tbdaily.tb_tran_month_shop t3 on t1.shop_id = t3.shop_id and t2.tran_month = ?"
				 +" left join tbbase.tb_shop t4 on t1.shop_id = t4.shop_id"
				 +" where t1.shop_id = ?";
		 
		 return sqlUtil.search(ShopSearch.class, sql, DateUtils.getCurMonth(), DateUtils.getOffsetMonth(-1), shopId);
		
	}
	
	/**
	 * 店铺分析-动态评分
	 * @param shopId
	 * @return
	 * @throws Exception
	 */
	public PageEntity<ShopRate> getDynamicRate(String shopId, String startDate, String endDate, PageParam pageParam) throws Exception{
		
		String sql = " SELECT * FROM tbdaily.tb_shop_rate where shop_id = ? and catch_date between str_to_date(?, '%Y-%m-%d') and str_to_date(?, '%Y-%m-%d')";
		
		List<ShopRate> list = sqlUtil.searchList(ShopRate.class, pageParam.buildSql(sql), shopId, startDate, endDate);
		
		return PageEntity.getPageEntity(pageParam, list);
		
	}
	
	/**
	 * 宝贝搜索
	 * @param category
	 * @param shopName
	 * @param region
	 * @param shopType
	 * @param prdName
	 * @param notPrdName
	 * @param startAvgPrice
	 * @param endAvgPrice
	 * @param startAvgPriceTranPre
	 * @param endAvgPriceTranPre
	 * @param startVolumePre
	 * @param endVolumePre
	 * @param startVolume
	 * @param endVolume
	 * @param startAmountPre
	 * @param endAmountPre
	 * @param startAmount
	 * @param endAmount
	 * @param pageParam
	 * @return
	 * @throws Exception
	 */
	public PageEntity<HotGoods> getGoodsSearchList(String uid, String category, String shopName, String region, String shopType, String prdName, String notPrdName, String startAvgPrice, String endAvgPrice,
		String startAvgPriceTranPre, String endAvgPriceTranPre, String startVolumePre, String endVolumePre, String startVolume, String endVolume, String startAmountPre, String endAmountPre,
		String startAmount, String endAmount, PageParam pageParam) throws Exception{
		
		PageEntity<HotGoods> pageEntity = new PageEntity<HotGoods>();
		
		String reSql = " from tbbase.tb_base_product t1"
				+ " join tbbase.tb_base_shop t2 on t1.shop_id = t2.shop_id"
				+ " left join tbdaily.tb_tran_month t3 on t1.shop_id = t3.shop_id and t1.item_id = t3.item_id and t3.tran_month = ?"
				+ " left join tbdaily.tb_tran_month t4 on t1.shop_id = t4.shop_id and t1.item_id = t4.item_id and t4.tran_month = ?"
				+ " where 1= 1 ";

		List<Object> params = new ArrayList<Object>();
		params.add(DateUtils.getCurMonth());
		params.add(DateUtils.getOffsetMonth(-1, "yyyy-MM"));
		
		
		if (StringUtils.isNotBlank(category)) {
			
			reSql += " and t1.cat_path like '" + category + "%'";
		}
		if (StringUtils.isNotBlank(shopName)) {
			reSql += " and t2.shop_name like '%" + shopName + "%'";
		}
		if (StringUtils.isNotBlank(region)) {
			reSql += " and t1.region like '%" + region + "%'";
		}
		if(!"ALL".equals(shopType)){
			reSql += " and t2.shop_type = ?";
			params.add(shopType);
		}
		if (StringUtils.isNotBlank(prdName)) {
			reSql += " and t1.prd_name like '%" + prdName + "%'";
		}
		if (StringUtils.isNotBlank(notPrdName)) {
			reSql += " and t1.prd_name not like '%" + notPrdName + "%'";
		}
		
		if (StringUtils.isNotBlank(startAvgPrice)) {
			reSql += " and t3.avg_price >= ?";
			params.add(startAvgPrice);
		}
		if (StringUtils.isNotBlank(endAvgPrice)) {
			reSql += " and t3.avg_price <= ?";
			params.add(endAvgPrice);
		}
		if (StringUtils.isNotBlank(startAvgPriceTranPre)) {
			reSql += " and t4.avg_price_tran >= ?";
			params.add(startAvgPriceTranPre);
		}
		if (StringUtils.isNotBlank(endAvgPriceTranPre)) {
			reSql += " and t4.avg_price_tran <= ?";
			params.add(endAvgPriceTranPre);
		}
		
		if (StringUtils.isNotBlank(startVolumePre)) {
			reSql += " and t4.sales_volume >= ?";
			params.add(startVolumePre);
		}
		if (StringUtils.isNotBlank(endVolumePre)) {
			reSql += " and t4.sales_volume <= ?";
			params.add(endVolumePre);
		}
		
		if (StringUtils.isNotBlank(startVolume)) {
			reSql += " and t3.sales_volume >= ?";
			params.add(startVolume);
		}
		if (StringUtils.isNotBlank(endVolume)) {
			reSql += " and t3.sales_volume <= ?";
			params.add(endVolume);
		}
		
		if (StringUtils.isNotBlank(startAmountPre)) {
			reSql += " and t4.sales_amount >= ?";
			params.add(startAmountPre);
		}
		if (StringUtils.isNotBlank(endAmountPre)) {
			reSql += " and t4.sales_amount <= ?";
			params.add(endAmountPre);
		}
		
		if (StringUtils.isNotBlank(startAmount)) {
			reSql += " and t3.sales_amount >= ?";
			params.add(startAmount);
		}
		if (StringUtils.isNotBlank(endAmount)) {
			reSql += " and t3.sales_amount <= ?";
			params.add(endAmount);
		}
		
		String totalSql = "select count(0) as recordsTotal "+  reSql;
		Map<String, Object> totalMap = sqlUtil.search(totalSql, params.toArray());

		long recordsTotal = StringUtils.toLong(totalMap.get("recordsTotal"));
		pageEntity.setRecordsFiltered(recordsTotal);
		pageEntity.setRecordsTotal(recordsTotal);

		// 排序
		String orderSql = " order by ";
		if(StringUtils.isNotBlank(pageParam.getoTag())){
			orderSql = " order by " + pageParam.getoTag() + ".";
		}
		
		if (StringUtils.isNotBlank(pageParam.getOrderColumn()) && pageParam.getOrderColumn().indexOf("pre") > -1) {// 特殊处理
			orderSql += pageParam.getOrderColumn().replace("_pre", "");
		} else {
			orderSql += pageParam.getOrderColumn();
		}
		
		orderSql += " " + pageParam.getOrderDir();

		String pageSql = "select concat_ws(',',tt2.shop_id, tt2.item_id) as asid, tt1.* from ("
				+ " select t1.prd_img,t1.item_id,t1.prd_name,t2.shop_name,t2.shop_id,t2.shop_type,t3.avg_price,t4.avg_price_tran as avg_price_tran_pre,t3.sales_volume,t4.sales_volume as sales_volume_pre,"
				  +" t3.sales_amount, t4.sales_amount as sales_amount_pre,t3.tran_count,t4.tran_count as tran_count_pre,t1.region, date_format(t3.createtime,'%Y-%m-%d') as createtime" 
				  +  reSql +  orderSql + " limit " + pageParam.getStart() + ","+ pageParam.getLength()
				  + " ) tt1"
				  + " left join (select a1.shop_id, a1.item_id from tbweb.tb_attn_dir_detail a1 join tbweb.tb_attn_dir a2 on a2.adid = a1.adid"
				  + " where a2.uid = '"+uid+"' group by a1.shop_id, a1.item_id)  tt2 on tt1.shop_id = tt2.shop_id and tt1.item_id = tt2.item_id";

		List<HotGoods> pageList = sqlUtil.searchList(HotGoods.class, pageSql, params.toArray());

		pageEntity.setData(pageList);

		pageEntity.setDraw(pageParam.getDraw());
		return pageEntity;
	}

	
}
