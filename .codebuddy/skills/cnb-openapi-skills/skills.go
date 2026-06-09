package skills

import (
	"bytes"
	"embed"
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"path"
	"regexp"
	"strings"
	"time"
)

// DefaultAPIEndpoint 是默认的 API 基础地址
const DefaultAPIEndpoint = "https://api.cnb.cool"

// GetAPIEndpoint 从环境变量 CNB_API_ENDPOINT 获取 API 基础地址，默认为 https://api.cnb.cool
func GetAPIEndpoint() string {
	if ep := os.Getenv("CNB_API_ENDPOINT"); ep != "" {
		return strings.TrimRight(ep, "/")
	}
	return DefaultAPIEndpoint
}

//go:embed references/*
var referencesFS embed.FS

//go:embed SKILL.md
var skillContent string

// GetSkill 返回完整的 SKILL.md 内容
func GetSkill() string {
	return skillContent
}

// GetCompactIndex 从 SKILL.md 生成精简索引（每个 API 一行）
// 格式: "- APIName: METHOD /path — 描述 [service/apiname]"
// 相比完整 SKILL.md (~2700行) 大幅缩减 token 消耗
func GetCompactIndex() string {
	lines := strings.Split(skillContent, "\n")

	serviceRe := regexp.MustCompile(`^### (.+?) 服务$`)
	apiRe := regexp.MustCompile(`^#### (.+)$`)
	descRe := regexp.MustCompile(`^\*\*描述：\*\* (.+)$`)
	methodRe := regexp.MustCompile(`^\*\*方法：\*\* (.+)$`)
	pathRe := regexp.MustCompile(`^\*\*路径：\*\* (.+)$`)

	var (
		currentService string
		result         []string
		apiName        string
		method         string
		apiPath        string
		description    string
	)

	for _, line := range lines {
		if m := serviceRe.FindStringSubmatch(line); m != nil {
			if currentService != "" {
				result = append(result, "")
			}
			currentService = m[1]
			result = append(result, "### "+currentService)
			continue
		}
		if m := apiRe.FindStringSubmatch(line); m != nil {
			apiName = m[1]
			continue
		}
		if m := descRe.FindStringSubmatch(line); m != nil {
			description = strings.SplitN(m[1], "。", 2)[0]
			continue
		}
		if m := methodRe.FindStringSubmatch(line); m != nil {
			method = m[1]
			continue
		}
		if m := pathRe.FindStringSubmatch(line); m != nil {
			apiPath = m[1]
			continue
		}
		if strings.HasPrefix(line, "**详细文档：**") && apiName != "" {
			result = append(result, fmt.Sprintf("- %s: %s %s — %s [%s/%s]",
				apiName, method, apiPath, description,
				currentService, strings.ToLower(apiName)))
			apiName, method, apiPath, description = "", "", "", ""
		}
	}

	return strings.Join(result, "\n")
}

// ListServices 返回所有 API 服务分类名称
func ListServices() ([]string, error) {
	entries, err := referencesFS.ReadDir("references")
	if err != nil {
		return nil, fmt.Errorf("failed to read references directory: %w", err)
	}
	var services []string
	for _, e := range entries {
		if e.IsDir() {
			services = append(services, e.Name())
		}
	}
	return services, nil
}

// ListAPIs 返回指定服务下的所有 API 名称（不含 .md 扩展名）
func ListAPIs(service string) ([]string, error) {
	dir := path.Join("references", service)
	entries, err := referencesFS.ReadDir(dir)
	if err != nil {
		services, _ := ListServices()
		return nil, fmt.Errorf("service %q not found. Available: %s", service, strings.Join(services, ", "))
	}
	var apis []string
	for _, e := range entries {
		name := e.Name()
		if !e.IsDir() && strings.HasSuffix(name, ".md") {
			apis = append(apis, strings.TrimSuffix(name, ".md"))
		}
	}
	return apis, nil
}

// GetAPIDoc 获取指定 API 的详细文档
// 支持两种调用方式：
//
//	GetAPIDoc("issues", "listissues")   — 两个参数
//	GetAPIDoc("issues/listissues")      — 单个 "service/apiname" 引用
func GetAPIDoc(serviceOrRef string, api ...string) (string, error) {
	var service, apiName string
	if len(api) == 0 {
		parts := strings.SplitN(serviceOrRef, "/", 2)
		if len(parts) != 2 {
			return "", fmt.Errorf("invalid doc ref %q, expected \"service/apiname\", e.g. \"issues/listissues\"", serviceOrRef)
		}
		service, apiName = parts[0], parts[1]
	} else {
		service, apiName = serviceOrRef, api[0]
	}

	filePath := path.Join("references", service, apiName+".md")
	data, err := referencesFS.ReadFile(filePath)
	if err != nil {
		available, _ := ListAPIs(service)
		if len(available) == 0 {
			services, _ := ListServices()
			return "", fmt.Errorf("service %q not found. Available: %s", service, strings.Join(services, ", "))
		}
		return "", fmt.Errorf("API %q not found in service %q. Available: %s", apiName, service, strings.Join(available, ", "))
	}
	return string(data), nil
}

