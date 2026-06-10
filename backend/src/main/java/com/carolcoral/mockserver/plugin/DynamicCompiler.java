/**
* Copyright (c) 2026, XINDU.SITE
* All Rights Reserved.
* XINDU.SITE CONFIDENTIAL
*/

package com.carolcoral.mockserver.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * 动态Java代码编译器
 * <p>
 * 在运行时编译用户通过页面提交的Java源码，生成实现了CustomResponseTransformer接口的类实例。
 * 编译结果会被缓存，源码不变时直接复用。
 * </p>
 *
 * @author carolcoral
 */
public class DynamicCompiler {

    private static final Logger log = LoggerFactory.getLogger(DynamicCompiler.class);

    private static final JavaCompiler compiler = initCompiler();

    // 源码缓存：apiId -> 上次编译的源码
    private static final ConcurrentHashMap<Long, String> sourceCodeCache = new ConcurrentHashMap<>();
    // 实例缓存：apiId -> 编译后的转换器实例
    private static final ConcurrentHashMap<Long, CustomResponseTransformer> instanceCache = new ConcurrentHashMap<>();
    // 类字节码缓存：apiId -> 编译后的字节码
    private static final ConcurrentHashMap<Long, Class<?>> classCache = new ConcurrentHashMap<>();

    // 编译时classpath
    private static volatile String compilationClasspath = null;

    // 类是否已加载标志
    private static volatile boolean classLoaded = false;

