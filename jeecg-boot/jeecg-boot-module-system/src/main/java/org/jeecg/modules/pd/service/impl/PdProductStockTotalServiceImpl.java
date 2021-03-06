package org.jeecg.modules.pd.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jeecg.modules.pd.entity.PdProductStock;
import org.jeecg.modules.pd.entity.PdProductStockTotal;
import org.jeecg.modules.pd.entity.PdStockRecord;
import org.jeecg.modules.pd.entity.PdStockRecordDetail;
import org.jeecg.modules.pd.mapper.PdProductStockMapper;
import org.jeecg.modules.pd.mapper.PdProductStockTotalMapper;
import org.jeecg.modules.pd.service.IPdProductStockService;
import org.jeecg.modules.pd.service.IPdProductStockTotalService;
import org.jeecg.modules.pd.vo.PdProductStockTotalPage;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 库存总表
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Service
public class PdProductStockTotalServiceImpl extends ServiceImpl<PdProductStockTotalMapper, PdProductStockTotal> implements IPdProductStockTotalService {
	@Autowired
	private IPdProductStockService productStockService;
	@Autowired
	private PdProductStockTotalMapper pdProductStockTotalMapper;
	@Autowired
	private PdProductStockMapper pdProductStockMapper;


	/**
	 * 查询列表
	 * @param page
	 * @param stockTotal
	 * @return
	 */
	@Override
	public Page<PdProductStockTotalPage> selectList(Page<PdProductStockTotalPage> page, PdProductStockTotal stockTotal) {
		return page.setRecords(pdProductStockTotalMapper.selectList(stockTotal));
	}

	@Override
	public List<PdProductStockTotalPage> findListForQuery(PdProductStockTotal stockTotal) {
		return pdProductStockTotalMapper.selectList(stockTotal);
	}


	@Override
	@Transactional
	public void updateProductStockTotal(PdProductStockTotal stockTotal) {
		pdProductStockTotalMapper.updateProductStockTotal(stockTotal);
	}



	/***
	 * 	耗材入库更新库存信息
	 *
	 * @param   pdStockRecord    入库记录
	 * @return  String   更新库存结果  入库成功，返回字符串“true”，否则返回错误信息
	 */
	@Transactional
	public String updateInStock(PdStockRecord pdStockRecord){

		if(pdStockRecord == null || CollectionUtils.isEmpty(pdStockRecord.getPdStockRecordDetailList())){
			return "参数有误";
		}

		String inDeptId = pdStockRecord.getInDepartId();
		String supplierId = pdStockRecord.getSupplierId();
		List<PdStockRecordDetail> stockRecordDetails = pdStockRecord.getPdStockRecordDetailList();

		for(PdStockRecordDetail stockRecordDetail :stockRecordDetails){
			String productId = stockRecordDetail.getProductId();            //产品ID
			String productBarCode = stockRecordDetail.getProductBarCode();  //产品条码
			String productName = stockRecordDetail.getProductName();  //产品名称
			String batchNo = stockRecordDetail.getBatchNo();
			String number=stockRecordDetail.getNumber();
			Double productNum = stockRecordDetail.getProductNum();  //数量
			BigDecimal inPrice = stockRecordDetail.getPurchasePrice();//入库单价
			String huoweiCode = stockRecordDetail.getHuoweiCode(); //货位编号
			//2、增加入库库存
			PdProductStockTotal stockTotalqi = new PdProductStockTotal();
			stockTotalqi.setDeptId(inDeptId);
			stockTotalqi.setProductId(productId);
			//先查询库存总表下是否存在该产品；
			List<PdProductStockTotal> i_productStockTotals = pdProductStockTotalMapper.findForUpdate(stockTotalqi);
			//如果库存总表不存在产品，则新增产品库存总表信息
			if(i_productStockTotals == null || i_productStockTotals.size() == 0){
				PdProductStockTotal productStockTotal = new PdProductStockTotal();
				productStockTotal.setDeptId(inDeptId);  //库房
				productStockTotal.setProductId(productId);    //产品ID
				productStockTotal.setStockNum(productNum);    //入库数量
				if(StringUtils.isNotEmpty(supplierId)){
					productStockTotal.setSupplierId(supplierId);   //供应商
				}
				super.save(productStockTotal);
			}else{ //如果库存总表存在，则增加库存数量
				PdProductStockTotal productStockTotal = i_productStockTotals.get(0);
				productStockTotal.setStockNum(productNum + productStockTotal.getStockNum());
				pdProductStockTotalMapper.updateStockNum(productStockTotal);
			}
			//增加入库库存明细
			PdProductStock i_productStockq = new PdProductStock();
			i_productStockq.setDeptId(inDeptId);
			i_productStockq.setProductId(productId);
			i_productStockq.setProductBarCode(productBarCode);//2019年7月24日16:53:43 放开
			i_productStockq.setBatchNo(batchNo);
			i_productStockq.setHuoweiCode(huoweiCode);
			List<PdProductStock> i_productStocks = pdProductStockMapper.findForUpdate(i_productStockq);
			//如果库存明细表不存在，则新增
			if(i_productStocks == null || i_productStocks.size() == 0){
				PdProductStock productStock = new PdProductStock();
				productStock.setDeptId(inDeptId);
				productStock.setProductId(productId);
				productStock.setProductBarCode(productBarCode);
				productStock.setStockNum(productNum);
				productStock.setProductName(productName);
				productStock.setBatchNo(batchNo);
				productStock.setNumber(number);
				productStock.setHuoweiCode(huoweiCode);
				productStock.setExpDate(stockRecordDetail.getLimitDate());
				if(StringUtils.isNotEmpty(supplierId)){
					productStock.setSupplierId(supplierId);
				}
				productStockService.save(productStock);
			}else{//存在，则增加库存数量
				PdProductStock productStock = i_productStocks.get(0);
				productStock.setStockNum(productNum + productStock.getStockNum());
				pdProductStockMapper.updateStockNum(productStock);
			}
		}
		return "true";
	}


