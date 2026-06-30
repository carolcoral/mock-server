"""
Swagger 导入测试 - 记录性测试（均不计入通过率）
测试 Swagger JSON 文件导入和 URL 导入端点
由于导入依赖外部 Swagger 文件/URL，这些均为记录性用例
"""

from core.test_runner import TestRunner, TestSuite


class SwaggerImportTests:
    """
    Swagger 导入功能测试套件（全部 SKIP，仅作记录）

    注意：本项目已全面禁用 Swagger 文档公开访问（CHANGELOG v2.3.0），
    因此 Swagger 相关测试仅作存在性记录，不计入通过率统计。
    """

    def __init__(self, runner: TestRunner):
        self.runner = runner

    def build_suite(self) -> TestSuite:
        suite = self.runner.create_suite(
            name="Swagger 导入测试",
            description="Swagger JSON/URL 导入端点及冲突解决（记录性测试，不计入通过率）"
        )

        # Swagger JSON 文件导入
        suite.cases.append(self.runner.run_test(
            "swagger_import_file",
            "从 Swagger JSON 文件导入",
            lambda: (True, None, None),
            description="POST /api/projects/{id}/import-swagger-file（需要外部文件，仅作记录）",
            category="swagger_import",
            skip=True
        ))

        # Swagger URL 导入
        suite.cases.append(self.runner.run_test(
            "swagger_import_url",
            "从 Swagger URL 导入",
            lambda: (True, None, None),
            description="POST /api/projects/{id}/import-swagger-url（需要外部URL，仅作记录）",
            category="swagger_import",
            skip=True
        ))

        # 导入冲突解决
        suite.cases.append(self.runner.run_test(
            "swagger_import_conflicts",
            "解决导入冲突",
            lambda: (True, None, None),
            description="POST /api/projects/{id}/import-conflicts/resolve（仅作记录）",
            category="swagger_import",
            skip=True
        ))

        # Swagger 访问权限验证
        suite.cases.append(self.runner.run_test(
            "swagger_verify_access",
            "验证 Swagger 访问权限",
            lambda: (True, None, None),
            description="POST /api/auth/verify-swagger-access（仅作记录）",
            category="swagger_import",
            skip=True
        ))

        # Swagger UI 专用登录
        suite.cases.append(self.runner.run_test(
            "swagger_login",
            "Swagger 专用登录",
            lambda: (True, None, None),
            description="POST /api/auth/swagger-login（仅作记录）",
            category="swagger_import",
            skip=True
        ))

        return suite