    /**
     * 初始化Java编译器
     * <p>
     * 尝试多种方式获取编译器：直接获取 -> 从JAVA_HOME/lib/tools.jar -> 从当前JVM进程路径
     * </p>
     */
    private static JavaCompiler initCompiler() {
        // 方式1: 直接获取
        JavaCompiler c = ToolProvider.getSystemJavaCompiler();
        if (c != null) {
            log.info("找到Java编译器（ToolProvider）");
            compilationClasspath = buildClasspath();
            log.info("动态编译器初始化成功, classpath长度: {}", compilationClasspath.length());
            return c;
        }

        // 方式2: 从当前JVM的tools.jar获取（仅JDK有效，JRE无效）
        // JDK的lib/tools.jar在Spring Boot fat JAR中被重打包到BOOT-INF/lib
        try {
            String javaHome = System.getProperty("java.home");
            File toolsJar = new File(javaHome + "/lib/tools.jar");
            if (toolsJar.exists()) {
                log.info("找到tools.jar: {}", toolsJar);
                URLClassLoader toolCl = new URLClassLoader(
                        new URL[]{toolsJar.toURI().toURL()},
                        Thread.currentThread().getContextClassLoader());
                Thread.currentThread().setContextClassLoader(toolCl);
                Class<?> toolProviderClass = toolCl.loadClass("com.sun.tools.javac.api.ToolProvider");
                Object tool = toolProviderClass.getMethod("getSystemJavaCompiler").invoke(null);
                if (tool instanceof JavaCompiler) {
                    compilationClasspath = buildClasspath();
                    log.info("动态编译器初始化成功（从tools.jar）");
                    return (JavaCompiler) tool;
                }
            }
        } catch (Exception e) {
            log.warn("从tools.jar获取编译器失败: {}", e.getMessage());
        }

        // 方式3: 从当前运行的java路径推断JDK
        try {
            String javaExe = System.getProperty("java.home") + "/bin/java";
            if (new File(javaExe).exists()) {
                File javaHome = new File(System.getProperty("java.home")).getParentFile();
                if (javaHome != null) {
                    File libDir = new File(javaHome, "lib");
                    File toolsJar = new File(libDir, "tools.jar");
                    if (toolsJar.exists()) {
                        log.info("找到tools.jar（备用路径）: {}", toolsJar);
                        URLClassLoader toolCl = new URLClassLoader(
                                new URL[]{toolsJar.toURI().toURL()},
                                Thread.currentThread().getContextClassLoader());
                        Class<?> toolProviderClass = toolCl.loadClass("com.sun.tools.javac.api.ToolProvider");
                        Object tool = toolProviderClass.getMethod("getSystemJavaCompiler").invoke(null);
                        if (tool instanceof JavaCompiler) {
                            compilationClasspath = buildClasspath();
                            log.info("动态编译器初始化成功（备用路径）");
                            return (JavaCompiler) tool;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从备用路径获取编译器失败: {}", e.getMessage());
        }

        log.error("未找到Java编译器。请确保使用JDK而非JRE运行本应用。");
        return null;
    }

    /**
     * 从当前类加载器构建classpath
     * <p>
     * 支持多种运行环境：IDE直接运行、Maven exec、Spring Boot fat JAR等
     * </p>
     */
    private static String buildClasspath() {
        StringBuilder sb = new StringBuilder();
        Set<String> paths = new LinkedHashSet<>();

        try {
            // 0. 首先尝试获取项目自身的target/classes（最优先，IDE和Maven运行时的主要来源）
            String userDir = System.getProperty("user.dir");
            File targetClasses = new File(userDir, "target/classes");
            if (targetClasses.exists()) {
                paths.add(targetClasses.getAbsolutePath());
                log.info("添加target/classes到classpath: {}", targetClasses.getAbsolutePath());
            }

            // 1. 尝试从URLClassLoader获取classpath（IDE、Maven直接运行）
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            while (cl != null) {
                if (cl instanceof URLClassLoader urlCl) {
                    for (URL url : urlCl.getURLs()) {
                        String path = urlToPath(url);
                        if (path != null && !paths.contains(path)) {
                            paths.add(path);
                        }
                    }
                }
                cl = cl.getParent();
            }

            // 2. 从DynamicCompiler自身所在位置推断classpath
            URL selfUrl = DynamicCompiler.class.getProtectionDomain().getCodeSource().getLocation();
            if (selfUrl != null) {
                String path = urlToPath(selfUrl);
                if (path != null && !paths.contains(path)) {
                    paths.add(path);
                }
            }

            // 3. 从CustomResponseTransformer所在位置推断classpath
            URL pluginUrl = CustomResponseTransformer.class.getProtectionDomain().getCodeSource().getLocation();
            if (pluginUrl != null) {
                String path = urlToPath(pluginUrl);
                if (path != null && !paths.contains(path)) {
                    paths.add(path);
                }
            }

            // 4. 添加系统类路径
            String sysCp = System.getProperty("java.class.path");
            if (sysCp != null && !sysCp.isEmpty()) {
                for (String entry : sysCp.split(File.pathSeparator)) {
                    if (entry != null && !entry.trim().isEmpty()) {
                        String trimmed = entry.trim();
                        if (!paths.contains(trimmed)) {
                            paths.add(trimmed);
                        }
                    }
                }
            }

            // 5. Spring Boot fat JAR处理：扫描BOOT-INF/lib下的所有jar
            List<String> fatJarPaths = new ArrayList<>();
            for (String path : new ArrayList<>(paths)) {
                if (path.endsWith(".jar")) {
                    fatJarPaths.add(path);
                    // 检查是否是Spring Boot fat JAR
                    try (java.util.zip.ZipFile zip = new java.util.zip.ZipFile(path)) {
                        if (zip.getEntry("BOOT-INF/classes/") != null || zip.getEntry("BOOT-INF/lib/") != null) {
                            log.info("检测到Spring Boot fat JAR: {}", path);
                            // 扫描BOOT-INF/lib下的所有jar
                            java.util.Enumeration<? extends java.util.zip.ZipEntry> entries = zip.entries();
                            while (entries.hasMoreElements()) {
                                java.util.zip.ZipEntry entry = entries.nextElement();
                                String name = entry.getName();
                                if (name.startsWith("BOOT-INF/lib/") && name.endsWith(".jar")) {
                                    // 从JAR内读取依赖jar并添加到classpath
                                    // 注意：这里不能直接添加内部路径，需要展开
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.debug("检查fat JAR失败: {}", e.getMessage());
                    }
                }
            }

            // 6. 从Maven本地仓库扫描关键依赖
            String m2Repo = System.getProperty("user.home") + "/.m2/repository";
            File m2Dir = new File(m2Repo);
            if (m2Dir.exists() && m2Dir.canRead()) {
                // 查找常见的关键依赖
                String[][] commonDeps = {
                    // groupId, artifactId prefix
                    {"com", "jayway", "json-path"},
                    {"com", "fasterxml", "jackson-core"},
                    {"com", "fasterxml", "jackson-databind"},
                    {"com", "fasterxml", "jackson-annotations"},
                    {"org", "slf4j", "slf4j-api"},
                    {"org", "springframework", "spring-beans"},
                    {"org", "springframework", "spring-context"},
                    {"org", "springframework", "spring-web"},
                    {"org", "yaml", "snakeyaml"}
                };

                for (String[] dep : commonDeps) {
                    File depBase = new File(m2Repo);
                    for (int i = 0; i < dep.length - 1; i++) {
                        depBase = new File(depBase, dep[i]);
                    }
                    if (depBase.exists()) {
                        String prefix = dep[dep.length - 1];
                        File[] versions = depBase.listFiles((dir, name) -> name.startsWith(prefix));
                        if (versions != null && versions.length > 0) {
                            // 找到最新版本
                            Arrays.sort(versions, (a, b) -> b.getName().compareTo(a.getName()));
                            File latestVersion = versions[0];
                            File jarFile = new File(latestVersion, prefix + ".jar");
                            if (jarFile.exists() && !paths.contains(jarFile.getAbsolutePath())) {
                                paths.add(jarFile.getAbsolutePath());
                            }
                        }
                    }
                }
            }

            // 7. 尝试从父目录查找target目录
            File parentDir = new File(userDir).getParentFile();
            if (parentDir != null) {
                File parentTargetClasses = new File(parentDir, "backend/target/classes");
                if (parentTargetClasses.exists() && !paths.contains(parentTargetClasses.getAbsolutePath())) {
                    paths.add(parentTargetClasses.getAbsolutePath());
                }
            }

        } catch (Exception e) {
            log.warn("构建classpath时出错: {}", e.getMessage(), e);
        }

        // 确保至少包含当前目录
        if (paths.isEmpty()) {
            paths.add(".");
        }

        for (String p : paths) {
            if (sb.length() > 0) {
                sb.append(File.pathSeparator);
            }
            sb.append(p);
        }
        String cp = sb.toString();
        log.info("编译classpath总长度: {} 字符, 路径数量: {}", cp.length(), paths.size());
        log.debug("编译classpath详情: {}", cp);
        return cp;
    }

    private static String urlToPath(URL url) {
        try {
            if (url == null) return null;

            String path = url.getPath();

            // 处理 jar:file: URL格式 (Spring Boot fat JAR)
            if (path.startsWith("jar:file:")) {
                path = path.substring(9); // 去掉 "jar:file:"
            } else if (path.startsWith("file:")) {
                path = path.substring(5); // 去掉 "file:"
            }

            // 去掉Spring Boot fat JAR中的 "!/" 后缀
            int bangIndex = path.indexOf("!");
            if (bangIndex > 0) {
                path = path.substring(0, bangIndex);
            }

            // URL解码（处理 %20 等编码）
            path = java.net.URLDecoder.decode(path, "UTF-8");

            // 验证路径有效
            if (path != null && !path.isEmpty() && !path.equals("/")) {
                // 如果路径是目录，返回目录路径
                File file = new File(path);
                if (file.exists()) {
                    return file.getAbsolutePath();
                }
                // 如果文件存在（可能是jar文件）
                if (new File(path).isFile()) {
                    return path;
                }
                // 返回解码后的路径
                return path;
            }
        } catch (Exception e) {
            log.debug("URL转路径失败: {}, 错误: {}", url, e.getMessage());
        }
        return null;
    }

    private static void findJars(File dir, Set<String> paths) {
        if (!dir.exists()) return;
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                findJars(f, paths);
            } else if (f.getName().endsWith(".jar")) {
                paths.add(f.getAbsolutePath());
            }
        }
    }

    // 安全校验：禁止使用的危险类/包
    private static final List<Pattern> FORBIDDEN_PATTERNS = List.of(
            Pattern.compile("java\\.lang\\.reflect"),
            Pattern.compile("java\\.lang\\.Process"),
            Pattern.compile("java\\.lang\\.Runtime"),
            Pattern.compile("java\\.lang\\.System"),
            Pattern.compile("java\\.lang\\.Thread"),
            Pattern.compile("java\\.lang\\.ClassLoader"),
            Pattern.compile("java\\.lang\\.Class\\b"),
            Pattern.compile("java\\.io\\.File"),
            Pattern.compile("java\\.io\\.RandomAccessFile"),
            Pattern.compile("java\\.net\\."),
            Pattern.compile("java\\.nio\\.file\\."),
            Pattern.compile("javax\\.script\\."),
            Pattern.compile("java\\.util\\.concurrent\\.(?!stream\\.)"),
            Pattern.compile("java\\.sql\\."),
            Pattern.compile("javax\\.sql\\."),
            Pattern.compile("com\\.carolcoral\\.mockserver\\.repository\\."),
            Pattern.compile("org\\.springframework\\.beans\\.factory\\.annotation\\.Autowired"),
            Pattern.compile("org\\.springframework\\.context\\."),
            Pattern.compile("org\\.springframework\\.jdbc\\."),
            Pattern.compile("org\\.hibernate\\."),
            Pattern.compile("jakarta\\.persistence\\.")
    );

    /**
     * 编译并实例化用户提交的源码
     *
     * @param apiId      接口ID
     * @param sourceCode 用户提交的Java源码
     * @return 编译并实例化后的转换器，失败返回null
     * @throws CompilationException 编译失败时抛出
     */
    public static synchronized CustomResponseTransformer compileAndInstantiate(Long apiId, String sourceCode)
            throws CompilationException {
        if (compiler == null) {
            throw new CompilationException("未找到Java编译器，请使用JDK运行");
        }

        // 如果源码没变，直接返回缓存实例
        String cachedSource = sourceCodeCache.get(apiId);
        if (cachedSource != null && cachedSource.equals(sourceCode)) {
            CustomResponseTransformer cached = instanceCache.get(apiId);
            if (cached != null) {
                log.info("使用缓存的动态转换器实例: apiId={}", apiId);
                return cached;
            }
        }

        // 安全校验
        validateSourceCode(sourceCode);

        // 生成唯一的类名
        String className = "DynamicTransformer_" + apiId + "_" + Math.abs(new SecureRandom().nextLong() % 100000);

        // 构建完整的Java源码
        String fullSourceCode = buildFullSourceCode(sourceCode, className);

        // 编译
        Class<?> compiledClass = compile(className, fullSourceCode);

        // 验证实现了接口
        if (!CustomResponseTransformer.class.isAssignableFrom(compiledClass)) {
            throw new CompilationException("代码中的类必须实现 CustomResponseTransformer 接口");
        }

        // 实例化
        CustomResponseTransformer instance;
        try {
            instance = (CustomResponseTransformer) compiledClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new CompilationException("无法实例化类: " + e.getMessage());
        }

        // 缓存
        sourceCodeCache.put(apiId, sourceCode);
        instanceCache.put(apiId, instance);
        classCache.put(apiId, compiledClass);

        log.info("动态编译成功: apiId={}, className={}", apiId, className);
        return instance;
    }

    /**
     * 验证源码是否通过安全校验
     */
    private static void validateSourceCode(String sourceCode) throws CompilationException {
        if (sourceCode == null || sourceCode.trim().isEmpty()) {
            throw new CompilationException("源码不能为空");
        }

        // 检查是否包含public class定义
        if (!Pattern.compile("public\\s+class\\s+\\w+").matcher(sourceCode).find()) {
            throw new CompilationException("源码中必须包含一个 public class 定义");
        }

        // 检查是否实现了接口
        if (!sourceCode.contains("CustomResponseTransformer")) {
            throw new CompilationException("类必须实现 CustomResponseTransformer 接口");
        }

        // 安全扫描：检查是否使用了禁止的类/包
        for (Pattern pattern : FORBIDDEN_PATTERNS) {
            if (pattern.matcher(sourceCode).find()) {
                throw new CompilationException("安全限制：代码中不允许使用 " + pattern.pattern());
            }
        }

        // 检查代码长度限制
        if (sourceCode.length() > 50000) {
            throw new CompilationException("代码长度超过限制（最大50000字符）");
        }
    }

    /**
     * 构建完整的Java源码（添加package、import等）
     */
    private static String buildFullSourceCode(String userCode, String className) {
        StringBuilder sb = new StringBuilder();
        sb.append("package com.carolcoral.mockserver.plugin.dynamic;\n\n");

        // 提取用户代码中的import语句
        String imports = extractImports(userCode);
        sb.append(imports);

        // 注入用户代码（替换类名为动态生成的类名）
        String processedCode = userCode;
        java.util.regex.Matcher classMatcher = Pattern.compile("public\\s+class\\s+(\\w+)").matcher(userCode);
        if (classMatcher.find()) {
            String originalClassName = classMatcher.group(1);
            processedCode = userCode.replaceAll("\\b" + originalClassName + "\\b", className);
        }

        sb.append(processedCode);

        return sb.toString();
    }

    /**
     * 提取用户代码中的import语句
     */
    private static String extractImports(String code) {
        StringBuilder imports = new StringBuilder();
        String[] lines = code.split("\n");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.startsWith("import ")) {
                imports.append(line).append("\n");
            }
        }
        // 添加必需的import（避免重复）
        if (!imports.toString().contains("com.carolcoral.mockserver.plugin.CustomResponseTransformer")) {
            imports.append("import com.carolcoral.mockserver.plugin.CustomResponseTransformer;\n");
        }
        if (!imports.toString().contains("com.carolcoral.mockserver.dto.MockResponseDTO")) {
            imports.append("import com.carolcoral.mockserver.dto.MockResponseDTO;\n");
        }
        if (!imports.toString().contains("com.carolcoral.mockserver.dto.MockRequest")) {
            imports.append("import com.carolcoral.mockserver.dto.MockRequest;\n");
        }
        if (!imports.toString().contains("java.util.")) {
            imports.append("import java.util.*;\n");
        }
        imports.append("\n");
        return imports.toString();
    }

    /**
     * 编译Java源码
     */
    private static Class<?> compile(String className, String sourceCode) throws CompilationException {
        // 每次编译创建新的ClassFileManager和DiagnosticCollector
        ClassFileManager classFileManager = new ClassFileManager(compiler.getStandardFileManager(null, null, null));
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaFileObject javaFile = new JavaSourceFromString(className, sourceCode);

        // 准备编译选项，包含classpath
        List<String> options = new ArrayList<>();
        options.add("-classpath");
        options.add(compilationClasspath);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null,                      // Writer for diagnostics
                classFileManager,           // FileManager
                diagnostics,               // Diagnostics
                options,                   // Options
                null,                      // Classes to annotate (annotations)
                Collections.singletonList(javaFile) // Source files
        );

        boolean success = task.call();
        if (!success) {
            StringBuilder errorMsg = new StringBuilder();
            boolean hasError = false;
            for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                if (diagnostic.getKind() == Diagnostic.Kind.ERROR) {
                    hasError = true;
                    long lineNum = diagnostic.getLineNumber();
                    String msg = diagnostic.getMessage(Locale.getDefault());
                    // 清理重复的路径信息
                    msg = msg.replaceAll(" location:.*", "");
                    errorMsg.append(String.format(" 行%d: %s\n", lineNum, msg));
                }
            }
            if (!hasError) {
                // 只有警告没有错误，仍然视为失败，给出友好提示
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    long lineNum = diagnostic.getLineNumber();
                    String msg = diagnostic.getMessage(Locale.getDefault());
                    msg = msg.replaceAll(" location:.*", "");
                    errorMsg.append(String.format(" 行%d: %s\n", lineNum, msg));
                }
            }
            if (errorMsg.length() > 0) {
                throw new CompilationException("编译失败:\n" + errorMsg.toString());
            }
        }

        // 加载编译后的类
        try {
            byte[] classBytes = classFileManager.getClassBytes(className);
            if (classBytes == null) {
                // 尝试多种可能的类名格式
                String[] possibleNames = {
                        className,
                        "com.carolcoral.mockserver.plugin.dynamic." + className
                };
                for (String name : possibleNames) {
                    classBytes = classFileManager.getClassBytes(name);
                    if (classBytes != null) {
                        className = name;
                        break;
                    }
                }
                if (classBytes == null) {
                    throw new CompilationException("未找到编译后的类文件，可能是编译失败但未报告错误");
                }
            }

            DynamicClassLoader classLoader = new DynamicClassLoader(
                    DynamicCompiler.class.getClassLoader());
            return classLoader.defineClass(
                    "com.carolcoral.mockserver.plugin.dynamic." + className, classBytes);
        } catch (CompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new CompilationException("加载编译后的类失败: " + e.getMessage());
        }
    }

