<template>
  <a-card :bordered="false">
    <!-- 查询区域 -->
    <div class="table-page-search-wrapper">
      <a-form layout="inline" @keyup.enter.native="searchQuery">
        <a-row :gutter="24">
          <a-col :md="6" :sm="8">
            <a-form-item label="编码名称">
              <!--<a-input placeholder="请输入编码名称" v-model="queryParam.name"></a-input>-->
              <a-select
                showSearch
                :value="value"
                placeholder="input search text"
                style="width: 200px"
                :defaultActiveFirstOption="false"
                :showArrow="false"
                :filterOption="false"
                @search="handleSearch"
                @change="handleChange"
                :notFoundContent="null"
              >
                <a-select-option v-for="d in data" :key="d.value">{{d.text}}</a-select-option>
              </a-select>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8">
            <a-form-item label="规则简码">
              <a-input placeholder="请输入规则简码" v-model="queryParam.codeQuery"></a-input>
            </a-form-item>
          </a-col>
          <a-col :md="6" :sm="8" >
            <span style="float: left;overflow: hidden;" class="table-page-search-submitButtons">
              <a-button type="primary" @click="searchQuery" icon="search">查询</a-button>
              <a-button type="primary" @click="searchReset" icon="reload" style="margin-left: 8px">重置</a-button>
              <a @click="handleToggleSearch" style="margin-left: 8px">
                {{ toggleSearchStatus ? '收起' : '展开' }}
                <a-icon :type="toggleSearchStatus ? 'up' : 'down'"/>
              </a>
            </span>
          </a-col>

        </a-row>
      </a-form>
    </div>
    <!-- 查询区域-END -->
    
    <!-- 操作按钮区域 -->
    <div class="table-operator">
      <a-button @click="handleAdd" type="primary" icon="plus">新增</a-button>
      <a-button type="primary" icon="download" @click="handleExportXls('编码规则表')">导出</a-button>
      <a-upload name="file" :showUploadList="false" :multiple="false" :headers="tokenHeader" :action="importExcelUrl" @change="handleImportExcel">
        <a-button type="primary" icon="import">导入</a-button>
      </a-upload>
      <a-dropdown v-if="selectedRowKeys.length > 0">
        <a-menu slot="overlay">
          <a-menu-item key="1" @click="batchDel"><a-icon type="delete"/>删除</a-menu-item>
        </a-menu>
        <a-button style="margin-left: 8px"> 批量操作 <a-icon type="down" /></a-button>
      </a-dropdown>
    </div>

    <!-- table区域-begin -->
    <div>
      <div class="ant-alert ant-alert-info" style="margin-bottom: 16px;">
        <i class="anticon anticon-info-circle ant-alert-icon"></i> 已选择 <a style="font-weight: 600">{{ selectedRowKeys.length }}</a>项
        <a style="margin-left: 24px" @click="onClearSelected">清空</a>
      </div>

      <a-table
        ref="table"
        size="middle"
        bordered
        rowKey="id"
        :columns="columns"
        :dataSource="dataSource"
        :pagination="ipagination"
        :loading="loading"
        :rowSelection="{fixed:true,selectedRowKeys: selectedRowKeys, onChange: onSelectChange}"
        
        @change="handleTableChange">

        <template slot="htmlSlot" slot-scope="text">
          <div v-html="text"></div>
        </template>
        <template slot="imgSlot" slot-scope="text">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无此图片</span>
          <img v-else :src="getImgView(text)" height="25px" alt="图片不存在" style="max-width:80px;font-size: 12px;font-style: italic;"/>
        </template>
        <template slot="fileSlot" slot-scope="text">
          <span v-if="!text" style="font-size: 12px;font-style: italic;">无此文件</span>
          <a-button
            v-else
            :ghost="true"
            type="primary"
            icon="download"
            size="small"
            @click="uploadFile(text)">
            下载
          </a-button>
        </template>

        <span slot="action" slot-scope="text, record">
          <a @click="handleEdit(record)">编辑</a>

          <a-divider type="vertical" />
          <a-dropdown>
            <a class="ant-dropdown-link">更多 <a-icon type="down" /></a>
            <a-menu slot="overlay">
              <a-menu-item>
                <a-popconfirm title="确定删除吗?" @confirm="() => handleDelete(record.id)">
                  <a>删除</a>
                </a-popconfirm>
              </a-menu-item>
            </a-menu>
          </a-dropdown>
        </span>

      </a-table>
    </div>

    <pdEncodingRule-modal ref="modalForm" @ok="modalFormOk"></pdEncodingRule-modal>
  </a-card>
