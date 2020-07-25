package com.taotao.content.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.common.utils.JsonUtils;
import com.taotao.jedis.JedisClient;
import com.taotao.pojo.TbContentExample;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.TaotaoResult;
import com.taotao.content.service.ContentService;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.TbContent;

@Service
public class ContentServiceImpl implements ContentService {

	@Autowired
	private TbContentMapper contentMapper;
	@Autowired
	private JedisClient jedisClient;
	@Value("${INDEX_CONTENT}")
	private String INDEX_CONTENT;

	@Override
	public TaotaoResult addContent(TbContent content) {
		//补全pojo的属性
		content.setCreated( new Date());
		content.setUpdated(new Date());
		//插入到内容表
		contentMapper.insert(content);
		//同步缓存
		//删除对应的缓存信息
		jedisClient.hdel("INDEX_CONTENT",content.getCategoryId().toString());
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
	public TaotaoResult contentDelete(String[] ids) {
		List<TbContent> contentList=new ArrayList<>();
		//设置条件
		for (String id : ids) {
			//获取当前要删的数据
			TbContent content = contentMapper.selectByPrimaryKey(Long.parseLong(id));
			//删除当前id数据
			contentMapper.deleteByPrimaryKey(Long.parseLong(id));
			contentList.add(content);
		}
		//同步缓存
		//删除对应的缓存信息
		for (TbContent content:contentList) {
			jedisClient.hdel("INDEX_CONTENT", content.getCategoryId().toString());
		}
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

	@Override
	public List<TbContent> getContentByid(Long cid) {
		//先查询缓存
		//添加缓存不能影响正常业务逻辑
		try {
			//查询缓存
			String json = jedisClient.hget(INDEX_CONTENT, cid + "");
			//查询到结果，把json转换成List返回
			if (StringUtils.isNotBlank(json)) {
				List<TbContent> list = JsonUtils.jsonToList(json, TbContent.class);
				return list;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		//缓存中没有命中，需要查询数据库
		TbContentExample contentExample = new TbContentExample();
		TbContentExample.Criteria criteria = contentExample.createCriteria();
		//设置查询条件
		criteria.andCategoryIdEqualTo(cid);
		//执行查询
		List<TbContent> list =contentMapper.selectByExample(contentExample);
		//把结果添加到缓存
		try {
			jedisClient.hset(INDEX_CONTENT, cid + "", JsonUtils.objectToJson(list));
		} catch (Exception e) {
			e.printStackTrace();
		}
		//返回结果
		return list;
	}


}
