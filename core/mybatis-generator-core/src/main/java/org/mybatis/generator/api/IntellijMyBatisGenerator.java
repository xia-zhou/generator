package org.mybatis.generator.api;

import org.mybatis.generator.api.intellij.IntellijTableInfo;
import org.mybatis.generator.codegen.RootClassInfo;
import org.mybatis.generator.config.Configuration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.config.MergeConstants;
import org.mybatis.generator.exception.InvalidConfigurationException;
import org.mybatis.generator.exception.ShellException;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.NullProgressCallback;
import org.mybatis.generator.internal.ObjectFactory;
import org.mybatis.generator.internal.XmlFileMergerJaxp;
import org.mybatis.generator.internal.util.ClassloaderUtility;
import org.mybatis.generator.internal.util.messages.Messages;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class IntellijMyBatisGenerator {
    private Configuration configuration;

    private ShellCallback shellCallback;

    private List<GeneratedJavaFile> generatedJavaFiles = new ArrayList();

    private List<GeneratedXmlFile> generatedXmlFiles = new ArrayList();

    private List<GeneratedKotlinFile> generatedKotlinFiles = new ArrayList();

    private List<String> warnings;

    private Set<String> projects = new HashSet();

    public IntellijMyBatisGenerator(Configuration configuration, ShellCallback shellCallback, List<String> warnings)
            throws InvalidConfigurationException {
        if (configuration == null) {
            throw new IllegalArgumentException(Messages.getString("RuntimeError.2"));
        } else {
            this.configuration = configuration;
            if (shellCallback == null) {
                this.shellCallback = new DefaultShellCallback(false);
            } else {
                this.shellCallback = shellCallback;
            }

            if (warnings == null) {
                this.warnings = new ArrayList();
            } else {
                this.warnings = warnings;
            }

            this.configuration.validate();
        }
    }

    public void generate(ProgressCallback callback, IntellijTableInfo tableInfo)
            throws SQLException, IOException, InterruptedException {
        generate(callback, (Set) null, (Set) null, true, tableInfo);
    }

    public void generate(ProgressCallback callback, Set<String> contextIds, IntellijTableInfo tableInfo)
            throws SQLException, IOException, InterruptedException {
        generate(callback, contextIds, (Set) null, true, tableInfo);
    }

    public void generate(ProgressCallback callback, Set<String> contextIds, Set<String> fullyQualifiedTableNames,
                         IntellijTableInfo tableInfo) throws SQLException, IOException, InterruptedException {
        generate(callback, contextIds, fullyQualifiedTableNames, true, tableInfo);
    }

    public void generate(ProgressCallback callback, Set<String> contextIds, Set<String> fullyQualifiedTableNames,
                         boolean writeFiles, IntellijTableInfo tableInfo)
            throws SQLException, IOException, InterruptedException {
        if (callback == null) {
            callback = new NullProgressCallback();
        }

        generatedJavaFiles.clear();
        generatedXmlFiles.clear();
        ObjectFactory.reset();
        RootClassInfo.reset();
        Object contextsToRun;
        if (contextIds != null && !contextIds.isEmpty()) {
            contextsToRun = new ArrayList();
            Iterator var7 = configuration.getContexts().iterator();

            while (var7.hasNext()) {
                Context context = (Context) var7.next();
                if (contextIds.contains(context.getId())) {
                    ((List) contextsToRun).add(context);
                }
            }
        } else {
            contextsToRun = configuration.getContexts();
        }

        if (!configuration.getClassPathEntries().isEmpty()) {
            ClassLoader classLoader = ClassloaderUtility.getCustomClassloader(configuration.getClassPathEntries());
            ObjectFactory.addExternalClassLoader(classLoader);
        }

        int totalSteps = 0;

        Context context;
        Iterator var12;
        for (var12 = ((List) contextsToRun).iterator(); var12.hasNext();
                totalSteps += context.getIntrospectionSteps()) {
            context = (Context) var12.next();
        }

        ((ProgressCallback) callback).introspectionStarted(totalSteps);
        var12 = ((List) contextsToRun).iterator();

        while (var12.hasNext()) {
            context = (Context) var12.next();
            context.introspectIntellijTables((ProgressCallback) callback, warnings, fullyQualifiedTableNames,
                                             tableInfo);
        }

        totalSteps = 0;

        for (var12 = ((List) contextsToRun).iterator(); var12.hasNext(); totalSteps += context.getGenerationSteps()) {
            context = (Context) var12.next();
        }

        ((ProgressCallback) callback).generationStarted(totalSteps);
        var12 = ((List) contextsToRun).iterator();

        while (var12.hasNext()) {
            context = (Context) var12.next();
            context.generateFiles((ProgressCallback) callback, generatedJavaFiles, generatedXmlFiles,
                                  generatedKotlinFiles, warnings);
        }

        if (writeFiles) {
            ((ProgressCallback) callback).saveStarted(generatedXmlFiles.size() + generatedJavaFiles.size());
            var12 = generatedXmlFiles.iterator();

            while (var12.hasNext()) {
                GeneratedXmlFile gxf = (GeneratedXmlFile) var12.next();
                projects.add(gxf.getTargetProject());
                writeGeneratedXmlFile(gxf, (ProgressCallback) callback);
            }

            var12 = generatedJavaFiles.iterator();

            while (var12.hasNext()) {
                GeneratedJavaFile gjf = (GeneratedJavaFile) var12.next();
                projects.add(gjf.getTargetProject());
                writeGeneratedJavaFile(gjf, (ProgressCallback) callback);
            }

            var12 = generatedKotlinFiles.iterator();

            while (var12.hasNext()) {
                GeneratedKotlinFile gkf = (GeneratedKotlinFile) var12.next();
                projects.add(gkf.getTargetProject());
                writeGeneratedKotlinFile(gkf, (ProgressCallback) callback);
            }

            var12 = projects.iterator();

            while (var12.hasNext()) {
                String project = (String) var12.next();
                shellCallback.refreshProject(project);
            }
        }

        ((ProgressCallback) callback).done();
    }

    private void writeGeneratedJavaFile(GeneratedJavaFile gjf, ProgressCallback callback)
            throws InterruptedException, IOException {
        try {
            File directory = shellCallback.getDirectory(gjf.getTargetProject(), gjf.getTargetPackage());
            File targetFile = new File(directory, gjf.getFileName());
            String source;
            if (targetFile.exists()) {
                if (shellCallback.isMergeSupported()) {
                    source = shellCallback.mergeJavaFile(gjf.getFormattedContent(), targetFile,
                                                              MergeConstants.getOldElementTags(),
                                                              gjf.getFileEncoding());
                } else if (shellCallback.isOverwriteEnabled()) {
                    source = gjf.getFormattedContent();
                    warnings.add(Messages.getString("Warning.11", targetFile.getAbsolutePath()));
                } else {
                    source = gjf.getFormattedContent();
                    targetFile = getUniqueFileName(directory, gjf.getFileName());
                    warnings.add(Messages.getString("Warning.2", targetFile.getAbsolutePath()));
                }
            } else {
                source = gjf.getFormattedContent();
            }

            callback.checkCancel();
            callback.startTask(Messages.getString("Progress.15", targetFile.getName()));
            writeFile(targetFile, source, gjf.getFileEncoding());
        } catch (ShellException var6) {
            warnings.add(var6.getMessage());
        }

    }

    private void writeGeneratedKotlinFile(GeneratedKotlinFile gkf, ProgressCallback callback)
            throws InterruptedException, IOException {
        try {
            File directory = shellCallback.getDirectory(gkf.getTargetProject(), gkf.getTargetPackage());
            File targetFile = new File(directory, gkf.getFileName());
            String source;
            if (targetFile.exists()) {
                if (shellCallback.isOverwriteEnabled()) {
                    source = gkf.getFormattedContent();
                    warnings.add(Messages.getString("Warning.11", targetFile.getAbsolutePath()));
                } else {
                    source = gkf.getFormattedContent();
                    targetFile = getUniqueFileName(directory, gkf.getFileName());
                    warnings.add(Messages.getString("Warning.2", targetFile.getAbsolutePath()));
                }
            } else {
                source = gkf.getFormattedContent();
            }

            callback.checkCancel();
            callback.startTask(Messages.getString("Progress.15", targetFile.getName()));
            writeFile(targetFile, source, gkf.getFileEncoding());
        } catch (ShellException var6) {
            warnings.add(var6.getMessage());
        }

    }

    private void writeGeneratedXmlFile(GeneratedXmlFile gxf, ProgressCallback callback)
            throws InterruptedException, IOException {
        try {
            File directory = shellCallback.getDirectory(gxf.getTargetProject(), gxf.getTargetPackage());
            File targetFile = new File(directory, gxf.getFileName());
            String source;
            if (targetFile.exists()) {
                if (gxf.isMergeable()) {
                    source = XmlFileMergerJaxp.getMergedSource(gxf, targetFile);
                } else if (shellCallback.isOverwriteEnabled()) {
                    source = gxf.getFormattedContent();
                    warnings.add(Messages.getString("Warning.11", targetFile.getAbsolutePath()));
                } else {
                    source = gxf.getFormattedContent();
                    targetFile = getUniqueFileName(directory, gxf.getFileName());
                    warnings.add(Messages.getString("Warning.2", targetFile.getAbsolutePath()));
                }
            } else {
                source = gxf.getFormattedContent();
            }

            callback.checkCancel();
            callback.startTask(Messages.getString("Progress.15", targetFile.getName()));
            writeFile(targetFile, source, "UTF-8");
        } catch (ShellException var6) {
            warnings.add(var6.getMessage());
        }

    }

    private void writeFile(File file, String content, String fileEncoding) throws IOException {
        FileOutputStream fos = new FileOutputStream(file, false);
        OutputStreamWriter osw;
        if (fileEncoding == null) {
            osw = new OutputStreamWriter(fos);
        } else {
            osw = new OutputStreamWriter(fos, fileEncoding);
        }

        BufferedWriter bw = new BufferedWriter(osw);
        Throwable var7 = null;

        try {
            bw.write(content);
        } catch (Throwable var16) {
            var7 = var16;
            throw var16;
        } finally {
            if (bw != null) {
                if (var7 != null) {
                    try {
                        bw.close();
                    } catch (Throwable var15) {
                        var7.addSuppressed(var15);
                    }
                } else {
                    bw.close();
                }
            }

        }

    }

    private File getUniqueFileName(File directory, String fileName) {
        File answer = null;
        StringBuilder sb = new StringBuilder();

        for (int i = 1; i < 1000; ++i) {
            sb.setLength(0);
            sb.append(fileName);
            sb.append('.');
            sb.append(i);
            File testFile = new File(directory, sb.toString());
            if (!testFile.exists()) {
                answer = testFile;
                break;
            }
        }

        if (answer == null) {
            throw new RuntimeException(Messages.getString("RuntimeError.3", directory.getAbsolutePath()));
        } else {
            return answer;
        }
    }

    public List<GeneratedJavaFile> getGeneratedJavaFiles() {
        return generatedJavaFiles;
    }

    public List<GeneratedKotlinFile> getGeneratedKotlinFiles() {
        return generatedKotlinFiles;
    }

    public List<GeneratedXmlFile> getGeneratedXmlFiles() {
        return generatedXmlFiles;
    }
}