</template>

<script>

  import { JeecgListMixin } from '@/mixins/JeecgListMixin'
  import PdEncodingRuleModal from './modules/PdEncodingRuleModal'
  import { getEncodingRuleList } from '@/api/api'
  import { getAction } from  '@/api/manage'

  let timeout;
  let currentValue;

  function fetch(value, callback) {
    if (timeout) {
      clearTimeout(timeout);
      timeout = null;
    }
    currentValue = value;

    function fake() {
      getAction("/pd/pdEncodingRule/getEncodingRuleList",{name:value}).then((res)=>{
          if (!res.success) {
            this.cmsFailed(res.message);
          }
          if (currentValue === value) {
            const result = res.result;
            const data = [];
            result.forEach(r => {
              data.push({
                value: r.id,
                text: r.name,
              });
            });
            callback(data);
          }
        })
    }
    timeout = setTimeout(fake, 300);
  }
  export default {
    name: "PdEncodingRuleList",
    mixins:[JeecgListMixin],
    components: {
      PdEncodingRuleModal
    },
    data () {
      return {
        description: '编码规则表管理页面',
        data: [],
        value: undefined,
        // 表头
        columns: [
          {
            title: '#',
            dataIndex: '',
            key:'rowIndex',
            width:60,
            align:"center",
            customRender:function (t,r,index) {
              return parseInt(index)+1;
            }
          },
          {
            title:'编码名称',
            align:"center",
            dataIndex: 'name'
          },
          {
            title:'拼音简码',
            align:"center",
            dataIndex: 'py'
          },
          {
            title:'五笔简码',
            align:"center",
            dataIndex: 'wb'
          },
          {
            title:'自定义查询码',
            align:"center",
            dataIndex: 'zdy'
          },
          {
            title:'规则详情',
            align:"center",
            dataIndex: 'codeDetail'
          },
          {
            title:'规则描述',
            align:"center",
            dataIndex: 'codeDesc'
          },
          {
            title:'规则简码',
            align:"center",
            dataIndex: 'codeQuery'
          },
          {
            title:'总位数',
            align:"center",
            dataIndex: 'totalDigit'
          },
          {
            title:'备注',
            align:"center",
            dataIndex: 'remarks'
          },
          {
            title: '操作',
            dataIndex: 'action',
            align:"center",
            scopedSlots: { customRender: 'action' }
          }
        ],
        url: {
          list: "/pd/pdEncodingRule/list",
          delete: "/pd/pdEncodingRule/delete",
          deleteBatch: "/pd/pdEncodingRule/deleteBatch",
          exportXlsUrl: "/pd/pdEncodingRule/exportXls",
          importExcelUrl: "pd/pdEncodingRule/importExcel",
        },
        dictOptions:{
        },
      }
    },
    computed: {
      importExcelUrl: function(){
        return `${window._CONFIG['domianURL']}/${this.url.importExcelUrl}`;
      }
    },
    methods: {
      initDictConfig(){
      },
      handleSearch(value) {
        fetch(value, data => (this.data = data));
      },
      handleChange(value) {
        this.value = value;
        fetch(value, data => (this.data = data));
      },
    }
  }
</script>
<style scoped>
  @import '~@assets/less/common.less'
</style>