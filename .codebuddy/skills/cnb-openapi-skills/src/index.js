const fs = require('fs');
const path = require('path');
const { execSync } = require('child_process');

const REFERENCES_DIR = path.join(__dirname, '../references');
const SKILL_PATH = path.join(__dirname, '../SKILL.md');

/** 默认 API 基础地址 */
const DEFAULT_API_ENDPOINT = 'https://api.cnb.cool';

/**
 * 从环境变量 CNB_API_ENDPOINT 获取 API 基础地址，默认为 https://api.cnb.cool
 * @returns {string}
 */
function getAPIEndpoint() {
  const ep = process.env.CNB_API_ENDPOINT;
  return ep ? ep.replace(/\/+$/, '') : DEFAULT_API_ENDPOINT;
}

/**
 * 获取 SKILL.md 完整内容
 * @returns {string}
 */
function getSkill() {
  return fs.readFileSync(SKILL_PATH, 'utf8');
}

/**
 * 从 SKILL.md 生成精简索引（每个 API 一行）
 * 格式: "- APIName: METHOD /path — 描述 [service/apiname]"
 * 相比完整 SKILL.md (~2700行) 大幅缩减 token 消耗
 * @returns {string}
 */
function getCompactIndex() {
  const content = fs.readFileSync(SKILL_PATH, 'utf8');
  const lines = content.split('\n');

  let currentService = '';
  let result = [];
  let apiName = '';
  let method = '';
  let apiPath = '';
  let description = '';

  for (const line of lines) {
    const serviceMatch = line.match(/^### (.+?) 服务$/);
    if (serviceMatch) {
      if (currentService) result.push('');
      currentService = serviceMatch[1];
      result.push(`### ${currentService}`);
      continue;
    }

    const apiMatch = line.match(/^#### (.+)$/);
    if (apiMatch) { apiName = apiMatch[1]; continue; }

    const descMatch = line.match(/^\*\*描述：\*\* (.+)$/);
    if (descMatch) { description = descMatch[1].split('。')[0]; continue; }

    const methodMatch = line.match(/^\*\*方法：\*\* (.+)$/);
    if (methodMatch) { method = methodMatch[1]; continue; }

    const pathMatch = line.match(/^\*\*路径：\*\* (.+)$/);
    if (pathMatch) { apiPath = pathMatch[1]; continue; }

    if (line.startsWith('**详细文档：**') && apiName) {
      result.push(`- ${apiName}: ${method} ${apiPath} — ${description} [${currentService}/${apiName.toLowerCase()}]`);
      apiName = method = apiPath = description = '';
    }
  }

  return result.join('\n');
}

/**
 * 获取所有 API 服务分类列表
 * @returns {string[]} 服务名称数组，如 ['activities', 'ai', 'git', 'issues', ...]
 */
function listServices() {
  return fs.readdirSync(REFERENCES_DIR).filter((name) => {
    return fs.statSync(path.join(REFERENCES_DIR, name)).isDirectory();
  });
}

/**
 * 获取指定服务下的所有 API 文档名称
 * @param {string} service - 服务名称，如 'issues', 'pulls', 'git'
 * @returns {string[]} API 文档文件名数组（不含扩展名）
 */
function listAPIs(service) {
  const dir = path.join(REFERENCES_DIR, service);
  if (!fs.existsSync(dir)) {
    throw new Error(`Service "${service}" not found. Available: ${listServices().join(', ')}`);
  }
  return fs.readdirSync(dir)
    .filter((f) => f.endsWith('.md'))
    .map((f) => f.replace(/\.md$/, ''));
}

/**
 * 获取指定 API 文档内容
 * 支持两种调用方式：
 *   getAPIDoc('issues', 'listissues')   — 两个参数
 *   getAPIDoc('issues/listissues')      — 单个 "service/apiname" 引用
 * @param {string} serviceOrRef - 服务名称 或 "service/apiname" 格式的文档引用
 * @param {string} [api] - API 名称（使用两参数模式时传入）
 * @returns {string} API 文档 Markdown 内容，或错误信息
 */
function getAPIDoc(serviceOrRef, api) {
  let service;
  if (api === undefined) {
    const parts = serviceOrRef.split('/');
    if (parts.length !== 2) {
      return `错误：无效的文档引用格式 "${serviceOrRef}"，应为 "service/apiname"，如 "issues/listissues"`;
    }
    [service, api] = parts;
  } else {
    service = serviceOrRef;
  }

  const filePath = path.join(REFERENCES_DIR, service, `${api}.md`);
  if (!fs.existsSync(filePath)) {
    try {
      const available = listAPIs(service);
      return `错误：API "${api}" not found in service "${service}". Available: ${available.join(', ')}`;
    } catch (err) {
      return `错误：${err.message}`;
    }
  }
  return fs.readFileSync(filePath, 'utf8');
}

/**
 * 构建两阶段检索的 System Prompt
 * 使用精简索引代替完整 SKILL.md，大幅减少 token 消耗
 * API 基础地址从环境变量 CNB_API_ENDPOINT 获取，默认为 https://api.cnb.cool
 * @returns {string}
 */
function buildSystemPrompt() {
  const endpoint = getAPIEndpoint();
  return `你是一个 CNB 平台 Agent，能够通过调用 CNB OpenAPI 来完成用户的请求。

## API 基础配置

- API 基础地址: ${endpoint}
- 认证方式: Bearer Token
- 请求头: Accept: application/vnd.cnb.api+json, Authorization: Bearer <CNB_TOKEN>

## API 接口索引

以下是所有可用的 API 列表。每行格式: \`API名称: 方法 路径 — 描述 [文档引用]\`
方括号中的 \`[service/apiname]\` 是文档引用标识，用于获取详细文档。

${getCompactIndex()}

## 工作流程

1. 用户提出需求后，分析需求并确定需要哪些 API。
2. **获取 API 详细文档**：如果你需要了解某个 API 的详细参数、请求体格式或响应结构，请输出文档查询指令：

\`\`\`get_api_doc
service/apiname
\`\`\`

例如，要查看 ListIssues 的详细文档：
\`\`\`get_api_doc
issues/listissues
\`\`\`

系统会返回该 API 的完整文档（包含参数、请求体、响应格式、cURL 示例等）。

3. **调用 API**：获取到详细文档后，输出可执行的 curl 命令，用 \`\`\`bash 代码块包裹：

\`\`\`bash
curl -X GET \\
  "${endpoint}/{repo}/-/issues" \\
  -H "Accept: application/vnd.cnb.api+json" \\
  -H "Authorization: Bearer <CNB_TOKEN>"
\`\`\`

4. 系统会执行该 curl 命令并将结果返回给你。
5. 你根据返回结果继续分析，可以继续获取文档或调用 API，或者给出最终回答。
6. 当你要给出最终回答时，正常使用文字回复即可（不要输出代码块）。

## 重要规则

- 在调用不熟悉的 API 前，**先用 get_api_doc 获取详细文档**，确认参数格式
- 路径中的变量要替换为实际值，如 \`{repo}\` 替换为实际仓库路径
- 认证 token 统一使用占位符 \`<CNB_TOKEN>\`，系统会自动替换
- 每次只输出一个代码块（get_api_doc 或 bash）
- 如果不确定参数，先调用 GET 接口查询信息
`;
}

/**
 * 解析 AI 响应中的动作指令
 * @param {string} content - AI 响应内容
 * @returns {{ type: 'get_api_doc' | 'curl', value: string } | null} 解析结果，null 表示最终回答
 */
function parseAction(content) {
  const docMatch = content.match(/```get_api_doc\s*([\s\S]*?)```/);
  if (docMatch) {
    return { type: 'get_api_doc', value: docMatch[1].trim() };
  }

  const curlMatch = content.match(/```bash\s*(curl[\s\S]*?)```/);
  if (curlMatch) {
    return { type: 'curl', value: curlMatch[1].trim().replace(/\\\n\s*/g, ' ') };
  }

  return null;
}

/**
 * 执行 curl 命令
 * @param {string} curlCmd - curl 命令字符串
 * @param {Object.<string, string>} [vars={}] - 占位符替换映射，如 { '<CNB_TOKEN>': 'xxx', '{repo}': 'owner/repo' }
 * @param {Object} [options={}] - 可选配置
 * @param {number} [options.timeout=30000] - 命令超时时间（毫秒）
 * @param {boolean} [options.silent=true] - 是否自动追加 -s 静默模式
 * @returns {{ success: boolean, data: any, error?: string }}
 */
function execCurl(curlCmd, vars = {}, options = {}) {
  const { timeout = 30000, silent = true } = options;

  let cmd = curlCmd;
  for (const [placeholder, value] of Object.entries(vars)) {
    cmd = cmd.replaceAll(placeholder, value);
  }

  if (silent && !cmd.includes(' -s')) {
    cmd = cmd.replace(/^curl/, 'curl -s');
  }

  try {
    const output = execSync(cmd, { encoding: 'utf-8', timeout });
    let data;
    try { data = JSON.parse(output); } catch { data = output; }
    return { success: true, data };
  } catch (err) {
    return { success: false, error: err.message };
  }
}

/**
 * 获取 references 目录的绝对路径（供外部工具直接读取文件）
 * @returns {string}
 */
function getReferencesDir() {
  return REFERENCES_DIR;
}

module.exports = {
  getSkill,
  getCompactIndex,
  listServices,
  listAPIs,
  getAPIDoc,
  getReferencesDir,
  getAPIEndpoint,
  buildSystemPrompt,
  parseAction,
  execCurl,
};
