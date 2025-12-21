package org.lombok.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;

public class LombokPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> warnings) {
        return true;
    }

    /**
     * 在实体类生成时调用，用于修改类定义
     */
    @Override
    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        // 1. 添加 Lombok 注解的 import
        topLevelClass.addImportedType("lombok.Data");
        topLevelClass.addImportedType("lombok.NoArgsConstructor");
        topLevelClass.addImportedType("lombok.AllArgsConstructor");

        // 2. 为实体类添加 Lombok 注解
        topLevelClass.addAnnotation("@Data");
        topLevelClass.addAnnotation("@NoArgsConstructor");
        topLevelClass.addAnnotation("@AllArgsConstructor");

        // 3. （可选）让实体类实现Serializable接口并生成serialVersionUID
        topLevelClass.addImportedType(new FullyQualifiedJavaType("java.io.Serializable"));
        topLevelClass.addSuperInterface(new FullyQualifiedJavaType("Serializable"));

        Field field = new Field("serialVersionUID", new FullyQualifiedJavaType("long"));
        field.setFinal(true);
        field.setInitializationString("1L");
        field.setStatic(true);
        field.setVisibility(JavaVisibility.PRIVATE);
        topLevelClass.addField(field);

        return true;
    }

    /**
     * 为每个字段添加数据库注释到JavaDoc
     */
    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        // 从数据库列信息中获取备注（注释）
        String remarks = introspectedColumn.getRemarks();
        if (remarks != null && !remarks.trim().isEmpty()) {
            // 生成JavaDoc注释
            field.addJavaDocLine("/**");
            field.addJavaDocLine(" * " + remarks);
            field.addJavaDocLine(" */");
        }
        return true;
    }

    // 可选：抑制 getter 和 setter 方法的生成
    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return false;
    }
}
