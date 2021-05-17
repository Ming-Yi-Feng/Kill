package com.MingYi.kill.model.mapper;

import com.MingYi.kill.model.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ItemMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Item record);

    int insertSelective(Item record);

    Item selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Item record);

    int updateByPrimaryKey(Item record);
}