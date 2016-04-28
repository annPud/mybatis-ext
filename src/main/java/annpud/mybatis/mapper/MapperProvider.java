package annpud.mybatis.mapper;

import annpud.mybatis.entity.Entity;
import annpud.mybatis.entity.PkField;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.ibatis.jdbc.SqlBuilder.*;

/**
 * com.gxws.tool.mybatis.mapper.Mapper接口的实际实现类
 *
 * @since 1.0
 */
public class MapperProvider {

    private static final Logger log = LoggerFactory.getLogger(MapperProvider.class);

    private static Map<String, Method> providerMethodMap = new HashMap<>();

    private Map<String, Entity> enMap = new HashMap<>();

    private PkField pk = new PkField();

    /**
     * 初始化所有参数
     *
     * @since 1.0
     */
    public MapperProvider() {
        Class<? extends MapperProvider> clz = this.getClass();
        Method[] methods = clz.getMethods();
        for (Method method : methods) {
            providerMethodMap.put(method.getName(), method);
        }
    }

    /**
     * mybatis指定实现对象，没有实际意义
     *
     * @return 返回没有实际意义的sql语句
     * @since 1.0
     */
    public String fakeSQL() {
        return "fakeSQL";
    }

    /**
     * 根据java反射提供对应方法的具体实现
     *
     * @param ms 需要处理的sql语句中的mybatis plugin对象
     * @since 1.0
     */
    public void handle(MappedStatement ms) {
        Entity en = null;
        try {
            en = getEntity(ms.getId());
        } catch (ClassNotFoundException e1) {
            log.error(e1.getMessage(), e1);
            return;
        }
        if (!en.isSubMapper()) {
            return;
        }
        if (!providerMethodMap.containsKey(en.getMapperMethodName())) {
            log.debug("方法名：" + en.getMapperMethodName() + "不是通用方法");
            return;
        }
        Method method = providerMethodMap.get(en.getMapperMethodName());
        try {
            method.invoke(this, ms, en);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * 对mybatis的内置变量进行重新赋值
     *
     * @param obj   mybatis的MappedStatement对象
     * @param name  内置参数名，一般是"type"或"sqlSource"
     * @param value 内置参数值，一般是class对象或sqlSource对象
     * @since 1.0
     */
    private void resetValue(Object obj, String name, Object value) {
        MetaObject mo = MetaObject.forObject(obj, new DefaultObjectFactory(), new DefaultObjectWrapperFactory(),
                new DefaultReflectorFactory());
        mo.setValue(name, value);
    }

    /**
     * 获取所有实体类对象的参数
     *
     * @param methodFullName 方法全名，包名.类名.方法名
     * @return 参数对象
     * @throws ClassNotFoundException 没有找到类
     * @since 1.0
     */
    private Entity getEntity(String methodFullName) throws ClassNotFoundException {
        Entity en = enMap.get(methodFullName);
        if (null == en) {
            en = new Entity(methodFullName);
            enMap.put(methodFullName, en);
        }
        return en;
    }

    /**
     * Mapper接口select方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.0
     */
    public void select(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        SELECT(en.getDbColumnNameString());
        FROM(en.getDbTableName());
        WHERE(pk.getDbColumnName() + "=#{" + pk.getEntityField() + "}");
        nodes.add(new StaticTextSqlNode(SQL()));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
        resetValue(ms.getResultMaps().get(0), "type", en.getEntityClass());
    }

    public void lock(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        SELECT(en.getDbColumnNameString());
        FROM(en.getDbTableName());
        WHERE(pk.getDbColumnName() + "=#{" + pk.getEntityField() + "} for update");
        nodes.add(new StaticTextSqlNode(SQL()));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
        resetValue(ms.getResultMaps().get(0), "type", en.getEntityClass());
    }

    /**
     * Mapper接口selectAll方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 2.1
     */
    public void selectAll(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        SELECT(en.getDbColumnNameString());
        FROM(en.getDbTableName());
        nodes.add(new StaticTextSqlNode(SQL()));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
        resetValue(ms.getResultMaps().get(0), "type", en.getEntityClass());
    }

    /**
     * Mapper接口noid方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.0
     */
    public void noid(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        nodes.add(new StaticTextSqlNode(
                "select noid.get_noid(concat((select database()),'." + en.getDbTableName() + "'))"));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
    }

    /**
     * Mapper接口insert方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.0
     */
    public void insert(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        INSERT_INTO(en.getDbTableName());
        for (String fieldName : en.getEntityFieldSet()) {
            VALUES(Entity.underline(fieldName), "#{" + fieldName + "}");
        }
        nodes.add(new StaticTextSqlNode(SQL()));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
    }

    /**
     * Mapper接口insertNotNull方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.0
     */
    public void insertNotNull(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        INSERT_INTO(en.getDbTableName());
        nodes.add(new StaticTextSqlNode(SQL()));
        List<SqlNode> ifColumnNodes = new ArrayList<SqlNode>();
        List<SqlNode> ifValueNodes = new ArrayList<SqlNode>();
        for (String fieldName : en.getEntityFieldSet()) {
            ifColumnNodes
                    .add(new IfSqlNode(new StaticTextSqlNode(Entity.underline(fieldName) + ","), fieldName + "!=null"));
            ifValueNodes.add(new IfSqlNode(new StaticTextSqlNode("#{" + fieldName + "},"), fieldName + "!=null"));
        }
        nodes.add(new TrimSqlNode(ms.getConfiguration(), new MixedSqlNode(ifColumnNodes), "(", null, ")", ","));
        nodes.add(new TrimSqlNode(ms.getConfiguration(), new MixedSqlNode(ifValueNodes), "VALUES (", null, ")", ","));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
    }

    /**
     * Mapper接口delete方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.0
     */
    public void delete(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        DELETE_FROM(en.getDbTableName());
        WHERE(pk.getDbColumnName() + "=#{" + pk.getEntityField() + "}");
        nodes.add(new StaticTextSqlNode(SQL()));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
    }

    /**
     * Mapper接口update方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.0
     */
    public void update(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        UPDATE(en.getDbTableName());
        for (String fieldName : en.getEntityFieldSet()) {
            if (pk.getEntityField().equals(fieldName)) {
                continue;
            }
            SET(Entity.underline(fieldName) + "=#{" + fieldName + "}");
        }
        WHERE(pk.getDbColumnName() + "=#{" + pk.getEntityField() + "}");
        nodes.add(new StaticTextSqlNode(SQL()));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
    }

    /**
     * Mapper接口updateNotNull方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.0
     */
    public void updateNotNull(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        UPDATE(en.getDbTableName());
        nodes.add(new StaticTextSqlNode(SQL()));
        List<SqlNode> ifNodes = new ArrayList<SqlNode>();
        for (String fieldName : en.getEntityFieldSet()) {
            if (!pk.getEntityField().equals(fieldName)) {
                ifNodes.add(new IfSqlNode(
                        new StaticTextSqlNode(Entity.underline(fieldName) + "=#{" + fieldName + "}" + ","),
                        fieldName + "!=null"));
            }
        }
        nodes.add(new TrimSqlNode(ms.getConfiguration(), new MixedSqlNode(ifNodes), "SET ", null, "", ","));
        nodes.add(new StaticTextSqlNode("WHERE " + pk.getDbColumnName() + "=#{" + pk.getEntityField() + "}"));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
    }

    /**
     * Mapper接口count方法的具体实现
     *
     * @param ms MappedStatement 对象
     * @param en Entity 对象
     * @since 1.1
     */
    public void count(MappedStatement ms, Entity en) {
        List<SqlNode> nodes = new ArrayList<>();
        BEGIN();
        SELECT("count(" + pk.getDbColumnName() + ")");
        FROM(en.getDbTableName());
        nodes.add(new StaticTextSqlNode(SQL()));
        DynamicSqlSource sqlSource = new DynamicSqlSource(ms.getConfiguration(), new MixedSqlNode(nodes));
        resetValue(ms, "sqlSource", sqlSource);
        resetValue(ms.getResultMaps().get(0), "type", long.class);
    }
}
