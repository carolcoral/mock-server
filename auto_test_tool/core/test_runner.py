"""
测试运行器 - 管理测试用例执行、结果收集和统计
"""

import time
import traceback
from typing import List, Dict, Callable, Any, Optional, Tuple
from dataclasses import dataclass, field
from enum import Enum


class TestStatus(Enum):
    PASS = "PASS"
    FAIL = "FAIL"
    SKIP = "SKIP"
    ERROR = "ERROR"
    WARN = "WARN"


@dataclass
class TestCase:
    """测试用例"""
    id: str
    name: str
    description: str = ""
    category: str = ""
    status: TestStatus = TestStatus.SKIP
    duration_ms: float = 0
    error_message: str = ""
    details: Dict = field(default_factory=dict)


@dataclass
class TestSuite:
    """测试套件"""
    name: str
    description: str = ""
    cases: List[TestCase] = field(default_factory=list)
    start_time: float = 0
    end_time: float = 0

    @property
    def passed(self) -> int:
        return sum(1 for c in self.cases if c.status == TestStatus.PASS)

    @property
    def failed(self) -> int:
        return sum(1 for c in self.cases if c.status == TestStatus.FAIL)

    @property
    def skipped(self) -> int:
        return sum(1 for c in self.cases if c.status == TestStatus.SKIP)

    @property
    def errors(self) -> int:
        return sum(1 for c in self.cases if c.status == TestStatus.ERROR)

    @property
    def warnings(self) -> int:
        return sum(1 for c in self.cases if c.status == TestStatus.WARN)

    @property
    def total(self) -> int:
        return len(self.cases)

    @property
    def duration_seconds(self) -> float:
        return self.end_time - self.start_time


@dataclass
class TestReport:
    """完整测试报告"""
    suites: List[TestSuite] = field(default_factory=list)
    start_time: float = 0
    end_time: float = 0
    config_warnings: List[str] = field(default_factory=list)

    @property
    def total_cases(self) -> int:
        return sum(s.total for s in self.suites)

    @property
    def total_passed(self) -> int:
        return sum(s.passed for s in self.suites)

    @property
    def total_failed(self) -> int:
        return sum(s.failed for s in self.suites)

    @property
    def total_skipped(self) -> int:
        return sum(s.skipped for s in self.suites)

    @property
    def total_errors(self) -> int:
        return sum(s.errors for s in self.suites)

    @property
    def effective_total(self) -> int:
        """有效用例总数（排除 SKIP）"""
        return self.total_passed + self.total_failed + self.total_errors

    @property
    def pass_rate(self) -> float:
        """通过率 = 通过 / (通过 + 失败 + 错误)，排除跳过的用例"""
        if self.effective_total == 0:
            return 0.0
        return self.total_passed / self.effective_total * 100

    @property
    def duration_seconds(self) -> float:
        return self.end_time - self.start_time


