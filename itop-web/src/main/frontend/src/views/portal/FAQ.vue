<template>
  <div class="faq-page">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <span>常见问题 (FAQ)</span>
          <el-input
            v-model="searchText"
            placeholder="搜索问题..."
            prefix-icon="Search"
            style="width: 300px"
            clearable
          />
        </div>
      </template>

      <el-collapse v-model="activeNames" accordion>
        <el-collapse-item
          v-for="faq in filteredFAQs"
          :key="faq.id"
          :name="faq.id"
          :title="faq.question"
        >
          <div class="faq-answer" v-html="faq.answer"></div>
        </el-collapse-item>
      </el-collapse>

      <el-empty v-if="filteredFAQs.length === 0" description="没有找到相关问题" />
    </el-card>

    <!-- Submit Question -->
    <el-card shadow="never" class="submit-question">
      <template #header>
        <span>没有找到答案？</span>
      </template>
      <p>如果您的问题不在上述列表中，请提交工单，我们的服务团队将尽快为您解答。</p>
      <el-button type="primary" @click="$router.push('/portal')">
        <el-icon><Edit /></el-icon>
        提交工单
      </el-button>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

const searchText = ref('')
const activeNames = ref(['1'])

interface FAQ {
  id: string
  question: string
  answer: string
  category: string
}

