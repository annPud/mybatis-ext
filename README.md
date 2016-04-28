# mybatis-ext
mybatis extended


mail list:朱伟亮 <annpud@qq.com>

mybatis工具提供通用增删改查方法。
主键id生成。

## 1. 版本变更说明



## 2. 功能

### 2.1. 提供通用的、泛型的增删改查方法

annpud.mybatis.mapper.Mapper<br>
继承自Mapper接口的接口，不需要再实现以下方法<br>

public T select(String id);

public int insert(T o);

public int insertNotNull(T o);

public int update(T o);

public int updateNotNull(T o);

public int delete(String id);

public Long count();

public List<T> selectAll();

具体方法的说明可以查看api或代码注释。

2、自动生成对象id(主键)值

由spring aop的方式，在执行insert*方法时，添加对象名为id的属性的值。
不使用配置而使用约定的方式进行操作。对所有泛型对象字段名为"id"的字段，在执行insert开头的方法时，会自动填入相应的值。
即如果字段名为"id"，则该字段对应为数据库主键，而且数值自动填入，不能指定。如果该字段不是对应数据库主键的话，则不能命名为"id"。


## 3. 使用方式

### 3.1. 泛型增删改查

在配置文件classpath:mybatis.xml中添加plugin。

<configuration>
    <plugins>
        <plugin interceptor="annpud.mybatis.plugin.MapperPlugin"></plugin>
    </plugins>
</configuration>
mapper接口继承annpud.mybatis.mapper.Mapper。

package annpud.demo.mapper;

import annpud.demo.tb.DemoTb;
import annpud.mybatis.mapper.Mapper;

public interface DemoTbMapper extends Mapper<DemoTb> {

}
接口DemoTbMapper的对象，已经可以使用Mapper提供的增删改查方法。

DemoTb dtb = demoMapper.select(dm.getId());
2、使用自动生成id和noid的值

在配置文件spring-mybatis.xml中添加spring aop配置。

<beans>
    <!-- mybatis mapper 层的aop配置 -->
    <aop:config>
        <aop:aspect id="id" ref="idAspect">
            <aop:pointcut id="insert"
                expression="execution(* annpud.demo.mapper.*.insert*(..))" />
            <aop:before method="before" pointcut-ref="insert" />
        </aop:aspect>
    </aop:config>
    <!-- 生成数据库主键id值的aop bean -->
    <bean id="idAspect" class="com.gxws.tool.mybatis.aspect.IdAspect" />
</beans>
在包annpud.demo.mapper以内所有类，方法名以insert开始的方法，在执行时会自动添加id的值。