class TestRunner:
    """测试运行器"""

    def __init__(self, config, client, auth_manager):
        self.config = config
        self.client = client
        self.auth = auth_manager
        self.report = TestReport()

    # ---------- 测试用例执行 ----------

    def run_test(
        self,
        test_id: str,
        name: str,
        test_fn: Callable[[], Tuple[bool, Optional[str], Optional[Dict]]],
        description: str = "",
        category: str = "",
        skip: bool = False,
    ) -> TestCase:
        """
        执行单个测试用例
        test_fn 返回: (是否通过, 错误信息, 额外详情字典)
        """
        case = TestCase(
            id=test_id,
            name=name,
            description=description,
            category=category
        )

        if skip:
            case.status = TestStatus.SKIP
            return case

        start = time.time()
        try:
            passed, error_msg, details = test_fn()
            case.duration_ms = (time.time() - start) * 1000

            if passed:
                case.status = TestStatus.PASS
            else:
                case.status = TestStatus.FAIL
                case.error_message = error_msg or "测试未通过"

            if details:
                case.details = details

        except Exception as e:
            case.duration_ms = (time.time() - start) * 1000
            case.status = TestStatus.ERROR
            case.error_message = f"{type(e).__name__}: {str(e)}"
            if self.config.verbose:
                case.error_message += f"\n{traceback.format_exc()}"

        return case

    # ---------- 测试套件管理 ----------

    def create_suite(self, name: str, description: str = "") -> TestSuite:
        """创建测试套件"""
        return TestSuite(name=name, description=description)

    def run_suite(self, suite: TestSuite) -> TestSuite:
        """运行测试套件"""
        suite.start_time = time.time()

        if self.config.console_progress:
            print(f"\n{'='*60}")
            print(f"  测试套件: {suite.name}")
            if suite.description:
                print(f"  说明: {suite.description}")
            print(f"{'='*60}")

        for i, case in enumerate(suite.cases, 1):
            # 打印进度
            if self.config.console_progress:
                status_icon = {
                    TestStatus.PASS: "✓",
                    TestStatus.FAIL: "✗",
                    TestStatus.SKIP: "⊘",
                    TestStatus.ERROR: "!",
                    TestStatus.WARN: "⚠",
                }.get(case.status, "?")

                # 如果还未执行（SKIP），显示跳过
                if case.status == TestStatus.SKIP:
                    print(f"  [{i}/{suite.total}] {status_icon} {case.name} (跳过)")
                else:
                    color = {
                        TestStatus.PASS: "\033[32m",  # 绿色
                        TestStatus.FAIL: "\033[31m",  # 红色
                        TestStatus.ERROR: "\033[31m",  # 红色
                        TestStatus.WARN: "\033[33m",  # 黄色
                    }.get(case.status, "")
                    reset = "\033[0m"
                    print(f"  [{i}/{suite.total}] {color}{status_icon} {case.name}{reset}")
                    if case.error_message and self.config.verbose:
                        print(f"        {case.error_message[:200]}")

                # 测试间等待
                if i < suite.total:
                    time.sleep(self.config.test_interval_ms / 1000)

        suite.end_time = time.time()

        # 套件汇总
        if self.config.console_progress:
            print(f"\n  --- {suite.name} 完成 ---")
            print(f"  通过: {suite.passed}, 失败: {suite.failed}, "
                  f"跳过: {suite.skipped}, 错误: {suite.errors}, "
                  f"耗时: {suite.duration_seconds:.2f}s")

        return suite

    def add_suite(self, suite: TestSuite):
        """添加测试套件到报告"""
        self.report.suites.append(suite)

    # ---------- 断言辅助 ----------

    def assert_status(self, status_code: int, expected: int = 200) -> Tuple[bool, Optional[str]]:
        """断言 HTTP 状态码"""
        if status_code == expected:
            return True, None
        return False, f"状态码 {status_code} != {expected}"

    def assert_api_success(self, status_code: int, resp: Any) -> Tuple[bool, Optional[str]]:
        """断言 API 响应成功（code == 200）"""
        if not self.client.is_success(status_code, resp):
            msg = resp.get("message", "未知错误") if isinstance(resp, dict) else f"状态码: {status_code}"
            return False, msg
        return True, None

    def assert_not_empty(self, value: Any) -> Tuple[bool, Optional[str]]:
        """断言值不为空"""
        if value is None or (isinstance(value, (list, str, dict)) and len(value) == 0):
            return False, "值为空"
        return True, None

    def assert_contains(self, text: str, substring: str) -> Tuple[bool, Optional[str]]:
        """断言文本包含子串"""
        if substring not in text:
            return False, f"文本不包含 '{substring}'"
        return True, None

    def assert_true(self, condition: bool, msg: str = "条件不成立") -> Tuple[bool, Optional[str]]:
        """通用断言"""
        if not condition:
            return False, msg
        return True, None

    # ---------- 报告 ----------

    def finalize_report(self):
        """完成报告"""
        self.report.end_time = time.time()
        return self.report
