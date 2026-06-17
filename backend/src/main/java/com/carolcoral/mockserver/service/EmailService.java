/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.service;

import com.carolcoral.mockserver.entity.EmailConfig;
import com.carolcoral.mockserver.entity.EmailTemplate;
import com.carolcoral.mockserver.entity.VerificationCode;
import com.carolcoral.mockserver.repository.EmailConfigRepository;
import com.carolcoral.mockserver.repository.EmailTemplateRepository;
import com.carolcoral.mockserver.repository.VerificationCodeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.AuthenticationFailedException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 邮件服务
 * 负责 SMTP 邮件发送和验证码管理
 *
 * @author carolcoral
 * @version 1.0
 * @since 2026-06-16
 */
@Tag(name = "邮件服务", description = "邮件发送与验证码管理")
@Service
public class EmailService {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EmailService.class);

    private final EmailConfigRepository emailConfigRepository;
    private final EmailTemplateRepository emailTemplateRepository;
    private final VerificationCodeRepository verificationCodeRepository;

    public EmailService(EmailConfigRepository emailConfigRepository,
                        EmailTemplateRepository emailTemplateRepository,
                        VerificationCodeRepository verificationCodeRepository) {
        this.emailConfigRepository = emailConfigRepository;
        this.emailTemplateRepository = emailTemplateRepository;
        this.verificationCodeRepository = verificationCodeRepository;
    }

    /**
     * 获取启用的邮箱配置
     *
     * @return 邮箱配置
     */
    public Optional<EmailConfig> getActiveEmailConfig() {
        return emailConfigRepository.findFirstByEnabledTrue();
    }

    /**
     * 发送邮件（通用方法）
     *
     * @param to      收件人邮箱
     * @param subject 主题
     * @param content 内容（HTML格式）
     * @return 是否发送成功
     */
    @Operation(summary = "发送邮件")
    public boolean sendEmail(String to, String subject, String content) {
        return sendEmail(to, subject, content, null);
    }

    /**
     * 发送邮件（通用方法，支持收集错误信息）
     *
     * @param to        收件人邮箱
     * @param subject   主题
     * @param content   内容（HTML格式）
     * @param errorRef  错误信息收集器（可为 null）
     * @return 是否发送成功
     */
    @Operation(summary = "发送邮件（带错误收集）")
    public boolean sendEmail(String to, String subject, String content, AtomicReference<String> errorRef) {
        Optional<EmailConfig> configOpt = getActiveEmailConfig();
        if (configOpt.isEmpty()) {
            String msg = "未找到启用的邮箱配置";
            log.warn("邮件发送失败：{}", msg);
            if (errorRef != null) errorRef.set(msg);
            return false;
        }

        EmailConfig config = configOpt.get();
        if (!config.getEnabled()) {
            String msg = "邮箱服务未启用";
            log.warn("邮件发送失败：{}", msg);
            if (errorRef != null) errorRef.set(msg);
            return false;
        }

        try {
            JavaMailSender mailSender = createMailSender(config);
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String from = config.getFromAddress();
            String displayName = config.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                helper.setFrom(displayName + " <" + from + ">");
            } else {
                helper.setFrom(from);
            }
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("邮件发送成功：to={}, subject={}", to, subject);
            return true;
        } catch (AuthenticationFailedException e) {
            String msg = "SMTP认证失败，请检查邮箱账号和密码是否正确（可能需要使用应用专用密码）";
            log.error("邮件发送失败（认证错误）：to={}, error={}", to, e.getMessage());
            if (errorRef != null) errorRef.set(msg);
            return false;
        } catch (MessagingException e) {
            String msg = extractErrorMessage(e);
            log.error("邮件发送失败：to={}, error={}", to, msg != null ? msg : e.getMessage());
            if (errorRef != null) errorRef.set(msg != null ? msg : "邮件发送失败：" + e.getMessage());
            return false;
        } catch (Exception e) {
            String msg = "邮件发送异常：" + e.getMessage();
            log.error("邮件发送失败（未知错误）：to={}, error={}", to, e.getMessage(), e);
            if (errorRef != null) errorRef.set(msg);
            return false;
        }
    }

    /**
     * 发送测试邮件
     *
     * @param to 收件人邮箱
     * @return 包含 success (boolean) 和 error (String, 成功时为 null) 的结果
     */
    @Operation(summary = "发送测试邮件")
    public java.util.Map<String, Object> sendTestEmail(String to) {
        AtomicReference<String> errorRef = new AtomicReference<>();
        boolean success = sendEmail(to, "【Mock Server】邮件发送测试",
                "<div style='padding:20px;font-family:Arial,sans-serif;'>" +
                "<h2 style='color:#667eea;'>Mock Server</h2>" +
                "<p>这是一封测试邮件。</p>" +
                "<p>如果您能收到此邮件，说明邮箱配置正确，邮件发送功能正常。</p>" +
                "<hr style='border:1px solid #eee;'/>" +
                "<p style='color:#999;font-size:12px;'>此邮件由系统自动发送，请勿回复。</p>" +
                "</div>", errorRef);

        java.util.Map<String, Object> result = new java.util.HashMap<>();
        result.put("success", success);
        if (!success) {
            result.put("error", errorRef.get() != null ? errorRef.get() : "邮件发送失败，请检查SMTP配置");
        }
        return result;
    }

    /**
     * 生成验证码（当前时间戳的后6位）
     *
     * @return 6位验证码字符串
     */
    public String generateVerificationCode() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        return timestamp.substring(timestamp.length() - 6);
    }

    /**
     * 发送注册验证码
     *
     * @param email 收件人邮箱
     * @return 验证码（如果发送成功），null（如果发送失败）
     */
    @Operation(summary = "发送注册验证码")
    @Transactional
    public String sendRegisterVerificationCode(String email, Long templateId) {
        // 使该邮箱之前的注册验证码失效
        List<VerificationCode> oldCodes = verificationCodeRepository.findByEmailAndTypeAndUsedFalse(email, "REGISTER");
        for (VerificationCode oldCode : oldCodes) {
            oldCode.setUsed(true);
            verificationCodeRepository.save(oldCode);
        }

        // 生成新验证码
        String code = generateVerificationCode();

        // 保存验证码（有效期5分钟）
        VerificationCode verificationCode = new VerificationCode();
        verificationCode.setEmail(email);
        verificationCode.setCode(code);
        verificationCode.setType("REGISTER");
        verificationCode.setExpiresAt(LocalDateTime.now().plusMinutes(5));
        verificationCode.setUsed(false);
        verificationCodeRepository.save(verificationCode);

        // 获取邮件模板（优先使用指定模板ID）
        Optional<EmailTemplate> templateOpt;
        if (templateId != null) {
            templateOpt = emailTemplateRepository.findById(templateId);
        } else {
            templateOpt = emailTemplateRepository.findFirstByTypeAndEnabledTrue("REGISTER");
        }
        String subject;
        String content;

        if (templateOpt.isPresent() && templateOpt.get().getEnabled()) {
            EmailTemplate template = templateOpt.get();
            subject = template.getSubject();
            content = template.getContent();
        } else {
            // 使用默认模板
            subject = "【Mock Server】注册验证码";
            content = "<div style='padding:20px;font-family:Arial,sans-serif;'>" +
                    "<h2 style='color:#667eea;'>Mock Server</h2>" +
                    "<p>您正在注册 Mock Server 账号，验证码如下：</p>" +
                    "<div style='text-align:center;margin:30px 0;'>" +
                    "<span style='font-size:32px;font-weight:bold;color:#667eea;letter-spacing:8px;'>{{code}}</span>" +
                    "</div>" +
                    "<p>验证码 <strong>5分钟</strong> 内有效，请勿泄露给他人。</p>" +
                    "<hr style='border:1px solid #eee;'/>" +
                    "<p style='color:#999;font-size:12px;'>此邮件由系统自动发送，请勿回复。如非本人操作，请忽略。</p>" +
                    "</div>";
        }

        // 替换占位符
        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        subject = replacePlaceholders(subject, null, email, currentTime, code);
        content = replacePlaceholders(content, null, email, currentTime, code);

        // 发送邮件
        boolean sent = sendEmail(email, subject, content);
        if (sent) {
            log.info("注册验证码已发送：email={}, code={}", email, code);
            return code; // 返回验证码供后续验证
        } else {
            // 发送失败，删除验证码记录
            verificationCode.setUsed(true);
            verificationCodeRepository.save(verificationCode);
            return null;
        }
    }

    /**
     * 验证注册验证码
     *
     * @param email 邮箱
     * @param code  验证码
     * @return 是否验证通过
     */
    @Operation(summary = "验证注册验证码")
    @Transactional
    public boolean verifyRegisterCode(String email, String code) {
        Optional<VerificationCode> codeOpt = verificationCodeRepository
                .findTopByEmailAndCodeAndTypeAndUsedFalseOrderByCreateTimeDesc(email, code, "REGISTER");

        if (codeOpt.isEmpty()) {
            return false;
        }

        VerificationCode vc = codeOpt.get();

        // 检查是否过期
        if (vc.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }

        // 标记为已使用
        vc.setUsed(true);
        verificationCodeRepository.save(vc);
        return true;
    }

    /**
     * 替换模板占位符
     *
     * @param text        模板文本
     * @param username    用户名
     * @param email       邮箱
     * @param currentTime 当前时间
     * @param code        验证码
     * @return 替换后的文本
     */
    private String replacePlaceholders(String text, String username, String email,
                                       String currentTime, String code) {
        if (text == null) return null;

        String result = text;
        if (username != null) {
            result = result.replace("{{username}}", username);
        }
        if (email != null) {
            result = result.replace("{{email}}", email);
        }
        if (currentTime != null) {
            result = result.replace("{{time}}", currentTime);
            result = result.replace("{{loginTime}}", currentTime);
        }
        if (code != null) {
            result = result.replace("{{code}}", code);
        }
        return result;
    }

    /**
     * 创建 JavaMailSender 实例
     */
    private JavaMailSender createMailSender(EmailConfig config) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getSmtpHost());
        sender.setPort(config.getSmtpPort());
        sender.setUsername(config.getUsername());
        sender.setPassword(config.getPassword());

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");

        if (config.getUseSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else {
            props.put("mail.smtp.starttls.enable", "true");
        }

        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

        return sender;
    }

    /**
     * 从 MessagingException 中提取可读的错误信息
     * 递归查找根因，提取最具体的错误描述
     */
    private String extractErrorMessage(MessagingException e) {
        // 优先使用根因的消息
        Throwable cause = e.getCause();
        while (cause != null) {
            if (cause instanceof MessagingException) {
                // 继续深入查找
                Throwable inner = cause.getCause();
                if (inner != null) {
                    cause = inner;
                    continue;
                }
            }
            // 找到非 MessagingException 的根因
            String causeMsg = cause.getMessage();
            if (causeMsg != null && !causeMsg.isEmpty()) {
                return formatErrorByType(causeMsg);
            }
            break;
        }

        // 如果没有根因或根因消息为空，使用当前异常的消息
        String msg = e.getMessage();
        if (msg != null && !msg.isEmpty()) {
            return formatErrorByType(msg);
        }
        return "邮件发送失败，请检查SMTP配置";
    }

    /**
     * 根据错误消息内容，返回更友好的中文提示
     */
    private String formatErrorByType(String rawMessage) {
        if (rawMessage == null) return "邮件发送失败";
        String lower = rawMessage.toLowerCase();

        if (lower.contains("authentication failed") || lower.contains("535") || lower.contains("auth")) {
            return "SMTP认证失败，请检查邮箱账号密码（可能需要使用应用专用密码）：" + rawMessage;
        } else if (lower.contains("connection refused") || lower.contains("connect exception") || lower.contains("timeout")) {
            return "无法连接到SMTP服务器，请检查SMTP地址和端口：" + rawMessage;
        } else if (lower.contains("relay") || lower.contains("denied")) {
            return "邮件发送被拒绝，发件人地址可能未经SMTP服务器授权：" + rawMessage;
        } else if (lower.contains("recipient") || lower.contains("rcpt")) {
            return "收件人地址无效或被拒绝：" + rawMessage;
        } else {
            return "邮件发送失败：" + rawMessage;
        }
    }
}
