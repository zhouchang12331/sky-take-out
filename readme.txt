day03
1 公共字段填充【OVER】

2 文件上传
  接口定义：
        请求路径： /admin/common/upload
        请求方式；POST
        请求参数：multipart/form-data  <参数名：file
        相应数据：文件上传路径

  思路分析：
        controller：
        service：
        mapper：


3 新增菜品
  接口定义：
          请求路径： /admin/dish
          请求方式；POST
          请求参数：application/json
          相应数据：
    思路分析：
          controller：
          service：
          mapper：

  数据模型：dish,dish_flavors,categories

4 菜品分页查询
 接口定义：
          请求路径： /admin/dish/page
          请求方式；Get
          请求参数：application/json
          相应数据：

5 删除菜品
 接口定义：
          请求路： /admin/dish
          请求方式:DELETE
          请求参数：ids
          相应数据：
 数据模型：dish,dish_flour,setmeal_dish