const faqs: FAQ[] = [
  {
    id: '1',
    question: '如何申请办公软件安装？',
    answer: '<p>您可以通过以下步骤申请办公软件安装：</p><ol><li>登录IT服务门户</li><li>点击"提交服务请求"</li><li>选择软件安装类型</li><li>填写软件名称和安装原因</li><li>提交申请</li></ol><p>一般情况下，软件安装申请会在1-2个工作日内处理完成。</p>',
    category: '软件服务'
  },
  {
    id: '2',
    question: '如何重置邮箱密码？',
    answer: '<p>邮箱密码重置有以下两种方式：</p><h4>方式一：自助重置</h4><p>访问邮件系统登录页面，点击"忘记密码"，按照提示操作。</p><h4>方式二：提交工单</h4><p>如果自助重置失败，请提交工单申请密码重置，我们将为您处理。</p>',
    category: '账号服务'
  },
  {
    id: '3',
    question: '办公电脑出现故障怎么办？',
    answer: '<p>当办公电脑出现故障时，请按以下步骤处理：</p><ol><li>首先尝试重启电脑</li><li>检查电源、网线等连接是否正常</li><li>如果问题依然存在，登录IT服务门户提交事件工单</li><li>在工单中详细描述故障现象</li></ol><p>紧急故障请拨打服务台热线：400-XXX-XXXX</p>',
    category: '硬件服务'
  },
  {
    id: '4',
    question: '如何申请VPN访问权限？',
    answer: '<p>VPN访问权限申请流程：</p><ol><li>登录IT服务门户</li><li>提交服务请求，选择"VPN权限申请"</li><li>填写申请理由和访问时长</li><li>部门负责人审批</li><li>IT部门开通权限</li></ol><p>审批通过后，您将收到VPN账号和使用说明邮件。</p>',
    category: '网络服务'
  },
  {
    id: '5',
    question: '如何申请会议室设备支持？',
    answer: '<p>会议室设备支持申请：</p><ol><li>登录IT服务门户</li><li>提交服务请求</li><li>选择会议室和时间</li><li>说明需要的设备支持（投影仪、视频会议等）</li></ol><p>建议提前1-2天提交申请，以便技术人员提前调试设备。</p>',
    category: '会议服务'
  },
  {
    id: '6',
    question: '工单提交后多久能得到响应？',
    answer: '<p>工单响应时间取决于优先级：</p><table style="width: 100%; border-collapse: collapse;"><tr style="background: #f5f5f5;"><th style="padding: 8px; border: 1px solid #ddd;">优先级</th><th style="padding: 8px; border: 1px solid #ddd;">响应时间</th><th style="padding: 8px; border: 1px solid #ddd;">适用场景</th></tr><tr><td style="padding: 8px; border: 1px solid #ddd;">P1 - 紧急</td><td style="padding: 8px; border: 1px solid #ddd;">15分钟</td><td style="padding: 8px; border: 1px solid #ddd;">系统瘫痪、核心业务中断</td></tr><tr><td style="padding: 8px; border: 1px solid #ddd;">P2 - 高</td><td style="padding: 8px; border: 1px solid #ddd;">30分钟</td><td style="padding: 8px; border: 1px solid #ddd;">重要功能故障</td></tr><tr><td style="padding: 8px; border: 1px solid #ddd;">P3 - 中</td><td style="padding: 8px; border: 1px solid #ddd;">2小时</td><td style="padding: 8px; border: 1px solid #ddd;">一般功能问题</td></tr><tr><td style="padding: 8px; border: 1px solid #ddd;">P4 - 低</td><td style="padding: 8px; border: 1px solid #ddd;">1工作日</td><td style="padding: 8px; border: 1px solid #ddd;">咨询、建议类</td></tr></table>',
    category: '服务说明'
  },
  {
    id: '7',
    question: '如何查看我的工单处理进度？',
    answer: '<p>查看工单处理进度：</p><ol><li>登录IT服务门户</li><li>点击顶部导航"我的工单"</li><li>在工单列表中查看状态</li><li>点击工单可查看详细处理记录</li></ol><p>工单状态说明：</p><ul><li><strong>新建</strong>：工单已提交，等待分派</li><li><strong>处理中</strong>：技术人员正在处理</li><li><strong>已解决</strong>：问题已解决，等待确认</li><li><strong>已关闭</strong>：工单完成并关闭</li></ul>',
    category: '工单管理'
  },
  {
    id: '8',
    question: '如何申请新员工入职IT设备？',
    answer: '<p>新员工入职IT设备申请流程：</p><ol><li>人事部门提前3个工作日通知IT部门</li><li>IT部门准备电脑、账号等资源</li><li>新员工入职当天领取设备</li><li>IT人员协助配置和指导使用</li></ol><p>如需特殊软件或设备，请在入职前一周告知，以便提前准备。</p>',
    category: '入职服务'
  },
  {
    id: '9',
    question: '打印机无法使用怎么办？',
    answer: '<p>打印机故障排查步骤：</p><ol><li>检查打印机电源和连接线</li><li>查看打印机是否有卡纸、缺纸、缺墨</li><li>尝试重启打印机和电脑</li><li>检查打印队列，清除卡住的打印任务</li></ol><p>如果以上步骤无法解决，请提交工单，注明打印机型号和故障现象。</p>',
    category: '办公设备'
  },
  {
    id: '10',
    question: '如何申请数据恢复？',
    answer: '<p>数据恢复申请：</p><ol><li>立即停止对存储设备的写入操作</li><li>登录IT服务门户提交事件工单</li><li>选择"数据恢复"类型</li><li>详细说明：<ul><li>丢失的文件类型</li><li>文件大致删除时间</li><li>文件存储位置</li></ul></li></ol><p><strong>注意：</strong>数据恢复成功率与时间相关，请尽快提交工单。</p>',
    category: '数据服务'
  }
]

const filteredFAQs = computed(() => {
  if (!searchText.value) return faqs
  const keyword = searchText.value.toLowerCase()
  return faqs.filter(faq =>
    faq.question.toLowerCase().includes(keyword) ||
    faq.answer.toLowerCase().includes(keyword) ||
    faq.category.toLowerCase().includes(keyword)
  )
})
</script>

<style scoped lang="scss">
.faq-page {
  max-width: 900px;
  margin: 0 auto;

  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .el-collapse {
    border: none;

    .el-collapse-item__header {
      font-size: 16px;
      font-weight: 500;
    }

    .faq-answer {
      padding: 16px;
      background: #f5f7fa;
      border-radius: 4px;
      line-height: 1.8;

      h4 {
        margin: 16px 0 8px 0;
        color: #303133;

        &:first-child {
          margin-top: 0;
        }
      }

      p, ol, ul {
        margin: 8px 0;
        color: #606266;
      }

      ol, ul {
        padding-left: 24px;
      }

      li {
        margin: 4px 0;
      }

      table {
        margin: 16px 0;
      }
    }
  }

  .submit-question {
    margin-top: 24px;
    text-align: center;

    p {
      color: #8c8c8c;
      margin-bottom: 16px;
    }
  }
}
</style>