	/***
	 * 	库存出库更新库存信息
	 *
	 * @param  outDeptId       出库科室ID
	 * @param stockRecordDetails   出库明细列表
	 * @return  String   更新库存结果  入库成功，返回字符串“true”，否则返回错误信息
	 */
	@Transactional
	public Map updateOutStock(String outDeptId,List<PdStockRecordDetail> stockRecordDetails){
		Map rtMap = new HashMap<String, String>();
		if(StringUtils.isEmpty(outDeptId) || stockRecordDetails == null
				|| stockRecordDetails.size() == 0){
			rtMap.put("code", "201");
			rtMap.put("msg", "传入参数有误");
			return rtMap;
		}

		for(PdStockRecordDetail stockRecordDetail:stockRecordDetails){
			String productId = stockRecordDetail.getProductId();     //产品ID
			String number=stockRecordDetail.getNumber();          //产品编号
			String productBarCode = stockRecordDetail.getProductBarCode();  //产品条码
			String batchNo = stockRecordDetail.getBatchNo();          //批次号
			Double productNum = stockRecordDetail.getProductNum();  //数量
			PdProductStockTotal stockTotalq = new PdProductStockTotal();
			stockTotalq.setDeptId(outDeptId);
			stockTotalq.setProductId(productId);
			List<PdProductStockTotalPage> productStockTotals = pdProductStockTotalMapper.selectList(stockTotalq);
			if(productStockTotals != null && productStockTotals.size() == 1){
				PdProductStockTotal productStockTotal = productStockTotals.get(0);
				Double stockNum = productStockTotal.getStockNum();
				Double num = stockNum - productNum;
				productStockTotal.setStockNum(num);
				pdProductStockTotalMapper.updateStockNum(productStockTotal);
			}

			PdProductStock o_productStockq = new PdProductStock();
			o_productStockq.setDeptId(outDeptId);
			o_productStockq.setProductId(productId);
			o_productStockq.setProductBarCode(productBarCode);
			o_productStockq.setBatchNo(batchNo);
			List<PdProductStock> productStocks = pdProductStockMapper.selectList(o_productStockq);
			if(productStocks != null && productStocks.size() >= 1){
				PdProductStock productStock = productStocks.get(0);
				Double stockNum = productStock.getStockNum();
				Double num = stockNum - productNum;
				productStock.setStockNum(num);
				pdProductStockMapper.updateStockNum(productStock);
			}
		}
		rtMap.put("code", "200");
		return rtMap;
	}

