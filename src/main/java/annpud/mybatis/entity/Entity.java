package annpud.mybatis.entity;

import annpud.mybatis.mapper.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * 需要处理的泛型实体类信息
 */
public class Entity {

    private static final Logger log = LoggerFactory.getLogger(Entity.class);

    // 实体类对象类名（包名+类名）
    private String entityClassName;

    // 实体类对象类
    private Class<?> entityClass;

    // 实体类对象字段集合（字段内"_"分隔，包含所有声明字段，不包含父类）
    private Set<String> entityFieldSet;

    // 实体类对象字段（字段内"_"分隔，字段间","分隔，包含所有声明字段，不包含父类）
    private String entityFieldString;

    // 数据库表名（下划线）
    private String dbTableName;

    // 数据库列名（字段内"_"分隔，字段间","分隔，包含所有声明字段，不包含父类）
    private String dbColumnNameString;

    // 数据库列名（字段内"_"分隔，包含所有声明字段，不包含父类）
    private Set<String> dbColumnNameSet;

    // mapper对象方法名（select，insert，update，delete）
    private String mapperMethodName;

    // mapper对象类名（包名+类名）
    private String mapperClassName;

    // mapper对象类
    private Class<?> mapperClass;

    // 是否调用通用方法
    private boolean subMapper;

    private final static Class<Mapper> MAPPER_CLASS = Mapper.class;

    /**
     * 根据方法全面创建Entity对象
     *
     * @param methodFullName 方法全名，包名.类名.方法名
     * @throws ClassNotFoundException 没有找到类
     * @since 1.0
     */
    public Entity(String methodFullName) throws ClassNotFoundException {
        this.setMapperMethodName(methodFullName);
        this.setMapperClassName(methodFullName);
        this.setMapperClass();
        ParameterizedType pt = superMapper(this.mapperClass.getGenericInterfaces());
        if (null != pt) {
            this.setSubMapper(true);
            this.setEntityClass(pt);
            this.setEntityClassName();
            this.setDbTableName();
            this.setFieldAndColumn();
        }
    }

    /**
     * 获取接口的父类，是否属于Mapper子类
     *
     * @param types 当前接口的所有Type
     * @return 接口父类
     * @since 1.0
     */
    private ParameterizedType superMapper(Type[] types) {
        Class<?> superClass = null;
        ParameterizedType pt = null;
        for (Type type : types) {
            if (type instanceof ParameterizedType) {
                pt = (ParameterizedType) type;
                superClass = (Class<?>) pt.getRawType();
                if (MAPPER_CLASS.equals(superClass)) {
                    log.debug(this.getMapperClassName() + "是" + MAPPER_CLASS.getName() + "的子类");
                    return pt;
                }
            }
            superClass = (Class<?>) type;
            pt = superMapper(superClass.getGenericInterfaces());
            if (null != pt) {
                return pt;
            }
        }
        log.debug(this.getMapperClassName() + "不是" + MAPPER_CLASS.getName() + "的子类");
        return null;
    }

    public boolean isSubMapper() {
        return this.subMapper;
    }

    public String getDbTableName() {
        return dbTableName;
    }

    public Class<?> getEntityClass() {
        return entityClass;
    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public String getMapperMethodName() {
        return mapperMethodName;
    }

    public String getMapperClassName() {
        return mapperClassName;
    }

    public Class<?> getMapperClass() {
        return mapperClass;
    }

    private void setDbTableName() {
        this.dbTableName = underline(this.getEntityClass().getSimpleName());
    }

    private void setEntityClass(ParameterizedType pt) {
        this.entityClass = (Class<?>) pt.getActualTypeArguments()[0];
    }

    private void setEntityClassName() {
        this.entityClassName = this.entityClass.getName();
    }

    private void setMapperMethodName(String mapperMethodName) {
        this.mapperMethodName = mapperMethodName.substring(mapperMethodName.lastIndexOf(".") + 1);
    }

    private void setMapperClassName(String mapperClassName) {
        this.mapperClassName = mapperClassName.substring(0, mapperClassName.lastIndexOf("."));
    }

    private void setMapperClass() throws ClassNotFoundException {
        this.mapperClass =
            Class.forName(this.mapperClassName, false, this.getClass().getClassLoader());
    }

    private void setSubMapper(boolean subMapper) {
        this.subMapper = subMapper;
    }

    public Set<String> getDbColumnNameSet() {
        return dbColumnNameSet;
    }

    private void setFieldAndColumn() {
        Field[] fields = this.entityClass.getDeclaredFields();
        this.entityFieldSet = new HashSet<>();
        this.dbColumnNameSet = new HashSet<>();
        StringBuilder sb = new StringBuilder();
        for (Field field : fields) {
            this.entityFieldSet.add(field.getName());
            this.dbColumnNameSet.add(underline(field.getName()));
            sb.append(",");
            sb.append(field.getName());
        }
        this.entityFieldString = sb.substring(1);
        this.dbColumnNameString = underline(sb.substring(1));
    }

    public String getEntityFieldString() {
        return entityFieldString;
    }

    /**
     * 给大小写区分的字母添加下划线区分，并将大写字母转换成小写
     *
     * @param uplow 大小写区分的字符串
     * @return 转换后的字符串
     * @since 1.0
     */
    public static String underline(String uplow) {
        char[] uplowChar = uplow.toCharArray();
        StringBuilder sb = new StringBuilder();
        for (char anUplowChar : uplowChar) {
            if (Character.isUpperCase(anUplowChar)) {
                sb.append("_");
                sb.append(Character.toLowerCase(anUplowChar));
            } else {
                sb.append(anUplowChar);
            }
        }
        if ("_".equals(sb.substring(0, 1))) {
            return sb.substring(1);
        } else {
            return sb.toString();
        }

    }

    public String getDbColumnNameString() {
        return dbColumnNameString;
    }

    public Set<String> getEntityFieldSet() {
        return entityFieldSet;
    }
}
