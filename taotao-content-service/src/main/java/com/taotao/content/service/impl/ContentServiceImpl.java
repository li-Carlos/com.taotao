package com.taotao.content.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.pojo.TbContentExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.content.service.ContentService;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	
	@Override
	public TaotaoResult addContent(TbContent content) {
		//补全pojo的属性
		content.setCreated( new Date());
		content.setUpdated(new Date());
		//插入到内容表
		contentMapper.insert(content);
		return TaotaoResult.ok();
	}
	@Override
	public EasyUIDataGridResult getContentList(long categoryId, Integer page, Integer rows) {
		//设置分页信息
		PageHelper.startPage(page,rows);
		//执行查询
		TbContentExample contentExample = new TbContentExample();
		TbContentExample.Criteria criteria = contentExample.createCriteria();
		if(categoryId!=0){
			criteria.andCategoryIdEqualTo(categoryId);
		}
		List<TbContent> list =contentMapper.selectByExample(contentExample);
		//取查询结果
		PageInfo<TbContent> pageInfo = new PageInfo<>(list);
		EasyUIDataGridResult result = new EasyUIDataGridResult();
		result.setTotal(pageInfo.getTotal());
		result.setRows(list);
		return result;
	}

	@Override
	public TaotaoResult contentSave(TbContent tbContent) {
		//补全对象的属性
		tbContent.setCreated(new Date());
		tbContent.setUpdated(new Date());
		//插入到数据库
		contentMapper.insert(tbContent);
		return TaotaoResult.ok(tbContent);
	}

	@Override
	public TaotaoResult contentDelete(String[] ids) {
		//设置条件
		for (String id : ids) {
			contentMapper.deleteByPrimaryKey(Long.parseLong(id));
		}
		//删除当前id数据
		return TaotaoResult.ok();
	}

	@Override
	public TaotaoResult restContentEdit(TbContent tbContent) {
		//补全对象的属性
		tbContent.setCreated(new Date());
		tbContent.setUpdated(new Date());
		//插入到数据库
		contentMapper.updateByPrimaryKeySelective(tbContent);
		return TaotaoResult.ok(tbContent);
	}


}
