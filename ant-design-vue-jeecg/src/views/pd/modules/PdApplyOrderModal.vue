<template>
    <a-modal
      :visible="visible"
      :width="popModal.width"
      :style="popModal.style"
      :maskClosable="disableSubmit"
      :confirmLoading="confirmLoading"
      @cancel="handleCancel"
      :footer="null">
      <template slot="title">
        <div style="width: 100%;height:20px;padding-right:32px;">
          <div style="float: left;">{{ title }}</div>
          <div style="float: right;">
            <a-button icon="fullscreen" style="width:56px;height:100%;border:0" @click="handleClickToggleFullScreen"/>
          </div>
        </div>
      </template>
    <a-spin :spinning="confirmLoading">
      <!-- 主表单区域 -->
      <div style="background:#ECECEC; padding:20px">
        <a-card title="" style="margin-bottom: 10px;">
     <a-form :form="form">
        <a-row>
          <a-col :span="12">
            <a-form-item label="申领单号" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input disabled="disabled" v-decorator="[ 'applyNo', validatorRules.applyNo]"></a-input>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="申领科室" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input disabled="disabled" v-decorator="[ 'deptName', validatorRules.deptName]"></a-input>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="申领人" :labelCol="labelCol" :wrapperCol="wrapperCol">
            <a-input disabled="disabled" v-decorator="[ 'realName', validatorRules.realName]"></a-input>
          </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="申领日期" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <j-date  disabled="disabled" v-decorator="[ 'applyDate', validatorRules.applyDate]" :trigger-change="true" style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item label="申领总数量" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input-number disabled="disabled" v-decorator="[ 'totalNum', validatorRules.totalNum]"  style="width: 100%"/>
            </a-form-item>
          </a-col>
          <a-col :span="12">
            <a-form-item   label="备注" :labelCol="labelCol" :wrapperCol="wrapperCol">
              <a-input  :disabled="disableSubmit"  v-decorator="[ 'remarks', validatorRules.remarks]"  style="width: 100%;height: 60px"/>
            </a-form-item>
          </a-col>
        </a-row>
     </a-form>
      </a-card>
        <a-card style="margin-bottom: 10px;">
          <a-tabs v-model="activeKey" @change="handleChangeTabs">
            <a-tab-pane tab="申领明细表" :key="refKeys[0]" :forceRender="true">
              <div style="margin-bottom: 8px;">
                <a-button v-show="!disableSubmit" type="primary" icon="plus" @click="choice">选择产品</a-button>
                <span style="padding-left: 8px;"></span>
                <a-popconfirm
                  :title="`确定要删除吗?`"
                  @confirm="handleConfirmDelete">
                  <a-button v-show="!disableSubmit" type="primary" icon="minus">删除</a-button>
                  <span class="gap"></span>
                </a-popconfirm>
                <span style="padding-left: 8px;"></span>
                <a-button type="primary" icon="download" @click="exportXls('申领产品列表')">导出</a-button>
              </div>
              <j-editable-table
                bordered
                :ref="refKeys[0]"
                :loading="pdApplyDetailTable.loading"
                :columns="pdApplyDetailTable.columns"
                :dataSource="pdApplyDetailTable.dataSource"
                :maxHeight="500"
                :rowNumber="true"
                :rowSelection="true"
                :disabled="disableSubmit"
                :actionButton="false"
                @valueChange="valueChange"
                style="text-overflow: ellipsis;"
              />
            </a-tab-pane>
          </a-tabs>
        </a-card>
        <a-card style="margin-bottom: 10px;" v-show="disableSubmit">
          <a-form :form="form">
            <a-row>
              <a-col :span="12">
                <a-form-item label="审核意见" :labelCol="labelCol" :wrapperCol="wrapperCol">
                  <a-input :disabled="true" v-decorator="[ 'refuseReason', validatorRules.refuseReason]" placeholder="请输入审核意见" style="width: 100%;height: 80px"/>
                </a-form-item>
              </a-col>
            </a-row>
          </a-form>
        </a-card>
      </div>
    </a-spin>
      <div class="drawer-bootom-button" v-show="!disableSubmit">
        <a-button @click="handleOk('submit')" type="primary" :loading="confirmLoading" style="margin-right: 15px;">提交</a-button>
        <a-button @click="handleOk('save')" type="primary" :loading="confirmLoading" style="margin-right: 15px;">保存草稿</a-button>
        <a-popconfirm title="确定放弃编辑？" @confirm="handleCancel" okText="确定" cancelText="取消">
          <a-button style="margin-right: 15px;">取消</a-button>
        </a-popconfirm>
      </div>
      <pd-apply-detail-add-modal  ref="PdApplyDetailAddModal" @ok="modalFormOk"></pd-apply-detail-add-modal>
    </a-modal>
