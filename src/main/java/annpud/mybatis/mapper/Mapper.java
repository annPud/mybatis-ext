package annpud.mybatis.mapper;

import org.apache.ibatis.annotations.DeleteProvider;
import org.apache.ibatis.annotations.InsertProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.annotations.UpdateProvider;

import java.util.List;

/**
 * 提供基础增删改查的泛型调用方法
 *
 * @author zhuwl120820@gxwsxx.com 2015年2月1日下午4:26:55
 * @since 1.0
 */
public interface Mapper<T> {

    /**
     * select语句，根据id查询数据
     *
     * @param id 数据的主键
     * @return 查询数据的对象，没有则返回null
     * @since 1.0
     */
    @SelectProvider(method = "fakeSQL", type = MapperProvider.class)
    T select(String id);

    /**
     * selectAll语句，查询表所有数据
     *
     * @return 查询数据表的全部对象
     * @since 2.1
     */
    @SelectProvider(method = "fakeSQL", type = MapperProvider.class)
    List<T> selectAll();

    /**
     * 获取对应表的noid值，由insert或insert*方法自动调用，不需要显式调用
     *
     * @return noid的值
     * @since 1.0
     */
    @SelectProvider(method = "fakeSQL", type = MapperProvider.class)
    String noid();

    /**
     * insert语句，将对象“所有”的字段加入sql语句中，“包括”值为null的字段
     *
     * @param o 要insert的对象
     * @return 操作的记录数
     * @since 1.0
     */
    @InsertProvider(method = "fakeSQL", type = MapperProvider.class)
    int insert(T o);

    /**
     * insert语句，将对象中“有值”的字段动态加入sql语句中，“不包括”值为null的字段
     *
     * @param o 要insert的对象
     * @return 操作的记录数
     * @since 1.0
     */
    @InsertProvider(method = "fakeSQL", type = MapperProvider.class)
    int insertNotNull(T o);

    /**
     * update语句，将对象“所有”的字段加入sql语句中，“包括”值为null的字段
     *
     * @param o 要update的对象
     * @return 操作的记录数
     * @since 1.0
     */
    @UpdateProvider(method = "fakeSQL", type = MapperProvider.class)
    int update(T o);

    /**
     * update语句，将对象中“有值”的字段动态加入sql语句中，“不包括”值为null的字段
     *
     * @param o 要update的对象
     * @return 操作的记录数
     * @since 1.0
     */
    @UpdateProvider(method = "fakeSQL", type = MapperProvider.class)
    int updateNotNull(T o);

    /**
     * delete语句，根据id查询数据
     *
     * @param id 数据的主键
     * @return 查询数据的对象，没有则返回null
     * @since 1.0
     */
    @DeleteProvider(method = "fakeSQL", type = MapperProvider.class)
    int delete(String id);

    /**
     * 查询统计记录书
     *
     * @return 记录数
     * @since 1.1
     */
    @SelectProvider(method = "fakeSQL", type = MapperProvider.class)
    long count();

    /**
     * 锁定指定id的行,select for update
     *
     * @param id 指定id
     * @return 返回查询对象
     */
    @SelectProvider(method = "fakeSQL", type = MapperProvider.class)
    T lock(String id);
}
