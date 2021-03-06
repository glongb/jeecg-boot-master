package org.jeecg.modules.quartz.job;

import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.jeecg.common.constant.PdConstant;
import org.jeecg.common.util.DateUtils;
import org.jeecg.modules.pd.entity.PdProductStock;
import org.jeecg.modules.pd.entity.PdProductStockTotal;
import org.jeecg.modules.pd.service.IPdProductStockService;
import org.jeecg.modules.pd.service.IPdProductStockTotalService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/***
 * 更新库存总表及库存明细表的久存状态定时任务
 */
@Slf4j
public class QuartPdStockLongJob implements Job {
    @Autowired
    private IPdProductStockTotalService pdProductStockTotalService;
    @Autowired
    private IPdProductStockService pdProductStockService;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        /**
         * 定时获取库存总表及明细表数据，进行判断是否超过设置的久存值，并根据规则进行更新久存状态值
         *
         */
        log.info("-------------------更新久存状态开始-------------------");
        List<PdProductStock> list = pdProductStockService.selectList(new PdProductStock());
        Map<String, Set<String>> m=new HashMap<String, Set<String>>();
        for (PdProductStock pdProductStock : list) {
            PdProductStock pd = new PdProductStock();
            String deptId = pdProductStock.getDeptId();
            Integer remind = 7;//久存期提醒
            Date validDate = pdProductStock.getExpDate();
            Date date = new Date();
            if (ObjectUtil.isNotEmpty(validDate)){
                if ((!DateUtils.isSameDay(date, validDate)) && date.after(validDate)) {//否
                    pd.setIsLong(PdConstant.IS_LONG_0);
                }
                String isLong = pd.getIsLong();
                if (StringUtils.isNotEmpty(isLong) && !PdConstant.IS_LONG_0.equals(isLong)) {
                    pd.setId(pdProductStock.getId());
                    pd.setDeptId(deptId);
                    pdProductStockService.updateProductStock(pd);
                    if (m.containsKey(deptId)) {
                        Set<String> pids = (Set<String>) m.get(deptId);
                        String pid = pdProductStock.getProductId();
                        if (pids.contains(pid)) {
                            continue;
                        } else {
                            pids.add(pid);
                            m.put(deptId, pids);
                        }
                    } else {
                        Set<String> pids = new HashSet<String>();
                        String pid = pdProductStock.getProductId();
                        pids.add(pid);
                        m.put(deptId, pids);
                    }
                }
            }
        }


        log.info("-------------------更新的主表产品久存状态-------------------");
        log.info(m.toString());
        Iterator<String> iter = m.keySet().iterator();
        while(iter.hasNext()){
            String deptId = iter.next();
            Set<String> set = m.get(deptId);
            for (String pid : set) {
                PdProductStock productStock = new PdProductStock();
                productStock.setDeptId(deptId);
                productStock.setProductId(pid);
                List<PdProductStock> l = pdProductStockService.selectList(productStock);
                PdProductStock pk = l.get(0);
                PdProductStockTotal stockTotal = new PdProductStockTotal();
                stockTotal.setIsLong(pk.getIsLong());
                stockTotal.setProductId(pid);
                stockTotal.setDeptId(deptId);
                pdProductStockTotalService.updateProductStockTotal(stockTotal);
            }

        }
        log.info("-------------------更新久存状态结束-------------------");
    }


}