</template>
<script>

  import pick from 'lodash.pick'
  import { httpAction,getAction,downFile,inArray} from '@/api/manage'
  import { FormTypes,getRefPromise,validateFormAndTables } from '@/utils/JEditableTableUtil'
  import {JEditableTableMixin } from '@/mixins/JEditableTableMixin'
  import JDate from '@/components/jeecg/JDate'
  import JDictSelectTag from "@/components/dict/JDictSelectTag"
  import PdApplyDetailAddModal from './PdChooseProductListModel'

  const VALIDATE_NO_PASSED = Symbol()
  export { FormTypes, VALIDATE_NO_PASSED }
  export default {
    name: 'PdApplyOrderModal',
    mixins: [JEditableTableMixin],
    components: {JDate, JDictSelectTag,PdApplyDetailAddModal},
    data() {
      return {
        model:{},
        disableSubmit:false,
        confirmLoading: false,
        labelCol: {span: 6},
        wrapperCol: {span: 16},
        labelCol2: {span: 3},
        wrapperCol2: {span: 20},
        validatorRules: {
          applyNo:{},
          deptName:{},
          applyDate:{},
          totalNum:{},
          realName:{},
          remarks:{},
          refuseReason:{},
        },
        refKeys: ['pdApplyDetail', ],
        tableKeys:['pdApplyDetail', ],
        activeKey: 'pdApplyDetail',
        // 申领单详细表
        pdApplyDetailTable: {
          loading: false,
          dataSource: [],
          columns: [
            { title: '定数包编号', width:"130px",   key: 'packageId' },
            { title: '定数包名称',  width:"130px", key: 'packageName' },
            { title: '产品ID', key: 'productId', type: FormTypes.hidden },
            { title: '产品名称', width:"250px",  key: 'productName' },
            { title: '产品编号',width:"150px", align:"center", key: 'number' },
            { title: '规格',width:"240px", align:"center", key: 'spec' },
            { title: '型号', width:"240px",align:"center", key: 'version' },
            { title: '单位',width:"50px", align:"center", key: 'unitName' },
            { title: '发货数量', width:"100px",align:"center", key: 'productNum' },
            { title: '申领数量', key: 'applyNum', type: FormTypes.input, width:"80px",
              placeholder: '${title}', defaultValue: '1',
              validateRules: [{ required: true, message: '${title}不能为空' },
                { pattern: '^(?:[1-9][0-9]*(?:\\.[0-9]+)?|0\\.(?!0+$)[0-9]+)$',message: '${title}的格式不正确' }]
            },
            { title: '库存数量', align:"center", key: 'stockNum' },
          ]
        },
        url: {
          add: "/pd/pdApplyOrder/add",
          edit: "/pd/pdApplyOrder/edit",
          exportXlsUrl: "/pd/pdApplyOrder/exportXls",
          pdPurchaseDetail: {
            list: '/pd/pdApplyOrder/queryApplyDetail'
          },
        },
        popModal: {
          title: '这里是标题',
          visible: false,
          width: '100%',
          style: { top: '10px' },
          fullScreen: true
        },
      }
    },
    methods: {
      add () { //初始化新增
        this.pdApplyDetailTable.dataSource = [];
        this.edit({});
        this.applyInfo();
      },
      /* 导出 */
      exportXls(fileName){
        if(!fileName || typeof fileName != "string"){
          fileName = "导出文件"
        }
        if(this.pdApplyDetailTable.dataSource.length>0){
          let param = {"applyNo":this.model.applyNo};
          console.log("导出参数",param)
          downFile(this.url.exportXlsUrl,param).then((data)=>{
            if (!data) {
              this.$message.warning("文件下载失败")
              return
            }
            if (typeof window.navigator.msSaveBlob !== 'undefined') {
              window.navigator.msSaveBlob(new Blob([data]), fileName+'.xls')
            }else{
              let url = window.URL.createObjectURL(new Blob([data]))
              let link = document.createElement('a')
              link.style.display = 'none'
              link.href = url
              link.setAttribute('download', fileName+'.xls')
              document.body.appendChild(link)
              link.click()
              document.body.removeChild(link); //下载完成移除元素
              window.URL.revokeObjectURL(url); //释放掉blob对象
            }
          })
        }else{
          this.$message.warning("产品为空")
          return
        }
      },
      applyInfo() { //新增页面初始化
        getAction("/pd/pdApplyOrder/applyInfo",{}).then((res)=>{
          if (res.success) {
            this.model=res.result;
            this.$nextTick(() => {
              this.form.setFieldsValue(pick(this.model,'applyNo','deptName','totalNum','applyDate','realName','remarks'))
            })
          }
        })
      },

      // 表格数据变更
      valueChange(event) {
         if(event){
          const { type, row, column, value, target } = event;
         if(type === FormTypes.input){
            if(column.key === "applyNum"){
              // 申领数量变更
              let rows = target.getValuesSync({ validate: false });
                  /*if(row.applyNum>row.stockNum){
                    this.$message.error("申领数量不能大于库存数量！");
                    let pdApplyDetailList = this.pdApplyDetailTable.dataSource;
                    for(let i=0;i<pdApplyDetailList.length;i++){
                     let pdApplyDetail=pdApplyDetailList[i];
                      if(pdApplyDetail.id==row.id){
                      target.setValues([{rowKey: row.id, values: {applyNum :pdApplyDetail.applyNum }}]);
                      break;
                      }
                    }
                  }else{*/
                    target.setValues([{rowKey: row.id, values: {applyNum :row.applyNum }}]);
                    // 计算总数量
                    this.getTotalNumAndPrice();
                   /*}*/

            }
          }
        }
      },



      //选择产品
      choice() {
        this.$refs.PdApplyDetailAddModal.show();
        this.$refs.PdApplyDetailAddModal.title = "选择产品";
      },

      handleConfirmDelete() {
        if(this.$refs.pdApplyDetail.selectedRowIds.length > 0){
          this.$refs.pdApplyDetail.removeSelectedRows();
          this.$nextTick(() => {
            // 计算总数量
            this.getTotalNumAndPrice();
          })
        }else{
          this.$message.error("请选择需要删除的数据！")
        }
      },
       // 计算总数量
      getTotalNumAndPrice(){
        this.$refs.pdApplyDetail.getValues((error, values) => {
          let totalNum = 0;
          values.forEach((item, idx) => {
            totalNum+=parseFloat(item.applyNum);
          })
          this.model.totalNum = totalNum;
          this.form.setFieldsValue(pick(this.model,'totalNum'))

        })
      },
      modalFormOk (formData) { //选择产品确定后返回所选择的数据
        let idList = [];
        let values = [];
        let totalNum=0;
        let data= this.pdApplyDetailTable.dataSource;
        for(let j=0;j<data.length;j++) {
          totalNum+=data[j].applyNum;
          let prodId=data[j].productId;
          idList.push(prodId);
        }
        values=data;
        for(let i=0;i<formData.length;i++){
          let prodId=formData[i].productId;
          if(inArray(prodId, idList) ==-1) {
            values.push({
              productId: formData[i].productId,
              number: formData[i].number,
              productName: formData[i].productName,
              spec: formData[i].spec,
              version: formData[i].version,
              unitName: formData[i].unitName,
              applyNum: "1",//默认 1
              stockNum: formData[i].stockNum
            })
            totalNum+=1;//计算总数量
          }
        }
        let model={};
        this.model.totalNum=totalNum;//申领总数量
        this.$nextTick(() => {
          this.form.setFieldsValue(pick(this.model,'totalNum'))
        })
        this.pdApplyDetailTable.dataSource = values;
      },
      handleOk (submitType) { //提交
        if(submitType=="submit"){
          this.model.submitStatus='2';
          this.model.auditStatus='1';
        }
        const that = this;
        // 触发表单验证
            this.getAllTable().then(tables => {
              /** 一次性验证主表和所有的次表 */
              return validateFormAndTables(this.form, tables)
            }).then(allValues => {
              if (typeof this.classifyIntoFormData !== 'function') {
                throw this.throwNotFunction('classifyIntoFormData')
              }
              let formData = this.classifyIntoFormData(allValues)
              // 发起请求
              let pdApplyDetailList=formData.pdApplyDetailList;
              if(pdApplyDetailList.length>0){
                let httpurl = '';
                let method = '';
                if(!this.model.id){
                  httpurl+=this.url.add;
                  method = 'post';
                }else{
                  httpurl+=this.url.edit;
                  method = 'put';
                }
                httpAction(httpurl,formData,method).then((res)=>{
                  if(res.success){
                    that.$message.success(res.message);
                    that.$emit('ok');
                  }else{
                    that.$message.warning(res.message);
                  }
                }).finally(() => {
                  that.confirmLoading = false;
                  that.close();
                })
              }else{
                that.$message.error("请选择产品");
              }
            }).catch(e => {
              if (e.error === VALIDATE_NO_PASSED) {
                // 如果有未通过表单验证的子表，就自动跳转到它所在的tab
                this.activeKey = e.index == null ? this.activeKey : this.refKeys[e.index]
              } else {
                console.error(e)
              }
            })
      },
      /** 调用完edit()方法之后会自动调用此方法 */
      editAfter() {
        let fieldval = pick(this.model,'applyNo','deptName','totalNum','applyDate','realName','remarks','refuseReason')
        this.$nextTick(() => {
          this.form.setFieldsValue(fieldval)
        })
        // 加载子表数据
        if (this.model.id) {
          let params = { applyNo: this.model.applyNo }
          this.requestSubTableData(this.url.pdPurchaseDetail.list, params, this.pdApplyDetailTable)
        }
      },
      /** 整理成formData */
      classifyIntoFormData(allValues) {
        let main = Object.assign(this.model, allValues.formValue)

        return {
          ...main, // 展开
          pdApplyDetailList: allValues.tablesValue[0].values,
        }
      },
      /** 切换全屏显示 */
      handleClickToggleFullScreen() {
        let mode = !this.popModal.fullScreen
        if (mode) {
          this.popModal.width = '100%'
          this.popModal.style.top = '20px'
        } else {
          this.popModal.width = '1200px'
          this.popModal.style.top = '50px'
        }
        this.popModal.fullScreen = mode
      },
      validateError(msg){
        this.$message.error(msg)
      },
      popupCallback(row){
        this.form.setFieldsValue(pick(row,'applyNo','deptName','totalNum','applyDate','realname','remarks','refuseReason'))
      },

    }
  }
</script>

<style scoped>
  .drawer-bootom-button {
    width: 100%;
    text-align: right;
    background: #fff;
    margin-top:10px;
  }
  .tableStyle> tr > th{
    border: 1px solid #e8e8e8;
    text-align: center;
    padding: 16px 16px;
    font-weight: 500;
    color: rgba(0, 0, 0, 0.85);
    background: #fafafa;
    transition: background 0.3s ease;
    display: table-cell;
    vertical-align: inherit;
    box-sizing: border-box;
  }
  .tableStyle> tr > td{
    border:1px solid #e8e8e8;
    text-align: center;
    padding: 1px 16px;
    font-weight: 500;
    box-sizing: border-box;
  }

  .tableStyle> tr > td >input{
    width:45px;
    text-align: center;
  }
</style>

