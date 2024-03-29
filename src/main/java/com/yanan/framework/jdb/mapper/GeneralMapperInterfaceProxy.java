package com.yanan.framework.jdb.mapper;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.yanan.framework.jdb.SqlSession;
import com.yanan.framework.jdb.annotation.Sql;
import com.yanan.framework.jdb.entity.BaseMapping;
import com.yanan.framework.jdb.exception.SqlExecuteException;
import com.yanan.framework.jdb.mapper.annotations.Param;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.framework.plugin.annotations.Support;
import com.yanan.framework.plugin.handler.InvokeHandler;
import com.yanan.framework.plugin.handler.MethodHandler;
import com.yanan.utils.reflect.ReflectUtils;
import com.yanan.utils.reflect.cache.ClassHelper;
import com.yanan.utils.reflect.cache.MethodHelper;
import com.yanan.utils.reflect.cache.ParameterHelper;
import com.yanan.framework.plugin.ProxyModel;

/**
 * 通用Sql映射接口调用实现
 * @author yanan
 *
 */
@Support(Sql.class)
@Register(priority=1,model = ProxyModel.JDK)
public class GeneralMapperInterfaceProxy implements InvokeHandler{
	private SqlSession sqlSession;
	public GeneralMapperInterfaceProxy(SqlSession sqlSession) {
		super();
		this.sqlSession = sqlSession;
	}
	/**
	 * sql的接口进行拦截，才能知道具体调用了接口哪个方法
	 * 对接口方法进行分析，调用具体的sql语句，然后执行sql，什么orm之类的返回出去
	 */
	@Override
	public Object around(MethodHandler methodHandler) {
		//获取SqlSession
		//获取类名和方法并组装为sqlId
		String clzz = methodHandler.getPlugsProxy().getInterfaceClass().getName();
		String method = methodHandler.getMethod().getName();
		String sqlId=clzz+"."+method;
		Object parameter = null; 
		//此部分代码用于判断是否接口参数中使用了@Param注解
		parameter = decodeParamerters(methodHandler);
		//从映射中获取sqlId对应的映射，并通过映射获取SQL的类型，对应增删查改
		BaseMapping mapping = sqlSession.getContext().getWrapper(sqlId);
		if(mapping==null)
			throw new SqlExecuteException("could not found sql mapper id \""+method+"\" at namespace \""+clzz+"\"");
		String op = mapping.getNode().trim().toLowerCase();
		if(op.equals("select")){
			if(ReflectUtils.implementsOf(methodHandler.getMethod().getReturnType(), List.class)){
				return sqlSession.selectList(sqlId, parameter);
			}else{
				return sqlSession.selectOne(sqlId, parameter);
			}
		}else if(op.equals("insert")){
			return sqlSession.insert(sqlId, parameter);
		}else if(op.equals("updaete")){
			return sqlSession.update(sqlId, parameter);
		}else if(op.equals("delete")){
			return sqlSession.delete(sqlId, parameter);
		}
		throw new IllegalAccessError("not supported mapper operate "+op);
	}
	/**
	 * 查询参数是否有注解，有Param注解则将参数组装成Map返回。
	 * @param methodHandler method handler 
	 * @return instance
	 */
	public Object decodeParamerters(MethodHandler methodHandler) {
		Map<String,Object> parameter = new HashMap<>();
		ClassHelper classHelper = ClassHelper.getClassHelper(methodHandler.getPlugsProxy().getInterfaceClass());
		MethodHelper methodHelper = classHelper.getMethodHelper(methodHandler.getMethod());
		Parameter[] actParameters = methodHelper.getParameters();
		for(int i = 0;i<actParameters.length ; i++) {
			ParameterHelper parameterHelper = methodHelper.getParmeterHelper(actParameters[i]);
			Param param = parameterHelper.getAnnotation(Param.class);
			if(param != null) {
				parameter.put(param.value(), methodHandler.getParameters()[i]);
			}else {
				parameter.put(parameterHelper.getParameter().getName(), methodHandler.getParameters()[i]);
			}
		}
		return parameter;
	}
}