    /**
     * 获取缓存的转换器实例
     */
    public static CustomResponseTransformer getCachedInstance(Long apiId) {
        return instanceCache.get(apiId);
    }

    /**
     * 清除指定接口的缓存
     */
    public static void evictCache(Long apiId) {
        sourceCodeCache.remove(apiId);
        instanceCache.remove(apiId);
        classCache.remove(apiId);
        log.info("清除动态转换器缓存: apiId={}", apiId);
    }

    /**
     * 检查是否有缓存的源码
     */
    public static boolean hasCachedSource(Long apiId) {
        return sourceCodeCache.containsKey(apiId);
    }

    /**
     * 获取缓存的源码
     */
    public static String getCachedSource(Long apiId) {
        return sourceCodeCache.get(apiId);
    }

    /**
     * 内存中的Java源文件
     */
    private static class JavaSourceFromString extends SimpleJavaFileObject {
        private final String code;

        JavaSourceFromString(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }

    /**
     * 自定义ClassLoader，用于加载动态编译的类
     */
    private static class DynamicClassLoader extends ClassLoader {
        public DynamicClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] bytes) {
            return super.defineClass(name, bytes, 0, bytes.length);
        }
    }

    /**
     * 自定义FileManager，用于获取编译后的字节码
     */
    private static class ClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
        private final Map<String, ByteArrayOutputStream> classBytes = new HashMap<>();

        ClassFileManager(StandardJavaFileManager fileManager) {
            super(fileManager);
        }

        @Override
        public JavaFileObject getJavaFileForOutput(Location location, String className,
                                                   JavaFileObject.Kind kind, FileObject sibling) throws IOException {
            if (kind == JavaFileObject.Kind.CLASS) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                classBytes.put(className, bos);
                return new SimpleJavaFileObject(
                        URI.create("bytes:///" + className.replace('.', '/') + ".class"),
                        JavaFileObject.Kind.CLASS) {
                    @Override
                    public OutputStream openOutputStream() {
                        return bos;
                    }
                };
            }
            return super.getJavaFileForOutput(location, className, kind, sibling);
        }

        byte[] getClassBytes(String className) {
            // 先精确匹配
            if (classBytes.containsKey(className)) {
                return classBytes.get(className).toByteArray();
            }
            // 再尝试带包名的匹配
            for (Map.Entry<String, ByteArrayOutputStream> entry : classBytes.entrySet()) {
                if (entry.getKey().equals(className) ||
                    entry.getKey().endsWith("." + className) ||
                    entry.getKey().equals(className.replace("-", "_"))) {
                    return entry.getValue().toByteArray();
                }
            }
            return null;
        }
    }

    /**
     * 编译异常
     */
    public static class CompilationException extends Exception {
        public CompilationException(String message) {
            super(message);
        }
    }

    /**
     * 检查编译器是否可用
     * <p>
     * 调用此方法会触发类的静态初始化
     * </p>
     */
    public static boolean isCompilerAvailable() {
        // 强制触发静态初始化
        forceStaticInitialization();
        return compiler != null;
    }

    /**
     * 获取编译时的classpath
     * <p>
     * 调用此方法会触发类的静态初始化
     * </p>
     */
    public static String getCompilationClasspath() {
        // 强制触发静态初始化
        forceStaticInitialization();
        return compilationClasspath;
    }

    /**
     * 强制触发类的静态初始化
     * <p>
     * 通过访问 compiler 字段来确保静态初始化块已经执行
     * </p>
     */
    private static void forceStaticInitialization() {
        // 访问 compiler 会触发类的静态初始化
        // compiler 是 final 的，所以在任何线程看到它之前，它已经被初始化
        // 但我们需要确保 compilationClasspath 也被设置
        if (compilationClasspath == null) {
            // 重新构建 classpath
            compilationClasspath = buildClasspath();
            log.info("动态编译classpath已重新构建: 长度={}", compilationClasspath.length());
        }
    }
}
