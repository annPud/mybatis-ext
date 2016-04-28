package annpud.mybatis;

import annpud.mybatis.entity.PkField;
import org.aspectj.lang.JoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 设置主键的值
 */
public class IdAspect {

    private Logger log = LoggerFactory.getLogger(getClass());

    private PkField pk = new PkField();

    /**
     * 在insert方法执行之前插入id的值
     *
     * @param jp JoinPoint对象
     */
    public void before(JoinPoint jp) {
        Object entity = jp.getArgs()[0];
        String fieldName = pk.getEntityField();

        StringBuilder methodNameGetter = new StringBuilder("get");
        methodNameGetter.append(fieldName.substring(0, 1).toUpperCase());
        methodNameGetter.append(fieldName.substring(1));

        StringBuilder methodNameSetter = new StringBuilder("s");
        methodNameSetter.append(methodNameGetter.substring(1));
        Method method = null;
        try {
            try {
                method = entity.getClass().getMethod(methodNameGetter.toString());
            } catch (NoSuchMethodException e1) {
                log.debug("没有 " + methodNameGetter.toString() + " 方法 " + e1.getMessage(), e1);
                return;
            }
            Object o = method.invoke(entity);
            if (null != o) {
                return;
            }
            try {
                method = entity.getClass().getMethod(methodNameSetter.toString(), String.class);
            } catch (NoSuchMethodException e) {
                log.debug("没有 " + methodNameSetter.toString() + " 方法 " + e.getLocalizedMessage(),
                    e);
            }
            method.invoke(entity, Uuid.order());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e2) {
            log.error("操作主键值错误" + e2.getLocalizedMessage(), e2);
        }
    }
}
