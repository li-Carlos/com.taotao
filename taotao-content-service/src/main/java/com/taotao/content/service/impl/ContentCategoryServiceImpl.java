package com.taotao.content.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.taotao.common.pojo.EasyUIDataGridResult;
import com.taotao.mapper.TbContentMapper;
import com.taotao.pojo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.taotao.common.pojo.EasyUITreeNode;
import com.taotao.common.pojo.TaotaoResult;
import com.taotao.content.service.ContentCategoryService;
import com.taotao.mapper.TbContentCategoryMapper;
import com.taotao.pojo.TbContentCategoryExample.Criteria;

/**
 * 内容分类管理service
 * <p>Title: ContentCategoryServiceImpl</p>
 * <p>Description: </p>
 * <p>Company: www.itcast.cn</p>
 *
 * @version 1.0
 */
@Service
public class ContentCategoryServiceImpl implements ContentCategoryService {

    @Autowired
    private TbContentCategoryMapper contentCategoryMapper;
    @Autowired
    private TbContentMapper contentMapper;

    @Override
    public List<EasyUITreeNode> getContentCategoryList(long parentId) {
        //根据parentId查询子节点列表
        TbContentCategoryExample example = new TbContentCategoryExample();
        //设置查询条件
        Criteria criteria = example.createCriteria();
        criteria.andParentIdEqualTo(parentId);
        //执行查询
        List<TbContentCategory> list = contentCategoryMapper.selectByExample(example);
        List<EasyUITreeNode> resultList = new ArrayList<>();
        for (TbContentCategory tbContentCategory : list) {
            EasyUITreeNode node = new EasyUITreeNode();
            node.setId(tbContentCategory.getId());
            node.setText(tbContentCategory.getName());
            node.setState(tbContentCategory.getIsParent() ? "closed" : "open");
            //添加到结果列表
            resultList.add(node);
        }
        return resultList;
    }

    @Override
    public TaotaoResult addContentCategory(Long parentId, String name) {
        //创建一个pojo对象
        TbContentCategory contentCategory = new TbContentCategory();
        //补全对象的属性
        contentCategory.setParentId(parentId);
        contentCategory.setName(name);
        //状态。可选值:1(正常),2(删除)
        contentCategory.setStatus(1);
        //排序，默认为1
        contentCategory.setSortOrder(1);
        contentCategory.setIsParent(false);
        contentCategory.setCreated(new Date());
        contentCategory.setUpdated(new Date());
        //插入到数据库
        contentCategoryMapper.insert(contentCategory);
        //判断父节点的状态
        TbContentCategory parent = contentCategoryMapper.selectByPrimaryKey(parentId);
        if (!parent.getIsParent()) {
            //如果父节点为叶子节点应该改为父节点
            parent.setIsParent(true);
            //更新父节点
            contentCategoryMapper.updateByPrimaryKey(parent);
        }

        //返回结果
        return TaotaoResult.ok(contentCategory);
    }

    public void deleteFather(Long id) {

    }

    @Override
    public TaotaoResult deleteContentCategory(Long id) {
        //获得当前节点数据
        TbContentCategory currentContentCategory = contentCategoryMapper.selectByPrimaryKey(id);
        //创建一个查询对象
        TbContentCategoryExample tbContentCategoryExample;
        //            创建一个pojo对象
        TbContentCategory contentCategory = new TbContentCategory();
        if (currentContentCategory.getIsParent()) {
            //设置查询条件
            tbContentCategoryExample = new TbContentCategoryExample();
            Criteria criteria1 = tbContentCategoryExample.createCriteria();
            criteria1.andParentIdEqualTo(id);
//            如果是父节点
            List<TbContentCategory> contentCategoryList = contentCategoryMapper.selectByExample(tbContentCategoryExample);
            for(TbContentCategory ct:contentCategoryList){
               contentCategoryMapper.deleteByPrimaryKey(ct.getId());
            }
            //删除id对应的节点
            contentCategoryMapper.deleteByPrimaryKey(id);
        } else {
            //不是父节点
            //删除id对应的节点
            contentCategoryMapper.deleteByPrimaryKey(id);
            //设置查询条件
            tbContentCategoryExample = new TbContentCategoryExample();
            Criteria criteria2 = tbContentCategoryExample.createCriteria();
            criteria2.andParentIdEqualTo(currentContentCategory.getParentId());
            //执行查询
            List<TbContentCategory> parentContentCategory2 = contentCategoryMapper.selectByExample(tbContentCategoryExample);
            //判断父节点下是否还有子节点，如果没有，则Isparent(false)
            if (parentContentCategory2.isEmpty()) {
                contentCategory.setId(currentContentCategory.getParentId());
                contentCategory.setIsParent(false);
                //更新时间
                contentCategory.setUpdated(new Date());
                contentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
            }
        }
        return TaotaoResult.ok(contentCategory);
    }

    @Override
    public TaotaoResult updateContentCategory(Long id, String name) {
        //创建一个pojo对象
        TbContentCategory contentCategory = new TbContentCategory();
        //补全对象的属性
        contentCategory.setId(id);
        contentCategory.setName(name);
        //状态。可选值:1(正常),2(删除)
        contentCategory.setStatus(1);
        //排序，默认为1
        contentCategory.setSortOrder(1);
        contentCategory.setIsParent(false);
        contentCategory.setCreated(new Date());
        contentCategory.setUpdated(new Date());
        //插入到数据库
        contentCategoryMapper.updateByPrimaryKeySelective(contentCategory);
        return TaotaoResult.ok(contentCategory);
    }


}
