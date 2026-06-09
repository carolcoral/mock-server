const { buildSystemPrompt, parseAction, getAPIDoc, execCurl, getAPIEndpoint } = require('cnb-openapi-skills');

const CNB_TOKEN = process.env.CNB_TOKEN;
const REPO = 'looc/test-ci';
const BASE_URL = getAPIEndpoint();
const MAX_TURNS = 10;

const CURL_VARS = {
  '<CNB_TOKEN>': CNB_TOKEN,
};

// ========== AI 对话 ==========

async function chatWithCNBAI(messages) {
  const response = await fetch(
    `${BASE_URL}/${REPO}/-/ai/chat/completions`,
    {
      method: 'POST',
      headers: {
        'Accept': 'application/vnd.cnb.api+json',
        'Authorization': `Bearer ${CNB_TOKEN}`,
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({ model: 'deepseek-chat', stream: false, messages }),
    }
  );
  const data = await response.json();
  return data.choices[0].message.content;
}

// ========== Agent 主循环 ==========

async function runAgent(userMessage) {
  console.log(`\n${'='.repeat(60)}`);
  console.log(`用户: ${userMessage}`);
  console.log('='.repeat(60));

  const messages = [
    { role: 'system', content: buildSystemPrompt() },
    { role: 'user', content: userMessage },
  ];

  for (let turn = 0; turn < MAX_TURNS; turn++) {
    console.log(`\n--- Turn ${turn + 1} ---`);

    const aiResponse = await chatWithCNBAI(messages);
    console.log(`AI: ${aiResponse.substring(0, 5000)}${aiResponse.length > 5000 ? '...' : ''}`);

    const action = parseAction(aiResponse);

    if (!action) {
      console.log('\n[Agent 完成]');
      return aiResponse;
    }

    messages.push({ role: 'assistant', content: aiResponse });

    if (action.type === 'get_api_doc') {
      console.log(`[获取文档] ${action.value}`);
      const doc = getAPIDoc(action.value);
      messages.push({
        role: 'user',
        content: `以下是 ${action.value} 的详细 API 文档:\n\n${doc}`,
      });
    } else if (action.type === 'curl') {
      console.log(`[执行] ${action.value.substring(0, 200)}${action.value.length > 200 ? '...' : ''}`);
      const result = execCurl(action.value, CURL_VARS);
      const resultStr = JSON.stringify(result.data, null, 2);
    //   console.log(`[curl 结果] ${resultStr.substring(0, 300)}${resultStr.length > 300 ? '...' : ''}`);
      messages.push({
        role: 'user',
        content: `curl 执行结果:\n${resultStr}`,
      });
    }
  }

  console.log('\n[达到最大轮次限制]');
  return '已达到最大调用轮次，请尝试简化请求。';
}

// ========== 运行 ==========

async function main() {
  // 示例 1: 查询信息
  // await runAgent('获取 looc/test-ci 仓库的最新一个 issue，并且给这个 issue 增加一个评论，评论内容是"tttt"');

  // 示例 2: 查询 Issue 列表
  // await runAgent('帮我查看 looc/test-ci 仓库的 Issue 列表');

  // 示例 3: 多步操作 - 创建 Issue
  // await runAgent('帮我在 looc/test-ci 仓库创建一个 Issue，标题为 "测试 Agent"，内容为 "这是通过 Agent 自动创建的 Issue"');

  // 示例 4: 复杂查询
  await runAgent('帮我查询我的仓库墙有哪些仓库');
}

main().catch(console.error);
