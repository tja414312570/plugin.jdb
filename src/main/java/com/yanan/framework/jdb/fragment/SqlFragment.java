package com.yanan.framework.jdb.fragment;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import com.yanan.framework.jdb.JDBContext;
import com.yanan.framework.jdb.entity.BaseMapping;
import com.yanan.framework.jdb.exception.SqlExecuteException;
import com.yanan.framework.jdb.mapper.PreparedSql;
import com.yanan.framework.plugin.annotations.Register;
import com.yanan.utils.reflect.AppClassLoader;
import com.yanan.utils.reflect.ParameterUtils;
import com.yanan.utils.reflect.ReflectUtils;

/**
 * sal片段，用于存储动态sql语句片段
 * 
 * @author yanan
 *
 */
@Register(attribute = "*.root", priority = Integer.MAX_VALUE, signlTon = false)
public abstract class SqlFragment implements FragmentBuilder {
	protected String id;
	protected String resultType;
	protected String parameterType;
	protected BaseMapping baseMapping;
	protected FragmentSet fragemntSet;
	protected Class<?> resultTypeClass;
	protected Class<?> parameterTypeClass;
	protected String scheml;
	
	public String getScheml() {
		return scheml;
	}

	public void setScheml(String scheml) {
		this.scheml = scheml;
	}

	// 参数列表
	protected List<String> arguments = new ArrayList<String>();
	protected JDBContext context;

	public void addParameter(String argument) {
		if (!this.arguments.contains(argument))
			this.arguments.add(argument);
	}

	public List<String> getArguments() {
		return arguments;
	}

	public BaseMapping getBaseMapping() {
		return baseMapping;
	}

	public void setBaseMapping(BaseMapping baseMapping) {
		this.baseMapping = baseMapping;
	}

	public Class<?> getResultTypeClass() {
		return resultTypeClass;
	}

	public void setResultTypeClass(Class<?> resultTypeClass) {
		this.resultTypeClass = resultTypeClass;
	}

	public Class<?> getParameterTypeClass() {
		return parameterTypeClass;
	}

