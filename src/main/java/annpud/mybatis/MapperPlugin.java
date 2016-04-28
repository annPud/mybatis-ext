package annpud.mybatis;

import annpud.mybatis.mapper.MapperProvider;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;

import java.util.Properties;

/**
 * mybatis plugin，实现泛型增删改查的方法<br>
 * 具体使用方式参考mybatis官方文档
 */
@Intercepts({
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class,
        RowBounds.class, ResultHandler.class}),
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class,
        Object.class})}) public class MapperPlugin implements Interceptor {

    private MapperProvider provider = new MapperProvider();

    @Override public Object intercept(Invocation invocation) throws Throwable {
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        provider.handle(ms);
        return invocation.proceed();
    }

    @Override public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override public void setProperties(Properties properties) {

    }

}
