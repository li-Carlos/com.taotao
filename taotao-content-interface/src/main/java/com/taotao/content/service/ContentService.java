package com.taotao.content.service;

import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.pojo.TbContent;


public interface ContentService {

	TaotaoResult addContent(TbContent content);
	EasyUIDataGridResult getContentList(long categoryId, Integer page, Integer rows);
	TaotaoResult contentSave(TbContent tbContent);
	TaotaoResult contentDelete(String[] id);
	TaotaoResult restContentEdit(TbContent tbContent);
}