	/***
	 * 	器械使用更新库存信息
	 * @param deptId     科室ID
	 * @param dosageDetails   使用明细
	 * @return  String   更新库存结果
	 */
	/*@Transactional
	public String updateUseStock(String deptId, List<PdDosageDetail> dosageDetails){
		//1、扣减出库库存，扣减出库库存明细
		for(PdDosageDetail dosageDetail:dosageDetails){
			String productId = dosageDetail.getProdId();    //产品ID
			String prodBarcode = dosageDetail.getProdBarcode();                  //条码
			String batchNo = dosageDetail.getBatchNo();
			Integer productNum_i = dosageDetail.getDosageCount();  //数量
			int productNum = productNum_i.intValue();

			//1、扣减出库库存，扣减出库库存明细
			PdProductStockTotal stockTotalq = new PdProductStockTotal();
			stockTotalq.setStoreroomId(storeroomId);
			stockTotalq.setProductId(productId);
			List<PdProductStockTotal> productStockTotals = pdProductStockTotalDao.findList(stockTotalq);
			if(productStockTotals != null && productStockTotals.size() == 1){
				PdProductStockTotal productStockTotal = productStockTotals.get(0);
				Integer stockNum = productStockTotal.getStockNum();
				Integer num = stockNum.intValue() - productNum;
				productStockTotal.setStockNum(num);
				pdProductStockTotalDao.updateStockNum(productStockTotal);
			}

			PdProductStock o_productStockq = new PdProductStock();
			o_productStockq.setStoreroomId(storeroomId);
			o_productStockq.setProductId(productId);
			o_productStockq.setProductBarCode(prodBarcode);
			o_productStockq.setBatchNo(batchNo);
			List<PdProductStock> productStocks = pdProductStockDao.findList(o_productStockq);
			if(productStocks != null && productStocks.size() >= 0){
				PdProductStock productStock = productStocks.get(0);
				Integer stockNum = productStock.getStockNum();
				Integer num = stockNum.intValue() - productNum;
				productStock.setStockNum(num);
				pdProductStockDao.updateStockNum(productStock);
			}
		}

		//2、登记日志


		return "true";
	}*/


	/***
	 * 	用量退回更新库存信息
	 * @param  storeroomId     退回库房ID
	 * @param  detailList    退回用量明细
	 * @return  String        更新库存结果
	 */
	/*@Transactional
	public String updateRetunuseStock(String storeroomId, List<PdDosagertDetail> detailList){

		//1、增加库存，增加库存明细
		for(PdDosagertDetail drt:detailList){
			String productId = drt.getProdId();      //产品ID
			String productNo = drt.getProdNo();      //产品编码
			String productBarCode = drt.getProdBarcode();  //产品条码
			String batchNo = drt.getBatchNo();       //产品批号
			Integer productNum_i = drt.getRtCount();  //退回数量
			int rtNum = productNum_i.intValue();

			//2、增加入库库存
			PdProductStockTotal stockTotalqi = new PdProductStockTotal();
			stockTotalqi.setStoreroomId(storeroomId);
			stockTotalqi.setProductId(productId);
			List<PdProductStockTotal> i_productStockTotals = pdProductStockTotalDao.findForUpdate(stockTotalqi);
			//如果库存总表不存在产品，则新增产品库存总表信息
			if(i_productStockTotals == null || i_productStockTotals.size() == 0){

			}
			//如果库存总表存在，则增加库存数量
			else{
				PdProductStockTotal productStockTotal = i_productStockTotals.get(0);
				productStockTotal.setStockNum(rtNum);
				pdProductStockTotalDao.addStock(productStockTotal);
			}

			//增加入库库存明细
			PdProductStock i_productStockq = new PdProductStock();
			i_productStockq.setStoreroomId(storeroomId);
			i_productStockq.setProductId(productId);
			i_productStockq.setProductBarCode(productBarCode);
			i_productStockq.setBatchNo(batchNo);
			i_productStockq.setProductNo(productNo);
			List<PdProductStock> i_productStocks = pdProductStockDao.findForUpdate(i_productStockq);
			//如果库存明细表不存在，则新增
			if(i_productStocks == null || i_productStocks.size() == 0){

			}
			//存在，则增加库存数量
			else{
				PdProductStock productStock = i_productStocks.get(0);
				productStock.setStockNum(rtNum);
				pdProductStockDao.addStock(productStock);
			}
		}

		//2、登记日志


		return "";
	}*/

