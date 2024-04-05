/*
 *    Copyright 2006-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.mybatis.generator.internal;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author song.z
 */
public class MyCommentGenerator extends DefaultCommentGenerator {

    @Override
    public void addJavaFileComment(CompilationUnit compilationUnit) {
        if (compilationUnit instanceof Interface) {
            Interface interfaze = (Interface) compilationUnit;
            interfaze.addJavaDocLine("/**");

            interfaze.addJavaDocLine(" * @author " + System.getenv().getOrDefault("USER", "autoGenerator"));
            Date date = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            String strDate = sdf.format(date);
            interfaze.addJavaDocLine(" * @date " + strDate);

            interfaze.addJavaDocLine(" */");
        }
    }

    @Override
    public void addClassComment(InnerClass innerClass, IntrospectedTable introspectedTable) {
        innerClass.addJavaDocLine("/**");

        innerClass.addJavaDocLine(" * 表名:" + introspectedTable.getFullyQualifiedTable());
        innerClass.addJavaDocLine(" * ");
        innerClass.addJavaDocLine(" * @author " + System.getenv().getOrDefault("USER", "autoGenerator"));
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strDate = sdf.format(date);
        innerClass.addJavaDocLine(" * @date " + strDate);

        innerClass.addJavaDocLine(" */");
    }

    @Override
    public void addModelClassComment(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        topLevelClass.addJavaDocLine("/**");

        topLevelClass.addJavaDocLine(" * 表名:" + introspectedTable.getFullyQualifiedTable());
        topLevelClass.addJavaDocLine(" * ");
        topLevelClass.addJavaDocLine(" * @author " + System.getenv().getOrDefault("USER", "autoGenerator"));
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String strDate = sdf.format(date);
        topLevelClass.addJavaDocLine(" * @date " + strDate);

        topLevelClass.addJavaDocLine(" */");
    }

    @Override
    public void addFieldComment(Field field, IntrospectedTable introspectedTable,
                                IntrospectedColumn introspectedColumn) {
        field.addJavaDocLine("/**");
        String remarks = introspectedColumn.getRemarks();
        if (StringUtility.stringHasValue(remarks)) {
            field.addJavaDocLine(" * " + introspectedColumn.getActualColumnName() + ":" + remarks);
        }

        field.addJavaDocLine(" */");
    }

    @Override
    public void addGeneralMethodComment(Method method, IntrospectedTable introspectedTable) {
        StringBuilder sb = new StringBuilder();
        method.addJavaDocLine("/**");
        sb.append(" * ");
        if (method.isConstructor()) {
            sb.append("构造查询条件");
        }
        String method_name = method.getName();
        if ("deleteByPrimaryKey".equals(method_name)) {
            sb.append("根据主键删除数据库的记录");
        } else if ("queryByPrimaryKey".equals(method_name)) {
            sb.append("根据指定主键获取一条数据库记录");
        } else if ("insert".equals(method_name)) {
            sb.append("新写入数据库记录");
        } else if ("insertSelective".equals(method_name)) {
            sb.append("动态字段,写入数据库记录");
        } else if ("selectByPrimaryKey".equals(method_name)) {
            sb.append("根据指定主键获取一条数据库记录");
        } else if ("updateByPrimaryKeySelective".equals(method_name)) {
            sb.append("动态字段,根据主键来更新符合条件的数据库记录");
        } else if ("updateByPrimaryKey".equals(method_name)) {
            sb.append("根据主键来更新符合条件的数据库记录");
        } else if ("batchInsert".equals(method_name)) {
            sb.append("批量写入数据库记录");
        }
        method.addJavaDocLine(sb.toString());
        method.addJavaDocLine(" * ");
        final List<Parameter> parameterList = method.getParameters();
        String paramterName;
        for (Parameter parameter : parameterList) {
            sb.setLength(0);
            sb.append(" * @param ");
            paramterName = parameter.getName();
            sb.append(paramterName);
            if ("id".equals(paramterName)) {
                sb.append(" 主键ID");
            } else {
                sb.append(" 对应的数据记录");
            }
            method.addJavaDocLine(sb.toString());
        }
        if ("int".equals(method.getReturnType().map(FullyQualifiedJavaType::getShortName).orElse(""))) {
            method.addJavaDocLine(" * @return 受影响的记录条数");
        } else {
            method.addJavaDocLine(" * @return 查询结果");
        }
        method.addJavaDocLine(" */");
    }
}
