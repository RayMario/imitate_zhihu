package com.imitatezhihu.dao;

import com.imitatezhihu.model.Feed;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FeedDao {
    String TABLE_NAME = "feed";
    String INSERT_FIELDS = " user_id, created_date, type, data";
    String SELECT_FIELDS = "id, "+INSERT_FIELDS;

    //插入Feed
    @Insert({"insert into", TABLE_NAME, "(", INSERT_FIELDS,
            ") values (#{userId}, #{createdDate}, #{type}, #{data})"})
    int addFeed(Feed feed);

    //通过Id选择Feed
    @Select({"select", SELECT_FIELDS, "from", TABLE_NAME , "where id = #{id}"})
    Feed selectById(int id);

    //读取一定数量的Feed，通过xml实现SQL
    List<Feed> selectUserFeeds(@Param("maxId") int maxId,
                               @Param("userIds") List<Integer> userIds,
                               @Param("count") int count);
}
