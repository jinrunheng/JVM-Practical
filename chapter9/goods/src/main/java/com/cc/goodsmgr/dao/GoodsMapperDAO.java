package com.cc.goodsmgr.dao;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.cc.goodsmgr.vo.GoodsModel;
import com.cc.goodsmgr.vo.GoodsQueryModel;

@Repository
public interface GoodsMapperDAO extends GoodsDAO{
	public List<Integer> getIdsByConditionPage(GoodsQueryModel gqm);
	public List<GoodsModel> getByIds(String ids);
}
