package org.jeecg.modules.pd.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.jeecg.modules.pd.entity.PdEncodingRule;
import org.jeecg.modules.pd.entity.PdEncodingRuleDetail;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.jeecg.modules.pd.entity.PdProductRule;

/**
 * @Description: 编码规则详情表
 * @Author: zxh
 * @Date:   2019-12-26
 * @Version: V1.0
 */
public interface PdEncodingRuleDetailMapper extends BaseMapper<PdEncodingRuleDetail> {

    void removeByCodeId(PdEncodingRule pdEncodingRule);

    List<PdEncodingRuleDetail> selectList(PdEncodingRuleDetail pdEncodingRuleDetail);
}
