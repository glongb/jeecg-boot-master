package org.jeecg.modules.pd.entity;

import java.io.Serializable;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;
import java.util.Date;

/**
 * @Description: 调拨明细表
 * @Author: jeecg-boot
 * @Date:   2020-02-25
 * @Version: V1.0
 */
@Data
@TableName("pd_allocation_detail")
public class PdAllocationDetail extends BaseEntity {
    private static final long serialVersionUID = 1L;
    
	/**主键*/
	@TableId(type = IdType.ID_WORKER_STR)
	private String id;
	/**创建人*/
	@Excel(name = "创建人", width = 15)
	private String createBy;
	/**创建日期*/
	@Excel(name = "创建日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**更新人*/
	@Excel(name = "更新人", width = 15)
	private String updateBy;
	/**更新日期*/
	@Excel(name = "更新日期", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	/**所属部门*/
	@Excel(name = "所属部门", width = 15)
	private String sysOrgCode;
	/**调拨单编号*/
	private String allocationNo;
	/**产品ID*/
	@Excel(name = "产品ID", width = 15)
	private String productId;
	/**产品批次号*/
	@Excel(name = "产品批次号", width = 15)
	private String batchNo;
	/**产品条码*/
	@Excel(name = "产品条码", width = 15)
	private String productBarCode;
	/**调拨数量*/
	@Excel(name = "调拨数量", width = 15)
	private Double allocationNum;
	/**调拨时库存数量*/
	@Excel(name = "调拨时库存数量", width = 15)
	private Double stockNum;
	/**产品属性：1、产品 2、定数包*/
	@Excel(name = "产品属性：1、产品 2、定数包", width = 15)
	private String productAttr;
}
