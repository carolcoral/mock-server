package main

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"os"
	"strings"

	skills "cnb.cool/cnb/sdk/cnb-openapi-skills"
)

var (
	cnbToken = os.Getenv("CNB_TOKEN")
	repo     = "looc/test-ci"
	baseURL  = skills.GetAPIEndpoint()
	maxTurns = 10
	curlVars = map[string]string{"<CNB_TOKEN>": cnbToken}
)

type message struct {
	Role    string `json:"role"`
	Content string `json:"content"`
}

func chatWithCNBAI(messages []message) (string, error) {
	body, _ := json.Marshal(map[string]any{
		"model":    "deepseek-chat",
		"stream":   false,
		"messages": messages,
	})

	req, _ := http.NewRequest("POST",
		fmt.Sprintf("%s/%s/-/ai/chat/completions", baseURL, repo),
		bytes.NewReader(body))
	req.Header.Set("Accept", "application/vnd.cnb.api+json")
	req.Header.Set("Authorization", "Bearer "+cnbToken)
	req.Header.Set("Content-Type", "application/json")

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return "", err
	}
	defer resp.Body.Close()

	respBody, _ := io.ReadAll(resp.Body)
	var result struct {
		Choices []struct {
			Message struct {
				Content string `json:"content"`
			} `json:"message"`
		} `json:"choices"`
	}
	if err := json.Unmarshal(respBody, &result); err != nil {
		return "", err
	}
	return result.Choices[0].Message.Content, nil
}

func runAgent(userMessage string) (string, error) {
	fmt.Printf("\n%s\n用户: %s\n%s\n",
		strings.Repeat("=", 60), userMessage, strings.Repeat("=", 60))

	messages := []message{
		{Role: "system", Content: skills.BuildSystemPrompt()},
		{Role: "user", Content: userMessage},
	}

	for turn := 0; turn < maxTurns; turn++ {
		fmt.Printf("\n--- Turn %d ---\n", turn+1)

		aiResponse, err := chatWithCNBAI(messages)
		if err != nil {
			return "", err
		}
		truncated := aiResponse
		if len(truncated) > 5000 {
			truncated = truncated[:5000] + "..."
		}
		fmt.Printf("AI: %s\n", truncated)

		action := skills.ParseAction(aiResponse)
		if action == nil {
			fmt.Println("\n[Agent 完成]")
			return aiResponse, nil
		}

		messages = append(messages, message{Role: "assistant", Content: aiResponse})

		switch action.Type {
		case "get_api_doc":
			fmt.Printf("[获取文档] %s\n", action.Value)
			doc, docErr := skills.GetAPIDoc(action.Value)
			if docErr != nil {
				doc = "错误：" + docErr.Error()
			}
			messages = append(messages, message{
				Role:    "user",
				Content: fmt.Sprintf("以下是 %s 的详细 API 文档:\n\n%s", action.Value, doc),
			})

		case "curl":
			display := action.Value
			if len(display) > 200 {
				display = display[:200] + "..."
			}
			fmt.Printf("[执行] %s\n", display)
			result := skills.ExecCurl(action.Value, curlVars)
			resultJSON, _ := json.MarshalIndent(result.Data, "", "  ")
			messages = append(messages, message{
				Role:    "user",
				Content: fmt.Sprintf("curl 执行结果:\n%s", string(resultJSON)),
			})
		}
	}

	fmt.Println("\n[达到最大轮次限制]")
	return "已达到最大调用轮次，请尝试简化请求。", nil
}

func main() {
	// 示例 1: 查询信息
	// runAgent("获取 looc/test-ci 仓库的最新一个 issue，并且给这个 issue 增加一个评论，评论内容是\"tttt\"")

	// 示例 2: 查询 Issue 列表
	// runAgent("帮我查看 looc/test-ci 仓库的 Issue 列表")

	// 示例 3: 多步操作 - 创建 Issue
	// runAgent("帮我在 looc/test-ci 仓库创建一个 Issue，标题为 \"测试 Agent\"，内容为 \"这是通过 Agent 自动创建的 Issue\"")

	// 示例 4: 复杂查询
	answer, err := runAgent("帮我查询我的仓库墙有哪些仓库")
	if err != nil {
		fmt.Fprintf(os.Stderr, "Error: %v\n", err)
		os.Exit(1)
	}
	_ = answer
}
