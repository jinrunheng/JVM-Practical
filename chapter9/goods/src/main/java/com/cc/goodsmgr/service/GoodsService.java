package com.cc.goodsmgr.service;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cc.common.service.BaseService;
import com.cc.goodsmgr.dao.GoodsDAO;
import com.cc.goodsmgr.vo.GoodsModel;
import com.cc.goodsmgr.vo.GoodsQueryModel;
import com.cc.pageutil.Page;

@Service
@Transactional
public class GoodsService extends BaseService<GoodsModel,GoodsQueryModel> implements IGoodsService{
	private GoodsDAO dao = null;
	@Autowired
	private void setDao(GoodsDAO dao){
		this.dao = dao;
		super.setDAO(dao);
	}
	@Override
	public Page<GoodsModel> getByConditionPage(GoodsQueryModel qm){
		List<GoodsModel> list = dao.getByConditionPage(qm);
		qm.getPage().setResult(list);
		
//		MyData.addList();
		
		return qm.getPage();
	}
}