	public void setParameterTypeClass(Class<?> parameterTypeClass) {
		this.parameterTypeClass = parameterTypeClass;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getResultType() {
		return resultType;
	}

	public void setResultType(String resultType) {
		this.resultType = resultType;
	}

	public String getParameterType() {
		return parameterType;
	}

	public void setParameterType(String parameterType) {
		this.parameterType = parameterType;
	}

	public FragmentSet getFragemntSet() {
		return fragemntSet;
	}

	public void setFragemntSet(FragmentSet fragemntSet) {
		this.fragemntSet = fragemntSet;
	}

	public Class<?> matchClassType(String typeString, boolean isParmeter) {
		Class<?> typeClass = null;
		if (typeString != null)
			if (typeString.indexOf(".") == -1) {
				typeString = typeString.trim().toLowerCase();
				if (typeString.equals("string")) {
					typeClass = String.class;
				} else if (typeString.equals("int")) {
					typeClass = int.class;
				} else if (typeString.equals("float")) {
					typeClass = float.class;
				}else if (typeString.equals("long")) {
					typeClass = long.class;
				} else if (typeString.equals("double")) {
					typeClass = double.class;
				} else if (typeString.equals("map")) {
					typeClass = Map.class;
				} else if (typeString.equals("list")) {
					typeClass = List.class;
				} else if (typeString.equals("boolean")) {
					typeClass = boolean.class;
				} else if (typeString.equals("ResultSet")) {
					typeClass = ResultSet.class;
				}   else {
					throw new SqlExecuteException(
							"Unsupport " + (isParmeter ? "parameterType" : "resultType") + " type '" + typeString
									+ "' at mapping file '" + baseMapping.getXmlFile() + "' at id '" + this.id + "'");
				}
			} else {
				try {
					typeClass = Class.forName(typeString);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					throw new SqlExecuteException(
							"Unsupport " + (isParmeter ? "parameterType" : "resultType") + " type '" + typeString
									+ "' at mapping file '" + baseMapping.getXmlFile() + "' at id '" + this.id + "'");
				}
			}
		return typeClass;
	}

	@Override
	public void build(Object wrapper) {
		this.baseMapping = (BaseMapping) wrapper;
		this.id = this.baseMapping.getId();
		this.parameterType = this.baseMapping.getParameterType();
		this.id = this.baseMapping.getId();
		this.parameterType = this.baseMapping.getParameterType();
		this.parameterTypeClass = matchClassType(this.parameterType, true);
		this.resultType = this.baseMapping.getResultType();
		this.resultTypeClass = matchClassType(this.resultType, false);
		if (this.context.getDataSource() == null)
			throw new SqlExecuteException("could not found dataSource '" + this.baseMapping.getWrapperMapping()
					+ "' at mapping file '" + baseMapping.getXmlFile() + "' at id '" + this.id + "'");
	}

	public JDBContext getContext() {
		return context;
	}

	@Override
	public PreparedFragment prepared(Object objects) {
		this.fragemntSet.setSqlFragment(this);
		return null;
	}

	public void buldListParameter(List<String> variables, List<Object> arguments2, List<?> object) {
		for (int i = 0; i < variables.size(); i++)
			arguments2.add(i < object.size() ? object.get(i) : null);
	}

	public void buildMapParameter(List<String> variables, List<Object> arguments, Map<?, ?> object) {
		Iterator<String> iterator = variables.iterator();
		while (iterator.hasNext()) {
			String key = iterator.next();
			arguments.add(object.get(key));
		}
	}

	public List<Object> preparedParameter(Object... parameter) {
		List<Object> arguments = new ArrayList<Object>();
		if (parameter != null) {
			if (parameter.length > 1) {
				for (int i = 0; i < parameter.length; i++)
					arguments.add(parameter[i]);
			} else if (parameter.length == 1) {
				Object object = parameter[0];
				if (ReflectUtils.implementsOf(object.getClass(), Map.class)) {
					this.buildMapParameter(this.arguments, arguments, (Map<?, ?>) object);
				} else if (ReflectUtils.implementsOf(object.getClass(), List.class)) {
					this.buldListParameter(this.arguments, arguments, (List<?>) object);
				} else {
					arguments.add(object);
				}
			}
		}
		return arguments;
	}

	public List<Object> preparedParameter(List<String> variables, Object... parameter) {
		List<Object> arguments = new ArrayList<Object>();
		if (parameter != null&&variables.size()>0) {
			// 需要参数的数量是否与传入参数的数量相同
			if (parameter.length > 1) {
				if (SqlFragment.removeDuplicate(variables).size() <= parameter.length)
					for (int i = 0; i < variables.size(); i++){
						int pos = this.getArguments().indexOf(variables.get(i));
						arguments.add(pos>=parameter.length?null:parameter[pos]);
					}
				else
					throw new SqlExecuteException("failed to prepared parameter \"" + variables
							+ "\"because the need parameter \"" + variables.size() + "\" get the parameter \""
							+ parameter.length + "\"! at mapping file '" + this.baseMapping.getXmlFile() + "' at id '"
							+ this.baseMapping.getId() + "'");
			} else if (parameter.length == 1) {
				Object object = parameter[0];
				if(object==null){
					arguments.add(object);
				}else if (ReflectUtils.implementsOf(object.getClass(), Map.class)) {
					this.buildMapParameter(variables, arguments, (Map<?, ?>) object);
				} else if (ReflectUtils.implementsOf(object.getClass(), List.class)) {
					this.buldListParameter(variables, arguments, (List<?>) object);
				} else if (ParameterUtils.isBaseType(object.getClass())) {
					if (SqlFragment.removeDuplicate(variables).size() == 1)
						for(int i = 0 ;i<variables.size();i++)
							arguments.add(object);
					else
						throw new SqlExecuteException("failed to prepared parameter \"" + variables
								+ "\"because the need parameter \"" + variables.size() + "\" get the parameter \""
								+ parameter.length + "\"! at mapping file '" + this.baseMapping.getXmlFile()
								+ "' at id '" + this.baseMapping.getId() + "'");
				} else {
					AppClassLoader loader = new AppClassLoader(object);
					for (int i = 0; i < variables.size(); i++)
						try {
							arguments.add(loader.get(variables.get(i)));
						} catch (NoSuchMethodException | SecurityException | IllegalAccessException
								| IllegalArgumentException | InvocationTargetException e) {
							throw new SqlExecuteException("failed to get need parameter \"" + variables.get(i)
									+ "\" at parameterType " + loader.getLoadedClass() + "at mapping file '"
									+ this.baseMapping.getXmlFile() + "' at id '" + this.baseMapping.getId() + "'", e);
						}
				}
			}
		}
		return arguments;
	}

	public static <T> List<T> removeDuplicate(List<T> list) {
		LinkedHashSet<T> set = new LinkedHashSet<T>(list.size());
		set.addAll(list);
		List<T> norepetList = new ArrayList<T>(list.size());
		norepetList.addAll(set);
		return norepetList;
	}

	public abstract PreparedSql getPreparedSql(Object parameter);

	public void setContext(JDBContext context) {
		this.context = context;
		
	}
}