	/***
	 * 	退货更新库存信息
	 *
	 * @param  outStoreroomId       出库库房ID，允许为空
	 * @param  pdRejectedListHead    退货明细列表，不允许为空
	 * @return Map      更新库存结果  入库成功，返回字符串“true”，否则返回错误信息
	 */
	/*@Transactional(readOnly = false)
	public Map updateRejectionStock(String outStoreroomId, PdRejectedListHead pdRejectedListHead){
		List<PdRejectedListChild> rejectedDetails = pdRejectedListHead.getChild();
		Map rtMap = new HashMap<String, String>();
		if(StringUtils.isEmpty(outStoreroomId) || rejectedDetails == null
				|| rejectedDetails.size() == 0){
			rtMap.put("code", "201");
			rtMap.put("msg", "传入参数有误");
			return rtMap;
		}

		for(PdRejectedListChild rejectedDetail:rejectedDetails){
			String productId = rejectedDetail.getProdId();     //产品ID
			String productNo = rejectedDetail.getProdNo();          //产品编号
			String productBarCode = rejectedDetail.getBarcode();  //产品条码
			String batchNo = rejectedDetail.getBatchNo();          //批次号
			int rejectedNum = rejectedDetail.getRejectedCount();  //退货数量

			//1、
			PdProductStockTotal stockTotalq = new PdProductStockTotal();
			stockTotalq.setStoreroomId(outStoreroomId);
			stockTotalq.setProductId(productId);
			List<PdProductStockTotal> productStockTotals = pdProductStockTotalDao.findForUpdate(stockTotalq);
			if(productStockTotals != null && productStockTotals.size() == 1){
				PdProductStockTotal productStockTotal = productStockTotals.get(0);
				Integer stockNum = productStockTotal.getStockNum();
				Integer num = stockNum.intValue() - rejectedNum;
				if(num < 0){

				}
				productStockTotal.setStockNum(num);
				pdProductStockTotalDao.updateStockNum(productStockTotal);
			}

			PdProductStock o_productStockq = new PdProductStock();
			o_productStockq.setStoreroomId(outStoreroomId);
			o_productStockq.setProductId(productId);
			o_productStockq.setProductNo(productNo);
			o_productStockq.setProductBarCode(productBarCode);
			o_productStockq.setBatchNo(batchNo);
			List<PdProductStock> productStocks = pdProductStockDao.findForUpdate(o_productStockq);
			if(productStocks != null && productStocks.size() >= 1){
				PdProductStock productStock = productStocks.get(0);
				Integer stockNum = productStock.getStockNum();
				Integer num = stockNum.intValue() - rejectedNum;
				if(num < 0){

				}
				productStock.setStockNum(num);
				pdProductStockDao.updateStockNum(productStock);
			}
		}

		//3、登记日志

		rtMap.put("code", "200");
		return rtMap;
	}*/


	/**
	 * 院外退货更新库存信息
	 * @param stockTotal
	 * @return
	 * */
	@Transactional
	public Map<String,String> updateStockNumByProdIdAndDeptId(PdProductStockTotal stockTotal){
		List<PdProductStockTotal> totalList = pdProductStockTotalMapper.findForUpdate(stockTotal);
		PdProductStockTotal total = totalList.get(0);
		Map<String,String> result = new HashMap<String,String>();
		Double before = total.getStockNum();
		Double after = stockTotal.getStockNum();
		if(before >= after){
			stockTotal.setStockNum(before - after);
			pdProductStockTotalMapper.updateForDosagert(stockTotal);
			result.put("code", "200");
			return result;
		}else{
			result.put("code", "500");
			return result;
		}
	}
}
