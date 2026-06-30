"""
AI 模型管理器 - 模型切换、可用性检测、告警
"""

import time
import requests
from typing import List, Tuple, Optional


class AIModelManager:
    """
    AI 模型管理器
    - 维护可用模型列表
    - 模型不可用时自动切换到下一个
    - 所有模型都不可用时告警
    """

    def __init__(self, config):
        self.config = config
        self.api_url = config.ai_api_url
        self.api_key = config.ai_api_key
        self.available_models: List[str] = list(config.ai_models)
        self.failed_models: List[str] = []
        self.current_model: Optional[str] = None
        self._all_failed_warning = False

    def is_available(self) -> bool:
        """是否有可用模型"""
        return len(self.available_models) > 0

    def get_current_model(self) -> Optional[str]:
        """获取当前可用模型"""
        if self.current_model is None and self.available_models:
            self.current_model = self.available_models[0]
        return self.current_model

    def test_model(self, model: str) -> Tuple[bool, Optional[str]]:
        """
        测试指定模型是否可用
        返回: (是否可用, 错误信息)
        兼容 OpenAI Chat Completions 和 Volces Ark Responses API 格式
        """
        headers = {
            "Content-Type": "application/json",
            "Authorization": f"Bearer {self.api_key}"
        }

        # 先尝试 Responses API 格式（Volces Ark）
        payload = {
            "model": model,
            "input": "Hello, respond with 'OK' only.",
            "max_output_tokens": 10,
        }

        try:
            response = requests.post(
                self.api_url,
                json=payload,
                headers=headers,
                timeout=self.config.ai_timeout
            )

            if response.status_code == 200:
                return True, None
            else:
                error_data = response.json() if response.text else {}
                error_msg = ""
                if isinstance(error_data, dict):
                    err_info = error_data.get("error", {})
                    if isinstance(err_info, dict):
                        error_msg = err_info.get("message", "")
                    else:
                        error_msg = str(err_info)
                if not error_msg:
                    error_msg = response.text[:200] if response.text else f"HTTP {response.status_code}"
                return False, error_msg

        except requests.exceptions.Timeout:
            return False, f"超时（{self.config.ai_timeout}s）"
        except Exception as e:
            return False, str(e)

    def test_all_models(self) -> List[Tuple[str, bool, Optional[str]]]:
        """
        测试所有模型，返回每个模型的测试结果
        返回: [(模型名, 是否可用, 错误信息), ...]
        """
        results = []
        models_to_test = list(self.config.ai_models)

        self.available_models = []
        self.failed_models = []

        for model in models_to_test:
            ok, err = self.test_model(model)
            results.append((model, ok, err))
            if ok:
                self.available_models.append(model)
            else:
                self.failed_models.append(model)

        # 设置当前模型
        if self.available_models:
            self.current_model = self.available_models[0]
            self._all_failed_warning = False
        else:
            self.current_model = None
            self._all_failed_warning = True

        return results

    def switch_to_next_model(self) -> Tuple[bool, Optional[str]]:
        """
        切换到下一个可用模型（当前模型不可用时调用）
        返回: (是否切换成功, 消息)
        """
        if self.current_model and self.current_model in self.available_models:
            self.available_models.remove(self.current_model)
            self.failed_models.append(self.current_model)

        if not self.available_models:
            self.current_model = None
            self._all_failed_warning = True
            return False, "所有模型均已不可用！"

        self.current_model = self.available_models[0]
        return True, f"已切换到模型: {self.current_model}"

    def send_chat(self, prompt: str, max_tokens: int = 100) -> Tuple[bool, Optional[str], Optional[str]]:
        """
        发送 AI 对话请求（自动模型切换）
        返回: (是否成功, 响应文本, 错误信息)
        兼容 OpenAI Chat Completions 和 Volces Ark Responses API 格式
        """
        if not self.is_available():
            return False, None, "没有可用的 AI 模型"

        attempts = 0
        max_attempts = len(self.available_models) + 1

        while attempts < max_attempts:
            model = self.get_current_model()
            if model is None:
                return False, None, "没有可用的 AI 模型"

            headers = {
                "Content-Type": "application/json",
                "Authorization": f"Bearer {self.api_key}"
            }

            # 使用 Responses API 格式（Volces Ark）
            payload = {
                "model": model,
                "input": prompt,
                "max_output_tokens": max_tokens,
            }

            try:
                response = requests.post(
                    self.api_url,
                    json=payload,
                    headers=headers,
                    timeout=self.config.ai_timeout
                )

                if response.status_code == 200:
                    data = response.json()
                    # 尝试提取回复内容 - Responses API 格式
                    content = None
                    # Volces Ark Responses API 格式
                    try:
                        # output 是一个数组，每项有 content 数组
                        for item in data.get("output", []):
                            for c in item.get("content", []):
                                if c.get("type") == "output_text":
                                    content = c.get("text", "")
                                    break
                    except:
                        pass
                    # 兼容 Chat Completions 格式
                    if not content:
                        try:
                            content = data["choices"][0]["message"]["content"]
                        except (KeyError, IndexError):
                            try:
                                content = data.get("output", {}).get("text", "")
                            except:
                                content = str(data)

                    return True, content, None

                else:
                    # 当前模型不可用，切换
                    switched, msg = self.switch_to_next_model()
                    attempts += 1
                    if not switched:
                        return False, None, f"模型 {model} 不可用，且无备用模型: {msg}"
                    continue

            except requests.exceptions.Timeout:
                switched, msg = self.switch_to_next_model()
                attempts += 1
                if not switched:
                    return False, None, f"请求超时，且无备用模型"
                continue

            except Exception as e:
                switched, msg = self.switch_to_next_model()
                attempts += 1
                if not switched:
                    return False, None, f"请求异常: {str(e)}，且无备用模型"
                continue

        return False, None, "所有模型尝试均失败"

    def get_status_report(self) -> dict:
        """获取模型状态报告"""
        return {
            "total_models": len(self.config.ai_models),
            "available_models": self.available_models,
            "failed_models": self.failed_models,
            "current_model": self.current_model,
            "all_failed": self._all_failed_warning,
            "warning": "所有AI模型均不可用！请检查API配置。" if self._all_failed_warning else None
        }