// BuildSystemPrompt 构建两阶段检索的 System Prompt
// 使用精简索引代替完整 SKILL.md，大幅减少 token 消耗
// API 基础地址从环境变量 CNB_API_ENDPOINT 获取，默认为 https://api.cnb.cool
func BuildSystemPrompt() string {
	endpoint := GetAPIEndpoint()
	return `你是一个 CNB 平台 Agent，能够通过调用 CNB OpenAPI 来完成用户的请求。

## API 基础配置

- API 基础地址: ` + endpoint + `
- 认证方式: Bearer Token
- 请求头: Accept: application/vnd.cnb.api+json, Authorization: Bearer <CNB_TOKEN>

## API 接口索引

以下是所有可用的 API 列表。每行格式: ` + "`API名称: 方法 路径 — 描述 [文档引用]`" + `
方括号中的 ` + "`[service/apiname]`" + ` 是文档引用标识，用于获取详细文档。

` + GetCompactIndex() + `

## 工作流程

1. 用户提出需求后，分析需求并确定需要哪些 API。
2. **获取 API 详细文档**：如果你需要了解某个 API 的详细参数、请求体格式或响应结构，请输出文档查询指令：

` + "```get_api_doc\nservice/apiname\n```" + `

例如，要查看 ListIssues 的详细文档：
` + "```get_api_doc\nissues/listissues\n```" + `

系统会返回该 API 的完整文档（包含参数、请求体、响应格式、cURL 示例等）。

3. **调用 API**：获取到详细文档后，输出可执行的 curl 命令，用 ` + "```bash" + ` 代码块包裹：

` + "```bash\ncurl -X GET \\\n  \"" + endpoint + "/{repo}/-/issues\" \\\n  -H \"Accept: application/vnd.cnb.api+json\" \\\n  -H \"Authorization: Bearer <CNB_TOKEN>\"\n```" + `

4. 系统会执行该 curl 命令并将结果返回给你。
5. 你根据返回结果继续分析，可以继续获取文档或调用 API，或者给出最终回答。
6. 当你要给出最终回答时，正常使用文字回复即可（不要输出代码块）。

## 重要规则

- 在调用不熟悉的 API 前，**先用 get_api_doc 获取详细文档**，确认参数格式
- 路径中的变量要替换为实际值，如 ` + "`{repo}`" + ` 替换为实际仓库路径
- 认证 token 统一使用占位符 ` + "`<CNB_TOKEN>`" + `，系统会自动替换
- 每次只输出一个代码块（get_api_doc 或 bash）
- 如果不确定参数，先调用 GET 接口查询信息
`
}

// Action 表示从 AI 响应中解析出的动作指令
type Action struct {
	Type  string // "get_api_doc" 或 "curl"
	Value string
}

var (
	docActionRe  = regexp.MustCompile("(?s)```get_api_doc\\s*(.*?)```")
	curlActionRe = regexp.MustCompile("(?s)```bash\\s*(curl.*?)```")
	lineContinRe = regexp.MustCompile(`\\\n\s*`)
)

// ParseAction 解析 AI 响应中的动作指令
// 返回 nil 表示 AI 已给出最终回答（无需继续循环）
func ParseAction(content string) *Action {
	if m := docActionRe.FindStringSubmatch(content); m != nil {
		return &Action{Type: "get_api_doc", Value: strings.TrimSpace(m[1])}
	}
	if m := curlActionRe.FindStringSubmatch(content); m != nil {
		value := strings.TrimSpace(m[1])
		value = lineContinRe.ReplaceAllString(value, " ")
		return &Action{Type: "curl", Value: value}
	}
	return nil
}

// CurlResult 表示 curl 命令的执行结果
type CurlResult struct {
	Success bool   `json:"success"`
	Data    any    `json:"data,omitempty"`
	Error   string `json:"error,omitempty"`
}

// ExecCurlOptions 可选配置
type ExecCurlOptions struct {
	Timeout  time.Duration // 命令超时时间，默认 30s
	NoSilent bool          // 设为 true 则不自动追加 -s 静默模式（默认会追加）
}

// ExecCurl 执行 curl 命令
// vars 为占位符替换映射，如 map[string]string{"<CNB_TOKEN>": "xxx", "{repo}": "owner/repo"}
func ExecCurl(curlCmd string, vars map[string]string, opts ...ExecCurlOptions) CurlResult {
	opt := ExecCurlOptions{Timeout: 30 * time.Second}
	if len(opts) > 0 {
		if opts[0].Timeout > 0 {
			opt.Timeout = opts[0].Timeout
		}
		opt.NoSilent = opts[0].NoSilent
	}

	cmd := curlCmd
	for placeholder, value := range vars {
		cmd = strings.ReplaceAll(cmd, placeholder, value)
	}

	if !opt.NoSilent && !strings.Contains(cmd, " -s") {
		cmd = strings.Replace(cmd, "curl", "curl -s", 1)
	}

	c := exec.Command("sh", "-c", cmd)
	var stdout, stderr bytes.Buffer
	c.Stdout = &stdout
	c.Stderr = &stderr

	done := make(chan error, 1)
	go func() { done <- c.Run() }()

	select {
	case err := <-done:
		if err != nil {
			return CurlResult{Success: false, Error: fmt.Sprintf("%s: %s", err.Error(), stderr.String())}
		}
	case <-time.After(opt.Timeout):
		if c.Process != nil {
			_ = c.Process.Kill()
		}
		return CurlResult{Success: false, Error: "command timed out"}
	}

	output := stdout.String()
	var data any
	if err := json.Unmarshal([]byte(output), &data); err != nil {
		data = output
	}
	return CurlResult{Success: true, Data: data}
}
