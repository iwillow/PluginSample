package com.iwillow.plugin

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.LibraryPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.iwillow.asm.TraceClassVisitor
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

public class TracePluginImpl extends Transform implements Plugin<Project> {
    static final String PLUGIN_NAME = "TracePlugin"
    private Project project


    @Override
    void apply(Project project) {
        this.project = project
        project.extensions.create("trace", TraceExtention)
        def hasApp = project.plugins.withType(AppPlugin)
        def hasLib = project.plugins.withType(LibraryPlugin)
        if (!hasApp && !hasLib) {
            throw new IllegalStateException("'android' or 'android-library' plugin required.")
        }
        final def variants
        if (hasApp) {
            variants = project.android.applicationVariants
        } else {
            variants = project.android.libraryVariants
        }
        def enable = true
        variants.all { variant ->
            if (!variant.buildType.isDebuggable()) {
                enable = false
                println("Skipping non-debuggable build type '${variant.buildType.name}'.")
            } else if (!project.trace.enabled) {
                enable = false
                println("Trace is not disabled.")
            }
        }
        if (enable) {
            def android = project.extensions.getByType(AppExtension)
            android.registerTransform(this)
            println("registerTransform")
        }

        project.task('tracePluginTask') {
            println "Hello trace plugin task"
        }
    }

    @Override
    void transform(Context context, Collection<TransformInput> inputs, Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider, boolean isIncremental) throws IOException, TransformException, InterruptedException {
        long startTime = System.currentTimeMillis()
        project.logger.error "========== $PLUGIN_NAME transform start ==========="
        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                if (directoryInput.file.isDirectory()) {
                    println "==== directoryInput.file = $directoryInput.file"
                    directoryInput.file.eachFileRecurse { File file ->
                        def name = file.name
                        println "==== directoryInput file name ==== ${file.getAbsolutePath()}"
                        if (name.endsWith(".class")
                                && !name.endsWith("R.class")
                                && !name.endsWith("BuildConfig.class")
                                && !name.contains("R\$")) {
                            ClassReader classReader = new ClassReader(file.bytes)
                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                            TraceClassVisitor classVisitor = new TraceClassVisitor(Opcodes.ASM5, classWriter)
                            classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
                            byte[] bytes = classWriter.toByteArray()
                            File destFile = new File(file.parentFile.absoluteFile, name)
                            project.logger.error "==== 重新写入的位置->lastFilePath === ${destFile.getAbsolutePath()}"
                            FileOutputStream fileOutputStream = new FileOutputStream(destFile)
                            fileOutputStream.write(bytes)
                            fileOutputStream.close()
                        }
                    }
                }
                //处理完输入文件之后，要把输出给下一个任务
                def dest = outputProvider.getContentLocation(directoryInput.name, directoryInput.contentTypes, directoryInput.scopes, Format.DIRECTORY)
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            input.jarInputs.each { JarInput jarInput ->
                println "------=== jarInput.file === ${jarInput.file.getAbsolutePath()}"
                File tempFile = null
                if (jarInput.file.getAbsolutePath().endsWith(".jar")) {
                    // 将jar包解压后重新打包的路径
                    tempFile = new File(jarInput.file.getParent() + File.separator + "trace.jar")
                    if (tempFile.exists()) {
                        tempFile.delete()
                    }
                    FileOutputStream fos = new FileOutputStream(tempFile)
                    JarOutputStream jarOutputStream = new JarOutputStream(fos)
                    JarFile jarFile = new JarFile(jarInput.file)
                    Enumeration<JarEntry> enumeration = jarFile.entries()
                    while (enumeration.hasMoreElements()) {
                        JarEntry jarEntry = (JarEntry) enumeration.nextElement()
                        String entryName = jarEntry.getName()
                        ZipEntry zipEntry = new ZipEntry(entryName)
                        println "==== jarInput class entryName :$entryName"
                        if (entryName.endsWith(".class")) {
                            jarOutputStream.putNextEntry(zipEntry)
                            InputStream inputStream = jarFile.getInputStream(jarEntry)
                            ClassReader classReader = new ClassReader(IOUtils.toByteArray(inputStream))
                            ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                            TraceClassVisitor cv = new TraceClassVisitor(Opcodes.ASM5, classWriter)
                            classReader.accept(cv, ClassReader.EXPAND_FRAMES)

                            byte[] bytes = classWriter.toByteArray()
                            jarOutputStream.write(bytes)
                            inputStream.close()
                        }
                    }

                    //结束
                    jarOutputStream.closeEntry()
                    jarOutputStream.close()
                    fos.close()
                    jarFile.close()
                }
                /**
                 * 重名输出文件,因为可能同名,会覆盖
                 */
                def jarName = jarInput.name
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //处理jar进行字节码注入处理
                def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
                if (tempFile != null) {
                    FileUtils.copyFile(tempFile, dest)
                    tempFile.delete()
                } else {
                    FileUtils.copyFile(jarInput.file, dest)
                }
            }

        }
        project.logger.error "========== $PLUGIN_NAME transform const time (${System.currentTimeMillis() - startTime} ms) ==========="
        project.logger.error "========== $PLUGIN_NAME transform end ==========="
    }

    @Override
    String getName() {
        return PLUGIN_NAME
    }

    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }
}