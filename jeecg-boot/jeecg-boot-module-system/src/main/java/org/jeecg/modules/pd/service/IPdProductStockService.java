package org.jeecg.modules.pd.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.pd.entity.PdProductStock;
import org.jeecg.modules.pd.entity.PdProductStockTotal;
import org.jeecg.modules.pd.vo.PdProductStockTotalPage;

import java.util.List;

/**
 * @Description: 库存明细表
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
public interface IPdProductStockService extends IService<PdProductStock> {


	/**
	 * 分页查询
	 * @param pageList
	 * @param productStock
	 * @return
	 */
	Page<PdProductStock> selectList(Page<PdProductStock> pageList, PdProductStock productStock);

	public List<PdProductStock> selectByMainId(String mainId);

    List<PdProductStock> selectList(PdProductStock pdProductStock);

	/**
	 * 更新库存明细表过期状态
	 */
	public void updateProductStock(PdProductStock productStock);

}
