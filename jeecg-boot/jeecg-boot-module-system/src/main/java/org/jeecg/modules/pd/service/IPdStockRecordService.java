package org.jeecg.modules.pd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.modules.pd.entity.PdStockRecordDetail;
import org.jeecg.modules.pd.entity.PdStockRecord;
import com.baomidou.mybatisplus.extension.service.IService;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * @Description: 出入库记录表
 * @Author: jiangxz
 * @Date:   2020-02-13
 * @Version: V1.0
 */
public interface IPdStockRecordService extends IService<PdStockRecord> {

	/**
	 * 添加一对多
	 * 
	 */
	public void saveMain(PdStockRecord pdStockRecord, List<PdStockRecordDetail> pdStockRecordDetailList) ;
	
	/**
	 * 修改一对多
	 * 
	 */
	public void updateMain(PdStockRecord pdStockRecord, List<PdStockRecordDetail> pdStockRecordDetailList);
	
	/**
	 * 删除一对多
	 */
	public void delMain(String id);
	
	/**
	 * 批量删除一对多
	 */
	public void delBatchMain(Collection<? extends Serializable> idList);

	/**
	 * 查询列表
	 * @param pdStockRecord
	 * @return
	 */
	List<PdStockRecord> queryList(PdStockRecord pdStockRecord);

	/**
	 * 分页查询列表
	 * @param pageList
	 * @param pdStockRecord
	 * @return
	 */
	Page<PdStockRecord> queryList(Page<PdStockRecord> pageList, PdStockRecord pdStockRecord);

	/**
	 * 获取一条记录
	 * @param pdStockRecord
	 * @return
	 */
	PdStockRecord getOne(PdStockRecord pdStockRecord);

	/**
	 * 审批
	 */
	String audit(PdStockRecord auditEntity,PdStockRecord entity);
	
}
